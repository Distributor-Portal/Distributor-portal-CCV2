package com.energizer.core.datafeed.processor.product;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.google.common.collect.Iterables;
import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.net.URISyntaxException;
import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.services.product.EnergizerProductService;

// Azure SDK v12 imports
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;

public class EnergizerCMIRMonitorJob extends AbstractJobPerformable<EnergizerCronJobModel>
{
	@Resource
	private EnergizerProductService energizerProductService;

	@Resource
	private ModelService modelService;

	@Resource
	private ConfigurationService configurationService;

	@Resource
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;

	@Value("{$sharedFolderPath}")
	private String path;

	@Resource
	private EmailService emailService;

	private CronJobService cronJobService;

	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	@Override
	public boolean isAbortable()
	{
		return true;
	}

	private static final Logger LOG = Logger.getLogger(EnergizerCMIRMonitorJob.class);

	private static final String PERSONALCAREEMEA_PRODUCTCATALOG = "personalCareEMEAProductCatalog";
	private static final String PERSONALCARE_PRODUCTCATALOG = "personalCareProductCatalog";

	public static final String dummyFileName = Config.getParameter("azure.blob.storage.dummy.file.name");

	@Resource(name = "energizerCMIRCSVProcessor")
	private AbstractEnergizerCSVProcessor csvUtils;

