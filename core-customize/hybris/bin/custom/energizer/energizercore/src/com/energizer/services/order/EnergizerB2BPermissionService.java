/**
 * 
 */
package com.energizer.services.order;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.enums.B2BPermissionTypeEnum;
import de.hybris.platform.b2b.enums.PermissionStatus;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BOrderThresholdPermissionModel;
import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.platform.b2b.model.B2BPermissionResultModel;
import de.hybris.platform.b2b.services.impl.DefaultB2BPermissionService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.fest.util.Collections;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author kaushik.ganguly
 * 
 */
public class EnergizerB2BPermissionService extends DefaultB2BPermissionService
{
	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	private ModelService modelService;

	@Override
	public Set<B2BPermissionResultModel> getApproversForOpenPermissions(final AbstractOrderModel order,
			final B2BCustomerModel employee, final Collection<B2BPermissionResultModel> openPermissions)
	{
		final Set<B2BPermissionResultModel> permissionResult = new HashSet<>();
		final EnergizerB2BCustomerModel customer = (EnergizerB2BCustomerModel) order.getUser();
		if (Collections.isEmpty(openPermissions))
		{
			return permissionResult;
		}
		if (b2bCommerceUserService.getParentUnitForCustomer(customer.getUid()) != null)
		{
			final EnergizerB2BUnitModel b2bUnit = (EnergizerB2BUnitModel) b2bCommerceUserService.getParentUnitForCustomer(customer
					.getUid());
			EnergizerB2BCustomerModel approver = null;

			for (final UserModel user : b2bUnit.getApprovers())
			{
				approver = (EnergizerB2BCustomerModel) user;
				break;
			}
			if (approver != null)
			{
				permissionResult.add(createPermissionResult(order, approver, b2bUnit));
			}
		}
		return permissionResult;
	}

	/**
	 * this method will return all the permissions which are open without removing duplicates.
	 * 
	 * @param order
	 * @param employee
	 * @param openPermissions
	 * @return
	 */
	public List<B2BPermissionResultModel> getApproversForOpenPermission(final AbstractOrderModel order,
			final B2BCustomerModel employee, final Collection<B2BPermissionResultModel> openPermissions)
	{
		final List<B2BPermissionResultModel> permissionResult = new ArrayList<>();
		final EnergizerB2BCustomerModel customer = (EnergizerB2BCustomerModel) order.getUser();
		if (Collections.isEmpty(openPermissions))
		{
			return permissionResult;
		}
		if (b2bCommerceUserService.getParentUnitForCustomer(customer.getUid()) != null)
		{
			final EnergizerB2BUnitModel b2bUnit = (EnergizerB2BUnitModel) b2bCommerceUserService.getParentUnitForCustomer(customer
					.getUid());
			EnergizerB2BCustomerModel approver = null;

			for (final UserModel user : b2bUnit.getApprovers())
			{
				approver = (EnergizerB2BCustomerModel) user;
				if (approver != null && hasOrderThresholdPermissionToApprove(order, approver, (List) openPermissions))
				{
					permissionResult.add(createPermissionResult(order, approver, b2bUnit));
				}
			}

		}
		return permissionResult;
	}

	/**
	 * @param order
	 * @param approver
	 * @param openPermissions
	 * @return
	 */
	private boolean hasOrderThresholdPermissionToApprove(final AbstractOrderModel order, final EnergizerB2BCustomerModel approver,
			final List<B2BPermissionResultModel> openPermissions)
	{
		for (final B2BPermissionResultModel permission : openPermissions)
		{
			if (permission.getPermissionTypeCode().equals(B2BPermissionTypeEnum.B2BORDERTHRESHOLDPERMISSION.getCode()))
			{
				//permission;
				for (final B2BPermissionModel approverPermission : approver.getPermissions())
				{
					if (approverPermission.getItemtype().equals(B2BPermissionTypeEnum.B2BORDERTHRESHOLDPERMISSION.getCode()))
					{
						final B2BOrderThresholdPermissionModel apprPerm = (B2BOrderThresholdPermissionModel) approverPermission;
						return apprPerm.getThreshold() > order.getTotalPrice();
					}

				}
			}
		}
		//return !(evaluatePermissions(order, approver, openPermissions).size() > 0);
		return false;
	}

	private B2BPermissionResultModel createPermissionResult(final AbstractOrderModel order,
			final EnergizerB2BCustomerModel approver, final EnergizerB2BUnitModel b2bUnit)
	{
		final B2BPermissionResultModel model = modelService.create(B2BPermissionResultModel.class);
		final B2BPermissionModel permission = getFirstPermission(b2bUnit.getPermissions());
		model.setOrder(order);
		model.setApprover(approver);
		model.setPermission(permission);
		model.setStatus(PermissionStatus.PENDING_APPROVAL);
		model.setPermissionTypeCode(permission.getItemtype());
		modelService.save(model);
		return model;
	}

	private B2BPermissionModel getFirstPermission(final Set<B2BPermissionModel> permissions)
	{
		B2BPermissionModel retVal = null;
		for (final B2BPermissionModel permission : permissions)
		{
			retVal = permission;
			break;
		}
		return retVal;
	}
}
