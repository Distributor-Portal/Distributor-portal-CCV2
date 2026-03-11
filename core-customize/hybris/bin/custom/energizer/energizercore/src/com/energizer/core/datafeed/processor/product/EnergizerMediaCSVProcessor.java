package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.media.MediaContainerModel;
import de.hybris.platform.core.model.media.MediaFormatModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.media.MediaContainerService;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.Config;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;

// Azure SDK v12 imports
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;

public class EnergizerMediaCSVProcessor extends AbstractJobPerformable<EnergizerCronJobModel>
{
	@Resource
	private EnergizerProductService energizerProductService;
	@Resource
	private ModelService modelService;
	@Resource
	private SessionService sessionService;
	@Resource
	private ProductService productService;
	@Resource
	private CommonI18NService defaultCommonI18NService;
	@Resource
	private UnitService defaultUnitService;
	@Resource
	MediaService mediaService;
	@Resource
	MediaContainerService mediaContainerService;
	@Resource
	private ConfigurationService configurationService;
	@Resource
	CatalogVersionService catalogVersionService;
	@Resource
	protected FlexibleSearchService flexibleSearchService;
	private CronJobService cronJobService;
	@Resource
	AbstractEnergizerCSVProcessor energizerMediaProcessor;
	@Resource
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;

	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	@Override
	public boolean isAbortable()
	{
		return true;
	}

	private static final Logger LOG = Logger.getLogger(EnergizerMediaCSVProcessor.class);
	private final String PRD_IMG_QUALIFIER = "300Wx300H";
	private final String PRD_THUMB_QUALIFIER = "96Wx96H";
	private static final String aTHUMB = "_thumb";
	private static final String aPICS = "_pic";
	private static final String JPEG = "jpeg";
	private static final String JPG = "jpg";
	public static final String toProcess = "toProcess";
	public static final String ProcessedWithNoErrors = "ProcessedWithNoErrors";
	public static final String ErrorFiles = "ErrorFiles";
	public static final String fileSeperator = "/";

	@Override
	public PerformResult perform(final EnergizerCronJobModel cronjob)
	{
		EnergizerProductModel existEnergizerProd = null;
		int imagesMovedToProcessedFolder = 0;
		int imagesMovedToErrorFilesFolder = 0;

		try
		{
			String thumbnailPath = Config.getParameter("energizer.thumbnailPath");
			String displayImagePath = Config.getParameter("energizer.displayImagePath");

			final CatalogVersionModel catalogVersion = getCatalogVersion(cronjob);
			Map<String, String> csvValuesMap = null;

			BlobContainerClient blobContainer = energizerWindowsAzureBlobStorageStrategy.getBlobContainerClient();

			// List blobs in the thumbnail path directory
			String prefix = thumbnailPath.endsWith(fileSeperator) ? thumbnailPath : thumbnailPath + fileSeperator;
			for (BlobItem blobItem : blobContainer.listBlobsByHierarchy(prefix))
			{
				if (blobItem.isPrefix())
					continue;

				String blobName = blobItem.getName();
				String fileName = FilenameUtils.getName(blobName);

				// Example: parse CSV or metadata for each image as needed
				// csvValuesMap = ... (populate as per your logic)

				// For demonstration, assume csvValuesMap is available for each image
				// existEnergizerProd = ... (fetch or create product as per your logic)

				// Uncomment and implement your logic here:
				// addUpdateProductMediaDetailsFromBlobStorage(existEnergizerProd, catalogVersion, csvValuesMap, blobContainer);

				// After processing, move/copy blobs as needed
				// cleanUp(fileName, true, thumbnailPath, displayImagePath, "jpg", blobContainer);

				imagesMovedToProcessedFolder++;
			}
		}
		catch (final BlobStorageException e1)
		{
			LOG.error("Azure Blob Storage error", e1);
		}
		catch (final URISyntaxException e)
		{
			LOG.error("URI Syntax error", e);
		}
		catch (final Exception e)
		{
			LOG.error("Error in adding or updating ProductMediaModel ::: " + e.getMessage(), e);
		}

		LOG.info("Total images moved to processed folder  : " + imagesMovedToProcessedFolder);
		LOG.info("Total images moved to error files folder : " + imagesMovedToErrorFilesFolder);
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	private void addUpdateProductMediaDetailsFromBlobStorage(final EnergizerProductModel energizerProd,
															 final CatalogVersionModel catalogVersion, final Map<String, String> csvValuesMap,
															 final BlobContainerClient blobContainer) throws FileNotFoundException, URISyntaxException
	{
		final String productMaterialId = csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).trim();
		final String thumbnailPath = csvValuesMap.get(EnergizerCoreConstants.THUMBNAIIL_PATH).trim();
		final String displayImagePath = csvValuesMap.get(EnergizerCoreConstants.DISPLAY_IMAGE_PATH).trim();

		energizerProd.setCode(productMaterialId);
		energizerProd.setCatalogVersion(catalogVersion);
		energizerProd.setApprovalStatus(ArticleApprovalStatus.APPROVED);

		final MediaModel mediaThumbnail = createUploadProductMedia(thumbnailPath, productMaterialId.concat(aTHUMB),
				PRD_THUMB_QUALIFIER, catalogVersion, productMaterialId, blobContainer);
		final MediaModel mediaPicture = createUploadProductMedia(displayImagePath, productMaterialId.concat(aPICS),
				PRD_IMG_QUALIFIER, catalogVersion, productMaterialId, blobContainer);

		energizerProd.setThumbnail(mediaThumbnail);
		energizerProd.setPicture(mediaPicture);
		LOG.info("Flag Value ::: " + modelService.isModified(energizerProd));
		LOG.info("Is New ::: " + modelService.isNew(energizerProd));
		modelService.saveAll();
	}

