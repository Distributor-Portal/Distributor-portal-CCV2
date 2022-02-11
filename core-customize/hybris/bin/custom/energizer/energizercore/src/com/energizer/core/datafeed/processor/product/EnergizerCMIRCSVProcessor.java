/**
 *
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.data.CMIRCSVData;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;


/**
 *
 * This processors imports the CMIR
 *
 * Sample file will look like
 *
 * EnergizerAccountID,ERPMaterialID,CustomerMaterialID,Language,CustomerMaterial Description,MaterialList
 * price,CustomerListPrice,CustomerListprice currency,ShipmentPointNumber 1000, 10, 10, EN, tanning creme spf2 6 oz
 * 4/3s,21, 12, USD, 712
 *
 * Total column count : 9
 */
public class EnergizerCMIRCSVProcessor extends AbstractEnergizerCSVProcessor
{
	@Resource
	private ProductService productService;
	@Resource
	private ModelService modelService;
	@Resource
	private B2BCommerceUnitService b2bCommerceUnitService;
	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Resource
	private UnitService unitService;
	@Resource
	private CommonI18NService defaultCommonI18NService;
	@Resource
	private UnitService defaultUnitService;
	@Resource
	ConfigurationService configurationService;

	@Resource
	EnergizerProductService energizerProductService;

	boolean hasCustomerListPriceBusinessError = false;
	boolean hasCustomerListPriceTechnicalError = false;
	boolean hasCustomerBusinessError = false;
	boolean hasCustomerTechnicalError = false;

	private static final Logger LOG = Logger.getLogger(EnergizerCMIRCSVProcessor.class.getName());

	private static final String UNIT = "EA";

	private String defaultMOQ = "";

	private String defaultUOM = "";

	private final String ZERO = "0";

	private static final String PERSONALCAREEMEA_PRODUCTCATALOG = "personalCareEMEAProductCatalog";
	private static final String PERSONALCARE_PRODUCTCATALOG = "personalCareProductCatalog";

