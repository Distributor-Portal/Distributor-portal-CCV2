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
import de.hybris.platform.util.Config;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.energizer.core.datafeed.processor.product.EnergizerProduct2CategoryRelationCSVProcessor;
import com.energizer.core.model.EnergizerCronJobModel;

// Azure SDK v12 imports
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;

public class EnergizerCSVFeedCronJob extends AbstractJobPerformable<EnergizerCronJobModel>
{
	private static final Logger LOG = Logger.getLogger(EnergizerCSVFeedCronJob.class);
	private static final DecimalFormat df2 = new DecimalFormat("#.##");
	public static final String dummyFileName = Config.getParameter("azure.blob.storage.dummy.file.name");

	@Resource
	EmailService emailService;

	private CronJobService cronJobService;

	@Resource
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;

	@Resource
	private ConfigurationService configurationService;

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

	@Override
	public synchronized PerformResult perform(final EnergizerCronJobModel cronjob)
	{
		LOG.info("************************ PROCESSING START FOR THIS CRONJOB  ***************************");
		final Long jobStartTime = System.currentTimeMillis();
		LOG.info("Before processing this cronjob : " + jobStartTime + " milliseconds !!");

		List<EnergizerCSVFeedError> errors = new ArrayList<>();
		List<EnergizerCSVFeedError> techfeedErrors = new ArrayList<>();
		List<EnergizerCSVFeedError> busfeedErrors = new ArrayList<>();
		PerformResult performResult = null;
		final List<String> emailAddress = new ArrayList<>();
		final String type = cronjob.getType();

		if (type == null)
		{
			LOG.info("There is no Type defined for the job " + cronjob.getCode());
			LOG.info("*********************************** NOTHING TO PROCESS FOR THIS CRONJOB  *****************************************");
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
		final AbstractEnergizerCSVProcessor energizerCSVProcessor = (AbstractEnergizerCSVProcessor) Registry.getApplicationContext()
				.getBean(type);
		energizerCSVProcessor.setCronjob(cronjob);

		try
		{
			// Get container and iterate blob list
			BlobContainerClient container = energizerWindowsAzureBlobStorageStrategy.getBlobContainerClient();
			String prefix = cronjob.getPath() + AbstractEnergizerCSVProcessor.fileSeperator + type +
					AbstractEnergizerCSVProcessor.fileSeperator + AbstractEnergizerCSVProcessor.toProcess +
					AbstractEnergizerCSVProcessor.fileSeperator;

			Boolean exceptionOccured = false;
			if (null != cronjob.getEmailAddress())
			{
				emailAddress.add(cronjob.getEmailAddress());
			}
			energizerCSVProcessor.flush();

			String resultType = "";

			for (BlobItem blobItem : container.listBlobsByHierarchy(prefix))
			{
				if (blobItem.isPrefix())
					continue;

				final String fullFilePath = blobItem.getName();
				final String fileName = StringUtils.substringAfterLast(fullFilePath, "/");

				if (!(dummyFileName.equalsIgnoreCase(fileName)))
				{
					final Long fileProcessingStartTime = System.currentTimeMillis();

					Iterable<CSVRecord> csvRecords;
					BlobClient blobClient = container.getBlobClient(fullFilePath);

					// Download blob content as text
					byte[] blobBytes = blobClient.downloadContent().toBytes();
					String blobText = new String(blobBytes);

					csvRecords = energizerCSVProcessor.parse(fullFilePath);

					LOG.info("************** PROCESSING START FOR THIS FILE  '" + fileName + "' ***************");
					LOG.info("Before processing this file : " + fileProcessingStartTime + " milliseconds !!");

					sessionService.setAttribute("fileName", fileName);
					energizerCSVProcessor.setFileName(fileName);

					errors = energizerCSVProcessor.process(csvRecords, cronjob.getCatalogName(), cronjob);
					// Clear session after processing
                     sessionService.removeAttribute("fileName");
					exceptionOccured = (errors.size() != 0);

					energizerCSVProcessor.setMasterDataStream(new DataInputStream(new ByteArrayInputStream(blobText.getBytes())));

					final List<EmailAttachmentModel> emailAttachmentList = new ArrayList<>();
					final EmailAttachmentModel attachmentModel = emailService.createEmailAttachment(
							energizerCSVProcessor.getMasterDataStream(),
							StringUtils.replace(fileName.toLowerCase(), ".csv",
									"_" + new Date().getTime() + "." +
											de.hybris.platform.impex.constants.ImpExConstants.File.EXTENSION_CSV).toLowerCase(),
							de.hybris.platform.impex.constants.ImpExConstants.File.MIME_TYPE_CSV);

					emailAttachmentList.add(attachmentModel);

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
						energizerCSVProcessor.mailErrors(cronjob, techfeedErrors, cronjob.getTechnicalEmailAddress(),
								emailAttachmentList);
					}
					busfeedErrors = energizerCSVProcessor.getBusinessFeedErrors();
					if (!busfeedErrors.isEmpty())
					{
						energizerCSVProcessor.setRecordFailed(energizerCSVProcessor.getBusRecordError());
						energizerCSVProcessor.mailErrors(cronjob, busfeedErrors, cronjob.getBusinessEmailAddress(),
								emailAttachmentList);
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
							BlobClient sourceBlob = container.getBlobClient(fullFilePath);
							energizerCSVProcessor.Blobcleanup(fileName, cronjob, true, fullFilePath, sourceBlob, container);
						}
						energizerCSVProcessor.flush();
					}
					else
					{
						if (!(energizerCSVProcessor instanceof EnergizerProduct2CategoryRelationCSVProcessor))
						{
							BlobClient sourceBlob = container.getBlobClient(fullFilePath);
							energizerCSVProcessor.Blobcleanup(fileName, cronjob, false, fullFilePath, sourceBlob, container);
						}
						energizerCSVProcessor.flush();
					}

					// If the cronjob abort is requested, then perform clean up and return the PerformResult
					this.modelService.refresh(cronjob);
					if (clearAbortRequestedIfNeeded(cronjob))
					{
						LOG.info(cronjob.getRegion() + " : CRONJOB IS ABORTED WHILE PERFORMING ...");
						energizerCSVProcessor.flush();
						resultType = "aborted";
						cronJobService.requestAbortCronJob(cronjob);
						return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
					}

					if (exceptionOccured)
					{
						performResult = new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
						resultType = "error";
					}
					else
					{
						performResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
						resultType = "sucess";
					}
					energizerCSVProcessor.flush();

					final Long fileProcessingEndTime = System.currentTimeMillis();
					LOG.info("After processing this file : " + fileProcessingEndTime + " milliseconds !!");
					LOG.info("Cronjob file processing time taken in milliseconds == " + (fileProcessingEndTime - fileProcessingStartTime)
							+ " , seconds == " + (fileProcessingEndTime - fileProcessingStartTime) / 1000);
					LOG.info("************** PROCESSING END FOR THIS FILE  '" + fileName + "' ***************");
				}
				else
				{
					LOG.info("************** Nothing to processing, there is dummy file  '" + fileName + "' ***************");
					LOG.info("************** Result Type  '" + resultType + "' ***************");

					if ("error".equalsIgnoreCase(resultType))
					{
						performResult = new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
					}
					else if ("sucess".equalsIgnoreCase(resultType))
					{
						performResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
					}
					else if ("aborted".equalsIgnoreCase(resultType))
					{
						performResult = new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
					}
					else
					{
						performResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
					}
				}
			}
		}
		catch (final BlobStorageException e1)
		{
			LOG.error("Azure Blob Storage error", e1);
		}
		catch (final Exception e)
		{
			LOG.error("Exception", e);
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
