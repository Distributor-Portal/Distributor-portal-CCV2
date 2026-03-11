/**
 * 
 */
package com.energizer.facades.accounts.impl;

import de.hybris.platform.converters.Converters;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.data.EnergizerPasswordQuestionsData;
import com.energizer.core.model.EnergizerPasswordQuestionsModel;
import com.energizer.facades.accounts.EnergizerB2BPasswordQuestionsFacade;
import com.energizer.services.account.impl.DefaultEnergizerB2BPasswordQuestionsService;


/**
 * @author M1028886
 * 
 */
public class DefaultEnergizerB2BPasswordQuestionsFacade implements EnergizerB2BPasswordQuestionsFacade
{
	private Converter<EnergizerPasswordQuestionsModel, EnergizerPasswordQuestionsData> energizerPasswordQuestionsConverter;

	@Resource(name = "defaultEnergizerB2BPasswordQuestionsService")
	private DefaultEnergizerB2BPasswordQuestionsService passwordQuestionsService;

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BPasswordQuestionsFacade.class);


	public List<EnergizerPasswordQuestionsData> getEnergizerPasswordQuestions()
	{
		//LOG.info("Size of list,in Facade class: " + passwordQuestionsService.getPasswordQuestions().size());
		return Converters.convertAll(passwordQuestionsService.getPasswordQuestions(), getEnergizerPasswordQuestionsConverter());
		//return passwordQuestionsService.getPasswordQuestions();
	}



	/**
	 * @return the energizerPasswordQuestionsConverter
	 */
	public Converter<EnergizerPasswordQuestionsModel, EnergizerPasswordQuestionsData> getEnergizerPasswordQuestionsConverter()
	{
		return energizerPasswordQuestionsConverter;
	}

	/**
	 * @param energizerPasswordQuestionsConverter
	 *           the energizerPasswordQuestionsConverter to set
	 */
	public void setEnergizerPasswordQuestionsConverter(
			final Converter<EnergizerPasswordQuestionsModel, EnergizerPasswordQuestionsData> energizerPasswordQuestionsConverter)
	{
		this.energizerPasswordQuestionsConverter = energizerPasswordQuestionsConverter;
	}






}
