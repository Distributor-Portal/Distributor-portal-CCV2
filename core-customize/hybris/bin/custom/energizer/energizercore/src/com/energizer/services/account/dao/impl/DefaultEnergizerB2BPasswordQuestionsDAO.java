/**
 * 
 */
package com.energizer.services.account.dao.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.jalo.EnergizerPasswordQuestions;
import com.energizer.services.account.dao.EnergizerB2BPasswordQuestionsDAO;


/**
 * @author M1028886
 * 
 */
public class DefaultEnergizerB2BPasswordQuestionsDAO implements EnergizerB2BPasswordQuestionsDAO
{

	@Resource
	private FlexibleSearchService flexibleSearchService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.account.dao.EnergizerB2BPasswordQuestionsDAO#getPasswordQuestions()
	 */
	@Override
	public List getPasswordQuestions()
	{
		final String query = "select {" + EnergizerPasswordQuestions.PK + "} from {EnergizerPasswordQuestions}";
		final FlexibleSearchQuery fq = new FlexibleSearchQuery(query);
		//fq.setResultClassList(Arrays.asList(String.class));
		final de.hybris.platform.servicelayer.search.SearchResult<EnergizerPasswordQuestions> searchList = flexibleSearchService
				.search(fq);
		//final List<String> resultList = searchList.getResult();
		return searchList.getResult();
	}
}
