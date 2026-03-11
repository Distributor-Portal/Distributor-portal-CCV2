/**
 *
 */
package com.energizer.core.datafeed.processor;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerProductModel;



/**
 *
 * This processors imports the product category.
 *
 * Sample file will look like
 *
 * ERPMaterialID,SegmentName,SegmentDescription,FamilyName,
 * FamilyDescription,GroupName,GroupDescription,SubGroupName,SubGroupDescription E0213600, Skin Care, skin care product,
 * Skin Care-Edge, abc product, Skin Care-Edge-Holder, ,12345, skin care prodcut
 *
 * Total column count : 9
 */
public class EnergizerProductCategoryCSVProcessor extends AbstractEnergizerCSVProcessor
{


	@Resource
	private ModelService modelService;

	@Resource
	private SessionService sessionService;


	@Resource
	private ProductService productService;

	@Resource
	CatalogService catalogService;

	@Resource
	CatalogVersionService catalogVersionService;

	private static String CSV_HEADERS[] = null;

	private static final String MATERIAL_CATEGORY_FEED_HEADERS_KEY = "feedprocessor.materialcategoryfeed.headers";
	private static final String MATERIAL_CATEGORY_FEED_HEADERS_MANDATORY_KEY = "feedprocessor.materialcategoryfeed.headers.mandatory";


	private static final Logger LOG = Logger.getLogger(EnergizerProductCategoryCSVProcessor.class);

	/**
	 *
	 */
	public EnergizerProductCategoryCSVProcessor()
	{
		super();
	}

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)

	{
		/* Started EMEA Code refactor for get value from Model instead of properties */
		CatalogVersionModel catalogModelVersionModel;
		try
		{
			catalogModelVersionModel = this.getCatalogVersion(cronjob);
		}
		catch (final Exception e)
		{
			LOG.info("Invalid Catalog Version " + e.getMessage());
			return null;
		}
		/* End EMEA Code refactor for get value from Model instead of properties */
		long succeedRecord = getRecordSucceeded();
		CSV_HEADERS = Config.getParameter(MATERIAL_CATEGORY_FEED_HEADERS_KEY).split(new Character(DELIMETER).toString());
		for (final CSVRecord record : records)
		{

			//super.technicalFeedErrors = new ArrayList<EnergizerCSVFeedError>();
			//super.businessFeedErrors = new ArrayList<EnergizerCSVFeedError>();

			final Map<String, String> csvValuesMap = record.toMap();
			LOG.info("Processing record " + record);
			validate(record);
			if (!getTechnicalFeedErrors().isEmpty())
			{
				csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
				getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
				getTechnicalFeedErrors().clear();
				continue;
			}
			EnergizerProductModel pm = null;
			try
			{
				pm = (EnergizerProductModel) productService.getProductForCode(catalogModelVersionModel,
						(csvValuesMap).get(CSV_HEADERS[0]));
			}
			catch (final UnknownIdentifierException ep)
			{
				long recordFailed = getRecordFailed();
				LOG.error("No such Product", ep);
				final EnergizerCSVFeedError error = new EnergizerCSVFeedError();
				error.setLineNumber(record.getRecordNumber());
				error.setMessage("No such Product : " + csvValuesMap.get(CSV_HEADERS[0]));
				final List columnNmaeList = new ArrayList();
				columnNmaeList.add(csvValuesMap.get(CSV_HEADERS[0]));
				error.setColumnName(columnNmaeList);
				getTechnicalFeedErrors().add(error);
				setTechRecordError(getTechnicalFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);

				getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
				getTechnicalFeedErrors().clear();
			}
			if (pm != null)
			{
				final StringBuffer concatCode = new StringBuffer();


				pm.setSegmentCode(csvValuesMap.get(CSV_HEADERS[1]));
				pm.setSegmentName(csvValuesMap.get(CSV_HEADERS[2]));
				concatCode.append(csvValuesMap.get(CSV_HEADERS[1]));

				pm.setFamilyCode(csvValuesMap.get(CSV_HEADERS[3]));
				pm.setFamilyName(csvValuesMap.get(CSV_HEADERS[4]));
				concatCode.append(csvValuesMap.get(CSV_HEADERS[3]));

				pm.setGroupCode(csvValuesMap.get(CSV_HEADERS[5]));
				pm.setGroupName(csvValuesMap.get(CSV_HEADERS[6]));
				concatCode.append(csvValuesMap.get(CSV_HEADERS[5]));

				pm.setSubGroupCode(csvValuesMap.get(CSV_HEADERS[7]));
				pm.setSubGroupName(csvValuesMap.get(CSV_HEADERS[8]));
				concatCode.append(csvValuesMap.get(CSV_HEADERS[7]));


				if (!concatCode.toString().trim().isEmpty())
				{
					pm.setSapCategoryConcatValue(concatCode.toString());
					modelService.save(pm);
				}

				LOG.info("EnergizerProduct " + csvValuesMap.get(CSV_HEADERS[0]) + " category information is saved");

			}
			succeedRecord++;
			setRecordSucceeded(succeedRecord);
		}
		return getCsvFeedErrorRecords();
	}

	/**
	 * @param record
	 * @param csvValuesMap
	 * @return
	 */
	private EnergizerCSVFeedError validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		setRecordFailed(getRecordFailed());
		//if (!hasMandatoryFields(record, getHeadersForFeed(MATERIAL_CATEGORY_FEED_HEADERS_MANDATORY_KEY)))
		//{
		final List<String> mandatoryFields = Arrays
				.asList(Config.getParameter(MATERIAL_CATEGORY_FEED_HEADERS_MANDATORY_KEY).split(new Character(DELIMETER).toString()));
		final Map<String, String> map = record.toMap();
		Integer columnNumber = 0;
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
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error.setLineNumber(record.getRecordNumber());
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

		//}
		return error;
	}


}
