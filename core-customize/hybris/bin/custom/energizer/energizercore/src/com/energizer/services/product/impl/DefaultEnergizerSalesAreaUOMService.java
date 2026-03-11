/**
 * 
 */
package com.energizer.services.product.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.energizer.core.model.EnergizerSalesAreaUOMModel;
import com.energizer.services.product.EnergizerSalesUOMService;
import com.energizer.services.product.dao.EnergizerSalesUOMDao;



/**
 * @author Geetika Singh
 * 
 */
@Component(value = "energizerSalesUOMService")
public class DefaultEnergizerSalesAreaUOMService implements EnergizerSalesUOMService
{
	@Resource
	private EnergizerSalesUOMDao energizerSalesUOMDao;

	public List<EnergizerSalesAreaUOMModel> getSalesAreaUOM(final String familyId)
	{
		final List<EnergizerSalesAreaUOMModel> salesUOMs = energizerSalesUOMDao.findSalesAreaUOM(familyId);

		return salesUOMs;
	}
}
