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

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;


@UnitTest
public class CategoryPageControllerUnitTest
{

	private static final Logger LOG = Logger.getLogger(CategoryPageControllerUnitTest.class);

	private static final int DEFAULT_PAGE_SIZE = 20;

	private final AbstractSearchPageController controller = new CategoryPageController();

	@Mock
	private Model model;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private SearchPageData<?> searchPageData;

	@Before
	public void prepare()
	{
		MockitoAnnotations.initMocks(this);
	}



	@Test
	public void sampleCategoryController()
	{
		LOG.info("i am here at the category page");
		LOG.info("i am here at the category page");
	}




}
