/**
 *
 */
package com.energizer.storefront.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;


/**
 * @author M1023097
 *
 */
public class EnergizerDateTimeUtil
{

	protected static final Logger LOG = Logger.getLogger(EnergizerDateTimeUtil.class);

	public static String displayDate(final String fetchedDate) throws ParseException
	{

		final SimpleDateFormat DBDateStringfmt = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");

		final DateFormat UIdateFormat = new SimpleDateFormat("MM/dd/yyyy");

		final Date convertedDate = DBDateStringfmt.parse(fetchedDate);

		final String displayDate = UIdateFormat.format(convertedDate);

		return displayDate;
	}

	public static String displayRequestedDeliveryDate(final String fetchedDate, final String dateFormat) throws ParseException
	{

		final SimpleDateFormat DBDateStringfmt = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");

		final DateFormat UIdateFormat = new SimpleDateFormat(dateFormat);

		final Date convertedDate = DBDateStringfmt.parse(fetchedDate);

		final String displayDate = UIdateFormat.format(convertedDate);

		return displayDate;
	}

	public static long getTimeInMilliSeconds(final String fetchedDate) throws ParseException
	{
		final SimpleDateFormat DBDateStringfmt = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");

		final Date convertedDate = DBDateStringfmt.parse(fetchedDate);
		LOG.info("convertedDate ::: " + convertedDate);

		final Calendar cal = Calendar.getInstance();
		cal.setTime(convertedDate);

		LOG.info("cal.getTimeInMillis() ::: " + cal.getTimeInMillis());
		LOG.info("cal.getTime() ::: " + cal.getTime());

		return cal.getTimeInMillis();

	}

}
