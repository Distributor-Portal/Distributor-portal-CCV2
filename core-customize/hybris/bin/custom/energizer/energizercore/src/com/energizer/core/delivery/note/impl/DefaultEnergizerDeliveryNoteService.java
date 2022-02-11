/**
 *
 */
package com.energizer.core.delivery.note.impl;

import de.hybris.platform.b2b.services.B2BCartService;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.media.MediaContainerModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.media.MediaContainerService;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.media.NoDataAvailableException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.energizer.core.delivery.note.EnergizerDeliveryNoteService;
import com.energizer.services.delivery.note.dao.impl.DefaultEnergizerDeliveryNoteDAO;


/**
 * @author AS398193
 *
 */
public class DefaultEnergizerDeliveryNoteService implements EnergizerDeliveryNoteService
{

	private static final String VERSION = "Online";

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerDeliveryNoteService.class);

	@Resource
	CatalogVersionService catalogVersionService;

	@Resource
	MediaService mediaService;

	@Resource
	MediaContainerService mediaContainerService;

	@Resource
	private ModelService modelService;

	@Resource(name = "b2bOrderService")
	private B2BOrderService b2bOrderService;

	@Resource(name = "b2bCartService")
	private B2BCartService b2bCartService;

	@Resource(name = "defaultEnergizerDeliveryNoteDao")
	private DefaultEnergizerDeliveryNoteDAO deliveryNoteDao;

	public void createUploadOrderMedia(final File serverFile, final String fileName, final String mediaCode,
			final String catalogName, final String orderCode) throws FileNotFoundException, IOException
	{
		final InputStream mediaInputStream = new FileInputStream(serverFile);
		final OrderModel orderModel = b2bOrderService.getOrderForCode(orderCode);
		final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(catalogName, VERSION);

		// Creating or Updating  Media
		MediaModel mediaModel = null;
		try
		{
			mediaModel = mediaService.getMedia(catalogVersion, mediaCode);
		}
		catch (final Exception e)
		{
			LOG.error("Media does not exist for Media model code" + mediaCode + " !, Exception ::: " + e);
		}

		// Create New Media Model if it doesn't exist already
		if (null == mediaModel)
		{
			mediaModel = modelService.create(MediaModel.class);
			mediaModel.setCode(mediaCode);
			mediaModel.setCatalogVersion(catalogVersion);
			mediaModel.setURL(serverFile.getAbsolutePath());
			mediaModel.setAltText(fileName);
			mediaModel.setRealFileName(fileName);
		}
		mediaModel.setSize(FileUtils.sizeOf(serverFile));
		modelService.save(mediaModel);
		mediaService.setStreamForMedia(mediaModel, mediaInputStream);

		LOG.info("MediaModel code '" + mediaCode + "' saved successfully *************");

		// Creating or Updating  mediaContainer and add media
		MediaContainerModel mediaContainer = orderModel.getDeliveryNoteFiles();
		final String mediaContainerQualifier = orderCode.concat("_mediaContainer");
		try
		{
			mediaContainer = mediaContainerService.getMediaContainerForQualifier(mediaContainerQualifier);
		}
		catch (final Exception e)
		{
			LOG.error("MediaContainer Qualifier '" + mediaContainerQualifier + "' does not exist !, Exception ::: " + e);
		}

		if (mediaContainer == null)
		{
			mediaContainer = modelService.create(MediaContainerModel.class);
			mediaContainer.setQualifier(mediaContainerQualifier);
			mediaContainer.setCatalogVersion(catalogVersion);
			modelService.save(mediaContainer);
			LOG.info("MediaContainer Qualifier '" + mediaContainerQualifier + "' created successfully for the order '" + orderCode
					+ "' !");
		}

		final Set<MediaModel> mediaModelSet = Collections.synchronizedSet(new HashSet<MediaModel>());
		mediaModelSet.addAll(mediaContainer.getMedias());
		mediaModelSet.add(mediaModel);
		mediaContainer.setMedias(mediaModelSet);

		modelService.save(mediaContainer);
		mediaContainerService.addMediaToContainer(mediaContainer, Collections.singletonList(mediaModel));
		modelService.refresh(mediaContainer);

		orderModel.setDeliveryNoteFiles(mediaContainer);
		modelService.save(orderModel);

		LOG.info("Media/MediaContainer saved/updated successfully for the order '" + orderCode + "' !");
	}

	public byte[] getDeliveryNoteFileAsBytes(final String mediaModelCode)
			throws NoDataAvailableException, IllegalArgumentException, IOException
	{
		return deliveryNoteDao.getDeliveryNoteFileAsBytes(mediaModelCode);
	}

	public void deleteDeliveryNoteFileMedia(final String mediaCode, final String catalogName, final String orderCode)
			throws FileNotFoundException, IOException, ModelRemovalException
	{
		final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(catalogName, VERSION);

		// Deleting Media
		try
		{
			final MediaModel mediaModel = mediaService.getMedia(catalogVersion, mediaCode);
			if (null != mediaModel)
			{
				modelService.remove(mediaModel);
				LOG.info("Media '" + mediaCode + "' removed successfully for the order '" + orderCode + "' !");
			}
		}
		catch (final Exception e)
		{
			LOG.error("Error while removing media model " + mediaCode + " !, Exception ::: " + e);
			e.printStackTrace();
		}

		final OrderModel orderModel = b2bOrderService.getOrderForCode(orderCode);

		// Deleting Empty Media Container
		MediaContainerModel mediaContainer = orderModel.getDeliveryNoteFiles();
		final String mediaContainerQualifier = orderCode.concat("_mediaContainer");
		try
		{
			mediaContainer = mediaContainerService.getMediaContainerForQualifier(mediaContainerQualifier);
		}
		catch (final Exception e)
		{
			LOG.error("MediaContainer '" + mediaContainerQualifier + "' does not exist !, Exception ::: " + e);
		}

		if (null != mediaContainer.getMedias() && mediaContainer.getMedias().size() == 0)
		{
			modelService.remove(mediaContainer);
			LOG.info(
					"Empty MediaContainer '" + mediaContainerQualifier + "' removed successfully for the order '" + orderCode + "' !");
		}

		//orderModel.setDeliveryNoteFiles(mediaContainer);
		//modelService.save(orderModel);
	}

	/**
	 * @param serverFile
	 * @param fileName
	 * @param mediaCode
	 * @param catalogName
	 * @param cartId
	 */
	public boolean createUploadCartMedia(final File serverFile, final String fileName, final String mediaCode,
			final String catalogName, final String cartId) throws FileNotFoundException, IOException
	{
		final InputStream mediaInputStream = new FileInputStream(serverFile);
		final CartModel cartModel = b2bCartService.getSessionCart();
		final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(catalogName, VERSION);

		// Creating or Updating  Media
		MediaModel mediaModel = null;
		boolean newMediaModel = false;
		try
		{
			mediaModel = mediaService.getMedia(catalogVersion, mediaCode);
		}
		catch (final Exception e)
		{
			LOG.error("Media does not exist for Media model code" + mediaCode + " !, Exception ::: " + e);
		}

		// Create New Media Model if it doesn't exist already
		if (null == mediaModel)
		{
			mediaModel = modelService.create(MediaModel.class);
			mediaModel.setCode(mediaCode);
			mediaModel.setCatalogVersion(catalogVersion);
			mediaModel.setURL(serverFile.getAbsolutePath());
			mediaModel.setAltText(fileName);
			mediaModel.setRealFileName(fileName);

			newMediaModel = true;
		}
		mediaModel.setSize(FileUtils.sizeOf(serverFile));
		modelService.save(mediaModel);
		mediaService.setStreamForMedia(mediaModel, mediaInputStream);

		LOG.info("MediaModel code '" + mediaCode + "' saved successfully *************");

		// Creating or Updating  mediaContainer and add media
		MediaContainerModel mediaContainer = cartModel.getDeliveryNoteFiles();
		final String mediaContainerQualifier = cartModel.getCode().concat("_mediaContainer");
		try
		{
			mediaContainer = mediaContainerService.getMediaContainerForQualifier(mediaContainerQualifier);
		}
		catch (final Exception e)
		{
			LOG.error("MediaContainer Qualifier '" + mediaContainerQualifier + "' does not exist !, Exception ::: " + e);

		}

		if (mediaContainer == null)
		{
			mediaContainer = modelService.create(MediaContainerModel.class);
			mediaContainer.setQualifier(mediaContainerQualifier);
			mediaContainer.setCatalogVersion(catalogVersion);
			modelService.save(mediaContainer);
			LOG.info("MediaContainer Qualifier '" + mediaContainerQualifier + "' created successfully for the cart : '" + cartId
					+ "' !");
		}

		final Set<MediaModel> mediaModelSet = Collections.synchronizedSet(new HashSet<MediaModel>());
		mediaModelSet.addAll(mediaContainer.getMedias());
		mediaModelSet.add(mediaModel);
		mediaContainer.setMedias(mediaModelSet);

		modelService.save(mediaContainer);
		mediaContainerService.addMediaToContainer(mediaContainer, Collections.singletonList(mediaModel));
		modelService.refresh(mediaContainer);

		cartModel.setDeliveryNoteFiles(mediaContainer);
		modelService.save(cartModel);

		LOG.info("Media/MediaContainer saved/updated successfully for the cart : '" + cartId + "' !");

		return newMediaModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.core.delivery.note.EnergizerDeliveryNoteService#deleteDeliveryNoteFileMediaFForCart(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteDeliveryNoteFileMediaForCart(final String mediaCode, final String catalogName, final String cartID)
			throws FileNotFoundException, IOException
	{

		final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(catalogName, VERSION);

		boolean mediaDeletedFlag = false;

		// Deleting Media
		try
		{
			final MediaModel mediaModel = mediaService.getMedia(catalogVersion, mediaCode);
			if (null != mediaModel)
			{
				modelService.refresh(mediaModel);
				if (mediaModel.getRemovable().booleanValue())
				{
					modelService.remove(mediaModel);
					mediaDeletedFlag = true;
					LOG.info("Media '" + mediaCode + "' removed successfully for the order '" + cartID + "' !");
				}
				else
				{
					LOG.info("Media model is not removable ..");
				}
			}
			else
			{
				LOG.info("Media model with code '" + mediaCode + "' not found ...");
			}
		}
		catch (final Exception e)
		{
			mediaDeletedFlag = false;
			LOG.error("Error while removing media model " + mediaCode + " !, Exception ::: " + e);
			e.printStackTrace();
		}

		final CartModel cartModel = b2bCartService.getSessionCart();

		// Deleting Empty Media Container
		MediaContainerModel mediaContainer = cartModel.getDeliveryNoteFiles();
		final String mediaContainerQualifier = cartID.concat("_mediaContainer");
		try
		{
			mediaContainer = mediaContainerService.getMediaContainerForQualifier(mediaContainerQualifier);
		}
		catch (final Exception e)
		{
			LOG.error("MediaContainer '" + mediaContainerQualifier + "' does not exist !, Exception ::: " + e);
		}

		if (null != mediaContainer.getMedias() && mediaContainer.getMedias().size() == 0)
		{
			modelService.remove(mediaContainer);
			LOG.info("Empty MediaContainer '" + mediaContainerQualifier + "' removed successfully for the order '" + cartID + "' !");
		}

		//orderModel.setDeliveryNoteFiles(mediaContainer);
		//modelService.save(orderModel);

		return mediaDeletedFlag;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.core.delivery.note.EnergizerDeliveryNoteService#setMediaRemovableForCart(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public boolean setMediaRemovableForCart(final String mediaCode, final String catalogName, final String cartID)
			throws ModelSavingException
	{

		final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(catalogName, VERSION);
		MediaModel mediaModel = null;
		try
		{
			mediaModel = mediaService.getMedia(catalogVersion, mediaCode);
			if (null != mediaModel)
			{
				mediaModel.setRemovable(true);
				modelService.save(mediaModel);
				modelService.refresh(mediaModel);

				LOG.info("Media '" + mediaCode + "' set removable for the order '" + cartID + "' !");
			}
			else
			{
				LOG.info("Media model with code '" + mediaCode + "' not found ...");
			}
		}
		catch (final ModelSavingException e)
		{
			LOG.error("Error while saving media model " + mediaCode + " !, Exception ::: " + e);
			e.printStackTrace();
		}
		catch (final Exception e)
		{
			LOG.error("Exception occurred : " + e.getMessage());
			e.printStackTrace();
		}
		return mediaModel.getRemovable().booleanValue();
	}
}
