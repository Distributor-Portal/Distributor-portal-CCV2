/**
 *
 */
package com.energizer.order.strategies.calculation.impl;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.order.strategies.calculation.impl.FindPricingWithCurrentPriceFactoryStrategy;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.PriceValue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author anitha.shastry
 *
 */
public class EnergizerFindPricingFactoryStrategy extends FindPricingWithCurrentPriceFactoryStrategy
{

	@Resource
	private ProductService productService;

	@Resource
	private EnergizerProductService energizerProductService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	private UserService userService;

	private static final Logger LOG = Logger.getLogger(EnergizerFindPricingFactoryStrategy.class);



	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.platform.order.strategies.calculation.FindPriceStrategy#findBasePrice(de.hybris.platform.core.model.
	 * order.AbstractOrderEntryModel)
	 */
	@Override
	public PriceValue findBasePrice(final AbstractOrderEntryModel entry)
	{
		final AbstractOrderEntry entryItem = getModelService().getSource(entry);
		PriceValue basePriceValue = null;
		EnergizerProductConversionFactorModel productConversionOfPriceUOM = null;

		try
		{
			if (null != userService.getCurrentUser() && !userService.getCurrentUser().getUid().equalsIgnoreCase("anonymous")
					&& !((boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn")))
			{
				basePriceValue = getCurrentPriceFactory().getBasePrice(entryItem);
			}

			final String b2bUnitID = getCurrentUserB2BUnit();

			if (b2bUnitID != null && !b2bUnitID.equals(""))
			{
				final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(entry.getProduct().getCode(),
						b2bUnitID);

				final EnergizerPriceRowModel cmirPriceRow = energizerProductService
						.getEnergizerPriceRowForB2BUnit(entry.getProduct().getCode(), b2bUnitID);

				if (cmirPriceRow != null && cmirPriceRow.getPrice() != 0.0)
				{
					basePriceValue = new PriceValue(cmirPriceRow.getCurrency().getIsocode(), cmirPriceRow.getPrice(), false);
				}

				/* Added code changes for Wesell when price value is '0.00' It will execute below condition- Only Wesell */
				else if (((boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn")) && cmirPriceRow != null
						&& cmirPriceRow.getPrice() == 0.0)
				{
					basePriceValue = new PriceValue(cmirPriceRow.getCurrency().getIsocode(), cmirPriceRow.getPrice(), false);
				}
				final EnergizerProductConversionFactorModel productConversionOfOrderingUOM = energizerProductService
						.getEnergizerProductConversion(entry.getProduct().getCode(), b2bUnitID);

				if (null != cmirPriceRow)
				{
					if (null != cmirPriceRow.getPriceUOM() && !cmirPriceRow.getPriceUOM().isEmpty())
					{
						productConversionOfPriceUOM = energizerProductService
								.getEnergizerProductConversionByUOM(entry.getProduct().getCode(), cmirPriceRow.getPriceUOM());
					}
					else
					{
						LOG.info("Price UOM not available/empty for Energizer Price Row for product '" + entry.getProduct().getCode()
								+ "', so defaulting to Price Row Model , basePriceValue :: " + basePriceValue.getValue()
								+ ", Conversion Multiplier :: " + productConversionOfOrderingUOM + " ..");
					}
				}
				else if (null != basePriceValue)
				{
					LOG.info("Energizer Price Row not available for this product '" + entry.getProduct().getCode() + "', b2bUnitID : "
							+ b2bUnitID + " so defaulting to Price Row Model , basePriceValue :: " + basePriceValue.getValue()
							+ ", Conversion Multiplier :: " + productConversionOfOrderingUOM + " ..");
				}

				if (null != energizerCMIR && null != energizerCMIR.getIsWeSellProduct() && energizerCMIR.getIsWeSellProduct()
						&& null != basePriceValue)
				{
					LOG.info("Energizer product basePrice for WeSell product : " + energizerCMIR.getErpMaterialId() + ", price : "
							+ basePriceValue.getValue());
					return new PriceValue(basePriceValue.getCurrencyIso(), basePriceValue.getValue(), false);
				}
				else
				{
					if (productConversionOfOrderingUOM != null && productConversionOfPriceUOM != null && null != basePriceValue)
					{
						LOG.info(
								"Energizer product : '" + entry.getProduct().getCode() + "',  basePrice : " + basePriceValue.getValue());
						LOG.info("Ordering UOM :  " + productConversionOfOrderingUOM.getAlternateUOM() + " , Conversion multipiler : "
								+ productConversionOfOrderingUOM.getConversionMultiplier());

						LOG.info("PriceUOM :  " + productConversionOfPriceUOM.getAlternateUOM() + " , Conversion multipiler : "
								+ productConversionOfPriceUOM.getConversionMultiplier());
						//BigDecimal newPrice = new BigDecimal(basePriceValue.getValue() * productConversion.getConversionMultiplier());
						BigDecimal newPrice = new BigDecimal(
								basePriceValue.getValue() * (productConversionOfOrderingUOM.getConversionMultiplier()
										/ productConversionOfPriceUOM.getConversionMultiplier()));
						newPrice = newPrice.setScale(2, RoundingMode.HALF_EVEN);

						return new PriceValue(basePriceValue.getCurrencyIso(), newPrice.doubleValue(), false);
					}
					else if (null != productConversionOfOrderingUOM && null != basePriceValue)
					{
						BigDecimal newPrice = new BigDecimal(
								basePriceValue.getValue() * productConversionOfOrderingUOM.getConversionMultiplier());
						newPrice = newPrice.setScale(2, RoundingMode.HALF_EVEN);
						return new PriceValue(basePriceValue.getCurrencyIso(), newPrice.doubleValue(), false);
					}
				}
			}

		}
		catch (final Exception e)
		{
			LOG.error(" Error while calculating item price of the product ", e);
		}
		return basePriceValue;
	}

	public String getCurrentUserB2BUnit()
	{
		final UserModel user = userService.getCurrentUser();
		if (user != null)
		{
			final B2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(user.getUid());
			if (b2bUnit != null)
			{
				return b2bUnit.getUid();
			}
		}
		return "";
	}
}
