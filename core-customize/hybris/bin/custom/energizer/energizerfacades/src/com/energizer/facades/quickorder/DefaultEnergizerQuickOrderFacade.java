/**
 *
 */
package com.energizer.facades.quickorder;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.quickorder.QuickOrderData;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author kaushik.ganguly
 *
 */
public class DefaultEnergizerQuickOrderFacade implements EnergizerQuickOrderFacade
{

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerQuickOrderFacade.class);

	@Resource(name = "b2bProductFacade")
	private ProductFacade productFacade;


	@Resource(name = "productService")
	private ProductService productService;

	@Resource(name = "energizerProductService")
	EnergizerProductService energizerProductService;


	@Resource(name = "energizerSolrQueryManipulationService")
	private EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;


	@Resource
	private CartService cartService;

	@Resource
	private CartFacade cartFacade;


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.core.quickorder.EnergizerQuickOrderService#getOrderEntryDataFromProductCode(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public OrderEntryData getOrderEntryDataFromProductCode(final String productCode, final String b2bunit)
	{

		OrderEntryData orderEntry = null;

		if (productCode != null && b2bunit != null)
		{
			orderEntry = new OrderEntryData();

			final ProductModel productModel = productService.getProductForCode(productCode);
			final ProductData productData = productFacade.getProductForOptions(productModel, Arrays.asList(ProductOption.BASIC));
			orderEntry.setProduct(productData);
		}

		return orderEntry;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.quickorder.EnergizerQuickOrderFacade#getQuickOrderFromSession(javax.servlet.http.HttpSession )
	 */
	@Override
	public QuickOrderData getQuickOrderFromSession(QuickOrderData quickOrder)
	{

		try
		{
			if (quickOrder == null)
			{
				quickOrder = new QuickOrderData();
				quickOrder.setLineItems(new ArrayList<OrderEntryData>());
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in getting Quick Order Data :::" + e);
			throw e;
		}
		return quickOrder;
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.quickorder.EnergizerQuickOrderFacade#addItemToQuickOrder(com.energizer.quickorder.QuickOrder ,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void addItemToQuickOrder(final QuickOrderData quickOrder, final String productCode, final String customerProductCode)
	{
		try
		{
			if (productExistsInList(productCode, quickOrder))
			{
				final OrderEntryData entry = getExistingProductFromList(productCode, quickOrder);
				if (entry !=null) {
					Long qty = entry.getQuantity();
					qty += ((qty % entry.getProduct().getMoq()) + 1) * entry.getProduct().getMoq();
					entry.setQuantity(qty);
				}
			}
			else
			{
				final EnergizerCMIRModel cmir = getCMIRForProductCodeOrCustomerMaterialID(productCode, customerProductCode);
				if (cmir != null)
				{
					if (validateCMIRforOrderEntry(cmir))
					{
						quickOrder.getLineItems().add(getProductData(cmir.getErpMaterialId(), cmir.getCustomerMaterialId(), cmir));
					}
					else
					{
						LOG.error("Ordering Unit is empty, Item cannot be added to Cart");
					}

				}

			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in adding item in quick order:::" + e);
			e.printStackTrace();
		}
	}

	public OrderEntryData getProductData(final String productCode, final String customerProductCode, final EnergizerCMIRModel cmir)
	{
		OrderEntryData orderEntry = null;
		ProductModel productModel = null;
		if (null != productCode && !productCode.isEmpty())
		{
			try
			{
				productModel = productService.getProductForCode(productCode);
			}
			catch (final UnknownIdentifierException ex)
			{
				productModel = null;
			}
		}
		if (null != customerProductCode && !customerProductCode.isEmpty())
		{
			if (cmir != null)
			{
				try
				{
					productModel = productService.getProductForCode(cmir.getErpMaterialId());
				}
				catch (final UnknownIdentifierException ex)
				{
					productModel = null;
				}
			}
		}
		if (productModel != null)
		{
			if (validateCMIRforOrderEntry(cmir))
			{
				orderEntry = new OrderEntryData();
				final ProductData product = new ProductData();
				product.setCode(cmir.getErpMaterialId());
				product.setCustomerMaterialId(cmir.getCustomerMaterialId());
				product.setName(productModel.getName());
				product.setUom(cmir.getUom());
				product.setMoq(cmir.getOrderingUnit());
				orderEntry.setQuantity(new Long(cmir.getOrderingUnit()));
				//orderEntry.setQuantity(cmir.getOrderingUnit() != null ? new Long(cmir.getOrderingUnit()) : null);
				orderEntry.setProduct(product);
				orderEntry.setShippingPoint(cmir.getShippingPoint());

				final String shippingPointLocation = energizerProductService.getShippingPointLocation(cmir.getShippingPoint());
				orderEntry.setShippingPointLocation(shippingPointLocation);
				try
				{
					getReferenceShippingPoint(orderEntry);
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return orderEntry;
	}


	public void getReferenceShippingPoint(final OrderEntryData orderEntryData)
	{
		try
		{
			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);

			if (cartService.hasSessionCart())
			{
				final CartModel cartModel = cartService.getSessionCart();
				if (cartModel.getEntries().size() != 0 && !cartModel.getEntries().isEmpty())
				{
					final AbstractOrderEntryModel orderEntry = cartModel.getEntries().get(0);

					final String cartProductCode = orderEntry.getProduct().getCode();

					final EnergizerCMIRModel cartenergizerCMIR = energizerProductService.getEnergizerCMIR(cartProductCode,
							b2bUnit.getUid());

					final String cartshippingPoint = cartenergizerCMIR.getShippingPoint();
					//                      LOG.info("The shippingPointNo of product in the cart : " + cartshippingPoint);
					orderEntryData.setReferenceShippingPoint(cartshippingPoint);
					orderEntryData
							.setReferenceShippingPointLocation(energizerProductService.getShippingPointLocation(cartshippingPoint));

				}
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in getting Reference Shipping Point:::" + e);
			e.printStackTrace();
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.quickorder.EnergizerQuickOrderFacade#removeItemFromQuickOrder(com.energizer.quickorder.
	 * QuickOrder, java.lang.String)
	 */
	@Override
	public void removeItemFromQuickOrder(final QuickOrderData quickOrder, final String productCode)
	{
		for (final OrderEntryData entry : quickOrder.getLineItems())
		{
			if (entry.getProduct().getCode().equals(productCode))
			{
				quickOrder.getLineItems().remove(entry);
				break;
			}
		}

	}

	public boolean productExistsInList(final String productCode, final QuickOrderData quickOrder)
	{
		boolean flag = false;
		try
		{
			for (final OrderEntryData orderEntry : quickOrder.getLineItems())
			{
				if (orderEntry.getProduct() != null && orderEntry.getProduct().getCode().equals(productCode))
				{
					flag = true;
					break;
				}

			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured while checking for product exists in cart:::" + e);
			throw e;
		}
		return flag;
	}

	private OrderEntryData getExistingProductFromList(final String productCode, final QuickOrderData quickOrder) throws Exception
	{
		OrderEntryData entry = null;
		try
		{
			for (final OrderEntryData orderEntry : quickOrder.getLineItems())
			{
				if (orderEntry.getProduct() != null && (orderEntry.getProduct().getCode().equals(productCode)
						|| orderEntry.getProduct().getCustomerMaterialId().equals(productCode)))
				{
					entry = orderEntry;
					break;
				}

			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in getting existing product from list::::" + e);
			throw e;
		}
		return entry;
	}

	public EnergizerCMIRModel getCMIRForProductCodeOrCustomerMaterialID(final String productCode, final String customerMaterialId)
			throws Exception
	{
		EnergizerCMIRModel cmir = null;
		try
		{
			if ((productCode != null && !productCode.isEmpty()) && (null != customerMaterialId && !customerMaterialId.isEmpty()))
			{
				cmir = energizerProductService.getEnergizerCMIR(productCode,
						energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser().getUid());
				if (cmir != null && !cmir.getCustomerMaterialId().equals(customerMaterialId))
				{
					return null;
				}
				else
				{
					return cmir;
				}
			}

			if (null != productCode && !productCode.isEmpty())
			{
				cmir = energizerProductService.getEnergizerCMIR(productCode,
						energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser().getUid());
			}

			if (cmir == null && null != customerMaterialId && !customerMaterialId.isEmpty())
			{
				cmir = energizerProductService.getEnergizerCMIRforCustomerMaterialID(customerMaterialId,
						energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser().getUid());
			}
		}
		catch (final ModelNotFoundException mn)
		{
			LOG.info("Exception occured in getting CMIR model::::" + mn);
			throw mn;
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in getting CMIR model:::" + e);
			throw e;
		}
		return cmir;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.quickorder.EnergizerQuickOrderFacade#updateQtyToExistingProduct(com.energizer.quickorder
	 * .QuickOrderData, java.lang.String, java.lang.Integer)
	 */
	@Override
	public void updateQtyToExistingProduct(final QuickOrderData quickOrder, final String productCode, final Long qty)
	{
		try
		{
			for (final OrderEntryData entry : quickOrder.getLineItems())
			{
				if (entry.getProduct().getCode().equals(productCode))
				{
					entry.setQuantity(qty);
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean validateCMIRforOrderEntry(final EnergizerCMIRModel cmir)
	{
		return cmir.getOrderingUnit() != null && cmir.getUom() != null && !cmir.getUom().isEmpty();
	}

	public CartData getCurrentSessionCart()
	{
		final CartData cartData = cartFacade.getSessionCart();
		if (cartData != null && cartData.getEntries() != null && cartData.getEntries().size() > 0)
		{
			final OrderEntryData cartOrderEntry = cartData.getEntries().get(0);
			if (cartOrderEntry.getProduct().getShippingPoint() != null)
			{
				final String shippingPointName = energizerProductService
						.getShippingPointName(cartOrderEntry.getProduct().getShippingPoint());
				//this would only reflect for quick order page.
				cartData.setShippingPoint((shippingPointName == null) ? "" : shippingPointName);
			}


		}
		return cartData;
	}

	public void getOrderEntryShippingPoints(final OrderEntryData orderEntryData, final QuickOrderData quickOrder)
	{
		try
		{
			final String productCode = orderEntryData.getProduct().getCode();
			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
			final String shippingPointNo = energizerCMIR.getShippingPoint();

			orderEntryData.setShippingPoint(shippingPointNo);
			/*
			 * if (cartService.hasSessionCart()) {
			 */
			final CartModel cartModel = cartService.getSessionCart();
			if (cartModel.getEntries() != null && !cartModel.getEntries().isEmpty())
			{
				final AbstractOrderEntryModel orderEntry = cartModel.getEntries().get(0);

				final String cartProductCode = orderEntry.getProduct().getCode();

				final EnergizerCMIRModel cartenergizerCMIR = energizerProductService.getEnergizerCMIR(cartProductCode,
						b2bUnit.getUid());

				final String cartshippingPoint = cartenergizerCMIR.getShippingPoint();
				LOG.info("The shippingPointNo of product in the cart : " + cartshippingPoint);
				orderEntryData.setReferenceShippingPoint(cartshippingPoint);
				quickOrder.setCurrentShippingPointId(cartshippingPoint);

			}
			else
			{
				if (quickOrder.getCurrentShippingPointId() != null)
				{
					energizerCMIR = energizerProductService.getEnergizerCMIR(orderEntryData.getProduct().getCode(), b2bUnit.getUid());
					final String shippingPoint = energizerCMIR.getShippingPoint();
					orderEntryData.setShippingPoint(shippingPoint);
					orderEntryData.setReferenceShippingPoint(quickOrder.getCurrentShippingPointId());
				}
				else
				{
					energizerCMIR = energizerProductService.getEnergizerCMIR(orderEntryData.getProduct().getCode(), b2bUnit.getUid());
					final String shippingPoint = energizerCMIR.getShippingPoint();
					orderEntryData.setShippingPoint(shippingPoint);
					orderEntryData.setReferenceShippingPoint(quickOrder.getCurrentShippingPointId());
					quickOrder.setCurrentShippingPointId(shippingPoint);
				}

			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in getting Order Entry Shipping Point:::" + e);
			e.printStackTrace();
		}
		//}
	}

}
