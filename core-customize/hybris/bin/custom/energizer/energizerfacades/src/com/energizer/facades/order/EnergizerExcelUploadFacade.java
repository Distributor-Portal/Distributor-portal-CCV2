/**
 *
 */
package com.energizer.facades.order;

import java.util.List;

import com.energizer.core.product.data.EnergizerFileUploadData;


/**
 * @author M9005674
 *
 */
public interface EnergizerExcelUploadFacade
{
	public List<EnergizerFileUploadData> convertExcelRowtoBean(final List<EnergizerFileUploadData> energizerFileUploadModels)
			throws Exception;

}
