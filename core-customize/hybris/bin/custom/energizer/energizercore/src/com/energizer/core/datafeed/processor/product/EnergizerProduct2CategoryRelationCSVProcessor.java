/**
 *
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;



public class EnergizerProduct2CategoryRelationCSVProcessor extends AbstractEnergizerCSVProcessor
{

	@Resource
	private ModelService modelService;

	@Resource
	CatalogService catalogService;

	@Resource
	CatalogVersionService catalogVersionService;

	@Resource
	private EnergizerProductService energizerProductService;

	@Resource
	private CategoryService categoryService;

	@Resource
	private ConfigurationService configurationService;

	private static String CSV_HEADERS[] = null;

	private static final String PRODUCT_CATEGORY_FEED_HEADERS_KEY = "feedprocessor.productcategoryrelationfeed.headers";
	private static final String PRODUCT_CATEGORY_FEED_HEADERS_MANDATORY_KEY = "feedprocessor.productcategoryrelationfeed.headers.mandatory";

	private static final Logger LOG = Logger.getLogger(EnergizerProduct2CategoryRelationCSVProcessor.class);

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogId,
			final EnergizerCronJobModel cronjob)
	{
		final String catalogName = Config.getParameter(FEED_PROCESSOR_PRODUCT_CATALOG_NAME);
		final String catalogVersion = Config.getParameter(FEED_PROCESSOR_PRODUCT_CATALOG_VERSION);
		//final CatalogVersionModel catalogModelVersionModel = catalogVersionService.getCatalogVersion(catalogName, catalogVersion);
		long succeedRecord = getRecordSucceeded();
		CSV_HEADERS = Config.getParameter(PRODUCT_CATEGORY_FEED_HEADERS_KEY).split(new Character(DELIMETER).toString());
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
			final List<EnergizerProductModel> products = energizerProductService
					.getEnergizerProductListForSapCatgy(csvValuesMap.get(CSV_HEADERS[0]).trim());

			if (products != null && !products.isEmpty())
			{
				for (final EnergizerProductModel enrProductModel : products)
				{
					if (csvValuesMap.get(CSV_HEADERS[4]) != null)
					{
						enrProductModel.setSearchAttribute(csvValuesMap.get(CSV_HEADERS[4]).trim());
					}
					mapProduct2Category(enrProductModel, csvValuesMap, catalogId);
					modelService.save(enrProductModel);
					succeedRecord++;
					setRecordSucceeded(succeedRecord);
				}
			}
		}
		getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
		getBusinessFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	private void mapProduct2Category(final EnergizerProductModel enrProductModel, final Map<String, String> csvValuesMap,
			final String catalogId)
	{
		//get category for concatenated value
		final StringBuilder concatCatgy = new StringBuilder(csvValuesMap.get(CSV_HEADERS[1]).trim());
		final boolean eanStartsWith0245 = ((null != enrProductModel.getEan() && !StringUtils.isEmpty(enrProductModel.getEan()))
				? enrProductModel.getEan().startsWith(EnergizerCoreConstants.UPC_STARTS_WITH_0245)
				: false);
		final boolean eanStartsWith049 = ((null != enrProductModel.getEan() && !StringUtils.isEmpty(enrProductModel.getEan()))
				? enrProductModel.getEan().startsWith(EnergizerCoreConstants.UPC_STARTS_WITH_049)
				: false);

		// For LATAM
		if (catalogId.equalsIgnoreCase(EnergizerCoreConstants.PERSONALCARE_PRODUCTCATALOG))
		{
			// For PBG Products
			if (null != enrProductModel.getIsPBG() && enrProductModel.getIsPBG())
			{
				// Save Level2 & Level 3 headers for LATAM
				if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.LATAM))
				{
					// If skip property is set 'true', then add the custom logic
					if (Boolean.valueOf(
							configurationService.getConfiguration().getString(EnergizerCoreConstants.PBG_SKIP_CSV_LEVEL23_HEADERS)))
					{
						LOG.debug("Skip CSV Level2 & Level3 Headers and read it from the properties configured in local !");
						// For UPC/EAN starting with '0245'
						if (null != enrProductModel.getEan() && !StringUtils.isEmpty(enrProductModel.getEan()) && eanStartsWith0245)
						{
							concatCatgy.append("-");
							concatCatgy.append(
									configurationService.getConfiguration().getString(EnergizerCoreConstants.PBG_UPC0245_BRAND_NAME));
						}
						else if (null != enrProductModel.getEan() && !StringUtils.isEmpty(enrProductModel.getEan()) && eanStartsWith049)
						{ // For UPC/EAN starting with '049'
							concatCatgy.append("-");
							concatCatgy.append(
									configurationService.getConfiguration().getString(EnergizerCoreConstants.PBG_UPC049_BRAND_NAME));
						}
						else if (null == enrProductModel.getEan())
						{
							LOG.info("EAN/UPC is Null, material : " + enrProductModel.getCode() + " !! ");
						}
						else if (null != enrProductModel.getEan() && !StringUtils.isEmpty(enrProductModel.getEan())
								&& !(eanStartsWith0245 || eanStartsWith049))
						{
							LOG.info("EAN/UPC starts with '" + enrProductModel.getEan() + "', material : " + enrProductModel.getCode()
									+ ", so saving it under Level 1 category itself !! ");
						}
						else
						{ // NO Brand Label associated for this EAN/UPC in the properties file !
							LOG.info("Please configure brand label as a property for EAN/UPC starting with '" + enrProductModel.getEan()
									+ "'. NO Brand Label associated for this EAN/UPC in the properties file !");
						}
					}
					else
					{
						// If the skip property is set 'false', then DO NOT Skip CSV Level2 & Level3 Headers. Save the brand labels from CSV feed file as it is !
						saveCSVLevel23Headers(csvValuesMap, concatCatgy);
					}
				}
				//Do not save Level 2 & Level 3 headers for WeSell in case of PBG products, the products will be automatically saved directly under Level 1 category i..e 'PBG' Menu
			}
			else
			// For Non-PBG products - both LATAM & WESELL
			{
				saveCSVLevel23Headers(csvValuesMap, concatCatgy);
			}
		}
		else
		// Save Level2 & Level 3 headers for EMEA
		{
			saveCSVLevel23Headers(csvValuesMap, concatCatgy);
		}

		// Do not save Level2 & Level 3 headers for WeSell products.
		LOG.debug("PRODUCT CODE = " + enrProductModel.getCode() + ", CATEGORY CODE = " + concatCatgy);
		//prdtCategoryModel.setProducts(enrProductModels);
		enrProductModel.setSupercategories(categoryService.getCategoriesForCode(concatCatgy.toString()));
	}


	/**
	 * @param csvValuesMap
	 * @param concatCatgy
	 */
	private void saveCSVLevel23Headers(final Map<String, String> csvValuesMap, final StringBuilder concatCatgy)
	{

		if (csvValuesMap.get(CSV_HEADERS[2]) != null && !csvValuesMap.get(CSV_HEADERS[2]).trim().isEmpty())
		{
			concatCatgy.append("-");
			concatCatgy.append(csvValuesMap.get(CSV_HEADERS[2]).trim());
		}

		if (csvValuesMap.get(CSV_HEADERS[3]) != null && !csvValuesMap.get(CSV_HEADERS[3]).trim().isEmpty())
		{
			concatCatgy.append("-");
			concatCatgy.append(csvValuesMap.get(CSV_HEADERS[3]).trim());
		}

	}

	/**
	 * @param record
	 * @param csvValuesMap
	 * @return
	 */
	private EnergizerCSVFeedError validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;

		if (!hasMandatoryFields(record, getHeadersForFeed(PRODUCT_CATEGORY_FEED_HEADERS_KEY)))
		{
			final List<String> mandatoryFields = Arrays.asList(
					Config.getParameter(PRODUCT_CATEGORY_FEED_HEADERS_MANDATORY_KEY).split(new Character(DELIMETER).toString()));
			final Map<String, String> map = record.toMap();
			Integer columnNumber = 0;
			final List<String> columnNames = new ArrayList<String>();
			final List<Integer> columnNumbers = new ArrayList<Integer>();
			long recordFailed = getRecordFailed();
			for (final String columnHeader : map.keySet())
			{
				setTotalRecords(record.getRecordNumber());
				if (mandatoryFields.contains(columnHeader))
				{
					columnNumber++;
					final String value = map.get(columnHeader);

					if (value.isEmpty())
					{
						error = new EnergizerCSVFeedError();
						error.setLineNumber(record.getRecordNumber() + 1);
						columnNames.add(columnHeader);
						error.setColumnName(columnNames);
						error.setMessage(columnHeader + " column should not be empty");
						columnNumbers.add(columnNumber);
						error.setUserType(TECHNICAL_USER);
						error.setColumnNumber(columnNumbers);
						getTechnicalFeedErrors().add(error);
						setTechRecordError(getTechnicalFeedErrors().size());
						recordFailed++;
						setRecordFailed(recordFailed);
					}
				}
			}
		}
		return error;
	}

}
