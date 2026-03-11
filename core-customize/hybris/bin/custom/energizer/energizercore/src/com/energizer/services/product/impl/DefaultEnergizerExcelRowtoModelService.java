/**
 *
 */
package com.energizer.services.product.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.product.data.EnergizerFileUploadData;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.services.product.EnergizerExcelRowtoModelService;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author M9005674
 * 
 */
public class DefaultEnergizerExcelRowtoModelService implements EnergizerExcelRowtoModelService
{
	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerExcelRowtoModelService.class);

	@Resource
	EnergizerProductService energizerProductService;

	@Resource
	EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.product.EnergizerExcelRowtoModelService#processExcelRowtoBean(java.util.List)
	 */

	@Override
	public List<EnergizerFileUploadData> processExcelRowtoBean(final List<EnergizerFileUploadData> energizerFileUploadModels)
			throws Exception

	{
		final List<EnergizerFileUploadData> energizerFileUploadsModels = new ArrayList<EnergizerFileUploadData>();
		try
		{

		for (final EnergizerFileUploadData energizerFileUploadModel : energizerFileUploadModels)
		{

			if (energizerFileUploadModel.getMaterialId() != null && energizerFileUploadModel.getCustomerMaterialId() != null)
			{
				final EnergizerB2BUnitModel energizerB2BUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
				final EnergizerCMIRModel cmirModelforMatIdAndCustId = energizerProductService.getEnergizerCMIRListForMatIdAndCustId(
						energizerFileUploadModel.getMaterialId(), energizerFileUploadModel.getCustomerMaterialId(),
						energizerB2BUnitModel.getUid());
				if (cmirModelforMatIdAndCustId != null && cmirModelforMatIdAndCustId.getIsActive() == true)
				{
					energizerFileUploadModel.setShippingPoint(cmirModelforMatIdAndCustId.getShippingPoint());
					energizerFileUploadModel.setUom(cmirModelforMatIdAndCustId.getUom());
					energizerFileUploadModel.setOrderingUnit(Long.valueOf(cmirModelforMatIdAndCustId.getOrderingUnit()));
					if (energizerFileUploadModel.getQuantity() == null || energizerFileUploadModel.getQuantity() == 0L)
					{
						//energizerFileUploadModel.setQuantity(Long.valueOf(cmirModelforMatIdAndCustId.getOrderingUnit()));
						energizerFileUploadModel.setQuantity(cmirModelforMatIdAndCustId.getOrderingUnit() != null ? Long
								.valueOf(cmirModelforMatIdAndCustId.getOrderingUnit()) : null);
					}
					energizerFileUploadsModels.add(energizerFileUploadModel);
				}
				else
				{
					energizerFileUploadModel.setHasError(true);
					energizerFileUploadModel.setMessage(energizerFileUploadModel.getMaterialId() + "---"
							+ energizerFileUploadModel.getCustomerMaterialId());
					energizerFileUploadsModels.add(energizerFileUploadModel);
				}
			}
			else if (energizerFileUploadModel.getMaterialId() != null && energizerFileUploadModel.getCustomerMaterialId() == null)
			{

				final EnergizerB2BUnitModel energizerB2BUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
				final EnergizerCMIRModel energizerCMIRModelforMatId = energizerProductService.getEnergizerCMIR(
						energizerFileUploadModel.getMaterialId(), energizerB2BUnitModel.getUid());
				if (energizerCMIRModelforMatId != null && energizerCMIRModelforMatId.getIsActive() == true)
				{
					energizerFileUploadModel.setCustomerMaterialId(energizerCMIRModelforMatId.getCustomerMaterialId());
					energizerFileUploadModel.setShippingPoint(energizerCMIRModelforMatId.getShippingPoint());
					energizerFileUploadModel.setUom(energizerCMIRModelforMatId.getUom());
					energizerFileUploadModel.setOrderingUnit(Long.valueOf(energizerCMIRModelforMatId.getOrderingUnit()));
					if (energizerFileUploadModel.getQuantity() == null || energizerFileUploadModel.getQuantity() == 0L)
					{
						//energizerFileUploadModel.setQuantity(Long.valueOf(energizerCMIRModelforMatId.getOrderingUnit()));
						energizerFileUploadModel.setQuantity(energizerCMIRModelforMatId.getOrderingUnit() != null ? Long
								.valueOf(energizerCMIRModelforMatId.getOrderingUnit()) : null);
					}
					energizerFileUploadsModels.add(energizerFileUploadModel);
				}
				else
				{
					energizerFileUploadModel.setHasError(true);
					energizerFileUploadModel.setMessage(energizerFileUploadModel.getMaterialId());
					energizerFileUploadsModels.add(energizerFileUploadModel);
				}
			}
			else if (energizerFileUploadModel.getMaterialId() == null && energizerFileUploadModel.getCustomerMaterialId() != null)
			{

				final EnergizerB2BUnitModel energizerB2BUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
				final EnergizerCMIRModel energizerCMIRModelforCustId = energizerProductService.getEnergizerCMIRforCustomerMaterialID(
						energizerFileUploadModel.getCustomerMaterialId(), energizerB2BUnitModel.getUid());
				if (energizerCMIRModelforCustId != null && energizerCMIRModelforCustId.getIsActive() == true)
				{
					energizerFileUploadModel.setMaterialId(energizerCMIRModelforCustId.getErpMaterialId());
					energizerFileUploadModel.setShippingPoint(energizerCMIRModelforCustId.getShippingPoint());
					energizerFileUploadModel.setUom(energizerCMIRModelforCustId.getUom());
					energizerFileUploadModel.setOrderingUnit(Long.valueOf(energizerCMIRModelforCustId.getOrderingUnit()));
					if (energizerFileUploadModel.getQuantity() == null || energizerFileUploadModel.getQuantity() == 0L)
					{
						//energizerFileUploadModel.setQuantity(Long.valueOf(energizerCMIRModelforCustId.getOrderingUnit()));
						energizerFileUploadModel.setQuantity(energizerCMIRModelforCustId.getOrderingUnit() != null ? Long
								.valueOf(energizerCMIRModelforCustId.getOrderingUnit()) : null);
					}
					energizerFileUploadsModels.add(energizerFileUploadModel);
				}
				else
				{
					energizerFileUploadModel.setHasError(true);
					energizerFileUploadModel.setMessage(energizerFileUploadModel.getCustomerMaterialId());
					energizerFileUploadsModels.add(energizerFileUploadModel);
				}
			}
		}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while file upload::: " + e);
			throw e;
		}
		return energizerFileUploadsModels;
	}

}
