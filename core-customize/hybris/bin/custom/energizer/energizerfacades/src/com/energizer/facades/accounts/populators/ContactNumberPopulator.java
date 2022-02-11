/**
 * 
 */
package com.energizer.facades.accounts.populators;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import com.energizer.core.model.EnergizerB2BCustomerModel;


/**
 * @author M1023097
 * 
 */
public class ContactNumberPopulator implements Populator<CustomerModel, CustomerData>
{


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.converters.Populator#populate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void populate(final CustomerModel source, final CustomerData target) throws ConversionException
	{
		if (source instanceof EnergizerB2BCustomerModel)
		{
			target.setContactNumber(((EnergizerB2BCustomerModel) source).getContactNumber());
		}
	}


}
