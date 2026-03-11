/**
 *
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.MetricUnitModel;


/**
 *
 * This processors imports the product conversion.
 *
 * Sample file will look like
 *
 * ERPMaterialID,AlternateUOM,BaseUOMMultiplier,VolumeInUOM,VolumeUOM,WeightInUOM,WeightUOM 3, EA, 1, 0.311, CDM,
 * 126.552 ,G
 *
 * Total column count : 7
 */
public class EnergizerProductConversionCSVProcessor extends AbstractEnergizerCSVProcessor
{

	@Resource
	private ModelService modelService;
	@Resource
	private SessionService sessionService;
	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Resource
	private ProductService productService;
	@Resource
	private CommonI18NService defaultCommonI18NService;
	@Resource
	private UnitService defaultUnitService;
	@Resource
	ConfigurationService configurationService;

	private static final Logger LOG = Logger.getLogger(EnergizerProductConversionCSVProcessor.class);

	private static final String ENERGIZER_PRODUCT_CONVERSION = "feedprocessor.energizerOrderUpdateFeed.mandatory";

	private static final String REDUCED_INCHES_FROM_HEIGHT = "reduced.height";
	private static final String REDUCED_INCHES_FROM_HEIGHT_EMEA = "reduced.height.EMEA";//EMEA variable
	private Double reducedHeightInInches;//Class level variable introduced for region specific changes

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		List<EnergizerProductConversionFactorModel> energizerProductFactorList = null;
		EnergizerProductModel energizerProduct = null;
		try
		{
			final CatalogVersionModel catalogVersion = this.getCatalogVersion(cronjob);
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord record : records)
			{
				LOG.info(" CSV Record number: " + record.getRecordNumber());
				LOG.info(" CSV Record: " + record.toMap());

				final Map<String, String> csvValuesMap = record.toMap();
				validate(record);

				if (!getTechnicalFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
					getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
					getTechnicalFeedErrors().clear();
					continue;
				}
				/*
				 * Retrieve the energizerProduct based on an erpMaterialId. After retrieval check for the existence of
				 * EnergizerProductConversionFactors field.
				 */
				try
				{
					energizerProduct = (EnergizerProductModel) productService.getProductForCode(catalogVersion,
							csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).trim());

				}
				catch (final Exception e)
				{
					LOG.error(e.getMessage());
					continue;
				}
				if (energizerProduct == null)
				{
					LOG.error((csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID) + " EnergizerProduct does  not exist ");
					continue;
				}
				//if (energizerProduct != null)
				//{
				LOG.info("THE DETAILS OF THE PRODUCT IS :" + energizerProduct.getCode() + " " + energizerProduct.getApprovalStatus());
				//Check for the EnergizerConversionFactor
				energizerProductFactorList = energizerProduct.getProductConversionFactors();
				final ArrayList<EnergizerProductConversionFactorModel> tmpFactorList = new ArrayList<EnergizerProductConversionFactorModel>();

				//If there is no associated productConversionFactor then create and attach with the product
				EnergizerProductConversionFactorModel energizerProductConversionModel = null;

				try
				{
					boolean matchingRecordFound = false;
					if (energizerProductFactorList != null)
					{
						tmpFactorList.addAll(energizerProductFactorList);
						LOG.info("The size of productConversionFactorModels is :" + tmpFactorList.size());
						//Retrieve the productConversionFactor and perform the matching process and do an update in case of any mismatch

						for (final EnergizerProductConversionFactorModel energizerProductConversionFactorModel : energizerProductFactorList)
						{
							LOG.info("Factor Model UOM is:" + energizerProductConversionFactorModel.getAlternateUOM());

							if (isModelSame(energizerProductConversionFactorModel, csvValuesMap))
							{
								matchingRecordFound = true;
								energizerProductConversionModel = energizerProductConversionFactorModel;
								break;
							}
						}
					}
					if (!matchingRecordFound)
					{
						energizerProductConversionModel = modelService.create(EnergizerProductConversionFactorModel.class);
						energizerProductConversionModel
								.setErpMaterialId(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).trim());
						energizerProductConversionModel.setAlternateUOM(csvValuesMap.get(EnergizerCoreConstants.ALTERNATE_UOM).trim()); //PALLET, LAYER, CASE
						tmpFactorList.add(energizerProductConversionModel);
					}
					this.addUpdateRecord(energizerProductConversionModel, csvValuesMap);

