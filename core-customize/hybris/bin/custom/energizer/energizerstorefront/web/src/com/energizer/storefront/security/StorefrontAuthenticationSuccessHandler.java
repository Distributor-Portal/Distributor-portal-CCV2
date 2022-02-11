/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package com.energizer.storefront.security;

import de.hybris.platform.acceleratorservices.uiexperience.UiExperienceService;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commerceservices.enums.UiExperienceLevel;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.servicelayer.session.SessionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.energizer.core.datafeed.facade.impl.DefaultEnergizerPasswordExpiryFacade;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.pages.AbstractPageController;
import com.energizer.storefront.util.EnergizerPasswordNotificationUtil;


/**
 * Success handler initializing user settings and ensuring the cart is handled correctly
 */
public class StorefrontAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler
{
	private CustomerFacade customerFacade;
	private UiExperienceService uiExperienceService;
	private CartFacade cartFacade;
	private SessionService sessionService;
	private BruteForceAttackCounter bruteForceAttackCounter;

	private Map<UiExperienceLevel, Boolean> forceDefaultTargetForUiExperienceLevel;

	private final static String HOMEPAGE = "/";

	@Resource
	protected DefaultEnergizerPasswordExpiryFacade defaultEnergizerPasswordExpiryFacade;

	@Resource
	protected EnergizerPasswordNotificationUtil energizerPasswordNotificationUtil;

	public UiExperienceService getUiExperienceService()
	{
		return uiExperienceService;
	}

