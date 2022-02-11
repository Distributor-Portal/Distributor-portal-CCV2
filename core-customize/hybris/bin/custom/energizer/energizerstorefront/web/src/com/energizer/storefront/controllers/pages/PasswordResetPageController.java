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

import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.b2bacceleratorfacades.company.CompanyB2BCommerceFacade;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.core.datafeed.facade.impl.DefaultEnergizerPasswordGenerateFacade;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.facades.accounts.impl.DefaultEnergizerB2BPasswordQuestionsFacade;
import com.energizer.facades.accounts.impl.DefaultEnergizerCompanyB2BCommerceFacade;
import com.energizer.facades.accounts.impl.DefaultEnergizerCustomerFacade;
import com.energizer.facades.accounts.populators.EnergizerB2BCustomerReversePopulator;
import com.energizer.services.b2bemployee.impl.DefaultEnergizerB2BEmployeeService;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.ConfirmAccountForm;
import com.energizer.storefront.forms.ForgottenPwdForm;
import com.energizer.storefront.forms.ResetPwdForm;
import com.energizer.storefront.forms.UnlockAccountForm;
import com.energizer.storefront.forms.UpdatePwdForm;


/**
 * Controller for the forgotten password pages. Supports requesting a password reset email as well as changing the
 * password once you have got the token that was sent via email.
 */
@Controller
@Scope("tenant")
@RequestMapping(value = "/login/pw")
public class PasswordResetPageController extends AbstractPageController
{
	private static final Logger LOG = Logger.getLogger(PasswordResetPageController.class);

	private static final String REDIRECT_LOGIN = "redirect:/login";
	private static final String REDIRECT_HOME = "redirect:/";

