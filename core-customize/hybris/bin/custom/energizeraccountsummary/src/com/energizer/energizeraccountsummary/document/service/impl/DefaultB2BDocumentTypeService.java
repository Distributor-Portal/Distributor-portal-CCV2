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
package com.energizer.energizeraccountsummary.document.service.impl;

import javax.annotation.Resource;

import com.energizer.energizeraccountsummary.document.dao.B2BDocumentTypeDao;
import com.energizer.energizeraccountsummary.document.service.B2BDocumentTypeService;
import com.energizer.energizeraccountsummary.model.B2BDocumentTypeModel;

import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;
import de.hybris.platform.servicelayer.search.SearchResult;


public class DefaultB2BDocumentTypeService extends AbstractBusinessService implements B2BDocumentTypeService
{

	@Resource
	private B2BDocumentTypeDao b2bDocumentTypeDao;

	@Override
	public SearchResult<B2BDocumentTypeModel> getAllDocumentTypes()
	{
		return b2bDocumentTypeDao.getAllDocumentTypes();
	}

	public void setB2bDocumentTypeDao(final B2BDocumentTypeDao b2bDocumentTypeDao)
	{
		this.b2bDocumentTypeDao = b2bDocumentTypeDao;
	}
}
