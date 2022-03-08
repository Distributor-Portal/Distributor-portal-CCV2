/**
 *
 */
package com.energizer.core.datafeed;

import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import com.energizer.core.model.EnergizerCronJobModel;
import com.microsoft.azure.storage.blob.CloudBlobContainer;


/**
 * @author M9005674
 *
 */
public interface EnergizerCSVProcessor
{

	public static final char DELIMETER = ',';
	public static final String EMAIL_REPLY_TO = "mail.smtp.user";
	public static final String FEED_PROCESSOR_PRODUCT_CATALOG_NAME = "feedprocessor.productcatalog.id";
	public static final String FEED_PROCESSOR_PRODUCT_CATALOG_VERSION = "feedprocessor.productcatalog.version";
	public static final String TECHNICAL_USER = "T";
	public static final String BUSINESS_USER = "B";

	public Iterable<CSVRecord> parse(final File file) throws FileNotFoundException;

	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> iterable, String catalogName,
			final EnergizerCronJobModel cronjob);

	public void logErrors(final EnergizerCronJobModel cronjob, final List<EnergizerCSVFeedError> errors);

	public void cleanup(final String type, final File file, final EnergizerCronJobModel cronjob, boolean errors);

	public void flush();

	/**
	 * @return the recordErrors
	 */
	public List<EnergizerCSVFeedError> getCSVFeedErrorRecords();

	/**
	 * @param toEmail
	 * @param type
	 * @param errorSize
	 */
	public void mailErrors(final EnergizerCronJobModel cronjob, final List<EnergizerCSVFeedError> errors,
			final List<String> toEmail, final List<EmailAttachmentModel> emailAttachmentList);


	public String[] getHeadersForFeed(final String key);

	public Boolean hasMandatoryFields(final CSVRecord record, final String[] mandatoryFields);

	/**
	 * @param type
	 * @return
	 */
	Iterable<CSVRecord> parse(String type);

	/**
	 * @return
	 */
	CloudBlobContainer getBlobContainer();





}
