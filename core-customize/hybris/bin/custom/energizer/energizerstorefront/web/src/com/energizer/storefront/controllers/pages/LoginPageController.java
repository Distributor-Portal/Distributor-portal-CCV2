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
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.storesession.StoreSessionFacade;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.b2bemployee.EnergizerB2BEmployeeFacade;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.util.EnergizerPasswordNotificationUtil;


/**
 * Login Controller. Handles login and register for the account flow.
 */
@Controller
@Scope("tenant")
@RequestMapping(value = "/login")
public class LoginPageController extends AbstractLoginPageController
{

	private static final String LOGIN_URL = "/login";
	private static final String SLASH = "/";
	static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";

	private static final Logger LOG = Logger.getLogger(LoginPageController.class);

	@Resource(name = "httpSessionRequestCache")
	private HttpSessionRequestCache httpSessionRequestCache;

	@Resource
	private UserService userService;

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource(name = "b2bUnitService")
	private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService;

	@Resource(name = "storeSessionFacade")
	private StoreSessionFacade storeSessionFacade;

	@Resource(name = "userFacade")
	private UserFacade userFacade;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	private final static String FAILED_MAX_ATTEMPTS_TO_LOGIN = "FAILED_MAX_ATTEMPTS_TO_LOGIN";

	private final static String ACCOUNT_IS_BLOCKED = "login.maxattempts.failed";


	protected static final String SPRING_SECURITY_LAST_USERNAME = "SPRING_SECURITY_LAST_USERNAME";

	@Resource
	protected EnergizerPasswordNotificationUtil energizerPasswordNotificationUtil;

	@Resource(name = "defaultB2BEmployeeFacade")
	private EnergizerB2BEmployeeFacade defaultB2BEmployeeFacade;

	@Resource
	private SessionService sessionService;


	public void setHttpSessionRequestCache(final HttpSessionRequestCache accHttpSessionRequestCache)
	{
		this.httpSessionRequestCache = accHttpSessionRequestCache;
	}


