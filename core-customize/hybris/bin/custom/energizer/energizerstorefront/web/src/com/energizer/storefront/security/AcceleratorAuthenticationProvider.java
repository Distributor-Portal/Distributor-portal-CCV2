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

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.core.Constants;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.JaloConnection;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.user.LoginToken;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.spring.security.CoreAuthenticationProvider;
import de.hybris.platform.spring.security.CoreUserDetails;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.wesell.virtualid.creation.EnergizerWesellVirtualID;
import com.energizer.storefront.util.EnergizerPasswordNotificationUtil;


/**
 * Derived authentication provider supporting additional authentication checks. See
 * {@link de.hybris.platform.spring.security.RejectUserPreAuthenticationChecks}.
 *
 * <ul>
 * <li>prevent login without password for users created via CSCockpit</li>
 * <li>prevent login as user in group admingroup</li>
 * <li>prevent login as user if not authorised for B2B</li>
 * <li>prevent login as user if not authorised for B2B</li>
 * </ul>
 *
 * any login as admin disables SearchRestrictions and therefore no page can be viewed correctly
 */
public class AcceleratorAuthenticationProvider extends CoreAuthenticationProvider
{
	private static final Logger LOG = Logger.getLogger(AcceleratorAuthenticationProvider.class);
	private static final String ROLE_ADMIN_GROUP = "ROLE_" + Constants.USER.ADMIN_USERGROUP.toUpperCase();
	private static final Object String = null;

	private BruteForceAttackCounter bruteForceAttackCounter;
	private UserService userService;
	private ModelService modelService;
	private GrantedAuthority adminAuthority = new SimpleGrantedAuthority(ROLE_ADMIN_GROUP);
	private CartService cartService;

	private B2BUserGroupProvider b2bUserGroupProvider;

	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;

	@Resource
	protected EnergizerPasswordNotificationUtil energizerPasswordNotificationUtil;

	@Resource
	private SessionService sessionService;

	@Resource
	private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource
	EnergizerWesellVirtualID energizerWesellVirtualID;

	/*-private final UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks(this,
			(DefaultPostAuthenticationChecks) null);*/

	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException,
			InsufficientAuthenticationException, BadCredentialsException, DisabledException, LockedException
	{
		try
		{
			final String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();

			// check if the user of the cart matches the current user and if the
			// user is not anonymous. If otherwise, remove delete the session cart as it might
			// be stolen / from another user
			final String sessionCartUserId = getCartService().getSessionCart().getUser().getUid();

			if (!username.equals(sessionCartUserId) && !sessionCartUserId.equals(userService.getAnonymousUser().getUid()))
			{
				getCartService().setSessionCart(null);
			}
			//Added Code changes for WeSell Implementation - START
			if (null != username && !username.equalsIgnoreCase("anonymous"))
			{
				final UserModel userModel = userService.getUserForUID(authentication.getName().toLowerCase());
				if (userModel instanceof EnergizerB2BEmployeeModel)
				{
					return salesRepUserAuthenticate(authentication);
				}
			}
			//Added Code changes for WeSell Implementation - END
			return super.authenticate(authentication);
		}
		catch (final InsufficientAuthenticationException iae)
		{
			throw iae;
		}
		catch (final BadCredentialsException bce)
		{
			throw bce;
		}
		catch (final DisabledException de)
		{
			throw de;
		}
		catch (final LockedException le)
		{
			throw le;
		}
		catch (final AuthenticationException ae)
		{
			throw ae;
		}
		catch (final Exception e)
		{
			throw e;
		}
	}

