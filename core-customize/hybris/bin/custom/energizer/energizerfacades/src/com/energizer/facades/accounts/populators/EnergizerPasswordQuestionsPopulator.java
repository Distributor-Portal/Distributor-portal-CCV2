/**
 * 
 */
package com.energizer.facades.accounts.populators;

import de.hybris.platform.converters.impl.AbstractPopulatingConverter;

import org.springframework.util.Assert;

import com.energizer.core.data.EnergizerPasswordQuestionsData;
import com.energizer.core.model.EnergizerPasswordQuestionsModel;


/**
 * @author M1028886
 * 
 */
public class EnergizerPasswordQuestionsPopulator extends
		AbstractPopulatingConverter<EnergizerPasswordQuestionsModel, EnergizerPasswordQuestionsData>
{
	@Override
	protected EnergizerPasswordQuestionsData createTarget()
	{
		return new EnergizerPasswordQuestionsData();
	}

	@Override
	public void populate(final EnergizerPasswordQuestionsModel source, final EnergizerPasswordQuestionsData target)
	{
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");

		target.setCode(source.getPasswordQuestion());
		target.setName(source.getPasswordQuestion());
		super.populate(source, target);
	}


}
