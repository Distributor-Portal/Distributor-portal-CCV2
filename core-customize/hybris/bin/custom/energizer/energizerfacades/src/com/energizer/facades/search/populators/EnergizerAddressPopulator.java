package com.energizer.facades.search.populators;

import de.hybris.platform.commercefacades.user.converters.populator.AddressPopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;



/**
 * @author M1023097
 *
 */
public class EnergizerAddressPopulator extends AddressPopulator
{

	@Override
	public void populate(final AddressModel source, final AddressData target)
	{
		super.populate(source, target);
		if (source.getErpAddressId() != null)
		{
			target.setErpAddressId(source.getErpAddressId());
		}
		if (source.getSalesPersonEmailId() != null)
		{
			target.setSalesPersonEmailId(source.getSalesPersonEmailId());
		}
		if (source.getDisplayName() != null)
		{
			target.setDisplayName(source.getDisplayName());
		}
		if (source.getActive() != null)
		{
			target.setActive(source.getActive());
		}
		if (null != source.getSoldToAddressId())
		{
			target.setSoldToAddressId(source.getSoldToAddressId());
		}

	}

}
