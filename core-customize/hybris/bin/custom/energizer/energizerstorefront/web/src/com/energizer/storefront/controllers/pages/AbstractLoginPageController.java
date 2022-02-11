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

import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.user.data.TitleData;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.b2bemployee.EnergizerB2BEmployeeFacade;
import com.energizer.storefront.breadcrumb.Breadcrumb;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.LoginForm;


/**
 * Abstract base class for login page controllers
 */
public abstract class AbstractLoginPageController extends AbstractPageController
{
	protected static final String SPRING_SECURITY_LAST_USERNAME = "SPRING_SECURITY_LAST_USERNAME";

	private static final Logger LOG = Logger.getLogger(AbstractLoginPageController.class);

	@Resource
	private UserService userService;

	@Resource(name = "defaultB2BEmployeeFacade")
	private EnergizerB2BEmployeeFacade defaultB2BEmployeeFacade;
	@Resource
	private SessionService sessionService;

	@ModelAttribute("titles")
	public Collection<TitleData> getTitles()
	{
		return getUserFacade().getTitles();
	}

	protected abstract String getLoginView();

	protected abstract AbstractPageModel getLoginCmsPage() throws CMSItemNotFoundException;

	protected abstract String getSuccessRedirect(final HttpServletRequest request, final HttpServletResponse response);


	protected String getDefaultLoginPage(final boolean loginError, final HttpSession session, final Model model)
			throws CMSItemNotFoundException
	{
		try
		{
		final LoginForm loginForm = new LoginForm();
		model.addAttribute(loginForm);
		final String username = (String) session.getAttribute(SPRING_SECURITY_LAST_USERNAME);
		//Added Code changes for WeSell Implementation - START
		setSalesRepUserLoginForm(loginError, loginForm, username, model);
		//Added Code changes for WeSell Implementation - END
		if (username != null)
		{
			session.removeAttribute(SPRING_SECURITY_LAST_USERNAME);
		}

		storeCmsPageInModel(model, getLoginCmsPage());
		setUpMetaDataForContentPage(model, (ContentPageModel) getLoginCmsPage());
		model.addAttribute("metaRobots", "index,no-follow");
		model.addAttribute("contentPageId", getLoginCmsPage().getUid());
		final Breadcrumb loginBreadcrumbEntry = new Breadcrumb("#",
				getMessageSource().getMessage("header.link.login", null, getI18nService().getCurrentLocale()), null);
		model.addAttribute("breadcrumbs", Collections.singletonList(loginBreadcrumbEntry));

		/*
		 * if (loginError) { GlobalMessages.addErrorMessage(model, "login.error.account.not.found.title"); }
		 */

		return getLoginView();
		}
		catch (final Exception e)
		{
			GlobalMessages.addErrorMessage(model, "SalesRepUser and Distributor mapping is incorrect.");
			e.printStackTrace();
			return getLoginView();
		}
	}

	//Added Code changes for WeSell Implementation - START
	public void setSalesRepUserLoginForm(final boolean loginError, final LoginForm loginForm, String username, final Model model)
	{
		try
		{
		String password = null;
		final List<EnergizerB2BUnitModel> energizerB2BUnitList = null;
		final Set<EnergizerB2BUnitModel> energizerB2BUnitSet = new HashSet<EnergizerB2BUnitModel>();

		EnergizerB2BEmployeeModel b2bEmployee = null;
		Map<String, String> distributorMap = new TreeMap<String, String>();

		if (loginError && null != username)
		{
			/*
			 * final UserModel userModel = userService.getUserForUID(username.toLowerCase()); if (userModel instanceof
			 * EnergizerB2BEmployeeModel) { final EnergizerB2BEmployeeModel b2bEmployee = (EnergizerB2BEmployeeModel)
			 * userModel; energizerB2BUnitList = defaultB2BEmployeeFacade.getEnergizerB2BUnitList(b2bEmployee.getUid()); }
			 */
			final UserModel userModel = userService.getUserForUID(username.toLowerCase());
			if (null != userModel && userModel instanceof EnergizerB2BEmployeeModel)
			{
				b2bEmployee = (EnergizerB2BEmployeeModel) userService.getUserForUID(username.trim().toLowerCase());
				distributorMap = addSalesRepCustomersToMap(distributorMap, b2bEmployee);
			}
		}
		else
		{
			final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			final Cookie[] cookies = request.getCookies();
			if (null != cookies)
			{
				for (final Cookie cookie : cookies)
				{
					if (null != cookie.getName() && null != cookie.getValue() && cookie.getName().equalsIgnoreCase("logInUserName")
							&& StringUtils.isNotEmpty(cookie.getValue()))
					{
						final UserModel userModel = userService.getUserForUID(cookie.getValue().toLowerCase());
						if (userModel instanceof EnergizerB2BEmployeeModel)
						{
							final EnergizerB2BEmployeeModel b2bEmployeeFromCookie = (EnergizerB2BEmployeeModel) userModel;
							//energizerB2BUnitList = defaultB2BEmployeeFacade.getEnergizerB2BUnitList(b2bEmployee.getUid());
							distributorMap = addSalesRepCustomersToMap(distributorMap, b2bEmployeeFromCookie);
							username = cookie.getValue();
						}

					}
					if (null != cookie.getName() && null != cookie.getValue() && cookie.getName().equalsIgnoreCase("logInPwd")
							&& StringUtils.isNotEmpty(cookie.getValue()))
					{

						password = cookie.getValue();

					}
				}
			}
		}

		model.addAttribute("b2bUnitList", energizerB2BUnitList);
		model.addAttribute("distributorMap", distributorMap);

		loginForm.setJ_username(username);
		loginForm.setJ_password(password);
		sessionService.removeAttribute("b2bunitID");
		sessionService.removeAttribute("salesRepLogin");
		sessionService.removeAttribute("selectedB2BCustomer");
		sessionService.removeAttribute("isSalesRepUserLoggedIn");
		sessionService.removeAttribute("salesRepEmployeeModel");
		sessionService.removeAttribute("selectedEmployee");
		}
		catch (final Exception e)
		{
			LOG.error("SalesRepUser / Distributor Maping Exception ::: ", e);
			throw e;
		}
	}

