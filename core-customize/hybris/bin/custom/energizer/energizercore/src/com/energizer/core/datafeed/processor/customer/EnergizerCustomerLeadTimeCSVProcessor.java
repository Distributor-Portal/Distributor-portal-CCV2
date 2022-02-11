/**
 *
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.facade.impl.DefaultEnergizerCustomerLeadTimeFacade;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCronJobModel;


/**
 *
 * This processors imports the customer lead time.
 *
 * Sample file will look like
 *
 * EnergizerAccountID, ShippingPointNo, ShipTo, LeadTimeInDays 43211, 100, dist112, 1
 *
 * Total column count : 4
 */
public class EnergizerCustomerLeadTimeCSVProcessor extends AbstractEnergizerCSVProcessor
{

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.datafeed.EnergizerCSVProcessor#parse(java.io.File)
	 */
	@Resource
	private ModelService modelService;

	@Resource
	private B2BCommerceUnitService b2bCommerceUnitService;

	@Resource
	protected FlexibleSearchService flexibleSearchService;

	@Resource
	private DefaultEnergizerCustomerLeadTimeFacade defaultEnergizerCustomerLeadTimeFacade;
	private static final Logger LOG = Logger.getLogger(EnergizerCustomerLeadTimeCSVProcessor.class);

	private static String ENERGIZER_ACCOUNT_ID = "EnergizerAccountID";
	private static String SHIPPING_POINT_NO = "ShippingPointNo";
	private static String SHIP_TO = "ShipTo";
	private static String LEAD_TIME_IN_DAYS = "LeadTimeInDays";
	private static String energizerAccountID = "";
	private static String shippingPointNo = "";
	private static String shipTo = "";
	private static Integer leadTimeInDays = 0;
	private static final long DEFAULT_LEAD_TIME = 30;
	private static final String DEFAULT_LEAD_TIME_VALUE = "defaultLeadTime";

	private static final long DEFAULT_LEAD_TIME_EMEA = 4;
	private static final String DEFAULT_LEAD_TIME_VALUE_EMEA = "defaultLeadTimeEMEA";
	private int defaultLeadTime;
	@Autowired
	private ConfigurationService configurationService;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.datafeed.EnergizerCSVProcessor#process(java.lang.Iterable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{

