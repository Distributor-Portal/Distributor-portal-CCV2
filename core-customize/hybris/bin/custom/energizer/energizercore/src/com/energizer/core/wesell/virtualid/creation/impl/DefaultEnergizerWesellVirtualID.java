/**
 *
 */
package com.energizer.core.wesell.virtualid.creation.impl;

import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.wesell.virtualid.creation.EnergizerWesellVirtualID;


/**
 * @author kaki.rajasekhar
 *
 */
public class DefaultEnergizerWesellVirtualID implements EnergizerWesellVirtualID
{

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerWesellVirtualID.class);

	@Resource
	private ModelService modelService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "userService")
	private UserService userService;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.core.wesell.virtualid.creation.EnergizerWesellVirtualID#createVirtualID(com.energizer.core.model.
	 * EnergizerB2BUnitModel, java.lang.String)
	 */
	@Override
	public EnergizerB2BCustomerModel createVirtualID(final EnergizerB2BUnitModel b2bUnit,
			final EnergizerB2BEmployeeModel loginB2BEmployee)
	{
		try
		{
			final String virtualIDPrefix = loginB2BEmployee.getUid().split("@")[0].toLowerCase();
			final String virtualIDSuffix = configurationService.getConfiguration()
					.getString(EnergizerCoreConstants.WESELL_USERID_SUFFIX);

			//Example - alejandro.medina_0098112157@edgewell.com created as a virtual ID for sales rep individual cart maintenance
			final String wesellUid = virtualIDPrefix + "_" + b2bUnit.getUid() + virtualIDSuffix;

			final EnergizerB2BCustomerModel energizerB2BCustomerModel = modelService.create(EnergizerB2BCustomerModel.class);
			energizerB2BCustomerModel.setUid(wesellUid.trim().toLowerCase());
			energizerB2BCustomerModel.setEmail(wesellUid.trim().toLowerCase());
			energizerB2BCustomerModel.setOriginalUid(wesellUid.trim().toLowerCase());
			//energizerB2BCustomerModel.setOriginalUid(energizerB2BCustomerModel.getUid());
			//energizerB2BCustomerModel.setName(salesRepCode + "_" + b2bUnit.getUid());
			energizerB2BCustomerModel.setName(loginB2BEmployee.getName() + " " + b2bUnit.getUid()); //alejandro medina 0098112157

			energizerB2BCustomerModel
					.setContactNumber(configurationService.getConfiguration().getString(EnergizerCoreConstants.WESELL_CONTACT_NUMBER));
			// Set the flag to 'true', so that the registration email is not sent to this invalid email ID since it is Virtual ID for WeSell Sales Reps.
			energizerB2BCustomerModel.setRegistrationEmailFlag(true);

			final Set<PrincipalGroupModel> customerGroups = new HashSet<PrincipalGroupModel>();

			final String wesellDefaultUserGroups = configurationService.getConfiguration()
					.getString(EnergizerCoreConstants.WESELL_B2BCUSTOMER_DEFAULT_USER_GROUPS);

			if (null != wesellDefaultUserGroups)
			{
				for (final String userGroup : wesellDefaultUserGroups.split(";"))
				{
					try
					{
						final UserGroupModel userGroupModel = userService.getUserGroupForUID(userGroup);
						if (!customerGroups.contains(userGroupModel))
						{
							customerGroups.add(userGroupModel);
						}
					}
					catch (final Exception e)
					{
						LOG.info("Exception occured ::: " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
			energizerB2BCustomerModel.setGroups(customerGroups);
			energizerB2BCustomerModel.setDefaultB2BUnit(b2bUnit);
			energizerB2BCustomerModel.setPermissions(b2bUnit.getPermissions());
			modelService.save(energizerB2BCustomerModel);
			modelService.refresh(energizerB2BCustomerModel);
			LOG.info("Successfully created Wesell B2BCustomer model :  " + energizerB2BCustomerModel.getUid());

			modelService.refresh(b2bUnit);
			//LOG.info("Refreshed b2bUnit model :  " + b2bUnit.getUid());

			return energizerB2BCustomerModel;
		}
		catch (final Exception e)
		{
			// YTODO: handle exception
			LOG.info("Exception while creating Wesell customer " + e);
		}

		return null;
		//Added for WeSell Implementation - update/create b2b customer model - END
	}


}
