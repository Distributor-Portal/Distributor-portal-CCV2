/**
 *
 */
package com.energizer.core.errorcronjobs;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerCronJobModel;


/**
 * @author Pavanip
 *
 */
public class EnergizerCronJobsStatusJob extends AbstractJobPerformable<EnergizerCronJobModel>
{

	@Resource(name = "flexibleSearchService")
	private FlexibleSearchService flexibleSearchService;
	@Resource
	private ConfigurationService configurationService;

	@Resource
	EmailService emailService;
	final String MIME_TYPE = "application/vnd.ms-excel";
	final Logger LOG = Logger.getLogger(EnergizerCronJobsStatusJob.class);

	/* (non-Javadoc)
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.CronJobModel)
	 */
	@SuppressWarnings(
	{ "resource", "unused" })
	@Override
	public PerformResult perform(final EnergizerCronJobModel energizerCronJobModel)
	{



		List<EnergizerCronJobModel> cronjobModels = new ArrayList<EnergizerCronJobModel>();


		try
		{

			final String allCronjobs = "SELECT {pk} FROM {EnergizerCronJob}";
			final FlexibleSearchQuery query = new FlexibleSearchQuery(allCronjobs);

			cronjobModels =flexibleSearchService.<EnergizerCronJobModel> search(query).getResult();

			if(CollectionUtils.isNotEmpty(cronjobModels)) {
				final HSSFWorkbook workBookLatam = new HSSFWorkbook();
				final HSSFWorkbook workBookEmea = new HSSFWorkbook();
				final HSSFWorkbook workBookWesell = new HSSFWorkbook();

				final HSSFSheet latamsheet = workBookLatam.createSheet("Cronjob Status - LATAM");
				final HSSFSheet emeasheet = workBookEmea.createSheet("Cronjob Status - EMEA");
				final HSSFSheet wesellsheet = workBookWesell.createSheet("Cronjob Status - WESELL");

				int latamRowNum = 0;
				int emeaRowNum = 0;
				int weSellRowNum = 0;
				int sNoLatam = 0;
				int sNoEmea = 0;
				int sNoWesell = 0;


				final HSSFRow rowheaderLatam = latamsheet.createRow(latamRowNum);
				final HSSFRow rowheaderEmea = emeasheet.createRow(emeaRowNum);
				final HSSFRow rowheaderWesell = wesellsheet.createRow(weSellRowNum);

				final SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HHmmss");
				final String dateTime = sdf.format(new Date());

				final String FILE_PATH = configurationService.getConfiguration().getString("catalogdownload.downloadPath");

				final File dirLatam = new File(
						System.getProperty("user.home") + "\\" + FILE_PATH + "\\" + "Cronjob Status in LATAM_" + dateTime + ".xls");
				final File dirEmea = new File(
						System.getProperty("user.home") + "\\" + FILE_PATH + "\\" + "Cronjob Status in EMEA_" + dateTime + ".xls");
				final File dirWesell = new File(
						System.getProperty("user.home") + "\\" + FILE_PATH + "\\" + "Cronjob Status in WESELL_" + dateTime + ".xls");
				rowheaderLatam.createCell(0).setCellValue("S.NO");
				rowheaderLatam.createCell(1).setCellValue("Cronjob Name");
				rowheaderLatam.createCell(2).setCellValue("Status");

				rowheaderEmea.createCell(0).setCellValue("S.NO");
				rowheaderEmea.createCell(1).setCellValue("Cronjob Name");
				rowheaderEmea.createCell(2).setCellValue("Status");

				rowheaderWesell.createCell(0).setCellValue("S.NO");
				rowheaderWesell.createCell(1).setCellValue("Cronjob Name");
				rowheaderWesell.createCell(2).setCellValue("Status");

				latamRowNum = latamRowNum + 1;
				emeaRowNum = emeaRowNum + 1;
				weSellRowNum = weSellRowNum + 1;

				sNoLatam = sNoLatam + 1;
				sNoEmea = sNoEmea + 1;
				sNoWesell = sNoWesell + 1;


				FileOutputStream fileOutLatam;
				FileOutputStream fileOutEmea;
				FileOutputStream fileOutWesell;





				 for (final EnergizerCronJobModel energizerCronJobModel2 : cronjobModels)
				{


					final String catalogName = energizerCronJobModel2.getCatalogName();
					final HSSFRow rowLatam = latamsheet.createRow(latamRowNum);
					final HSSFRow rowEmea = emeasheet.createRow(emeaRowNum);
					final HSSFRow rowWesell = wesellsheet.createRow(weSellRowNum);


					String status = null;



					if (null == energizerCronJobModel2.getResult())
					{
						status = StringUtils.EMPTY;
					}
					else if (null != energizerCronJobModel2.getResult()
							&& energizerCronJobModel2.getResult().getCode().equalsIgnoreCase("UNKNOWN"))
					{
						status = "NEW";

					}

					else
					{
						status = energizerCronJobModel2.getResult().getCode();
					}




					final boolean dataFeedJob = energizerCronJobModel2.getDataFeedJob() == null ? false
							: energizerCronJobModel2.getDataFeedJob();

					if (null != catalogName
							&& catalogName.equalsIgnoreCase(configurationService.getConfiguration().getString("catalogName")))
						{

						/* weSell Cronjobs Start */

						if (null != energizerCronJobModel2.getPath()
								&& energizerCronJobModel2.getPath().contains(EnergizerCoreConstants.WESELL))
						{

							if(dataFeedJob== true)
							{

							rowWesell.createCell(0).setCellValue(sNoWesell);
							rowWesell.createCell(1).setCellValue(energizerCronJobModel2.getCode());
							rowWesell.createCell(2).setCellValue(status);
							weSellRowNum = weSellRowNum + 1;
							sNoWesell = sNoWesell + 1;
							LOG.info("weSell jobs name :" + energizerCronJobModel2.getCode() + " Status:"
									+ energizerCronJobModel2.getResult());
						}
						}

						/* weSell cronjobs end */
						/* Latam cronjobs start */
						else
						{
							if(dataFeedJob== true)
							{
							rowLatam.createCell(0).setCellValue(sNoLatam);
							rowLatam.createCell(1).setCellValue(energizerCronJobModel2.getCode());
							rowLatam.createCell(2).setCellValue(status);
							latamRowNum = latamRowNum + 1;
							sNoLatam = sNoLatam + 1;
							LOG.info("LAtam job name :" + energizerCronJobModel2.getCode() + " Status:"
									+ energizerCronJobModel2.getResult());

							}
						}
						/* Latam cronjob end */

						}
					/* Emea cronjobs start */
					else if (null != catalogName
							&& catalogName.equalsIgnoreCase(configurationService.getConfiguration().getString("catalogNameEMEA")))
					{
						if(dataFeedJob== true)
						{
							rowEmea.createCell(0).setCellValue(sNoEmea);
						rowEmea.createCell(1).setCellValue(energizerCronJobModel2.getCode());
						rowEmea.createCell(2).setCellValue(status);
						emeaRowNum = emeaRowNum + 1;
						sNoEmea = sNoEmea + 1;
						LOG.info(
								"EMEA job name :" + energizerCronJobModel2.getCode() + " Status:" + energizerCronJobModel2.getResult());
						configurationService.getConfiguration().getString("site.personalCareEMEA");

					}
					}
					/* Emea cronjobs end */
				}




				fileOutLatam = new FileOutputStream(dirLatam);
				fileOutEmea = new FileOutputStream(dirEmea);
				fileOutWesell = new FileOutputStream(dirWesell);

				workBookLatam.write(fileOutLatam);
				workBookEmea.write(fileOutEmea);
				workBookWesell.write(fileOutWesell);

				fileOutLatam.close();
				fileOutEmea.close();
				fileOutWesell.close();
				final boolean mailSent = sendEmailNotification(dirLatam, dirEmea, dirWesell);

				if (mailSent)
				{
					deleteFile(dirLatam);
					deleteFile(dirEmea);
					deleteFile(dirWesell);

				}


			}



		}
		 catch(final Exception e) {
			e.printStackTrace();
		 }

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}


