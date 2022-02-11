/**
 *
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.b2b.constants.B2BConstants;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.processor.exception.B2BGroupUnknownIdentifierException;
import com.energizer.core.datafeed.processor.exception.B2BUnitUnknownIdentifierException;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCronJobModel;


/**
 *
 * This processors imports the b2b users.
 *
 * Sample file will look like
 *
 * userID, userName, contactNumber, active, email, defaultB2BUnit, groups, approvers omkar, omkar, 123, Y, a@a.com,
 * 1000, b2bcustomergroup, abc@m.com
 *
 * Total column count : 8
 */
public class EnergizerB2BUserCSVProcessor extends AbstractEnergizerCSVProcessor
{

	private static final Logger LOG = Logger.getLogger(EnergizerB2BUserCSVProcessor.class);
	@Resource
	private ModelService modelService;

	@Resource
	private B2BCommerceUnitService b2bCommerceUnitService;
	@Resource
	private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService;
	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Resource(name = "userService")
	private UserService userService;

	private static final String EMAIL_ID = "email";
	private static final String USER_NAME = "userName";
	private static final String CONTACT_NUMBER = "contactNumber";
	private static final String DEFAULT_B2B_UNIT = "defaultB2BUnit";
	private static final String APPROVERS = "approvers";
	private static final String ACTIVE = "active";
	private static final String GROUPS = "groups";
	private static final String USERID = "userID";

	/**
	 *
	 */
	public EnergizerB2BUserCSVProcessor()
	{
		super();
	}

	/**
	 * This process will create only admin group users. if there was any other group users specified in the datafeed,
	 * those data will be ignored.
	 *
	 **/
	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		EnergizerB2BCustomerModel b2bCustomerModel = null;
		long succeedRecord = getRecordSucceeded();
		for (final CSVRecord record : records)
		{
			final Map<String, String> csvValuesMap = record.toMap();
			validate(record);
			if (!getBusinessFeedErrors().isEmpty())
			{
				csvFeedErrorRecords.addAll(getBusinessFeedErrors());
				getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
				getBusinessFeedErrors().clear();
				continue;
			}
			try
			{
				b2bCustomerModel = userService.getUserForUID(csvValuesMap.get(EMAIL_ID).trim().toLowerCase(),
						EnergizerB2BCustomerModel.class);
				updateB2BCustomer(csvValuesMap, b2bCustomerModel);
				succeedRecord++;
				setRecordSucceeded(succeedRecord);
			}
			catch (final UnknownIdentifierException exception)
			{
				try
				{
					createB2BCustomer(csvValuesMap, record, b2bCustomerModel);
					succeedRecord++;
					setRecordSucceeded(succeedRecord);
				}
				catch (final UnknownIdentifierException identifierException)
				{

					if (identifierException instanceof B2BGroupUnknownIdentifierException)
					{
						//invalid group supplied
						long recordFailed = getBusRecordError();
						final EnergizerCSVFeedError error = new EnergizerCSVFeedError();
						final List<String> columnNames = new ArrayList<String>();
						final List<Integer> columnNumbers = new ArrayList<Integer>();
						error.setLineNumber(record.getRecordNumber() + 1);
						columnNames.add(GROUPS);
						error.setColumnName(columnNames);
						error.setUserType(BUSINESS_USER);
						error.setMessage("Invalid group Specified");
						columnNumbers.add(7);
						error.setColumnNumber(columnNumbers);
						getBusinessFeedErrors().add(error);
						setBusRecordError(getBusinessFeedErrors().size());
						recordFailed++;
						setBusRecordError(recordFailed);
						continue;
					}
					if (identifierException instanceof B2BUnitUnknownIdentifierException)
					{
						// unit is null
						long recordFailed = getBusRecordError();
						final EnergizerCSVFeedError error = new EnergizerCSVFeedError();
						final List<String> columnNames = new ArrayList<String>();
						final List<Integer> columnNumbers = new ArrayList<Integer>();
						error.setLineNumber(record.getRecordNumber() + 1);
						columnNames.add(DEFAULT_B2B_UNIT);
						error.setColumnName(columnNames);
						error.setUserType(BUSINESS_USER);
						error.setMessage("Invalid B2BUnit Specified");
						columnNumbers.add(6);
						error.setColumnNumber(columnNumbers);
						getBusinessFeedErrors().add(error);
						setBusRecordError(getBusinessFeedErrors().size());
						recordFailed++;
						setBusRecordError(recordFailed);
						continue;
					}
				}
			}
		}
		getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
		getTechnicalFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	private void updateB2BCustomer(final Map<String, String> csvValuesMap, final EnergizerB2BCustomerModel b2bCustomerModel)
	{

		final EnergizerB2BUnitModel b2bUnitModel = (EnergizerB2BUnitModel) b2bUnitService
				.getUnitForUid(csvValuesMap.get(DEFAULT_B2B_UNIT).trim());
		if (null != b2bUnitModel)
		{
			//			b2bCustomerModel.setEmail(csvValuesMap.get(EMAIL_ID).trim());
			//		b2bCustomerModel.setOriginalUid(csvValuesMap.get(EMAIL_ID).trim());
			//		b2bCustomerModel.setCustomerID(csvValuesMap.get(EMAIL_ID).trim());
			b2bCustomerModel.setName(csvValuesMap.get(USER_NAME).trim());
			b2bCustomerModel.setContactNumber(csvValuesMap.get(CONTACT_NUMBER).trim());
			final boolean activeStatus = csvValuesMap.get(ACTIVE).trim().equalsIgnoreCase("Y") ? true : false;
			b2bCustomerModel.setActive(activeStatus);
			// Added for multiple currency implementation
			b2bCustomerModel.setSessionCurrency(b2bUnitModel.getCurrencyPreference());
			//	Added validation for groups fields
			final Set<PrincipalGroupModel> customerGroups = new HashSet<PrincipalGroupModel>(b2bCustomerModel.getGroups());
			for (final String group : csvValuesMap.get(GROUPS).split(";"))
			{
				final UserGroupModel userGroupModel = userService.getUserGroupForUID(group);
				if (!customerGroups.contains(userGroupModel))
				{
					customerGroups.add(userGroupModel);
				}
			}
			b2bCustomerModel.setGroups(customerGroups);
			modelService.saveAll(b2bCustomerModel);
			LOG.info("EnergizerB2BUser updated" + b2bCustomerModel.getUid() + " & Email ID" + b2bCustomerModel.getEmail());
		}
		else
		{
			throw new B2BUnitUnknownIdentifierException("Invalid Unit Specified");
		}
	}

