/**
 *
 */
package com.energizer.core.order;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.services.order.EnergizerOrderService;


/**
 * @author KA289322
 *
 */
public class EnergizerOrderStuckJob extends AbstractJobPerformable<EnergizerCronJobModel>
{

	@Resource
	EnergizerOrderService energizerOrderService;

	@Resource
	private ConfigurationService configurationService;

	@Resource
	EmailService emailService;

	final Logger LOG = Logger.getLogger(EnergizerOrderStuckJob.class);

	final String MIME_TYPE = "application/vnd.ms-excel";

	final String PERSONALCARE = "personalCare";

	final String PERSONALCAREEMEA = "personalCareEMEA";

	final String WESELL = "weSell";

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel )
	 */
	@Override
	public PerformResult perform(final EnergizerCronJobModel energizerCronJobModel)
	{
		// YTODO Auto-generated method stub
		final List<OrderStatus> validStates = new ArrayList<OrderStatus>();
		validStates.add(OrderStatus.ASSIGNED_TO_ADMIN);
		validStates.add(OrderStatus.B2B_PROCESSING_ERROR);


		final StringBuilder sb = new StringBuilder();
		final List<String> orderStatusList = new ArrayList<String>();

		for (final OrderStatus status : validStates)
		{
			sb.append("'").append(status).append("'").append(",");
		}

		final String status = sb.toString();
		String Statuslist = null;
		if (status.endsWith(","))
		{
			Statuslist = status.substring(0, status.length() - 1);
		}
		orderStatusList.add(Statuslist);


		final String site = getSite(energizerCronJobModel);
		final String region = energizerCronJobModel.getRegion();
		final List<Date> dateList = getDates(energizerCronJobModel);
		final List<Object> orderList = energizerOrderService.getAllStuckOrders(orderStatusList, dateList, site, region);
		if (null != orderList && !orderList.isEmpty())
		{
			final boolean isFileWritten = writingToExcel(orderList, site, region);

			if (isFileWritten)
			{
				return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
			}
			else
			{
				LOG.info(" Data is not written into Excel or file is not sent in Mail ");
				return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
			}
		}


		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}


	/**
	 * @param energizerCronJobModel
	 * @return
	 */
	private List<Date> getDates(final EnergizerCronJobModel energizerCronJobModel)
	{
		final Calendar calendar = Calendar.getInstance();
		final List<Date> dates = new ArrayList<Date>();
		Date startDate;
		Date endDate;

		if (energizerCronJobModel.getFromDate() != null && energizerCronJobModel.getToDate() != null)
		{
			calendar.setTime(energizerCronJobModel.getFromDate());
			startDate = calendar.getTime();
			LOG.info("energizerCronJobModel.getFromDate() " + startDate);
			calendar.setTime(energizerCronJobModel.getToDate());
			endDate = calendar.getTime();
			LOG.info("energizerCronJobModel.getToDate() " + endDate);
		}
		else
		{
			if (energizerCronJobModel.getFromDate() != null)
			{
				calendar.setTime(energizerCronJobModel.getFromDate());
				startDate = calendar.getTime();
				LOG.info("In else energizerCronJobModel.getFromDate() " + startDate);

			}
			else
			{
				calendar.add(calendar.DATE, -1);
				calendar.set(calendar.HOUR, 0);
				calendar.set(calendar.MINUTE, 0);
				calendar.set(calendar.SECOND, 0);
				startDate = calendar.getTime();
				LOG.info("startDate " + startDate);
			}
			if (energizerCronJobModel.getToDate() != null)
			{
				calendar.setTime(energizerCronJobModel.getToDate());
				endDate = calendar.getTime();
				LOG.info("In else energizerCronJobModel.getFromDate() " + endDate);
			}
			else
			{
				endDate = calendar.getInstance().getTime();
				LOG.info("endDate " + endDate);
			}
		}
		dates.add(startDate);
		dates.add(endDate);

		return dates;

	}


	/**
	 * @param energizerCronJobModel
	 * @return
	 */
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


	private boolean writingToExcel(final List<Object> orderList, final String site, final String region)
	{
		boolean isFileWritten = false;
		FileOutputStream fileOut;
		final String FILE_PATH = configurationService.getConfiguration().getString("catalogdownload.downloadPath");

		try
		{
			final HSSFWorkbook workBook = new HSSFWorkbook();

			final HSSFSheet sheet = workBook.createSheet("Order Stuck in Hybris");
			int rownum = 0;
			final int columnnum = 0;
			String headerColumns = null;
			boolean wesellOrder = false;
			if (null != region && StringUtils.isNotEmpty(region) && region.equalsIgnoreCase("WESELL"))
			{
				wesellOrder = true;
				headerColumns = configurationService.getConfiguration().getString("wesell.orderStuck.download.header.columns");
			}
			else
			{
				headerColumns = configurationService.getConfiguration().getString("orderStuck.download.header.columns");
			}

			final String[] columnArray = headerColumns.split(",");


			HSSFRow row = sheet.createRow(rownum);
			HSSFCell cell = row.createCell(columnnum);

			if (rownum == 0)
			{
				for (int k = 0; k < columnArray.length; k++)
				{
					cell = row.createCell(k);
					final String rowheader = columnArray[k];

					cell.setCellValue(rowheader);
				}

			}

			for (final Object obj : orderList)
			{
				row = sheet.createRow(++rownum);

				final String objData = obj.toString();
				final String[] arrdata = objData.split(",");

				for (int i = 0; i < arrdata.length + 1; i++)
				{
					cell = row.createCell(i);
					if (i == arrdata.length)
					{
						cell.setCellType(CellType.STRING);
						cell.setCellValue(region);
					}
					else
					{
						String data = arrdata[i].trim();

						if (data == null || data.equalsIgnoreCase("null") || StringUtils.isEmpty(data))
						{
							data = StringUtils.EMPTY;
						}


						if (data.startsWith("=") || data.startsWith("\""))
						{
							cell.setCellType(CellType.STRING);
							data = data.replaceAll("\"", "");
							data = data.replaceAll("=", "");

							cell.setCellValue(data);
						}
						else if (null == data || data.isEmpty())
						{
							cell.setCellType(CellType.NUMERIC);
							cell.setCellValue(StringUtils.EMPTY);
						}
						else
						{
							data = data.replaceAll("\"", "");
							data = data.replaceAll("\\[", "");
							data = data.replaceAll("\\]", "");
							if (wesellOrder && i == 11)
							{
								cell.setCellType(CellType.NUMERIC);
								final double totalValWithTax = (Double.parseDouble(arrdata[i - 1].trim()) + Double.parseDouble(data));
								cell.setCellValue(String.valueOf(totalValWithTax));
							}
							else
							{
								cell.setCellType(CellType.NUMERIC);
								cell.setCellValue(data);
							}
						}
					}
				}

			}

			final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			final String dateTime = sdf.format(new Date());
			final File dir = new File(System.getProperty("user.home") + "\\" + FILE_PATH + "\\" + "OrderStuck_" + dateTime + ".xls");
			fileOut = new FileOutputStream(dir);
			workBook.write(fileOut);
			fileOut.close();
			final boolean mailSent = sendEmailNotification(dir, site, region);
			if (mailSent)
			{
				deleteFile(dir);
			}
			isFileWritten = true;
		}
		catch (final Exception ex)
		{
			LOG.info("Exception Caught while writing the data into Excel" + ex);
		}
		return isFileWritten;
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


	private boolean sendEmailNotification(final File file, final String site, final String region)
	{
		LOG.info("Inside sendEmailNotification");
		boolean isMailSent = false;
		final List<EmailAddressModel> toAddresses = new ArrayList<EmailAddressModel>();
		final List<EmailAddressModel> ccAddresses = new ArrayList<EmailAddressModel>();
		final List<EmailAddressModel> bccAddresses = new ArrayList<EmailAddressModel>();
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

			final EmailAttachmentModel EmailAttachmentModel = emailService
					.createEmailAttachment(new DataInputStream(new FileInputStream(file)), file.getName(), MIME_TYPE);

			attachments.add(EmailAttachmentModel);

			String subject = "";
			final StringBuilder body = new StringBuilder();

			final String replyToAddress = null;
			final String mailEnvironment = configurationService.getConfiguration().getString("mail.environment.stuck.orders");

			if (site.equalsIgnoreCase(PERSONALCARE))
			{
				if (null != region && StringUtils.isNotEmpty(region) && region.equalsIgnoreCase("WESELL"))
				{
					subject = "Order Stuck in hybris WESELL site in " + mailEnvironment.toUpperCase() + " environment !";
				}
				else
				{
					subject = "Order Stuck in hybris LATAM site in " + mailEnvironment.toUpperCase() + " environment !";
				}
				body.append("Hi,");
				body.append("<br/>");
				body.append("<br/>");
				if (null != region && StringUtils.isNotEmpty(region) && region.equalsIgnoreCase("WESELL"))
				{
					body.append("Please find the attached orders blocked in hybris WESELL in " + mailEnvironment.toUpperCase()
							+ " environment and it needs further action to release the orders.");
				}
				else
				{
					body.append("Please find the attached orders blocked in hybris LATAM in " + mailEnvironment.toUpperCase()
							+ " environment and it needs further action to release the orders.");
				}

				body.append("<br/>");
				body.append("<br/>");
				body.append("Thanks,");
				body.append("<br/>");
				body.append("Edgewell Personal Care Portal Team");
				body.append("<br/>");
				body.append("Note: This is an automatically generated email. Please do not reply to this mail.");
			}

			else if (site.equalsIgnoreCase(PERSONALCAREEMEA))
			{
				subject = "Order Stuck in hybris EMEA site in " + mailEnvironment.toUpperCase() + " environment !";
				body.append("Hi,");
				body.append("<br/>");
				body.append("<br/>");
				body.append("Please find the attached orders blocked in hybris EMEA in " + mailEnvironment.toUpperCase()
						+ " environment and it needs further action to release the orders.");
				body.append("<br/>");
				body.append("<br/>");
				body.append("Thanks,");
				body.append("Edgewell Personal Care Portal Team");
				body.append("<br/>");
				body.append("Note: This is an automatically generated email. Please do not reply to this mail.");


			}
			else if (site.equalsIgnoreCase(WESELL))
			{
				subject = "Order Stuck in hybris WESELL site in " + mailEnvironment.toUpperCase() + " environment !";
				body.append("Hi,");
				body.append("<br/>");
				body.append("<br/>");
				body.append("Please find the attached orders blocked in hybris WESELL in " + mailEnvironment.toUpperCase()
						+ " environment and it needs further action to release the orders.");
				body.append("<br/>");
				body.append("<br/>");
				body.append("Thanks,");
				body.append("<br/>");
				body.append("Edgewell Personal Care Portal Team");
				body.append("<br/>");
				body.append("Note: This is an automatically generated email. Please do not reply to this mail.");


			}

			//body.append(System.getProperty("line.separator"));
			/*
			 * Sending email notification for communicating the data mismatch to internal team members.
			 */
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
		}

		return isMailSent;
	}
}