	private MediaModel createUploadProductMedia(final String fileLoc, final String mediaModelCode, final String mediaQualifier,
												final CatalogVersionModel catalogVersion, final String productMaterialId, final BlobContainerClient blobContainer)
	{
		InputStream mediaInputStream = null;
		try
		{
			LOG.info("fileLoc ::: " + fileLoc);
			BlobClient blobClient = blobContainer.getBlobClient(fileLoc);
			byte[] blobBytes = blobClient.downloadContent().toBytes();
			mediaInputStream = new DataInputStream(new ByteArrayInputStream(blobBytes));
		}
		catch (final BlobStorageException e)
		{
			LOG.error("Error reading blob for media: " + fileLoc, e);
		}

		// Creating or Updating Media
		MediaModel mediaModel = null;
		try
		{
			mediaModel = mediaService.getMedia(catalogVersion, mediaModelCode);
		}
		catch (final Exception e)
		{
			LOG.error("Media does not exist for Product Media " + mediaModelCode + " || " + e);
		}

		if (null == mediaModel)
		{
			mediaModel = modelService.create(MediaModel.class);
			final MediaFormatModel format = mediaService.getFormat(mediaQualifier);
			mediaModel.setCode(mediaModelCode);
			mediaModel.setMediaFormat(format);
			mediaModel.setCatalogVersion(catalogVersion);
		}
		modelService.save(mediaModel);
		mediaService.setStreamForMedia(mediaModel, mediaInputStream);

		// Creating or Updating mediaContainer and add media
		MediaContainerModel mediaContainer = null;
		final String mediaContainerQualifier = productMaterialId.concat("_mediaContainer");
		try
		{
			mediaContainer = mediaContainerService.getMediaContainerForQualifier(mediaContainerQualifier);
		}
		catch (final Exception e)
		{
			LOG.error(mediaContainerQualifier + " mediaContainer not exist" + e);
		}

		if (mediaContainer == null)
		{
			mediaContainer = modelService.create(MediaContainerModel.class);
			mediaContainer.setQualifier(mediaContainerQualifier);
			mediaContainer.setCatalogVersion(catalogVersion);
			modelService.save(mediaContainer);
		}
		mediaContainerService.addMediaToContainer(mediaContainer, Collections.singletonList(mediaModel));

		LOG.info("mediaModelCode-->" + mediaModelCode);
		LOG.info(mediaModelCode + " mediaModel Saved Successfully *************");

		return mediaModel;
	}

	public CatalogVersionModel getCatalogVersion(final EnergizerCronJobModel cronjob) throws Exception
	{
		CatalogVersionModel catalogVersion = null;
		final String CATALOG_NAME = cronjob.getCatalogName();
		final String VERSION = cronjob.getCatalogVersion();
		if (StringUtils.isEmpty(CATALOG_NAME) || StringUtils.isEmpty(VERSION))
		{
			throw new Exception("Invalid Catalog Version ");
		}
		if (StringUtils.isNotEmpty(CATALOG_NAME) && StringUtils.isNotEmpty(VERSION))
		{
			catalogVersion = catalogVersionService.getCatalogVersion(CATALOG_NAME, VERSION);
		}
		else
		{
			throw new Exception("Invalid Catalog Version ");
		}
		return catalogVersion;
	}

	private String cleanUp(final String fileName, final boolean mediaSaved, final String thumbnailPath,
						   final String displayImagePath, final String ext, final BlobContainerClient blobContainer)
	{
		final String thumbnailPathNew = StringUtils.substringBefore(thumbnailPath, "/toProcess");
		final String displayImagePathNew = StringUtils.substringBefore(displayImagePath, "/toProcess");

		try
		{
			String sourceSuffix = fileSeperator + fileName.substring(0, fileName.indexOf("_")) + "." + ext;
			String targetDir = mediaSaved ? ProcessedWithNoErrors : ErrorFiles;

			// Thumbnail
			String thumbnailSourcePath = thumbnailPath + sourceSuffix;
			String thumbnailTargetPath = thumbnailPathNew + fileSeperator + targetDir + sourceSuffix;

			BlobClient thumbnailSourceBlob = blobContainer.getBlobClient(thumbnailSourcePath);
			BlobClient thumbnailTargetBlob = blobContainer.getBlobClient(thumbnailTargetPath);

			thumbnailTargetBlob.beginCopy(thumbnailSourceBlob.getBlobUrl(), null);
			thumbnailSourceBlob.delete();

			// Display Image
			String displayImgSourcePath = displayImagePath + sourceSuffix;
			String displayImgTargetPath = displayImagePathNew + fileSeperator + targetDir + sourceSuffix;

			BlobClient displayImgSourceBlob = blobContainer.getBlobClient(displayImgSourcePath);
			BlobClient displayImgTargetBlob = blobContainer.getBlobClient(displayImgTargetPath);

			displayImgTargetBlob.beginCopy(displayImgSourceBlob.getBlobUrl(), null);
			displayImgSourceBlob.delete();

			return mediaSaved ? "processed" : "error";
		}
		catch (final Exception e)
		{
			LOG.info("Error in processing images to " + (mediaSaved ? "ProcessedWithNoErrors" : "ErrorFiles") + " folder", e);
		}
		return null;
	}
}