	/**
	 * @param dir
	 * @return
	 */
	private boolean sendEmailNotification(final File dirLatam, final File dirEmea, final File dirWesell)
	{

		LOG.info("Inside sendEmailNotification");
		boolean isMailSent = false;
		final List<EmailAddressModel> toAddresses = new ArrayList<EmailAddressModel>();
		final List<EmailAddressModel> ccAddresses = new ArrayList<EmailAddressModel>();
		final List<EmailAddressModel> bccAddresses = new ArrayList<EmailAddressModel>();

		DataInputStream latamInputStream = null;
		DataInputStream emeaInputStream = null ;
		DataInputStream weSellInputStream = null;

		try
		{
			final String mailFrom = configurationService.getConfiguration().getString("order.stuck.mail.from");
			if (mailFrom.isEmpty())
			{
				LOG.info(" Mail from is not defined. The email alert could not be sent. ");
				return false;
			}
			final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(mailFrom, mailFrom);

			final List<EmailAttachmentModel> attachments = new ArrayList<EmailAttachmentModel>();

			latamInputStream = new DataInputStream(new FileInputStream(dirLatam));
			emeaInputStream = new DataInputStream(new FileInputStream(dirEmea));
			weSellInputStream =new DataInputStream(new FileInputStream(dirWesell));

			final EmailAttachmentModel EmailAttachmentModelLatam = emailService
					.createEmailAttachment(latamInputStream, dirLatam.getName(), MIME_TYPE);
			final EmailAttachmentModel EmailAttachmentModelEmea = emailService
					.createEmailAttachment(emeaInputStream, dirEmea.getName(), MIME_TYPE);

			final EmailAttachmentModel EmailAttachmentModelWesell = emailService
					.createEmailAttachment(weSellInputStream, dirWesell.getName(), MIME_TYPE);

			attachments.add(EmailAttachmentModelLatam);
			attachments.add(EmailAttachmentModelEmea);
			attachments.add(EmailAttachmentModelWesell);

			String subject = "";
			final StringBuilder body = new StringBuilder();

			final String replyToAddress = null;
			final String mailEnvironment = configurationService.getConfiguration().getString("mail.environment.stuck.orders");


			subject = "Status of cronjobs in " + mailEnvironment.toUpperCase() + " environment !";
			body.append("Hi,");
			body.append("<br/>");
			body.append("<br/>");
			body.append("Please find the attached cronjobs status in Hybris " + mailEnvironment.toUpperCase()
					+ " environment and kindly check with the SAP team for any data related issues as mentioned in the attached excel sheet.");

			body.append("<br/>");
			body.append("<br/>");
			body.append("Thanks,");
			body.append("<br/>");
			body.append("Edgewell Personal Care Portal Team");
			body.append("<br/>");
			body.append("Note: This is an automatically generated email. Please do not reply to this mail.");



			final String mailTo = configurationService.getConfiguration().getString("order.stuck.mail.to");
			if (mailTo.isEmpty())
			{
				LOG.info(" Mail to is not defined. The email alert could not be sent. ");
				return false;
			}
			for (final String toAddress : mailTo.split(","))
			{
				final EmailAddressModel toAddressEmail = emailService.getOrCreateEmailAddressForEmail(toAddress, toAddress);

				toAddresses.add(toAddressEmail);
			}

			final EmailMessageModel email = emailService.createEmailMessage(toAddresses, ccAddresses, bccAddresses, fromAddress,
					replyToAddress, subject, body.toString(), attachments);

			emailService.send(email);
			isMailSent = true;
		}
		catch (final Exception ex)
		{
			isMailSent = false;
			LOG.info("Exception Caught while sending email" + ex);
		}finally{
			if(null != latamInputStream){
				try {
					latamInputStream.close();
				} catch (IOException e) {
					LOG.error(e.getMessage());
				}
			}
			if(null != emeaInputStream){
				try {
					emeaInputStream.close();
				} catch (IOException e) {
					LOG.error(e.getMessage());
				}
			}
			if(null != weSellInputStream){
				try {
					weSellInputStream.close();
				} catch (IOException e) {
					LOG.error(e.getMessage());
				}
			}
		}

		return isMailSent;

	}


	/**
	 * @param dir
	 */
	private void deleteFile(final File file)
	{
		// YTODO Auto-generated method stub
		if (file.exists())
		{
			file.delete();
			LOG.info("Deleting the file from the folder");
		}

	}


	private String getSite(final EnergizerCronJobModel energizerCronJobModel)
	{
		final String catalogName = energizerCronJobModel.getCatalogName();
		String site = null;

		if (catalogName.equalsIgnoreCase(configurationService.getConfiguration().getString("catalogName")))
		{
			site = configurationService.getConfiguration().getString("site.personalCare");
		}

		else if (catalogName.equalsIgnoreCase(configurationService.getConfiguration().getString("catalogNameEMEA")))
		{
			site = configurationService.getConfiguration().getString("site.personalCareEMEA");
		}
		return site;
	}


}
