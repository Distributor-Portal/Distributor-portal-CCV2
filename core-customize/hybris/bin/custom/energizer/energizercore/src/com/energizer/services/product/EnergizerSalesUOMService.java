/**
 * 
 */
package com.energizer.services.product;

import java.util.List;

import com.energizer.core.model.EnergizerSalesAreaUOMModel;


/**
 * @author Geetika Singh
 * 
 */
public interface EnergizerSalesUOMService
{
	public List<EnergizerSalesAreaUOMModel> getSalesAreaUOM(String familyId);

}
