/**
 * 
 */
package com.energizer.services.product.dao;

import java.util.List;

import com.energizer.core.model.EnergizerSalesAreaUOMModel;


/**
 * @author Geetika Singh
 * 
 */
public interface EnergizerSalesUOMDao
{
	public List<EnergizerSalesAreaUOMModel> findSalesAreaUOM(String familyId);

}
