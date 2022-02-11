/**
 * 
 */
package com.energizer.storefront.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.energizer.core.datafeed.facade.impl.DefaultEnergizerPasswordExpiryFacade;
import com.energizer.core.model.EnergizerB2BCustomerModel;


/**
 * @author M1023278
 * 
 */
public class EnergizerPasswordNotificationUtil
{
	private static final Logger LOG = Logger.getLogger(EnergizerPasswordNotificationUtil.class);
	@Resource
	protected DefaultEnergizerPasswordExpiryFacade defaultEnergizerPasswordExpiryFacade;

	@Value("${passwordExpiryDays}")
	int passwordExpiryDays;

	@Value("${passwordNotificationDays}")
	int passwordNotificationDays;

	public List<String> checkPasswordExpiryStatus(final String userName)
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		final Date maxDate;
		String returnMessage = "";
		String messageValue = "";
		String daysCount = "";
		EnergizerB2BCustomerModel b2bCustomerModel = null;
		final List<String> notificationMessages = new ArrayList<String>();
		if (null != userName)
		{

			b2bCustomerModel = defaultEnergizerPasswordExpiryFacade.getCustomerByUID(userName);

			if (null != b2bCustomerModel && null != b2bCustomerModel.getPasswordModifiedTime())
			{
				final Date latestModifiedTime = b2bCustomerModel.getPasswordModifiedTime();
				final Calendar calPasswordModifiedDate = Calendar.getInstance();
				calPasswordModifiedDate.setTime(latestModifiedTime);

				LOG.info("Calender password modified date is " + calPasswordModifiedDate.getTime());
				calPasswordModifiedDate.add(Calendar.DATE, passwordExpiryDays);

				LOG.info("Calender::: Pasword Expiry Date " + calPasswordModifiedDate.getTime());
				final Calendar calNotificationDate = Calendar.getInstance();


				if (calNotificationDate.compareTo(calPasswordModifiedDate) <= 0)
				{
					calNotificationDate.add(Calendar.DATE, passwordNotificationDays);

					LOG.info("Calender Notification date " + calNotificationDate.getTime());
					if (calNotificationDate.compareTo(calPasswordModifiedDate) <= 0)
					{
						if (null != b2bCustomerModel)
						{
							LOG.info("Dear " + b2bCustomerModel.getEmail() + " Your password has not been expired");
						}
					}
					else
					{
						final Calendar calCurrentDate = Calendar.getInstance();
						final Integer remainingDays = calPasswordModifiedDate.getTime().getDate() - calCurrentDate.getTime().getDate();

						LOG.info("Your Password will expire in " + remainingDays);
						if (remainingDays <= 10 && remainingDays >= 1)
						{
							returnMessage = "account.password.expiry.notification";
							daysCount = remainingDays.toString();
							messageValue = "0";
						}
					}
				}
				else
				{
					returnMessage = "account.password.expiry";
					messageValue = "1";

				}
				notificationMessages.add(messageValue);
				notificationMessages.add(returnMessage);
				if (null != daysCount)
				{
					notificationMessages.add(daysCount);
				}
			}

		}
		return notificationMessages;
	}
}
