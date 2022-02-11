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
package com.energizer.energizeraccountsummary.document.dao;

import com.energizer.energizeraccountsummary.document.AccountSummaryDocumentQuery;
import com.energizer.energizeraccountsummary.model.B2BDocumentModel;

import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;


public interface PagedB2BDocumentDao
{

	/**
	 * Finds all B2BDocumentModel filtered by the query. The resulting list only contains document associated to the
	 * current user's B2BUnit.
	 * 
	 * @param query
	 *           paged document query
	 * @return result : a SeachPageData< B2BDocumentModel > containing documents.
	 */
	SearchPageData<B2BDocumentModel> findDocuments(final AccountSummaryDocumentQuery query);

}
