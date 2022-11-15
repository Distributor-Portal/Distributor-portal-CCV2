package com.energizer.core.datafeed.processor.product;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.google.common.collect.Iterables;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
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

import java.io.File;
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


/**
 * @author M1030106
 *
 */
public class EnergizerCMIRMonitorJob extends AbstractJobPerformable<EnergizerCronJobModel>
{
	@Resource
	private EnergizerProductService energizerProductService;

	@Resource
	private ModelService modelService;
	//private List<EnergizerCMIRModel> energizerCMIRModels = energizerProductService;
	@Resource
	private ConfigurationService configurationService;

	@Resource
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;

	@Value("{$sharedFolderPath}")
	private String path;

	@Resource
	private EmailService emailService;

	private CronJobService cronJobService;

	/**
	 * @return the cronJobService
	 */
	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	/**
	 * @param cronJobService
	 *           the cronJobService to set
	 */
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	// Added by Soma - To abort those cronjobs during run time that take more time for processing.
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


	@SuppressWarnings("unused")
	@Override
	public synchronized PerformResult perform(final EnergizerCronJobModel cronjob)
	{

		LOG.info("************************ PROCESSING START FOR THIS CRONJOB  ***************************");
		LOG.info("Starting CMIR Monitor Job for " + cronjob.getRegion() + " !!");

		final Long jobStartTime = System.currentTimeMillis();

		List<EnergizerCMIRModel> cmirListFromDB = null; //= energizerProductService.getAllEnergizerCMIRList();
		List<EnergizerCMIRModel> cmirListFromDB_buff = null; // = energizerProductService.getAllEnergizerCMIRList();
		//final List<EnergizerCMIRModel> cmirFinalList = new ArrayList<EnergizerCMIRModel>();
		Set<EnergizerCMIRModel> cmirFinalSet = null;
		cmirFinalSet = new HashSet<EnergizerCMIRModel>();
		Set<EnergizerCMIRModel> cmirSetFromDB = null;
		cmirSetFromDB = new HashSet<EnergizerCMIRModel>();
		//Setting cronjob
		csvUtils.setCronjob(cronjob);
		String siteId = StringUtils.EMPTY;
		final int wesellCSVFilesCount = Integer
				.parseInt(configurationService.getConfiguration().getString("wesell.cmir.csv.files.count"));
		final int wesellSplitFilesCount = Integer
				.parseInt(configurationService.getConfiguration().getString("wesell.cmir.split.cmirSetFromDB.count"));
		try
		{
			// Added to fetch Site specific CMIRs from the hybris DB
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
			cmirListFromDB_buff = energizerProductService.getAllEnergizerCMIRListBySiteIdAndStatus(siteId, cronjob.getRegion(),
					true);
			cmirSetFromDB.addAll(cmirListFromDB);

			if (cmirListFromDB != null && cmirListFromDB_buff != null)
			{
				LOG.info("Total CMIRs in the DB : " + cmirListFromDB_buff.size()); //Total active CMIRs in the DB
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
			LOG.info(
					"*********************************** NOTHING TO PROCESS FOR THIS CRONJOB  *****************************************");
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
		final AbstractEnergizerCSVProcessor energizerCSVProcessor = (AbstractEnergizerCSVProcessor) Registry.getApplicationContext()
				.getBean(type);
		try
		{


			CloudBlobContainer cloudBlobContainer = null;
			cloudBlobContainer = energizerWindowsAzureBlobStorageStrategy.getBlobContainer();
			/* Added for EMEA cronjob import */

			final CloudBlobDirectory blobDirectory = energizerCSVProcessor.getBlobDirectoryForFeedType(cronjob.getType());

			final Integer filesCount = filesCount(blobDirectory);

			//final List<File> files = csvUtils.getFilesForFeedType("energizerCMIRCSVProcessor");
			//LOG.info("LOADING FILES FROM CMIR folder FOR MONITORING" + files);


			if (blobDirectory.listBlobs() == null || filesCount == 0)
			{

				LOG.info("NO FILES FOUND, NOTHING TO PROCESS FOR THIS CRONJOB");
				return new PerformResult(CronJobResult.FILE_NOT_FOUND, CronJobStatus.FINISHED);
			}
			else if (null != blobDirectory.listBlobs()  && filesCount != 0 && cronjob.getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL)
					&& filesCount != wesellCSVFilesCount)
			{
				LOG.info("THERE ARE NOT EXACTLY '" + wesellCSVFilesCount + "' FILES FOUND FOR WESELL, SO IGNORING THIS CRONJOB");
				return new PerformResult(CronJobResult.FAILURE, CronJobStatus.FINISHED);
			}
			else
			{
				final Map<String, EnergizerCMIRModel> cmirMapFromDB = new HashMap<String, EnergizerCMIRModel>();
				int nullCounter = 0;
				for (final EnergizerCMIRModel cmir : cmirListFromDB_buff)
				{
					if (null != cmir.getErpMaterialId() && null != cmir.getB2bUnit())
					{
						cmirMapFromDB.put(cmir.getErpMaterialId().trim().concat("_").concat(cmir.getB2bUnit().getUid().trim()), cmir);
					}
					else
					{
						nullCounter++;
					}
				}
				if (nullCounter > 0)
				{
					LOG.info("Either the erpMaterialID/b2bUnit is NULL for '" + nullCounter
							+ "' CMIR records in DB, so ignoring them for comparison ...");
				}
				LOG.info("CMIR Map from DB size : " + cmirMapFromDB.size());
				try
				{
					Iterable<CSVRecord> csvRecords = null;
					final Long cmirFinalSetStartTime = System.currentTimeMillis();
					for (final ListBlobItem blobItem : blobDirectory.listBlobs()) {

						final String subfullFilePath = blobItem.getStorageUri().getPrimaryUri().getPath();
						final String fullFilePath = subfullFilePath.substring(8);
						final String fileName = org.apache.commons.lang3.StringUtils.substringAfterLast(fullFilePath, "/");


						if (!(dummyFileName.equalsIgnoreCase(fileName))) {
							final Long fileProcessingStartTime = System.currentTimeMillis();
							CloudBlockBlob blob2;
							blob2 = cloudBlobContainer.getBlockBlobReference(fullFilePath);
							csvRecords = energizerCSVProcessor.parse(fullFilePath);
							//csvRecords = csvUtils.parse(f);
							if (null != cmirListFromDB_buff && null != csvRecords) {
								final Long preparedSetStartTime = System.currentTimeMillis();
								//final Set<EnergizerCMIRModel> preparedSet = checkUpdate(cmirListFromDB_buff, csvRecords, cronjob);

								final Set<EnergizerCMIRModel> preparedSet = checkUpdate(cmirMapFromDB, csvRecords, cronjob);
								if (null != preparedSet && preparedSet.size() > 0) {
									LOG.info("Matching records for file : '" + fileName + "' is : " + preparedSet.size());
									cmirFinalSet.addAll(preparedSet);
									preparedSet.clear();
									final Long preparedSetEndTime = System.currentTimeMillis();
									LOG.info("Matching comparison completed for file : " + fileName + ", total time taken : "
											+ (preparedSetEndTime - preparedSetStartTime) + " milliseconds, "
											+ (preparedSetEndTime - preparedSetStartTime) / 1000 + " seconds");
								}
							}else {
								LOG.info("No Matching records for file : '" + fileName );
							}
						}
						// If the cronjob abort is requested, then perform clean up and return the PerformResult
						this.modelService.refresh(cronjob);
						if (null != cronjob.getRequestAbort() && BooleanUtils.isTrue(cronjob.getRequestAbort())) {
							LOG.info(cronjob.getRegion() + " : CMIR Monitor Job is ABORTED while performing ...");
							//abort the job
							cronJobService.requestAbortCronJob(cronjob);
							return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
						}
					}
						final Long cmirFinalSetEndTime = System.currentTimeMillis();
						LOG.info("Total time taken for preparing set of total matching records : "
								+ (cmirFinalSetEndTime - cmirFinalSetStartTime) + " milliseconds, "
								+ (cmirFinalSetEndTime - cmirFinalSetStartTime) / 1000 + " seconds ...");

						final Long cmirSetupInactiveStartTime = System.currentTimeMillis();
						LOG.info("Total matching records in the DB vs. CSV : " + (null != cmirFinalSet ? cmirFinalSet.size() : 0));
						// Valid records(cmirFinalSet) are removed from the total list (cmirSetFromDB) to filter out the invalid records that are to be set inactive.
						cmirSetFromDB.removeAll(cmirFinalSet);

						if (null != cmirSetFromDB && cmirSetFromDB.size() > 0 && cmirFinalSet.size() > 0)
						{
							LOG.info("Total non-matching CMIR records(overall) to be set false : " + cmirSetFromDB.size());
							int cmirSetInActive = 0;
							int priceRowsSetInActive = 0;
							final List<EnergizerCMIRModel> cmirs = new ArrayList<EnergizerCMIRModel>();
							// cmirSetFromDB has only the invalid CMIR records of all the files now -> Has to be set inactive.
							for (final EnergizerCMIRModel cmir : cmirSetFromDB)
							{
								if (cmir.getIsActive() == true)
								{
									/*
									 * LOG.debug("cmirs getting disabled for models having erpmaterialid -" +
									 * cmir.getErpMaterialId() + "\tcust_matid - " + cmir.getCustomerMaterialId() + "\tb2bunit - " +
									 * cmir.getB2bUnit().getUid());
									 */
									cmir.setIsActive(false);
									cmirs.add(cmir);
									//modelService.save(cmir);
									/*
									 * final List<EnergizerPriceRowModel> energizerPriceRow = energizerProductService
									 * .getAllEnergizerPriceRowForB2BUnit(cmir.getErpMaterialId(), cmir.getB2bUnit().getUid()); if
									 * (energizerPriceRow != null) {
									 *
									 * LOG.debug("Number of price rows to be modified for " + cmir.getErpMaterialId() + "=" +
									 * energizerPriceRow.size());
									 *
									 * for (final EnergizerPriceRowModel priceRow : energizerPriceRow) {
									 * priceRow.setIsActive(false); modelService.save(priceRow); } }
									 */
									/* c.getErpMaterialId(). */
									cmirSetInActive = cmirSetInActive + 1;
								}
							}

							modelService.saveAll(cmirs); // Save all inactive CMIRs

							final List<EnergizerPriceRowModel> energizerPriceRows = new ArrayList<>();

							if(cronjob.getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL) && cmirSetFromDB.size() > 0 ){
								//To Avoid Flexible search exception on querying for WESELL
								LOG.info("Inside WESELL Split Clause" + cmirSetFromDB.size());
								List<Set<EnergizerCMIRModel>> splitSets = split(cmirSetFromDB, wesellSplitFilesCount);
								for (final Set<EnergizerCMIRModel> splitSet : splitSets)
								{
									LOG.info("Querying Size " + splitSet.size());
									List<EnergizerPriceRowModel> energizerPriceRow = energizerProductService
											.getActiveEnergizerPriceRowForCMIRModelSet(splitSet);
									energizerPriceRows.addAll(energizerPriceRow);
									//energizerPriceRow.clear();
								}
						    }else{
								LOG.info("Querying Size " + cmirSetFromDB.size());
								final List<EnergizerPriceRowModel> energizerPriceRow = energizerProductService
										.getActiveEnergizerPriceRowForCMIRModelSet(cmirSetFromDB);
								energizerPriceRows.addAll(energizerPriceRow);
							}


							final List<EnergizerPriceRowModel> priceRows = new ArrayList<EnergizerPriceRowModel>();

							if (null != energizerPriceRows && energizerPriceRows.size() > 0)
							{
								LOG.info("Total non-matching PriceRow records(overall) to be set false : " + energizerPriceRows.size());
								for (final EnergizerPriceRowModel priceRow : energizerPriceRows)
								{
									priceRow.setIsActive(false);
									priceRows.add(priceRow);
									//modelService.save(priceRow);
									priceRowsSetInActive = priceRowsSetInActive + 1;
								}
								modelService.saveAll(priceRows); // Save all inactive price rows
							}

							LOG.info("Total non-matching CMIRs in the DB setup inactive now : " + cmirSetInActive);
							LOG.info("Total non-matching Price Rows in the DB setup inactive now : " + priceRowsSetInActive);
							LOG.info("CMIR Monitor Job is COMPLETED for " + cronjob.getRegion() + " !!");

							//csvUtils.getReader().close();
							sendMail(cmirSetFromDB.toString(), cronjob.getEmailAddress());

							final Long cmirSetupInactiveEndTime = System.currentTimeMillis();
							LOG.info("Time taken for setting up CMIRs & PriceRows inactive : "
									+ (cmirSetupInactiveEndTime - cmirSetupInactiveStartTime) + " milliseconds, "
									+ (cmirSetupInactiveEndTime - cmirSetupInactiveStartTime) / 1000 + " seconds ...");
						}
						else
						{
							LOG.info("nothing to update");
							//csvUtils.getReader().close();
						}

				}
				catch (final Exception e)
				{
					LOG.error("EXC CAUSED BY : " + e + e.getMessage() + e.getCause());
					e.printStackTrace();
					//csvUtils.getReader().close();
				}
			}
		}
		catch (final Exception e)
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

	/**
	 * This method compares the CMIR records of the DB with the CMIR records of the CSV file and checks if both matches.
	 * If the matching record is found, the record is added to the preparedSet and returned back to the caller.
	 *
	 * @param cmirList
	 *           List<EnergizerCMIRModel>
	 * @param csvRecords
	 *           Iterable<CSVRecord>
	 * @param cronjob
	 *           EnergizerCronJobModel
	 * @return
	 */
	/*
	 * public Set<EnergizerCMIRModel> checkUpdate(final List<EnergizerCMIRModel> cmirList, final Iterable<CSVRecord>
	 * csvRecords, final EnergizerCronJobModel cronjob)
	 */
	public Set<EnergizerCMIRModel> checkUpdate(final Map<String, EnergizerCMIRModel> cmirMapFromDB,
			final Iterable<CSVRecord> csvRecords, final EnergizerCronJobModel cronjob)
	{
		Map<String, String> csvValuesMap = null;
		final Set<EnergizerCMIRModel> preparedSet = new HashSet<EnergizerCMIRModel>();

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
					if (null != erpMaterialId && !StringUtils.isEmpty(erpMaterialId) && null != b2bUnitId
							&& !StringUtils.isEmpty(b2bUnitId))
					{
						mapKey = erpMaterialId.toString().trim().concat("_").concat(b2bUnitId.toString().trim());
					}
					/* for (final EnergizerCMIRModel cmirModel : cmirList) */
					if (null != cmirMapFromDB && null != cmirMapFromDB.get(mapKey)
							&& cmirMapFromDB.get(mapKey).getErpMaterialId().equals(erpMaterialId)
							&& cmirMapFromDB.get(mapKey).getB2bUnit().getUid().equals(b2bUnitId))
					{
						/*
						 * if (null != cmirModel.getErpMaterialId() &&
						 * cmirModel.getErpMaterialId().equals(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID)) &&
						 * (null != cmirModel.getB2bUnit() &&
						 * cmirModel.getB2bUnit().getUid().equals(csvValuesMap.get(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID
						 * ))) )
						 */
						//checking whether the corresponding csv file row attributes and model attributes matches or not
						/* { */
						//preparedSet.add(cmirModel);//here set is taken to avoid the duplication of models satisfying the condition within if block.

						preparedSet.add(cmirMapFromDB.get(mapKey));
						addedToPreparedSet = addedToPreparedSet + 1;
						//break;
						/*
						 * LOG.info("cmir record exists in both back end and excel file for erpmat id" +
						 * cmirModel.getErpMaterialId() + "\tb2bunit id-" + cmirModel.getB2bUnit() + "\tcustomer_matid" +
						 * cmirModel.getCustomerMaterialId());
						 */
						/* } */
					}
					if (addedToPreparedSet != 0 && addedToPreparedSet % 3000 == 0)
					{
						LOG.info("Added " + addedToPreparedSet + " cmirModels to the preparedSet so far !!");
					}
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
				"" + cmirsList.toString(), null);
		LOG.info("sending mail for list of cmirs to be deleted");
		emailService.send(message);
		LOG.info("mail send");
		return;
	}

	public int filesCount( CloudBlobDirectory blobDirectory) throws URISyntaxException, StorageException {

		int counter = 0;
		for (final ListBlobItem blobItem : blobDirectory.listBlobs()) {
				counter++;
		}
		return counter;
	}

	public static <T> List<Set<T>> split(Set<T> original, int count) {
		// Create a list of sets to return.
		ArrayList<Set<T>> result = new ArrayList<Set<T>>(count);

		// Create an iterator for the original set.
		Iterator<T> it = original.iterator();

		// Calculate the required number of elements for each set.
		int each = original.size() / count;

		// Create each new set.
		for (int i = 0; i < count; i++) {
			HashSet<T> s = new HashSet<T>(original.size() / count + 1);
			result.add(s);
			for (int j = 0; j < each && it.hasNext(); j++) {
				s.add(it.next());
			}
		}
		return result;
	}
}
