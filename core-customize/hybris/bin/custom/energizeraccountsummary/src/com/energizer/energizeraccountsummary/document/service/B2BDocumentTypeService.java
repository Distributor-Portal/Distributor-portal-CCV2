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
package com.energizer.energizeraccountsummary.document.service;

import com.energizer.energizeraccountsummary.model.B2BDocumentTypeModel;

import de.hybris.platform.servicelayer.search.SearchResult;


public interface B2BDocumentTypeService
{
	/**
	 * Gets all document types.
	 * 
	 * @return a SearchResult<B2BDocumentTypeModel> containing document types.
	 */
	SearchResult<B2BDocumentTypeModel> getAllDocumentTypes();
}
