/**
 * 
 */
package com.energizer.services.account.impl;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import com.energizer.services.account.EnergizerB2BPasswordQuestionsService;
import com.energizer.services.account.dao.impl.DefaultEnergizerB2BPasswordQuestionsDAO;


/**
 * @author M1028886
 * 
 */
public class DefaultEnergizerB2BPasswordQuestionsService implements EnergizerB2BPasswordQuestionsService
{

	@Resource(name = "defaultEnergizerB2BPasswordQuestionsDAO")
	private DefaultEnergizerB2BPasswordQuestionsDAO passwordQuestionsDAO;


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.account.EnergizerB2BPasswordQuestionsService#getPasswordQuestions()
	 */
	@Override
	public Collection getPasswordQuestions()
	{
		final List<String> passwordQuestionsList = passwordQuestionsDAO.getPasswordQuestions();
		return passwordQuestionsList;
	}
}
