/**
 *
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.apache.solr.common.StringUtils;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.facade.impl.DefaultEnergizerAddressFacade;
import com.energizer.core.datafeed.facade.impl.DefaultEnergizerCustomerLeadTimeFacade;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCronJobModel;



/**
 *
 * This processors imports the customer address .
 *
 * Sample file will look like
 *
 * SPCustomerID,Status,SPNAME,SPCITY,SPPOSTALCODE,SPSTREET,SPCOUNTRYKEY,SPREGION,SPSalesPerson,CustomerID
 * 0002707368,1,SAMY,MONTEVIDEO,11300,GENERAL HOMOS 4834,UY,00,sales_pc@abc.com,0098900010
 *
 * Total column count : 9
 */
public class EnergizerCustomerAddressCSVProcessor extends AbstractEnergizerCSVProcessor
{

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.datafeed.EnergizerCSVProcessor#parse(de.hybris.platform.util.CSVReader)
	 */

	@Resource
	private ModelService modelService;

	@Resource
	private CatalogService catalogService;

	@Resource
	private B2BCommerceUnitService b2bCommerceUnitService;

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Resource
	private DefaultEnergizerAddressFacade defaultEnergizerAddressFacade;

	@Resource
	private ConfigurationService configurationService;

	@Resource
	private DefaultEnergizerCustomerLeadTimeFacade defaultEnergizerCustomerLeadTimeFacade;

	private static final Logger LOG = Logger.getLogger(EnergizerCustomerAddressCSVProcessor.class);

	private static final String SP_CUSTOMER_ID = "SPCustomerID";
	private static final String SP_SALES_PERSON = "SPSalesPerson";
	private static final String SP_NAME = "SPNAME";
	private static final String SP_CITY = "SPCITY";
	private static final String SP_POSTAL_CODE = "SPPOSTALCODE";
	private static final String SP_STREET = "SPSTREET";
	private static final String SP_COUNTRY_KEY = "SPCOUNTRYKEY";
	private static final String SP_REGION = "SPREGION";
	private static final String STATUS = "Status";
	private static final String CUSTOMER_ID = "CustomerID";
	//private static String SHIPPING_POINT_NO = "ShippingPointNo";
	private static String SHIPPING_POINT_NO = "Plant";
	//private static final String SOLD_TO_ADDRESS_ID = "SoldToAddressId";
	private static final String SOLD_TO_ADDRESS_ID = "Ship2";