	/**
	 * @see de.hybris.platform.spring.security.CoreAuthenticationProvider#additionalAuthenticationChecks(org.springframework.security.core.userdetails.UserDetails,
	 *      org.springframework.security.authentication.AbstractAuthenticationToken)
	 */
	@Override
	protected void additionalAuthenticationChecks(final UserDetails details, final AbstractAuthenticationToken authentication)
			throws AuthenticationException, InsufficientAuthenticationException, BadCredentialsException, DisabledException,
			LockedException
	{
		try
		{
			super.additionalAuthenticationChecks(details, authentication);
			List<String> notificationMessages = null;
			boolean isSalesRepUserLoggedIn = false;
			// Check if user has supplied no password
			if (StringUtils.isEmpty((String) authentication.getCredentials()))
			{
				throw new BadCredentialsException("Login without password");
			}

			// Check if the user is in role admingroup
			if (getAdminAuthority() != null && details.getAuthorities().contains(getAdminAuthority()))
			{
				throw new LockedException("Login attempt as " + Constants.USER.ADMIN_USERGROUP + " is rejected");
			}

			//Added Code changes for WeSell Implementation - START
			final UserModel user = userService.getUserForUID(authentication.getName().toLowerCase());
			if (user instanceof EnergizerB2BEmployeeModel)
			{
				isSalesRepUserLoggedIn = salesRepUserLoginAdditionalAuthenticationChecks(details, authentication);
			}
			//Added Code changes for WeSell Implementation - END
			else
			{
				// Check if the customer is B2B type
				if (!getB2bUserGroupProvider().isUserAuthorized(details.getUsername()))
				{
					throw new InsufficientAuthenticationException(
							messages.getMessage("checkout.error.invalid.accountType", "You are not allowed to login"));
				}
				/*
				 * fetch site name from BaseSiteService and fetch user group from userService for site specific user login
				 * check
				 */
				final EnergizerB2BCustomerModel b2bCustomer = (EnergizerB2BCustomerModel) userService
						.getUserForUID(authentication.getName().toLowerCase());
				final String siteName = this.baseSiteService.getCurrentBaseSite().getUid();
				if (!validateUserGroupBaseSite(siteName, b2bCustomer))
				{
					throw new InsufficientAuthenticationException(
							messages.getMessage("login.error.user.not.exist.forsite", "You are not allowed to login for this Site"));
				}

				if (!getB2bUserGroupProvider().isUserEnabled(details.getUsername()))
				{
					throw new DisabledException("User " + details.getUsername() + " is disabled... "
							+ messages.getMessage("text.company.manage.units.disabled"));
				}

			}
			sessionService.setAttribute("isSalesRepUserLoggedIn", isSalesRepUserLoggedIn);
			sessionService.setAttribute("isUpdateQtyError", false);

			if (null != authentication.getName())
			{
				notificationMessages = energizerPasswordNotificationUtil.checkPasswordExpiryStatus(authentication.getName());
				if (null != notificationMessages && notificationMessages.size() > 0
						&& notificationMessages.get(0).equalsIgnoreCase("1"))
				{
					throw new LockedException("Your Password Has Been expired.......Please reset your password");

				}
			}
		}
		catch (final AuthenticationException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			throw e;
		}

	}

	/**
	 * @return the b2bUserGroupProvider
	 */
	protected B2BUserGroupProvider getB2bUserGroupProvider()
	{
		return b2bUserGroupProvider;
	}

	/**
	 * @param b2bUserGroupProvider
	 *           the b2bUserGroupProvider to set
	 */
	public void setB2bUserGroupProvider(final B2BUserGroupProvider b2bUserGroupProvider)
	{
		this.b2bUserGroupProvider = b2bUserGroupProvider;
	}

	/**
	 * @param adminGroup
	 *           the adminGroup to set
	 */
	public void setAdminGroup(final String adminGroup)
	{
		if (StringUtils.isBlank(adminGroup))
		{
			adminAuthority = null;
		}
		else
		{
			adminAuthority = new SimpleGrantedAuthority(adminGroup);
		}
	}

	protected GrantedAuthority getAdminAuthority()
	{
		return adminAuthority;
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

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public CartService getCartService()
	{
		return cartService;
	}

	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}

