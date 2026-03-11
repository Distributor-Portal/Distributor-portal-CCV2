/**
 *
 */
package com.energizer.services.delivery.note.dao.impl;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.media.NoDataAvailableException;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.energizer.services.delivery.note.dao.EnergizerDeliveryNoteDAO;


/**
 * @author AS398193
 *
 */
public class DefaultEnergizerDeliveryNoteDAO implements EnergizerDeliveryNoteDAO
{
	private static final Logger LOG = Logger.getLogger(DefaultEnergizerDeliveryNoteDAO.class);

	@Resource
	FlexibleSearchService flexibleSearchService;

	@Resource
	MediaService mediaService;

	public byte[] getDeliveryNoteFileAsBytes(final String mediaModelCode) throws NoDataAvailableException,
			IllegalArgumentException, IOException
	{

		final String queryString = "SELECT {p:" + MediaModel.PK + "} FROM {" + MediaModel._TYPECODE + " AS p} WHERE " + "{p:"
				+ MediaModel.CODE + "}=?code ";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("code", mediaModelCode);

		return IOUtils.toByteArray(mediaService.getStreamFromMedia(flexibleSearchService.<MediaModel> search(query).getResult()
				.get(0)));

	}
}
