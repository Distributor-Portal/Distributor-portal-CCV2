/**
 *
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.service.EnergizerCustomerLeadTimeService;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerShippingPointModel;


/**
 * @author Pavanip
 *
 */
public class EnergizerShipmentCSVProcessor extends AbstractEnergizerCSVProcessor
{
	private static final Logger LOG = Logger.getLogger(EnergizerShipmentCSVProcessor.class);

	@Resource
	private ModelService modelService;

	@Resource(name = "userService")
	private UserService userService;

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Resource(name = "defaultEnergizerCustomerLeadTimeService")
	private EnergizerCustomerLeadTimeService energizerLeadTimeService;

	@Resource
	private B2BCommerceUnitService b2bCommerceUnitService;

	private static final String SHIPPING_POINT_NUMBER = "ShipmentPointnumber";
	private static final String SHIPPING_POINT_NAME = "ShipmentPointName";
	private static final String HOUSE = "House";
	private static final String POSTAL_CODE = "PostalCode";
	private static final String CITY = "City";
	private static final String COUNTRY = "Country";
	private static final String REGION = "Region";



	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		EnergizerShippingPointModel shippingPointModel = null;
		long succeedRecord = getRecordSucceeded();
		final Logger LOG = Logger.getLogger(EnergizerShipmentCSVProcessor.class);
		LOG.info("EnergizerShipmentCSVProcessor:process:Start");
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
			LOG.info("shippingPointNumber" + csvValuesMap.get(SHIPPING_POINT_NUMBER));

			try
			{
				shippingPointModel = energizerLeadTimeService.fetchShippingPointId(csvValuesMap.get(SHIPPING_POINT_NUMBER).trim());

				if (null == shippingPointModel)
				{
					createShippingPoint(csvValuesMap, record);
				}
				else
				{
					final AddressModel addressModel = shippingPointModel.getShippingPointAddress();
					final UserModel userModel = new UserModel();
					try
					{
						final CountryModel countryModel = new CountryModel();
						countryModel.setIsocode(csvValuesMap.get(COUNTRY).trim());
						final CountryModel countryModelfound = flexibleSearchService.getModelByExample(countryModel);
						addressModel.setCountry(countryModelfound);

						final RegionModel regionModel = new RegionModel();
						regionModel.setIsocode(csvValuesMap.get(REGION).trim());
						regionModel.setCountry(countryModelfound);
						final RegionModel regionModelfound = flexibleSearchService.getModelByExample(regionModel);
						addressModel.setRegion(regionModelfound);
					}
					catch (final Exception e)
					{
						LOG.error("No Region or country Found : ", e);
					}

					userModel.setUid("admin");
					final UserModel userModelfound = flexibleSearchService.getModelByExample(userModel);

					addressModel.setOwner(userModelfound);
					addressModel.setPostalcode(csvValuesMap.get(POSTAL_CODE));
					addressModel.setTown(csvValuesMap.get(CITY));
					addressModel.setStreetname(csvValuesMap.get(HOUSE));
					modelService.save(addressModel);
					modelService.refresh(addressModel);

					shippingPointModel.setShippingPointAddress(addressModel);
					shippingPointModel.setShippingPointName(csvValuesMap.get(SHIPPING_POINT_NAME).trim());
					modelService.saveAll(shippingPointModel);

					LOG.info("Updated Shipping Point details for " + SHIPPING_POINT_NUMBER + " successfully");
				}

				succeedRecord++;
				setRecordSucceeded(succeedRecord);
			}
			catch (final Exception ex)
			{
				LOG.info("Updated Shipping Point details failed " + ex);
			}
		}
		getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
		getTechnicalFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}



	/**
	 * @param csvValuesMap
	 * @param record
	 * @param shippingPointModel
	 *                              Read shipping record from csv file and Verifying Region and country, if exist save
	 *                              shipping point and address
	 */
	private void createShippingPoint(final Map<String, String> csvValuesMap, final CSVRecord record)
	{
		try
		{
			final EnergizerShippingPointModel shippingPointModel = new EnergizerShippingPointModel();
			final AddressModel addressModel = new AddressModel();
			final UserModel userModel = new UserModel();

			try
			{
				final CountryModel countryModel = new CountryModel();
				countryModel.setIsocode(csvValuesMap.get(COUNTRY).trim());
				final CountryModel countryModelfound = flexibleSearchService.getModelByExample(countryModel);
				addressModel.setCountry(countryModelfound);

				final RegionModel regionModel = new RegionModel();
				regionModel.setIsocode(csvValuesMap.get(REGION).trim());
				regionModel.setCountry(countryModelfound);
				final RegionModel regionModelfound = flexibleSearchService.getModelByExample(regionModel);
				addressModel.setRegion(regionModelfound);
			}
			catch (final Exception e)
			{
				LOG.error("No Region or country Found", e);
			}

			userModel.setUid("admin");
			final UserModel userModelfound = flexibleSearchService.getModelByExample(userModel);

			addressModel.setOwner(userModelfound);
			addressModel.setPostalcode(csvValuesMap.get(POSTAL_CODE));
			addressModel.setTown(csvValuesMap.get(CITY));
			addressModel.setStreetname(csvValuesMap.get(HOUSE));
			modelService.save(addressModel);
			modelService.refresh(addressModel);

			shippingPointModel.setShippingPointAddress(addressModel);
			shippingPointModel.setShippingPointId(csvValuesMap.get(SHIPPING_POINT_NUMBER));
			shippingPointModel.setShippingPointName(csvValuesMap.get(SHIPPING_POINT_NAME));
			modelService.saveAll(shippingPointModel);
			LOG.info("Shipping details created for shipping point " + SHIPPING_POINT_NUMBER + " successfully");
		}
		catch (final Exception ex)
		{
			LOG.error(csvValuesMap.get(SHIPPING_POINT_NUMBER) + ": Error while saving shipping address.. " + ex);

		}

	}



	/**
	 * @param record
	 */
	private void validate(final CSVRecord record)
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
			if (columnHeader.equalsIgnoreCase(SHIPPING_POINT_NUMBER) || columnHeader.equalsIgnoreCase(SHIPPING_POINT_NAME)
					|| columnHeader.equalsIgnoreCase(POSTAL_CODE))
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
		}
	}

}






