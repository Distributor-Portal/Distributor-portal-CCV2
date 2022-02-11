/**
 *
 */
package com.energizer.facades.product.populators;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BCustomerService;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.product.PriceService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.MetricUnitModel;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author Bivash Pandit
 *
 */
public class EnergizerProductPopulator implements Populator<EnergizerProductModel, ProductData>
{
	private static final Logger LOG = Logger.getLogger(EnergizerProductPopulator.class.getName());

	@Resource
	B2BCommerceUserService b2bCommerceUserService;
	@Resource(name = "b2bCustomerFacade")
	protected CustomerFacade customerFacade;
	@Resource
	PriceService priceService;
	@Resource
	B2BCustomerService b2bCustomerService;
	@Resource
	EnergizerProductService energizerProductService;

	@Resource
	private UserService userService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	private EnergizerB2BUnitModel loggedInUserB2bUnit;

	private static int ZERO = 0;
	private static String EMPTY = "";
	private static final String PERSONALCARE_EMEA = "personalCareEMEA";


	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Override
	public void populate(final EnergizerProductModel energizerProductModel, final ProductData productData)
			throws ConversionException
	{
		try
		{
			int baseUOM = 1;

			int numberOfEachInCase = 0;
			int numberOfEachInLayer = 0;
			int numberOfEachInPallet = 0;
			int numberOfEachInCU = 0;
			int numberOfEachInSU = 0;
			int numberOfLayersPerPallet = 0;
			int numberOfCasesPerPallet = 0;
			int numberOfCasesPerLayer = 0;
			int numberOfCUsPerSU = 0;
			int numberOfSUsPerPallet = 0;

			final String currentUserId = customerFacade.getCurrentCustomer().getUid();
			if (currentUserId.equals("anonymous"))
			{
				loggedInUserB2bUnit = null;
			}
			else
			{
				loggedInUserB2bUnit = (EnergizerB2BUnitModel) b2bCommerceUserService.getParentUnitForCustomer(currentUserId);
			}
			//productData.setObsolete((energizerProductModel.getObsolete() == null ? false : energizerProductModel.getObsolete()));


			if (cmsSiteService.getCurrentSite().getUid().equalsIgnoreCase(EnergizerCoreConstants.SITE_PERSONALCARE))
			{
				final String salesOrgString = energizerProductModel.getNonObsoleteSalesOrgsString();
				if (null != salesOrgString && StringUtils.isNotEmpty(salesOrgString) && null != loggedInUserB2bUnit
						&& null != loggedInUserB2bUnit.getSalesOrganisation()
						&& StringUtils.isNotEmpty(loggedInUserB2bUnit.getSalesOrganisation())
						&& salesOrgString.contains(loggedInUserB2bUnit.getSalesOrganisation()))
				{
					productData.setObsolete(false);
				}
				else
				{
					productData.setObsolete(true);
				}
			}
			else
			{
				productData.setObsolete(energizerProductModel.getObsolete());
			}

			productData
					.setDescription(energizerProductModel.getDescription() == null ? EMPTY : energizerProductModel.getDescription());
			productData.setErpMaterialID(energizerProductModel.getCode() == null ? EMPTY : energizerProductModel.getCode());
			productData.setEan(energizerProductModel.getEan() == null ? EMPTY : energizerProductModel.getEan());
			//Set PBG/Non-PBG Product flag
			productData.setIsPBG(energizerProductModel.getIsPBG() == null ? false : energizerProductModel.getIsPBG());

			//Setting the segment,family , group
			productData
					.setSegmentName(energizerProductModel.getSegmentCode() == null ? EMPTY : energizerProductModel.getSegmentCode());
			productData.setFamilyName(energizerProductModel.getFamilyCode() == null ? EMPTY : energizerProductModel.getFamilyCode());
			productData.setGroupName(energizerProductModel.getGroupCode() == null ? EMPTY : energizerProductModel.getGroupCode());

			final String userId = userService.getCurrentUser().getUid();
			EnergizerCMIRModel energizerCMIRModel = null;
			final EnergizerProductConversionFactorModel energizerProductConversionFactorModel;
			if (loggedInUserB2bUnit != null)
			{
				energizerCMIRModel = energizerProductService.getEnergizerCMIR(energizerProductModel.getCode(),
						loggedInUserB2bUnit.getUid());
				energizerProductConversionFactorModel = energizerProductService
						.getEnergizerProductConversion(energizerProductModel.getCode(), loggedInUserB2bUnit.getUid());
				if (energizerCMIRModel != null)
				{
					productData.setCustomerMaterialId(
							energizerCMIRModel.getCustomerMaterialId() == null ? EMPTY : energizerCMIRModel.getCustomerMaterialId());
					productData.setIsActive(energizerCMIRModel.getIsActive() == null ? true : energizerCMIRModel.getIsActive());
				}
				else
				{
					productData.setCustomerMaterialId("");
				}
				String shippingPointId = "";
				String shippingPointName = "";
				if (energizerCMIRModel != null)
				{
					productData.setCustomerProductName(energizerCMIRModel.getCustomerMaterialDescription() == null ? EMPTY
							: energizerCMIRModel.getCustomerMaterialDescription());
					productData.setMoq(energizerCMIRModel.getOrderingUnit() == null ? ZERO : energizerCMIRModel.getOrderingUnit());
					productData.setUom(energizerCMIRModel.getUom() == null ? EMPTY : energizerCMIRModel.getUom());
					shippingPointId = energizerCMIRModel.getShippingPoint();
					shippingPointName = energizerProductService.getShippingPointName(shippingPointId);
					productData.setShippingPoint(shippingPointId);

					//Set WeSell/Non-WeSell Product flag
					productData.setIsWeSellProduct(
							energizerCMIRModel.getIsWeSellProduct() == null ? false : energizerCMIRModel.getIsWeSellProduct());
				}
				if (productData.isObsolete())
				{
					productData.setRemoveFromCart(true);
				}
				else if (energizerCMIRModel == null || (energizerCMIRModel != null && !productData.isIsActive()))
				{
					productData.setRemoveFromCart(true);
				}
				else
				{
					productData.setRemoveFromCart(false);
				}
				productData.setShippingPointName(shippingPointName == null ? EMPTY : shippingPointName);

				final String shippingPointLocation = energizerProductService
						.getShippingPointLocation(energizerCMIRModel.getShippingPoint());
				productData.setShippingPointLocation(shippingPointLocation);


				if (energizerProductConversionFactorModel != null)
				{
					final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM(); // EACH / CASE/ PALLET / LAYER

					if (null != alternateUOM && null != productData.getUom() && alternateUOM.equalsIgnoreCase(productData.getUom()))
					{
						baseUOM = (energizerProductConversionFactorModel.getConversionMultiplier()).intValue();

						productData.setBaseUOM(baseUOM);
						final MetricUnitModel volumeMetricUnit = energizerProductConversionFactorModel.getPackageVolume();
						if (null != volumeMetricUnit)
						{
							productData.setVolume(volumeMetricUnit.getMeasurement() == null ? ZERO : volumeMetricUnit.getMeasurement());
							productData.setVolumeUom(
									volumeMetricUnit.getMeasuringUnits() == null ? EMPTY : volumeMetricUnit.getMeasuringUnits());
						}
						final MetricUnitModel weightMetricUnit = energizerProductConversionFactorModel.getPackageWeight();
						if (null != weightMetricUnit)
						{
							productData.setWeight(weightMetricUnit.getMeasurement() == null ? ZERO : weightMetricUnit.getMeasurement());
							productData.setWeightUom(
									weightMetricUnit.getMeasuringUnits() == null ? EMPTY : weightMetricUnit.getMeasuringUnits());

						}
					}
				}
			}

			final Collection<PriceRowModel> rowPrices = energizerProductModel.getEurope1Prices();
			boolean foundCmirPrice = false;
			for (final Iterator<PriceRowModel> iterator = rowPrices.iterator(); iterator.hasNext();)
			{
				final PriceRowModel priceRowModel = iterator.next();
				if (priceRowModel instanceof EnergizerPriceRowModel)
				{
					final EnergizerPriceRowModel energizerPriceRowModel = (EnergizerPriceRowModel) priceRowModel;
					if (null != energizerPriceRowModel.getB2bUnit() && null != loggedInUserB2bUnit
							&& energizerPriceRowModel.getB2bUnit().getUid().equalsIgnoreCase(loggedInUserB2bUnit.getUid())
							&& (energizerCMIRModel != null && null != energizerCMIRModel.getCustPriceUOM()
									&& null != energizerPriceRowModel.getPriceUOM()
									&& energizerCMIRModel.getCustPriceUOM().equalsIgnoreCase(energizerPriceRowModel.getPriceUOM())))
					{
						if (energizerPriceRowModel.getPrice() == null || energizerPriceRowModel.getPrice().doubleValue() == 0.0)
						{
							foundCmirPrice = false;
						}
						else
						{
							foundCmirPrice = true;
							if ("personalCare".equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()))
							{
								if (energizerPriceRowModel.getIsActive())
								{
									productData
											.setCustomerProductPrice(energizerPriceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO)
													: BigDecimal.valueOf(energizerPriceRowModel.getPrice()).setScale(2, RoundingMode.CEILING));
									productData.setCustomerPriceCurrency(energizerPriceRowModel.getCurrency().getSymbol() == null ? EMPTY
											: energizerPriceRowModel.getCurrency().getSymbol());
									productData.setPriceUOM(energizerPriceRowModel.getPriceUOM());
									break;
								}
								else
								{
									/*
									 * LOG.info("energizerPriceRowModel is inactive for this LATAM product ::: " +
									 * energizerPriceRowModel.getProduct().getCode() + " with price ::: " +
									 * energizerPriceRowModel.getPrice());
									 */
								}

							}
							else
							{
								if (energizerPriceRowModel.getIsActive())
								{
									productData
											.setCustomerProductPrice(energizerPriceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO)
													: BigDecimal.valueOf(energizerPriceRowModel.getPrice() * baseUOM).setScale(2,
															RoundingMode.CEILING));
									productData.setCustomerPriceCurrency(energizerPriceRowModel.getCurrency().getSymbol() == null ? EMPTY
											: energizerPriceRowModel.getCurrency().getSymbol());

									break;
								}
								else
								{

									/*
									 * LOG.info("energizerPriceRowModel is inactive for this EMEA product ::: " +
									 * energizerPriceRowModel.getProduct().getCode() + " with price ::: " +
									 * energizerPriceRowModel.getPrice());
									 */

								}
							}
						}
					}
				}
			}
			if (!foundCmirPrice)
			{
				for (final Iterator<PriceRowModel> iterator = rowPrices.iterator(); iterator.hasNext();)
				{
					final PriceRowModel priceRowModel = iterator.next();
					if (priceRowModel instanceof EnergizerPriceRowModel)
					{
						continue;
					}
					LOG.info(
							"Energizer priceRow model not available, so we are taking priceRow model for calculating product price, Product : "
									+ productData.getCode() + " b2b unit : " + loggedInUserB2bUnit.getUid());
					if ("personalCare".equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()))
					{
						productData.setCustomerProductPrice(priceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO)
								: BigDecimal.valueOf(priceRowModel.getPrice()).setScale(2, RoundingMode.CEILING));
						productData.setPriceUOM(priceRowModel.getPriceUOM());
					}
					else
					{
						productData.setCustomerProductPrice(priceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO)
								: BigDecimal.valueOf(priceRowModel.getPrice() * baseUOM).setScale(2, RoundingMode.CEILING));
					}
					productData.setCustomerPriceCurrency(
							priceRowModel.getCurrency().getSymbol() == null ? EMPTY : priceRowModel.getCurrency().getSymbol());
				}

			}

			final List<EnergizerProductConversionFactorModel> conversionFactor = energizerProductModel.getProductConversionFactors();
			for (final EnergizerProductConversionFactorModel factor : conversionFactor)
			{
				if (factor.getAlternateUOM().equalsIgnoreCase("CS") && factor.getConversionMultiplier() != null)
				{
					numberOfEachInCase = factor.getConversionMultiplier();
				}
				else if (factor.getAlternateUOM().equalsIgnoreCase("LAY") && factor.getConversionMultiplier() != null)
				{
					numberOfEachInLayer = factor.getConversionMultiplier();
				}
				else if (factor.getAlternateUOM().equalsIgnoreCase("PAL") && factor.getConversionMultiplier() != null)
				{
					numberOfEachInPallet = factor.getConversionMultiplier();
				}
				// Calculate numberOfCUsPerSU & numberOfSUsPerPallet ONLY for EMEA - START
				if (PERSONALCARE_EMEA.equalsIgnoreCase(
						configurationService.getConfiguration().getString("site.personalCareEMEA", "personalCareEMEA")))
				{
					if (factor.getAlternateUOM().equalsIgnoreCase("CU") && factor.getConversionMultiplier() != null)
					{
						numberOfEachInCU = factor.getConversionMultiplier();
					}
					else if (factor.getAlternateUOM().equalsIgnoreCase("SU") && factor.getConversionMultiplier() != null)
					{
						numberOfEachInSU = factor.getConversionMultiplier();
					}
					if (numberOfEachInCU > 0 && numberOfEachInSU > 0)
					{
						numberOfCUsPerSU = numberOfEachInSU / numberOfEachInCU;
						numberOfSUsPerPallet = numberOfEachInPallet / numberOfEachInSU;
					}
					productData.setNumberOfCUsPerSU(numberOfCUsPerSU);
					productData.setNumberOfSUsPerPallet(numberOfSUsPerPallet);
				}
				// Calculate numberOfCUsPerSU & numberOfSUsPerPallet ONLY for EMEA - END

			}

			// WeSell Implementation - Just added if condition since conversions not required for WeSell - START
			if (!productData.isIsWeSellProduct())
			{
				if (numberOfEachInCase > 0)
				{
					numberOfLayersPerPallet = numberOfEachInPallet / numberOfEachInLayer;
					numberOfCasesPerPallet = numberOfEachInPallet / numberOfEachInCase;
					numberOfCasesPerLayer = numberOfEachInLayer / numberOfEachInCase;
				}

				productData.setNumberOfLayersPerPallet(numberOfLayersPerPallet);
				productData.setNumberOfCasesPerPallet(numberOfCasesPerPallet);
				productData.setNumberOfCasesPerLayer(numberOfCasesPerLayer);
			}
			// WeSell Implementation - Just added if condition since conversions not required for WeSell - END
		}
		catch (final Exception e)
		{
			LOG.info(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @return the loggedInUserB2bUnit
	 */
	public B2BUnitModel getLoggedInUserB2bUnit()
	{
		return loggedInUserB2bUnit;
	}

	/**
	 * the loggedInUserB2bUnit to set
	 */
	public void setLoggedInUserB2bUnit()
	{
		final String currentUserId = customerFacade.getCurrentCustomer().getUid();
		final EnergizerB2BUnitModel b2bUnit = (EnergizerB2BUnitModel) b2bCommerceUserService
				.getParentUnitForCustomer(currentUserId);

		this.loggedInUserB2bUnit = b2bUnit;
	}

}
