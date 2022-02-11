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
package com.energizer.storefront.security.impl;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.storefront.security.BruteForceAttackCounter;


/**
 * Default implementation of {@link BruteForceAttackCounter}
 */
public class DefaultBruteForceAttackCounter implements BruteForceAttackCounter
{
	private static final Logger LOG = Logger.getLogger(DefaultBruteForceAttackCounter.class);

	@Resource
	private UserService userService;

	@Resource
	private SessionService sessionService;

	private final ConcurrentHashMap<String, LoginFailure> bruteForceAttackCache;
	private final Integer maxFailedLogins;
	private final Integer cacheSizeLimit;
	private final Integer cacheExpiration;

	public DefaultBruteForceAttackCounter(final Integer maxFailedLogins, final Integer cacheExpiration,
			final Integer cacheSizeLimit)
	{
		bruteForceAttackCache = new ConcurrentHashMap((int) 0.5 * cacheSizeLimit);
		this.maxFailedLogins = maxFailedLogins;
		this.cacheSizeLimit = cacheSizeLimit;
		this.cacheExpiration = cacheExpiration;
	}

	@Override
	public void registerLoginFailure(final String userUid)
	{
		try
		{
		if (StringUtils.isNotEmpty(userUid))
		{
				final LoginFailure count = get(prepareUserUid(userUid), Integer.valueOf(0));
			final UserModel userModel = userService.getUserForUID(userUid.toLowerCase());
				if (userModel instanceof EnergizerB2BEmployeeModel && null != sessionService.getAttribute("b2bunitID"))
			{
					count.setCounter(count.getCounter() + 1);
					count.setDate(new Date());
					bruteForceAttackCache.put(prepareUserUid(userUid), count);
			}
				else if (userModel instanceof EnergizerB2BCustomerModel)
			{
					count.setCounter(count.getCounter() + 1);
					count.setDate(new Date());
					bruteForceAttackCache.put(prepareUserUid(userUid), count);
			}
		}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}


	@Override
	public boolean isAttack(final String userUid)
	{
		if (StringUtils.isNotEmpty(userUid))
		{
			return get(prepareUserUid(userUid), Integer.valueOf(0)).getCounter() >= maxFailedLogins;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void resetUserCounter(final String userUid)
	{
		if (StringUtils.isNotEmpty(userUid))
		{
			bruteForceAttackCache.remove(prepareUserUid(userUid));
		}
	}

	@Override
	public int getUserFailedLogins(final String userUid)
	{
		if (StringUtils.isNotEmpty(userUid))
		{
			return get(prepareUserUid(userUid), Integer.valueOf(0)).getCounter();
		}
		else
		{
			return Integer.valueOf(0);
		}
	}

	@Override
	public int getMaxLoginAttempts()
	{
		return maxFailedLogins;
	}

	@Override
	public void registerLoginFailure(final String userUid, final int count1)
	{
		if (StringUtils.isNotEmpty(userUid))
		{
			final LoginFailure count = get(prepareUserUid(userUid), Integer.valueOf(0));
			count.setCounter(count1);
			count.setDate(new Date());
			bruteForceAttackCache.put(prepareUserUid(userUid), count);
		}
	}

	protected LoginFailure get(final String userUid, final Integer startValue)
	{
		LoginFailure value = bruteForceAttackCache.get(prepareUserUid(userUid));
		if (value == null)
		{
			value = new LoginFailure(startValue, new Date());
			bruteForceAttackCache.put(prepareUserUid(userUid), value);
			if (bruteForceAttackCache.size() > cacheSizeLimit)
			{
				evict();
			}
		}
		return value;
	}


	protected String prepareUserUid(final String userUid)
	{
		return StringUtils.lowerCase(userUid);
	}


	protected void evict()
	{
		if (bruteForceAttackCache.size() > cacheSizeLimit)
		{
			final Iterator<String> cacheIterator = bruteForceAttackCache.keySet().iterator();
			final Date dateLimit = DateUtils.addMinutes(new Date(), 0 - cacheExpiration);
			while (cacheIterator.hasNext())
			{
				final String userKey = cacheIterator.next();
				final LoginFailure value = bruteForceAttackCache.get(userKey);
				if (value.getDate().before(dateLimit))
				{
					bruteForceAttackCache.remove(userKey);
				}
			}
		}
	}


	public class LoginFailure
	{
		private Integer counter;
		private Date date;

		public LoginFailure()
		{
			this.counter = Integer.valueOf(0);
			this.date = new Date();
		}

		public LoginFailure(final Integer counter, final Date date)
		{
			this.counter = counter;
			this.date = date;
		}

		public Integer getCounter()
		{
			return counter;
		}

		public void setCounter(final Integer counter)
		{
			this.counter = counter;
		}

		public Date getDate()
		{
			return date;
		}

		public void setDate(final Date date)
		{
			this.date = date;
		}
	}
}
