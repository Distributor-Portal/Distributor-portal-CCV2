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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;


/**
 *
 *
 * This processors imports the media.
 */
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

	/**
	 * @return the cronJobService
	 */
	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	/**
	 * @param cronJobService
	 *           the cronJobService to set
	 */
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	// To abort the cronjob during run time that take more time for processing.
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
	public static final String fileSeperator = "\\";

	@Override
	public PerformResult perform(final EnergizerCronJobModel cronjob)
	{
		EnergizerProductModel existEnergizerProd = null;

		final int imagesMovedToProcessedFolder = 0;
		final int imagesMovedToErrorFilesFolder = 0;

		try
		{

			final CatalogVersionModel catalogVersion = getCatalogVersion(cronjob);

			Map<String, String> csvValuesMap1 = null;

			CloudBlobContainer cloudBlobContainer = null;
			cloudBlobContainer = energizerWindowsAzureBlobStorageStrategy.getBlobContainer();

			final CloudBlobDirectory blobDirectoryForPersonalCareThumbNailPath = energizerMediaProcessor
					.getBlobDirectoryPersonalCareThumbNailPath();

			for (final ListBlobItem blobItemPCNP : blobDirectoryForPersonalCareThumbNailPath.listBlobs())
			{

				final String subfullFilePath = blobItemPCNP.getStorageUri().getPrimaryUri().getPath();
				final String fullFilePath = subfullFilePath.substring(8);
				final String fileName = StringUtils.substringAfterLast(fullFilePath, "/");
				CloudBlockBlob blob2;
				blob2 = cloudBlobContainer.getBlockBlobReference(fullFilePath);

				csvValuesMap1 = new HashMap<>();

				LOG.info("files Name--->  ::: " + fileName);

				final String ext = FilenameUtils.getExtension(fileName);

				if (null != fileName && fileName.contains("_"))
				{
					int imagesProceesed1 = 0;
					boolean mediaSaved1 = false;


					final String imgRefId = fileName.toString().substring(0, fileName.indexOf("_"));

					LOG.info("imgRefId  ::: " + imgRefId);

					final List<EnergizerCMIRModel> erpId = energizerProductService.getERPMaterialIdForImageReferenceId(imgRefId);

					LOG.info("erpId.size()  ::: " + erpId.size());

					final int size = erpId.size();

					if (size > 0)
					{
						for (int j = 0; j < size; j++)
						{
							LOG.info("ERP ID ::: " + erpId.get(j).getErpMaterialId());

							csvValuesMap1.put(EnergizerCoreConstants.ERPMATERIAL_ID, erpId.get(j).getErpMaterialId());

							csvValuesMap1.put(EnergizerCoreConstants.THUMBNAIIL_PATH, fullFilePath);

							LOG.info("Thumbnail " + " ::: " + fullFilePath);

							LOG.info("Processing product : " + (csvValuesMap1).get(EnergizerCoreConstants.ERPMATERIAL_ID));

							try
							{
								existEnergizerProd = (EnergizerProductModel) productService.getProductForCode(catalogVersion,
										(csvValuesMap1).get(EnergizerCoreConstants.ERPMATERIAL_ID));

							}
							catch (final Exception e)
							{
								LOG.info("Product : " + (csvValuesMap1).get(EnergizerCoreConstants.ERPMATERIAL_ID) + " DOES NOT EXIST");
								continue;
							}
							if (null != existEnergizerProd)
							{
								try
								{
									addUpdateProductMediaDetailsFromBlobStorage(existEnergizerProd, catalogVersion, csvValuesMap1,
											cloudBlobContainer);
								}
								catch (final Exception e)
								{
									LOG.info("Image File does not exist for product " + existEnergizerProd.getCode());
									continue;
								}
							}
							mediaSaved1 = true;

						}

						LOG.info("****************** ProductMediaModel updated successfully for image ref Id : " + imgRefId
								+ "****************** ");
						// how many image files are processed so far
						imagesProceesed1 = imagesProceesed1 + 1;


					}

					else
					{
						LOG.info("No ERP Material Id For Image Reference Id '" + imgRefId + "' found !! ");
					}
					LOG.info("Total images processed  : " + imagesProceesed1);

				}
			}
		}


		catch (final StorageException e1)
		{
			// YTODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (final URISyntaxException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}

		catch (final Exception e)
		{
			LOG.error("Error in adding or updating  ProductMediaModel ::: " + e.getMessage());
			e.printStackTrace();
		}



		LOG.info("Total images moved to processed folder  : " + imagesMovedToProcessedFolder);
		LOG.info("Total images moved to error files folder : " + imagesMovedToErrorFilesFolder);
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	/**
	 *
	 * @param energizerProd
	 * @param catalogVersion
	 * @param csvValuesMap
	 * @throws FileNotFoundException
	 */
	private void addUpdateProductMediaDetails(final EnergizerProductModel energizerProd, final CatalogVersionModel catalogVersion,
			final Map<String, String> csvValuesMap) throws FileNotFoundException
	{
		final String productMaterialId = csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).toString().trim();
		final String thumbnailPath = csvValuesMap.get(EnergizerCoreConstants.THUMBNAIIL_PATH).toString().trim();
		final String displayImagePath = csvValuesMap.get(EnergizerCoreConstants.DISPLAY_IMAGE_PATH).toString().trim();

		energizerProd.setCode(productMaterialId);
		energizerProd.setCatalogVersion(catalogVersion);
		energizerProd.setApprovalStatus(ArticleApprovalStatus.APPROVED);

		final MediaModel mediaThumbnail = createUploadProductMedia(thumbnailPath, productMaterialId.concat(aTHUMB),
				PRD_THUMB_QUALIFIER, catalogVersion, productMaterialId);
		final MediaModel mediaPicture = createUploadProductMedia(displayImagePath, productMaterialId.concat(aPICS),
				PRD_IMG_QUALIFIER, catalogVersion, productMaterialId);

		energizerProd.setThumbnail(mediaThumbnail);
		energizerProd.setPicture(mediaPicture);
		LOG.info("Flag Value ::: " + modelService.isModified(energizerProd));
		LOG.info("Is New ::: " + modelService.isNew(energizerProd));
		modelService.saveAll();
	}

	/**
	 *
	 * @param fileLoc
	 * @param mediaModelCode
	 * @param mediaQualifier
	 * @param catalogVersion
	 * @param productMaterialId
	 * @return
	 * @throws FileNotFoundException
	 */
	private MediaModel createUploadProductMedia(final String fileLoc, final String mediaModelCode, final String mediaQualifier,
			final CatalogVersionModel catalogVersion, final String productMaterialId) throws FileNotFoundException
	{
		final InputStream mediaInputStream = new FileInputStream(new File(fileLoc));

		// Creating or Updating  Media
		MediaModel mediaModel = null;
		try
		{
			mediaModel = mediaService.getMedia(catalogVersion, mediaModelCode);
		}
		catch (final Exception e)
		{
			LOG.error(" Media does not exist for Product Media " + mediaModelCode + " || " + e);
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

		// Creating or Updating  mediaContainer and add media
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

		LOG.info(mediaModelCode + " mediaModel Saved Successfully *************");

		return mediaModel;

	}


	private void addUpdateProductMediaDetailsFromBlobStorage(final EnergizerProductModel energizerProd,
			final CatalogVersionModel catalogVersion, final Map<String, String> csvValuesMap,
			final CloudBlobContainer cloudBlobContainer) throws FileNotFoundException, URISyntaxException
	{
		final String productMaterialId = csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).toString().trim();
		final String thumbnailPath = csvValuesMap.get(EnergizerCoreConstants.THUMBNAIIL_PATH).toString().trim();
		//final String displayImagePath = csvValuesMap.get(EnergizerCoreConstants.DISPLAY_IMAGE_PATH).toString().trim();

		energizerProd.setCode(productMaterialId);
		energizerProd.setCatalogVersion(catalogVersion);
		energizerProd.setApprovalStatus(ArticleApprovalStatus.APPROVED);

		final MediaModel mediaThumbnail = createUploadProductMedia(thumbnailPath, productMaterialId.concat(aTHUMB),
				PRD_THUMB_QUALIFIER, catalogVersion, productMaterialId, cloudBlobContainer, thumbnailPath);
		//final MediaModel mediaPicture = createUploadProductMedia(displayImagePath, productMaterialId.concat(aPICS),
		//PRD_IMG_QUALIFIER, catalogVersion, productMaterialId, cloudBlobContainer, thumbnailPath);

		energizerProd.setThumbnail(mediaThumbnail);
		//energizerProd.setPicture(mediaPicture);
		LOG.info("Flag Value ::: " + modelService.isModified(energizerProd));
		LOG.info("Is New ::: " + modelService.isNew(energizerProd));
		modelService.saveAll();
	}

	private MediaModel createUploadProductMedia(final String fileLoc, final String mediaModelCode, final String mediaQualifier,
			final CatalogVersionModel catalogVersion, final String productMaterialId, final CloudBlobContainer cloudBlobContainer,
			final String thumbnailPath) throws FileNotFoundException, URISyntaxException
	{


		CloudBlockBlob blob2;
		InputStream mediaInputStream = null;
		try
		{
			blob2 = cloudBlobContainer.getBlockBlobReference(thumbnailPath.toString());
			//mediaInputStream = new FileInputStream(new File(blob2.getStorageUri().getPrimaryUri().getPath()));
			mediaInputStream = new DataInputStream(new ByteArrayInputStream(blob2.downloadText().getBytes()));
		}
		catch (final FileNotFoundException e1)
		{
			// YTODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (final StorageException e1)
		{
			// YTODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (final IOException e1)
		{
			// YTODO Auto-generated catch block
			e1.printStackTrace();
		}
		LOG.info("fileLoc---> ::: " + fileLoc);
		//final InputStream mediaInputStream = blob2.downloadText();

		// Creating or Updating  Media
		MediaModel mediaModel = null;
		try
		{
			mediaModel = mediaService.getMedia(catalogVersion, mediaModelCode);
		}
		catch (final Exception e)
		{
			LOG.error(" Media does not exist for Product Media " + mediaModelCode + " || " + e);
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

		// Creating or Updating  mediaContainer and add media
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

		LOG.info(mediaModelCode + " mediaModel Saved Successfully *************");

		return mediaModel;

	}


	public CatalogVersionModel getCatalogVersion(final EnergizerCronJobModel cronjob) throws Exception
	{
		CatalogVersionModel catalogVersion = null;
		/* Started EMEA Code refactor for get value from Model insted of properties */
		final String CATALOG_NAME = cronjob.getCatalogName();
		final String VERSION = cronjob.getCatalogVersion();
		/* End EMEA Code refactor for get value from Model insted of properties */
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
			final String displayImagePath, final String ext)
	{
		if (mediaSaved)
		{
			try
			{

				final Path thumbnailSourcePath = Paths
						.get(thumbnailPath + "\\" + fileName.substring(0, fileName.indexOf("_")) + "_2" + "." + ext);

				final Path displayImgSourcePath = Paths
						.get(displayImagePath + "\\" + fileName.substring(0, fileName.indexOf("_")) + "_1" + "." + ext);

				final Path thumbnailTargetPath = Paths.get(thumbnailSourcePath.getParent().getParent() + fileSeperator
						+ ProcessedWithNoErrors + fileSeperator + fileName.substring(0, fileName.indexOf("_")) + "_2" + "." + ext);

				final Path displayImgTargetPath = Paths.get(displayImgSourcePath.getParent().getParent() + fileSeperator
						+ ProcessedWithNoErrors + fileSeperator + fileName.substring(0, fileName.indexOf("_")) + "_1" + "." + ext);

				Files.move(thumbnailSourcePath, thumbnailTargetPath, StandardCopyOption.REPLACE_EXISTING);
				Files.move(displayImgSourcePath, displayImgTargetPath, StandardCopyOption.REPLACE_EXISTING);

				return "processed";
			}
			catch (final Exception e)
			{
				LOG.info("Error in processing images to processWithNoErrors folder");
				e.printStackTrace();
			}
		}
		else
		{
			try
			{

				final Path thumbnailSourcePath = Paths
						.get(thumbnailPath + "\\" + fileName.substring(0, fileName.indexOf("_")) + "_2" + "." + ext);

				final Path displayImgSourcePath = Paths
						.get(displayImagePath + "\\" + fileName.substring(0, fileName.indexOf("_")) + "_1" + "." + ext);

				final Path thumbnailTragetPath = Paths.get(thumbnailSourcePath.getParent().getParent() + fileSeperator + ErrorFiles
						+ fileSeperator + fileName.substring(0, fileName.indexOf("_")) + "_2" + "." + ext);

				final Path displayImgTargetPath = Paths.get(displayImgSourcePath.getParent().getParent() + fileSeperator + ErrorFiles
						+ fileSeperator + fileName.substring(0, fileName.indexOf("_")) + "_1" + "." + ext);

				Files.move(thumbnailSourcePath, thumbnailTragetPath, StandardCopyOption.REPLACE_EXISTING);
				Files.move(displayImgSourcePath, displayImgTargetPath, StandardCopyOption.REPLACE_EXISTING);

				return "error";
			}
			catch (final Exception e)
			{
				LOG.info("Error in processing images to Error folder");
				e.printStackTrace();
			}
		}
		return null;
	}
}
