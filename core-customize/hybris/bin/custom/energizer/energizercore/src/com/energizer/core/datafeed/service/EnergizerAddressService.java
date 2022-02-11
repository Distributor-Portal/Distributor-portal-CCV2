/**
 *
 */
package com.energizer.core.datafeed.service;

import de.hybris.platform.core.model.user.AddressModel;

import java.util.List;


/**
 * @author M1023278
 *
 */
public interface EnergizerAddressService
{
	public List<AddressModel> fetchAddress(final String erpAddressId);

	public List<AddressModel> fetchAddressForB2BUnit(final String b2bUnitUId);

	public List<AddressModel> fetchAddressForB2BUnitAndErpAddressID(final String b2bUnitId, final String erpAddressId);

}
