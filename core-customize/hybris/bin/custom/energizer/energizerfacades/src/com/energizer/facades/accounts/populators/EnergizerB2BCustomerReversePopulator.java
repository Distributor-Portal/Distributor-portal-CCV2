/**
 *
 */
package com.energizer.facades.accounts.populators;

import de.hybris.platform.b2b.company.B2BCommerceB2BUserGroupService;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.strategies.CustomerNameStrategy;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.facades.accounts.impl.DefaultEnergizerGroupsLookUpStrategy;


/**
 * @author M1028720
 *
 */
public class EnergizerB2BCustomerReversePopulator implements Populator<CustomerData, EnergizerB2BCustomerModel>
{

	private B2BCommerceUnitService b2bCommerceUnitService;
	private B2BCommerceB2BUserGroupService b2BCommerceB2BUserGroupService;
	private CustomerNameStrategy customerNameStrategy;
	private DefaultEnergizerGroupsLookUpStrategy energizerGroupsLookUpStrategy;
	private UserService userService;
	private B2BUnitService<B2BUnitModel, UserModel> b2bUnitService;

	/**
	 * @return the energizerGroupsLookUpStrategy
	 */
	public DefaultEnergizerGroupsLookUpStrategy getEnergizerGroupsLookUpStrategy()
	{
		return energizerGroupsLookUpStrategy;
	}

	/**
	 * @param energizerGroupsLookUpStrategy
	 *           the energizerGroupsLookUpStrategy to set
	 */
	public void setEnergizerGroupsLookUpStrategy(final DefaultEnergizerGroupsLookUpStrategy energizerGroupsLookUpStrategy)
	{
		this.energizerGroupsLookUpStrategy = energizerGroupsLookUpStrategy;
	}

	public List<String> getUserGroups()
	{
		return getEnergizerGroupsLookUpStrategy().getUserGroups();
	}


	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected <T extends B2BCommerceUnitService> T getB2bCommerceUnitService()
	{
		return (T) b2bCommerceUnitService;
	}

	@Required
	public void setB2bCommerceUnitService(final B2BCommerceUnitService companyB2BCommerceService)
	{
		this.b2bCommerceUnitService = companyB2BCommerceService;
	}

	protected CustomerNameStrategy getCustomerNameStrategy()
	{
		return customerNameStrategy;
	}

	@Required
	public void setCustomerNameStrategy(final CustomerNameStrategy customerNameStrategy)
	{
		this.customerNameStrategy = customerNameStrategy;
	}

	public B2BCommerceB2BUserGroupService getB2BCommerceB2BUserGroupService()
	{
		return b2BCommerceB2BUserGroupService;
	}

	@Required
	public void setB2BCommerceB2BUserGroupService(final B2BCommerceB2BUserGroupService b2bCommerceB2BUserGroupService)
	{
		b2BCommerceB2BUserGroupService = b2bCommerceB2BUserGroupService;
	}

	protected B2BUnitService<B2BUnitModel, UserModel> getB2bUnitService()
	{
		return b2bUnitService;
	}

	@Required
	public void setB2bUnitService(final B2BUnitService<B2BUnitModel, UserModel> b2bUnitService)
	{
		this.b2bUnitService = b2bUnitService;
	}

	@Override
	public void populate(final CustomerData source, final EnergizerB2BCustomerModel target) throws ConversionException
	{
		if (null != source)
		{
			target.setEmail(source.getEmail());
			target.setName(getCustomerNameStrategy().getName(source.getFirstName(), source.getLastName()));
			target.setContactNumber(source.getContactNumber());
			final B2BUnitModel defaultUnit = getB2bCommerceUnitService().getUnitForUid(source.getUnit().getUid());
			final B2BUnitModel oldDefaultUnit = getB2bUnitService().getParent(target);
			target.setDefaultB2BUnit(defaultUnit);

			final Set<PrincipalGroupModel> groups = new HashSet<PrincipalGroupModel>(target.getGroups());
			if (oldDefaultUnit != null && groups.contains(oldDefaultUnit))
			{
				groups.remove(oldDefaultUnit);
			}
			groups.add(defaultUnit);
			target.setGroups(groups);
			updateUserGroups(getUserGroups(), source.getRoles(), target);
			if (StringUtils.isNotBlank(source.getTitleCode()))
			{
				target.setTitle(getUserService().getTitleForCode(source.getTitleCode()));
			}
			else
			{
				target.setTitle(null);
			}
			setUid(source, target);
		}
	}

	protected void setUid(final CustomerData source, final EnergizerB2BCustomerModel target)
	{
		if (source.getDisplayUid() != null && !source.getDisplayUid().isEmpty())
		{
			target.setOriginalUid(source.getDisplayUid());
			target.setUid(source.getDisplayUid().toLowerCase());
		}
		else if (source.getEmail() != null)
		{
			target.setOriginalUid(source.getEmail());
			target.setUid(source.getEmail().toLowerCase());
		}
	}

	public Set<PrincipalGroupModel> updateUserGroups(final Collection<String> availableUserGroups,
			final Collection<String> selectedUserGroups, final EnergizerB2BCustomerModel customerModel)
	{
		final Set<PrincipalGroupModel> customerGroups = new HashSet<PrincipalGroupModel>(customerModel.getGroups());

		// If you pass in NULL then nothing will happen
		if (selectedUserGroups != null)
		{
			for (final String group : availableUserGroups)
			{
				// add a group
				final UserGroupModel userGroupModel = getUserService().getUserGroupForUID(group);
				if (selectedUserGroups.contains(group))
				{
					customerGroups.add(userGroupModel);
				}
				else
				{ // remove a group
					customerGroups.remove(userGroupModel);
				}
			}
			customerModel.setGroups(customerGroups);
		}

		return customerGroups;
	}
}
