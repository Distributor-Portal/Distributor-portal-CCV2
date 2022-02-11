/**
 *
 */
package com.energizer.services.product;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.List;
import java.util.Set;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;


/**
 * @author Bivash Pandit
 *
 */
public interface EnergizerProductService
{

	EnergizerCMIRModel getEnergizerCMIR(String erpMaterialId)
			throws AmbiguousIdentifierException, UnknownIdentifierException, ModelNotFoundException, Exception;

	List<EnergizerCMIRModel> getEnergizerCMIRList(String erpMaterialId) throws Exception;

	EnergizerCMIRModel getEnergizerCMIR(String erpMaterialId, String b2bUnitId) throws ModelNotFoundException, Exception;

	EnergizerProductConversionFactorModel getEnergizerProductConversion(String erpMaterialId, String b2bUnitId) throws Exception;

	public EnergizerCMIRModel getEnergizerCMIRforCustomerMaterialID(String customerMaterialId, String b2bUnitId);

	public EnergizerCMIRModel getEnergizerCMIRListForMatIdAndCustId(String erpMaterialId, String customerMaterialId,
			String b2bUnitId);

	public List<EnergizerProductModel> getEnergizerOrphanedProductList();

	public EnergizerPriceRowModel getEnergizerPriceRowForB2BUnit(String erpMaterialID, String b2bUnitId);

	public List<EnergizerProductModel> getEnergizerProductListForSapCatgy(String sapCatgyCode);

	public String getShippingPointName(final String shippingPointId);

	public List<EnergizerCMIRModel> getAllEnergizerCMIRList();

	public List<EnergizerCMIRModel> getAllEnergizerCMIRListBySiteIdAndStatus(String siteId, String region, boolean status);

	public List<EnergizerPriceRowModel> getAllEnergizerPriceRowForB2BUnit(final String erpMaterialID, final String b2bUnitId);

	public List<EnergizerPriceRowModel> getActiveEnergizerPriceRowForCMIRModelSet(final Set<EnergizerCMIRModel> cmirSetFromDB);

	public List<EnergizerProductConversionFactorModel> getAllEnergizerProductConversion(final String erpMaterialId);

	public List<EnergizerProductModel> getEnergizerERPMaterialID();

	public EnergizerProductModel getProductWithCode(final String code);

	public List<EnergizerCMIRModel> getERPMaterialIdForImageReferenceId(final String imageId);

	public List<EnergizerProductConversionFactorModel> getAllEnergizerProductConversionForMaterialIdAndB2BUnit(
			String erpMaterialId, String b2bUnitId) throws Exception;

	public EnergizerProductConversionFactorModel getEnergizerProductConversionByUOM(final String erpMaterialId, final String uom);

	public String getShippingPointLocation(final String shippingPointId);

	/**
	 * Returns the product models for the list of product codes that are non-obsolete for a particular sales org
	 *
	 * @param catalogVersion
	 *           the CatalogVersion of the Product
	 *
	 * @param productsSet
	 *           the list of product codes
	 *
	 * @param salesOrg
	 *           the Sales Organisation
	 * @return the list of product models for the list of product codes
	 */
	List<EnergizerProductModel> getProductsModelsForListAndSalesOrg(CatalogVersionModel catalogVersion, Set<String> productsSet,
			String salesOrg);

	/**
	 * Returns the product models for the list of product codes that are non-obsolete
	 *
	 * @param catalogVersion
	 *           the CatalogVersion of the Product
	 *
	 * @param productsSet
	 *           the list of product codes
	 *
	 *
	 * @return the list of product models for the list of product codes
	 */
	List<EnergizerProductModel> getNonObsoleteProductsModelsForList(CatalogVersionModel catalogVersion, Set<String> productsSet);

	/**
	 * @param region
	 *           the region whether it is LATAM, EMEA, WESELL etc.
	 *
	 * @return boolean value with the status of the clean up
	 *
	 *         This method cleans up the inactive price row for a given region and returns the status of the clean up
	 */

	/**
	 * @param region
	 * @return the boolean status true/false whether the cmir clean up is successful
	 */
	public boolean cleanUpInActiveCMIRForRegion(String region);

	/**
	 * @param region
	 * @return the boolean status true/false whether the price row clean up is successful
	 */
	public boolean cleanUpInActivePriceRowForRegion(String region);
}