	@Override
	public synchronized PerformResult perform(final EnergizerCronJobModel cronjob)
	{
		LOG.info("************************ PROCESSING START FOR THIS CRONJOB  ***************************");
		LOG.info("Starting CMIR Monitor Job for " + cronjob.getRegion() + " !!");

		final Long jobStartTime = System.currentTimeMillis();

		List<EnergizerCMIRModel> cmirListFromDB = null;
		List<EnergizerCMIRModel> cmirListFromDB_buff = null;
		Set<EnergizerCMIRModel> cmirFinalSet = new HashSet<>();
		Set<EnergizerCMIRModel> cmirSetFromDB = new HashSet<>();
		csvUtils.setCronjob(cronjob);
		String siteId = StringUtils.EMPTY;
		final int wesellCSVFilesCount = Integer.parseInt(configurationService.getConfiguration().getString("wesell.cmir.csv.files.count"));
		final int wesellSplitFilesCount = Integer.parseInt(configurationService.getConfiguration().getString("wesell.cmir.split.cmirSetFromDB.count"));
		try
		{
			final String SITE_PERSONALCARE = configurationService.getConfiguration().getString("site.personalCare");
			final String SITE_PERSONALCAREEMEA = configurationService.getConfiguration().getString("site.personalCareEMEA");
			if (cronjob.getCatalogName().equalsIgnoreCase(PERSONALCAREEMEA_PRODUCTCATALOG))
			{
				siteId = SITE_PERSONALCAREEMEA;
			}
			else if (cronjob.getCatalogName().equalsIgnoreCase(PERSONALCARE_PRODUCTCATALOG))
			{
				siteId = SITE_PERSONALCARE;
			}
			cmirListFromDB = energizerProductService.getAllEnergizerCMIRListBySiteIdAndStatus(siteId, cronjob.getRegion(), true);
			cmirListFromDB_buff = energizerProductService.getAllEnergizerCMIRListBySiteIdAndStatus(siteId, cronjob.getRegion(), true);
			cmirSetFromDB.addAll(cmirListFromDB);

			if (cmirListFromDB != null && cmirListFromDB_buff != null)
			{
				LOG.info("Total CMIRs in the DB : " + cmirListFromDB_buff.size());
			}
			else
			{
				LOG.error("No CMIRs fetched from db, aborting cronjob ...");
				return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while loading data from db for this site '" + siteId + "' " + e.getMessage());
		}
		final String type = cronjob.getType();
		if (type == null)
		{
			LOG.info("There is no Type defined for the job " + cronjob.getCode());
			LOG.info("*********************************** NOTHING TO PROCESS FOR THIS CRONJOB  *****************************************");
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
		final AbstractEnergizerCSVProcessor energizerCSVProcessor = (AbstractEnergizerCSVProcessor) Registry.getApplicationContext()
				.getBean(type);
		try
		{
			BlobContainerClient container = energizerWindowsAzureBlobStorageStrategy.getBlobContainer();
			String prefix = cronjob.getPath() + AbstractEnergizerCSVProcessor.fileSeperator + cronjob.getType() +
					AbstractEnergizerCSVProcessor.fileSeperator + AbstractEnergizerCSVProcessor.toProcess + AbstractEnergizerCSVProcessor.fileSeperator;

			List<String> blobNames = new ArrayList<>();
			for (BlobItem blobItem : container.listBlobsByHierarchy(prefix))
			{
				if (!blobItem.isPrefix())
				{
					blobNames.add(blobItem.getName());
				}
			}

			final Integer filesCount = blobNames.size();

			if (filesCount == 0)
			{
				LOG.info("NO FILES FOUND, NOTHING TO PROCESS FOR THIS CRONJOB");
				return new PerformResult(CronJobResult.FILE_NOT_FOUND, CronJobStatus.FINISHED);
			}
			else if (filesCount != 0 && cronjob.getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL)
					&& filesCount != wesellCSVFilesCount)
			{
				LOG.info("THERE ARE NOT EXACTLY '" + wesellCSVFilesCount + "' FILES FOUND FOR WESELL, SO IGNORING THIS CRONJOB");
				return new PerformResult(CronJobResult.FAILURE, CronJobStatus.FINISHED);
			}
			else
			{
				final Map<String, EnergizerCMIRModel> cmirMapFromDB = new HashMap<>();
				int nullCounter = 0;
				for (final EnergizerCMIRModel cmir : cmirListFromDB_buff)
				{
					if(null != cmir.getErpMaterialId() && null != cmir.getB2bUnit()) {
						cmirMapFromDB.put(cmir.getErpMaterialId().trim().concat("_").concat(cmir.getB2bUnit().getUid().trim()), cmir);
					} else {
						nullCounter++;
					}
				}
				if (nullCounter > 0) {
					LOG.info("Either the erpMaterialID/b2bUnit is NULL for '" + nullCounter + "' CMIR records in DB, so ignoring them for comparison...");
				}
				LOG.info("CMIR Map from DB size : " + cmirMapFromDB.size());
				try
				{
					final Long cmirFinalSetStartTime = System.currentTimeMillis();
					for (String blobName : blobNames)
					{
						BlobClient blobClient = container.getBlobClient(blobName);
						// Download blob content as text
						byte[] blobBytes = blobClient.downloadContent().toBytes();
						String blobText = new String(blobBytes);

						Iterable<CSVRecord> csvRecords = energizerCSVProcessor.parse(blobName);

						// Your logic to process csvRecords and compare with cmirMapFromDB
					}
				}
				catch (final Exception e)
				{
					LOG.error("EXC CAUSED BY : " + e + e.getMessage() + e.getCause());
					e.printStackTrace();
				}
			}
		}
		catch (final BlobStorageException e)
		{
			LOG.error("ERROR OCCURED WHILE LOADING FILES : " + "\t\t" + e.getMessage());
			e.printStackTrace();
		}

		final Long jobEndTime = System.currentTimeMillis();
		LOG.info("Time taken for CMIR Monitor Job : " + (jobEndTime - jobStartTime) + " milliseconds, "
				+ (jobEndTime - jobStartTime) / 1000 + " seconds ...");

		LOG.info("************************ PROCESSING END FOR THIS CRONJOB  ***************************");
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	public Set<EnergizerCMIRModel> checkUpdate(final Map<String, EnergizerCMIRModel> cmirMapFromDB,
											   final Iterable<CSVRecord> csvRecords, final EnergizerCronJobModel cronjob)
	{
		Map<String, String> csvValuesMap = null;
		final Set<EnergizerCMIRModel> preparedSet = new HashSet<>();

		Integer addedToPreparedSet = 0;
		String erpMaterialId = null;
		String b2bUnitId = null;
		String mapKey = null;
		try
		{
			if (null != csvRecords)
			{
				for (final CSVRecord record : csvRecords)
				{
					csvValuesMap = record.toMap();
					erpMaterialId = csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).toString().trim();
					b2bUnitId = csvValuesMap.get(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID).toString().trim();
					// Your logic here
				}
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occurred : " + e.getMessage());
			e.printStackTrace();
		}
		return preparedSet;
	}

	void sendMail(final String cmirsList, final String toEmail)
	{
		final EmailAddressModel toaddress = emailService.getOrCreateEmailAddressForEmail(toEmail, "CMIR Monitor Job");
		final EmailAddressModel fromaddress = emailService.getOrCreateEmailAddressForEmail(configurationService.getConfiguration()
				.getString("cronjobs.from.email", Config.getParameter("fromEmailAddress.orderEmailSender")), "");
		final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toaddress), null, null, fromaddress, "",
				"(" + configurationService.getConfiguration().getString("mail.enviorment") + ")" + "list of cmirs to be deactivated",
				"" + cmirsList, null);
		LOG.info("sending mail for list of cmirs to be deleted");
		emailService.send(message);
		LOG.info("mail send");
	}

	public int filesCount(List<String> blobNames)
	{
		return blobNames.size();
	}

	public static <T> List<Set<T>> split(Set<T> original, int count)
	{
		ArrayList<Set<T>> result = new ArrayList<>(count);
		Iterator<T> it = original.iterator();
		int each = original.size() / count;
		for (int i = 0; i < count; i++)
		{
			HashSet<T> s = new HashSet<>(original.size() / count + 1);
			result.add(s);
			for (int j = 0; j < each && it.hasNext(); j++)
			{
				s.add(it.next());
			}
		}
		return result;
	}
}
