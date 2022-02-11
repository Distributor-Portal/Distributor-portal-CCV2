/**
 * 
 */
package com.energizer.core.solr.query;

import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * This interface defines the constants and methods that would be required to manipulate the solr query to enforce the
 * b2bunit specific catalog listing.
 * 
 * @author kaushik.ganguly
 * 
 */
public interface EnergizerSolrQueryManipulationService
{

	public static final String QUERY_CONNECTOR = ":";

	public static final String B2B_UNIT_SEARCHQUERY_PREFIX = "b2bunit";

	public static final String ADD_SORT = "topRated";

	public static final String B2B_UNIT_EXTRAFILTER_PREFIX = "relevance";

	public static final String B2B_UNIT_KEYWORDSEARCH_PREFIX = "keywordsearch";

	public static final String B2B_UNIT_SEARCHQUERY_SEPARATOR = "-";

	public static final String EMPTY_STRING = "";

	public static final String SPACES_REGEX = "\\s";


	public EnergizerB2BUnitModel getB2BUnitForLoggedInUser();

	public String getB2BUnitInSolrQuery();

	public String getSolrQueryForCategorySearch(final String sortCode, final String existingQuery);

	public String getSolrQueryForTextSearchPage(final String sortCode, final String existingQuery);

}
