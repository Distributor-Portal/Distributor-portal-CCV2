/**
 *
 */
package com.energizer.facades.flow.impl;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.storesession.StoreSessionFacade;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.facades.flow.EnergizerB2BCartFacade;
import com.energizer.services.order.impl.DefaultEnergizerCartService;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author KA289322
 *
 */
public class DefaultEnergizerB2BCartFacade implements EnergizerB2BCartFacade
{

	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BCartFacade.class);

	private de.hybris.platform.commercefacades.order.CartFacade cartFacade;

	private static final String CART_MODIFICATION_ERROR = "basket.error.occurred";

	private static int ZERO = 0;
	private static String EMPTY = "";

	@Resource(name = "energizerCartService")
	DefaultEnergizerCartService energizerCartService;

	@Resource(name = "storeSessionFacade")
	private StoreSessionFacade storeSessionFacade;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	EnergizerProductService energizerProductService;

	private Converter<CommerceCartModification, CartModificationData> cartModificationConverter;




	/**
	 * @return the cartModificationConverter
	 */
	public Converter<CommerceCartModification, CartModificationData> getCartModificationConverter()
	{
		return cartModificationConverter;
	}



	/**
	 * @param cartModificationConverter
	 *           the cartModificationConverter to set
	 */
	public void setCartModificationConverter(
			final Converter<CommerceCartModification, CartModificationData> cartModificationConverter)
	{
		this.cartModificationConverter = cartModificationConverter;
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCartFacade#updateOrderEntry(de.hybris.platform.commercefacades.order.data.
	 * OrderEntryData)
	 */
	@Override
	public CartModificationData updateOrderEntry(final OrderEntryData orderEntry)
	{

		CommerceCartModification cartModification = null;
		orderEntry.setEntryNumber(getOrderEntryNumber(orderEntry));

		if (orderEntry.getEntryNumber() != null)
		{
			// grouped ite
			cartModification = energizerCartService.updateExpectedUnitPriceForCartEntry(orderEntry);

		}
		return getCartModificationConverter().convert(cartModification);

	}



	protected Integer getOrderEntryNumber(final OrderEntryData findEntry)
	{

		if (findEntry.getEntryNumber() != null && findEntry.getEntryNumber().intValue() >= 0)
		{
			return findEntry.getEntryNumber();
		}
		else if (findEntry.getProduct() != null && findEntry.getProduct().getCode() != null)
		{
			for (final OrderEntryData orderEntry : cartFacade.getSessionCart().getEntries())
			{
				// find the entry
				if (orderEntry.getProduct().getCode().equals(findEntry.getProduct().getCode()))
				{
					if (CollectionUtils.isNotEmpty(orderEntry.getEntries()))
					{
						findEntry.setEntries(orderEntry.getEntries());
					}
					return orderEntry.getEntryNumber();
				}
				// check sub entries
				else if (orderEntry.getEntries() != null && !orderEntry.getEntries().isEmpty())
				{
					for (final OrderEntryData subEntry : orderEntry.getEntries())
					{
						// find the entry
						if (subEntry.getProduct().getCode().equals(findEntry.getProduct().getCode()))
						{
							return subEntry.getEntryNumber();
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public CartModificationData updateOrderEntryForAgreeEdgewellPrice(final OrderEntryData orderEntry)
	{

		CommerceCartModification cartModification = null;
		orderEntry.setEntryNumber(getOrderEntryNumber(orderEntry));

		if (orderEntry.getEntryNumber() != null)
		{
			// grouped ite
			cartModification = energizerCartService.updateAgreeEdgewellUnitPriceForCartEntry(orderEntry);

		}
		return getCartModificationConverter().convert(cartModification);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCartFacade#updateCartData(de.hybris.platform.commercefacades.order.data
	 * .CartData)
	 */
	@Override
	public void updateAgreeEdgewellPriceForAllProducts(final CartData cartData)
	{

		//final CommerceCartModification cartModification = null;

		if (null != cartData)
		{
			// update cart data for agree edgewell price for all the products
			//cartModification = energizerCartService.updateAgreeEdgewellPriceForAllProducts(cartData);
			energizerCartService.updateAgreeEdgewellPriceForAllProducts(cartData);

		}
		//return getCartModificationConverter().convert(cartModification);


	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCartFacade#getEachUnitPrice(com.energizer.core.model.EnergizerProductModel,
	 * com.energizer.core.model.EnergizerB2BUnitModel)
	 */
	public PriceData getEachUnitPrice(final EnergizerProductModel energizerProductModel,
			final EnergizerB2BUnitModel loggedInUserB2bUnit) throws Exception
	{
		final PriceData eachUnitPrice = new PriceData();
		EnergizerCMIRModel energizerCMIR = null;
		try
		{
		final Collection<PriceRowModel> rowPrices = energizerProductModel.getEurope1Prices();
		boolean foundCmirPrice = false;
				energizerCMIR = energizerProductService.getEnergizerCMIR(energizerProductModel.getCode(),
						loggedInUserB2bUnit.getUid());
		for (final Iterator iterator = rowPrices.iterator(); iterator.hasNext();)
		{
			final PriceRowModel priceRowModel = (PriceRowModel) iterator.next();
			if (priceRowModel instanceof EnergizerPriceRowModel)
			{
				final EnergizerPriceRowModel energizerPriceRowModel = (EnergizerPriceRowModel) priceRowModel;
				if (null != energizerPriceRowModel.getB2bUnit() && null != loggedInUserB2bUnit
							&& energizerPriceRowModel.getB2bUnit().getUid().equalsIgnoreCase(loggedInUserB2bUnit.getUid())
							&& (energizerCMIR != null && null != energizerCMIR.getCustPriceUOM()
									&& null != energizerPriceRowModel.getPriceUOM()
									&& energizerCMIR.getCustPriceUOM().equalsIgnoreCase(energizerPriceRowModel.getPriceUOM())))
				{
					if (energizerPriceRowModel.getPrice() == null || energizerPriceRowModel.getPrice().doubleValue() == 0.0)
					{
						foundCmirPrice = false;
					}
					else
					{
						foundCmirPrice = true;

						if (energizerPriceRowModel.getIsActive())
						{
								LOG.info("Energizer priceRow model available:::");
							LOG.info("eachUnitPrice for " + energizerProductModel.getCode() + " is === "
									+ BigDecimal.valueOf(energizerPriceRowModel.getPrice()));
							eachUnitPrice.setValue(energizerPriceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO)
									: new BigDecimal(energizerPriceRowModel.getPrice().toString()).setScale(2, RoundingMode.CEILING));
							eachUnitPrice.setCurrencyIso(energizerPriceRowModel.getCurrency().getIsocode() == null ? EMPTY
									: energizerPriceRowModel.getCurrency().getIsocode());
							eachUnitPrice.setFormattedValue(storeSessionFacade.getCurrentCurrency().getSymbol()
									.concat(eachUnitPrice.getValue().toString()));

							break;
						}
						else
						{
							/*
							 * LOG.info("energizerPriceRowModel is inactive for this product ::: " +
							 * energizerPriceRowModel.getProduct().getCode() + " with price ::: " +
							 * energizerPriceRowModel.getPrice());
							 */
						}
					}

				}
			}
		}
		if (!foundCmirPrice)
		{
			for (final Iterator iterator = rowPrices.iterator(); iterator.hasNext();)
			{
				final PriceRowModel priceRowModel = (PriceRowModel) iterator.next();
				if (priceRowModel instanceof EnergizerPriceRowModel)
				{
					continue;
				}
					LOG.info(
							"Energizer priceRow model not available, so we are taking priceRow model for calculating product price:::");
				LOG.info("eachUnitPrice for " + energizerProductModel.getCode() + " is === "
						+ BigDecimal.valueOf(priceRowModel.getPrice()));
				eachUnitPrice.setValue(priceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO) : new BigDecimal(priceRowModel
						.getPrice().toString()).setScale(2, RoundingMode.CEILING));
				eachUnitPrice.setCurrencyIso(priceRowModel.getCurrency().getSymbol() == null ? EMPTY : priceRowModel.getCurrency()
						.getIsocode());
				eachUnitPrice.setFormattedValue(storeSessionFacade.getCurrentCurrency().getSymbol()
						.concat(eachUnitPrice.getValue().toString()));
			}
		}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in getting Each Unit price :::" + e);
			throw e;
		}
		return eachUnitPrice;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCartFacade#updateOrderEntryForEachUnitPrice(de.hybris.platform.commercefacades
	 * .order.data.OrderEntryData)
	 */
	@Override
	public CartModificationData updateOrderEntryForEachUnitPrice(final OrderEntryData orderEntry)
	{
		CartModificationData cartModificationData = null;
		try
		{
		CommerceCartModification cartModification = null;
		orderEntry.setEntryNumber(getOrderEntryNumber(orderEntry));

		if (orderEntry.getEntryNumber() != null)
		{
			// grouped ite
			cartModification = energizerCartService.updateEachUnitPriceForCartEntry(orderEntry);
		}
		cartModificationData = getCartModificationConverter().convert(cartModification);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in getting Cart Modification data ::::" + e);
			throw e;
		}
		return cartModificationData;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCartFacade#getOrderEntryDataForEachUnitPrice(de.hybris.platform.commercefacades
	 * .order.data.OrderEntryData)
	 */
	@Override
	public OrderEntryData getOrderEntryDataForEachUnitPrice(final OrderEntryData orderEntry) throws Exception
	{
		try
		{
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final EnergizerProductModel energizerProductModel = energizerProductService.getProductWithCode(orderEntry.getProduct()
				.getCode());

		if (!orderEntry.getProduct().isIsWeSellProduct())
		{
			// Setting each unit price to the order entry
			orderEntry.setEachUnitPrice(getEachUnitPrice(energizerProductModel, b2bUnit));
		}
		else
		{
			orderEntry.setEachUnitPrice(null);
		}

		return orderEntry;

		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while calculating each unit price in cart::", e);
			throw e;
		}
	}

}
