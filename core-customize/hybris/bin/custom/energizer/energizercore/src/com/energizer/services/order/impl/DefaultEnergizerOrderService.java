/**
 *
 */
package com.energizer.services.order.impl;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.order.EnergizerOrderService;
import com.energizer.services.order.dao.EnergizerOrderDAO;


/**
 * @author ROMIL JAISHWAL M1028411
 */
public class DefaultEnergizerOrderService implements EnergizerOrderService
{
	@Resource
	private EnergizerOrderDAO energizerOrderDAO;
	@Resource
	private EnergizerOrderService energizerOrderService;
	@Resource
	private UserService userService;
	@Resource
	private CatalogVersionService catalogVersionService;
	@Resource
	private ProductService productService;


	@Override
	public OrderModel getExistingOrder(final String sapOrderNo)
	{
		return energizerOrderDAO.findExistingOrder(sapOrderNo);
	}

	@Override
	public OrderModel getExistingOrder(final String sapOrderNo, final String hybrisOrderNo)
	{
		return energizerOrderDAO.findExistingOrder(sapOrderNo, hybrisOrderNo);
	}

	@Override
	public OrderModel getHybrisOrderNoForOfflineOrder(final String sapOrderNo)
	{
		return energizerOrderDAO.findHybrisOrderNoForOfflineOrder(sapOrderNo);
	}

	@Override
	public OrderEntryModel getExistingOrderItem(final OrderModel energizerOrderModel,
			final EnergizerProductModel energizerProductModel)
	{
		return energizerOrderDAO.findExistingOrderItem(energizerOrderModel, energizerProductModel);
	}

	@Override
	public OrderStatus getEnergizerOrderStatus(final String energizerOrderStatusCode)
	{
		OrderStatus orderStatus = null;
		//Validate OrderStatus
		//Possible OrderStatus values
		//these values must be present in energizercore.xml
		final List<String> possibleOrderStatusValues = Arrays
				.asList(Config.getParameter("possibleOrderStatus").split(new Character(',').toString()));
		final String orderStatusCode = energizerOrderStatusCode.toUpperCase();
		final boolean result = possibleOrderStatusValues.contains(orderStatusCode);
		if (result)
		{
			orderStatus = OrderStatus.valueOf(orderStatusCode);
		}
		return orderStatus;
	}

	@Override
	public List<String> getAdminIdsOfEnergizerB2BUnitModel(final EnergizerB2BUnitModel energizerB2BUnitModel)

	{
		final List<String> adminIdsOfEnergizerB2BUnitModel = new ArrayList<String>();

		//Get all members of energizerB2BUnitModel
		final Set<PrincipalModel> allMemberOfEnergizerB2BUnitModel = energizerB2BUnitModel.getMembers();
		//Iterate all members and check any members is having admingroup or b2badmingroup if having then return id of that member
		//if no such member is there then return null
		for (final PrincipalModel eachMemberOfEnergizerB2BUnitModel : allMemberOfEnergizerB2BUnitModel)
		{
			final B2BCustomerModel b2bCustomerModel = (B2BCustomerModel) userService
					.getUserForUID(eachMemberOfEnergizerB2BUnitModel.getUid());
			if (!b2bCustomerModel.getActive())
			{
				continue;
			}

			//Get all groups of particular member of EnergizerB2BUnitModel
			final Set<PrincipalGroupModel> allGroupOfMemberOfEnergizerB2BUnitModel = eachMemberOfEnergizerB2BUnitModel.getGroups();
			//Iterate all groups and check for admin group
			for (final PrincipalGroupModel eachGroupOfmemberOfEnergizerB2BUnitModel : allGroupOfMemberOfEnergizerB2BUnitModel)
			{
				if (eachGroupOfmemberOfEnergizerB2BUnitModel.getUid().contains("admingroup"))
				{
					adminIdsOfEnergizerB2BUnitModel.add(eachMemberOfEnergizerB2BUnitModel.getUid());
					break;
				}
			}

		}
		return adminIdsOfEnergizerB2BUnitModel;
	}

	@Override
	public List<String> getAdminEmailIdsOfEnergizerB2BUnitModel(final EnergizerB2BUnitModel energizerB2BUnitModel)
	{
		final List<String> adminEmailIdsOfEnergizerB2BUnitModel = new ArrayList<String>();

		final List<String> adminIdsOfEnergizerB2BUnitModel = energizerOrderService
				.getAdminIdsOfEnergizerB2BUnitModel(energizerB2BUnitModel);

		//Iterate all admins for B2BAccount
		for (final String adminIdOfEnergizerB2BUnitModel : adminIdsOfEnergizerB2BUnitModel)
		{
			final UserModel adminUserModel = userService.getUserForUID(adminIdOfEnergizerB2BUnitModel);
			final B2BCustomerModel adminB2BCustomerModel = (B2BCustomerModel) adminUserModel;
			if (!StringUtils.isEmpty(adminB2BCustomerModel.getEmail()))
			{
				adminEmailIdsOfEnergizerB2BUnitModel.add(adminB2BCustomerModel.getEmail());
			}
		}
		return adminEmailIdsOfEnergizerB2BUnitModel;
	}

	@Override
	public EnergizerProductModel getEnergizerProduct(final String productMaterialId)
	{
		final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion("energizerProductCatalog", "Online");
		EnergizerProductModel existEnergizerProd = null;
		try
		{
			existEnergizerProd = (EnergizerProductModel) productService.getProductForCode(catalogVersion, productMaterialId);
		}
		catch (final UnknownIdentifierException e)
		{
			existEnergizerProd = null;
		}
		return existEnergizerProd;
	}

	@Override
	public BigDecimal getAdjustedTotalPriceForOrder(final OrderModel orderModel)
	{
		BigDecimal orderAdjustedTotalPrice = new BigDecimal(0);
		final List<OrderEntryModel> energizerOrderEntryModels = energizerOrderDAO.findExistingOrderItems(orderModel);
		if (energizerOrderEntryModels != null)
		{
			for (final OrderEntryModel orderEntryModel : energizerOrderEntryModels)
			{
				if (orderEntryModel.getAdjustedLinePrice() != null)
				{
					orderAdjustedTotalPrice = orderAdjustedTotalPrice.add(orderEntryModel.getAdjustedLinePrice());
				}
			}
		}
		return orderAdjustedTotalPrice;
	}


	@Override
	public List<Object> getAllStuckOrders(final List<String> status, final List<Date> dateList, final String site,
			final String region)
	{
		return energizerOrderDAO.getAllStuckOrders(status, dateList, site, region);

	}
}