	/**
	 *
	 */
	public EnergizerCustomerAddressCSVProcessor()
	{
		super();
	}

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		final List<EnergizerCSVFeedError> errors = new ArrayList<EnergizerCSVFeedError>();
		List<AddressModel> energizerAddressModelList = null;
		try
		{
			long succeedRecord = getRecordSucceeded();
			int count = 0;
			for (final CSVRecord record : records)
			{
				count++;

				final Map<String, String> csvValuesMap = record.toMap();
				final String b2bUnitId = csvValuesMap.get(CUSTOMER_ID).trim();
				String erpAddressId = "";

				if (null != csvValuesMap.get(SP_CUSTOMER_ID) && !StringUtils.isEmpty(csvValuesMap.get(SP_CUSTOMER_ID)))
				{
					erpAddressId = csvValuesMap.get(SP_CUSTOMER_ID).trim();
					LOG.info("erpAddressId ::: " + erpAddressId);
				}
				else
				{
					LOG.info("erpAddressId is null or empty for this record ::: " + count);
				}

				validate(record);
				if (!getTechnicalFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
					getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
					getTechnicalFeedErrors().clear();
					continue;
				}
				AddressModel energizerAddress = modelService.create(AddressModel.class);

				//Retreive an energizer model based on erpAddressId.
				//If doesn't exist then insert into Database otherwise disallow the insertion.
				try
				{
					//energizerB2BUnitModelList = defaultEnergizerAddressFacade.fetchAddress(erpAddressId);
					energizerAddressModelList = defaultEnergizerAddressFacade.fetchAddressForB2BUnitAndErpAddressID(b2bUnitId,
							erpAddressId);
				}
				catch (final Exception e)
				{
					LOG.error("Unknown Identifier Exception", e);
				}

				final EnergizerB2BUnitModel b2bUnitmodel = (EnergizerB2BUnitModel) b2bCommerceUnitService
						.getUnitForUid(csvValuesMap.get(CUSTOMER_ID).trim());

				if (null == energizerAddressModelList || energizerAddressModelList.size() == 0)
				{

					if (null != csvValuesMap.get(SP_CUSTOMER_ID) && !StringUtils.isEmpty(csvValuesMap.get(SP_CUSTOMER_ID)))
					{
						energizerAddress.setErpAddressId(csvValuesMap.get(SP_CUSTOMER_ID).trim());
						energizerAddress.setCompany(csvValuesMap.get(SP_NAME).trim());
						energizerAddress.setTown(csvValuesMap.get(SP_CITY).trim());
						energizerAddress.setPostalcode(csvValuesMap.get(SP_POSTAL_CODE).trim());
						energizerAddress.setStreetname(csvValuesMap.get(SP_STREET).trim());
						final String[] display = csvValuesMap.get(SP_SALES_PERSON).trim().split("@");
						energizerAddress.setDisplayName(display[0]);
						energizerAddress.setSalesPersonEmailId(csvValuesMap.get(SP_SALES_PERSON).trim());
						final boolean activeStatus = csvValuesMap.get(STATUS).trim().equalsIgnoreCase("1") ? true : false;
						energizerAddress.setShippingAddress(activeStatus);
						energizerAddress.setActive(Boolean.TRUE);
						if (catalogName.contains("EMEA"))
						{
							energizerAddress.setSoldToAddressId(erpAddressId);
						}
						else
						{
							energizerAddress.setSoldToAddressId(
									null != csvValuesMap.get(SOLD_TO_ADDRESS_ID) ? csvValuesMap.get(SOLD_TO_ADDRESS_ID).trim() : null);
						}

						try
						{
							final CountryModel countryModel = new CountryModel();
							countryModel.setIsocode(csvValuesMap.get(SP_COUNTRY_KEY).trim());
							final CountryModel countryModelfound = flexibleSearchService.getModelByExample(countryModel);
							energizerAddress.setCountry(countryModelfound);
							final RegionModel regionModel = new RegionModel();
							regionModel.setIsocode(csvValuesMap.get(SP_REGION).trim());
							regionModel.setCountry(countryModelfound);
							final RegionModel regionModelfound = flexibleSearchService.getModelByExample(regionModel);
							energizerAddress.setRegion(regionModelfound);
						}
						catch (final Exception e)
						{
							LOG.error("No Region or country Found", e);
						}

						if (b2bUnitmodel != null)
						{

							if (catalogName.equalsIgnoreCase(configurationService.getConfiguration().getString("catalogName"))
									&& null != csvValuesMap.get(SHIPPING_POINT_NO)
									&& !StringUtils.isEmpty(csvValuesMap.get(SHIPPING_POINT_NO)))
							{
								setEnergizerUnitLeadTime(b2bUnitmodel, csvValuesMap.get(SHIPPING_POINT_NO).trim(), erpAddressId,
										succeedRecord);
								LOG.info( catalogName + " catalogName");
								LOG.info(csvValuesMap.get(SHIPPING_POINT_NO).trim() + " printing SHIPPING_POINT_NO !");

							}
							else
							{
								LOG.info( catalogName + " catalogName ");
								LOG.info(SHIPPING_POINT_NO + " is null or empty !");
							}

							energizerAddress.setOwner(b2bUnitmodel);
							modelService.save(energizerAddress);
							LOG.info("CustomerAddress saved");
							succeedRecord++;
							setRecordSucceeded(succeedRecord);
						}
						else
						{
							LOG.error("Invalid attempt!!! No corresponding B2BUnit found ... ");
						}
					}
				}
				else if (null != energizerAddressModelList)
				{
					if (!csvValuesMap.get(SP_CUSTOMER_ID).trim().isEmpty())
					{
						energizerAddress = energizerAddressModelList.get(0);
						energizerAddress.setCompany(csvValuesMap.get(SP_NAME).trim());
						energizerAddress.setTown(csvValuesMap.get(SP_CITY).trim());
						energizerAddress.setPostalcode(csvValuesMap.get(SP_POSTAL_CODE).trim());
						energizerAddress.setStreetname(csvValuesMap.get(SP_STREET).trim());

						try
						{
							final CountryModel countryModel = new CountryModel();
							countryModel.setIsocode(csvValuesMap.get(SP_COUNTRY_KEY).trim());
							final CountryModel countryModelfound = flexibleSearchService.getModelByExample(countryModel);
							energizerAddress.setCountry(countryModelfound);
							final RegionModel regionModel = new RegionModel();
							regionModel.setIsocode(csvValuesMap.get(SP_REGION).trim());
							regionModel.setCountry(countryModelfound);
						}
						catch (final Exception e)
						{
							LOG.error("No Region or country Found", e);
						}
						energizerAddress.setSalesPersonEmailId(csvValuesMap.get(SP_SALES_PERSON).trim());
						final String[] display = csvValuesMap.get(SP_SALES_PERSON).trim().split("@");
						energizerAddress.setDisplayName(display[0]);
						//						energizerAddress.setShippingAddress(new Boolean(csvValuesMap.get(STATUS).trim()));
						final boolean activeStatus = csvValuesMap.get(STATUS).trim().equalsIgnoreCase("1") ? true : false;
						energizerAddress.setShippingAddress(activeStatus);
						energizerAddress.setActive(Boolean.TRUE);

						if (catalogName.contains("EMEA"))
						{
							energizerAddress.setSoldToAddressId(erpAddressId);
						}
						else
						{
							energizerAddress.setSoldToAddressId(
									null != csvValuesMap.get(SOLD_TO_ADDRESS_ID) ? csvValuesMap.get(SOLD_TO_ADDRESS_ID).trim() : null);
						}

						if (catalogName.equalsIgnoreCase(configurationService.getConfiguration().getString("catalogName"))
								&& null != csvValuesMap.get(SHIPPING_POINT_NO)
								&& !StringUtils.isEmpty(csvValuesMap.get(SHIPPING_POINT_NO)))
						{
							setEnergizerUnitLeadTime(b2bUnitmodel, csvValuesMap.get(SHIPPING_POINT_NO).trim(), erpAddressId,
									succeedRecord);
						}
						else
						{
							LOG.info(catalogName + "catalogName in B2b unit is present");
							LOG.info(SHIPPING_POINT_NO + " is null or empty !");
						}

						modelService.save(energizerAddress);
						succeedRecord++;
						setRecordSucceeded(succeedRecord);
						LOG.info("Models are updated");
					}
				}
			}
			LOG.info("EnergizerCustomerAddressCSVProcessor:process:Start");
		}
		catch (final Exception e)
		{
			LOG.error("EnergizerCustomerAddressCSVProcessor:Exception in saving EnergizerCustomerAddress", e);
		}

		getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
		getBusinessFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	/**
	 * @param record
	 * @param csvValuesMap
	 * @return
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader);
			if (value != null && value.isEmpty())
			{
				long recordFailed = getRecordFailed();
				error = new EnergizerCSVFeedError();
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error.setLineNumber(record.getRecordNumber() + 1);
				columnNames.add(columnHeader);
				error.setUserType(TECHNICAL_USER);
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


	/**
	 * @param b2bUnitmodel
	 *                           the b2bUnitmodel
	 * @param shippingPointNo
	 *                           the shippingPointNo
	 * @param erpAddressId
	 *                           the erpAddressId
	 * @param succeedRecord
	 *                           the succeedRecord
	 *
	 *                           This method creates/updates the Lead Time model based on the B2bUnitModel, ShippingPointNo
	 *                           and ErpAddressId
	 */
	public void setEnergizerUnitLeadTime(final EnergizerB2BUnitModel b2bUnitmodel, final String shippingPointNo,
			final String erpAddressId, Long succeedRecord)
	{
		EnergizerB2BUnitLeadTimeModel energizerB2BUnitLeadTimeModel = getEnergizerB2BUnitLeadTime(b2bUnitmodel, shippingPointNo,
				erpAddressId);

		if (energizerB2BUnitLeadTimeModel == null)
		{
			energizerB2BUnitLeadTimeModel = modelService.create(EnergizerB2BUnitLeadTimeModel.class);
			energizerB2BUnitLeadTimeModel.setB2bUnitId(b2bUnitmodel);
			energizerB2BUnitLeadTimeModel.setShippingPointId(shippingPointNo);
			energizerB2BUnitLeadTimeModel.setSoldToAddressId(erpAddressId);
			modelService.save(energizerB2BUnitLeadTimeModel);
			LOG.info("Customer lead time model is saved for '" + b2bUnitmodel.getUid() + "' without Lead time ");
			succeedRecord++;
			setRecordSucceeded(succeedRecord);
		}
		else
		{
			energizerB2BUnitLeadTimeModel.setB2bUnitId(b2bUnitmodel);
			energizerB2BUnitLeadTimeModel.setShippingPointId(shippingPointNo);
			energizerB2BUnitLeadTimeModel.setSoldToAddressId(erpAddressId);
			modelService.save(energizerB2BUnitLeadTimeModel);
			LOG.info("Customer lead time model is updated for the unit '" + b2bUnitmodel.getUid() + "' without Lead time");
			succeedRecord++;
			setRecordSucceeded(succeedRecord);
		}
	}


	private EnergizerB2BUnitLeadTimeModel getEnergizerB2BUnitLeadTime(final EnergizerB2BUnitModel energizerB2BUnitModel,
			final String shippingPointNo, final String shipTo)
	{
		EnergizerB2BUnitLeadTimeModel energizerB2BUnitLeadTimeModel = null;
		final String pkEnergizerB2BUnit;
		pkEnergizerB2BUnit = energizerB2BUnitModel.getPk().getLongValueAsString();
		final List<EnergizerB2BUnitLeadTimeModel> energizerB2BUnitLeadTimeModels = defaultEnergizerCustomerLeadTimeFacade
				.fetchEnergizerB2BUnitLeadTime(pkEnergizerB2BUnit, shippingPointNo, shipTo);

		if (!(energizerB2BUnitLeadTimeModels.isEmpty()))
		{
			energizerB2BUnitLeadTimeModel = energizerB2BUnitLeadTimeModels.get(0);
		}
		return energizerB2BUnitLeadTimeModel;
	}

}
