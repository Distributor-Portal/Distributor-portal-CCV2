/**
 *
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.processor.exception.B2BGroupUnknownIdentifierException;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.wesell.virtualid.creation.EnergizerWesellVirtualID;



public class EnergizerB2BEmployeeCSVProcessor extends AbstractEnergizerCSVProcessor
{

	private static final Logger LOG = Logger.getLogger(EnergizerB2BEmployeeCSVProcessor.class);
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

	@Resource
	EnergizerWesellVirtualID energizerWesellVirtualID;

	@Autowired
	protected ConfigurationService configurationService;

	private static final String SALESREP_USERNAME = "salesRepUsername";
	private static final String SALESREP_CODE = "salesRepCode";
	private static final String SALESREP_NAME = "salesRepName";
	private static final String CONTACT_NUMBER = "contactNumber";
	private static final String ACTIVE = "active";
	private static final String ISSALESREP = "isSalesRep";
	private static final String EMAIL_ID = "email";
	private static final String B2B_UNIT_LIST = "b2bUnitList";
	private static final String GROUPS = "groups";



	/**
	 *
	 */
	public EnergizerB2BEmployeeCSVProcessor()
	{
		super();
	}

	/**
	 * This process will create only admin group users. if there was any other group users specified in the datafeed, those
	 * data will be ignored.
	 *
	 **/
	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		EnergizerB2BEmployeeModel b2bEmployeeModel = null;
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
				b2bEmployeeModel = userService.getUserForUID(csvValuesMap.get(SALESREP_USERNAME).trim().toLowerCase(),
						EnergizerB2BEmployeeModel.class);
				updateB2BEmployee(csvValuesMap, b2bEmployeeModel);
				succeedRecord++;
				setRecordSucceeded(succeedRecord);
			}

			catch (final UnknownIdentifierException exception)
			{
				try
				{
					createB2BEmployee(csvValuesMap, record, b2bEmployeeModel);
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
						error.setLineNumber(record.getRecordNumber());
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
				}
			}
		}
		getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
		getTechnicalFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	private void updateB2BEmployee(final Map<String, String> csvValuesMap, final EnergizerB2BEmployeeModel b2bEmployeeModel)
	{
		if (null != b2bEmployeeModel)
		{
			EnergizerB2BCustomerModel customer = null;
			final String salesRepCode = csvValuesMap.get(SALESREP_CODE).trim().toLowerCase();
			b2bEmployeeModel.setSalesRepCode(csvValuesMap.get(SALESREP_CODE).trim().toLowerCase());
			final String fullName = csvValuesMap.get(SALESREP_NAME).trim();
			b2bEmployeeModel.setName(fullName);
			if (null != fullName)
			{
				b2bEmployeeModel.setFirstName(fullName.contains(" ") ? fullName.split(" ")[0] : fullName);
				b2bEmployeeModel.setLastName(fullName.contains(" ") ? fullName.substring(fullName.indexOf(" ")).trim() : null);
			}
			b2bEmployeeModel.setContactNumber(csvValuesMap.get(CONTACT_NUMBER).trim());
			final boolean activeStatus = csvValuesMap.get(ACTIVE).trim().equalsIgnoreCase("Y") ? true : false;
			b2bEmployeeModel.setActive(activeStatus);
			final boolean isSalesRep = csvValuesMap.get(ISSALESREP).trim().equalsIgnoreCase("Y") ? true : false;
			b2bEmployeeModel.setIsSalesRep(isSalesRep);
			b2bEmployeeModel.setEmail(csvValuesMap.get(EMAIL_ID).trim().toLowerCase());
			//b2bEmployeeModel.setPriority(csvValuesMap.get(PRIORITY).trim());
			final Set<EnergizerB2BUnitModel> b2bUnitList = new HashSet<EnergizerB2BUnitModel>();
			final Set<String> salesOrgSet = new HashSet<String>();
			final Set<String> currencySet = new HashSet<String>();

			for (final String b2bunit : csvValuesMap.get(B2B_UNIT_LIST).split(";"))
			{
				final EnergizerB2BUnitModel b2bUnitModel = (EnergizerB2BUnitModel) b2bUnitService.getUnitForUid(b2bunit);
				if (null != b2bUnitModel)
				{
					salesOrgSet.add(b2bUnitModel.getSalesOrganisation());
					currencySet.add(b2bUnitModel.getCurrencyPreference().getIsocode());
					b2bUnitList.add(b2bUnitModel);

					boolean salesRepCustomerCreated = false;
					/*-final String wesellCustomerUid = salesRepCode + "_" + b2bUnitModel.getUid()
							+ getConfigValue(EnergizerCoreConstants.WESELL_USERID_SUFFIX);*/
					final String virtualIDPrefix = b2bEmployeeModel.getUid().split("@")[0].toLowerCase();
					//Example - alejandro.medina_0098112157@edgewell.com created as a virtual ID for sales rep individual cart maintenance
					final String wesellCustomerUid = virtualIDPrefix + "_" + b2bUnitModel.getUid()
							+ getConfigValue(EnergizerCoreConstants.WESELL_USERID_SUFFIX);

					@SuppressWarnings("unchecked")
					final List<EnergizerB2BCustomerModel> b2bCustomerList = ((List<EnergizerB2BCustomerModel>) CollectionUtils
							.select(b2bUnitModel.getMembers(), PredicateUtils.instanceofPredicate(EnergizerB2BCustomerModel.class)));
					if (null != b2bCustomerList && CollectionUtils.isNotEmpty(b2bCustomerList))
					{
						for (final EnergizerB2BCustomerModel b2bCustomer : b2bCustomerList)
						{
							if (b2bCustomer.getUid().equalsIgnoreCase(wesellCustomerUid.trim().toLowerCase()))
							{
								salesRepCustomerCreated = true;
								break;
							}
						}
						if (!salesRepCustomerCreated)
						{
							customer = energizerWesellVirtualID.createVirtualID(b2bUnitModel, b2bEmployeeModel);
						}
					}
					else
					{
						customer = energizerWesellVirtualID.createVirtualID(b2bUnitModel, b2bEmployeeModel);
					}
				}
				else
				{
					LOG.info("B2BUnitModel with ID " + b2bunit + " not found... ");
				}
			}

			// Sales Org
			if (null != salesOrgSet)
			{
				if (salesOrgSet.size() > 1)
				{
					LOG.info("Multiple sales org from different b2b units for this sales rep ...");
				}
				else if (salesOrgSet.size() == 1)
				{
					b2bEmployeeModel.setSalesOrganisation(salesOrgSet.iterator().next());
				}
			}

			// Currency
			if (null != currencySet)
			{
				if (currencySet.size() > 1)
				{
					LOG.info("Multiple currency preferences/isocodes from different b2b units for this sales rep ...");
				}
				else if (currencySet.size() == 1)
				{
					b2bEmployeeModel.setCurrency(currencySet.iterator().next());
				}
			}
			//	Added validation for groups fields
			/*-final Set<PrincipalGroupModel> customerGroups = new HashSet<PrincipalGroupModel>(b2bEmployeeModel.getGroups());
			for (final String group : csvValuesMap.get(GROUPS).split(";"))
			{
				final UserGroupModel userGroupModel = userService.getUserGroupForUID(group);
				if (null != userGroupModel && !customerGroups.contains(userGroupModel))
				{
					customerGroups.add(userGroupModel);
				}
				if (null == userGroupModel)
				{
					LOG.info("UserGroupModel with ID " + group + " not found... ");
				}
			}*/
			final Set<PrincipalGroupModel> userGroups = new HashSet<PrincipalGroupModel>();
			final String wesellSalesRepDefaultUserGroups = getConfigValue(
					EnergizerCoreConstants.WESELL_SALESREP_DEFAULT_USER_GROUPS);

			if (null != wesellSalesRepDefaultUserGroups)
			{
				for (final String userGroup : wesellSalesRepDefaultUserGroups.split(";"))
				{
					try
					{
						final UserGroupModel userGroupModel = userService.getUserGroupForUID(userGroup);
						if (!userGroups.contains(userGroupModel))
						{
							userGroups.add(userGroupModel);
						}
					}
					catch (final Exception e)
					{
						LOG.info("Exception occured ::: " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
			b2bEmployeeModel.setGroups(userGroups);
			b2bEmployeeModel.setB2bUnitList(b2bUnitList);
			modelService.saveAll(b2bEmployeeModel);
			LOG.info("EnergizerB2BEmployee updated " + b2bEmployeeModel.getUid() + " & Email ID " + b2bEmployeeModel.getEmail());
		}
	}

	private void createB2BEmployee(final Map<String, String> csvValuesMap, final CSVRecord record,
			EnergizerB2BEmployeeModel b2bEmployeeModel)
	{
		EnergizerB2BCustomerModel customer = null;
		b2bEmployeeModel = modelService.create(EnergizerB2BEmployeeModel.class);
		final String salesRepCode = csvValuesMap.get(SALESREP_CODE).trim().toLowerCase();
		b2bEmployeeModel.setUid(csvValuesMap.get(SALESREP_USERNAME).trim().toLowerCase());
		b2bEmployeeModel.setSalesRepCode(csvValuesMap.get(SALESREP_CODE).trim().toLowerCase());
		final String fullName = csvValuesMap.get(SALESREP_NAME).trim();
		b2bEmployeeModel.setName(fullName);
		if (null != fullName)
		{
			b2bEmployeeModel.setFirstName(fullName.contains(" ") ? fullName.split(" ")[0] : fullName);
			b2bEmployeeModel.setLastName(fullName.contains(" ") ? fullName.substring(fullName.indexOf(" ")).trim() : null);
		}
		b2bEmployeeModel.setContactNumber(csvValuesMap.get(CONTACT_NUMBER).trim());
		final boolean activeStatus = csvValuesMap.get(ACTIVE).trim().equalsIgnoreCase("Y") ? true : false;
		b2bEmployeeModel.setActive(activeStatus);
		final boolean isSalesRep = csvValuesMap.get(ISSALESREP).trim().equalsIgnoreCase("Y") ? true : false;
		b2bEmployeeModel.setIsSalesRep(isSalesRep);
		b2bEmployeeModel.setEmail(csvValuesMap.get(EMAIL_ID).trim().toLowerCase());
		//b2bEmployeeModel.setPriority(csvValuesMap.get(PRIORITY).trim());
		final Set<EnergizerB2BUnitModel> b2bUnitList = new HashSet<EnergizerB2BUnitModel>();

		final Set<String> salesOrgSet = new HashSet<String>();
		final Set<String> currencySet = new HashSet<String>();

		for (final String b2bunit : csvValuesMap.get(B2B_UNIT_LIST).split(";"))
		{
			final EnergizerB2BUnitModel b2bUnitModel = (EnergizerB2BUnitModel) b2bUnitService.getUnitForUid(b2bunit);
			if (null != b2bUnitModel)
			{
				salesOrgSet.add(b2bUnitModel.getSalesOrganisation());
				currencySet.add(b2bUnitModel.getCurrencyPreference().getIsocode());
				b2bUnitList.add(b2bUnitModel);

				boolean salesRepCustomerCreated = false;
				/*-final String wesellCustomerUid = salesRepCode + "_" + b2bUnitModel.getUid()
						+ getConfigValue(EnergizerCoreConstants.WESELL_USERID_SUFFIX);*/

				final String virtualIDPrefix = b2bEmployeeModel.getUid().split("@")[0].toLowerCase();
				//Example - alejandro.medina_0098112157@edgewell.com created as a virtual ID for sales rep individual cart maintenance
				final String wesellCustomerUid = virtualIDPrefix + "_" + b2bUnitModel.getUid()
						+ getConfigValue(EnergizerCoreConstants.WESELL_USERID_SUFFIX);

				@SuppressWarnings("unchecked")
				final List<EnergizerB2BCustomerModel> b2bCustomerList = ((List<EnergizerB2BCustomerModel>) CollectionUtils
						.select(b2bUnitModel.getMembers(), PredicateUtils.instanceofPredicate(EnergizerB2BCustomerModel.class)));
				if (null != b2bCustomerList && CollectionUtils.isNotEmpty(b2bCustomerList))
				{
					for (final EnergizerB2BCustomerModel b2bCustomer : b2bCustomerList)
					{
						if (b2bCustomer.getUid().equalsIgnoreCase(wesellCustomerUid.trim().toLowerCase()))
						{
							salesRepCustomerCreated = true;
							break;
						}
					}
					if (!salesRepCustomerCreated)
					{
						customer = energizerWesellVirtualID.createVirtualID(b2bUnitModel, b2bEmployeeModel);
					}
				}
				else
				{
					customer = energizerWesellVirtualID.createVirtualID(b2bUnitModel, b2bEmployeeModel);
				}
			}
			else
			{
				LOG.info("B2BUnitModel with ID " + b2bunit + " not found... ");
			}

		}

		// Sales Org
		if (null != salesOrgSet)
		{
			if (salesOrgSet.size() > 1)
			{
				LOG.info("Multiple sales org from different b2b units for this sales rep ...");
			}
			else if (salesOrgSet.size() == 1)
			{
				b2bEmployeeModel.setSalesOrganisation(salesOrgSet.iterator().next());
			}
		}

		// Currency
		if (null != currencySet)
		{
			if (currencySet.size() > 1)
			{
				LOG.info("Multiple currency preferences/isocodes from different b2b units for this sales rep ...");
			}
			else if (currencySet.size() == 1)
			{
				b2bEmployeeModel.setCurrency(currencySet.iterator().next());
			}
		}


		/*-final Set<PrincipalGroupModel> customerGroups = new HashSet<PrincipalGroupModel>();
		
		for (final String group : csvValuesMap.get(GROUPS).split(";"))
		{
			try
			{
				final UserGroupModel userGroupModel = userService.getUserGroupForUID(group);
				if (null != userGroupModel && !customerGroups.contains(userGroupModel))
				{
					customerGroups.add(userGroupModel);
				}
				if (null == userGroupModel)
				{
					LOG.info("UserGroupModel with ID " + group + " not found... ");
				}
			}
			catch (final UnknownIdentifierException b2bGroupUnknownIdentifierException)
			{
				throw new B2BGroupUnknownIdentifierException("Invalid User group specified");
			}
		}*/
		final Set<PrincipalGroupModel> userGroups = new HashSet<PrincipalGroupModel>();
		final String wesellSalesRepDefaultUserGroups = getConfigValue(EnergizerCoreConstants.WESELL_SALESREP_DEFAULT_USER_GROUPS);

		if (null != wesellSalesRepDefaultUserGroups)
		{
			for (final String userGroup : wesellSalesRepDefaultUserGroups.split(";"))
			{
				try
				{
					final UserGroupModel userGroupModel = userService.getUserGroupForUID(userGroup);
					if (!userGroups.contains(userGroupModel))
					{
						userGroups.add(userGroupModel);
					}
				}
				catch (final Exception e)
				{
					LOG.info("Exception occured ::: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		b2bEmployeeModel.setGroups(userGroups);
		b2bEmployeeModel.setB2bUnitList(b2bUnitList);

		// A new Sales rep is unregistered in the system until the registration email is sent out.
		b2bEmployeeModel.setRegistrationEmailFlag(Boolean.FALSE);
		modelService.saveAll(b2bEmployeeModel);
		LOG.info("Created SalesRepUser with SalesRepUsername " + b2bEmployeeModel.getUid() + " & Email ID "
				+ b2bEmployeeModel.getEmail());

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
			if (value.isEmpty())
			{
				long recordFailed = getBusRecordError();
				error = new EnergizerCSVFeedError();
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error.setLineNumber(record.getRecordNumber());
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

			if (columnHeader.equalsIgnoreCase(GROUPS))
			{

				if (!value.contains("personalCare"))
				{
					long recordFailed = getBusRecordError();
					error = new EnergizerCSVFeedError();
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error.setLineNumber(record.getRecordNumber());
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
				error.setLineNumber(record.getRecordNumber());
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

	private String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}

}
