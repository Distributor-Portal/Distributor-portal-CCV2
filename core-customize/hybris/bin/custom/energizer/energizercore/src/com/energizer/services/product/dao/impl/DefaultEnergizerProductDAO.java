/**
 *
 */
package com.energizer.services.product.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.EnergizerShippingPointModel;
import com.energizer.services.product.dao.EnergizerProductDAO;


/**
 * @author Bivash Pandit
 *
 *         anitha.shastry added method getOrphanedProductList()
 */
public class DefaultEnergizerProductDAO implements EnergizerProductDAO
{

	@Resource
	FlexibleSearchService flexibleSearchService;

	@Resource
	ConfigurationService configurationService;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerProductDAO.class);


	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.product.dao.EnergizerProductDAO#getMoq()
	 */
	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRList(final String erpMaterialId) throws Exception
	{
		FlexibleSearchQuery query = null;
		try
		{
			final String queryString = //
					"SELECT {p:" + EnergizerCMIRModel.PK + "}" //
							+ "FROM {" + EnergizerCMIRModel._TYPECODE + " AS p} "//
							+ "WHERE " + "{p:" + EnergizerCMIRModel.ERPMATERIALID + "}=?erpMaterialId " + "AND {p:"
							+ EnergizerCMIRModel.ISACTIVE + "}=?isActive";

			query = new FlexibleSearchQuery(queryString);
			query.addQueryParameter("erpMaterialId", erpMaterialId);
			query.addQueryParameter("isActive", true);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured in fetching CMIR list:::: " + e);
			throw e;
		}
		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.product.dao.EnergizerProductDAO#getEnergizerCMIRList(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRList(final String erpMaterialId, final String b2bUnitId)
	{

		FlexibleSearchQuery query1 = null;
		try
		{
			EnergizerB2BUnitModel energizerB2BUnitModel = null;
			final String queryString = //
					"SELECT {p:" + EnergizerB2BUnitModel.PK + "}" //
							+ "FROM {" + EnergizerB2BUnitModel._TYPECODE + " AS p} "//
							+ "WHERE " + "{p:" + EnergizerB2BUnitModel.UID + "}=?b2bUnitId ";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
			query.addQueryParameter("b2bUnitId", b2bUnitId);
			LOG.info("Fetching b2bunit Query " + query);

			final List<EnergizerB2BUnitModel> b2bUnitModels = flexibleSearchService.<EnergizerB2BUnitModel> search(query)
					.getResult();
			for (final Iterator iterator = b2bUnitModels.iterator(); iterator.hasNext();)
			{
				energizerB2BUnitModel = (EnergizerB2BUnitModel) iterator.next();
			}

			if (null != energizerB2BUnitModel)
			{
				final String queryString1 = //
						"SELECT {p:" + EnergizerCMIRModel.PK + "}" //
								+ "FROM {" + EnergizerCMIRModel._TYPECODE + " AS p} "//
								+ "WHERE " + "{p:" + EnergizerCMIRModel.ERPMATERIALID + "}=?erpMaterialId "//
								+ "AND {p:" + EnergizerCMIRModel.B2BUNIT + "}=?energizerB2BUnitModel ";

				query1 = new FlexibleSearchQuery(queryString1);

				query1.addQueryParameter("erpMaterialId", erpMaterialId);
				query1.addQueryParameter("energizerB2BUnitModel", energizerB2BUnitModel);
				LOG.info("Fetching CMIR Query " + queryString1);
			}
			else
			{
				LOG.info("energizerB2BUnitModel is null, so unable to fetch CMIR for product : " + erpMaterialId + ", b2bUnitId : "
						+ b2bUnitId);
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occurred in fetching CMIR list for product : " + erpMaterialId + ", b2bUnitId : " + b2bUnitId
					+ ", exception cause : " + e);
			throw e;
		}
		return flexibleSearchService.<EnergizerCMIRModel> search(query1).getResult();
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.product.dao.EnergizerProductDAO#getEnergizerProductConversionLst(java.lang.String)
	 */
	@Override
	public List<EnergizerProductConversionFactorModel> getEnergizerProductConversionLst(final String erpMaterialId)
	{

		final String queryString = //
				"SELECT {p:" + EnergizerProductConversionFactorModel.PK + "}" //
						+ "FROM {" + EnergizerProductConversionFactorModel._TYPECODE + " AS p} "//
						+ "WHERE " + "{p:" + EnergizerProductConversionFactorModel.ERPMATERIALID + "}=?erpMaterialId ";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("erpMaterialId", erpMaterialId);

		return flexibleSearchService.<EnergizerProductConversionFactorModel> search(query).getResult();
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.product.dao.EnergizerProductDAO#getEnergizerCMIRListForCustomerMaterialID(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRListForCustomerMaterialID(final String customerMaterialID,
			final String b2bUnitId)
	{
		final String queryString = //
				"SELECT {p:" + EnergizerCMIRModel.PK + "}" //
						+ "FROM {" + EnergizerCMIRModel._TYPECODE + " AS p JOIN EnergizerB2BUnit as eu ON {p.b2bUnit}={eu.pk}} "//
						+ "WHERE " + "{p:" + EnergizerCMIRModel.CUSTOMERMATERIALID + "}=?erpMaterialId "//
						+ "AND {eu:" + EnergizerB2BUnitModel.UID + "}=?energizerB2BUnitModel ";


		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);

		query.addQueryParameter("erpMaterialId", customerMaterialID);
		query.addQueryParameter("energizerB2BUnitModel", b2bUnitId);

		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
	}

	@Override
	public List<EnergizerCMIRModel> getERPMaterialIdForImageReferenceId(final String imageId)
	{

		final String queryString = "SELECT {c:" + EnergizerCMIRModel.PK + "}"//
				+ "FROM {" + EnergizerCMIRModel._TYPECODE + " AS c  JOIN EnergizerProduct as p ON {c.ERPMATERIALID}={p.CODE}} "//
				+ "WHERE " + "{p:" + EnergizerProductModel.IMAGEREFERENCEID + "}=?ImageRefId";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);

		query.addQueryParameter("ImageRefId", imageId);

		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();


	}


	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRListForMatIdAndCustId(final String erpMaterialId,
			final String customerMaterialID, final String b2bUnitId)
	{

		/*
		 * final String queryString =
		 * "SELECT {C.PK},{C.ERPMATERIALID},{C.CUSTOMERMATERIALID},{C.SHIPPINGPOINT}, {C.UOM} from  " +
		 * " {EnergizerCMIR as C   " + " JOIN EnergizerProduct as P ON {P:code}={C:ERPMaterialId}  " +
		 * " AND {C:customerMaterialId}= ?customerMaterialId " + "AND {C:ERPMaterialId}= ?erpMaterialId" + " WHERE {eu:" +
		 * EnergizerB2BUnitModel.UID + "}=?energizerB2BUnitModel" + "}";
		 */

		final String queryString = "SELECT {C.PK},{C.ERPMATERIALID},{C.CUSTOMERMATERIALID},{C.SHIPPINGPOINT}, {C.UOM} from "
				+ " {EnergizerCMIR as C JOIN EnergizerProduct as P ON {P.code}={C.ERPMaterialId} JOIN EnergizerB2BUnit as B ON {C.b2bUnit}={B.pk}}"
				+ " where {C.CUSTOMERMATERIALID} =?customerMaterialId AND {C.ERPMATERIALID}=?erpMaterialId AND {B.UID}=?b2bUnitId";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);

		query.addQueryParameter("erpMaterialId", erpMaterialId);
		query.addQueryParameter("customerMaterialId", customerMaterialID);
		query.addQueryParameter("b2bUnitId", b2bUnitId);

		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getEnergizerOrphanedProductList()
	 */
	@Override
	public List<EnergizerProductModel> getEnergizerOrphanedProductList()
	{
		final String queryString = "SELECT {prod.pk}, {prod.code} FROM {EnergizerProduct AS prod} WHERE not EXISTS "
				+ "({{select * from {CategoryProductRelation as cat} WHERE {cat.target}={prod:pk} }})";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);

		return flexibleSearchService.<EnergizerProductModel> search(query).getResult();
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getEnergizerPriceRowForB2BUnit(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerPriceRowModel getEnergizerPriceRowForB2BUnit(final String erpMaterialId, final String b2bUnitId)
	{
		final String queryString = "select {enrprice.pk} from " + "{EnergizerCMIR as cmir JOIN EnergizerB2BUnit AS myb2bunit ON "
				+ "{cmir.b2bUnit}={myb2bunit.pk} JOIN EnergizerProduct AS prod ON "
				+ "{cmir.erpMaterialId}={prod.code} JOIN EnergizerPriceRow AS enrprice ON "
				+ " {enrprice.b2bUnit}={myb2bunit.pk} and {enrprice.product}={prod.pk} and {cmir.custPriceUOM}={enrprice.priceUOM} "
				+ "} WHERE {myb2bunit.uid}=?b2bUnitId and {cmir.erpMaterialId}=?erpMaterialId and"
				+ "{enrprice.isActive}=1 and {cmir.custPriceUOM} IS NOT NULL and {enrprice.priceUOM} IS NOT NULL";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("erpMaterialId", erpMaterialId);
		query.addQueryParameter("b2bUnitId", b2bUnitId);
		query.setCount(1);

		final List<EnergizerPriceRowModel> result = flexibleSearchService.<EnergizerPriceRowModel> search(query).getResult();
		if (result != null && !result.isEmpty())
		{
			return result.get(0);
		}

		return null;
	}

	@Override
	public List<EnergizerProductModel> getEnergizerProductListForSapCatgy(final String sapCatgyCode)
	{
		final String queryString = "SELECT {prod.pk} FROM {EnergizerProduct AS prod} " + " WHERE {prod:"
				+ EnergizerProductModel.SAPCATEGORYCONCATVALUE + "}=?sapCatgyCode";
		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("sapCatgyCode", sapCatgyCode);
		return flexibleSearchService.<EnergizerProductModel> search(query).getResult();
	}




	@Override
	public List<EnergizerShippingPointModel> getShippingPointName(final String shippingPointId)
	{
		final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery(
				"SELECT {pk} FROM {EnergizerShippingPoint} where {shippingPointId}=?shippingPointId");
		retreiveQuery.addQueryParameter("shippingPointId", shippingPointId);
		return flexibleSearchService.<EnergizerShippingPointModel> search(retreiveQuery).getResult();

	}

	@Override
	public List<EnergizerCMIRModel> getAllEnergizerCMIRList()
	{
		final String querystring = //
				"SELECT {p:" + EnergizerCMIRModel.PK + "}" //
						+ "FROM {" + EnergizerCMIRModel._TYPECODE + " AS p} WHERE {p:" + EnergizerCMIRModel.ISACTIVE + "}=?isActive";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(querystring);
		query.addQueryParameter("isActive", true);
		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
	}

	@Override
	public List<EnergizerCMIRModel> getAllEnergizerCMIRListBySiteIdAndStatus(final String siteId, final String region,
			final boolean status)
	{
		String queryString = StringUtils.EMPTY;
		FlexibleSearchQuery query = null;

		try
		{
			if (null != region && !StringUtils.isEmpty(region))
			{
				if (region.equalsIgnoreCase(EnergizerCoreConstants.EMEA))
				{
					queryString = "SELECT {p:" + EnergizerCMIRModel.PK + "} FROM {" + EnergizerCMIRModel._TYPECODE + " AS p} WHERE {"
							+ EnergizerCMIRModel.SITEID + "}=?siteId AND {" + EnergizerCMIRModel.ISACTIVE + "}=?isActive";
					query = new FlexibleSearchQuery(queryString);
					query.addQueryParameter("siteId", EnergizerCoreConstants.SITE_PERSONALCAREEMEA);
				}
				else if (region.equalsIgnoreCase(EnergizerCoreConstants.LATAM))
				{
					// Check {isWeSellProduct}=0 for CMIR Monitor and {isWeSellProduct}=0 or NULL for clean up
					queryString = "SELECT {p:" + EnergizerCMIRModel.PK + "} FROM {" + EnergizerCMIRModel._TYPECODE + " AS p} WHERE {"
							+ EnergizerCMIRModel.SITEID + "}=?siteId AND {" + EnergizerCMIRModel.ISACTIVE + "}=?isActive AND {"
							+ EnergizerCMIRModel.ISWESELLPRODUCT + "}=?isWeSellProduct";
					/*
					 * if (status) { queryString = "SELECT {p:" + EnergizerCMIRModel.PK + "} FROM {" +
					 * EnergizerCMIRModel._TYPECODE + " AS p} WHERE {" + EnergizerCMIRModel.SITEID + "}=?siteId AND {" +
					 * EnergizerCMIRModel.ISACTIVE + "}=?isActive AND {" + EnergizerCMIRModel.ISWESELLPRODUCT +
					 * "}=?isWeSellProduct"; } else {
					 *
					 * queryString = "SELECT {p:" + EnergizerCMIRModel.PK + "} FROM {" + EnergizerCMIRModel._TYPECODE +
					 * " AS p} WHERE {" + EnergizerCMIRModel.SITEID + "}=?siteId AND {" + EnergizerCMIRModel.ISACTIVE +
					 * "}=?isActive AND ({" + EnergizerCMIRModel.ISWESELLPRODUCT + "}=?isWeSellProduct OR {" +
					 * EnergizerCMIRModel.ISWESELLPRODUCT + "} IS NULL) AND {" + EnergizerCMIRModel.B2BUNIT + "} IS NULL";
					 *
					 * queryString = "SELECT {p:" + EnergizerCMIRModel.PK + "} FROM {" + EnergizerCMIRModel._TYPECODE +
					 * " AS p} WHERE {" + EnergizerCMIRModel.SITEID + "}=?siteId AND {" + EnergizerCMIRModel.ISACTIVE +
					 * "}=?isActive AND {" + EnergizerCMIRModel.ISWESELLPRODUCT + "}=?isWeSellProduct"; }
					 */

					query = new FlexibleSearchQuery(queryString);
					query.addQueryParameter("siteId", EnergizerCoreConstants.SITE_PERSONALCARE);
					query.addQueryParameter("isWeSellProduct", false);
				}
				else if (region.equalsIgnoreCase(EnergizerCoreConstants.WESELL))
				{
					queryString = "SELECT {p:" + EnergizerCMIRModel.PK + "} FROM {" + EnergizerCMIRModel._TYPECODE + " AS p} WHERE {"
							+ EnergizerCMIRModel.SITEID + "}=?siteId AND {" + EnergizerCMIRModel.ISWESELLPRODUCT + "} IS NOT NULL AND {"
							+ EnergizerCMIRModel.ISWESELLPRODUCT + "}=?isWeSellProduct AND {" + EnergizerCMIRModel.ISACTIVE
							+ "}=?isActive";

					query = new FlexibleSearchQuery(queryString);
					query.addQueryParameter("siteId", EnergizerCoreConstants.SITE_PERSONALCARE);
					query.addQueryParameter("isWeSellProduct", true);
				}
				else if (region.equalsIgnoreCase(EnergizerCoreConstants.ALL))
				{
					queryString = "SELECT {p:" + EnergizerCMIRModel.PK + "} FROM {" + EnergizerCMIRModel._TYPECODE + " AS p} WHERE {"
							+ EnergizerCMIRModel.ISACTIVE + "}=?isActive";
					query = new FlexibleSearchQuery(queryString);
				}
			}
			else
			{
				LOG.info("Region is null/empty, so unable to fetch CMIR list from DB  !!!");
			}

			//Status 'true' for CMIR Monitor job, 'false' for CMIR price row clean up job. Same logic in the query, only difference is the isActive status
			query.addQueryParameter("isActive", status);
			//query.addQueryParameter("siteId", siteId);
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching CMIR list from DB ::: " + e);
			e.printStackTrace();
		}

		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
	}

	@Override
	public List<EnergizerPriceRowModel> getAllEnergizerPriceRowForB2BUnit(final String erpMaterialId, final String b2bUnitId)
	{
		final String queryString = "select {enrprice.pk} from " + "{EnergizerCMIR as cmir JOIN EnergizerB2BUnit AS myb2bunit ON "
				+ "{cmir.b2bUnit}={myb2bunit.pk} JOIN EnergizerProduct AS prod ON "
				+ "{cmir.erpMaterialId}={prod.code} JOIN EnergizerPriceRow AS enrprice ON "
				+ " {enrprice.b2bUnit}={myb2bunit.pk} and {enrprice.product}={prod.pk} " + "} " + " WHERE "
				+ "{myb2bunit.uid}=?b2bUnitId and " + "{cmir.erpMaterialId}=?erpMaterialId";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("erpMaterialId", erpMaterialId);
		query.addQueryParameter("b2bUnitId", b2bUnitId);

		return flexibleSearchService.<EnergizerPriceRowModel> search(query).getResult();

	}



	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getEnergizerERPMaterialIDList()
	 */
	@Override
	public List<EnergizerProductModel> getEnergizerERPMaterialIDList()
	{
		//final String queryString = "select * from {EnergizerProduct as enrproduct}";

		/*
		 * final String queryString = // "SELECT {p:" + EnergizerProductModel.PK + "}" // + "FROM {" +
		 * EnergizerProductModel._TYPECODE + " AS p}";
		 */

		// Fetch only the relevant products for this customer whose CMIRs are active instead of fetching all the products from DB
		final String queryString = "SELECT {p:" + EnergizerProductModel.PK + "} FROM {" + EnergizerProductModel._TYPECODE
				+ " AS p JOIN " + EnergizerCMIRModel._TYPECODE + " AS c ON {p:" + EnergizerProductModel.CODE + "}={c:"
				+ EnergizerCMIRModel.ERPMATERIALID + "}} WHERE {c:" + EnergizerCMIRModel.ISACTIVE + "}=?isActive";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);

		query.addQueryParameter("isActive", true);

		LOG.info("Query results: " + flexibleSearchService.<EnergizerProductModel> search(query).getResult().size());

		return flexibleSearchService.<EnergizerProductModel> search(query).getResult();

	}

	@Override
	public EnergizerProductModel getProductWithCode(final String code)
	{
		EnergizerProductModel energizerProductModel = null;
		try
		{
			final String queryString = "select {product.PK} from " + "{EnergizerProduct as product}" + " WHERE"
					+ " {product.code}=?code ";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
			query.addQueryParameter("code", code);
			energizerProductModel = flexibleSearchService.<EnergizerProductModel> search(query).getResult().get(0);
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while fetching product data:::" + e);
			throw e;
		}
		return energizerProductModel;
	}



	@Override
	public List<Object> getCatalogDownloadList(final String uid, final String siteId, final String salesOrg)
	{

		SearchResult<Object> result = null;
		try
		{
			if (siteId.equalsIgnoreCase(configurationService.getConfiguration().getString("site.personalCare")))
			{
				final boolean isSalesRepUser = (boolean) sessionService.getAttribute("isSalesRepUserLoggedIn");

				final HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("uid", uid);
				params.put("siteId", siteId);
				params.put("isActive", true);
				FlexibleSearchQuery flexiQuery = null;

				if (!isSalesRepUser)
				{
					/* {cmir.orderingUnit} */
					// Backed up before obsolete overwrite issue
					/*-final String queryString = "SELECT DISTINCT {cmir.erpMaterialId},{cmir.customerMaterialId}, "
							+ "{cmir.uom},{pc.conversionMultiplier},{ep.ean},{ep.name},{enrprice.price},{me.measurement} as length,{mr.measurement} as width,{mu.measurement} as height,{me.measuringUnits} as dimensionInUnits,{mt.measurement} as weight,{mt.measuringUnits} as weightInUnits,{mv.measurement} as volume,{mv.measuringUnits} as volumeUOM"
							+ " FROM {EnergizerCMIR as cmir LEFT JOIN EnergizerProduct AS ep ON {cmir.erpMaterialId}={ep.code} JOIN EnergizerProductConversionFactor as pc ON {ep.code}={pc.erpMaterialId} "
							+ "JOIN MetricUnit as mu ON {pc.packageHeight}={mu.pk} JOIN MetricUnit as me ON {pc.packageLength}={me.pk} JOIN MetricUnit as mt ON {pc.packageWeight}={mt.pk} JOIN MetricUnit as mv ON {pc.packageVolume}={mv.pk}"
							+ "JOIN MetricUnit as mr ON {pc.packageWidth}={mr.pk} JOIN EnergizerB2BUnit as b ON {cmir.b2bUnit}={b.pk} JOIN EnergizerPriceRow AS enrprice ON {enrprice.b2bUnit}={b.pk} and {enrprice.product}={ep.pk} "
							+ "JOIN CMSSite as site ON {cmir.siteId}={site.uid} JOIN catalog as c ON {ep.catalog}={c.pk} JOIN CatalogVersion as cv ON {c.activeCatalogversion}={cv.pk}} WHERE {ep:obsolete}=0 AND {b.uid}=?uid AND {cmir.uom}={pc.alternateUOM} AND {cmir.isActive}=1 AND {site.uid}=?siteId AND {enrprice.isActive}=?isActive";*/

					final String queryString = "SELECT DISTINCT {cmir.erpMaterialId},{cmir.customerMaterialId}, "
							+ "{cmir.uom},{cmir.orderingUnit},{pc.conversionMultiplier},{ep.ean},{ep.name},{enrprice.price},{me.measurement} as length,{mr.measurement} as width,{mu.measurement} as height,{me.measuringUnits} as dimensionInUnits,{mt.measurement} as weight,{mt.measuringUnits} as weightInUnits,{mv.measurement} as volume,{mv.measuringUnits} as volumeUOM"
							+ " FROM {EnergizerCMIR as cmir LEFT JOIN EnergizerProduct AS ep ON {cmir.erpMaterialId}={ep.code} JOIN EnergizerProductConversionFactor as pc ON {ep.code}={pc.erpMaterialId} "
							+ "JOIN MetricUnit as mu ON {pc.packageHeight}={mu.pk} JOIN MetricUnit as me ON {pc.packageLength}={me.pk} JOIN MetricUnit as mt ON {pc.packageWeight}={mt.pk} JOIN MetricUnit as mv ON {pc.packageVolume}={mv.pk}"
							+ "JOIN MetricUnit as mr ON {pc.packageWidth}={mr.pk} JOIN EnergizerB2BUnit as b ON {cmir.b2bUnit}={b.pk} JOIN EnergizerPriceRow AS enrprice ON {enrprice.b2bUnit}={b.pk} and {enrprice.product}={ep.pk} "
							+ "JOIN CMSSite as site ON {cmir.siteId}={site.uid} JOIN catalog as c ON {ep.catalog}={c.pk} JOIN CatalogVersion as cv ON {c.activeCatalogversion}={cv.pk}} WHERE {ep:nonObsoleteSalesOrgsString} IS NOT NULL AND {b.uid}=?uid AND {cmir.uom}={pc.alternateUOM} AND {cmir.isActive}=1 AND {site.uid}=?siteId AND {enrprice.isActive}=?isActive AND {ep.nonObsoleteSalesOrgsString} LIKE ?salesOrg ";


					flexiQuery = new FlexibleSearchQuery(queryString, params);
					//flexiQuery.addQueryParameter("productsList", productCodeList);
					flexiQuery.addQueryParameter("salesOrg", '%' + salesOrg + '%');
					LOG.info("Query for LATAM Non-Sales Rep ::: " + flexiQuery);
					flexiQuery.setResultClassList(Arrays.asList(String.class, String.class, String.class, Integer.class,Integer.class, String.class,
							String.class, Double.class, Double.class, Double.class, Double.class, String.class, Double.class,
							String.class, Double.class, String.class));
				}
				else
				{
					// For WeSell Sales Rep, price is removed as prices are not maintained in Hybris.
					/* {cmir.orderingUnit} */
					// Backed up before obsolete overwrite issue
					/*-final String queryString = "SELECT DISTINCT {cmir.erpMaterialId},{cmir.customerMaterialId}, "
							+ "{cmir.uom},{cmir.orderingUnit},{pc.conversionMultiplier},{ep.ean},{ep.name},{me.measurement} as length,{mr.measurement} as width,{mu.measurement} as height,{me.measuringUnits} as dimensionInUnits,{mt.measurement} as weight,{mt.measuringUnits} as weightInUnits,{mv.measurement} as volume,{mv.measuringUnits} as volumeUOM"
							+ " FROM {EnergizerCMIR as cmir LEFT JOIN EnergizerProduct AS ep ON {cmir.erpMaterialId}={ep.code} JOIN EnergizerProductConversionFactor as pc ON {ep.code}={pc.erpMaterialId} "
							+ "JOIN MetricUnit as mu ON {pc.packageHeight}={mu.pk} JOIN MetricUnit as me ON {pc.packageLength}={me.pk} JOIN MetricUnit as mt ON {pc.packageWeight}={mt.pk} JOIN MetricUnit as mv ON {pc.packageVolume}={mv.pk}"
							+ "JOIN MetricUnit as mr ON {pc.packageWidth}={mr.pk} JOIN EnergizerB2BUnit as b ON {cmir.b2bUnit}={b.pk} JOIN EnergizerPriceRow AS enrprice ON {enrprice.b2bUnit}={b.pk} and {enrprice.product}={ep.pk} "
							+ "JOIN CMSSite as site ON {cmir.siteId}={site.uid} JOIN catalog as c ON {ep.catalog}={c.pk} JOIN CatalogVersion as cv ON {c.activeCatalogversion}={cv.pk}} WHERE {ep:obsolete}=0 AND {b.uid}=?uid AND {cmir.uom}={pc.alternateUOM} AND {cmir.isActive}=1 AND {site.uid}=?siteId AND {enrprice.isActive}=?isActive";*/

					final String queryString = "SELECT DISTINCT {cmir.erpMaterialId},{cmir.customerMaterialId}, "
							+ "{cmir.uom},{cmir.orderingUnit},{pc.conversionMultiplier},{ep.ean},{ep.name},{me.measurement} as length,{mr.measurement} as width,{mu.measurement} as height,{me.measuringUnits} as dimensionInUnits,{mt.measurement} as weight,{mt.measuringUnits} as weightInUnits,{mv.measurement} as volume,{mv.measuringUnits} as volumeUOM"
							+ " FROM {EnergizerCMIR as cmir LEFT JOIN EnergizerProduct AS ep ON {cmir.erpMaterialId}={ep.code} JOIN EnergizerProductConversionFactor as pc ON {ep.code}={pc.erpMaterialId} "
							+ "JOIN MetricUnit as mu ON {pc.packageHeight}={mu.pk} JOIN MetricUnit as me ON {pc.packageLength}={me.pk} JOIN MetricUnit as mt ON {pc.packageWeight}={mt.pk} JOIN MetricUnit as mv ON {pc.packageVolume}={mv.pk}"
							+ "JOIN MetricUnit as mr ON {pc.packageWidth}={mr.pk} JOIN EnergizerB2BUnit as b ON {cmir.b2bUnit}={b.pk} JOIN EnergizerPriceRow AS enrprice ON {enrprice.b2bUnit}={b.pk} and {enrprice.product}={ep.pk} "
							+ "JOIN CMSSite as site ON {cmir.siteId}={site.uid} JOIN catalog as c ON {ep.catalog}={c.pk} JOIN CatalogVersion as cv ON {c.activeCatalogversion}={cv.pk}} WHERE {ep:nonObsoleteSalesOrgsString} IS NOT NULL AND {b.uid}=?uid AND {cmir.uom}={pc.alternateUOM} AND {cmir.isActive}=1 AND {site.uid}=?siteId AND {enrprice.isActive}=?isActive AND {ep.nonObsoleteSalesOrgsString} LIKE ?salesOrg ";

					flexiQuery = new FlexibleSearchQuery(queryString, params);
					//flexiQuery.addQueryParameter("productsList", productCodeList);
					flexiQuery.addQueryParameter("salesOrg", '%' + salesOrg + '%');
					LOG.info("Query for LATAM Sales Rep ::: " + flexiQuery);
					flexiQuery.setResultClassList(Arrays.asList(String.class, String.class, String.class, Integer.class, Integer.class,
							String.class, String.class, Double.class, Double.class, Double.class, String.class, Double.class,
							String.class, Double.class, String.class));
				}

				result = flexibleSearchService.search(flexiQuery);
				//LOG.info("Catalog download result" + result.getResult());
				return result.getResult();
			}
			else
			{
				/*
				 * final String queryString =
				 * "SELECT DISTINCT {cmir.erpMaterialId},{cmir.customerMaterialId}, {cmir.uom},{cmir.orderingUnit} " +
				 * " FROM {EnergizerCMIR as cmir JOIN EnergizerProduct AS ep ON {cmir.erpMaterialId}={ep.code}" +
				 * "JOIN EnergizerB2BUnit as b ON {cmir.b2bUnit}={b.pk} " +
				 * "JOIN CMSSite as site ON {cmir.siteId}={site.uid} } WHERE {b.uid}=?uid AND  {cmir.isActive}=1 AND {site.uid}=?siteId "
				 * ;
				 */

				final String queryString = "SELECT DISTINCT {cmir.erpMaterialId},{cmir.customerMaterialId}, "
						+ "{cmir.uom},{cmir.orderingUnit},{ep.ean},{ep.name},{enrprice.price},{me.measurement} as length,{mr.measurement} as width,{mu.measurement} as height,{me.measuringUnits} as dimensionInUnits,{mt.measurement} as weight,{mt.measuringUnits} as weightInUnits"
						+ " FROM {EnergizerCMIR as cmir LEFT JOIN EnergizerProduct AS ep ON {cmir.erpMaterialId}={ep.code} JOIN EnergizerProductConversionFactor as pc ON {ep.code}={pc.erpMaterialId} "
						+ "JOIN MetricUnit as mu ON {pc.packageHeight}={mu.pk} JOIN MetricUnit as me ON {pc.packageLength}={me.pk} JOIN MetricUnit as mt ON {pc.packageWeight}={mt.pk} "
						+ "JOIN MetricUnit as mr ON {pc.packageWidth}={mr.pk} JOIN EnergizerB2BUnit as b ON {cmir.b2bUnit}={b.pk} JOIN EnergizerPriceRow AS enrprice ON {enrprice.b2bUnit}={b.pk} and {enrprice.product}={ep.pk} "
						+ "JOIN CMSSite as site ON {cmir.siteId}={site.uid} JOIN catalog as c ON {ep.catalog}={c.pk} JOIN CatalogVersion as cv ON {c.activeCatalogversion}={cv.pk}} WHERE {ep:obsolete}=0 AND {b.uid}=?uid AND {cmir.uom}={pc.alternateUOM} AND {cmir.isActive}=1 AND {site.uid}=?siteId AND {enrprice.isActive}=?isActive";


				/*-final String queryString = "SELECT DISTINCT {cmir.erpMaterialId},{cmir.customerMaterialId}, "
						+ "{cmir.uom},{cmir.orderingUnit},{ep.ean},{ep.name},{enrprice.price},{me.measurement} as length,{mr.measurement} as width,{mu.measurement} as height,{me.measuringUnits} as dimensionInUnits"
						+ " FROM {EnergizerCMIR as cmir LEFT JOIN EnergizerProduct AS ep ON {cmir.erpMaterialId}={ep.code} JOIN EnergizerProductConversionFactor as pc ON {ep.code}={pc.erpMaterialId} "
						+ "JOIN MetricUnit as mu ON {pc.packageHeight}={mu.pk} JOIN MetricUnit as me ON {pc.packageLength}={me.pk} "
						+ "JOIN MetricUnit as mr ON {pc.packageWidth}={mr.pk} JOIN EnergizerB2BUnit as b ON {cmir.b2bUnit}={b.pk} JOIN EnergizerPriceRow AS enrprice ON {enrprice.b2bUnit}={b.pk} and {enrprice.product}={ep.pk} "
						+ "JOIN CMSSite as site ON {cmir.siteId}={site.uid} JOIN catalog as c ON {ep.catalog}={c.pk} JOIN CatalogVersion as cv ON {c.activeCatalogversion}={cv.pk}} WHERE {ep:obsolete}=0 AND {b.uid}=?uid AND {cmir.uom}={pc.alternateUOM} AND {cmir.isActive}=1 AND {site.uid}=?siteId AND {enrprice.isActive}=?isActive";*/


				final HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("uid", uid);
				params.put("siteId", siteId);
				params.put("isActive", true);

				final FlexibleSearchQuery flexiQuery = new FlexibleSearchQuery(queryString, params);
				LOG.info("Query for EMEA ::: " + flexiQuery);
				/* flexiQuery.setResultClassList(Arrays.asList(String.class, String.class, String.class, Integer.class)); */

				flexiQuery.setResultClassList(
						Arrays.asList(String.class, String.class, String.class, Integer.class, String.class, String.class, Double.class,
								Double.class, Double.class, Double.class, String.class, Double.class, String.class));

				/*-flexiQuery.setResultClassList(Arrays.asList(String.class, String.class, String.class, Integer.class, String.class,
						String.class, Double.class, Double.class, Double.class, Double.class, String.class));*/
				result = flexibleSearchService.search(flexiQuery);
				//LOG.info("Catalog download result" + result.getResult());
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured while fetching  catalog list ::: " + e);
			throw e;
		}

		return result.getResult();

	}



	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getEnergizerProductConversionByUOM(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerProductConversionFactorModel getEnergizerProductConversionByUOM(final String erpMaterialId, final String uom)
	{
		final String queryString = //
				"SELECT {p:" + EnergizerProductConversionFactorModel.PK + "}" //
						+ "FROM {" + EnergizerProductConversionFactorModel._TYPECODE + " AS p} "//
						+ "WHERE " + "{p:" + EnergizerProductConversionFactorModel.ERPMATERIALID + "}=?erpMaterialId  AND {p:"
						+ EnergizerProductConversionFactorModel.ALTERNATEUOM + "}=?uom";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("erpMaterialId", erpMaterialId);
		query.addQueryParameter("uom", uom);
		final List<EnergizerProductConversionFactorModel> result = flexibleSearchService
				.<EnergizerProductConversionFactorModel> search(query).getResult();
		if (result != null && !result.isEmpty())
		{
			return result.get(0);
		}
		return null;
	}

	@Override
	public List<EnergizerProductModel> getProductsListForB2BUnit(final String b2bUnitId)
	{
		final String queryString = "SELECT {p.pk} FROM {EnergizerProduct AS p JOIN EnergizerCMIR AS c ON {p.code}={c.erpMaterialID} JOIN EnergizerB2BUnit AS b ON {c.b2bUnit}={b.pk}} WHERE {p.nonObsoleteSalesOrgsString} IS NOT NULL AND {c.isActive}=?isActive AND {b.uid}=?b2bUnitId ORDER BY {p.code} ASC";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("isActive", true);
		query.addQueryParameter("b2bUnitId", b2bUnitId);
		final List<EnergizerProductModel> result = flexibleSearchService.<EnergizerProductModel> search(query).getResult();

		if (null != result && !result.isEmpty())
		{
			return result;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getInactiveCMIRForRegion(java.lang.String)
	 */
	public List<EnergizerProductModel> getProductsModelsForListAndSalesOrg(final CatalogVersionModel catalogVersion,
			final Set<String> productsSet, final String salesOrg)
	{
		validateParameterNotNull(catalogVersion, "CatalogVersion must not be null !");
		validateParameterNotNull(productsSet, "Products Set must not be null/empty !");
		validateParameterNotNull(salesOrg, "Sales Org must not be null !");

		List<EnergizerProductModel> productsModelList = new ArrayList<EnergizerProductModel>();
		try
		{
			/*
			 * final String queryString = "SELECT DISTINCT {p.PK} from " + "{EnergizerProduct as p}" + " WHERE" +
			 * " {p.code} IN (?productsSet) AND {p.catalogVersion}=?catalogVersion AND {p.nonObsoleteSalesOrgsString} IS NOT NULL AND {p.nonObsoleteSalesOrgsString} LIKE ?salesOrg"
			 * ;
			 */
			final String queryString = "SELECT DISTINCT {p.PK} from " + "{EnergizerProduct as p}" + " WHERE"
					+ " {p.code} IN (?productsSet) AND {p.catalogVersion}=?catalogVersion";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
			query.addQueryParameter("productsSet", productsSet);
			query.addQueryParameter("catalogVersion", catalogVersion);
			//query.addQueryParameter("salesOrg", "%" + salesOrg + "%");

			productsModelList = flexibleSearchService.<EnergizerProductModel> search(query).getResult();
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while fetching product models :::" + e);
			throw e;
		}
		return productsModelList;
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.product.dao.EnergizerProductDAO#getNonObsoleteProductsModelsForList(de.hybris.platform.
	 * catalog.model.CatalogVersionModel, java.util.Set)
	 */
	public List<EnergizerProductModel> getNonObsoleteProductsModelsForList(final CatalogVersionModel catalogVersion,
			final Set<String> productsSet)
	{

		validateParameterNotNull(catalogVersion, "CatalogVersion must not be null !");
		validateParameterNotNull(productsSet, "Products Set must not be null/empty !");

		List<EnergizerProductModel> productsModelList = new ArrayList<EnergizerProductModel>();
		try
		{
			/*
			 * final String queryString = "SELECT DISTINCT {p.PK} from " + "{EnergizerProduct as p}" + " WHERE" +
			 * " {p.code} IN (?productsSet) AND {p.catalogVersion}=?catalogVersion AND {p.obsolete}=?obsolete";
			 */
			final String queryString = "SELECT DISTINCT {p.PK} from " + "{EnergizerProduct as p}" + " WHERE"
					+ " {p.code} IN (?productsSet) AND {p.catalogVersion}=?catalogVersion";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
			query.addQueryParameter("productsSet", productsSet);
			query.addQueryParameter("catalogVersion", catalogVersion);
			//query.addQueryParameter("obsolete", false);
			productsModelList = flexibleSearchService.<EnergizerProductModel> search(query).getResult();
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while fetching product models :::" + e);
			throw e;
		}
		return productsModelList;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getInactiveCMIRForRegion(java.lang.String)
	 */
	@Override
	public List<EnergizerCMIRModel> getInactiveCMIRForRegion(final String region)
	{
		FlexibleSearchQuery query = null;
		String queryString = "";
		try
		{
			if (!region.equalsIgnoreCase(EnergizerCoreConstants.ALL))
			{
				queryString = "SELECT {c.pk} FROM {" + EnergizerCMIRModel._TYPECODE
						+ " AS c JOIN EnergizerB2BUnit AS b ON {c.b2bUnit}={b.pk}} WHERE {c.isActive}=?isActive AND {b.salesArea}=?region";
				query = new FlexibleSearchQuery(queryString);
				query.addQueryParameter("region", region);
			}
			else
			{
				queryString = "SELECT {c.pk} FROM {" + EnergizerCMIRModel._TYPECODE + " AS c} WHERE {c.isActive}=?isActive";
				query = new FlexibleSearchQuery(queryString);
			}
			query.addQueryParameter("isActive", false);

			final List<EnergizerCMIRModel> result = flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
			if (null != result && !result.isEmpty())
			{
				return result;
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching inactive CMIR list from DB ::: " + e);
			e.printStackTrace();
		}
		return null;
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getInActivePricerowForRegion(java.lang.String)
	 */
	@Override
	public List<EnergizerPriceRowModel> getInActivePriceRowForRegion(final String region)
	{
		FlexibleSearchQuery query = null;
		String queryString = "";
		try
		{
			if (!region.equalsIgnoreCase(EnergizerCoreConstants.ALL))
			{
				queryString = "SELECT {p.pk} FROM {" + EnergizerPriceRowModel._TYPECODE
						+ " AS p JOIN EnergizerB2BUnit AS b ON {p.b2bUnit}={b.pk}} WHERE {p.isActive}=?isActive AND {b.salesArea}=?region";
				query = new FlexibleSearchQuery(queryString);
				query.addQueryParameter("region", region);
			}
			else
			{
				queryString = "SELECT {p.pk} FROM {" + EnergizerPriceRowModel._TYPECODE
						+ " AS p JOIN EnergizerB2BUnit AS b ON {p.b2bUnit}={b.pk}} WHERE {p.isActive}=?isActive";
				query = new FlexibleSearchQuery(queryString);
			}
			query.addQueryParameter("isActive", false);

			final List<EnergizerPriceRowModel> result = flexibleSearchService.<EnergizerPriceRowModel> search(query).getResult();
			if (null != result && !result.isEmpty())
			{
				return result;
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while fetching inactive pricerow list from DB ::: " + e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<EnergizerPriceRowModel> getActiveEnergizerPriceRowForCMIRModelSet(final Set<EnergizerCMIRModel> cmirSetFromDB)
	{

		final String queryString = "SELECT {enrPrice.pk} FROM {EnergizerCMIR AS cmir JOIN EnergizerProduct AS prod ON "
				+ "{cmir.erpMaterialId}={prod.code} JOIN EnergizerPriceRow AS enrPrice ON {enrPrice.product}={prod.pk} AND "
				+ "{cmir.b2bUnit}={enrPrice.b2bUnit}} WHERE {cmir.pk} IN (?cmirSetFromDB) AND {enrPrice.isActive}=?isActive";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("cmirSetFromDB", cmirSetFromDB);
		query.addQueryParameter("isActive", true);

		return flexibleSearchService.<EnergizerPriceRowModel> search(query).getResult();
	}
}
