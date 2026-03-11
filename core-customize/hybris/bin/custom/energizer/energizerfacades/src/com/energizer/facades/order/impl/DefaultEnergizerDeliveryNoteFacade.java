/**
 *
 */
package com.energizer.facades.order.impl;

import de.hybris.platform.b2bacceleratorfacades.order.B2BOrderFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.media.NoDataAvailableException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Resource;

import com.energizer.core.data.EnergizerDeliveryNoteData;
import com.energizer.core.delivery.note.impl.DefaultEnergizerDeliveryNoteService;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCheckoutFlowFacade;
import com.energizer.facades.order.EnergizerDeliveryNoteFacade;


/**
 * @author AS398193
 *
 */
public class DefaultEnergizerDeliveryNoteFacade implements EnergizerDeliveryNoteFacade
{

	@Resource(name = "b2bOrderFacade")
	private B2BOrderFacade orderFacade;

	@Resource(name = "defaultEnergizerDeliveryNoteService")
	private DefaultEnergizerDeliveryNoteService deliveryNoteService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Deprecated
	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "energizerB2BCheckoutFlowFacade")
	private DefaultEnergizerB2BCheckoutFlowFacade energizerB2BCheckoutFlowFacade;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerDeliveryNoteFacade#createUploadOrderMedia(java.io.File,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void createUploadOrderMedia(final File serverFile, final String fileName, final String mediaCode,
			final String catalogName, final String orderCode) throws FileNotFoundException, IOException
	{
		deliveryNoteService.createUploadOrderMedia(serverFile, fileName, mediaCode, catalogName, orderCode);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerDeliveryNoteFacade#getDeliveryNoteFileAsBytes(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public byte[] getDeliveryNoteFileAsBytes(final String mediaModelCode)
			throws NoDataAvailableException, IllegalArgumentException, IOException
	{
		return deliveryNoteService.getDeliveryNoteFileAsBytes(mediaModelCode);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerDeliveryNoteFacade#getDeliveryNoteFileData(java.lang.String,
	 * java.lang.String)
	 */
	public EnergizerDeliveryNoteData getDeliveryNoteFileData(final String orderCode, final String fileName)
	{
		final OrderData orderDetails = orderFacade.getOrderDetailsForCode(orderCode);

		EnergizerDeliveryNoteData deliveryNoteFileData = null;

		if (null != orderDetails)
		{
			final Collection<EnergizerDeliveryNoteData> deliveryNoteFiles = orderDetails.getDeliveryNoteFiles();
			if (null != deliveryNoteFiles)
			{
				for (final EnergizerDeliveryNoteData deliveryNoteFile : deliveryNoteFiles)
				{
					if (null != deliveryNoteFile && deliveryNoteFile.getFileName().equalsIgnoreCase(fileName))
					{
						deliveryNoteFileData = deliveryNoteFile;
						break;
					}
				}
			}
		}
		return deliveryNoteFileData;
	}

	public void deleteDeliveryNoteFileMedia(final String mediaCode, final String catalogName, final String orderCode)
			throws FileNotFoundException, IOException, ModelRemovalException
	{
		deliveryNoteService.deleteDeliveryNoteFileMedia(mediaCode, catalogName, orderCode);
	}

	/**
	 * @param serverFile
	 * @param originalFilename
	 * @param string
	 * @param personalcareContentcatalog
	 * @param cartId
	 */
	public boolean createUploadCartMedia(final File serverFile, final String fileName, final String mediaCode,
			final String catalogName, final String cartId) throws FileNotFoundException, IOException
	{
		return deliveryNoteService.createUploadCartMedia(serverFile, fileName, mediaCode, catalogName, cartId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerDeliveryNoteFacade#getDeliveryNoteFileDataForCart(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerDeliveryNoteData getDeliveryNoteFileDataForCart(final String cartID, final String fileName)
	{

		final CartData cartData = energizerB2BCheckoutFlowFacade.getCheckoutCart();

		EnergizerDeliveryNoteData deliveryNoteFileData = null;

		if (null != cartData)
		{
			final Collection<EnergizerDeliveryNoteData> deliveryNoteFiles = cartData.getDeliveryNoteFiles();
			if (null != deliveryNoteFiles)
			{
				for (final EnergizerDeliveryNoteData deliveryNoteFile : deliveryNoteFiles)
				{
					if (null != deliveryNoteFile && deliveryNoteFile.getFileName().equalsIgnoreCase(fileName))
					{
						deliveryNoteFileData = deliveryNoteFile;
						break;
					}
				}
			}
		}
		return deliveryNoteFileData;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerDeliveryNoteFacade#deleteDeliveryNoteFileMediaForCart(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteDeliveryNoteFileMediaForCart(final String mediaCode, final String catalogName, final String cartID)
			throws FileNotFoundException, IOException
	{
		return deliveryNoteService.deleteDeliveryNoteFileMediaForCart(mediaCode, catalogName, cartID);
	}

	public boolean setMediaRemovableForCart(final String mediaCode, final String catalogName, final String cartID)
			throws ModelSavingException
	{
		return deliveryNoteService.setMediaRemovableForCart(mediaCode, catalogName, cartID);
	}
}