	private static final String UPDATE_PWD_CMS_PAGE = "updatePassword";
	private static final String UNLOCK_ACCOUNT_CMS_PAGE = "unlockAccount";
	private static final String FORGOTTEN_PASSWORD_EXP_VALUE = "forgottenPassword.emailContext.expiresInMinutes";
	private static final String EXP_IN_SECONDS = "forgottenPassword.emailContext.expiresInSeconds";

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "b2bCommerceFacade")
	protected CompanyB2BCommerceFacade companyB2BCommerceFacade;

	@Resource(name = "defaultEnergizerCompanyB2BCommerceFacade")
	protected DefaultEnergizerCompanyB2BCommerceFacade defaultEnergizerCompanyB2BCommerceFacade;

	@Resource(name = "b2bCustomerFacade")
	protected CustomerFacade customerFacade;

	@Resource(name = "defaultB2BUnitService")
	private B2BUnitService defaultB2BUnitService;

	@Resource(name = "defaultEnergizerCustomerReversePopulator")
	protected EnergizerB2BCustomerReversePopulator energizerReversePopulator;

	@Resource(name = "defaultEnergizerB2BPasswordQuestionsFacade")
	private DefaultEnergizerB2BPasswordQuestionsFacade passwordQuestionsFacade;

	@Resource(name = "defaultEnergizerPasswordGenerateFacade")
	private DefaultEnergizerPasswordGenerateFacade defaultEnergizerPasswordGenerateFacade;

	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
	@Resource
	private UserService userService;

	@Resource
	private DefaultEnergizerCustomerFacade defaultEnergizerCustomerFacade;

	@Resource(name = "defaultEnergizerB2BEmployeeService")
	private DefaultEnergizerB2BEmployeeService defaultEnergizerB2BEmployeeService;


	/**
	 * @return the defaultEnergizerCustomerFacade
	 */
	public DefaultEnergizerCustomerFacade getDefaultEnergizerCustomerFacade()
	{
		return defaultEnergizerCustomerFacade;
	}

	/**
	 * @param defaultEnergizerCustomerFacade
	 *           the defaultEnergizerCustomerFacade to set
	 */
	public void setDefaultEnergizerCustomerFacade(final DefaultEnergizerCustomerFacade defaultEnergizerCustomerFacade)
	{
		this.defaultEnergizerCustomerFacade = defaultEnergizerCustomerFacade;
	}

	//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat

	@RequestMapping(value = "/request", method = RequestMethod.GET)
	public String getPasswordRequest(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute(new ForgottenPwdForm());

		return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPopup;
	}

	@RequestMapping(value = "/request", method = RequestMethod.POST)
	public String passwordRequest(@Valid
	final ForgottenPwdForm form, final BindingResult bindingResult) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPopup;
		}
		else
		{
			try
			{
				getCustomerFacade().forgottenPassword(form.getEmail());
			}
			catch (final UnknownIdentifierException unknownIdentifierException)
			{
				LOG.warn("Email: " + form.getEmail() + " does not exist in the database.");
			}
			return ControllerConstants.Views.Fragments.Password.ForgotPasswordValidationMessage;
		}
	}


	@RequestMapping(value = "/reset-password", method = RequestMethod.GET)
	public String getPasswordResetPage(final Model model, @RequestParam(required = false)
	final String uid) throws CMSItemNotFoundException
	{
		final ResetPwdForm resetPwdForm = new ResetPwdForm();
		if (null != uid)
		{
			resetPwdForm.setEmail(uid);
		}
		//model.addAttribute("passwordQuestionsList", passwordQuestionsFacade.getEnergizerPasswordQuestions());
		model.addAttribute(resetPwdForm);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));
		return ControllerConstants.Views.Fragments.Password.PasswordResetPage;
	}


	@RequestMapping(value = "/reset-password", method = RequestMethod.POST)
	public String passwordResetPage(@Valid
	final ResetPwdForm resetPwdForm, final BindingResult bindingResult, final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute(resetPwdForm);
		final String forgottenPassExpValue = Config.getParameter(FORGOTTEN_PASSWORD_EXP_VALUE);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));

		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "form.global.error");
			model.addAttribute(resetPwdForm);
			storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
			model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));
			return ControllerConstants.Views.Fragments.Password.PasswordResetPage;
		}
		else
		{

			try
			{
				/*
				 * final EnergizerB2BCustomerModel existCustomerModel = defaultEnergizerPasswordGenerateFacade
				 * .getCustomerByUID(resetPwdForm.getEmail().toLowerCase()); if (existCustomerModel != null) {
				 * getCustomerFacade().forgottenPassword(resetPwdForm.getEmail());
				 * GlobalMessages.addForgotPwdConfMessage(model, GlobalMessages.FORGOT_PWD_CONF_MESSAGES,
				 * "account.confirmation.forgotten.password.link.sent", new Object[] { forgottenPassExpValue });
				 * model.addAttribute(new ForgottenPwdForm()); }
				 *
				 * else { GlobalMessages.addErrorMessage(model, "password.reset.invalidEmailId");
				 *
				 * }
				 */
				EnergizerB2BCustomerModel existCustomerModel = null;
				EnergizerB2BEmployeeModel existEmployeeModel = null;

				final UserModel userModel = userService.getUserForUID(resetPwdForm.getEmail().trim().toLowerCase());
				if (userModel == null)
				{
					GlobalMessages.addErrorMessage(model, "password.reset.invalidEmailId");

				}
				else
				{
					if (userModel instanceof EnergizerB2BCustomerModel)
					{
						existCustomerModel = (EnergizerB2BCustomerModel) userModel;
						getCustomerFacade().forgottenPassword(resetPwdForm.getEmail());
						LOG.info("******* Sending email to reset password for the new customer account *******");
					}
					else if (userModel instanceof EnergizerB2BEmployeeModel)
					{
						existEmployeeModel = (EnergizerB2BEmployeeModel) userModel;
						getDefaultEnergizerCustomerFacade().employeeForgottenPassword(resetPwdForm.getEmail());
						LOG.info("******* Sending email to reset password for the new employee account *******");
					}
					GlobalMessages.addForgotPwdConfMessage(model, GlobalMessages.FORGOT_PWD_CONF_MESSAGES,
							"account.confirmation.forgotten.password.link.sent", new Object[]
							{ forgottenPassExpValue });

				}
			}

			catch (final Exception e)
			{

				GlobalMessages.addForgotPwdConfMessage(model, GlobalMessages.FORGOT_PWD_CONF_MESSAGES,
						"account.confirmation.forgotten.password.link.sent", new Object[]
						{ forgottenPassExpValue });
				model.addAttribute(new ForgottenPwdForm());
				LOG.info("User doesnot exists for " + resetPwdForm.getEmail());
			}

		}


		return ControllerConstants.Views.Fragments.Password.PasswordResetPage;
	}


	@RequestMapping(value = "/request-page", method = RequestMethod.GET)
	public String getPasswordRequestPage(final Model model, @RequestParam(required = false)
	final String uid) throws CMSItemNotFoundException
	{
		final ForgottenPwdForm forgottenPwdForm = new ForgottenPwdForm();
		if (null != uid)
		{
			forgottenPwdForm.setEmail(uid);
		}
		//model.addAttribute("passwordQuestionsList", passwordQuestionsFacade.getEnergizerPasswordQuestions());
		model.addAttribute(forgottenPwdForm);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));
		return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;
	}

	@RequestMapping(value = "/request-page", method = RequestMethod.POST)
	public String passwordRequestPage(@Valid
	final ForgottenPwdForm forgottenPwdForm, final BindingResult bindingResult, final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute(forgottenPwdForm);
		model.addAttribute("passwordQuestionsList", passwordQuestionsFacade.getEnergizerPasswordQuestions());
		final String forgottenPassExpValue = Config.getParameter(FORGOTTEN_PASSWORD_EXP_VALUE);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));


		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "form.global.error");
			LOG.info("Error on same page");
			return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;
		}
		else
		{
			//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
			/*
			 * try { getCustomerFacade().forgottenPassword(forgottenPwdForm.getEmail());
			 * GlobalMessages.addForgotPwdConfMessage(model, GlobalMessages.FORGOT_PWD_CONF_MESSAGES,
			 * "account.confirmation.forgotten.password.link.sent", new Object[] { forgottenPassExpValue });
			 * model.addAttribute(new ForgottenPwdForm()); }
			 */
			try
			{
				/*
				 * final EnergizerB2BCustomerModel existCustomerModel = defaultEnergizerPasswordGenerateFacade
				 * .getCustomerByUID(forgottenPwdForm.getEmail().toLowerCase());
				 */
				EnergizerB2BCustomerModel existCustomerModel = null;
				EnergizerB2BEmployeeModel existEmployeeModel = null;

				final UserModel userModel = userService.getUserForUID(forgottenPwdForm.getEmail().trim().toLowerCase());
				if (userModel == null)
				{
					GlobalMessages.addErrorMessage(model, "password.reset.invalidEmailId");

				}
				else
				{
					if (userModel instanceof EnergizerB2BCustomerModel)
					{
						existCustomerModel = (EnergizerB2BCustomerModel) userModel;
						getCustomerFacade().forgottenPassword(forgottenPwdForm.getEmail());
						LOG.info("******* Sending email with a token for password reset request for customer *******");
					}
					else if (userModel instanceof EnergizerB2BEmployeeModel)
					{
						existEmployeeModel = (EnergizerB2BEmployeeModel) userModel;
						getDefaultEnergizerCustomerFacade().employeeForgottenPassword(forgottenPwdForm.getEmail());
						LOG.info("******* Sending email with a token for password reset request for employee *******");
					}

					/*
					 * final String customerPasswordQuestion = existCustomerModel.getPasswordQuestion(); final String
					 * customerPasswordAnswer = existCustomerModel.getPasswordAnswer(); final String formPasswordQuestion =
					 * forgottenPwdForm.getPasswordQuestion(); final String formPasswordAnswer =
					 * forgottenPwdForm.getPasswordAnswer();
					 *
					 * if (customerPasswordQuestion == null && customerPasswordAnswer == null) {
					 * GlobalMessages.addErrorMessage(model, "password.question.noSet");
					 *
					 *
					 * } else if (customerPasswordQuestion.equalsIgnoreCase(formPasswordQuestion) &&
					 * customerPasswordAnswer.equalsIgnoreCase(formPasswordAnswer)) {
					 */
					//	WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
					GlobalMessages.addForgotPwdConfMessage(model, GlobalMessages.FORGOT_PWD_CONF_MESSAGES,
							"account.confirmation.forgotten.password.link.sent", new Object[]
							{ forgottenPassExpValue });

					/*
					 * } else { GlobalMessages.addErrorMessage(model, "password.questionOranswer.donotMatch");
					 *
					 *
					 * }
					 */
				}
			}
			catch (final UnknownIdentifierException unknownIdentifierException)
			{
				GlobalMessages.addErrorMessage(model, "user doesnot exist");
				model.addAttribute(new ForgottenPwdForm());
				return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;

			}
			return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;
		}

	}

	@RequestMapping(value = "/change", method = RequestMethod.GET)
	public String getChangePassword(@RequestParam(required = false)
	final String token, final Model model) throws CMSItemNotFoundException
	{
		if (StringUtils.isBlank(token))
		{
			return REDIRECT_HOME;
		}
		final UpdatePwdForm form = new UpdatePwdForm();
		form.setToken(token);
		model.addAttribute(form);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("updatePwd.title"));
		return ControllerConstants.Views.Pages.Password.PasswordResetChangePage;
	}

	@RequestMapping(value = "/change", method = RequestMethod.POST)
	public String changePassword(@Valid
	final UpdatePwdForm form, final BindingResult bindingResult, final Model model, final RedirectAttributes redirectModel)
			throws CMSItemNotFoundException
	{
		LOG.info("Inside /change - changePassword method ... ");

		if (bindingResult.hasErrors())
		{
			prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE);
			return ControllerConstants.Views.Pages.Password.PasswordResetChangePage;
		}
		if (!StringUtils.isBlank(form.getToken()))
		{
			try
			{
				boolean pwdUpdated = false;
				//				WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
				LOG.info("The password link expriy time in seconds :"
						+ configurationService.getConfiguration().getLong(EXP_IN_SECONDS, 1800));


				final EnergizerB2BCustomerModel existCustomerModel = defaultEnergizerPasswordGenerateFacade
						.getCustomerBySecureToken(form.getToken());
				final EnergizerB2BEmployeeModel existEmployeeModel = defaultEnergizerPasswordGenerateFacade
						.getEmployeeBySecureToken(form.getToken());

				if (null != existEmployeeModel)
				{
					getDefaultEnergizerCustomerFacade().employeeUpdatePassword(form.getToken(), form.getPwd());
					LOG.info("Password updated successfully for the employee '" + existEmployeeModel.getUid() + "' !!");
					pwdUpdated = true;
				}
				else if (null != existCustomerModel)
				{
					getCustomerFacade().updatePassword(form.getToken(), form.getPwd());
					LOG.info("Password updated successfully for the customer '" + existCustomerModel.getUid() + "' !!");
					//Write the custom login for employee
				}
				else
				{
					pwdUpdated = false;
					LOG.info("No user model found with the given token (or) the token might have expired !!");
					GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
							"account.password.token.empty");
				}
				/*
				 * final boolean flag = defaultEnergizerCompanyB2BCommerceFacade.updatingPassword(form.getPwd(),
				 * form.getToken()); if (!flag) { bindingResult.rejectValue("pwd", "profile.newPassword.match", new Object[]
				 * {}, "profile.newPassword.match"); }
				 *
				 * if (bindingResult.hasErrors()) { prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE); return
				 * ControllerConstants.Views.Pages.Password.PasswordResetChangePage; }
				 */
				//				WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
				if (pwdUpdated)
				{
					GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
							"account.confirmation.password.updated");
				}
				//adding a session attribute that will be removed in the StorefrontAuthenticationSuccessHandler.java once the successful login happens
				getSessionService().setAttribute(JUST_UPDATED_PWD, JUST_UPDATED_PWD);
			}
			catch (final TokenInvalidatedException e)
			{
				LOG.error("update passwoed failed due to, " + e.getMessage(), e);
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "updatePwd.token.invalidated");
			}
			catch (final RuntimeException e)
			{
				LOG.error("update passwoed failed due to, " + e.getMessage(), e);
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "updatePwd.token.invalid");
			}
		}
		LOG.info("Exiting /change - changePassword method ... ");
		return REDIRECT_LOGIN;
	}

	/**
	 * Prepares the view to display an error message
	 *
	 * @param model
	 * @param page
	 * @throws CMSItemNotFoundException
	 */
	protected void prepareErrorMessage(final Model model, final String page) throws CMSItemNotFoundException
	{
		GlobalMessages.addErrorMessage(model, "form.global.error");
		storeCmsPageInModel(model, getContentPageForLabelOrId(page));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(page));
	}

	@RequestMapping(value = "/unlock-account", method = RequestMethod.GET)
	public String getUnlockAccountRequestPage(final Model model, @RequestParam(required = false)
	final String uid) throws CMSItemNotFoundException
	{
		final UnlockAccountForm unlockAccountForm = new UnlockAccountForm();
		if (null != uid)
		{
			unlockAccountForm.setEmail(uid);
		}
		model.addAttribute(unlockAccountForm);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UNLOCK_ACCOUNT_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UNLOCK_ACCOUNT_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("unlockAccount.title"));
		return ControllerConstants.Views.Fragments.Account.UnlockAccountRequestPage;
	}

	@RequestMapping(value = "/unlock-account", method = RequestMethod.POST)
	public String unlockAccountRequestPage(@Valid
	final UnlockAccountForm unlockAccountForm, final BindingResult bindingResult, final Model model)
			throws CMSItemNotFoundException
	{
		try
		{
			model.addAttribute(unlockAccountForm);
			final String unlockAccountExpValue = Config.getParameter(FORGOTTEN_PASSWORD_EXP_VALUE);
			storeCmsPageInModel(model, getContentPageForLabelOrId(UNLOCK_ACCOUNT_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UNLOCK_ACCOUNT_CMS_PAGE));
			model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("unlockAccount.title"));


			if (bindingResult.hasErrors())
			{
				GlobalMessages.addErrorMessage(model, "form.global.error");
				LOG.info("Error on same page");
				return ControllerConstants.Views.Fragments.Account.UnlockAccountRequestPage;
			}
			else
			{
				try
				{
					//					WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
					EnergizerB2BCustomerModel existCustomerModel = null;
					EnergizerB2BEmployeeModel existEmployeeModel = null;

					final UserModel userModel = userService.getUserForUID(unlockAccountForm.getEmail().trim().toLowerCase());
					if (userModel == null)
					{
						GlobalMessages.addErrorMessage(model, "password.reset.invalidEmailId");

					}
					else
					{
						if (userModel instanceof EnergizerB2BCustomerModel)
						{
							existCustomerModel = (EnergizerB2BCustomerModel) userModel;
							defaultEnergizerCompanyB2BCommerceFacade.sendUnlockAccountEmail(unlockAccountForm.getEmail());
							LOG.info("******* Sending email with a token for unlock account request for customer*******");
							//defaultEnergizerPasswordGenerateFacade.getCustomerByUID(unlockAccountForm.getEmail().toLowerCase());
						}
						else if (userModel instanceof EnergizerB2BEmployeeModel)
						{
							//defaultEnergizerPasswordGenerateFacade.getEmployeeByUID(unlockAccountForm.getEmail().toLowerCase());
							existEmployeeModel = (EnergizerB2BEmployeeModel) userModel;
							defaultEnergizerCustomerFacade.sendEmployeeUnlockAccountEmail(unlockAccountForm.getEmail());
							LOG.info("******* Sending email with a token for unlock account request for employee *******");
						}

						GlobalMessages.addForgotPwdConfMessage(model, GlobalMessages.FORGOT_PWD_CONF_MESSAGES,
								"account.confirmation.unlock.account.link.sent", new Object[]
								{ unlockAccountExpValue });

					}
				}
				//				WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
				catch (final UnknownIdentifierException unknownIdentifierException)
				{
					GlobalMessages.addErrorMessage(model, "user does not exist");
					model.addAttribute(new ForgottenPwdForm());
					return ControllerConstants.Views.Fragments.Account.UnlockAccountRequestPage;

				}
				return ControllerConstants.Views.Fragments.Account.UnlockAccountRequestPage;
			}
		}
		catch (final NullPointerException nullEx)
		{
			LOG.info("NullPointerException occured :::: " + nullEx.getMessage());
			nullEx.printStackTrace();
		}
		catch (final ArrayIndexOutOfBoundsException arrayEx)
		{
			LOG.info("ArrayIndexOutOfBoundsException occured :::: " + arrayEx.getMessage());
			arrayEx.printStackTrace();
		}
		catch (final Exception ex)
		{
			LOG.info("Exception occured :::: " + ex.getMessage());
			ex.printStackTrace();
		}
		return ControllerConstants.Views.Fragments.Account.UnlockAccountRequestPage;
	}

	@RequestMapping(value = "/confirm-account", method = RequestMethod.GET)
	public String getConfirmAccountPage(@RequestParam(required = false)
	final String token, final Model model) throws CMSItemNotFoundException
	{
		try
		{
			if (StringUtils.isBlank(token))
			{
				return REDIRECT_HOME;
			}
			//			WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
			final EnergizerB2BCustomerModel existCustomerModel = defaultEnergizerPasswordGenerateFacade
					.getCustomerBySecureToken(token);
			final EnergizerB2BEmployeeModel existEmployeeModel = defaultEnergizerPasswordGenerateFacade
					.getEmployeeBySecureToken(token);
			if (null == existCustomerModel && null == existEmployeeModel)
			{
				return REDIRECT_HOME;
			}

			else if (existEmployeeModel != null)
			{
				final ConfirmAccountForm confirmAccountForm = new ConfirmAccountForm();
				confirmAccountForm.setToken(token);
				confirmAccountForm.setEmail(existEmployeeModel.getUid());
				model.addAttribute(confirmAccountForm);
				storeCmsPageInModel(model, getContentPageForLabelOrId(UNLOCK_ACCOUNT_CMS_PAGE));
				setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UNLOCK_ACCOUNT_CMS_PAGE));
				model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("confirmAccount.title"));
				return ControllerConstants.Views.Fragments.Account.confirmAccountRequestPage;
			}
			else
			//				WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
			if (existCustomerModel != null)
			{
				final ConfirmAccountForm confirmAccountForm = new ConfirmAccountForm();
				confirmAccountForm.setToken(token);
				confirmAccountForm.setEmail(existCustomerModel.getUid());
				model.addAttribute(confirmAccountForm);
				storeCmsPageInModel(model, getContentPageForLabelOrId(UNLOCK_ACCOUNT_CMS_PAGE));
				setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UNLOCK_ACCOUNT_CMS_PAGE));
				model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("confirmAccount.title"));
				return ControllerConstants.Views.Fragments.Account.confirmAccountRequestPage;
			}

		}
		catch (final NullPointerException nullEx)
		{
			LOG.info("NullPointerException occured :::: " + nullEx.getMessage());
			nullEx.printStackTrace();
		}
		catch (final ArrayIndexOutOfBoundsException arrayEx)
		{
			LOG.info("ArrayIndexOutOfBoundsException occured :::: " + arrayEx.getMessage());
			arrayEx.printStackTrace();
		}
		catch (final Exception ex)
		{
			LOG.info("Exception occured :::: " + ex.getMessage());
			ex.printStackTrace();
		}
		return ControllerConstants.Views.Fragments.Account.confirmAccountRequestPage;
	}

	@RequestMapping(value = "/confirm-account", method = RequestMethod.POST)
	public String confirmAccount(@Valid
	final ConfirmAccountForm form, final BindingResult bindingResult, final Model model, final RedirectAttributes redirectModel)
			throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			prepareErrorMessage(model, UNLOCK_ACCOUNT_CMS_PAGE);
			return ControllerConstants.Views.Fragments.Account.confirmAccountRequestPage;
		}
		if (!StringUtils.isBlank(form.getToken()))
		{
			try
			{
				LOG.debug("The unlock account link expiry time in seconds :"
						+ configurationService.getConfiguration().getLong(EXP_IN_SECONDS, 1800));

				//				WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
				final EnergizerB2BCustomerModel existCustomerModel = defaultEnergizerPasswordGenerateFacade
						.getCustomerBySecureToken(form.getToken());
				final EnergizerB2BEmployeeModel existEmployeeModel = defaultEnergizerPasswordGenerateFacade
						.getEmployeeBySecureToken(form.getToken());

				if (null != existCustomerModel)
				{
					final boolean isLoginDisabled = defaultEnergizerCompanyB2BCommerceFacade.unlockCustomerAccount(form.getEmail());

					if (isLoginDisabled)
					{
						LOG.info("******* There is a problem unlocking the customer account ******* ");
						//bindingResult.rejectValue("email", "profile.newPassword.match", new Object[] {}, "profile.newPassword.match");
					}
					else
					{
						LOG.info("******* The customer account has been unlocked successfully !!! ******* ");
					}
				}
				else if (null != existEmployeeModel)
				{
					final boolean isLoginDisabled = defaultEnergizerCustomerFacade.unlockEmployeeAccount(form.getEmail());
					if (isLoginDisabled)
					{
						LOG.info("******* There is a problem unlocking the Employee account ******* ");
						//bindingResult.rejectValue("email", "profile.newPassword.match", new Object[] {}, "profile.newPassword.match");
					}
					else
					{
						LOG.info("******* The Employee account has been unlocked successfully !!! ******* ");
					}
				}
				else
				{
					LOG.info("******* The secure token has expired, unable to unlock the customer account !!! ******* ");
				}
				//				WeSell Implementation -  Added Code Changes for Forgot Password/Unlock Account for Sales Rep login - by Venkat
				if (bindingResult.hasErrors())
				{
					prepareErrorMessage(model, UNLOCK_ACCOUNT_CMS_PAGE);
					return ControllerConstants.Views.Fragments.Account.confirmAccountRequestPage;
				}

				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
						"account.confirmation.account.unlocked");
				//adding a session attribute that will be removed in the StorefrontAuthenticationSuccessHandler.java once the successful login happens
				getSessionService().setAttribute(JUST_UNLOCKED_ACCOUNT, JUST_UNLOCKED_ACCOUNT);
			}
			catch (final UnknownIdentifierException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Unlock account failed due to, " + e.getMessage(), e);
				}
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
						"unlockAccount.token.invalidated");
			}
			catch (final RuntimeException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Unlock account failed due to, " + e.getMessage(), e);
				}
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
						"unlockAccount.token.invalidated");
			}
		}
		return REDIRECT_LOGIN;
	}
}