	@RequestMapping(method =
	{ RequestMethod.POST, RequestMethod.GET })
	public String doLogin(@RequestHeader(value = "referer", required = false)
	final String referer, @RequestParam(value = "error", defaultValue = "false")
	final boolean loginError, final Model model, final HttpServletRequest request, final HttpServletResponse response,
			final HttpSession session) throws CMSItemNotFoundException, IOException, Exception
	{
		try
		{
			final UserModel user = userService.getCurrentUser();
			final boolean isUserAnonymous = user == null || userService.isAnonymousUser(user);
			List<String> notificationMessages = null;
			boolean accountBlockedMessageFlag = false;
			EnergizerB2BUnitModel unit = null;
			EnergizerB2BCustomerModel b2bcustomer = null;

			if (!isUserAnonymous)
			{
				return REDIRECT_PREFIX + ROOT;
			}

			final String userName = (String) session.getAttribute(SPRING_SECURITY_LAST_USERNAME);
			if (null != userName)
			{
				notificationMessages = energizerPasswordNotificationUtil.checkPasswordExpiryStatus(userName);
				if (null != notificationMessages && notificationMessages.size() > 0
						&& notificationMessages.get(0).equalsIgnoreCase("1"))
				{
					GlobalMessages.addErrorMessage(model, notificationMessages.get(1));

				}
			}
			//if (userName != null && StringUtils.isNotBlank(userName))  	GlobalMessages.addErrorMessage(model, "login.error.account.not.found.title");
			if (!loginError)
			{
				//Commented out to avoid newly signed in customer going to same page as other customer logged out in same browser
				//storeReferer(referer, request, response);
			}
			else
			{
				if (getSessionService().getAttribute(FAILED_MAX_ATTEMPTS_TO_LOGIN) != null
						&& getSessionService().getAttribute(FAILED_MAX_ATTEMPTS_TO_LOGIN).equals(FAILED_MAX_ATTEMPTS_TO_LOGIN))
				{
					GlobalMessages.addErrorMessage(model, ACCOUNT_IS_BLOCKED);
					accountBlockedMessageFlag = true;
					model.addAttribute(FAILED_MAX_ATTEMPTS_TO_LOGIN, FAILED_MAX_ATTEMPTS_TO_LOGIN);
					getSessionService().removeAttribute(FAILED_MAX_ATTEMPTS_TO_LOGIN);
				}

				notificationMessages = energizerPasswordNotificationUtil.checkPasswordExpiryStatus(userName);

				if (null != notificationMessages && notificationMessages.size() > 0
						&& notificationMessages.get(0).equalsIgnoreCase("1"))
				{
					GlobalMessages.addErrorMessage(model, notificationMessages.get(1));

				}

				try
				{
					if (null == userName || StringUtils.isEmpty(userName))
					{
						GlobalMessages.addErrorMessage(model, "login.error.username.empty.title");
					}
					final UserModel userModel = userService.getUserForUID(userName.toLowerCase());
					final CMSSiteModel site = cmsSiteService.getCurrentSite();
					if (userModel instanceof EnergizerB2BEmployeeModel)
					{
						try
						{
							final EnergizerB2BEmployeeModel b2bEmployee = (EnergizerB2BEmployeeModel) userService
									.getUserForUID(userName.toLowerCase());
							LOG.info("Site is ::: " + site.getUid() + " and the salesRepName is ::: " + b2bEmployee.getUid());

							final String b2bUnitID = (String) sessionService.getAttribute("b2bunitID");
							if (null == b2bUnitID || StringUtils.isEmpty(b2bUnitID))
							{
								GlobalMessages.addErrorMessage(model, "salesRep.user.distributor.select.option");
								model.addAttribute("distributorError", true);
							}

							else if ((b2bEmployee.getActive().equals(Boolean.FALSE) || b2bEmployee.isLoginDisabled())
									&& (!accountBlockedMessageFlag))
							{
								GlobalMessages.addErrorMessage(model, "login.error.account.disabled.title");
							}

							else
							{
								//GlobalMessages.addErrorMessage(model, "login.error.incorrect.password.title");
								GlobalMessages.addErrorMessage(model, "login.error.incorrect.password");
							}
						}
						catch (final Exception e)
						{
							model.addAttribute("distributorError", true);
							GlobalMessages.addErrorMessage(model, "salesRep.user.distributor.select.option");
							throw e;
						}
					}
					else
					{
						b2bcustomer = (EnergizerB2BCustomerModel) userService.getUserForUID(userName.toLowerCase());

						unit = (EnergizerB2BUnitModel) b2bUnitService.getParent(b2bcustomer);

						LOG.info("Site is ::: " + site.getUid() + " and the customer is ::: " + b2bcustomer.getUid());

						if ((b2bcustomer.getActive().equals(Boolean.FALSE) || unit.getActive().equals(Boolean.FALSE)
								|| b2bcustomer.isLoginDisabled()) && (!accountBlockedMessageFlag))
						{
							GlobalMessages.addErrorMessage(model, "login.error.account.disabled.title");
						}
						else if (!(b2bcustomer.getActive().equals(Boolean.FALSE) || unit.getActive().equals(Boolean.FALSE)
								|| b2bcustomer.isLoginDisabled()))
						{
							//GlobalMessages.addErrorMessage(model, "login.error.incorrect.password.title");
							GlobalMessages.addErrorMessage(model, "login.error.incorrect.password");
						}
						else
						{
							//GlobalMessages.addErrorMessage(model, "login.error.incorrect.password.title");
							GlobalMessages.addErrorMessage(model, "login.error.incorrect.password");
						}
					}

					/*
					 * if (null != unit && site != null) { final CountryModel userCountry = unit.getCountry();
					 *
					 * final String siteid = site.getUid();
					 *
					 * if (null == userCountry) { GlobalMessages.addErrorMessage(model, "login.error.incorrect.country"); }
					 * else if (siteid.equals(HOUSEHOLD_NA) &&
					 * unit.getIsLatamCatalog())//!USER_COUNTRY.contains(userCountry.getIsocode()) {
					 * GlobalMessages.addErrorMessage(model, "login.error.incorrect.site.us"); } else if
					 * (siteid.equals(HOUSEHOLD) &&
					 * !unit.getIsLatamCatalog())//USER_COUNTRY.contains(userCountry.getIsocode()) {
					 * GlobalMessages.addErrorMessage(model, "login.error.incorrect.site"); } else if
					 * (!b2bcustomer.getActive()) { GlobalMessages.addErrorMessage(model, "login.error.account.disabled"); }
					 * else { GlobalMessages.addErrorMessage(model, "login.error.account.incorrect"); } }
					 */
				}
				catch (final Exception e)
				{
					//GlobalMessages.addErrorMessage(model, "login.error.incorrect.password.title");
					GlobalMessages.addErrorMessage(model, "login.error.incorrect.password");
					e.printStackTrace();
					throw e;
				}

			}
		}
		catch (final Exception e)
		{
			//GlobalMessages.addErrorMessage(model, "Unable to login user credentials. Can you please give correct username/password");
			e.printStackTrace();
			return getDefaultLoginPage(loginError, session, model);
		}
		return getDefaultLoginPage(loginError, session, model);
	}

