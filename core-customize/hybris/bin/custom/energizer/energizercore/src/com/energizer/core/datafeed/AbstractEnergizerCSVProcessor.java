/**
 *
 */
package com.energizer.core.datafeed;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerCronJobModel;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;


/**
 * @author M9005674
 *
 */
public class AbstractEnergizerCSVProcessor implements EnergizerCSVProcessor
{
	Logger LOG = Logger.getLogger(AbstractEnergizerCSVProcessor.class);
	@Value("${sharedFolderPath}")
	public String sharedFolderPath;
	public static final String toProcess = "toProcess";
	public static final String ProcessedWithNoErrors = "ProcessedWithNoErrors";
	public static final String ErrorFiles = "ErrorFiles";
	public static final String fileSeperator = "/";
	public final StringBuilder message = new StringBuilder(512);
	public static final String COMMA = ",";

	private String enviorment;
	private static String CATALOG_NAME = "";
	private static String VERSION = "";
	private static String fileName = "";
	private FileReader reader;
	private long totalRecords = 0;
	private long recordSucceeded = 0;
	private long recordFailed = 0;
	private long techRecordError = 0;
	private long busRecordError = 0;

	//set the global errors

	@Resource
	EmailService emailService;

	@Resource
	ModelService modelService;

	@Resource
	CatalogVersionService catalogVersionService;
	@Resource
	I18NService i18nService;
	@Resource
	private ConfigurationService configurationService;

	@Resource
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;


	public List<EnergizerCSVFeedError> csvFeedErrorRecords = new ArrayList<EnergizerCSVFeedError>();

	public List<EnergizerCSVFeedError> techFeedErrorRecords = new ArrayList<EnergizerCSVFeedError>();
	public List<EnergizerCSVFeedError> businessFeedErrorRecords = new ArrayList<EnergizerCSVFeedError>();

	public List<EnergizerCSVFeedError> technicalFeedErrors = new ArrayList<EnergizerCSVFeedError>();
	public List<EnergizerCSVFeedError> businessFeedErrors = new ArrayList<EnergizerCSVFeedError>();
	private DataInputStream masterDataStream;
	private String jobName;
	private String mailSubject;
	@Resource(name = "baseMessageSource")
	private ReloadableResourceBundleMessageSource messageSource;

	/* Class level variable added for EMEA cronjob development */
	private EnergizerCronJobModel cronjob;
	public static final String personalcarethumbnailpath = Config.getParameter("energizer.thumbnailPath");


	@Override
	public synchronized void logErrors(final EnergizerCronJobModel cronjob, final List<EnergizerCSVFeedError> errors)
	{
		final Locale locale = i18nService.getCurrentLocale();
		LOG.info("Before sending email/cron job errors: ");
		//enviorment = configurationService.getConfiguration().getString("mail.enviorment");
		if (null != errors && !errors.isEmpty() && errors.size() > 0)
		{
			LOG.info("There are " + errors.size() + " errors encountered during this data feed cronjob execution ...");
			for (final EnergizerCSVFeedError error : errors)
			{
				// Adding one more line number for including the file header columns.
				LOG.debug("Error ::: " + error.getMessage() + " at line number == " + (error.getLineNumber() + 1));
			}
		}
		message.append("Hi,");
		message.append("<br/>");

		message.append("Error occured while processing the " + cronjob.getDisplayName().toUpperCase() + " in Hybris " + enviorment
				+ " environment due to missing/bad data in the CSV feed file attached. Please check the data in SAP system and update them accordingly for further reprocessing in Hybris.");
		message.append("<br/><br/>");
		message.append("Note: These errored out records are not processed/saved in Hybris.");
		message.append("<br/>");
		message.append("<br/>");
		setJobName(cronjob.getDisplayName());
		LOG.info("Failed records " + getRecordFailed());
		message.append(messageSource.getMessage("text.error.message.email.template.section1", new Object[]
		{ "" }, locale));


		final Path fileDirectory = Paths
				.get(this.getCronjob().getPath() + fileSeperator + fileSeperator + toProcess + fileSeperator + getFileName());
		message.append(messageSource.getMessage("text.error.message.email.template.section2", new Object[]
		{ cronjob.getDisplayName(), cronjob.getStartTime(), getTotalRecords(), CronJobResult.ERROR, "", getRecordSucceeded(),
				CronJobStatus.FINISHED, getFileName(), (getTotalRecords() - getRecordSucceeded()), fileDirectory }, locale));
		message.append(messageSource.getMessage("text.error.message.email.template.section3", null, locale));
		message.append(messageSource.getMessage("text.error.message.email.template.section4", null, locale));

		int lineNumber = 0;
		for (final EnergizerCSVFeedError error : errors)
		{
			lineNumber++;
			message.append(messageSource.getMessage("text.error.message.email.template.section5", new Object[]
			{ lineNumber, error.getMessage(), error.getLineNumber(), "", error.getColumnName().toString() }, locale));
		}

		message.append(messageSource.getMessage("text.error.message.email.template.section6", null, locale));
		message.append("\n");
		if (LOG.isDebugEnabled())
		{
			LOG.debug(message.toString());
		}
		//LOG.debug(message.toString());
	}

