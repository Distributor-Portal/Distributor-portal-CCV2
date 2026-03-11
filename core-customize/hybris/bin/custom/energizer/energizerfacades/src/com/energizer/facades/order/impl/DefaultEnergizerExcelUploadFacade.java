/**
 * 
 */
package com.energizer.facades.order.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.product.data.EnergizerFileUploadData;
import com.energizer.facades.order.EnergizerExcelUploadFacade;
import com.energizer.services.product.EnergizerExcelRowtoModelService;


/**
 * @author M9005674
 *
 */
public class DefaultEnergizerExcelUploadFacade implements EnergizerExcelUploadFacade
{

	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerExcelUploadFacade.class);

	@Resource(name = "energizerExcelRowtoModelService")
	EnergizerExcelRowtoModelService energizerExcelRowtoModelService;

	@Override
	public List<EnergizerFileUploadData> convertExcelRowtoBean(final List<EnergizerFileUploadData> energizerFileUploadModels)
			throws Exception
	{
		List<EnergizerFileUploadData> energizerFileUploadBeans = new ArrayList<EnergizerFileUploadData>();
		try
		{
		energizerFileUploadBeans = energizerExcelRowtoModelService.processExcelRowtoBean(energizerFileUploadModels);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while converting excel file upload ::: " + e);
			throw e;
		}
		return energizerFileUploadBeans;
	}

}