	private void createB2BCustomer(final Map<String, String> csvValuesMap, final CSVRecord record,
			EnergizerB2BCustomerModel b2bCustomerModel)
	{
		b2bCustomerModel = modelService.create(EnergizerB2BCustomerModel.class);
		final EnergizerB2BUnitModel b2bUnitModel = (EnergizerB2BUnitModel) b2bUnitService
				.getUnitForUid(csvValuesMap.get(DEFAULT_B2B_UNIT).trim());
		if (null != b2bUnitModel)
		{
			b2bCustomerModel.setUid(csvValuesMap.get(EMAIL_ID).trim().toLowerCase());
			b2bCustomerModel.setEmail(csvValuesMap.get(EMAIL_ID).trim().toLowerCase());
			b2bCustomerModel.setOriginalUid(b2bCustomerModel.getUid());
			b2bCustomerModel.setCustomerID(b2bCustomerModel.getUid());
			b2bCustomerModel.setName(csvValuesMap.get(USER_NAME).trim());
			b2bCustomerModel.setContactNumber(csvValuesMap.get(CONTACT_NUMBER).trim());
			final boolean activeStatus = csvValuesMap.get(ACTIVE).trim().equalsIgnoreCase("Y") ? true : false;
			b2bCustomerModel.setActive(activeStatus);
			// Added for multiple currency implementation
			b2bCustomerModel.setSessionCurrency(b2bUnitModel.getCurrencyPreference());
			//   Added validation for groups fields
			final Set<PrincipalGroupModel> customerGroups = new HashSet<PrincipalGroupModel>();
			for (final String group : csvValuesMap.get(GROUPS).split(";"))
			{
				try
				{
					final UserGroupModel userGroupModel = userService.getUserGroupForUID(group);
					if (!customerGroups.contains(userGroupModel))
					{
						customerGroups.add(userGroupModel);
					}
				}
				catch (final UnknownIdentifierException b2bGroupUnknownIdentifierException)
				{
					throw new B2BGroupUnknownIdentifierException("Invalid User group specified");
				}
			}
			b2bCustomerModel.setGroups(customerGroups);
			b2bCustomerModel.setDefaultB2BUnit(b2bUnitModel);
			modelService.saveAll(b2bCustomerModel);
			LOG.info("Created user with userID : " + b2bCustomerModel.getUid() + " & Email ID : " + b2bCustomerModel.getEmail());
		}
		else
		{
			throw new B2BUnitUnknownIdentifierException("Invalid Unit Specified");
		}
	}

	/**
	 * @param record
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		Integer columnNumber = 0;
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader).trim();
			if (!columnHeader.equalsIgnoreCase(ACTIVE))
			{
				if (value.isEmpty())
				{
					long recordFailed = getBusRecordError();
					error = new EnergizerCSVFeedError();
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error.setLineNumber(record.getRecordNumber() + 1);
					columnNames.add(columnHeader);
					error.setColumnName(columnNames);
					error.setUserType(BUSINESS_USER);
					error.setMessage(columnHeader + " column should not be empty");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					getBusinessFeedErrors().add(error);
					recordFailed++;
					setBusRecordError(recordFailed);
				}
			}
			if (columnHeader.equalsIgnoreCase(GROUPS))
			{

				if (!(value.contains(B2BConstants.B2BADMINGROUP) && value.contains("personalCare")))
				{
					long recordFailed = getBusRecordError();
					error = new EnergizerCSVFeedError();
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error.setLineNumber(record.getRecordNumber() + 1);
					columnNames.add(GROUPS);
					error.setColumnName(columnNames);
					error.setUserType(BUSINESS_USER);
					error.setMessage("Invalid group specified");
					columnNumbers.add(7);
					error.setColumnNumber(columnNumbers);
					getBusinessFeedErrors().add(error);
					recordFailed++;
					setBusRecordError(recordFailed);

				}
			}
			// Added validation for active fields
			if (columnHeader.equalsIgnoreCase(ACTIVE) && (!value.equalsIgnoreCase("Y") && (!value.equalsIgnoreCase("N"))))
			{
				long recordFailed = getBusRecordError();
				error = new EnergizerCSVFeedError();
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error.setLineNumber(record.getRecordNumber() + 1);
				columnNames.add(columnHeader);
				error.setColumnName(columnNames);
				error.setUserType(BUSINESS_USER);
				error.setMessage(columnHeader + " column should be Y (for Active) Or N (for InActive)");
				columnNumbers.add(columnNumber);
				error.setColumnNumber(columnNumbers);
				getBusinessFeedErrors().add(error);
				recordFailed++;
				setBusRecordError(recordFailed);
			}
		}
	}


}