	public Map<String, String> addSalesRepCustomersToMap(final Map<String, String> distributorMap,
			final EnergizerB2BEmployeeModel b2bEmployee)
	{
		final Set<EnergizerB2BUnitModel> energizerB2BUnitSet = new HashSet<EnergizerB2BUnitModel>();

		if (null != b2bEmployee.getIsSalesRep() && b2bEmployee.getIsSalesRep() && null != b2bEmployee.getActive()
				&& b2bEmployee.getActive())
		{
			// Adding B2B Unit Set for Logged in Sales Rep
			energizerB2BUnitSet.addAll(b2bEmployee.getB2bUnitList());

			LOG.info("Adding B2B Unit Set for the logged in sales rep to the distributorMap ...");

			if (null != energizerB2BUnitSet && !energizerB2BUnitSet.isEmpty() && null != b2bEmployee)
			{
				for (final EnergizerB2BUnitModel energizerB2BUnitModel : energizerB2BUnitSet)
				{
					distributorMap.put(b2bEmployee.getName().concat(" - ").concat(energizerB2BUnitModel.getName()),
							b2bEmployee.getUid().concat("&").concat(energizerB2BUnitModel.getUid()));
				}
			}

			LOG.info("distributorMap size ::: " + (null != distributorMap ? distributorMap.size() : "map null ..."));
			//LOG.info("distributorMap ::: " + distributorMap);

			final Set<EnergizerB2BEmployeeModel> workingForSet = b2bEmployee.getWorkingForList();

			if (null != workingForSet && !workingForSet.isEmpty())
			{
				LOG.info(b2bEmployee.getName() + " working for '" + workingForSet.size() + "' sales Rep(s) as a backup !!");
				LOG.info("Adding customers of the workingForSet to the distributorMap ...");
				for (final EnergizerB2BEmployeeModel workingFor : workingForSet)
				{
					if (null != workingFor.getIsSalesRep() && workingFor.getIsSalesRep() && null != workingFor.getActive()
							&& workingFor.getActive())
					{
						LOG.info(b2bEmployee.getName() + " working for '" + workingFor.getName() + "' as a backup !!");

						if (null != workingFor.getB2bUnitList() && !workingFor.getB2bUnitList().isEmpty())
						{
							for (final EnergizerB2BUnitModel energizerB2BUnitModel : workingFor.getB2bUnitList())
							{
								distributorMap.put(workingFor.getName().concat(" - ").concat(energizerB2BUnitModel.getName()),
										workingFor.getUid().concat("&").concat(energizerB2BUnitModel.getUid()));
							}
							energizerB2BUnitSet.addAll(workingFor.getB2bUnitList());
						}
					}
					else
					{
						LOG.info(workingFor.getName() + " is either not a sales rep or inactive ...");
					}

				}

				LOG.info("Added customers of the workingForSet to the distributorMap ...");
				LOG.info("distributorMap size ::: " + (null != distributorMap ? distributorMap.size() : "map null ..."));
			}

			if (null != distributorMap && !distributorMap.isEmpty() && !distributorMap.entrySet().isEmpty())
			{
				for (final Map.Entry<String, String> entry : distributorMap.entrySet())
				{
					System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
				}
			}
		}
		else
		{
			LOG.info(b2bEmployee.getName() + " is either not a sales rep or inactive ...");
		}
		return distributorMap;
	}
	//Added Code changes for WeSell Implementation - END
}