	@SuppressWarnings("unchecked")
	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records, final String catalogName,
			final EnergizerCronJobModel cronjob)
	{
		final Collection<PriceRowModel> priceRows;
		EnergizerProductModel energizerProduct = null;

		String siteId = StringUtils.EMPTY;
		final String SITE_PERSONALCARE = configurationService.getConfiguration().getString("site.personalCare");
		final String SITE_PERSONALCAREEMEA = configurationService.getConfiguration().getString("site.personalCareEMEA");

		defaultMOQ = configurationService.getConfiguration().getString("feedprocessor.defalult.moq.value", null);
		defaultUOM = configurationService.getConfiguration().getString("feedprocessor.defalult.uom.value", null);
		try
		{
			if (cronjob.getCatalogName().equalsIgnoreCase(PERSONALCAREEMEA_PRODUCTCATALOG))
			{
				defaultUOM = configurationService.getConfiguration().getString("feedprocessor.defalult.uom.value.EMEA", null);
				siteId = SITE_PERSONALCAREEMEA;
			}
			else if (cronjob.getCatalogName().equalsIgnoreCase(PERSONALCARE_PRODUCTCATALOG))
			{
				defaultUOM = configurationService.getConfiguration().getString("feedprocessor.defalult.uom.value", null);
				siteId = SITE_PERSONALCARE;
			}

			//final String catalogName = this.getCronjob().getCatalogName();
			final CatalogVersionModel catalogVersion = this.getCatalogVersion(cronjob);
			//final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(CATALOG_NAME, VERSION);
			long succeedRecord = getRecordSucceeded();
			final Set<CMIRCSVData> cmirCSVDataSet = new HashSet<CMIRCSVData>();
			final Set<String> productsSet = new HashSet<String>();
			CMIRCSVData cmirCSVData = null;

			//LOG.info("Total records in the CSV file : " + IterableUtils.size(records));

			final Long csvDataSetPrepStartTime = System.currentTimeMillis();
			for (final CSVRecord record : records)
			{
				//LOG.info(" CSV Record number: " + record.getRecordNumber() + ", CSV Record: " + record.toMap());

				final Map<String, String> csvValuesMap = record.toMap();
				cmirCSVData = new CMIRCSVData();

				//if any field empty --- don't process record
				//if cmir price empty --- trigger email, chk if list price is also empty....if empty --- trigger email, don't process record if both empty
				final EnergizerCSVFeedError error = new EnergizerCSVFeedError();

				if (validate(record))
				{
					addErrors();
					continue;
				}
				if (validateCMIRPrice(record, error) || validateListPrice(record, error))
				{
					if (error != null)
					{
						getTechnicalFeedErrors().add(error);
						setTechRecordError(getTechnicalFeedErrors().size());
						long recordFailed = getRecordFailed();
						recordFailed++;
						setRecordFailed(recordFailed);
						addErrors();
						continue;
					}
				}
				boolean dataExists = false;
				if (null != cmirCSVDataSet && !cmirCSVDataSet.isEmpty())
				{
					for (final CMIRCSVData csvData : cmirCSVDataSet)
					{
						if (null != csvData.getErpMaterialID()
								&& csvData.getErpMaterialID().equalsIgnoreCase(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID))
								&& null != csvData.getEnergizerAccountID() && csvData.getEnergizerAccountID()
										.equalsIgnoreCase(csvValuesMap.get(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID)))
						{
							dataExists = true;
							break;
						}
					}
				}
				// Adding new object to the Set, ignore the existing object
				if (!dataExists)
				{
					// Read from CSV File and add to the Set to remove duplicates.
					cmirCSVData.setErpMaterialID(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID));
					cmirCSVData.setCustomerMaterialID(csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_MATERIAL_ID));
					cmirCSVData.setWesellDefaultUOM(csvValuesMap.get(EnergizerCoreConstants.WESELL_DEFAULT_UOM));
					cmirCSVData.setLanguage(csvValuesMap.get(EnergizerCoreConstants.LANGUAGE));
					cmirCSVData.setCustomerMaterialDescription(csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_MATERIAL_DESCRIPTION));
					cmirCSVData.setMaterialListprice(csvValuesMap.get(EnergizerCoreConstants.MATERIAL_LIST_PRICE));
					cmirCSVData.setMaterialListpricecurrency(csvValuesMap.get(EnergizerCoreConstants.MATERIAL_LIST_PRICE_CURRENCY));
					cmirCSVData.setCustomerListPrice(csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_LIST_PRICE));
					cmirCSVData.setCustomerListpricecurrency(csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_LIST_PRICE_CURRENCY));
					cmirCSVData.setShipmentPointNumber(csvValuesMap.get(EnergizerCoreConstants.SHIPMENT_POINT_NO));
					cmirCSVData.setEnergizerAccountID(csvValuesMap.get(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID));
					cmirCSVData.setListPriceUOM(csvValuesMap.get(EnergizerCoreConstants.LIST_PRICE_UOM));
					cmirCSVData.setCustPriceUOM(csvValuesMap.get(EnergizerCoreConstants.CUST_PRICE_UOM));
					cmirCSVData.setMinimumOrderingQuantity(csvValuesMap.get(EnergizerCoreConstants.MINIMUM_ORDERING_QUANTITY));

					// Add data object to the Set
					cmirCSVDataSet.add(cmirCSVData);
					// Add unique product codes to the set
					productsSet.add(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID));
				}

				// If the cronjob abort is requested, then return the csv feed errors captured so far
				this.modelService.refresh(cronjob);
				if (null != cronjob.getRequestAbort() && BooleanUtils.isTrue(cronjob.getRequestAbort()))
				{
					LOG.info("CMIR abort request before processing data : " + cronjob.getRequestAbort());
					LOG.info(cronjob.getRegion() + " : CMIR cronjob is ABORTED mid-way while processing...");
					return getCsvFeedErrorRecords();
				}
			} // for
			  // to end the loop here // }

			// ################# START 1
			List<EnergizerProductModel> productModelsList = new ArrayList<EnergizerProductModel>();
			final Map<String, EnergizerProductModel> productModelsMap = new HashMap<String, EnergizerProductModel>();
			if (!this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.EMEA))
			{
				// Fetch all the product in the list irrespective of it being obsolete & non-obsolete
				productModelsList = energizerProductService.getProductsModelsForListAndSalesOrg(catalogVersion, productsSet,
						getSalesOrg());
			}
			else
			{
				// Fetch all the product in the list irrespective of it being obsolete & non-obsolete
				productModelsList = energizerProductService.getNonObsoleteProductsModelsForList(catalogVersion, productsSet);
			}

			for (final EnergizerProductModel productModel : productModelsList)
			{
				productModelsMap.put(productModel.getCode(), productModel);
			}
			// ################# END 1

			final Long csvDataSetPrepEndTime = System.currentTimeMillis();
			LOG.info("CMIR CSV Data Set size / Total unique CMIR records in the CMIR file '" + this.getFileName() + "' is :: "
					+ cmirCSVDataSet.size());
			LOG.info("Total unique products : " + productsSet.size());
			//LOG.info("Total duplicates removed : " + (IterableUtils.size(records) - cmirCSVDataSet.size()));
			LOG.info("Time taken for preparing CSVDataSet : " + (csvDataSetPrepEndTime - csvDataSetPrepStartTime) + " milliseconds, "
					+ (csvDataSetPrepEndTime - csvDataSetPrepStartTime) / 1000 + " seconds ...");
			LOG.info("PROCESSING CMIR records ... ");
			final Long csvDataSetProcessStartTime = System.currentTimeMillis();

			int recordsFailedFromSet = 0;
			int processedRecordsSoFar = 0;
			// Iterate only the unique records from the CMIR CSV Data Set to avoid multiple time saving to model service
			for (final CMIRCSVData cmirData : cmirCSVDataSet)
			{
				try
				{
					if (null != cmirData.getErpMaterialID() && productModelsMap.containsKey(cmirData.getErpMaterialID()))
					{
						energizerProduct = productModelsMap.get(cmirData.getErpMaterialID());
						if (null == energizerProduct)
						{
							//TO DO log into EnergizerCSVFeedError...so that it can be mailed
							LOG.info(
									"EnergizerProduct does not exist in the productModelsMap, it could either be obsolete or inactive or record is invalid ...");
							continue;
						}
						else
						{
							try
							{
								final String b2bUnitId = cmirData.getEnergizerAccountID();
								final String erpMaterialId = cmirData.getErpMaterialID();
								String currency = cmirData.getCustomerListpricecurrency();
								String customerlistprice = cmirData.getCustomerListPrice();
								//final String listPriceUOM = csvValuesMap.get(EnergizerCoreConstants.LIST_PRICE_UOM);
								String custPriceUOM = ((null != cmirData.getCustPriceUOM() && !cmirData.getCustPriceUOM().isEmpty())
										? cmirData.getCustPriceUOM().toString()
										: null);

								// Setting UoM
								if (custPriceUOM == null || custPriceUOM.isEmpty())
								{
									if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL))
									{
										custPriceUOM = (null != cmirData.getWesellDefaultUOM() ? cmirData.getWesellDefaultUOM()
												: EnergizerCoreConstants.EA);
									}
									else if (this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.EMEA))
									{
										custPriceUOM = EnergizerCoreConstants.CU;
									}
									else
									{
										custPriceUOM = EnergizerCoreConstants.EA;
									}
								}
								//check if b2bunit exists
								final EnergizerB2BUnitModel energizerB2BUnitModel = getEnergizerB2BUnit(b2bUnitId);

								final List<EnergizerCMIRModel> energizerCmirModels = energizerProduct.getProductCMIR();
								final ArrayList<EnergizerCMIRModel> tmpCMIRModelList = new ArrayList<EnergizerCMIRModel>();

								//If there is no associated EnergizerCMIRModel then create and attach with the product
								EnergizerCMIRModel energizerCMIRModel = null;

								boolean matchingRecordFound = false;
								if (energizerCmirModels != null)
								{
									tmpCMIRModelList.addAll(energizerCmirModels);
									LOG.debug("The size of productCMIRModels is :" + tmpCMIRModelList.size());
									//Retrieve the CMIRModel and perform the matching process and do an update in case of any mismatch, create a new CMIR record if it doesn't match

									for (final EnergizerCMIRModel energizerProductCMIRModel : energizerCmirModels)
									{
										LOG.debug("CMIR Model Material ID is:" + energizerProductCMIRModel.getErpMaterialId());

										if (isCMIRModelSame(energizerProductCMIRModel, cmirData))
										{
											matchingRecordFound = true;
											energizerCMIRModel = energizerProductCMIRModel;
											energizerCMIRModel.setSiteId(siteId);
											energizerCMIRModel.setCustPriceUOM(custPriceUOM);
											break;
										}
									}
								}
								if (!matchingRecordFound)
								{
									energizerCMIRModel = modelService.create(EnergizerCMIRModel.class);
									energizerCMIRModel.setErpMaterialId(erpMaterialId);
									energizerCMIRModel.setB2bUnit(energizerB2BUnitModel);
									energizerCMIRModel.setSiteId(siteId);
									energizerCMIRModel.setCustPriceUOM(custPriceUOM);
									tmpCMIRModelList.add(energizerCMIRModel);
								}
								this.addUpdateCMIRRecord(energizerCMIRModel, cmirData, siteId);
								energizerProduct.setProductCMIR(tmpCMIRModelList);
								try
								{
									modelService.saveAll();
								}
								catch (final Exception e)
								{
									recordsFailedFromSet += 1;
									LOG.info("Error occured while saving Product CMIR model for ERP_MATERIAL_ID : "
											+ cmirData.getErpMaterialID() + ", ENERGIZER_ACCOUNT_ID : " + cmirData.getEnergizerAccountID()
											+ "cause : " + e.getMessage() + ", at line number :: " + e.getStackTrace()[0].getLineNumber());

									LOG.error(e.getMessage());
									continue;
								}

								//priceRows = energizerProduct.getEurope1Prices();
								final List<PriceRowModel> energizerPriceRowModels = new ArrayList<PriceRowModel>(
										energizerProduct.getEurope1Prices());

								final ArrayList<PriceRowModel> tmpPriceRowModelList = new ArrayList<PriceRowModel>();

								//If there is no associated EnergizerPriceRowModel then create and attach with the product
								EnergizerPriceRowModel priceRowModel = null;

								boolean matchingPriceRowFound = false;
								if (energizerPriceRowModels != null)
								{
									tmpPriceRowModelList.addAll(energizerPriceRowModels);
									LOG.debug("The size of product price row models is :" + tmpPriceRowModelList.size());
									//Retrieve the PriceRowModel and perform the matching process and do an update in case of any mismatch
									for (final PriceRowModel enrPriceRowModel : energizerPriceRowModels)
									{
										//LOG.debug("Product price product :" + enrPriceRowModel.getPrice());
										if (!(enrPriceRowModel instanceof EnergizerPriceRowModel))
										{
											LOG.debug("Not an energizer price row");
											continue;
										}
										final EnergizerPriceRowModel enrPriceRow = (EnergizerPriceRowModel) enrPriceRowModel;

										if (isENRPriceRowModelSame(enrPriceRow, cmirData, energizerProduct))
										{
											LOG.debug("matchingPriceRowFound...");
											matchingPriceRowFound = true;
											priceRowModel = enrPriceRow;
											break;
										}
									}
								}

								if (!matchingPriceRowFound)
								{
									LOG.debug("matchingPriceRow NOT Found ...");
									priceRowModel = modelService.create(EnergizerPriceRowModel.class);
									priceRowModel.setB2bUnit(energizerB2BUnitModel);
									priceRowModel.setUnit(defaultUnitService.getUnitForCode(UNIT));
									priceRowModel.setProduct(energizerProduct);
									priceRowModel.setCatalogVersion(catalogVersion);
									priceRowModel.setPriceUOM(custPriceUOM);
									tmpPriceRowModelList.add(priceRowModel);
								}
								if (customerlistprice == null || customerlistprice.isEmpty())
								{
									customerlistprice = ZERO;
								}
								if (currency == null || currency.isEmpty())
								{
									currency = energizerB2BUnitModel.getCurrencyPreference().getIsocode();
								}
								if (!(this.getCronjob().getRegion().equalsIgnoreCase(EnergizerCoreConstants.WESELL))
										&& priceRowModel != null && priceRowModel.getB2bUnit().getCurrencyPreference().getIsocode() != null
										&& !priceRowModel.getB2bUnit().getCurrencyPreference().getIsocode().equals(currency))
								{
									LOG.error("Energizer price row currency preference  "
											+ priceRowModel.getB2bUnit().getCurrencyPreference().getIsocode() + " does not match with : "
											+ currency);
								}
								else
								{
									priceRowModel.setCurrency(energizerB2BUnitModel.getCurrencyPreference());
									priceRowModel.setPrice(Double.parseDouble(customerlistprice));
									priceRowModel.setPriceUOM(custPriceUOM);
									modelService.save(priceRowModel);
									//priceRowModel.setUnit(defaultUnitService.getUnitForCode(UNIT));
									energizerProduct.setEurope1Prices(tmpPriceRowModelList);
									modelService.save(energizerProduct);
								} //else
								succeedRecord++;
								setRecordSucceeded(succeedRecord);
								/*-LOG.info("Processed CMIR for ERP_MATERIAL_ID : " + cmirData.getErpMaterialID() + ", ENERGIZER_ACCOUNT_ID : "
										+ cmirData.getEnergizerAccountID());*/
							}
							catch (final Exception e)
							{
								LOG.info("Error while processing CMIR at line number :: " + e.getStackTrace()[0].getLineNumber()
										+ " , cause:: " + e.getMessage() + ", for erpMaterialID : " + cmirData.getErpMaterialID()
										+ " , energizerAccountID : " + cmirData.getEnergizerAccountID());
								continue;
							}

							// If the cronjob abort is requested, then return the csv feed errors captured so far
							this.modelService.refresh(cronjob);
							if (null != cronjob.getRequestAbort() && BooleanUtils.isTrue(cronjob.getRequestAbort()))
							{
								LOG.info("CMIR abort request while processing data : " + cronjob.getRequestAbort());
								LOG.info(cronjob.getRegion() + " : CMIR cronjob is ABORTED mid-way while processing...");
								LOG.info("Total number of records succeeded in this file '" + this.getFileName() + " so far :: "
										+ succeedRecord);
								LOG.info("Total number of records failed while saving CMIR model so far :: " + recordsFailedFromSet);

								return getCsvFeedErrorRecords();
							}

							processedRecordsSoFar = processedRecordsSoFar + 1;
							if (processedRecordsSoFar % 3000 == 0)
							{
								LOG.info("Processed '" + processedRecordsSoFar + "' CMIR records so far, time taken : "
										+ (System.currentTimeMillis() - csvDataSetProcessStartTime) / 1000
										+ " seconds, still processing !!!");
							}
						}
					}
					else
					{
						LOG.info("The product '" + cmirData.getErpMaterialID()
								+ "' is either null/not available in the product models map ...");
					}
				}
				catch (final Exception e)
				{
					LOG.error("Error while processing material : '" + cmirData.getErpMaterialID() + "', b2bUnitId : '"
							+ cmirData.getEnergizerAccountID() + "'... Message : " + e.getMessage());
					continue;
				}
			} //for

			final Long csvDataSetProcessEndTime = System.currentTimeMillis();
			LOG.info("PROCESSING COMPLETED !!! ");

			LOG.info("Time taken for processing CSVDataSet : " + (csvDataSetProcessEndTime - csvDataSetProcessStartTime)
					+ " milliseconds, " + (csvDataSetProcessEndTime - csvDataSetProcessStartTime) / 1000 + " seconds ...");

			LOG.info("TOTAL RECORDS SUCCEEDED for file '" + this.getFileName() + " :: " + succeedRecord);
			LOG.info("TOTAL RECORDS FAILED : " + recordsFailedFromSet);
		} //try
		catch (final Exception e)
		{
			LOG.error("Error in adding or updating  Energizer Product/CMIR Model :  " + e.getMessage() + ", at line number :: "
					+ e.getStackTrace()[0].getLineNumber());
			//e.printStackTrace();
		}
		getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
		getBusinessFeedErrors().clear();

		return getCsvFeedErrorRecords();
	}//process

	private boolean isCMIRModelSame(final EnergizerCMIRModel cmirModel, final CMIRCSVData cmirData)
	{
		if (null != cmirModel && cmirModel.getErpMaterialId().equals(cmirData.getErpMaterialID()) && null != cmirModel.getB2bUnit()
				&& cmirModel.getB2bUnit().getUid().equals(cmirData.getEnergizerAccountID()))
		{
			// record exists, just update other attributes
			//check if record is active
			if (!cmirModel.getIsActive())
			{
				return false;
			}
			LOG.debug("Product CMIR record exists for ERP_ID : " + cmirModel.getErpMaterialId() + ", Customer Material ID : "
					+ cmirModel.getCustomerMaterialId() + " B2B Unit ID : " + cmirModel.getB2bUnit().getUid());
			return true;
		}
		return false;
	}

	private void addUpdateCMIRRecord(final EnergizerCMIRModel energizerCMIRModel, final CMIRCSVData cmirData, final String siteId)
			throws Exception
	{
		energizerCMIRModel.setCustomerMaterialId(cmirData.getCustomerMaterialID());
		if (cmirData.getCustomerMaterialDescription() != null && !(cmirData.getCustomerMaterialDescription().isEmpty()))//if cust mat desc is in the feed then update that with the existing or new model
		{
			energizerCMIRModel.setCustomerMaterialDescription(cmirData.getCustomerMaterialDescription(),
					new Locale(cmirData.getLanguage().toLowerCase()));
			LOG.debug("THE CUST-MAT-DESCRIPTION IS not empty for" + cmirData.getCustomerMaterialID());
		}
		else
		//if cust mat desc not in the feed , Then empty cust-mat description is updated as empty space to avoid null in the existing model or new model.
		{
			energizerCMIRModel.setCustomerMaterialDescription(" ", new Locale(cmirData.getLanguage().toLowerCase()));

			LOG.debug("THE CUST-MAT-DESCRIPTION IS empty for" + cmirData.getCustomerMaterialID());
		}

		energizerCMIRModel.setShippingPoint(cmirData.getShipmentPointNumber());
		// Setting Default UOM  and  MOQ

		final String currentCMIRUom = energizerCMIRModel.getUom();
		final Integer currentCMIRMoq = energizerCMIRModel.getOrderingUnit();
		final String SITE_PERSONALCAREEMEA = configurationService.getConfiguration().getString("site.personalCareEMEA");
		final String salesArea = energizerCMIRModel.getB2bUnit().getSalesArea();

		if (siteId.equalsIgnoreCase(SITE_PERSONALCAREEMEA))
		{
			final Integer currentCSVCMIRMoq = Integer.parseInt(cmirData.getMinimumOrderingQuantity());

			if (null == currentCMIRUom || currentCMIRUom.isEmpty() || null == currentCMIRMoq || null == currentCSVCMIRMoq)
			{
				energizerCMIRModel.setUom(defaultUOM);
				energizerCMIRModel.setOrderingUnit(Integer.parseInt(defaultMOQ));
			}
			if (null != currentCSVCMIRMoq && (currentCMIRMoq != currentCSVCMIRMoq))
			{
				energizerCMIRModel.setOrderingUnit(currentCSVCMIRMoq);
			}
		}
		else
		{
			if (null == currentCMIRUom || currentCMIRUom.isEmpty() || null == currentCMIRMoq)
			{
				energizerCMIRModel.setUom(defaultUOM);
				energizerCMIRModel.setOrderingUnit(Integer.parseInt(defaultMOQ));
			}
			// WESELL Implementation ONLY for LATAM, NOT for EMEA - START
			// Check if it is a WeSell (or) Non-WeSell product
			//if (null != this.getCronjob().getPath() && this.getCronjob().getPath().contains(EnergizerCoreConstants.WESELL))
			if (null != salesArea && !StringUtils.isEmpty(salesArea) && salesArea.contains(EnergizerCoreConstants.WESELL))
			{
				// Setting 'true' if the product CMIRs are loaded from WESELL folder
				energizerCMIRModel.setIsWeSellProduct(true);
				// Setting the default UOM for WeSell from the feed file. This CSV header is ONLY for WESELL CMIR feed file(s).
				if (null != cmirData.getWesellDefaultUOM())
				{
					energizerCMIRModel.setUom(!StringUtils.isEmpty(cmirData.getWesellDefaultUOM())
							? cmirData.getWesellDefaultUOM().toString().toUpperCase()
							: StringUtils.EMPTY);
				}
			}
			else
			{
				// Setting 'false' as default if the product CMIRs are loaded from any folder apart from WESELL
				energizerCMIRModel.setIsWeSellProduct(false);
			}
			// WESELL Implementation ONLY for LATAM, NOT for EMEA - END
		}

		// Setting the ownership of CMIRs whether it belongs to EMEA/LATAM/WESELL
		energizerCMIRModel.setCmirOwner(energizerCMIRModel.getB2bUnit().getSalesArea());
	}

	private boolean isENRPriceRowModelSame(final EnergizerPriceRowModel enrPriceRow, final CMIRCSVData cmirData,
			final EnergizerProductModel energizerProduct)
	{
		if (enrPriceRow != null && enrPriceRow.getB2bUnit().getUid().equals(cmirData.getEnergizerAccountID()))
		{
			if (!enrPriceRow.getIsActive())
			{
				return false;
			}
			LOG.debug(" isENRPriceRowModelSame()... SAME PRICE ROW RECORD");
			return true;
		}
		return false;
	}

	/**
	 * @param record
	 */
	private boolean validate(final CSVRecord record)
	{
		boolean isEmptyRecord = false;
		Integer columnNumber = 0;
		EnergizerCSVFeedError error = null;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader);
			//			CMIRPartnerID, , , MaterialList price,
			/*
			 * if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.ERPMATERIAL_ID) ||
			 * columnHeader.equalsIgnoreCase(EnergizerCoreConstants.CUSTOMER_MATERIAL_ID) ||
			 * columnHeader.equalsIgnoreCase(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID) ||
			 * columnHeader.equalsIgnoreCase(EnergizerCoreConstants.SHIPMENT_POINT_NO) ||
			 * columnHeader.equalsIgnoreCase(EnergizerCoreConstants.LANGUAGE) ||
			 * columnHeader.equalsIgnoreCase(EnergizerCoreConstants.CUSTOMER_MATERIAL_DESCRIPTION))
			 */
			//in the above check, customer material description is made non mandatory field.

			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.ERPMATERIAL_ID)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.CUSTOMER_MATERIAL_ID)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.SHIPMENT_POINT_NO)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.LANGUAGE))

			{
				if (value.isEmpty())
				{
					long recordFailed = getRecordFailed();
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setUserType(TECHNICAL_USER);
					error.setLineNumber(record.getRecordNumber() + 1);
					columnNames.add(columnHeader);
					error.setColumnName(columnNames);
					error.setErrorCode("CMIR1001");
					error.setMessage(columnHeader + " column should not be empty");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					getTechnicalFeedErrors().add(error);
					setTechRecordError(getTechnicalFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
					isEmptyRecord = true;
				}
			}
			/*
			 * if (!value.isEmpty() && columnHeader.equalsIgnoreCase(EnergizerCoreConstants.CUSTOMER_LIST_PRICE)) { if
			 * (!NumberUtils.isNumber(value) || Double.valueOf(value) <= 0.0) { long recordFailed = getRecordFailed();
			 * final List<String> columnNames = new ArrayList<String>(); final List<Integer> columnNumbers = new
			 * ArrayList<Integer>(); error = new EnergizerCSVFeedError(); error.setUserType(TECHNICAL_USER);
			 * error.setErrorCode("CMIR2001"); error.setLineNumber(record.getRecordNumber());
			 * columnNames.add(columnHeader); error.setColumnName(columnNames); error.setMessage(columnHeader +
			 * " column should be numeric and greater than 0"); columnNumbers.add(columnNumber);
			 * error.setColumnNumber(columnNumbers); getTechnicalFeedErrors().add(error);
			 * setTechRecordError(getTechnicalFeedErrors().size()); recordFailed++; setRecordFailed(recordFailed); } }
			 */
		}
		return isEmptyRecord;
	}

	private boolean validateListPrice(final CSVRecord record, final EnergizerCSVFeedError error)
	{
		boolean isEmptyListPrice = false;
		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader);
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.MATERIAL_LIST_PRICE)
					&& (value == null || (!NumberUtils.isNumber(value) || Double.valueOf(value) <= 0.0)))
			{
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error.setUserType(TECHNICAL_USER);
				error.setLineNumber(record.getRecordNumber() + 1);
				columnNames.add(columnHeader);
				error.setColumnName(columnNames);
				error.setErrorCode("CMIR2001");
				error.setMessage(columnHeader + " column should be numeric and greater than 0");
				columnNumbers.add(columnNumber);
				error.setColumnNumber(columnNumbers);
				isEmptyListPrice = true;
				break;
			}
		}
		return isEmptyListPrice;
	}

	private boolean validateCMIRPrice(final CSVRecord record, final EnergizerCSVFeedError error)
	{
		boolean isEmptyCMIRPrice = false;
		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader);
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.CUSTOMER_LIST_PRICE)
					&& (value == null || (!NumberUtils.isNumber(value) || Double.valueOf(value) <= 0.0)))
			{
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error.setUserType(TECHNICAL_USER);
				error.setLineNumber(record.getRecordNumber() + 1);
				columnNames.add(columnHeader);
				error.setColumnName(columnNames);
				error.setErrorCode("CMIR2001");
				error.setMessage(columnHeader + " column should be numeric and greater than 0");
				columnNumbers.add(columnNumber);
				error.setColumnNumber(columnNumbers);
				isEmptyCMIRPrice = true;
				break;
			}
		}
		return isEmptyCMIRPrice;
	}

	/**
	 *
	 * @param b2bUnitId
	 * @return
	 */

	public EnergizerB2BUnitModel getEnergizerB2BUnit(final String b2bUnitId)
	{
		final EnergizerB2BUnitModel energizerB2BUnitModel = (EnergizerB2BUnitModel) b2bCommerceUnitService.getUnitForUid(b2bUnitId);
		return energizerB2BUnitModel;
	}

	private void addErrors()
	{
		csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
		getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
		getTechnicalFeedErrors().clear();
	}

	/**
	 * @return the hasCustomerListPriceBusinessError
	 */
	public boolean isHasCustomerListPriceBusinessError()
	{
		return hasCustomerListPriceBusinessError;
	}

	/**
	 * * @param hasCustomerListPriceBusinessError the hasCustomerListPriceBusinessError to set
	 */
	public void setHasCustomerListPriceBusinessError(final boolean hasCustomerListPriceBusinessError)
	{
		this.hasCustomerListPriceBusinessError = hasCustomerListPriceBusinessError;
	}

	/**
	 * @return the hasCustomerListPriceTechnicalError
	 */
	public boolean isHasCustomerListPriceTechnicalError()
	{
		return hasCustomerListPriceTechnicalError;
	}

	/**
	 * @param hasCustomerListPriceTechnicalError
	 *           the hasCustomerListPriceTechnicalError to set
	 */
	public void setHasCustomerListPriceTechnicalError(final boolean hasCustomerListPriceTechnicalError)
	{
		this.hasCustomerListPriceTechnicalError = hasCustomerListPriceTechnicalError;
	}

}