	@Required
	public void setUiExperienceService(final UiExperienceService uiExperienceService)
	{
		this.uiExperienceService = uiExperienceService;
	}


	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
			final Authentication authentication) throws IOException, ServletException
	{
		try
		{
		getCustomerFacade().loginSuccess();
		List<String> notificationMessages = null;
		if (null != authentication.getName())
		{
			//Added Code changes for WeSell Implementation - START
			if (!authentication.getName().equalsIgnoreCase("anonymous"))
			{
				setSaleRepUsernameAndPasswordToCookie(authentication, request, response);

			}
			//Added Code changes for WeSell Implementation - END

				//we are added session attribute for solr seach text functionality
				sessionService.setAttribute("solrFreeTextEnable", false);
			notificationMessages = energizerPasswordNotificationUtil.checkPasswordExpiryStatus(authentication.getName());
			if (null != notificationMessages && notificationMessages.size() > 0 && notificationMessages.get(0).equalsIgnoreCase("0"))
			{
				sessionService.setAttribute("passwordAlert", notificationMessages.get(1));
				sessionService.setAttribute("dayCount", notificationMessages.get(2));

			}
		}

		//Password questions and answers not required to be set, so commenting the below line - START
		//isPasswordQuestionAnswerSet(authentication.getName());
		//Password questions and answers not required to be set, so commenting the below line - END


		if (!getCartFacade().hasSessionCart() || getCartFacade().getSessionCart().getEntries().isEmpty())
		{
			try
			{
				getSessionService().setAttribute(WebConstants.CART_RESTORATION, getCartFacade().restoreSavedCart(null));
			}
			catch (final CommerceCartRestorationException e)
			{
				getSessionService().setAttribute(WebConstants.CART_RESTORATION, "basket.restoration.errorMsg");
			}
		}

		getBruteForceAttackCounter().resetUserCounter(getCustomerFacade().getCurrentCustomer().getUid());

		//redirect the user to homepage if the user has just updated the password
		if (sessionService.getAttribute(AbstractPageController.JUST_UPDATED_PWD) != null
				&& sessionService.getAttribute(AbstractPageController.JUST_UPDATED_PWD).equals(
						AbstractPageController.JUST_UPDATED_PWD))
		{
			sessionService.removeAttribute(AbstractPageController.JUST_UPDATED_PWD);

			response.sendRedirect(HOMEPAGE);

		}
		else if (sessionService.getAttribute(AbstractPageController.JUST_UNLOCKED_ACCOUNT) != null
				&& sessionService.getAttribute(AbstractPageController.JUST_UNLOCKED_ACCOUNT).equals(
						AbstractPageController.JUST_UNLOCKED_ACCOUNT))
		{
			sessionService.removeAttribute(AbstractPageController.JUST_UNLOCKED_ACCOUNT);

			response.sendRedirect(HOMEPAGE);

		}
		else
		{
			//Added Code changes for WeSell Implementation - START
			final boolean isSalesRepUser = (boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn");
			if (isSalesRepUser)
			{
				clearAuthenticationAttributes(request);
				getRedirectStrategy().sendRedirect(request, response, HOMEPAGE);
			}
			//Added Code changes for WeSell Implementation - END
			else
			{
				super.onAuthenticationSuccess(request, response, authentication);
			}
		}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param name
	 */
	private void isPasswordQuestionAnswerSet(final String userName)
	{

		final EnergizerB2BCustomerModel energizerB2BCustomerModel = defaultEnergizerPasswordExpiryFacade.getCustomerByUID(userName);

		if (!energizerB2BCustomerModel.getIsPasswordQuestionSet())
		{
			sessionService.setAttribute("quesAnsAlert", "account.password.isQuestionAnsSet");
		}

	}

	protected CartFacade getCartFacade()
	{
		return cartFacade;
	}

	@Required
	public void setCartFacade(final CartFacade cartFacade)
	{
		this.cartFacade = cartFacade;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected CustomerFacade getCustomerFacade()
	{
		return customerFacade;
	}

	@Required
	public void setCustomerFacade(final CustomerFacade customerFacade)
	{
		this.customerFacade = customerFacade;
	}

	/*
	 * @see org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler#
	 * isAlwaysUseDefaultTargetUrl()
	 */
	@Override
	protected boolean isAlwaysUseDefaultTargetUrl()
	{
		final UiExperienceLevel uiExperienceLevel = getUiExperienceService().getUiExperienceLevel();
		if (getForceDefaultTargetForUiExperienceLevel().containsKey(uiExperienceLevel))
		{
			return Boolean.TRUE.equals(getForceDefaultTargetForUiExperienceLevel().get(uiExperienceLevel));
		}
		else
		{
			return false;
		}
	}

	protected Map<UiExperienceLevel, Boolean> getForceDefaultTargetForUiExperienceLevel()
	{
		return forceDefaultTargetForUiExperienceLevel;
	}

	@Required
	public void setForceDefaultTargetForUiExperienceLevel(
			final Map<UiExperienceLevel, Boolean> forceDefaultTargetForUiExperienceLevel)
	{
		this.forceDefaultTargetForUiExperienceLevel = forceDefaultTargetForUiExperienceLevel;
	}


	protected BruteForceAttackCounter getBruteForceAttackCounter()
	{
		return bruteForceAttackCounter;
	}

	@Required
	public void setBruteForceAttackCounter(final BruteForceAttackCounter bruteForceAttackCounter)
	{
		this.bruteForceAttackCounter = bruteForceAttackCounter;
	}

	//Added Code changes for WeSell Implementation - START
	public void setSaleRepUsernameAndPasswordToCookie(final Authentication authentication, final HttpServletRequest request,
			final HttpServletResponse response)
	{
		final boolean isSalesRepUser = (boolean) getSessionService().getAttribute("isSalesRepUserLoggedIn");
		if (isSalesRepUser)
		{
			final User user = UserManager.getInstance().getUserByLogin(authentication.getName());
			final String encodedPassword = user.getEncodedPassword();
			final Cookie userName = new Cookie("logInUserName", authentication.getName());
			final Cookie pwd = new Cookie("logInPwd", encodedPassword);
			response.addCookie(userName);
			response.addCookie(pwd);
		}
		else
		{
			final Cookie[] cookies = request.getCookies();
			if (null != cookies)
			{
				for (final Cookie cookie : cookies)
				{
					if (null != cookie.getName() && cookie.getName().equalsIgnoreCase("logInUserName"))
					{
						final Cookie resetUserCookie = new Cookie("logInUserName", "");
						cookie.setMaxAge(0);
						response.addCookie(resetUserCookie);
					}
					if (null != cookie.getName() && cookie.getName().equalsIgnoreCase("logInPwd"))
					{
						final Cookie resetUserPwdCookie = new Cookie("logInPwd", "");
						cookie.setMaxAge(0);
						response.addCookie(resetUserPwdCookie);
					}
				}
			}
		}
	}
	//Added Code changes for WeSell Implementation - END
}
