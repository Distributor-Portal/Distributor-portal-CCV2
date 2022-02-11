/**
 *
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.b2b.company.B2BCommerceCostCenterService;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.b2b.enums.B2BPeriodRange;
import de.hybris.platform.b2b.enums.B2BPermissionTypeEnum;
import de.hybris.platform.b2b.model.B2BBudgetModel;
import de.hybris.platform.b2b.model.B2BCostCenterModel;
import de.hybris.platform.b2b.services.B2BBudgetService;
import de.hybris.platform.b2bapprovalprocessfacades.company.B2BPermissionFacade;
import de.hybris.platform.b2bapprovalprocessfacades.company.data.B2BPermissionData;
import de.hybris.platform.b2bapprovalprocessfacades.company.data.B2BPermissionTypeData;
import de.hybris.platform.b2bcommercefacades.company.B2BBudgetFacade;
import de.hybris.platform.b2bcommercefacades.company.B2BCostCenterFacade;
import de.hybris.platform.b2bcommercefacades.company.B2BUnitFacade;
import de.hybris.platform.b2bcommercefacades.company.data.B2BBudgetData;
import de.hybris.platform.b2bcommercefacades.company.data.B2BCostCenterData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.storesession.data.CurrencyData;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.FormatFactory;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.form.B2BBudgetForm;
import com.energizer.core.datafeed.form.B2BCostCenterForm;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCronJobModel;


/**
 *
 * This processors imports the customer address .
 *
 * Sample file will look like
 *
 * CustomerID,SalesOrg,DistributionChannel,Division,Customername,OrderType,DeletionFlag,Status,Currency,
 * MinimumOrderValue,DefaultLanguage 1006, 1000, 10, 10, tyfg, ZOR, 1, 1, USD, 10, EN
 *
 * Total column count : 11
 */
public class EnergizerCustomerCSVProcessor extends AbstractEnergizerCSVProcessor
{
	@Resource
	private ModelService modelService;
	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Resource(name = "b2bCommerceUnitService")
	private B2BCommerceUnitService b2bCommerceUnitService;

	private static final Logger LOG = Logger.getLogger(EnergizerCustomerCSVProcessor.class);
	@Resource
	private CommonI18NService commonI18NService;

	@Resource(name = "b2bUnitFacade")
	protected B2BUnitFacade b2BUnitFacade;

	@Resource(name = "budgetFacade")
	protected B2BBudgetFacade budgetFacade;

	@Resource(name = "formatFactory")
	protected FormatFactory formatFactory;

	@Resource(name = "costCenterFacade")
	protected B2BCostCenterFacade costCenterFacade;

	@Resource(name = "b2bPermissionFacade")
	protected B2BPermissionFacade b2bCommercePermissionFacade;

	@Resource(name = "b2bCommerceCostCenterService")
	private B2BCommerceCostCenterService b2bCommerceCostCenterService;

	@Resource(name = "b2BBudgetService")
	private B2BBudgetService b2BBudgetService;

	@Resource(name = "userService")
	private UserService userService;

	@Autowired
	protected ConfigurationService configurationService;

	@Resource(name = "typeService")
	private TypeService typeService;

	protected List<B2BCostCenterData> b2bCostCenterDatas = new ArrayList<B2BCostCenterData>();

	protected List<B2BBudgetData> b2bBudgetDatas = new ArrayList<B2BBudgetData>();

	private static final String MAX_USER_LIMIT = "b2b.MaxUserLimit";
	private static final String MAX_USER_LIMIT_COUNT = "10";
	private static final String CUSTOMER_ID = "CustomerID";
	private final String SALES_ORG = "SalesOrg";
	private final String DISTRIBUTION_CHANNEL = "DistributionChannel";
	private final String DIVISION = "Division";
	private final String CUSTOMER_NAME = "Customername";
	private final String ORDER_TYPE = "OrderType";
	private final String STATUS = "Status";
	private final String DELETION_FLAG = "DeletionFlag";
	private final String CURRENCY = "Currency";
	private final String DEFAULT_LANGUAGE = "DefaultLanguage";
	private final String MINIMUM_ORDER_VALUE = "MinimumOrderValue";
	private final String DATE_FORMAT = "MM/dd/yyyy";

