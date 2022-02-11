/**
 * 
 */
package com.energizer.services.product.dao;

import java.util.List;

import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author Geetika Singh
 * 
 */
public interface EnergizerB2BUnitDao
{
	public List<EnergizerB2BUnitModel> findB2BUnitForSalesArea(String salesOrganisation, String distributionChannel,
			String division);

}
