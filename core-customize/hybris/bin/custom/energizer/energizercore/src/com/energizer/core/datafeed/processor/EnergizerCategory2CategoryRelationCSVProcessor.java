/**
 *
 */
package com.energizer.core.datafeed.processor;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerCategoryModel;
import com.energizer.core.model.EnergizerCronJobModel;


/**
 *
 * This processors imports the category to category.
 *
 * Sample file will look like
 *
 * CategoryCode, Category, SuperCategoryCode,SuperCategory, CategoryType,Language Feminine Care-Playtex, Playtex,
 * Feminine Care, Feminine Care, Brand, en Total column count : 6
 */
public class EnergizerCategory2CategoryRelationCSVProcessor extends AbstractEnergizerCSVProcessor
{


	@Resource
	private ModelService modelService;

	@Resource
	private SessionService sessionService;


	@Resource
	private CategoryService categoryService;

	@Resource
	CatalogService catalogService;

	@Resource
	CatalogVersionService catalogVersionService;

	@Resource
	UserService userService;

	private static String CSV_HEADERS[] = null;

	private static final String CATEGORY_CATEGORY_FEED_HEADERS_KEY = "feedprocessor.categorycategoryrelationfeed.headers";
	private static final String CATEGORY_CATEGORY_FEED_FEED_HEADERS_MANDATORY_KEY = "feedprocessor.categorycategoryrelationfeed.headers.mandatory";


	private static final Logger LOG = Logger.getLogger(EnergizerCategory2CategoryRelationCSVProcessor.class);