	/**
	 * @param site
	 * @param b2bcustomer
	 * @return
	 */
	private boolean validateUserGroupBaseSite(final String site, final EnergizerB2BCustomerModel b2bcustomer)
	{
		boolean flag = false;
		for (final PrincipalGroupModel objPrincipalGroupModel : b2bcustomer.getGroups())
		{
			if (site.equalsIgnoreCase(objPrincipalGroupModel.getUid()))
			{
				flag = true;
				break;
			}
		}
		return flag;
	}

	//Added Code changes for WeSell Implementation - START
	public Authentication salesRepUserAuthenticate(final Authentication authentication) throws AuthenticationException
	{
		if (Registry.hasCurrentTenant() && JaloConnection.getInstance().isSystemInitialized())
		{
			final String username = authentication.getPrincipal() == null ? "NONE_PROVIDED" : authentication.getName();
			UserDetails userDetails = null;

			try
			{
				userDetails = this.retrieveUser(username);
			}
			catch (final UsernameNotFoundException var6)
			{
				throw new BadCredentialsException(
						this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"), var6);
			}

			this.getPreAuthenticationChecks().check(userDetails);
			final User user = UserManager.getInstance().getUserByLogin(userDetails.getUsername());
			final Object credential = authentication.getCredentials();
			if (credential instanceof String)
			{
				final String loginCredential = (String) authentication.getCredentials();
				final String encodedPassword = user.getEncodedPassword();
				if (null != loginCredential && null != encodedPassword && loginCredential.equalsIgnoreCase(encodedPassword))
				{
					LOG.info(":::::: salesRep User Login with EncodedPassword :::::");
				}
				else if (!user.checkPassword((String) credential))
				{
					throw new BadCredentialsException(
							this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"));
				}
			}
			else
			{
				if (!(credential instanceof LoginToken))
				{
					throw new BadCredentialsException(
							this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"));
				}

				if (!user.checkPassword((LoginToken) credential))
				{
					throw new BadCredentialsException(
							this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"));
				}
			}

			this.additionalAuthenticationChecks(userDetails, (AbstractAuthenticationToken) authentication);

			//this.postAuthenticationChecks.check(userDetails);
			check(userDetails);
			JaloSession.getCurrentSession().setUser(user);
			return this.createSuccessAuthentication(authentication, userDetails);
		}
		else
		{
			return this.createSuccessAuthentication(authentication, new CoreUserDetails("systemNotInitialized",
					"systemNotInitialized", true, false, true, true, Collections.EMPTY_LIST, (String) null));
		}
	}

	public void check(final UserDetails user)
	{
		if (!user.isCredentialsNonExpired())
		{
			LOG.debug("User account credentials have expired");

			throw new CredentialsExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired",
					"User credentials have expired"));
		}
	}
	//Added Code changes for WeSell Implementation - END

	@SuppressWarnings("unchecked")
	public boolean salesRepUserLoginAdditionalAuthenticationChecks(final UserDetails details,
			final AbstractAuthenticationToken authentication)
	{
		EnergizerB2BCustomerModel b2bCustomer = null;
		String b2bUnitID = null;
		//final String selectedEmployeeId = null;
		String salesRepCode = null;
		boolean isSalesRepUserLoggedIn = false;
		final EnergizerB2BEmployeeModel loginB2BEmployee = (EnergizerB2BEmployeeModel) userService
				.getUserForUID(authentication.getName().toLowerCase());
		sessionService.setAttribute("salesRepLogin", authentication.getName().toLowerCase());
		sessionService.setAttribute("salesRepEmployeeModel", loginB2BEmployee);
		b2bUnitID = (String) sessionService.getAttribute("b2bunitID");
		LOG.info("SalesRepUser selected Distribution Id is::: " + b2bUnitID);

		/*-selectedEmployeeId = (String) sessionService.getAttribute("selectedEmployee");
		if (null != selectedEmployeeId
				&& !(selectedEmployeeId.trim().toLowerCase().equalsIgnoreCase(loginB2BEmployee.getUid().toLowerCase())))
		{
			LOG.info("Login SalesRepUser selected vacation salesRep Id::: " + b2bUnitID);
			final EnergizerB2BEmployeeModel selectB2BEmployee = (EnergizerB2BEmployeeModel) userService
					.getUserForUID(selectedEmployeeId.trim().toLowerCase());
			salesRepCode = selectB2BEmployee.getSalesRepCode().trim();
		}*/

		salesRepCode = loginB2BEmployee.getSalesRepCode();
		final String virtualIDPrefix = loginB2BEmployee.getUid().split("@")[0].toLowerCase();
		final String suffix = configurationService.getConfiguration().getString("wesell.userId.suffix");

		if (null != b2bUnitID && StringUtils.isNotEmpty(b2bUnitID))
		{
			//final String wesellCustomer = salesRepCode.toLowerCase() + "_" + b2bUnitID.toLowerCase() + suffix.toLowerCase();
			//Example - alejandro.medina_0098112157@edgewell.com
			final String wesellCustomer = virtualIDPrefix.toLowerCase() + "_" + b2bUnitID.toLowerCase() + suffix.toLowerCase();

			final EnergizerB2BUnitModel b2bUnitModel = (EnergizerB2BUnitModel) b2bUnitService.getUnitForUid(b2bUnitID);
			boolean isB2BCustomerAvailable = false;
			final List<EnergizerB2BCustomerModel> b2bCustomerList = ((List<EnergizerB2BCustomerModel>) CollectionUtils
					.select(b2bUnitModel.getMembers(), PredicateUtils.instanceofPredicate(EnergizerB2BCustomerModel.class)));
			if (null != b2bCustomerList && CollectionUtils.isNotEmpty(b2bCustomerList))
			{
				for (final EnergizerB2BCustomerModel customer : b2bCustomerList)
				{
					if (customer.getUid().equalsIgnoreCase(wesellCustomer.trim()))
					{
						isB2BCustomerAvailable = true;
						b2bCustomer = customer;
						break;
					}
				}
			}

			if (isB2BCustomerAvailable)
			{
				LOG.info("B2BCustomer is available : " + b2bCustomer.getUid());
				isSalesRepUserLoggedIn = true;
				SetB2BCustomer(details, b2bCustomer);

			}
			else
			{
				LOG.info("Login:: " + authentication.getName()
						+ " SalesRepUser:: B2BCustomer was not available on selected Distributor. We are creating new B2BCustomer.");
				b2bCustomer = energizerWesellVirtualID.createVirtualID(b2bUnitModel, loginB2BEmployee);
				if (null != b2bCustomer)
				{
					LOG.info("B2BCustomer Uid : " + b2bCustomer.getUid());
					isSalesRepUserLoggedIn = true;
					SetB2BCustomer(details, b2bCustomer);
				}
			}

		}
		else
		{
			LOG.info(
					"Unable to Login:: " + authentication.getName() + " SalesRepUser:: because Of B2BUnit getting NULL value::::: ");
			throw new UsernameNotFoundException("Distributor not selected");
		}

		return isSalesRepUserLoggedIn;
	}

	public void SetB2BCustomer(final UserDetails details, final EnergizerB2BCustomerModel b2bCustomer)
	{
		final User currentUser = UserManager.getInstance().getUserByLogin(b2bCustomer.getUid());
		JaloSession.getCurrentSession().setUser(currentUser);
		sessionService.setAttribute("selectedB2BCustomer", b2bCustomer);

		if (!getB2bUserGroupProvider().isUserAuthorized(b2bCustomer.getUid()))
		{
			throw new InsufficientAuthenticationException(
					messages.getMessage("checkout.error.invalid.accountType", "You are not allowed to login"));
		}
		if (!getB2bUserGroupProvider().isUserEnabled(b2bCustomer.getUid()))
		{
			throw new DisabledException(
					"User " + details.getUsername() + " is disabled... " + messages.getMessage("text.company.manage.units.disabled"));
		}
	}
}
