/**
 * 
 */
package com.energizer.facades.accounts.populators;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.converters.populator.CustomerPopulator;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;


/**
 * @author M1028886
 * 
 */
public class EnergizerCustomerPopulator extends CustomerPopulator
{

	private Converter<B2BCustomerModel, CustomerData> energizerCustomerConverter;

	@Override
	public void populate(final CustomerModel source, final CustomerData target)
	{
		super.populate(source, target);

		if (source.getPasswordQuestion() != null || source.getPasswordAnswer() != null)
		{
			target.setPasswordQuestion(source.getPasswordQuestion());
			target.setPasswordAnswer(source.getPasswordAnswer());
		}

	}

	/**
	 * @return the energizerCustomerConverter
	 */
	public Converter<B2BCustomerModel, CustomerData> getEnergizerCustomerConverter()
	{
		return energizerCustomerConverter;
	}

	/**
	 * @param energizerCustomerConverter the energizerCustomerConverter to set
	 */
	public void setEnergizerCustomerConverter(Converter<B2BCustomerModel, CustomerData> energizerCustomerConverter)
	{
		this.energizerCustomerConverter = energizerCustomerConverter;
	}
}