	/**
	 *
	 */
	public EnergizerCategory2CategoryRelationCSVProcessor()
	{
		super();
	}

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)

	{
		CatalogVersionModel catalogModelVersionModel = null;
		try
		{
			catalogModelVersionModel = this.getCatalogVersion(cronjob);
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage());
			return null;
		}
		long succeedRecord = getRecordSucceeded();
		CSV_HEADERS = Config.getParameter(CATEGORY_CATEGORY_FEED_HEADERS_KEY).split(new Character(DELIMETER).toString());
		for (final CSVRecord record : records)
		{
			final Map<String, String> csvValuesMap = record.toMap();
			validate(record);
			if (!getTechnicalFeedErrors().isEmpty())
			{
				csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
				continue;
			}

			if (null == csvValuesMap.get(CSV_HEADERS[0]) || csvValuesMap.get(CSV_HEADERS[0]).isEmpty()
					|| null == csvValuesMap.get(CSV_HEADERS[1]) || csvValuesMap.get(CSV_HEADERS[1]).isEmpty())
			{
				continue;
			}

			EnergizerCategoryModel childCategory = null;
			EnergizerCategoryModel parentCategory = null;
			try
			{
				childCategory = (EnergizerCategoryModel) categoryService.getCategoryForCode(catalogModelVersionModel,
						csvValuesMap.get(CSV_HEADERS[0]));
				childCategory.setName(csvValuesMap.get(CSV_HEADERS[1]), new Locale(csvValuesMap.get(CSV_HEADERS[5]).toLowerCase()));
				childCategory.setCategoryType(csvValuesMap.get(CSV_HEADERS[4]));
				modelService.save(childCategory);

			}
			catch (final UnknownIdentifierException ep)
			{
				childCategory = createCategory(csvValuesMap.get(CSV_HEADERS[0]), csvValuesMap.get(CSV_HEADERS[1]),
						catalogModelVersionModel, csvValuesMap.get(CSV_HEADERS[4]), csvValuesMap.get(CSV_HEADERS[5]));
			}
			if (childCategory == null)
			{
				childCategory = createCategory(csvValuesMap.get(CSV_HEADERS[0]), csvValuesMap.get(CSV_HEADERS[1]),
						catalogModelVersionModel, csvValuesMap.get(CSV_HEADERS[4]), csvValuesMap.get(CSV_HEADERS[5]));
			}

			if (csvValuesMap.get(CSV_HEADERS[2]).isEmpty() || csvValuesMap.get(CSV_HEADERS[3]).isEmpty())
			{
				continue;
			}

			if (!csvValuesMap.get(CSV_HEADERS[1]).equalsIgnoreCase("null"))
			{
				try
				{
					parentCategory = (EnergizerCategoryModel) categoryService.getCategoryForCode(catalogModelVersionModel,
							csvValuesMap.get(CSV_HEADERS[2]));
					parentCategory.setName(csvValuesMap.get(CSV_HEADERS[3]),
							new Locale(csvValuesMap.get(CSV_HEADERS[5]).toLowerCase()));
					modelService.save(parentCategory);
				}
				catch (final UnknownIdentifierException ep)
				{
					parentCategory = createCategory(csvValuesMap.get(CSV_HEADERS[2]), csvValuesMap.get(CSV_HEADERS[3]),
							catalogModelVersionModel, csvValuesMap.get(CSV_HEADERS[4]), csvValuesMap.get(CSV_HEADERS[5]));
				}
				if (parentCategory == null)
				{
					parentCategory = createCategory(csvValuesMap.get(CSV_HEADERS[2]), csvValuesMap.get(CSV_HEADERS[3]),
							catalogModelVersionModel, csvValuesMap.get(CSV_HEADERS[4]), csvValuesMap.get(CSV_HEADERS[5]));
				}
			}
			else
			{
				continue;
			}

			if (parentCategory != null)
			{
				final List<CategoryModel> existingCategories = new ArrayList<>();
				boolean alreadyExists = false;
				for (final CategoryModel category : childCategory.getSupercategories())
				{
					if (category.getName(new Locale(csvValuesMap.get(CSV_HEADERS[5]).toLowerCase()))
							.equals(parentCategory.getName(new Locale(csvValuesMap.get(CSV_HEADERS[5]).toLowerCase()))))
					{
						alreadyExists = true;
					}
					else
					{
						existingCategories.add(category);
					}
				}
				if (!alreadyExists)
				{
					existingCategories.add(parentCategory);
					childCategory.setSupercategories(existingCategories);
					modelService.save(childCategory);
				}

				//pm.setName(csvValuesMap.get(CSV_HEADERS[2]), new Locale(csvValuesMap.get(CSV_HEADERS[1]).toLowerCase()));

				LOG.info("CategoryModel is saved");

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
		if (!hasMandatoryFields(record, getHeadersForFeed(CATEGORY_CATEGORY_FEED_FEED_HEADERS_MANDATORY_KEY)))
		{
			final List<String> mandatoryFields = Arrays.asList(
					Config.getParameter(CATEGORY_CATEGORY_FEED_FEED_HEADERS_MANDATORY_KEY).split(new Character(DELIMETER).toString()));
			final Map<String, String> map = record.toMap();
			Integer columnNumber = 0;
			for (final String columnHeader : map.keySet())
			{
				setTotalRecords(record.getRecordNumber());
				if (mandatoryFields.contains(columnHeader))
				{
					columnNumber++;
					final String value = map.get(columnHeader);

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

		}
		return error;
	}

	private EnergizerCategoryModel createCategory(final String categoryCode, final String categoryName,
			final CatalogVersionModel catalogVersion, final String categoryType, final String language)
	{
		final EnergizerCategoryModel category = modelService.create(EnergizerCategoryModel.class);
		category.setCode(categoryCode);
		category.setCatalogVersion(catalogVersion);
		category.setSupercategories(new ArrayList<CategoryModel>());
		category.setName(categoryName, new Locale(language.toLowerCase()));
		category.setCategoryType(categoryType);
		category.setAllowedPrincipals(Arrays.asList(new PrincipalModel[]
		{ userService.getUserGroupForUID("customergroup") }));
		modelService.save(category);
		return category;
	}

}
