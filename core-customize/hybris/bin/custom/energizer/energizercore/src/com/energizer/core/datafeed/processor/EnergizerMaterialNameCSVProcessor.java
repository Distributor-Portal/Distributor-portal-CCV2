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
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerProductModel;


/**
 *
 * This processors imports the product material name.
 *
 * Sample file will look like
 *
 * ERPMaterialID,Language,ProductDesription 10, EN, omkar singh spf2 6 oz 4/3s
 *
 * Total column count : 3
 */
public class EnergizerMaterialNameCSVProcessor extends AbstractEnergizerCSVProcessor
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

	private static final String MATERIAL_NAME_FEED_HEADERS_KEY = "feedprocessor.materialnamefeed.headers";
	private static final String MATERIAL_NAME_FEED_HEADERS_MANDATORY_KEY = "feedprocessor.materialnamefeed.headers.mandatory";


	private static final Logger LOG = Logger.getLogger(EnergizerMaterialNameCSVProcessor.class);

	/**
	 *
	 */
	public EnergizerMaterialNameCSVProcessor()
	{
		super();
	}

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		/* Started implementation for EMEA : Product name will point to Region specific catalog */
		final CatalogVersionModel catalogModelVersionModel;
		try
		{
			catalogModelVersionModel = this.getCatalogVersion(cronjob);
		}
		catch (final Exception e1)
		{
			LOG.error("Invalid Catalog Version ", e1);
			return null;
		}
		/* End implementation for EMEA */
		long succeedRecord = getRecordSucceeded();
		CSV_HEADERS = Config.getParameter(MATERIAL_NAME_FEED_HEADERS_KEY).split(new Character(DELIMETER).toString());
		int descriptionSavedCount = 0;
		int descriptionNotSavedCount = 0;
		for (final CSVRecord record : records)
		{

			final Map<String, String> csvValuesMap = record.toMap();
			validate(record);
			EnergizerProductModel pm = null;
			try
			{
				pm = (EnergizerProductModel) productService.getProductForCode(catalogModelVersionModel,
						(csvValuesMap).get(CSV_HEADERS[0]));
			}
			catch (final UnknownIdentifierException ep)
			{
				LOG.error("No such Product", ep);
				final EnergizerCSVFeedError error = new EnergizerCSVFeedError();
				long recordFailed = getRecordFailed();
				error.setLineNumber(record.getRecordNumber());
				error.setMessage("No such Product : " + csvValuesMap.get(CSV_HEADERS[0]));
				getTechnicalFeedErrors().add(error);
				setTechRecordError(getTechnicalFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);

				//csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
			}
			if (pm != null)
			{
				if (null != csvValuesMap.get(CSV_HEADERS[2]) && StringUtils.isNotEmpty(csvValuesMap.get(CSV_HEADERS[2]).trim())
						&& null != csvValuesMap.get(CSV_HEADERS[1]))
				{
					try
					{
						pm.setName(csvValuesMap.get(CSV_HEADERS[2]), new Locale(csvValuesMap.get(CSV_HEADERS[1]).toLowerCase()));
						modelService.save(pm);
						//LOG.info("EnergizerProductModel is saved");
						descriptionSavedCount += 1;
					}
					catch (final Exception e)
					{
						LOG.error("Error ", e);
					}
				}
				else
				{
					descriptionNotSavedCount += 1;
				}
			}
			succeedRecord++;
			setRecordSucceeded(succeedRecord);
		}

		if (!getTechnicalFeedErrors().isEmpty())
		{
			csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
			//getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
			//getTechnicalFeedErrors().clear();
			//continue;
		}
		//getTechnicalFeedErrors().addAll(techFeedErrorRecords);
		//getBusinessFeedErrors().addAll(businessFeedErrorRecords);
		/*
		 * techFeedErrorRecords.clear(); businessFeedErrorRecords.clear(); return getCsvFeedErrorRecords();
		 */
		//getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
		LOG.info("Total records that doesn't have value for one or more fields : " + getTechnicalFeedErrors().size());
		LOG.info("Product description saved for '" + descriptionSavedCount + "' records ...");
		LOG.info("Product description NOT saved for '" + descriptionNotSavedCount + "' records ...");
		getTechnicalFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}



	/**
	 * @param record
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError techError = null;
		final EnergizerCSVFeedError busError = null;
		final Map<String, String> map = record.toMap();
		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			long recordFailed = getRecordFailed();
			setTotalRecords(record.getRecordNumber());
			final String value = map.get(columnHeader).trim();
			final List<String> mandatoryFields = Arrays
					.asList(Config.getParameter(MATERIAL_NAME_FEED_HEADERS_MANDATORY_KEY).split(new Character(DELIMETER).toString()));

			if (!hasMandatoryFields(record, getHeadersForFeed(MATERIAL_NAME_FEED_HEADERS_MANDATORY_KEY)))
			{

				if (mandatoryFields.contains(columnHeader))
				{

					if (value.isEmpty())
					{
						final List<String> columnNames = new ArrayList<String>();
						final List<Integer> columnNumbers = new ArrayList<Integer>();
						techError = new EnergizerCSVFeedError();
						techError.setLineNumber(record.getRecordNumber() + 1);
						columnNames.add(columnHeader);
						techError.setUserType(TECHNICAL_USER);
						techError.setColumnName(columnNames);
						columnNumbers.add(columnNumber);
						techError.setMessage(columnHeader + " column should not be empty");
						techError.setColumnNumber(columnNumbers);
						getTechnicalFeedErrors().add(techError);
						setTechRecordError(getTechnicalFeedErrors().size());
						recordFailed++;
						setRecordFailed(recordFailed);
					}
				}
			}
		}
	}
}




