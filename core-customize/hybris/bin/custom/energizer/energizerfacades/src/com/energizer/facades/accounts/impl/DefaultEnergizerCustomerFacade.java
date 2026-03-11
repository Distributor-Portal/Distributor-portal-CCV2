/**
 *
 */
package com.energizer.facades.accounts.impl;





import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.services.b2bemployee.impl.DefaultB2BEmployeeAccountService;



/**
 * @author kaki.rajasekhar
 *
 */
public class DefaultEnergizerCustomerFacade extends DefaultCustomerFacade
{
	@Resource
	private UserService userService;
	@Resource
	private SessionService sessionService;
	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
	@Resource
	private DefaultB2BEmployeeAccountService defaultB2BEmployeeAccountService;


	/**
	 * @return the defaultB2BEmployeeAccountService
	 */
	public DefaultB2BEmployeeAccountService getDefaultB2BEmployeeAccountService()
	{
		return defaultB2BEmployeeAccountService;
	}


	/**
	 * @param defaultB2BEmployeeAccountService
	 *           the defaultB2BEmployeeAccountService to set
	 */
	public void setDefaultB2BEmployeeAccountService(final DefaultB2BEmployeeAccountService defaultB2BEmployeeAccountService)
	{
		this.defaultB2BEmployeeAccountService = defaultB2BEmployeeAccountService;
	}
	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerCustomerFacade.class);

	@Override
	public void loginSuccess()
	{
		try
		{
			final EnergizerB2BCustomerModel selectedB2BCustomer = (EnergizerB2BCustomerModel) sessionService
					.getAttribute("selectedB2BCustomer");
			if (null != selectedB2BCustomer)
			{
				this.sessionService.setAttribute("user", selectedB2BCustomer);
			}
			final CustomerData userData = getCurrentCustomer();

			// First thing to do is to try to change the user on the session cart
			if (getCartService().hasSessionCart())
			{
				getCartService().changeCurrentCartUser(getCurrentUser());
			}

			// Update the session currency (which might change the cart currency)
			if (!updateSessionCurrency(userData.getCurrency(), getStoreSessionFacade().getDefaultCurrency()))
			{
				// Update the user
				getUserFacade().syncSessionCurrency();
			}

			// Update the user
			getUserFacade().syncSessionLanguage();

			// Calculate the cart after setting everything up
			if (getCartService().hasSessionCart())
			{
				final CartModel sessionCart = getCartService().getSessionCart();

				// Clean the existing info on the cart if it does not beling to the current user
				getCartCleanStrategy().cleanCart(sessionCart);
				try
				{
					final CommerceCartParameter parameter = new CommerceCartParameter();
					parameter.setEnableHooks(true);
					parameter.setCart(sessionCart);
					getCommerceCartService().recalculateCart(parameter);
				}
				catch (final CalculationException ex)
				{
					LOG.error("Failed to recalculate order [" + sessionCart.getCode() + "]", ex);
					throw ex;
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error("Customer data conversion Exception ::: ", e);
			e.printStackTrace();
		}
	}

	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
	public void employeeForgottenPassword(final String uid)
	{
		try
		{
		Assert.hasText(uid, "The field [uid] cannot be empty");
		final EnergizerB2BEmployeeModel employeeModel = userService.getUserForUID(uid.toLowerCase(),
				EnergizerB2BEmployeeModel.class);
		getDefaultB2BEmployeeAccountService().forgottenPassword(employeeModel);
		}
		catch (final Exception ex)
		{
			LOG.info("Exception occured in facade:::: " + ex.getMessage());
			ex.printStackTrace();
		}
	}



	public void employeeUpdatePassword(final String token, final String newPassword) throws TokenInvalidatedException
	{
		getDefaultB2BEmployeeAccountService().changeEmployeePassword(token, newPassword);
	}

	public void sendEmployeeUnlockAccountEmail(final String uid)
	{
		getDefaultB2BEmployeeAccountService().sendEmployeeUnlockAccountEmail(uid);
	}

	public boolean unlockEmployeeAccount(final String email) throws UnknownIdentifierException
	{
		return getDefaultB2BEmployeeAccountService().unlockEmployeeAccount(email);
	}
	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
}
