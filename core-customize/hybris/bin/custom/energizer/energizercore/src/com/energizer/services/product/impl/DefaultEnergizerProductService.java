/**
 *
 */
package com.energizer.services.product.impl;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.EnergizerShippingPointModel;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.services.product.dao.EnergizerProductDAO;



/**
 * @author Bivash Pandit
 *
 */
public class DefaultEnergizerProductService implements EnergizerProductService
{
	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerProductService.class);


	@Resource
	EnergizerProductService energizerProductService;

	EnergizerProductDAO energizerProductDAO;

	@Resource
	private ModelService modelService;

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.product.service.EnergizerProductService#getEnergizerCMIR(java.lang.String)
	 */
	@Override
	public EnergizerCMIRModel getEnergizerCMIR(final String erpMaterialId)
			throws AmbiguousIdentifierException, UnknownIdentifierException, ModelNotFoundException, Exception
	{
		EnergizerCMIRModel energizerCMIRModel = null;
		try
		{
			final List<EnergizerCMIRModel> result = energizerProductDAO.getEnergizerCMIRList(erpMaterialId);
			if (result.isEmpty())
			{
				throw new UnknownIdentifierException("EnergizerCMIR code '" + erpMaterialId + "' not found!");
			}
			else
			{
				energizerCMIRModel = result.get(0);
			}
		}
		catch (final ModelNotFoundException mn)
		{
			LOG.info("Exception occured in fetching CMIR model:::" + mn);
			throw mn;
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in fetching CMIR model:::" + e);
			throw e;
		}
		return energizerCMIRModel;
	}

	/**
	 * @param shippingPointId
	 *
	 */
	public String getShippingPointName(final String shippingPointId)
	{
		final List<EnergizerShippingPointModel> result = energizerProductDAO.getShippingPointName(shippingPointId);
		return (result.isEmpty()) ? null : result.get(0).getShippingPointName();
	}

	public String getShippingPointLocation(final String shippingPointId)
	{
		final List<EnergizerShippingPointModel> result = energizerProductDAO.getShippingPointName(shippingPointId);
		return (result.isEmpty()) ? null : result.get(0).getShippingPointLocation();
	}

	public List<EnergizerProductModel> getEnergizerERPMaterialID()
	{

		final List<EnergizerProductModel> result = energizerProductDAO.getEnergizerERPMaterialIDList();


		return result;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.product.service.EnergizerProductService#getEnergizerCMIR(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerCMIRModel getEnergizerCMIR(final String erpMaterialId, final String b2bUnitId)
			throws ModelNotFoundException, Exception
	{
		EnergizerCMIRModel energizerCMIRModel = null;
		List<EnergizerCMIRModel> result = null;
		try
		{
			result = energizerProductDAO.getEnergizerCMIRList(erpMaterialId, b2bUnitId);
			/*
			 * if (result.isEmpty()) { throw new UnknownIdentifierException("EnergizerCMIR  code '" + erpMaterialId +
			 * "' not found!"); } else if (result.size() > 1) { throw new
			 * AmbiguousIdentifierException("EnergizerCMIR code '" + erpMaterialId + "' is not unique, " + result.size() +
			 * " EnergizerCMIR found!"); }
			 */
			int count = 0;
			if (null != result)
			{
				for (final EnergizerCMIRModel cmir : result)
				{
					if (cmir.getIsActive() == true)
					{
						count++;
						energizerCMIRModel = cmir;

					}
				}
				if (count > 1)
				{
					LOG.info("More than 1 active CMIR(s) found for same product : " + erpMaterialId + " , b2bUnitId : " + b2bUnitId
							+ " , total " + count + " active CMIR(s) !!");
					return null;
				}
				if (count == 1)
				{
					return energizerCMIRModel;
				}
			}
			else
			{
				LOG.info("result is null ...");
			}
		}
		catch (final ModelNotFoundException mn)
		{
			LOG.info("Exception occured in fetching CMIR model:::" + mn);
			throw mn;
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in fetching CMIR model:::" + e);
			throw e;
		}
		return (result.isEmpty()) ? null : result.get((result.size()) - 1);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.product.service.EnergizerProductService#getEnergizerProductConversion(java.lang.String)
	 */
	@Override
	public EnergizerProductConversionFactorModel getEnergizerProductConversion(final String erpMaterialId, final String b2bUnitId)
			throws Exception
	{
		final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId, b2bUnitId);

		EnergizerProductConversionFactorModel coversionFactor = null;
		try
		{
			String cmirUom = "";

			if (null != energizerCMIRModel)
			{
				cmirUom = energizerCMIRModel.getUom();

				final List<EnergizerProductConversionFactorModel> result = energizerProductDAO
						.getEnergizerProductConversionLst(erpMaterialId);


				if (!result.isEmpty())
				{
					for (final Iterator iterator = result.iterator(); iterator.hasNext();)
					{
						final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = (EnergizerProductConversionFactorModel) iterator
								.next();

						final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();

						if (null != alternateUOM && alternateUOM.equalsIgnoreCase(cmirUom))
						{
							coversionFactor = energizerProductConversionFactorModel;
						}
					}
				}

			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in Product Conversion :::" + e);
			throw e;
		}

		return coversionFactor;
	}

	@Required
	public void setEnergizerProductDAO(final EnergizerProductDAO energizerProductDAO)
	{
		this.energizerProductDAO = energizerProductDAO;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.product.EnergizerProductService#getEnergizerCMIRforCustomerMaterialID(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerCMIRModel getEnergizerCMIRforCustomerMaterialID(final String customerMaterialId, final String b2bUnitId)
	{
		EnergizerCMIRModel energizerCMIRModel = null;

		final List<EnergizerCMIRModel> cmirList = energizerProductDAO.getEnergizerCMIRListForCustomerMaterialID(customerMaterialId,
				b2bUnitId);
		if (cmirList != null && cmirList.size() > 0)
		{
			for (final EnergizerCMIRModel cmir : cmirList)
			{
				if (cmir.getIsActive() == true)
				{
					energizerCMIRModel = cmir;
					return energizerCMIRModel;
				}
			}
			return cmirList.get(0);
		}


		else
		{
			return null;
		}


	}

	public EnergizerCMIRModel getEnergizerCMIRListForMatIdAndCustId(final String erpMaterialId, final String customerMaterialId,
			final String b2bUnitId)
	{
		final List<EnergizerCMIRModel> result = energizerProductDAO.getEnergizerCMIRListForMatIdAndCustId(erpMaterialId,
				customerMaterialId, b2bUnitId);

		/*
		 * if (result.isEmpty()) { throw new UnknownIdentifierException("EnergizerCMIR  code '" + erpMaterialId +
		 * "' not found!"); } else if (result.size() > 1) { throw new AmbiguousIdentifierException("EnergizerCMIR code '"
		 * + erpMaterialId + "' is not unique, " + result.size() + " EnergizerCMIR found!"); }
		 */
		for (final EnergizerCMIRModel cmir : result)
		{
			if (cmir.getIsActive() == true)
			{

				return cmir;
			}
		}
		return (result.isEmpty()) ? null : result.get(0);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.EnergizerProductService#getEnergizerCMIRList(java.lang.String)
	 */
	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRList(final String erpMaterialId) throws Exception
	{
		// YTODO Auto-generated method stub
		List<EnergizerCMIRModel> result = null;
		try
		{
			result = energizerProductDAO.getEnergizerCMIRList(erpMaterialId);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in getting CMIR model list::::" + e);
			throw e;
		}
		return result;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.EnergizerProductService#getEnergizerOrphanedProductList()
	 */
	@Override
	public List<EnergizerProductModel> getEnergizerOrphanedProductList()
	{
		final List<EnergizerProductModel> result = energizerProductDAO.getEnergizerOrphanedProductList();
		return result;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.EnergizerProductService#getEnergizerPriceRowForB2BUnit(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerPriceRowModel getEnergizerPriceRowForB2BUnit(final String erpMaterialID, final String b2bUnitId)
	{
		final EnergizerPriceRowModel result = energizerProductDAO.getEnergizerPriceRowForB2BUnit(erpMaterialID, b2bUnitId);
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.EnergizerProductService#getEnergizerPriceRowForB2BUnit(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public List<EnergizerProductModel> getEnergizerProductListForSapCatgy(final String sapCatgyCode)
	{
		final List<EnergizerProductModel> result = energizerProductDAO.getEnergizerProductListForSapCatgy(sapCatgyCode);
		return result;
	}

	@Override
	public List<EnergizerCMIRModel> getERPMaterialIdForImageReferenceId(final String imageId)
	{
		final List<EnergizerCMIRModel> result = energizerProductDAO.getERPMaterialIdForImageReferenceId(imageId);
		return result;
	}


	@Override
	public List<EnergizerCMIRModel> getAllEnergizerCMIRList()
	{
		final List<EnergizerCMIRModel> result = energizerProductDAO.getAllEnergizerCMIRList();

		return result;
	}

	@Override
	public List<EnergizerCMIRModel> getAllEnergizerCMIRListBySiteIdAndStatus(final String siteId, final String region,
			final boolean status)
	{
		final List<EnergizerCMIRModel> result = energizerProductDAO.getAllEnergizerCMIRListBySiteIdAndStatus(siteId, region,
				status);

		return result;
	}

	@Override
	public List<EnergizerPriceRowModel> getAllEnergizerPriceRowForB2BUnit(final String erpMaterialID, final String b2bUnitId)
	{
		final List<EnergizerPriceRowModel> result = energizerProductDAO.getAllEnergizerPriceRowForB2BUnit(erpMaterialID, b2bUnitId);
		return result;
	}

	@Override
	public List<EnergizerPriceRowModel> getActiveEnergizerPriceRowForCMIRModelSet(final Set<EnergizerCMIRModel> cmirSetFromDB)
	{
		final List<EnergizerPriceRowModel> result = energizerProductDAO.getActiveEnergizerPriceRowForCMIRModelSet(cmirSetFromDB);
		return result;
	}

	public List<EnergizerProductConversionFactorModel> getAllEnergizerProductConversion(final String erpMaterialId)
	{


		return energizerProductDAO.getEnergizerProductConversionLst(erpMaterialId);


	}

	@Override
	public EnergizerProductModel getProductWithCode(final String code)
	{
		return energizerProductDAO.getProductWithCode(code);
	}

	public List<EnergizerProductConversionFactorModel> getAllEnergizerProductConversionForMaterialIdAndB2BUnit(
			final String erpMaterialId, final String b2bUnitId) throws Exception
	{

		final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId, b2bUnitId);

		List<EnergizerProductConversionFactorModel> conversionFactorModels = null;
		try
		{
			if (null != energizerCMIRModel)
			{
				conversionFactorModels = energizerProductDAO.getEnergizerProductConversionLst(erpMaterialId);
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in Energizer Product Conversion Factor:::" + e);
			throw e;
		}
		return conversionFactorModels;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.EnergizerProductService#getEnergizerProductConversionByUOM(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerProductConversionFactorModel getEnergizerProductConversionByUOM(final String erpMaterialId, final String uom)
	{
		return energizerProductDAO.getEnergizerProductConversionByUOM(erpMaterialId, uom);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.product.EnergizerProductService#getProductsModelsForListAndSalesOrg(de.hybris.platform.
	 * catalog.model.CatalogVersionModel, java.util.Set, java.lang.String)
	 */
	public List<EnergizerProductModel> getProductsModelsForListAndSalesOrg(final CatalogVersionModel catalogVersion,
			final Set<String> productsSet, final String salesOrg)
	{
		return energizerProductDAO.getProductsModelsForListAndSalesOrg(catalogVersion, productsSet, salesOrg);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.product.EnergizerProductService#getNonObsoleteProductsModelsForList(de.hybris.platform.
	 * catalog.model.CatalogVersionModel, java.util.Set)
	 */
	public List<EnergizerProductModel> getNonObsoleteProductsModelsForList(final CatalogVersionModel catalogVersion,
			final Set<String> productsSet)
	{
		return energizerProductDAO.getNonObsoleteProductsModelsForList(catalogVersion, productsSet);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.EnergizerProductService#getInactiveCMIRForRegion(java.lang.String)
	 */
	@Override
	public boolean cleanUpInActiveCMIRForRegion(final String region)
	{
		//final List<EnergizerCMIRModel> inActiveCMIRList = energizerProductDAO.getInactiveCMIRForRegion(region);
		final List<EnergizerCMIRModel> inActiveCMIRList = energizerProductDAO.getAllEnergizerCMIRListBySiteIdAndStatus("", region,
				false);

		try
		{
			if (null != inActiveCMIRList && !inActiveCMIRList.isEmpty())
			{
				LOG.info("Total inactive CMIRs to be cleaned up for this region : " + region + " is : " + inActiveCMIRList.size());
				LOG.info("Cleaning up ...");
				modelService.removeAll(inActiveCMIRList);
			}
			else
			{
				LOG.info("InActive CMIR List is null/empty, nothing to clean up ...");
				return true;
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while removing inactive CMIRs from the system for this region : " + region);
			LOG.error("Error message : " + e.getMessage());
			return false;
		}
		LOG.info("Total inactive CMIRs cleaned up now : " + inActiveCMIRList.size());
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.EnergizerProductService#cleanUpInActivePricerowForRegion(java.lang.String)
	 */
	@Override
	public boolean cleanUpInActivePriceRowForRegion(final String region)
	{

		final List<EnergizerPriceRowModel> inActivePriceRowList = energizerProductDAO.getInActivePriceRowForRegion(region);

		try
		{
			if (null != inActivePriceRowList && !inActivePriceRowList.isEmpty())
			{
				LOG.info("Total inActive Pricerows to be cleaned up for this region : " + region + " is : "
						+ inActivePriceRowList.size());
				LOG.info("Cleaning up ...");
				modelService.removeAll(inActivePriceRowList);
			}
			else
			{
				LOG.info("InActive PriceRow List is null/empty, nothing to clean up ...");
				return true;
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while removing inactive price rows from the system for this region : " + region);
			LOG.error("Error message : " + e.getMessage());
			return false;
		}
		LOG.info("Total inactive Price Rows cleaned up now :" + inActivePriceRowList.size());
		return true;

	}
}