	@Override
	protected String getLoginView()
	{
		return ControllerConstants.Views.Pages.Account.AccountLoginPage;
	}

	@Override
	protected String getSuccessRedirect(final HttpServletRequest request, final HttpServletResponse response)
	{
		if (httpSessionRequestCache.getRequest(request, response) != null)
		{
			return httpSessionRequestCache.getRequest(request, response).getRedirectUrl();
		}

		return "/my-account";
	}

	@Override
	protected AbstractPageModel getLoginCmsPage() throws CMSItemNotFoundException
	{
		return getContentPageForLabelOrId("login");
	}

	protected void storeReferer(final String referer, final HttpServletRequest request, final HttpServletResponse response)
	{
		if (StringUtils.isNotBlank(referer))
		{
			httpSessionRequestCache.saveRequest(request, response);
		}
	}

	@RequestMapping(value = "/redirectSiteURL", method = RequestMethod.POST)
	public void redirectToRegionBasedSiteURL(@RequestParam("siteId")
	final String siteId, @RequestParam("currency")
	final String currency, @RequestParam("region")
	final String region, final HttpServletRequest request, final HttpServletResponse response, final Model model)
			throws IOException, CMSItemNotFoundException
	{
		LOG.info("************  Entering redirectToRegionBasedSiteURL method  **************  ");

		String redirectURL = StringUtils.EMPTY;
		try
		{
			redirectURL = getSiteURL(siteId, currency);

			if (this.getSessionService().hasCurrentSession())
			{
				this.getSessionService().closeCurrentSession();
			}
			//Initializing the CMS Site to null, so that we create a fresh CMS Site request based on dropdown region selection in the login page
			this.getCmsSiteService().setCurrentSite(null);

			final CMSSiteModel cmsSiteModel = this.getCmsSiteService().getSiteForURL(new URL(redirectURL));

			// Setting user session language to cms site default language for dropdown selection
			if (null != cmsSiteModel)
			{
				storeSessionFacade.setCurrentLanguage(cmsSiteModel.getDefaultLanguage().getIsocode());
				userFacade.syncSessionLanguage();
				LOG.info("********* Closing current session and initializing the CMS Site *********");
			}

			// Removing the already saved request
			removeRequest(request);

			LOG.info("***********  Redirecting to " + redirectURL + " ***********");
		}
		catch (final Exception ex)
		{
			LOG.info("Exception occured while redirecting to the redirectURL ::: " + ex.getMessage());
		}
		response.sendRedirect(redirectURL);
	}

	public String getSiteURL(final String siteId, final String currency)
	{

		LOG.info("**********  Entering getSiteURL method ********** ");

		final String PERSONALCARE = getConfigValue("site.personalCare");
		final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");
		final String PERSONALCARE_WEBSITE = getConfigValue("website.personalCare.https");
		final String PERSONALCARE_EMEA_WEBSITE = getConfigValue("website.personalCareEMEA.https");

		String redirectURL = StringUtils.EMPTY;

		if (siteId.equalsIgnoreCase(PERSONALCARE_EMEA))
		{
			redirectURL = PERSONALCARE_EMEA_WEBSITE + SLASH + currency + LOGIN_URL;
		}
		else if (siteId.equalsIgnoreCase(PERSONALCARE))
		{
			redirectURL = PERSONALCARE_WEBSITE + SLASH + currency + LOGIN_URL;
		}
		else
		{
			redirectURL = PERSONALCARE_WEBSITE + SLASH + currency + LOGIN_URL;
			LOG.info("There is no siteId information. So, we are redirecting to default LATAM Site --> " + redirectURL);
		}
		return redirectURL;
	}