		try
		{
			if (super.getCronjob().getCatalogName().contains("EMEA"))
			{
				defaultLeadTime = configurationService.getConfiguration()
						.getBigInteger(DEFAULT_LEAD_TIME_VALUE_EMEA, BigInteger.valueOf(DEFAULT_LEAD_TIME_EMEA)).intValue();
			}
			else
			{
				defaultLeadTime = configurationService.getConfiguration()
						.getBigInteger(DEFAULT_LEAD_TIME_VALUE, BigInteger.valueOf(DEFAULT_LEAD_TIME)).intValue();
			}
			LOG.info("EnergizerCustomerLeadTimeCSVProcessor:process:Start");
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord record : records)
			{
				final boolean isRecordEmpty = validate(record);
				if (!getBusinessFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getBusinessFeedErrors());
					getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
					getBusinessFeedErrors().clear();
					if (isRecordEmpty)
					{
						continue;
					}
				}
				final Map<String, String> csvValuesMap = record.toMap();
				energizerAccountID = csvValuesMap.get(ENERGIZER_ACCOUNT_ID).trim();
				shippingPointNo = csvValuesMap.get(SHIPPING_POINT_NO).trim();
				shipTo = csvValuesMap.get(SHIP_TO).trim();
				//set the default LeadTimeInDays to 30 Days as it is configured, else get value from csv map
				if (csvValuesMap.get(LEAD_TIME_IN_DAYS).isEmpty() || Integer.valueOf(csvValuesMap.get(LEAD_TIME_IN_DAYS)) == 0)
				{
					leadTimeInDays = defaultLeadTime;
				}
				else
				{
					leadTimeInDays = Integer.valueOf(csvValuesMap.get(LEAD_TIME_IN_DAYS).trim());
				}
				final EnergizerB2BUnitModel energizerB2BUnitModel = isEnergizerAccountExist(energizerAccountID);
				if (energizerB2BUnitModel != null)
				{
					EnergizerB2BUnitLeadTimeModel energizerB2BUnitLeadTimeModel = getEnergizerB2BUnitLeadTime(energizerB2BUnitModel,
							shippingPointNo, shipTo);
					if (energizerB2BUnitLeadTimeModel == null)
					{
						energizerB2BUnitLeadTimeModel = modelService.create(EnergizerB2BUnitLeadTimeModel.class);
						energizerB2BUnitLeadTimeModel.setB2bUnitId(energizerB2BUnitModel);
						energizerB2BUnitLeadTimeModel.setShippingPointId(shippingPointNo);
						energizerB2BUnitLeadTimeModel.setSoldToAddressId(shipTo);
						energizerB2BUnitLeadTimeModel.setLeadTime(leadTimeInDays);
						modelService.save(energizerB2BUnitLeadTimeModel);
						LOG.info("Customer lead time is saved for '" + energizerB2BUnitModel.getUid() + "' with Lead time "
								+ leadTimeInDays);
						succeedRecord++;
						setRecordSucceeded(succeedRecord);
					}
					else
					{
						energizerB2BUnitLeadTimeModel.setB2bUnitId(energizerB2BUnitModel);
						energizerB2BUnitLeadTimeModel.setShippingPointId(shippingPointNo);
						energizerB2BUnitLeadTimeModel.setSoldToAddressId(shipTo);
						energizerB2BUnitLeadTimeModel.setLeadTime(leadTimeInDays);
						modelService.save(energizerB2BUnitLeadTimeModel);
						LOG.info("Customer lead time is updated for the unit '" + energizerB2BUnitModel.getUid() + "' with Lead time "
								+ leadTimeInDays);
						succeedRecord++;
						setRecordSucceeded(succeedRecord);
					}
				}
			} //end of for loop
		} //end of try block
		catch (final Exception e)
		{
			LOG.error("EnergizerCustomerLeadTimeCSVProcessor ", e);
		}
		getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
		getTechnicalFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	/**
	 * @param energizerB2BUnitModel
	 * @param shippingPointNo
	 * @param shipTo
	 * @return EnergizerB2BUnitLeadTimeModel
	 */
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

	//Check Energizer Account is exist or not if exist return correspond EnergizerB2BUnitModel object otherwise return null
	public EnergizerB2BUnitModel isEnergizerAccountExist(final String accountId)
	{
		final EnergizerB2BUnitModel energizerB2BUnitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(accountId);
		return energizerB2BUnitModel;
	}

	/**
	 * @param record
	 *
	 */
	private boolean validate(final CSVRecord record)
	{
		boolean isRecordEmpty = false;
		EnergizerCSVFeedError error = null;
		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader);
			if (columnHeader.equalsIgnoreCase(ENERGIZER_ACCOUNT_ID) || columnHeader.equalsIgnoreCase(SHIP_TO)
					|| columnHeader.equalsIgnoreCase(SHIPPING_POINT_NO))
			{
				if (value.isEmpty())
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					long recordFailed = getRecordFailed();
					error = new EnergizerCSVFeedError();
					error.setLineNumber(record.getRecordNumber() + 1);
					columnNames.add(columnHeader);
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should not be empty");
					columnNumbers.add(columnNumber);
					error.setUserType(BUSINESS_USER);
					error.setColumnNumber(columnNumbers);
					getBusinessFeedErrors().add(error);
					setBusRecordError(getBusinessFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
					isRecordEmpty = true;
				}
			}
			if (columnHeader.equalsIgnoreCase(LEAD_TIME_IN_DAYS))
			{
				if (!NumberUtils.isNumber(value) || Double.valueOf(value) <= 0.0)
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					long recordFailed = getRecordFailed();
					error = new EnergizerCSVFeedError();
					error.setUserType(BUSINESS_USER);
					error.setErrorCode("ECLT2001");
					error.setLineNumber(record.getRecordNumber() + 1);
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should be numeric value & greater than 0");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					getBusinessFeedErrors().add(error);
					setBusRecordError(getBusinessFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
			}
		}
		return isRecordEmpty;
	}
}
