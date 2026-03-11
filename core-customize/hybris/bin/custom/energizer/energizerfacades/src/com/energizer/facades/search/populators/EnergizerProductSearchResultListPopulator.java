/**
 *
 */
package com.energizer.facades.search.populators;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.search.resultdata.SearchResultValueData;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.product.PriceService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
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
import com.energizer.facades.product.populators.EnergizerSearchResultProductPopulator;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author Bivash Pandit
 *
 */
public class EnergizerProductSearchResultListPopulator extends EnergizerSearchResultProductPopulator
{
	@Resource
	ProductService productService;
	@Resource
	B2BCommerceUserService b2bCommerceUserService;
	@Resource(name = "b2bCustomerFacade")
	protected CustomerFacade customerFacade;
	@Resource
	PriceService priceService;
	@Resource
	EnergizerProductService energizerProductService;

	@Resource
	private UserService userService;

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	private EnergizerB2BUnitModel loggedInUserB2bUnit;
	private static int ZERO = 0;
	private static String EMPTY = "";
	private static final String PERSONALCARE_EMEA = "personalCareEMEA";

	private static final Logger LOG = Logger.getLogger(EnergizerProductSearchResultListPopulator.class.getName());


	@Override
	public void populate(final SearchResultValueData source, final ProductData productData)
	{
		try
		{
			super.populate(source, productData);
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


			setLoggedInUserB2bUnit();
			final String productCode = source.getValues().get("code").toString();
			final EnergizerProductModel energizerProductModel = (EnergizerProductModel) productService
					.getProductForCode(productCode);

			if (null != energizerProductModel)
			{

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

				if (null != energizerProductModel.getDescription())
				{
					productData.setDescription(energizerProductModel.getDescription());
				}
				if (null != energizerProductModel.getCode())
				{
					productData.setErpMaterialID(energizerProductModel.getCode());
				}
				if (null != energizerProductModel.getEan())
				{
					productData.setEan(energizerProductModel.getEan());
				}

				//Set PBG/Non-PBG Product flag
				productData.setIsPBG(energizerProductModel.getIsPBG() == null ? false : energizerProductModel.getIsPBG());

			}

			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
			if (null != energizerCMIRModel)
			{
				if (null != energizerCMIRModel.getCustomerMaterialId())
				{
					productData.setCustomerMaterialId(energizerCMIRModel.getCustomerMaterialId());
				}
				if (null != energizerCMIRModel.getCustomerMaterialDescription())
				{
					productData.setCustomerProductName(energizerCMIRModel.getCustomerMaterialDescription());
				}
				if (null != energizerCMIRModel.getOrderingUnit())
				{
					productData.setMoq(energizerCMIRModel.getOrderingUnit());
				}
				if (null != energizerCMIRModel.getUom())
				{
					productData.setUom(energizerCMIRModel.getUom());
				}
				if (null != energizerCMIRModel.getShippingPoint())
				{
					productData.setShippingPoint(energizerCMIRModel.getShippingPoint());
					final String shippingPointName = energizerProductService
							.getShippingPointName(energizerCMIRModel.getShippingPoint());
					productData.setShippingPointName(shippingPointName == null ? EMPTY : shippingPointName);

					final String shippingPointLocation = energizerProductService
							.getShippingPointLocation(energizerCMIRModel.getShippingPoint());
					productData.setShippingPointLocation(shippingPointLocation);
				}

				//Set WeSell/Non-WeSell Product flag
				productData.setIsWeSellProduct(
						energizerCMIRModel.getIsWeSellProduct() == null ? false : energizerCMIRModel.getIsWeSellProduct());
			}
			final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
					.getEnergizerProductConversion(productCode, b2bUnit.getUid());

			if (energizerProductConversionFactorModel != null)
			{
				final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM(); // EACH / CASE/ PALLET / LAYER

				if (null != alternateUOM && null != productData.getUom() && alternateUOM.equalsIgnoreCase(productData.getUom()))
				{
					baseUOM = (energizerProductConversionFactorModel.getConversionMultiplier()).intValue();
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
							&& (null != energizerCMIRModel.getCustPriceUOM() && null != energizerPriceRowModel.getPriceUOM()
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
									+ productData.getCode() + ", b2b unit : " + loggedInUserB2bUnit.getUid());
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
			e.printStackTrace();
		}
	}

	/**
	 * @return the loggedInUserB2bUnit
	 */
	public EnergizerB2BUnitModel getLoggedInUserB2bUnit()
	{
		return loggedInUserB2bUnit;
	}

	/**
	 * the loggedInUserB2bUnit to set
	 */
	public void setLoggedInUserB2bUnit()
	{
		final String currentUserId = customerFacade.getCurrentCustomer().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(currentUserId);


		this.loggedInUserB2bUnit = b2bUnit;
	}


}
