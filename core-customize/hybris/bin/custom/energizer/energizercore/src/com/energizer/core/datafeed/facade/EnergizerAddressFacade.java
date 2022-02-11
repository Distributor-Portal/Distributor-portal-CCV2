/**
 *
 */
package com.energizer.core.datafeed.facade;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;

import java.util.List;


/**
 * @author M1023278
 *
 */
public interface EnergizerAddressFacade
{
	public List<AddressModel> fetchAddress(final String erpAddressId);

	public List<AddressData> fetchAddressForB2BUnit(final String b2bUnitUId);

	public List<AddressModel> fetchAddressForB2BUnitAndErpAddressID(final String b2bUnitId, final String erpAddressId);

}