	@Override
	public void mailErrors(final EnergizerCronJobModel cronjob, final List<EnergizerCSVFeedError> errors,
			final List<String> toEmail, final List<EmailAttachmentModel> emailAttachmentList)
	{
		try
		{

			// Added for weSell Implementation - Start
			String site = null;
			String salesOrg = null;

			if (cronjob.getPath().contains(EnergizerCoreConstants.LATAM))
			{
				site = EnergizerCoreConstants.LATAM;
			}
			else if (cronjob.getPath().contains(EnergizerCoreConstants.EMEA))
			{
				site = EnergizerCoreConstants.EMEA;
			}
			else if (cronjob.getPath().contains(EnergizerCoreConstants.WESELL))
			{
				site = EnergizerCoreConstants.WESELL;
				//FileDescription_<SalesOrgNumber>_timestamp.csv
				//setFileName("CMIR_8372_1234567890");
				if (null != getFileName() && null != getFileName().split("_"))
				{
					salesOrg = getFileName().split("_")[1];
				}
			}
			enviorment = configurationService.getConfiguration().getString("mail.environment.stuck.orders");

			if (null != salesOrg)
			{
				if (null == enviorment || StringUtils.isEmpty(enviorment))
				{
					enviorment = configurationService.getConfiguration().getString("mail.environment.stuck.orders");
				}

				setMailSubject(enviorment + " : " + site + " | " + salesOrg + " | " + "ERROR: Occurred in "
						+ cronjob.getDisplayName().toUpperCase());
			}
			else
			{
				setMailSubject(enviorment + " : " + site + " : ERROR: Occurred in " + cronjob.getDisplayName().toUpperCase());
			}
			// Added for WeSell Implementation - END
			logErrors(cronjob, errors);
			EmailMessageModel emailMessageModel = null;
			EmailAddressModel emailAddress = null;
			final List<EmailAddressModel> toAddressModels = new ArrayList<EmailAddressModel>();
			for (final String toAddress : toEmail)
			{
				emailAddress = emailService.getOrCreateEmailAddressForEmail(toAddress, "Error Message");
				toAddressModels.add(emailAddress);
			}
			emailAddress = emailService.getOrCreateEmailAddressForEmail(configurationService.getConfiguration()
					.getString("cronjobs.from.email", Config.getParameter("fromEmailAddress.orderEmailSender")), "Order Portal Team");
			synchronized (message)
			{
				emailMessageModel = emailService.createEmailMessage(toAddressModels, null, null, emailAddress,
						Config.getParameter(EMAIL_REPLY_TO), getMailSubject(), message.toString(), emailAttachmentList);
				emailService.send(emailMessageModel);
			}
			message.setLength(0);
		}
		catch (final Exception e)
		{
			message.setLength(0);
			LOG.error("Exception in Mail Errors", e);
		}
	}

	public Iterable<CSVRecord> parse(final String blobUri)
	{

		final CSVFormat csvFormat = CSVFormat.EXCEL.withDelimiter(DELIMETER).withIgnoreSurroundingSpaces();

		Iterable<CSVRecord> blobRecordS = null;

		try
		{
			CloudBlockBlob blob2;

			blob2 = energizerWindowsAzureBlobStorageStrategy.getBlobContainer().getBlockBlobReference(blobUri);
			if (StringUtils.isNotEmpty(blob2.downloadText()))
			{
				final Reader readerBlob = new StringReader(blob2.downloadText());
				blobRecordS = csvFormat.withHeader().parse(readerBlob);
			}

		}
		catch (URISyntaxException | StorageException | IOException e)
		{
			LOG.error(e.getMessage());
		}
		return blobRecordS;
	}


