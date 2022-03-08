/**
 *
 */
package com.energizer.core.datafeed;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.energizer.core.datafeed.processor.product.EnergizerProduct2CategoryRelationCSVProcessor;
import com.energizer.core.model.EnergizerCronJobModel;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.ListBlobItem;


/**
 * @author M9005674
 *
 */
public class EnergizerCSVFeedCronJob extends AbstractJobPerformable<EnergizerCronJobModel>
{

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel )
	 */

	private static final Logger LOG = Logger.getLogger(EnergizerCSVFeedCronJob.class);

	private static DecimalFormat df2 = new DecimalFormat("#.##");

	@Resource
	EmailService emailService;

	private CronJobService cronJobService;

	@Resource
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;

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

	@Resource
	private ConfigurationService configurationService;

	@Override
	public synchronized PerformResult perform(final EnergizerCronJobModel cronjob)
	{
		LOG.info("************************ PROCESSING START FOR THIS CRONJOB  ***************************");

		final Long jobStartTime = System.currentTimeMillis();
		LOG.info("Before processing this cronjob : " + jobStartTime + " milliseconds !!");

		List<EnergizerCSVFeedError> errors = new ArrayList<EnergizerCSVFeedError>();
		List<EnergizerCSVFeedError> techfeedErrors = new ArrayList<EnergizerCSVFeedError>();
		List<EnergizerCSVFeedError> busfeedErrors = new ArrayList<EnergizerCSVFeedError>();
		PerformResult performResult = null;
		final List<String> emailAddress = new ArrayList<String>();
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
		/* Added for EMEA cronjob import */
		energizerCSVProcessor.setCronjob(cronjob);
		/* EMEA End */

		final List<File> files = null;

		System.out.println("files-new--chages-> ");

		try
		{
			// get container and iterate blob list

			final CloudBlobContainer cloudBlobContainer = energizerWindowsAzureBlobStorageStrategy.getBlobContainer();
			//final String toProcessDirectoryPath = this.getCronjob().getPath() + fileSeperator + type + fileSeperator + toProcess;cloudBlobContainer.getDirectoryReference(type);



			final CloudBlobDirectory blobDirectory = energizerCSVProcessor.getBlobDirectoryForFeedType(type);

			//files = energizerCSVProcessor.getFilesForFeedType(type);

			/*
			 * if (null == files || files.size() == 0) { LOG.info(
			 * "************************* NO FILES FOUND, NOTHING TO PROCESS FOR THIS CRONJOB  ****************************"
			 * ); return new PerformResult(CronJobResult.FILE_NOT_FOUND, CronJobStatus.FINISHED); } else if (null != files
			 * && files.size() > 0 && (cronjob.getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL) && files.size()
			 * > wesellCSVFilesCount)) { LOG.info("Total files : " + files.size()); //LOG.info("MORE THAN '" +
			 * wesellCSVFilesCount + "' FILES FOUND TO PROCESS FOR WESELL, SO IGNORING THIS CRONJOB ..."); return new
			 * PerformResult(CronJobResult.FAILURE, CronJobStatus.FINISHED); }
			 */

			Boolean exceptionOccured = false;
			if (null != cronjob.getEmailAddress())
			{
				emailAddress.add(cronjob.getEmailAddress());
			}
			energizerCSVProcessor.flush(); /* This is to flush the buffer of existing errorList and message as well */

			for (final ListBlobItem blobItem : blobDirectory.listBlobs())
			{
				final String subfullFilePath = blobItem.getStorageUri().getPrimaryUri().getPath();
				final String fullFilePath = subfullFilePath.substring(8);
				final String fileName = StringUtils.substringAfterLast(fullFilePath, "/");
				//blob2.downloadText()

				final Long fileProcessingStartTime = System.currentTimeMillis();

				Iterable<CSVRecord> csvRecords;


				//csvRecords = energizerCSVProcessor.parse(file);
				csvRecords = energizerCSVProcessor.parse(fullFilePath);

				LOG.info("************** PROCESSING START FOR THIS FILE  '" + fileName + "' ***************");
				LOG.info("Before processing this file : " + fileProcessingStartTime + " milliseconds !!");
				errors = energizerCSVProcessor.process(csvRecords, cronjob.getCatalogName(), cronjob);
				exceptionOccured = (errors.size() != 0) ? true : false;
				//	energizerCSVProcessor.setMasterDataStream(new DataInputStream(new FileInputStream(fileName)));
				final List<EmailAttachmentModel> emailAttachmentList = new ArrayList<EmailAttachmentModel>();
				/*
				 * final EmailAttachmentModel attachmentModel = emailService.createEmailAttachment(
				 * energizerCSVProcessor.getMasterDataStream(), StringUtils.replace(file.getName().toLowerCase(), ".csv",
				 * "_" + new Date().getTime() + "." + de.hybris.platform.impex.constants.ImpExConstants.File.EXTENSION_CSV)
				 * .toLowerCase(), de.hybris.platform.impex.constants.ImpExConstants.File.MIME_TYPE_CSV);
				 * emailAttachmentList.add(attachmentModel);
				 */
				if (cronjob.getTechnicalEmailAddress().isEmpty())
				{
					cronjob.setTechnicalEmailAddress(emailAddress);
				}
				if (cronjob.getBusinessEmailAddress().isEmpty())
				{
					cronjob.setBusinessEmailAddress(emailAddress);
				}
				techfeedErrors = energizerCSVProcessor.getTechnicalFeedErrors();
				if (!techfeedErrors.isEmpty())
				{
					energizerCSVProcessor.setRecordFailed(energizerCSVProcessor.getTechRecordError());
					energizerCSVProcessor.mailErrors(cronjob, techfeedErrors, cronjob.getTechnicalEmailAddress(), emailAttachmentList);
				}
				busfeedErrors = energizerCSVProcessor.getBusinessFeedErrors();
				if (!busfeedErrors.isEmpty())
				{
					energizerCSVProcessor.setRecordFailed(energizerCSVProcessor.getBusRecordError());
					energizerCSVProcessor.mailErrors(cronjob, busfeedErrors, cronjob.getBusinessEmailAddress(), emailAttachmentList);
				}
				energizerCSVProcessor.setTotalRecords(0);
				energizerCSVProcessor.setRecordFailed(0);
				energizerCSVProcessor.setRecordSucceeded(0);
				energizerCSVProcessor.setBusRecordError(0);
				energizerCSVProcessor.setTechRecordError(0);
				emailAttachmentList.clear();
				if ((techfeedErrors != null && techfeedErrors.size() > 0) || (busfeedErrors != null && busfeedErrors.size() > 0))
				{
					if (!(energizerCSVProcessor instanceof EnergizerProduct2CategoryRelationCSVProcessor))
					{
						//energizerCSVProcessor.cleanup(type, file, cronjob, true);
					}
					energizerCSVProcessor.flush();
				}
				else
				{
					if (!(energizerCSVProcessor instanceof EnergizerProduct2CategoryRelationCSVProcessor))
					{
						//energizerCSVProcessor.cleanup(type, file, cronjob, false);
					}
					energizerCSVProcessor.flush();
				}

				// If the cronjob abort is requested, then perform clean up and return the PerformResult
				this.modelService.refresh(cronjob);
				if (clearAbortRequestedIfNeeded(cronjob))
				{
					LOG.info(cronjob.getRegion() + " : CRONJOB IS ABORTED WHILE PERFORMING ...");

					/* This is to flush the buffer of existing errorList and message as well */
					energizerCSVProcessor.flush();
					//abort the job
					cronJobService.requestAbortCronJob(cronjob);

					return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
				}
			}


			if (exceptionOccured)
			{
				performResult = new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
			}
			else
			{
				performResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
			}
			energizerCSVProcessor.flush();

			final Long fileProcessingEndTime = System.currentTimeMillis();
			//LOG.info("After processing this file : " + fileProcessingEndTime + " milliseconds !!");

			//LOG.info("Cronjob file processing time taken in milliseconds == " + (fileProcessingEndTime - fileProcessingStartTime)
			//	+ " , seconds == " + (fileProcessingEndTime - fileProcessingStartTime) / 1000);

			//LOG.info("************** PROCESSING END FOR THIS FILE  '" + file.getName() + "' ***************");
		}

		catch (final StorageException e1)
		{
			// YTODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (final URISyntaxException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final FileNotFoundException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}

		final Long jobEndTime = System.currentTimeMillis();

		LOG.info("Cronjob total processing time taken in milliseconds == " + (jobEndTime - jobStartTime) + ", seconds ==  "
				+ (jobEndTime - jobStartTime) / 1000);

		LOG.info("************************ PROCESSING END FOR THIS CRONJOB  ***************************");

		return performResult;
	}

	private String getFileSizeMegaBytes(final File file)
	{
		return df2.format((double) file.length() / (1024 * 1024)) + " mb";
	}

	private String getFileSizeKiloBytes(final File file)
	{
		return df2.format((double) file.length() / 1024) + "  kb";
	}

}
