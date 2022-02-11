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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;


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

		int imagesMovedToProcessedFolder = 0;
		int imagesMovedToErrorFilesFolder = 0;
		try
		{

			final CatalogVersionModel catalogVersion = getCatalogVersion(cronjob);


			String thumbnailPath = Config.getParameter("energizer.thumbnailPath");

			String displayImagePath = Config.getParameter("energizer.displayImagePath");

			if (cronjob.getCatalogName().contains("EMEA"))
			{
				thumbnailPath = Config.getParameter("energizer.thumbnailPath.EMEA");

				displayImagePath = Config.getParameter("energizer.displayImagePath.EMEA");

			}
			final File[] files = new File(thumbnailPath).listFiles();

			Map<String, String> csvValuesMap = null;

			if (files != null)
			{
				csvValuesMap = new HashMap<>();

				int imagesProceesed = 0;

				for (int i = 0; i < files.length; i++)
				{
					if (files[i].isFile())
					{
						boolean mediaSaved = false;

						LOG.info("files[" + i + "].getName()  ::: " + files[i].getName());

						final String ext = FilenameUtils.getExtension(files[i].getName());

						if (null != files[i].getName() && files[i].getName().contains("_"))
						{
							final String imgRefId = files[i].getName().toString().substring(0, files[i].getName().indexOf("_"));

							LOG.info("imgRefId  ::: " + imgRefId);

							final List<EnergizerCMIRModel> erpId = energizerProductService.getERPMaterialIdForImageReferenceId(imgRefId);

							LOG.info("erpId.size()  ::: " + erpId.size());

							final int size = erpId.size();

							if (size > 0)
							{
								for (int j = 0; j < size; j++)
								{
									LOG.info("ERP ID ::: " + erpId.get(j).getErpMaterialId());

									csvValuesMap.put(EnergizerCoreConstants.ERPMATERIAL_ID, erpId.get(j).getErpMaterialId());

									csvValuesMap.put(EnergizerCoreConstants.THUMBNAIIL_PATH, thumbnailPath + "\\"
											+ files[i].getName().substring(0, files[i].getName().indexOf("_")) + "_2" + "." + ext);

									LOG.info("Thumbnail " + i + " ::: " + thumbnailPath + "\\"
											+ files[i].getName().substring(0, files[i].getName().indexOf("_")) + "_2" + "." + ext);

									csvValuesMap.put(EnergizerCoreConstants.DISPLAY_IMAGE_PATH, displayImagePath + "\\"
											+ files[i].getName().substring(0, files[i].getName().indexOf("_")) + "_1" + "." + ext);

									LOG.info("Display Image  " + i + " ::: " + displayImagePath + "\\"
											+ files[i].getName().substring(0, files[i].getName().indexOf("_")) + "_1" + "." + ext);

									LOG.info("Processing product : " + (csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID));

									try
									{
										existEnergizerProd = (EnergizerProductModel) productService.getProductForCode(catalogVersion,
												(csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID));

									}
									catch (final Exception e)
									{
										LOG.info(
												"Product : " + (csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID) + " DOES NOT EXIST");
										continue;
									}
									if (null != existEnergizerProd)
									{
										try
										{
											addUpdateProductMediaDetails(existEnergizerProd, catalogVersion, csvValuesMap);
										}
										catch (final Exception e)
										{
											LOG.info("Image File does not exist for product " + existEnergizerProd.getCode());
											continue;
										}
									}
									mediaSaved = true;

								}

								LOG.info("****************** ProductMediaModel updated successfully for image ref Id : " + imgRefId
										+ "****************** ");
								// how many image files are processed so far
								imagesProceesed = imagesProceesed + 1;

								// move the processed image files to either 'ProcessedWithNoErrors' or 'ErrorFiles' folders.
								final String fileMovementStatus = cleanUp(files[i].getName(), mediaSaved, thumbnailPath, displayImagePath,
										ext);

								if (fileMovementStatus.equalsIgnoreCase("processed"))
								{
									imagesMovedToProcessedFolder = imagesMovedToProcessedFolder + 1;
								}
								else if (fileMovementStatus.equalsIgnoreCase("error"))
								{
									imagesMovedToErrorFilesFolder = imagesMovedToErrorFilesFolder + 1;
								}
							}
							else
							{
								LOG.info("No ERP Material Id For Image Reference Id '" + imgRefId + "' found !! ");
							}
							LOG.info("Total images processed  : " + imagesProceesed);
						}
						else
						{
							LOG.info("The image file name " + files[i].getName()
									+ " doesn't have proper naming convention with an underscore :: ");
						}
					}
					else
					{
						LOG.info("Not a valid image file !! ");
					}

					//If the cronjob abort is requested, then perform clean up and return the PerformResult
					this.modelService.refresh(cronjob);
					if (clearAbortRequestedIfNeeded(cronjob))
					{
						LOG.info(cronjob.getRegion() + " : CRONJOB IS ABORTED WHILE PERFORMING ...");
						//abort the job
						cronJobService.requestAbortCronJob(cronjob);
						return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
					}
				}
			}
			else
			{
				LOG.info("No image files present at the specified image folder location " + thumbnailPath + " !! ");
			}
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