	@Override
	public Iterable<CSVRecord> parse(final File file) throws FileNotFoundException
	{
		final CSVFormat csvFormat = CSVFormat.EXCEL.withDelimiter(DELIMETER).withIgnoreSurroundingSpaces();
		Iterable<CSVRecord> records = null;
		Iterable<CSVRecord> recordsForLog = null;
		reader = new FileReader(file);
		final FileReader readerForLog = new FileReader(file);
		try
		{
			setFileName(file.getName().toLowerCase());
			records = csvFormat.withHeader().parse(reader);
			recordsForLog = csvFormat.withHeader().parse(readerForLog);
			//logRecord(recordsForLog); - Commenting this to avoid large chuck of loggers in the console log file, we can already check the data in the CSV file itself.
			readerForLog.close();
		}
		catch (final IOException e)
		{
			LOG.error(e.getMessage());
		}
		return records;
	}

	@Override
	public void cleanup(final String type, final File file, final EnergizerCronJobModel cronjob, final boolean hasErrors)
	{
		try
		{
			reader.close();
		}
		catch (final IOException e1)
		{
			e1.printStackTrace();
		}
		if (hasErrors)
		{
			//			logErrors(cronjob, errors);
			//			errors.clear();
			try
			{
				final Path sourcePath1 = Paths.get(this.getCronjob().getPath() + fileSeperator + type + fileSeperator + toProcess
						+ fileSeperator + file.getName());
				final Path targetPath = Paths.get(this.getCronjob().getPath() + fileSeperator + type + fileSeperator + ErrorFiles
						+ fileSeperator + file.getName());
				Files.move(sourcePath1, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (final Exception e1)
			{
				LOG.error(e1.getMessage());
			}
		}
		else
		{
			try
			{
				final Path sourcePath1 = Paths.get(this.getCronjob().getPath() + fileSeperator + type + fileSeperator + toProcess
						+ fileSeperator + file.getName());
				final Path targetPath = Paths.get(this.getCronjob().getPath() + fileSeperator + type + fileSeperator
						+ ProcessedWithNoErrors + fileSeperator + file.getName());
				Files.move(sourcePath1, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (final Exception e1)
			{
				LOG.error(e1.getMessage());
			}
		}
	}

	public CloudBlobDirectory getBlobDirectoryForFeedType(final String type)
	{

		CloudBlobDirectory blobDirectory = null;

		try
		{
			final CloudBlobContainer container = energizerWindowsAzureBlobStorageStrategy.getBlobContainer();
			final String toProcessDirectoryPath = this.getCronjob().getPath() + fileSeperator + type + fileSeperator + toProcess;
			blobDirectory = container.getDirectoryReference(toProcessDirectoryPath);
			return blobDirectory;

		}
		catch (final URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return blobDirectory;
	}


	public CloudBlobDirectory getBlobDirectoryPersonalCareThumbNailPath(final String thumbnailpath)
	{

		CloudBlobDirectory blobDirectory = null;

		try
		{
			final CloudBlobContainer container = energizerWindowsAzureBlobStorageStrategy.getBlobContainer();
			blobDirectory = container.getDirectoryReference(thumbnailpath);
			return blobDirectory;

		}
		catch (final URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return blobDirectory;
	}




	public void Blobcleanup(final String fileName, final EnergizerCronJobModel cronjob, final boolean hasErrors,
			final String fullFilePath, final CloudBlockBlob sourceBlob, final CloudBlobContainer cloudBlobContainer)
	{

		// writing file to error directory
		final String processedWithNoErrorsDirectoryPath = StringUtils.substringBefore(fullFilePath, "toProcess")
				+ ProcessedWithNoErrors + fileSeperator + fileName;

		final String errorDirectoryPath = StringUtils.substringBefore(fullFilePath, "toProcess") + ErrorFiles + fileSeperator
				+ fileName;


		if (hasErrors)
		{
			try
			{
				final CloudBlockBlob blobWriteToError = cloudBlobContainer.getBlockBlobReference(errorDirectoryPath);
				blobWriteToError.startCopy(sourceBlob.getSnapshotQualifiedUri());
				//Delete file
				sourceBlob.delete();
			}
			catch (final Exception e1)
			{
				LOG.error(e1.getMessage());
			}
		}
		else
		{
			try
			{
				final CloudBlockBlob blobWriteToSucess = cloudBlobContainer.getBlockBlobReference(processedWithNoErrorsDirectoryPath);
				blobWriteToSucess.startCopy(sourceBlob.getSnapshotQualifiedUri());
				//Delete file
				sourceBlob.delete();
			}
			catch (final Exception e1)
			{
				LOG.error(e1.getMessage());
			}
		}
		//Need to delete from toProcess folder
		//final CloudBlockBlob blobToProcess = cloudBlobContainer.getBlockBlobReference(readFromProcessDirectoryPath);
	}

	public List<File> getFilesForFeedType(final String type)
	{
		final List<File> typeFilesList = new ArrayList<File>();
		final File csvFiles = new File(this.getCronjob().getPath() + fileSeperator + type + fileSeperator + toProcess);
		LOG.info("Loading files from :" + csvFiles);
		if (csvFiles.isDirectory())
		{
			final File[] file = csvFiles.listFiles();

			for (int i = 0; i < file.length; i++)
			{
				final File typeFile = file[i];
				typeFilesList.add(typeFile);
			}
		}
		return typeFilesList;
	}

	public String[] getHeadersForFeed(final String key)
	{
		String[] retVal = null;
		if (key != null && !key.isEmpty())
		{
			retVal = Config.getParameter(key).split(COMMA);
		}
		return retVal;
	}

	public Boolean hasMandatoryFields(final CSVRecord record, final String[] mandatoryFields)
	{
		Boolean flag = true;
		int count = 0;
		final Map<String, String> map = record.toMap();
		for (final String mField : mandatoryFields)
		{
			if (!StringUtils.isEmpty(map.get(mField)))
			{
				++count;
			}
		}
		if (mandatoryFields.length != count)
		{
			flag = false;
		}

		return flag;
	}

	/**
	 * @param input
	 *           input value
	 * @param isValid
	 *           isValid
	 * @param errBuffer
	 *           errBuffer
	 * @param field
	 *           field
	 * @return Numeric Double Value
	 */
	public Double calculateNumeric(final String input, boolean isValid, final StringBuffer errBuffer, final String field)
	{

		Double returnVal = null;
		if (StringUtils.isNotBlank(input))
		{
			if (NumberUtils.isNumber(input))
			{
				returnVal = NumberUtils.createDouble(input);
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + field + " is not an number ");
			}
		}
		return returnVal;

	}

	public void sendEmail(final List<String> toAddresses, final List<String> ccAddresses, final List<String> bccAddresses,
			final String fromAddress, final String fromAddressDisplayName, final String replyToAddress, final String subject,
			final String body)
	{

		final EmailAddressModel fromAddressEmail = emailService.getOrCreateEmailAddressForEmail(fromAddress,
				StringUtils.isEmpty(fromAddressDisplayName) ? fromAddress : fromAddressDisplayName);

		List<EmailAddressModel> toAddressList = null;
		List<EmailAddressModel> ccAddressesList = null;
		List<EmailAddressModel> bccAddressesList = null;

		if (!(toAddresses == null || toAddresses.isEmpty()))
		{
			toAddressList = new ArrayList<EmailAddressModel>();
			for (final String toAddress : toAddresses)
			{
				toAddressList.add(emailService.getOrCreateEmailAddressForEmail(toAddress, toAddress));
			}
		}

		if (!(ccAddresses == null || ccAddresses.isEmpty()))
		{
			ccAddressesList = new ArrayList<EmailAddressModel>();
			for (final String ccAddress : ccAddresses)
			{
				ccAddressesList.add(emailService.getOrCreateEmailAddressForEmail(ccAddress, ccAddress));
			}
		}

		if (!(bccAddresses == null || bccAddresses.isEmpty()))
		{
			bccAddressesList = new ArrayList<EmailAddressModel>();
			for (final String bccAddress : bccAddresses)
			{
				bccAddressesList.add(emailService.getOrCreateEmailAddressForEmail(bccAddress, bccAddress));
			}
		}

		final EmailMessageModel message = emailService.createEmailMessage(toAddressList, ccAddressesList, bccAddressesList,
				fromAddressEmail, replyToAddress, enviorment + subject, body, null);
		emailService.send(message);
	}


	public CatalogVersionModel getCatalogVersion(final EnergizerCronJobModel cronjob) throws Exception
	{
		CatalogVersionModel catalogVersion = null;
		/* Started EMEA Code refactor for get value from Model insted of properties */
		CATALOG_NAME = cronjob.getCatalogName();
		VERSION = cronjob.getCatalogVersion();
		/* End EMEA Code refactor for get value from Model insted of properties */
		if (StringUtils.isEmpty(CATALOG_NAME) || StringUtils.isEmpty(VERSION))
		{
			throw new Exception("Invalid Catalog Version ");
		}
		if (StringUtils.isNotEmpty(CATALOG_NAME) && StringUtils.isNotEmpty(VERSION))
		{
			catalogVersion = catalogVersionService.getCatalogVersion(CATALOG_NAME, VERSION);
		}
		else
		{
			throw new Exception("Invalid Catalog Version ");
		}
		return catalogVersion;
	}

	public String getSalesOrg() throws Exception
	{

		String salesOrg = null;

		if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.LATAM))
		{
			salesOrg = configurationService.getConfiguration().getString(EnergizerCoreConstants.LATAM_DEFAULT_SALES_ORG, "1000");
		}
		else if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL))
		{
			if (null != getFileName() && null != getFileName().split("_"))
			{
				salesOrg = getFileName().split("_")[1];
			}
		}
		return salesOrg;
	}


	public void logRecord(final Iterable<CSVRecord> recordsForLog)
	{
		//		final boolean isCronJobLoggerEnabled =  (configurationService.getConfiguration().getBoolean("isCronJobLoggerEnabled", false));

		//		if(isCronJobLoggerEnabled){
		boolean printedHeader = false;
		LOG.info("************************ CSV RECORDS ***********************************");
		for (final CSVRecord record : recordsForLog)
		{

			final Map<String, String> csvValuesMap = record.toMap();
			if (!printedHeader)
			{
				LOG.info(csvValuesMap.keySet());
			}
			printedHeader = true;
			LOG.info(csvValuesMap.values());
		}

		LOG.info("***********************************************************************************");
		//		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.core.datafeed.EnergizerCSVProcessor#getCSVFeedErrorRecords()
	 */
	@Override
	public List<EnergizerCSVFeedError> getCSVFeedErrorRecords()
	{
		return new ArrayList<EnergizerCSVFeedError>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.core.datafeed.EnergizerCSVProcessor#process(java.lang.Iterable)
	 */
	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> iterable, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		return null;
	}

	/**
	 * @return the csvFeedErrorRecords
	 */
	public List<EnergizerCSVFeedError> getCsvFeedErrorRecords()
	{
		return csvFeedErrorRecords;
	}

	/**
	 * @param csvFeedErrorRecords
	 *           the csvFeedErrorRecords to set
	 */
	public void setCsvFeedErrorRecords(final List<EnergizerCSVFeedError> csvFeedErrorRecords)
	{
		this.csvFeedErrorRecords = csvFeedErrorRecords;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * @param fileName
	 *           the fileName to set
	 */
	public static void setFileName(final String fileName)
	{
		AbstractEnergizerCSVProcessor.fileName = fileName;
	}

	/**
	 * @return the jobName
	 */
	public String getJobName()
	{
		return jobName;
	}

	/**
	 * @param jobName
	 *           the jobName to set
	 */
	public void setJobName(final String jobName)
	{
		this.jobName = jobName;
	}

	/**
	 * @return the technicalFeedErrors
	 */
	public List<EnergizerCSVFeedError> getTechnicalFeedErrors()
	{
		return technicalFeedErrors;
	}

	/**
	 * @param technicalFeedErrors
	 *           the technicalFeedErrors to set
	 */
	public void setTechnicalFeedErrors(final List<EnergizerCSVFeedError> technicalFeedErrors)
	{
		this.technicalFeedErrors = technicalFeedErrors;
	}

	/**
	 * @return the businessFeedErrors
	 */
	public List<EnergizerCSVFeedError> getBusinessFeedErrors()
	{
		return businessFeedErrors;
	}

	/**
	 * @param businessFeedErrors
	 *           the businessFeedErrors to set
	 */
	public void setBusinessFeedErrors(final List<EnergizerCSVFeedError> businessFeedErrors)
	{
		this.businessFeedErrors = businessFeedErrors;
	}

	/**
	 * @return the recordSucceeded
	 */
	public long getRecordSucceeded()
	{
		return recordSucceeded;
	}

	/**
	 * @param recordSucceeded
	 *           the recordSucceeded to set
	 */
	public void setRecordSucceeded(final long recordSucceeded)
	{
		this.recordSucceeded = recordSucceeded;
	}

	/**
	 * @return the totalRecords
	 */
	public long getTotalRecords()
	{
		return totalRecords;
	}

	/**
	 * @param totalRecords
	 */
	public void setTotalRecords(final long totalRecords)
	{
		this.totalRecords = totalRecords;
	}

	/**
	 * @return the recordFailed
	 */
	public long getRecordFailed()
	{
		return recordFailed;
	}

	/**
	 * @param recordFailed
	 *           the recordFailed to set
	 */
	public void setRecordFailed(final long recordFailed)
	{
		this.recordFailed = recordFailed;
	}

	/**
	 * @return the techRecordError
	 */
	public long getTechRecordError()
	{
		return techRecordError;
	}

	/**
	 * @return the busRecordError
	 */
	public long getBusRecordError()
	{
		return busRecordError;
	}

	/**
	 * @param techRecordError
	 *           the techRecordError to set
	 */
	public void setTechRecordError(final long techRecordError)
	{
		this.techRecordError = techRecordError;
	}

	/**
	 * @param busRecordError
	 *           the busRecordError to set
	 */
	public void setBusRecordError(final long busRecordError)
	{
		this.busRecordError = busRecordError;
	}

	/**
	 * @return the mailSubject
	 */
	public String getMailSubject()
	{
		return mailSubject;
	}

	/**
	 * @param mailSubject
	 *           the mailSubject to set
	 */
	public void setMailSubject(final String mailSubject)
	{
		this.mailSubject = mailSubject;
	}

	/**
	 * @return the masterDataStream
	 */
	public DataInputStream getMasterDataStream()
	{
		return masterDataStream;
	}

	/**
	 * @param masterDataStream
	 *           the masterDataStream to set
	 */
	public void setMasterDataStream(final DataInputStream masterDataStream)
	{
		this.masterDataStream = masterDataStream;
	}

	/**
	 * @return the techFeedErrorRecords
	 */
	public List<EnergizerCSVFeedError> getTechFeedErrorRecords()
	{
		return techFeedErrorRecords;
	}

	/**
	 * @param techFeedErrorRecords
	 *           the techFeedErrorRecords to set
	 */
	public void setTechFeedErrorRecords(final List<EnergizerCSVFeedError> techFeedErrorRecords)
	{
		this.techFeedErrorRecords = techFeedErrorRecords;
	}

	/**
	 * @return the businessFeedErrorRecords
	 */
	public List<EnergizerCSVFeedError> getBusinessFeedErrorRecords()
	{
		return businessFeedErrorRecords;
	}

	/**
	 * @param businessFeedErrorRecords
	 *           the businessFeedErrorRecords to set
	 */
	public void setBusinessFeedErrorRecords(final List<EnergizerCSVFeedError> businessFeedErrorRecords)
	{
		this.businessFeedErrorRecords = businessFeedErrorRecords;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.core.datafeed.EnergizerCSVProcessor#flush()
	 */
	@Override
	public void flush()
	{
		csvFeedErrorRecords.clear();
		techFeedErrorRecords.clear();
		businessFeedErrorRecords.clear();
		technicalFeedErrors.clear();
		businessFeedErrors.clear();
		message.setLength(0);
	}

	public FileReader getReader()
	{
		return reader;
	}

	/**
	 * @return the cronjob
	 */
	public EnergizerCronJobModel getCronjob()
	{
		return cronjob;
	}

	/**
	 * @param cronjob
	 *           the cronjob to set
	 */
	public void setCronjob(final EnergizerCronJobModel cronjob)
	{
		this.cronjob = cronjob;
	}
}