	/**
	 * @return the b2bCostCenterDatas
	 */
	public List<B2BCostCenterData> getB2bCostCenterDatas()
	{
		return b2bCostCenterDatas;
	}

	/**
	 * @param b2bCostCenterDatas
	 *           the b2bCostCenterDatas to set
	 */
	public void setB2bCostCenterDatas(final List<B2BCostCenterData> b2bCostCenterDatas)
	{
		this.b2bCostCenterDatas = b2bCostCenterDatas;
	}

	/**
	 * @return the b2bBudgetDatas
	 */
	public List<B2BBudgetData> getB2bBudgetDatas()
	{
		return b2bBudgetDatas;
	}

	/**
	 * @param b2bBudgetDatas
	 *           the b2bBudgetDatas to set
	 */
	public void setB2bBudgetDatas(final List<B2BBudgetData> b2bBudgetDatas)
	{
		this.b2bBudgetDatas = b2bBudgetDatas;
	}

	/**
	 *
	 */
	public EnergizerCustomerCSVProcessor()
	{
		super();
	}

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		EnergizerB2BUnitModel b2bUnit = null;
		try
		{
			final String maxUserLimit = Config.getString(MAX_USER_LIMIT, MAX_USER_LIMIT_COUNT);
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord record : records)
			{
				final Map<String, String> csvValuesMap = record.toMap();
				validate(record);
				if (!getTechnicalFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
					getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
					getTechnicalFeedErrors().clear();
					continue;
				}
				try
				{
					b2bUnit = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(csvValuesMap.get(CUSTOMER_ID).trim());
				}
				catch (final UnknownIdentifierException ep)
				{
					LOG.error("---No existing b2bunit found - New Record Inserting----", ep);
				}
				if (b2bUnit == null)
				{
					if (!csvValuesMap.get(CUSTOMER_ID).trim().isEmpty())
					{
						final EnergizerB2BUnitModel energizeB2BUnit = modelService.create(EnergizerB2BUnitModel.class);
						final String getParentB2BUnit = csvValuesMap.get(CUSTOMER_ID).trim();
						energizeB2BUnit.setUid(getParentB2BUnit);
						energizeB2BUnit.setSalesOrganisation(csvValuesMap.get(SALES_ORG).trim());
						energizeB2BUnit.setDistributionChannel(csvValuesMap.get(DISTRIBUTION_CHANNEL).trim());
						energizeB2BUnit.setDivision(csvValuesMap.get(DIVISION).trim());
						energizeB2BUnit.setName(csvValuesMap.get(CUSTOMER_NAME).trim());
						energizeB2BUnit.setCustomerAccountName(csvValuesMap.get(CUSTOMER_NAME).trim());
						energizeB2BUnit.setErpOrderingType(csvValuesMap.get(ORDER_TYPE).trim());
						final boolean blockStatus = csvValuesMap.get(STATUS).trim().equalsIgnoreCase("1") ? true : false;
						energizeB2BUnit.setOrderBlock(blockStatus);
						final boolean active = csvValuesMap.get(DELETION_FLAG).trim().equalsIgnoreCase("1") ? false : true;
						energizeB2BUnit.setActive(active);
						try
						{
							final CurrencyModel currencyModel = commonI18NService.getCurrency(csvValuesMap.get(CURRENCY).trim());
							energizeB2BUnit.setCurrencyPreference(currencyModel);
							final LanguageModel languageModel = commonI18NService.getLanguage(csvValuesMap.get(DEFAULT_LANGUAGE).trim());
							energizeB2BUnit.setLanguagePreference(languageModel.getIsocode());
						}
						catch (final Exception exception)
						{
							LOG.error("Currency Model or Language Model Not Found!", exception);
						}
						energizeB2BUnit.setLocName(csvValuesMap.get(CUSTOMER_NAME).trim());
						energizeB2BUnit.setMaxUserLimit(maxUserLimit);
						energizeB2BUnit.setBuyerSpecificID(csvValuesMap.get(CUSTOMER_ID).trim());
						energizeB2BUnit.setMinimumOrderValue(new BigDecimal(csvValuesMap.get(MINIMUM_ORDER_VALUE).trim()));
						energizeB2BUnit.setEnableContainerOptimization(false);


						energizeB2BUnit.setSalesArea(this.getCronjob().getRegion());  
						
						try
						{
							modelService.save(energizeB2BUnit);
							modelService.refresh(energizeB2BUnit);
							//Added for WeSell Implementation - update/create b2b customer model
							/*-final String wesellUid = getConfigValue(EnergizerCoreConstants.WESELL_USERID_PREFIX)
									+ energizeB2BUnit.getUid() + getConfigValue(EnergizerCoreConstants.WESELL_USERID_SUFFIX);

							final EnergizerB2BCustomerModel energizerB2BCustomerModel = modelService
									.create(EnergizerB2BCustomerModel.class);
							boolean wesellCustomerModelCreated = false;
							if (super.getCronjob().getPath().contains(EnergizerCoreConstants.WESELL))
							{
								energizerB2BCustomerModel.setUid(wesellUid.trim().toLowerCase());
								energizerB2BCustomerModel.setEmail(wesellUid.trim().toLowerCase());
								energizerB2BCustomerModel.setOriginalUid(wesellUid.trim().toLowerCase());
								//energizerB2BCustomerModel.setOriginalUid(energizerB2BCustomerModel.getUid());
								energizerB2BCustomerModel
										.setName(getConfigValue(EnergizerCoreConstants.WESELL_USERID_PREFIX) + energizeB2BUnit.getUid());
								energizerB2BCustomerModel.setContactNumber(getConfigValue(EnergizerCoreConstants.WESELL_CONTACT_NUMBER));

								final Set<PrincipalGroupModel> customerGroups = new HashSet<PrincipalGroupModel>();

								final String wesellDefaultUserGroups = getConfigValue(
										EnergizerCoreConstants.WESELL_B2BCUSTOMER_DEFAULT_USER_GROUPS);

								if (null != wesellDefaultUserGroups)
								{
									for (final String userGroup : wesellDefaultUserGroups.split(";"))
									{
										try
										{
											final UserGroupModel userGroupModel = userService.getUserGroupForUID(userGroup);
											if (!customerGroups.contains(userGroupModel))
											{
												customerGroups.add(userGroupModel);
											}
										}
										catch (final Exception e)
										{
											LOG.info("Exception occured ::: " + e.getMessage());
											e.printStackTrace();
										}
									}
								}
								energizerB2BCustomerModel.setGroups(customerGroups);
								energizerB2BCustomerModel.setDefaultB2BUnit(energizeB2BUnit);

								modelService.save(energizerB2BCustomerModel);
								modelService.refresh(energizerB2BCustomerModel);

								wesellCustomerModelCreated = true;
							}*/
							//Added for WeSell Implementation - update/create b2b customer model - END

							//---Create budget for the unit
							final B2BBudgetForm b2BBudgetForm = getB2BBudgetFormFromLocalProperties(getParentB2BUnit);
							addNewBudget(b2BBudgetForm);
							//---Create cost center for the unit
							final B2BCostCenterForm b2BCostCenterForm = getB2BCostCenterFromLocalProperties(getParentB2BUnit);
							addCostCenter(b2BCostCenterForm, b2BBudgetForm);

							//--- Save default permissions for the unit
							saveDefaultPermission(getParentB2BUnit);

							//Add the b2b unit default permissions to the b2b customer model for order auto approval - ONLY for WESELL - START
							/*-if (wesellCustomerModelCreated)
							{
								energizerB2BCustomerModel.setPermissions(energizeB2BUnit.getPermissions());
								modelService.save(energizerB2BCustomerModel);
								modelService.refresh(energizerB2BCustomerModel);
							}*/
							//Add for WeSell Implementation - ONLY for WESELL - END

							succeedRecord++;
							setRecordSucceeded(succeedRecord);
							b2bCostCenterDatas.clear();
							b2bBudgetDatas.clear();

						}
						catch (final Exception exception)
						{
							LOG.error("Exception while saving customer!", exception);
						}
						LOG.info("Energizer B2BUnit created");
					}

				}
				else
				{
					//update model and save
					if (!csvValuesMap.get(CUSTOMER_ID).trim().isEmpty())
					{
						//						b2bUnit.setUid(csvValuesMap.get(CUSTOMER_ID).trim());
						b2bUnit.setSalesOrganisation(csvValuesMap.get(SALES_ORG).trim());
						b2bUnit.setDistributionChannel(csvValuesMap.get(DISTRIBUTION_CHANNEL).trim());
						b2bUnit.setDivision(csvValuesMap.get(DIVISION).trim());
						b2bUnit.setName(csvValuesMap.get(CUSTOMER_NAME).trim());
						b2bUnit.setErpOrderingType(csvValuesMap.get(ORDER_TYPE).trim());
						final boolean active = csvValuesMap.get(DELETION_FLAG).trim().equalsIgnoreCase("1") ? false : true;
						b2bUnit.setActive(active);
						final boolean blockStatus = csvValuesMap.get(STATUS).trim().equalsIgnoreCase("1") ? true : false;
						b2bUnit.setOrderBlock(blockStatus);
						b2bUnit.setMinimumOrderValue(new BigDecimal(csvValuesMap.get(MINIMUM_ORDER_VALUE).trim()));
						try
						{
							final CurrencyModel currencyModel = commonI18NService.getCurrency(csvValuesMap.get(CURRENCY).trim());
							b2bUnit.setCurrencyPreference(currencyModel);
							final LanguageModel languageModel = commonI18NService.getLanguage(csvValuesMap.get(DEFAULT_LANGUAGE).trim());
							b2bUnit.setLanguagePreference(languageModel.getIsocode());
						}
						catch (final Exception exception)
						{
							LOG.info("Currency Model or Language Model Not Found!");
						}
						b2bUnit.setMaxUserLimit(maxUserLimit);
						b2bUnit.setLocname(csvValuesMap.get(CUSTOMER_NAME).trim());
						/*-if (this.getCronjob().getPath().contains(EnergizerCoreConstants.LATAM))
						{
							b2bUnit.setSalesArea(EnergizerCoreConstants.LATAM);
						}
						else if (this.getCronjob().getPath().contains(EnergizerCoreConstants.EMEA))
						{
							b2bUnit.setSalesArea(EnergizerCoreConstants.EMEA);
						}
						else if (this.getCronjob().getPath().contains(EnergizerCoreConstants.WESELL))
						{
							b2bUnit.setSalesArea(EnergizerCoreConstants.WESELL);
						}*/
						b2bUnit.setSalesArea(this.getCronjob().getRegion());
						try
						{
							modelService.save(b2bUnit);
							modelService.refresh(b2bUnit);
							succeedRecord++;
							setRecordSucceeded(succeedRecord);

							//Added for WeSell Implementation - update/create b2b customer model
							/*-if (super.getCronjob().getPath().contains(EnergizerCoreConstants.WESELL))
							{
								final String wesellUid = getConfigValue(EnergizerCoreConstants.WESELL_USERID_PREFIX) + b2bUnit.getUid()
										+ getConfigValue(EnergizerCoreConstants.WESELL_USERID_SUFFIX);
								boolean isB2BCustomerAvailable = false;
								EnergizerB2BCustomerModel b2bCustomer = null;

								if (null != b2bUnit.getMembers())
								{
									final Set<PrincipalModel> members = b2bUnit.getMembers();
									for (final PrincipalModel principalModel : members)
									{
										if (principalModel instanceof EnergizerB2BCustomerModel)
										{
											isB2BCustomerAvailable = true;
											b2bCustomer = (EnergizerB2BCustomerModel) principalModel;
											break;
										}
									}
									if (!isB2BCustomerAvailable)
									{
										LOG.info("B2B Customer does not exist for this b2b unit, so we need to create one now ...");

										final EnergizerB2BCustomerModel energizerB2BCustomerModel = modelService
												.create(EnergizerB2BCustomerModel.class);
										energizerB2BCustomerModel.setUid(wesellUid.trim().toLowerCase());
										energizerB2BCustomerModel.setEmail(wesellUid.trim().toLowerCase());
										energizerB2BCustomerModel.setOriginalUid(wesellUid.trim().toLowerCase());
										energizerB2BCustomerModel
												.setName(getConfigValue(EnergizerCoreConstants.WESELL_USERID_PREFIX) + b2bUnit.getUid());
										energizerB2BCustomerModel
												.setContactNumber(getConfigValue(EnergizerCoreConstants.WESELL_CONTACT_NUMBER));

										final Set<PrincipalGroupModel> customerGroups = new HashSet<PrincipalGroupModel>();

										final String wesellDefaultUserGroups = getConfigValue(
												EnergizerCoreConstants.WESELL_B2BCUSTOMER_DEFAULT_USER_GROUPS);

										if (null != wesellDefaultUserGroups)
										{
											for (final String userGroup : wesellDefaultUserGroups.split(";"))
											{
												try
												{
													final UserGroupModel userGroupModel = userService.getUserGroupForUID(userGroup);
													if (!customerGroups.contains(userGroupModel))
													{
														customerGroups.add(userGroupModel);
													}
												}
												catch (final Exception e)
												{
													LOG.info("Exception occured ::: " + e.getMessage());
													e.printStackTrace();
												}
											}
										}
										energizerB2BCustomerModel.setGroups(customerGroups);
										energizerB2BCustomerModel.setDefaultB2BUnit(b2bUnit);

										modelService.save(energizerB2BCustomerModel);
										modelService.refresh(energizerB2BCustomerModel);
									}


									else
									{
										LOG.info("B2B Customer already exists for this b2b unit, so no need to create one again ...");
									}
								}
							}*/
						}
						//Added for WeSell Implementation - update/create b2b customer model
						catch (final Exception exception)
						{
							LOG.error("Exception while saving customer!", exception);
						}
						LOG.info("Updating the EnergizerB2BUnit");
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error("EnergizerCustomerCSVProcessor:Exception in saving EnergizerCustomer", e);
		}
		getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
		getBusinessFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	protected B2BBudgetData populateB2BBudgetDataFromForm(final B2BBudgetForm b2BBudgetForm) throws ParseException
	{
		getB2bCostCenterDatas().addAll(getB2bCostCenterDatas());
		final B2BBudgetData b2BBudgetData = new B2BBudgetData();
		b2BBudgetData.setOriginalCode(b2BBudgetForm.getOriginalCode());
		b2BBudgetData.setActive(Boolean.TRUE);
		b2BBudgetData.setCode(b2BBudgetForm.getCode());
		b2BBudgetData.setName(b2BBudgetForm.getName());
		b2BBudgetData.setUnit(b2BUnitFacade.getUnitForUid(b2BBudgetForm.getParentB2BUnit()));
		final CurrencyData currencyData = new CurrencyData();
		currencyData.setIsocode(b2BBudgetForm.getCurrency());
		b2BBudgetData.setCurrency(currencyData);
		b2BBudgetData.setStartDate(b2BBudgetForm.getStartDate());
		b2BBudgetData.setEndDate(b2BBudgetForm.getEndDate());
		b2BBudgetData
				.setBudget(BigDecimal.valueOf(formatFactory.createNumberFormat().parse(b2BBudgetForm.getBudget()).doubleValue()));
		b2BBudgetData.setCostCenters(getB2bCostCenterDatas());
		b2bBudgetDatas.add(b2BBudgetData);
		return b2BBudgetData;
	}

	protected B2BBudgetForm getB2BBudgetFormFromLocalProperties(final String parentB2BUnit)
	{
		final B2BBudgetForm b2BBudgetForm = new B2BBudgetForm();
		final Date startDate;
		final Date endDate;
		final CurrencyData currencyData = new CurrencyData();
		final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		try
		{
			b2BBudgetForm.setOriginalCode(configurationService.getConfiguration().getProperty("b2BBudget.OriginalCode").toString());
			String CODE = configurationService.getConfiguration().getProperty("b2BBudget.Code").toString();
			String ISOCODE = configurationService.getConfiguration().getProperty("b2BBudget.Isocode").toString();
			if (this.getCronjob().getCatalogName().contains("EMEA"))
			{
				CODE = configurationService.getConfiguration().getProperty("b2BBudget.CodeEMEA").toString();
				/* ISOCODE = configurationService.getConfiguration().getProperty("b2BBudget.IsocodeEMEA").toString(); */
				final EnergizerB2BUnitModel unitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(parentB2BUnit);
				final CurrencyModel currencyModel = unitModel.getCurrencyPreference();
				ISOCODE = currencyModel.getIsocode();
			}


			if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL))
			{
				final EnergizerB2BUnitModel unitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(parentB2BUnit);
				final CurrencyModel currencyModel = unitModel.getCurrencyPreference();
				ISOCODE = currencyModel.getIsocode();
			}


			b2BBudgetForm.setCode(parentB2BUnit.concat("-").concat(CODE));
			b2BBudgetForm.setName(parentB2BUnit.concat("-")
					.concat(configurationService.getConfiguration().getProperty("b2BBudget.Name").toString()));
			b2BBudgetForm.setParentB2BUnit(parentB2BUnit);

			currencyData.setIsocode(ISOCODE);
			b2BBudgetForm.setCurrency(currencyData.getIsocode());

			startDate = formatter.parse(configurationService.getConfiguration().getProperty("b2BBudget.StartDate").toString());
			endDate = formatter.parse(configurationService.getConfiguration().getProperty("b2BBudget.EndDate").toString());
			b2BBudgetForm.setStartDate(startDate);
			b2BBudgetForm.setEndDate(endDate);
			b2BBudgetForm.setBudget(configurationService.getConfiguration().getProperty("b2BBudget.Budget").toString());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return b2BBudgetForm;
	}

	protected B2BCostCenterForm getB2BCostCenterFromLocalProperties(final String parentB2BUnit)
	{
		B2BCostCenterForm b2bCostCenterForm = null;
		try
		{
			b2bCostCenterForm = new B2BCostCenterForm();
			b2bCostCenterForm
					.setOriginalCode(configurationService.getConfiguration().getProperty("b2bCostCenter.OriginalCode").toString());
			String B2BCOSTCENTER_CODE = configurationService.getConfiguration().getProperty("b2bCostCenter.Code").toString();
			String B2BCOSTCENTER_ISOCODE = configurationService.getConfiguration().getProperty("b2bCostCenter.Isocode").toString();
			if (this.getCronjob().getCatalogName().contains("EMEA"))
			{
				B2BCOSTCENTER_CODE = configurationService.getConfiguration().getProperty("b2bCostCenter.CodeEMEA").toString();
				/*
				 * B2BCOSTCENTER_ISOCODE =
				 * configurationService.getConfiguration().getProperty("b2bCostCenter.IsocodeEMEA").toString();
				 */
				final EnergizerB2BUnitModel unitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(parentB2BUnit);
				final CurrencyModel currencyModel = unitModel.getCurrencyPreference();
				B2BCOSTCENTER_ISOCODE = currencyModel.getIsocode();

			}


			if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL))
			{
				final EnergizerB2BUnitModel unitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(parentB2BUnit);
				final CurrencyModel currencyModel = unitModel.getCurrencyPreference();
				B2BCOSTCENTER_ISOCODE = currencyModel.getIsocode();
			}


			b2bCostCenterForm.setCode(parentB2BUnit.concat("-").concat(B2BCOSTCENTER_CODE));
			b2bCostCenterForm.setName(parentB2BUnit.concat("-")
					.concat(configurationService.getConfiguration().getProperty("b2bCostCenter.Name").toString()));
			final CurrencyData currencyData = new CurrencyData();
			currencyData.setIsocode(B2BCOSTCENTER_ISOCODE);
			b2bCostCenterForm.setCurrency(currencyData.getIsocode());
			b2bCostCenterForm.setParentB2BUnit(parentB2BUnit);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return b2bCostCenterForm;
	}

