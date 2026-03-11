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

import java.util.List;

import com.energizer.energizeraccountsummary.document.data.B2BDragAndDropData;
import com.energizer.energizeraccountsummary.model.B2BDocumentPaymentInfoModel;

import de.hybris.platform.servicelayer.search.SearchResult;


public interface B2BDocumentPaymentInfoService
{

	/**
	 * Gets a list of document payment info associated to a Document.
	 *
	 * @param documentNumber
	 *           the document number
	 * @return list of documentPaymentInfos
	 */
	public SearchResult<B2BDocumentPaymentInfoModel> getDocumentPaymentInfo(final String documentNumber);

	/**
	 *
	 * Applies a list of drag & drop actions
	 *
	 * @param lstActions
	 *           the list of actions to be applied.
	 */
	public void applyPayment(List<B2BDragAndDropData> lstActions);


}
