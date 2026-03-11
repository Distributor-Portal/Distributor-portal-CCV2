/**
 *
 */
package com.energizer.facades.order.impl;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.facades.order.EnergizerB2BOrderHistoryFacade;
import com.energizer.services.b2bemployee.impl.DefaultEnergizerB2BEmployeeService;
import com.energizer.services.order.impl.DefaultEnergizerB2BOrderHistoryService;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author M1028886
 *
 */
public class DefaultEnergizerB2BOrderHistoryFacade implements EnergizerB2BOrderHistoryFacade
{
	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BOrderHistoryFacade.class);

	@Resource(name = "energizerOrderHistoryConverter")
	private Converter<OrderModel, OrderHistoryData> energizerOrderHistoryConverter;

	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "customerAccountService")
	private CustomerAccountService customerAccountService;

	@Resource(name = "baseStoreService")
	private BaseStoreService baseStoreService;

	@Resource(name = "orderConverter")
	private Converter<OrderModel, OrderData> orderConverter;

	@Resource(name = "defaultEnergizerB2BOrderHistoryService")
	private DefaultEnergizerB2BOrderHistoryService b2bOrderHistoryService;

	@Resource(name = "energizerProductService")
	private EnergizerProductService energizerProductService;

	@Resource(name = "cartService")
	private CartService cartService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;


	@Resource(name = "defaultEnergizerB2BEmployeeService")
	private DefaultEnergizerB2BEmployeeService defaultEnergizerB2BEmployeeService;


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getOrdersForB2BUnit(de.hybris.platform.b2b.jalo.B2BUnit
	 * )
	 */
	public SearchPageData<OrderHistoryData> getOrdersForB2BUnit(final B2BUnitModel unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses) throws Exception
	{
		SearchPageData<OrderModel> ordersHistoryList = null;
		try
		{
			ordersHistoryList = b2bOrderHistoryService.getOrdersForB2BUnit(unitId, pageableData, orderStatuses);
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in getting orders list:::::" + e);
			throw e;
		}
		return convertPageData(ordersHistoryList, energizerOrderHistoryConverter);
	}

	protected <S, T> SearchPageData<T> convertPageData(final SearchPageData<S> source, final Converter<S, T> converter)
	{
		final SearchPageData<T> result = new SearchPageData<T>();
		result.setPagination(source.getPagination());
		result.setSorts(source.getSorts());
		result.setResults(Converters.convertAll(source.getResults(), converter));
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getEnergizerCMIR(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerCMIRModel getEnergizerCMIR(final String erpMaterialId, final String b2bUnitId)
			throws ModelNotFoundException, Exception
	{
		EnergizerCMIRModel energizerCMIR = null;
		try
		{
			energizerCMIR = energizerProductService.getEnergizerCMIR(erpMaterialId, b2bUnitId);
		}
		catch (final ModelNotFoundException mn)
		{
			LOG.info("Exception Occured in getting cmir model:::::" + mn);
			throw mn;
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in getting cmir model:::::" + e);
			throw e;
		}
		return energizerCMIR;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getParentUnitForCustomer(java.lang.String)
	 */
	@Override
	public String getProductCodeForCustomer()
	{
		String productCode = null;
		try
		{
			productCode = cartService.getSessionCart().getEntries().get(0).getProduct().getCode();
		}
		catch (final Exception e)
		{
			LOG.error("Exception Occured while fetching product code for the customer.....");
			throw e;
		}
		return productCode;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getParentUnitForCustomer(java.lang.String)
	 */
	@Override
	public EnergizerB2BUnitModel getParentUnitForCustomer(final String userId)
	{
		EnergizerB2BUnitModel b2bUnitModel = null;
		try
		{
			b2bUnitModel = b2bCommerceUserService.getParentUnitForCustomer(userId);
		}
		catch (final Exception e)
		{
			LOG.error("Exception Occured at while fetching parent unit for Customer....");
			throw e;
		}
		return b2bUnitModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getCurrentUser()
	 */
	@Override
	public String getCurrentUser()
	{
		String userId = null;
		try
		{
			userId = userService.getCurrentUser().getUid();
		}
		catch (final Exception e)
		{
			LOG.error("Exception Occured While fetching current user...");
			throw e;
		}
		return userId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getOrderHistoryForB2BUnit(java.lang.String,
	 * de.hybris.platform.core.enums.OrderStatus[])
	 */
	@Override
	public List<Object> getOrderHistoryForB2BUnit(final String unitId, final List orderStatusList) throws Exception
	{
		List<Object> ordersModelList = null;
		try
		{
			ordersModelList = b2bOrderHistoryService.getOrderHistoryForB2BUnit(unitId, orderStatusList);
		}
		catch (final Exception ex)
		{
			LOG.info("Exception occured while getting Order list::: " + ex);
			throw ex;
		}
		return ordersModelList;

	}


	@Override
	public SearchPageData<OrderHistoryData> getFilterOrdersForB2BUnit(final String unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses, final Date fromDate, final Date toDate, final boolean fetchAllOrders)
	{
		SearchPageData<OrderModel> ordersHistoryList = null;
		try
		{
			ordersHistoryList = defaultEnergizerB2BEmployeeService.getFilterOrdersForB2BUnit(unitId, pageableData, orderStatuses,
					fromDate, toDate, fetchAllOrders);
			return convertPageData(ordersHistoryList, energizerOrderHistoryConverter);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
