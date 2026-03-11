/**
 * 
 */
package com.energizer.facades.product.populators;

import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.product.data.ImageDataType;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.search.converters.populator.SearchResultProductPopulator;
import de.hybris.platform.commerceservices.search.resultdata.SearchResultValueData;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;


/**
 * This populator resolves the problem of showing up images for the products on the search listing page.
 * 
 * @author kaushik.ganguly
 * 
 */
public class EnergizerSearchResultProductPopulator extends SearchResultProductPopulator
{

	private static final Logger LOG = Logger.getLogger(EnergizerSearchResultProductPopulator.class);

	private ProductService productService;

	@Override
	public void populate(final SearchResultValueData source, final ProductData target)
	{
		//super.populate(source, target);
		populateNameAndCode(source, target);
		target.setManufacturer(this.<String> getValue(source, "manufacturerName"));
		target.setDescription(this.<String> getValue(source, "description"));
		target.setSummary(this.<String> getValue(source, "summary"));
		target.setAverageRating(this.<Double> getValue(source, "reviewAvgRating"));

		populatePrices(source, target);

		// Populate product's classification features
		getProductFeatureListPopulator().populate(getFeaturesList(source), target);

		populateUrl(source, target);
		populatePromotions(source, target);
		populateStock(source, target);

		final List<ImageData> images = createImageDataEnergizer(source, target.getCode());
		if (CollectionUtils.isNotEmpty(images))
		{
			target.setImages(images);
		}


	}

	protected void populateNameAndCode(final SearchResultValueData source, final ProductData target)
	{
		target.setCode(this.<String> getValue(source, "code"));
		target.setName(this.<String> getValue(source, "productname"));
	}

	/**
	 * This method returns a list of image data beans for the Product Data to render on the search page
	 * 
	 * @param source
	 * @param productCode
	 * 
	 */
	protected List<ImageData> createImageDataEnergizer(final SearchResultValueData source, final String productCode)
	{
		final List<ImageData> result = new ArrayList<ImageData>();

		addImageDataEnergizer(source, "thumbnail", result, productCode);
		addImageDataEnergizer(source, "product", result, productCode);

		return result;
	}

	/**
	 * This method adds the image data beans to the image data list which needs to be a part of the product data.
	 * 
	 * @param source
	 * @param imageFormat
	 * @param images
	 * @param productCode
	 */
	protected void addImageDataEnergizer(final SearchResultValueData source, final String imageFormat,
			final List<ImageData> images, final String productCode)
	{
		try
		{
			final ProductModel product = productService.getProductForCode(productCode);
			final ImageData imageData = createImageData();
			imageData.setImageType(ImageDataType.PRIMARY);
			imageData.setFormat(imageFormat);
			if (imageFormat.equals("thumbnail"))
			{
				if (product.getThumbnail() != null && product.getThumbnail().getURL() != null)
				{
					imageData.setUrl(product.getThumbnail().getURL());
					images.add(imageData);
				}
			}
			else
			{
				if (product.getPicture() != null && product.getPicture().getURL() != null)
				{
					imageData.setUrl(product.getPicture().getURL());
					images.add(imageData);
				}
			}
		}
		catch (final UnknownIdentifierException exception)
		{
			LOG.error("Product  is not available, Hence removed from solr result.", exception);
		}
	}



	/**
	 * @return the productService
	 */
	@Override
	public ProductService getProductService()
	{
		return productService;
	}


	/**
	 * @param productService
	 *           the productService to set
	 */
	@Override
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}



}
