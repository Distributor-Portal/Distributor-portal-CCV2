/**
 * 
 */
package com.energizer.services.product.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.services.product.EnergizerB2BUnitService;
import com.energizer.services.product.dao.EnergizerB2BUnitDao;


/**
 * @author Geetika Singh
 * 
 */
@Component(value = "energizerB2BUnitService")
public class DefaultEnergizerB2BUnitService implements EnergizerB2BUnitService
{
	@Resource
	private EnergizerB2BUnitDao energizerB2BUnitDao;

	public List<EnergizerB2BUnitModel> getB2BUnitForSalesArea(final String salesOrganisation, final String distributionChannel,
			final String division)
	{
		final List<EnergizerB2BUnitModel> b2bUnitModels = energizerB2BUnitDao.findB2BUnitForSalesArea(salesOrganisation,
				distributionChannel, division);
		return b2bUnitModels;

	}
}
