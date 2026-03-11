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
package com.energizer.storefront.controllers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Displays "confirmation, information, error , business rule validation" messages.
 */
public class GlobalMessages
{
	public static final String CONF_MESSAGES_HOLDER = "accConfMsgs";
	public static final String INFO_MESSAGES_HOLDER = "accInfoMsgs";
	public static final String ERROR_MESSAGES_HOLDER = "accErrorMsgs";
	public static final String BUSINESS_RULE_MESSAGES = "businessRuleError";
	public static final String FORGOT_PWD_CONF_MESSAGES = "forgotPwdConfMsgs";
	public static final String B2BUNIT_MAZUSER_CONF_MESSAGE = "b2bUnitMaxUserConfMsgs";


	public static void addConfMessage(final Model model, final String messageKey)
	{
		addMessage(model, CONF_MESSAGES_HOLDER, messageKey, null);
	}

	public static void addInfoMessage(final Model model, final String messageKey)
	{
		addMessage(model, INFO_MESSAGES_HOLDER, messageKey, null);
	}

	public static void addErrorMessage(final Model model, final String messageKey)
	{
		addMessage(model, ERROR_MESSAGES_HOLDER, messageKey, null);
	}

	public static void addBusinessRuleMessage(final Model model, final String messageKey)
	{
		addMessage(model, BUSINESS_RULE_MESSAGES, messageKey, null);
	}

	public static void addForgotPwdConfMessage(final Model model, final String messageHolder, final String messageKey,
			final Object[] attributes)
	{
		addMessage(model, FORGOT_PWD_CONF_MESSAGES, messageKey, attributes);
	}

	public static void b2bUnitMaxUserConfMsgs(final Model model, final String messageKey, final Object[] attributes)
	{
		addMessage(model, B2BUNIT_MAZUSER_CONF_MESSAGE, messageKey, attributes);
	}

	public static boolean hasErrorMessage(final Model model)
	{
		return model.containsAttribute(ERROR_MESSAGES_HOLDER);
	}

	public static void addMessage(final Model model, final String messageHolder, final String messageKey, final Object[] attributes)
	{
		final GlobalMessage message = new GlobalMessage();
		message.setCode(messageKey);
		message.setAttributes(attributes != null ? Arrays.asList(attributes) : Collections.emptyList());

		final Map<String, Object> modelMap = model.asMap();
		if (modelMap.containsKey(messageHolder))
		{
			final List<GlobalMessage> messages = new ArrayList<GlobalMessage>((List<GlobalMessage>) modelMap.get(messageHolder));
			messages.add(message);
			model.addAttribute(messageHolder, messages);
		}
		else
		{
			model.addAttribute(messageHolder, Collections.singletonList(message));
		}
	}

	public static void addFlashMessage(final RedirectAttributes model, final String messageHolder, final String messageKey)
	{
		addFlashMessage(model, messageHolder, messageKey, null);
	}

	public static void addFlashMessage(final RedirectAttributes model, final String messageHolder, final String messageKey,
			final Object[] attributes)
	{
		final GlobalMessage message = new GlobalMessage();
		message.setCode(messageKey);
		message.setAttributes(attributes != null ? Arrays.asList(attributes) : Collections.emptyList());

		final Map<String, ?> flashModelMap = model.getFlashAttributes();
		if (flashModelMap.containsKey(messageHolder))
		{
			final List<GlobalMessage> messages = new ArrayList<GlobalMessage>((List<GlobalMessage>) flashModelMap.get(messageHolder));
			messages.add(message);
			model.addFlashAttribute(messageHolder, messages);
		}
		else
		{
			model.addFlashAttribute(messageHolder, Collections.singletonList(message));
		}
	}
}