	@Override
	public String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}

	public void removeRequest(final HttpServletRequest currentRequest)
	{
		final HttpSession session = currentRequest.getSession(false);

		if (session != null)
		{
			LOG.info("Removing DefaultSavedRequest from session if present");
			session.removeAttribute(SAVED_REQUEST);
		}
	}

	//Added Code changes for WeSell Implementation - START

	@ResponseBody
	@RequestMapping(value = "/isValidLoginUser", method =
	{ RequestMethod.GET, RequestMethod.POST }, produces = "application/json")
	public String isValidLoginUser(final HttpServletRequest request, final HttpServletResponse response)
	{
		sessionService.removeAttribute("b2bunitID");

		final String loginUser = request.getParameter("user");
		LOG.info("Checking if entered login user '" + loginUser + "'is valid --------");

		if (!StringUtils.isEmpty(loginUser))
		{
			UserModel userModel = null;
			try
			{
				userModel = userService.getUserForUID(loginUser.toLowerCase());
				if (null != userModel)
				{
					return "VALID_USER";
				}
				else
				{
					return "INVALID_USER";
				}
			}
			catch (final Exception e)
			{
				LOG.error("Not a valid login user ...");
				return "INVALID_USER";
			}
		}
		return "VALID_USER";
	}

	@ResponseBody
	@RequestMapping(value = "/getb2bunitlist", method =
	{ RequestMethod.GET, RequestMethod.POST }, produces = "application/json")
	public Map<String, String> getSalesRepB2BUnitList(final HttpServletRequest request, final HttpServletResponse response)
	{

		EnergizerB2BEmployeeModel b2bEmployee = null;
		Map<String, String> distributorMap = new TreeMap<String, String>();
		//Map<String, String> workingForMap = new TreeMap<String, String>();

		sessionService.removeAttribute("b2bunitID");

		LOG.info("getSalesRepB2BUnitList() method calling --------");
		final String loginUser = request.getParameter("user");
		LOG.info("salesRepuser ID---" + loginUser);
		if (!StringUtils.isEmpty(loginUser))
		{
			//energizerB2BUnitList = defaultB2BEmployeeFacade.getEnergizerB2BUnitList(loginUser.trim().toLowerCase());
			//final String b2bUnitID = (String) sessionService.getAttribute("b2bunitID");
			//LOG.info("we getting SalesRep B2BUnitID from Session----" + b2bUnitID);
			UserModel userModel = null;
			try
			{
				userModel = userService.getUserForUID(loginUser.toLowerCase());
				if (null != userModel && userModel instanceof EnergizerB2BEmployeeModel)
				{
					b2bEmployee = (EnergizerB2BEmployeeModel) userService.getUserForUID(loginUser.trim().toLowerCase());
					distributorMap = addSalesRepCustomersToMap(distributorMap, b2bEmployee);
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		return distributorMap;
	}

	@ResponseBody
	@RequestMapping(value = "/setb2bunitinsession", method =
	{ RequestMethod.GET, RequestMethod.POST }, produces = "application/json")
	public String setB2BUnitInSession(final HttpServletRequest request, final HttpServletResponse response)
	{
		final String b2bunitId = request.getParameter("b2bUnit");
		final String[] parts = b2bunitId.split("&");
		final String selectedEmployee = parts[0];
		final String selectedB2BUnit = parts[1];
		final String slaesRepUser = request.getParameter("salesRepUser");
		LOG.info("sales Distributor ID--" + selectedB2BUnit);
		LOG.info("SalesRepUser ID---" + slaesRepUser);
		if (!StringUtils.isEmpty(selectedB2BUnit))
		{
			sessionService.setAttribute("b2bunitID", selectedB2BUnit.trim());
			sessionService.setAttribute("selectedEmployee", selectedEmployee.trim());
		}

		return "Success";
	}
	//Added Code changes for WeSell Implementation - END
}
