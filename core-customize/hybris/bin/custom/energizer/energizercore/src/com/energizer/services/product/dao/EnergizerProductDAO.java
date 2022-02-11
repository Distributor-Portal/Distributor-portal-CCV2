/**
 *
 */
package com.energizer.services.product.dao;

import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.List;
import java.util.Set;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.EnergizerShippingPointModel;


/**
 * @author Bivash Pandit
 *
 *         anitha.shastry added method getOrphanedProductList()
 *
 *         anitha.shastry added method getEnergizerProductListForSapCatgy()
 *
 */
public interface EnergizerProductDAO
{


	List<EnergizerCMIRModel> getEnergizerCMIRList(String erpMaterialId) throws Exception;

	List<EnergizerCMIRModel> getEnergizerCMIRList(String erpMaterialId, String b2bUnitId) throws Exception;

	List<EnergizerProductConversionFactorModel> getEnergizerProductConversionLst(String erpMaterialId);

	List<EnergizerCMIRModel> getEnergizerCMIRListForCustomerMaterialID(String customerMaterialID, String b2bUnitId);

	List<EnergizerCMIRModel> getEnergizerCMIRListForMatIdAndCustId(String erpMaterialID, String customerMaterialID,
			String b2bUnitId);

	List<EnergizerProductModel> getEnergizerOrphanedProductList();

	EnergizerPriceRowModel getEnergizerPriceRowForB2BUnit(String erpMaterialID, String b2bUnitId);

	List<EnergizerProductModel> getEnergizerProductListForSapCatgy(String sapCatgyCode);

	List<EnergizerShippingPointModel> getShippingPointName(final String shippingPointId);

	List<EnergizerCMIRModel> getAllEnergizerCMIRList();

	List<EnergizerCMIRModel> getAllEnergizerCMIRListBySiteIdAndStatus(String siteId, String region, boolean status);

	List<EnergizerPriceRowModel> getAllEnergizerPriceRowForB2BUnit(final String erpMaterialId, final String b2bUnitId);

	List<EnergizerPriceRowModel> getActiveEnergizerPriceRowForCMIRModelSet(final Set<EnergizerCMIRModel> cmirSetFromDB);

	List<EnergizerProductModel> getEnergizerERPMaterialIDList();

	List<EnergizerCMIRModel> getERPMaterialIdForImageReferenceId(final String imageId);

	public EnergizerProductModel getProductWithCode(final String code);

	List<Object> getCatalogDownloadList(final String uid, final String siteId, final String salesOrg);

	public EnergizerProductConversionFactorModel getEnergizerProductConversionByUOM(final String erpMaterialId, final String uom);

	public List<EnergizerProductModel> getProductsListForB2BUnit(final String b2bUnitId);

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
	List<EnergizerProductModel> getProductsModelsForListAndSalesOrg(final CatalogVersionModel catalogVersion,
			final Set<String> productsSet, final String salesOrg);

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
	 * Returns the list of inactive CMIRs for that particular region
	 *
	 * @param region
	 * @return the list of inactive cmir models for the given region
	 */
	public List<EnergizerCMIRModel> getInactiveCMIRForRegion(String region);

	/**
	 * Returns the list of inactive price rows for that particular region
	 *
	 * @param region
	 * @return the list of inactive price rows for the given region
	 */
	public List<EnergizerPriceRowModel> getInActivePriceRowForRegion(String region);

}