	protected void addNewBudget(final B2BBudgetForm b2BBudgetForm) throws CMSItemNotFoundException, ParseException
	{
		final B2BBudgetData b2BBudgetData = populateB2BBudgetDataFromForm(b2BBudgetForm);
		try
		{
			budgetFacade.addBudget(b2BBudgetData);
			LOG.info("Saving the budget details");
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
	}

	protected B2BCostCenterData populateB2BCostCenterDataFromForm(final B2BCostCenterForm b2BCostCenterForm)
	{
		final B2BCostCenterData b2BCostCenterData = new B2BCostCenterData();
		b2BCostCenterData.setActive(Boolean.TRUE);
		b2BCostCenterData.setOriginalCode(b2BCostCenterForm.getOriginalCode());
		b2BCostCenterData.setCode(b2BCostCenterForm.getCode());
		b2BCostCenterData.setName(b2BCostCenterForm.getName());
		final CurrencyData currencyData = new CurrencyData();
		currencyData.setIsocode(b2BCostCenterForm.getCurrency());
		b2BCostCenterData.setCurrency(currencyData);
		b2BCostCenterData.setUnit(b2BUnitFacade.getUnitForUid(b2BCostCenterForm.getParentB2BUnit()));
		b2bCostCenterDatas.add(b2BCostCenterData);
		return b2BCostCenterData;
	}

	protected void addCostCenter(final B2BCostCenterForm b2BCostCenterForm, final B2BBudgetForm b2bBudgetForm)
			throws CMSItemNotFoundException
	{
		final B2BCostCenterData b2BCostCenterData = populateB2BCostCenterDataFromForm(b2BCostCenterForm);
		try
		{
			costCenterFacade.addCostCenter(b2BCostCenterData);
			final B2BCostCenterModel b2BCostCenterModel = getB2BCommerceCostCenterService()
					.getCostCenterForCode(b2BCostCenterData.getCode());
			final B2BBudgetModel b2BBudgetModel = getB2BBudgetService().getB2BBudgetForCode(b2bBudgetForm.getCode());
			final Set<B2BBudgetModel> budgetSet = new HashSet<B2BBudgetModel>();
			budgetSet.add(b2BBudgetModel);
			b2BCostCenterModel.setBudgets(budgetSet);
			modelService.save(b2BCostCenterModel);
		}
		catch (final Exception e)
		{
			LOG.warn("Exception while saving the cost center details " + e);
		}
	}

	protected void saveDefaultPermission(final String b2bUnit) throws CMSItemNotFoundException
	{
		try
		{
			b2bCommercePermissionFacade.addPermission(populateB2BPermission(b2bUnit));
			b2bCommercePermissionFacade.addPermission(populateOrderThresholdTimeSpanPermission(b2bUnit));
		}
		catch (final Exception e)
		{
			LOG.warn("Exception while saving the default permissions " + e);
		}
	}

	/**
	 * @param record
	 * @param csvValuesMap
	 * @return
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		final Map<String, String> map = record.toMap();
		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader);
			if (value.isEmpty())
			{
				long recordFailed = getRecordFailed();
				error = new EnergizerCSVFeedError();
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error.setLineNumber(record.getRecordNumber() + 1);
				columnNames.add(columnHeader);
				error.setColumnName(columnNames);
				error.setMessage(columnHeader + " column should not be empty");
				columnNumbers.add(columnNumber);
				error.setColumnNumber(columnNumbers);
				getTechnicalFeedErrors().add(error);
				setTechRecordError(getTechnicalFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);
			}
		}
	}

	protected B2BPermissionData populateB2BPermission(final String b2bUnit) throws ParseException
	{
		final B2BPermissionData b2BPermissionData = new B2BPermissionData();
		b2BPermissionData.setCode(b2bUnit.concat("_").concat("defaultBudgetPerm"));
		b2BPermissionData.setOriginalCode(b2bUnit.concat("_").concat("defaultBudgetPerm"));
		final B2BPermissionTypeData b2BPermissionTypeData = new B2BPermissionTypeData();
		b2BPermissionTypeData.setCode(B2BPermissionTypeEnum.B2BBUDGETEXCEEDEDPERMISSION.getCode());
		b2BPermissionTypeData.setName(typeService.getEnumerationValue(B2BPermissionTypeEnum.B2BBUDGETEXCEEDEDPERMISSION).getName());
		b2BPermissionData.setB2BPermissionTypeData(b2BPermissionTypeData);
		final CurrencyData currencyData = new CurrencyData();
		if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL))
		{
			final EnergizerB2BUnitModel unitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(b2bUnit);
			final CurrencyModel currencyModel = unitModel.getCurrencyPreference();
			currencyData.setIsocode(currencyModel.getIsocode());
		}
		else if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.LATAM))
		{
			currencyData.setIsocode(configurationService.getConfiguration().getProperty("b2BBudget.Isocode").toString());
		}
		else if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.EMEA))
		{
			/*
			 * currencyData.setIsocode(configurationService.getConfiguration().getProperty("b2BBudget.IsocodeEMEA").
			 * toString());
			 */
			final EnergizerB2BUnitModel unitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(b2bUnit);
			final CurrencyModel currencyModel = unitModel.getCurrencyPreference();
			currencyData.setIsocode(currencyModel.getIsocode());
		}
		b2BPermissionData.setCurrency(currencyData);
		b2BPermissionData.setUnit(b2BUnitFacade.getUnitForUid(b2bUnit));
		return b2BPermissionData;
	}


	protected B2BPermissionData populateOrderThresholdTimeSpanPermission(final String b2bUnit) throws ParseException
	{
		final B2BPermissionData b2BPermissionData = new B2BPermissionData();
		b2BPermissionData.setCode(b2bUnit.concat("_").concat("defaultOrdThresholdPerm"));
		b2BPermissionData.setOriginalCode(b2bUnit.concat("_").concat("defaultOrdThresholdPerm"));
		final B2BPermissionTypeData b2BPermissionTypeData = new B2BPermissionTypeData();
		b2BPermissionTypeData.setCode(B2BPermissionTypeEnum.B2BORDERTHRESHOLDTIMESPANPERMISSION.getCode());
		b2BPermissionTypeData
				.setName(typeService.getEnumerationValue(B2BPermissionTypeEnum.B2BORDERTHRESHOLDTIMESPANPERMISSION).getName());
		b2BPermissionData.setB2BPermissionTypeData(b2BPermissionTypeData);
		final CurrencyData currencyData = new CurrencyData();
		if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL))
		{
			final EnergizerB2BUnitModel unitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(b2bUnit);
			final CurrencyModel currencyModel = unitModel.getCurrencyPreference();
			currencyData.setIsocode(currencyModel.getIsocode());
		}
		else if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.LATAM))
		{
			currencyData.setIsocode(configurationService.getConfiguration().getProperty("b2BBudget.Isocode").toString());
		}
		else if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.EMEA))
		{
			/*
			 * currencyData.setIsocode(configurationService.getConfiguration().getProperty("b2BBudget.IsocodeEMEA").
			 * toString());
			 */

			final EnergizerB2BUnitModel unitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(b2bUnit);
			final CurrencyModel currencyModel = unitModel.getCurrencyPreference();
			currencyData.setIsocode(currencyModel.getIsocode());
		}

		b2BPermissionData.setCurrency(currencyData);
		b2BPermissionData.setUnit(b2BUnitFacade.getUnitForUid(b2bUnit));
		b2BPermissionData.setPeriodRange(B2BPeriodRange.valueOf("WEEK"));
		b2BPermissionData.setValue(Double.valueOf(formatFactory.createNumberFormat().parse("100000000").doubleValue()));
		return b2BPermissionData;

	}

	private String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}

	/**
	 * @return the companyB2BCommerceService
	 */
	public B2BCommerceUnitService geB2BCommerceUnitService()
	{
		return b2bCommerceUnitService;
	}

	/**
	 * @param companyB2BCommerceService
	 *           the companyB2BCommerceService to set
	 */
	public void setB2BCommerceUnitService(final B2BCommerceUnitService b2BCommerceUnitService)
	{
		this.b2bCommerceUnitService = b2BCommerceUnitService;
	}

	/**
	 * @return the b2BCommerceCostCenterService
	 */
	public B2BCommerceCostCenterService getB2BCommerceCostCenterService()
	{
		return b2bCommerceCostCenterService;
	}

	/**
	 * @param b2bCommerceCostCenterService
	 *           the b2BCommerceCostCenterService to set
	 */
	public void setB2BCommerceCostCenterService(final B2BCommerceCostCenterService b2BCommerceCostCenterService)
	{
		b2bCommerceCostCenterService = b2BCommerceCostCenterService;
	}

	/**
	 * @return the b2BCommerceBudgetService
	 */
	public B2BBudgetService getB2BBudgetService()
	{
		return b2BBudgetService;
	}

	/**
	 * @param b2bCommerceBudgetService
	 *           the b2BCommerceBudgetService to set
	 */
	public void setB2BBudgetService(final B2BBudgetService b2BBudgetService)
	{
		this.b2BBudgetService = b2BBudgetService;
	}
}