					energizerProduct.setProductConversionFactors(tmpFactorList);
					modelService.saveAll();
					succeedRecord++;
					setRecordSucceeded(succeedRecord);
				}
				catch (final Exception e)
				{
					LOG.error("Error ---- " + e);
					//this.logErrors(errors);
					continue;
				}

				//}
			}
		}
		catch (final Exception e)
		{
			LOG.error("Error ---- " + e);

		}
		getTechnicalFeedErrors().addAll(techFeedErrorRecords);
		getBusinessFeedErrors().addAll(businessFeedErrorRecords);
		techFeedErrorRecords.clear();
		businessFeedErrorRecords.clear();
		return getCsvFeedErrorRecords();
	}

	private void addUpdateRecord(final EnergizerProductConversionFactorModel conversionModel,
			final Map<String, String> csvValuesMap) throws Exception
	{
		/* Changes done to pick region specific values from the properties file */
		if (this.getCronjob().getCatalogName().contains("EMEA"))
		{
			reducedHeightInInches = configurationService.getConfiguration().getDouble(REDUCED_INCHES_FROM_HEIGHT_EMEA, null);
		}
		else
		{
			reducedHeightInInches = configurationService.getConfiguration().getDouble(REDUCED_INCHES_FROM_HEIGHT, null);
		}
		LOG.info("Reduced Height :" + reducedHeightInInches);
		LOG.info("Entering method addUpdateRecord.....");

		if (NumberUtils.isNumber(csvValuesMap.get(EnergizerCoreConstants.BASE_UOM_MULTIPLIER).trim()))
		{
			conversionModel
					.setConversionMultiplier(Integer.parseInt(csvValuesMap.get(EnergizerCoreConstants.BASE_UOM_MULTIPLIER).trim())); //conversion multiplier
		}

		if ((!csvValuesMap.get(EnergizerCoreConstants.VOLUME_IN_UOM).trim().isEmpty()
				&& NumberUtils.isNumber(csvValuesMap.get(EnergizerCoreConstants.VOLUME_IN_UOM).trim()))
				&& !csvValuesMap.get(EnergizerCoreConstants.VOLUME_UOM).trim().isEmpty())
		{


			MetricUnitModel packageVolumeMetric = conversionModel.getPackageVolume();
			if (packageVolumeMetric == null)
			{
				packageVolumeMetric = modelService.create(MetricUnitModel.class);
			}
			packageVolumeMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.VOLUME_IN_UOM).trim()));
			packageVolumeMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.VOLUME_UOM).trim());

			conversionModel.setPackageVolume(packageVolumeMetric);
		}


		if ((!csvValuesMap.get(EnergizerCoreConstants.WEIGHT_IN_UOM).trim().isEmpty()
				&& NumberUtils.isNumber(csvValuesMap.get(EnergizerCoreConstants.WEIGHT_IN_UOM).trim()))
				&& !csvValuesMap.get(EnergizerCoreConstants.WEIGHT_UOM).trim().isEmpty())
		{
			MetricUnitModel packageWeightMetric = conversionModel.getPackageWeight();
			if (packageWeightMetric == null)
			{
				packageWeightMetric = modelService.create(MetricUnitModel.class);
			}
			packageWeightMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.WEIGHT_IN_UOM).trim()));
			packageWeightMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.WEIGHT_UOM).trim());

			conversionModel.setPackageWeight(packageWeightMetric);
		}
		if ((!csvValuesMap.get(EnergizerCoreConstants.LENGTH).trim().isEmpty()
				&& NumberUtils.isNumber(csvValuesMap.get(EnergizerCoreConstants.LENGTH).trim()))
				&& !csvValuesMap.get(EnergizerCoreConstants.UNIT).trim().isEmpty())
		{
			MetricUnitModel packageLengthMetric = conversionModel.getPackageLength();
			if (packageLengthMetric == null)
			{
				packageLengthMetric = modelService.create(MetricUnitModel.class);
			}
			packageLengthMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.LENGTH).trim()));
			packageLengthMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.UNIT).trim());

			conversionModel.setPackageLength(packageLengthMetric);
		}

		if ((!csvValuesMap.get(EnergizerCoreConstants.WIDTH).trim().isEmpty()
				&& NumberUtils.isNumber(csvValuesMap.get(EnergizerCoreConstants.WIDTH).trim()))
				&& !csvValuesMap.get(EnergizerCoreConstants.UNIT).trim().isEmpty())
		{

			MetricUnitModel packageWidthMetric = conversionModel.getPackageWidth();
			if (packageWidthMetric == null)
			{
				packageWidthMetric = modelService.create(MetricUnitModel.class);
			}
			packageWidthMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.WIDTH).trim()));
			packageWidthMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.UNIT).trim());

			conversionModel.setPackageWidth(packageWidthMetric);
		}

		if ((!csvValuesMap.get(EnergizerCoreConstants.HEIGHT).trim().isEmpty()
				&& NumberUtils.isNumber(csvValuesMap.get(EnergizerCoreConstants.HEIGHT).trim()))
				&& !csvValuesMap.get(EnergizerCoreConstants.UNIT).trim().isEmpty())
		{

			MetricUnitModel packageHeightMetric = conversionModel.getPackageHeight();
			if (packageHeightMetric == null)
			{
				packageHeightMetric = modelService.create(MetricUnitModel.class);
			}
			if (conversionModel.getAlternateUOM().equals("PAL"))
			{
				LOG.info("Uom: " + conversionModel.getAlternateUOM());
				packageHeightMetric.setMeasurement(
						Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.HEIGHT).trim()) - reducedHeightInInches);

			}
			else
			{
				packageHeightMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.HEIGHT).trim()));
			}

			packageHeightMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.UNIT).trim());
			conversionModel.setPackageHeight(packageHeightMetric);

		}


		LOG.info("Ending method addUpdateRecord.....");
	}

	private boolean isModelSame(final EnergizerProductConversionFactorModel conversionModel,
			final Map<String, String> csvValuesMap)
	{
		if (null != conversionModel && null != conversionModel.getErpMaterialId() && null != conversionModel.getAlternateUOM()
				&& conversionModel.getErpMaterialId().trim()
						.equalsIgnoreCase(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).trim())
				&& conversionModel.getAlternateUOM().trim()
						.equalsIgnoreCase(csvValuesMap.get(EnergizerCoreConstants.ALTERNATE_UOM).trim()))
		{
			// record exists, just update other attributes
			LOG.info("Product Conversion record exists for ERP_ID : " + conversionModel.getErpMaterialId() + " AlternateUOM : "
					+ conversionModel.getAlternateUOM());
			return true;
		}
		return false;
	}



	/**
	 *
	 * @param record
	 * @return
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		final Map<String, String> map = record.toMap();
		boolean flag = false;

		if (map.get(EnergizerCoreConstants.ALTERNATE_UOM).trim().equals("PAL")
				|| map.get(EnergizerCoreConstants.ALTERNATE_UOM).trim().equals("CS"))
		{
			flag = true;
		}
		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());

		for (final String columnHeader : map.keySet())
		{

			columnNumber++;
			long recordFailed = getRecordFailed();
			setTotalRecords(record.getRecordNumber());
			final String value = map.get(columnHeader).trim();


			if (flag)
			{


				if (value.isEmpty())
				{

					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setLineNumber(record.getRecordNumber() + 1);
					columnNames.add(columnHeader);
					error.setUserType(TECHNICAL_USER);
					error.setColumnName(columnNames);
					columnNumbers.add(columnNumber);
					error.setMessage(columnHeader + " column should not be empty");
					error.setColumnNumber(columnNumbers);
					getTechnicalFeedErrors().add(error);
					setTechRecordError(getTechnicalFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}


				if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.BASE_UOM_MULTIPLIER))
				{
					if (!NumberUtils.isNumber(value) || Double.valueOf(value) <= 0.0)
					{
						final List<String> columnNames = new ArrayList<String>();
						final List<Integer> columnNumbers = new ArrayList<Integer>();
						error = new EnergizerCSVFeedError();
						error.setUserType(TECHNICAL_USER);
						columnNames.add(columnHeader);
						error.setLineNumber(record.getRecordNumber() + 1);
						error.setColumnName(columnNames);
						error.setMessage(columnHeader + " column should be numeric and greater than 0");
						columnNumbers.add(columnNumber);
						error.setColumnNumber(columnNumbers);
						getTechnicalFeedErrors().add(error);
						setTechRecordError(getTechnicalFeedErrors().size());
						recordFailed++;
						setRecordFailed(recordFailed);
					}
				}

				if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.VOLUME_IN_UOM)
						|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.WEIGHT_IN_UOM)
						|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.LENGTH)
						|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.WIDTH)
						|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.HEIGHT))
				{


					if (!NumberUtils.isNumber(value) || new BigDecimal(value).compareTo(BigDecimal.ZERO) == 0)
					{
						final List<String> columnNames = new ArrayList<String>();
						final List<Integer> columnNumbers = new ArrayList<Integer>();
						error = new EnergizerCSVFeedError();
						error.setUserType(TECHNICAL_USER);
						columnNames.add(columnHeader);
						error.setLineNumber(record.getRecordNumber() + 1);
						error.setColumnName(columnNames);
						error.setMessage(columnHeader + " column should be numeric and greater than 0");
						columnNumbers.add(columnNumber);
						error.setColumnNumber(columnNumbers);
						getTechnicalFeedErrors().add(error);
						setTechRecordError(getTechnicalFeedErrors().size());
						recordFailed++;
						setRecordFailed(recordFailed);

					}
				}


			}

			else if (map.get(EnergizerCoreConstants.ALTERNATE_UOM).trim().isEmpty())
			{

				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error = new EnergizerCSVFeedError();
				error.setLineNumber(record.getRecordNumber() + 1);
				columnNames.add(columnHeader);
				error.setUserType(TECHNICAL_USER);
				error.setColumnName(columnNames);
				columnNumbers.add(columnNumber);
				error.setMessage(columnHeader + " column should not be empty");
				error.setColumnNumber(columnNumbers);
				getTechnicalFeedErrors().add(error);
				setTechRecordError(getTechnicalFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);


			}


		}
	}
}
