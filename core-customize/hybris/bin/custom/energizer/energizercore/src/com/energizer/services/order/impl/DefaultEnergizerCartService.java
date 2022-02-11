/**
 *
 */
package com.energizer.services.order.impl;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.util.ContainerData;
import com.energizer.core.util.EnergizerProductPalletHeight;
import com.energizer.core.util.EnergizerWeightOrVolumeConverter;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.services.product.EnergizerProductService;



/**
 * @author Bivash Pandit
 *
 */
public class DefaultEnergizerCartService implements EnergizerCartService
{
	@Resource
	CartService cartService;
	@Resource
	ConfigurationService configurationService;

	@Resource(name = "energizerProductService")
	EnergizerProductService energizerProductService;

	@Resource
	ModelService modelService;

	@Resource
	private UserService userService;

	@Resource
	private CMSSiteService cmsSiteService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	private BigDecimal truckVolume;
	private BigDecimal truckWeight;

	private BigDecimal twentyFeetContainerVolume;
	private BigDecimal twentyFeetContainerWeight;
	private BigDecimal fourtyFeetContainerVolume;
	private BigDecimal fourtyFeetContainerWeight;
	private Double twentyFeetContainerHeightInInches;
	private Double fortyFeetContainerHeightInInches;
	private final BigDecimal hundred = new BigDecimal(100);

	private BigDecimal disableTwentyFeetContainerVolume;
	private BigDecimal disableFourtyFeetContainerVolume;

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerCartService.class.getName());

	private static final String TWENTY_FEET_CONTAINER = "twenty.feet.container";
	private static final String FORTY_FEET_CONTAINER = "forty.feet.container";
	private static final String TWENTY_FEET_CONTAINER_VOLUME_KEY = "twenty.feet.container.volume";
	private static final String TWENTY_FEET_CONTAINER_WEIGHT_KEY = "twenty.feet.container.weight";
	private static final String FORTY_FEET_CONTAINER_VOLUME_KEY = "fourty.feet.container.volume";
	private static final String FORTY_FEET_CONTAINER_WEIGHT_KEY = "fourty.feet.container.weight";
	private static final String FORTY_FEET_CONTAINER_HEIGHT_INCHES = "heightInInches.40FT";
	private static final String TWENTY_FEET_CONTAINER_HEIGHT_INCHES = "heightInInches.20FT";
	private static final String TOTAL_PALLET_COUNT_FOR_2SLIPSHEET = "total.palletcount.2slipsheet";
	private static final String SLOT_PERCENTAGE_40FT = "slot.percentage.40ft";
	private static final String SLOT_PERCENTAGE_20FT = "slot.percentage.20ft";

	private static final String DISABLE_TWENTY_FEET_CONTAINER_VOLUME_KEY = "twenty.feet.container.volume.disable";
	private static final String DISABLE_FORTY_FEET_CONTAINER_VOLUME_KEY = "fourty.feet.container.volume.disable";
	private static final String FREIGHT_TRUCK = "Truck";
	private static final String FREIGHT_CONTAINER = "Container";
	private static final String EU_PALLET = "EU";
	private static final String US_PALLET = "US";

	private final BigDecimal ZERO = new BigDecimal(0);

	boolean nonPalletProductsExists = false;

	// Added for EMEA Truck Optimization - START
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	// Added for EMEA Truck Optimization - END

	int palletCount = 0;
	int virtualPallet = 0;
	int partialPallet = 0;

	List<String> message = null;
	List<EnergizerProductPalletHeight> products = null;
	HashMap doubleStackMap = null;
	LinkedHashMap<Integer, Integer> floorSpaceProductMap = null;
	LinkedHashMap<Integer, Double> nonPalletFloorSpaceProductMap = null;
	ArrayList<EnergizerProductPalletHeight> productsListA = null;
	List<EnergizerProductPalletHeight> nonPalletProductsList = new ArrayList<EnergizerProductPalletHeight>();
	final ArrayList<EnergizerProductPalletHeight> sortedProductsListA = new ArrayList<EnergizerProductPalletHeight>();
	ArrayList<EnergizerProductPalletHeight> productsListB = null;
	LinkedHashMap<Integer, List<String>> palStackData = null;
	int fullPalletCount = 0;
	int partialPalletCount = 0;

	@Override
	public CartData calCartContainerUtilization(final CartData cartData, String containerHeight, final String packingOption,
			final boolean enableContOptimization) throws Exception
	{
		CartData cartDataTemp = null;
		try
		{
			final String packingOptionWithNoAlgorithm = Config.getParameter("energizer.disable.packingOption");
			twentyFeetContainerVolume = new BigDecimal(
					configurationService.getConfiguration().getDouble(TWENTY_FEET_CONTAINER_VOLUME_KEY, null));
			twentyFeetContainerWeight = new BigDecimal(
					configurationService.getConfiguration().getDouble(TWENTY_FEET_CONTAINER_WEIGHT_KEY, null));
			fourtyFeetContainerVolume = new BigDecimal(
					configurationService.getConfiguration().getDouble(FORTY_FEET_CONTAINER_VOLUME_KEY, null));
			fourtyFeetContainerWeight = new BigDecimal(
					configurationService.getConfiguration().getDouble(FORTY_FEET_CONTAINER_WEIGHT_KEY, null));

			disableTwentyFeetContainerVolume = new BigDecimal(
					configurationService.getConfiguration().getDouble(DISABLE_TWENTY_FEET_CONTAINER_VOLUME_KEY, null));
			disableFourtyFeetContainerVolume = new BigDecimal(
					configurationService.getConfiguration().getDouble(DISABLE_FORTY_FEET_CONTAINER_VOLUME_KEY, null));

			twentyFeetContainerVolume = twentyFeetContainerVolume.setScale(6, BigDecimal.ROUND_UP);
			twentyFeetContainerWeight = twentyFeetContainerWeight.setScale(2, BigDecimal.ROUND_UP);
			fourtyFeetContainerVolume = fourtyFeetContainerVolume.setScale(6, BigDecimal.ROUND_UP);
			fourtyFeetContainerWeight = fourtyFeetContainerWeight.setScale(2, BigDecimal.ROUND_UP);

			disableTwentyFeetContainerVolume = disableTwentyFeetContainerVolume.setScale(6, BigDecimal.ROUND_UP);
			disableFourtyFeetContainerVolume = disableFourtyFeetContainerVolume.setScale(6, BigDecimal.ROUND_UP);


			if (!enableContOptimization)
			{
				if (message != null && message.size() > 0)
				{
					message.clear();
				}
				if (products != null && products.size() > 0)
				{
					products.clear();
				}

				containerHeight = null;
				doubleStackMap = new HashMap();
				// 2 Wooden Base
				cartDataTemp = calCartContainerUtilizationWithSlipsheets(cartData, containerHeight);
				cartDataTemp.setContainerPackingType("Not Applicable");

			}
			else if (packingOption.equals(packingOptionWithNoAlgorithm) && enableContOptimization)
			{
				if (message != null && message.size() > 0)
				{
					message.clear();
				}
				if (products != null && products.size() > 0)
				{
					products.clear();
				}
				doubleStackMap = new HashMap();
				// 2 Slip Sheets
				cartDataTemp = calCartContainerUtilizationWithEnableSlipsheets(cartData, containerHeight);
				cartDataTemp.setContainerPackingType(packingOptionWithNoAlgorithm);

			}
			else
			{
				try
				{
					// Slip Sheets & Wooden Base
					cartDataTemp = calCartContainerUtilizationWithSlipSheetsWoodenBase(cartData, containerHeight, packingOption);
					cartDataTemp.setEnableFloorSpaceGraphics(true);
					cartDataTemp.setContainerHeight(containerHeight);
					cartDataTemp.setContainerPackingType(packingOption);
				}
				catch (final Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured in calCartContainerUtilization method ::: " + e);
		}

		return cartDataTemp;
	}

	/**
	 * // Slip Sheets & Wooden Base
	 *
	 * @param cartData
	 * @param containerHeight
	 * @param packingOption
	 * @return
	 * @throws Exception
	 */
	public CartData calCartContainerUtilizationWithSlipSheetsWoodenBase(final CartData cartData, final String containerHeight,
			final String packingOption) throws Exception
	{
		try
		{
			doubleStackMap = new HashMap();
			floorSpaceProductMap = new LinkedHashMap<Integer, Integer>();
			nonPalletFloorSpaceProductMap = new LinkedHashMap<Integer, Double>();
			palStackData = new LinkedHashMap<Integer, List<String>>();
			double availableVolume = 100;
			double availableWeight = 100;
			double availableHeight = 0;
			double volumeOfNonPalletProduct = 0;
			double weightOfNonPalletProduct = 0;
			double volumeOfAllNonPalletProduct = 0;
			double weightOfAllNonPalletProduct = 0;
			final double halfFilledSlotVolume = 0;
			double nonPalletVolumePercent = 0;
			int floorSpaceCount = 0;
			double volume = 0;
			double weight = 0;
			//int palletCount = 0;
			double totalPalletHeight = 0;
			int totalPalletsCount = 0;
			double productWeight = 0;
			double nonPalletProductWeightPercent = 0;
			double percentVolumePerSlot = 0.0;
			double slotPercentageForNonPallet = 0;
			double percentVolumePerSlotForNonPallet = 0.0;

			int actualPallet = 0;
			int virtualPallet = 0;
			int partialPallet = 0;

			String matrix[][] = null;

			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);

			message = new ArrayList<String>();
			products = new ArrayList<EnergizerProductPalletHeight>();
			productsListA = new ArrayList<EnergizerProductPalletHeight>();
			productsListB = new ArrayList<EnergizerProductPalletHeight>();

			//percentage=configurationService.getConfiguration().getDouble(SLOT_PERCENTAGE, null);


			if (containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
			{
				totalPalletsCount = 20;
				percentVolumePerSlot = availableVolume * 0.05;
				slotPercentageForNonPallet = configurationService.getConfiguration().getDouble(SLOT_PERCENTAGE_20FT, null);
			}
			else if (containerHeight.equals(Config.getParameter(FORTY_FEET_CONTAINER)))
			{
				totalPalletsCount = 40;
				percentVolumePerSlot = availableVolume * 0.025;
				slotPercentageForNonPallet = configurationService.getConfiguration().getDouble(SLOT_PERCENTAGE_40FT, null);
			}

			volume = getPercentage(getWeightOfProductsInCart(cartData, "WEIGHTOFALLPRODUCTS"),
					getVolumeOfProductsInCart(cartData, "VOLUMEOFALLPRODUCTS"), containerHeight).getPercentVolumeUses();

			LOG.info("Voume of all products with packing option " + packingOption + ":" + volume);

			weight = getPercentage(getWeightOfProductsInCart(cartData, "WEIGHTOFALLPRODUCTS"),
					getVolumeOfProductsInCart(cartData, "VOLUMEOFALLPRODUCTS"), containerHeight).getPercentWeightUses();

			LOG.info("weight of all products with packing option " + packingOption + ":" + weight);
			cartData.setContainerHeight(containerHeight);

			productsListA = getPalletsCountOfProductsInCart(cartData, productsListA);

			palletCount = productsListA.size();

			for (final Iterator iterator = productsListA.iterator(); iterator.hasNext();)
			{

				final EnergizerProductPalletHeight enrProductPalTemp = (EnergizerProductPalletHeight) iterator.next();

				if (enrProductPalTemp.getCalculatedUOM().equals(enrProductPalTemp.getOrderedUOM())
						&& enrProductPalTemp.getOrderedUOM().equals("PAL"))
				{
					actualPallet++;
				}
				else
				{
					virtualPallet++;
				}

			}

			LOG.info("Pallet count:" + palletCount);

			if (weight > 100 || volume > 100 || palletCount > totalPalletsCount)
			{
				floorSpaceCount = totalPalletsCount / 2;
				//Display an error message "Reduce the products in the cart"
				LOG.info("Either volume is greater than 100 or pallet count is greater than" + totalPalletsCount);

				//cartData.setTotalProductVolumeInPercent(100.00);
				if ((weight > 100 || volume > 100) && palletCount > totalPalletsCount)
				{
					message.add("Dear Customer, your order will not fit in one container. You can order maximum " + totalPalletsCount
							+ " PAL in one order with selected container packing material. Please, adjust the cart and/or place multiple orders.");

					availableVolume = -1;
					availableWeight = -1;
					cartData.setIsFloorSpaceFull(true);
					cartData.setTotalProductVolumeInPercent((Math.round((100 - availableVolume) * 100.0) / 100.0));
					cartData.setTotalProductWeightInPercent((Math.round((100 - availableWeight) * 100.0) / 100.0));
				}

				else if (weight > 100 || volume > 100)
				{
					cartData.setTotalProductVolumeInPercent(volume);
					cartData.setTotalProductWeightInPercent(weight);
					cartData.setIsContainerFull(true);
				}

				else if (palletCount > totalPalletsCount)
				{
					message.add("Dear Customer, your order will not fit in one container. You can order maximum " + totalPalletsCount
							+ " PAL in one order with selected container packing material. Please, adjust the cart and/or place multiple orders.");

					availableVolume = -1;
					availableWeight = -1;
					cartData.setIsFloorSpaceFull(true);
					cartData.setTotalProductVolumeInPercent((Math.round((100 - availableVolume) * 100.0) / 100.0));
					cartData.setTotalProductWeightInPercent((Math.round((100 - availableWeight) * 100.0) / 100.0));

				}
			}
			else
			{

				Collections.sort(productsListA);

				matrix = new String[2][totalPalletsCount / 2];

				for (floorSpaceCount = 0; floorSpaceCount < totalPalletsCount / 2; floorSpaceCount++)
				{
					LOG.info("***************** The floorSpace Count loop " + floorSpaceCount + " starts ****************");

					availableHeight = getAvailableHeight(packingOption, containerHeight);
					LOG.info("Available Height:" + availableHeight);
					if (availableVolume <= 0)
					{
						//Display an error message "Reduce the products in the cart"
						LOG.info(" Reduce the products in the cart!!! ");
						message.add("Please reduce  products fom the cart");
					}
					else
					{
						if ((productsListA.size() == 0))
						{
							if (floorSpaceCount >= (totalPalletsCount / 2))
							{
								LOG.info("Some products cannot be double stacked");
								break;
							}
							else
							{
								LOG.info(" The filledup volume is: " + (100 - availableVolume));
								break;
							}
						}
						else
						{
							final int minIndex = 1;
							final int maxIndex = productsListA.size();
							LOG.info("Maximum n Minimum Index: " + maxIndex + " : " + minIndex);
							if ((productsListA.size() != 1))
							{
								LOG.info("smallest height:" + productsListA.get(maxIndex - 1).getPalletHeight());
								LOG.info("largest height:" + productsListA.get(minIndex - 1).getPalletHeight());
								totalPalletHeight = productsListA.get(maxIndex - 1).getPalletHeight()
										+ productsListA.get(minIndex - 1).getPalletHeight();
							}
							else
							{
								totalPalletHeight = productsListA.get(maxIndex - 1).getPalletHeight();
							}
							if ((availableHeight > totalPalletHeight))
							{
								if (productsListA.size() > 0 && (productsListA.size() != 1))
								{
									final EnergizerProductPalletHeight tempHighestPalletHeightProduct = productsListA.get(0);
									productWeight = productWeight
											+ getWeightOfGivenMaterial(productsListA.get(maxIndex - 1).getErpMaterialId(),
													productsListA.get(maxIndex - 1).getCalculatedUOM())
											+ getWeightOfGivenMaterial(productsListA.get(minIndex - 1).getErpMaterialId(),
													productsListA.get(minIndex - 1).getCalculatedUOM());

									matrix[0][floorSpaceCount] = productsListA.get(maxIndex - 1).getErpMaterialId();
									matrix[1][floorSpaceCount] = productsListA.get(minIndex - 1).getErpMaterialId();
									productsListA.remove(maxIndex - 1); // removeHighestLowestPalletList(productsListA, maxIndex);;
									productsListA.remove(minIndex - 1);
									floorSpaceProductMap.put(floorSpaceCount, 2);


									if (containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
									{
										availableVolume = availableVolume - 10;
									}
									if (containerHeight.equals(Config.getParameter(FORTY_FEET_CONTAINER)))
									{
										availableVolume = availableVolume - 5;
									}
								}
								else
								{

									productWeight = productWeight
											+ getWeightOfGivenMaterial(productsListA.get(maxIndex - 1).getErpMaterialId(),
													productsListA.get(maxIndex - 1).getCalculatedUOM());
									final double volumeOfSinglePallet = getVolumeOfHighestPallet(productsListA.get(0).getErpMaterialId(),
											productsListA.get(0).getCalculatedUOM());
									final double percentageVolumeOfSinglePallet = getPercentage(new BigDecimal(0),
											new BigDecimal(volumeOfSinglePallet), containerHeight).getPercentVolumeUses();
									availableVolume = availableVolume - percentageVolumeOfSinglePallet;

									matrix[0][floorSpaceCount] = productsListA.get(0).getErpMaterialId();

									productsListA.remove(maxIndex - 1);
									floorSpaceProductMap.put(floorSpaceCount, 1);
								}


								availableHeight = availableHeight - totalPalletHeight;
								LOG.info("Available volume:" + availableVolume);
								LOG.info("Available Height:" + availableHeight);
							}
							else if (availableHeight > productsListA.get(0).getPalletHeight())
							{
								final EnergizerProductPalletHeight tempProduct = productsListA.get(0);
								LOG.info("first element :" + tempProduct.getErpMaterialId());
								availableHeight = availableHeight - tempProduct.getPalletHeight();

								productsListB.add(tempProduct);
								//	final long noOfcasesinHighestPallet = noOfCases(productsListA.get(minIndex).getErpMaterialId());
								final double volumeOfHighestPallet = getVolumeOfHighestPallet(tempProduct.getErpMaterialId(),
										tempProduct.getCalculatedUOM());
								final double percentageVolumeOfHighestPallet = getPercentage(new BigDecimal(0),
										new BigDecimal(volumeOfHighestPallet), containerHeight).getPercentVolumeUses();
								LOG.info("volume of single product:" + volumeOfHighestPallet);
								try
								{
									if (!doubleStackMap.containsKey(tempProduct.getErpMaterialId())
											&& tempProduct.getOrderedUOM().equals("PAL"))
									{
										doubleStackMap = getPossibleDoubleStackedProducts(tempProduct.getErpMaterialId(), availableHeight,
												doubleStackMap);
									}
									productWeight = productWeight
											+ getWeightOfGivenMaterial(tempProduct.getErpMaterialId(), tempProduct.getCalculatedUOM());

									matrix[0][floorSpaceCount] = tempProduct.getErpMaterialId();

									productsListA.remove(tempProduct);
									floorSpaceProductMap.put(floorSpaceCount, 1);
								}
								catch (final ArrayIndexOutOfBoundsException ax)
								{
									LOG.info("ArrayIndexOutOfBoundsException occured ::: ");
									ax.printStackTrace();
									throw ax;
								}
								catch (final Exception ex)
								{
									LOG.info("Exception occured ::: ");
									ex.printStackTrace();
									throw ex;
								}
								LOG.info("Available Height:" + availableHeight);
								availableVolume = availableVolume - percentageVolumeOfHighestPallet;
								LOG.info("Available volume:" + availableVolume);
							}
							else
							{
								if (containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
								{
									LOG.info("Please select the different container height!!!");
									message.add("Please select the different container height");
									break;
								}
							}
						}
					}
					LOG.info("*************** The floorSpace count loop ends******************");
				}

				LOG.info(" Productweight: " + productWeight);

				availableWeight = availableWeight
						- getPercentage(new BigDecimal(productWeight), new BigDecimal(0), containerHeight).getPercentWeightUses();

				/*
				 * if (nonPalletProductsList.size() > 0 && !packingOption.equalsIgnoreCase("2 WOODEN BASE")) {
				 *
				 * LOG.info("Select packing Option : 2 Wooden Base "); message .add(
				 * "Dear Customer, Your order contains some products whose quantity is less than a full pallet (partial pallet). Partial pallets can be shipped only with 2 wooden base packing material. Please change your packing material or remove the partial pallet products."
				 * ); cartData.setErrorMessage(true); }
				 *
				 * else
				 */if (nonPalletProductsList.size() > 0 && palletCount < totalPalletsCount)
				{
					LOG.info("******************** NonPallet Products Volume calculation starts***********************************");
					LOG.info("Non pallet products exist");


					percentVolumePerSlotForNonPallet = percentVolumePerSlot * (slotPercentageForNonPallet / 100);

					LOG.info(" NonPalletProductsList size: " + nonPalletProductsList.size());
					LOG.info("Available Volume after filling pallets: " + availableVolume);
					LOG.info("Available Weight: " + availableWeight);

					for (final Iterator iterator = nonPalletProductsList.iterator(); iterator.hasNext();)
					{

						final EnergizerProductPalletHeight enrProductPalTemp = (EnergizerProductPalletHeight) iterator.next();

						if (enrProductPalTemp.getCalculatedUOM().equalsIgnoreCase("LAY"))
						{
							volumeOfNonPalletProduct = EnergizerWeightOrVolumeConverter
									.getConversionValue(getLayerVolumeUnit(enrProductPalTemp.getErpMaterialId()),
											new BigDecimal(getLayerVolume(enrProductPalTemp.getErpMaterialId())))
									.doubleValue();
							weightOfNonPalletProduct = EnergizerWeightOrVolumeConverter
									.getConversionValue(getLayerWeightUnit(enrProductPalTemp.getErpMaterialId()),
											new BigDecimal(getLayerWeight(enrProductPalTemp.getErpMaterialId())))
									.doubleValue();
						}
						else
						{
							volumeOfNonPalletProduct = getVolumeOfHighestPallet(enrProductPalTemp.getErpMaterialId(),
									enrProductPalTemp.getOrderedUOM());
							weightOfNonPalletProduct = getWeightOfGivenMaterial(enrProductPalTemp.getErpMaterialId(),
									enrProductPalTemp.getOrderedUOM());
						}

						LOG.info("volume & weight of " + enrProductPalTemp.getErpMaterialId() + " are " + volumeOfNonPalletProduct
								+ " & " + weightOfNonPalletProduct);

						volumeOfAllNonPalletProduct = volumeOfAllNonPalletProduct + volumeOfNonPalletProduct;
						weightOfAllNonPalletProduct = weightOfAllNonPalletProduct + weightOfNonPalletProduct;

						LOG.info("NonPalletProduct fitted inside the container: " + enrProductPalTemp.getErpMaterialId());
						iterator.remove();

					}

					LOG.info("******************** NonPallet Products Volume calculation ends***********************************");
					LOG.info("Volume of all Nonpallet products: " + volumeOfAllNonPalletProduct);
					LOG.info("Weight of all Nonpallet products: " + weightOfAllNonPalletProduct);

					nonPalletProductWeightPercent = getPercentage(new BigDecimal(weightOfAllNonPalletProduct), new BigDecimal(0),
							containerHeight).percentWeightUses;
					availableWeight = availableWeight - nonPalletProductWeightPercent;
					nonPalletVolumePercent = getPercentage(new BigDecimal(0), new BigDecimal(volumeOfAllNonPalletProduct),
							containerHeight).percentVolumeUses;
					LOG.info("% Volume of all Nonpallet products: " + nonPalletVolumePercent);
					LOG.info("% Weight of all Nonpallet products: " + nonPalletProductWeightPercent);

					for (int nonPalletFloorSpaceCount = 0; nonPalletFloorSpaceCount < totalPalletsCount
							/ 2; nonPalletFloorSpaceCount++)
					{
						boolean isBlockFilled;
						isBlockFilled = floorSpaceProductMap.containsKey(nonPalletFloorSpaceCount);
						int value = 0;

						if (isBlockFilled && nonPalletVolumePercent > 0)
						{
							value = floorSpaceProductMap.get(nonPalletFloorSpaceCount);
							if (value == 1)
							{
								if (nonPalletVolumePercent >= percentVolumePerSlotForNonPallet)
								{
									availableVolume = availableVolume - percentVolumePerSlot;
									nonPalletVolumePercent = nonPalletVolumePercent - percentVolumePerSlotForNonPallet;
									nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount, 1.0);
									matrix[1][nonPalletFloorSpaceCount] = "Mixed-Pallet";
									partialPallet = partialPallet + 1;
								}

								else
								{
									availableVolume = availableVolume - nonPalletVolumePercent;
									nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount,
											nonPalletVolumePercent / percentVolumePerSlotForNonPallet);
									partialPallet = (int) (partialPallet
											+ Math.ceil(nonPalletVolumePercent / percentVolumePerSlotForNonPallet));
									matrix[1][nonPalletFloorSpaceCount] = "Mixed-Pallet";
									nonPalletVolumePercent = 0;

								}
							}
						}
						else
						{
							if (nonPalletVolumePercent > 0 && nonPalletVolumePercent >= 2 * percentVolumePerSlotForNonPallet)
							{
								availableVolume = availableVolume - (2 * percentVolumePerSlot);
								nonPalletVolumePercent = nonPalletVolumePercent - (2 * percentVolumePerSlotForNonPallet);
								nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount, 2.0);
								matrix[0][nonPalletFloorSpaceCount] = "Mixed-Pallet";
								matrix[1][nonPalletFloorSpaceCount] = "Mixed-Pallet";
								partialPallet = partialPallet + 2;
							}
							else if (nonPalletVolumePercent > 0 && nonPalletVolumePercent < 2 * percentVolumePerSlotForNonPallet
									&& nonPalletVolumePercent > percentVolumePerSlotForNonPallet)
							{
								nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount,
										nonPalletVolumePercent / percentVolumePerSlotForNonPallet);
								partialPallet = (int) (partialPallet
										+ Math.ceil(nonPalletVolumePercent / percentVolumePerSlotForNonPallet));
								availableVolume = availableVolume - percentVolumePerSlot;
								availableVolume = availableVolume - (nonPalletVolumePercent - percentVolumePerSlotForNonPallet);
								matrix[0][nonPalletFloorSpaceCount] = "Mixed-Pallet";
								matrix[1][nonPalletFloorSpaceCount] = "Mixed-Pallet";
								nonPalletVolumePercent = 0;
							}
							else if (nonPalletVolumePercent > 0 && nonPalletVolumePercent < percentVolumePerSlotForNonPallet)
							{
								nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount,
										nonPalletVolumePercent / percentVolumePerSlotForNonPallet);
								partialPallet = (int) (partialPallet
										+ Math.ceil(nonPalletVolumePercent / percentVolumePerSlotForNonPallet));
								availableVolume = availableVolume - nonPalletVolumePercent;
								nonPalletVolumePercent = 0;
								matrix[0][nonPalletFloorSpaceCount] = "Mixed-Pallet";
							}
						}
					}
				}
				if ((nonPalletProductsExists && palletCount > totalPalletsCount) || nonPalletVolumePercent > 0)
				{
					LOG.info(
							"******************Some nonpallet products cannot fit inside container.Please remove nonpallets products from the cart***************************");
					//message.add("Please remove nonpallets products from the cart");
					cartData.setIsContainerFull(true);
				}

				if (productsListA.size() > 0)
				{

					List<EnergizerProductPalletHeight> productList = new ArrayList<EnergizerProductPalletHeight>();
					productList = getProductsWithOrderedUOM(productsListA);

					for (final EnergizerProductPalletHeight product : productList)
					{
						products.add(product);
					}

				}
				/*
				 * if (nonPalletProductsList.size() > 0) { for (final Iterator nonPalletProduct = nonPalletProductsList.iterator();
				 * nonPalletProduct.hasNext();) { products.add((EnergizerProductPalletHeight) nonPalletProduct); } }
				 */

				LOG.info("******************* The final data is as below: *****************");
				LOG.info("FloorSpace count: " + floorSpaceCount);
				LOG.info(" Available Height: " + availableHeight);
				LOG.info(" After filling container Available Volume: " + availableVolume);
				LOG.info("last volume: " + halfFilledSlotVolume);

				LOG.info("Actual pallet count:" + actualPallet);
				LOG.info("Virtual pallet count:" + virtualPallet);
				LOG.info("partial pallet count:" + partialPallet);

				cartData.setFloorSpaceCount(floorSpaceCount);
				cartData.setTotalProductVolumeInPercent((Math.round((100 - availableVolume) * 100.0) / 100.0));
				cartData.setTotalProductWeightInPercent((Math.round((100 - availableWeight) * 100.0) / 100.0));


				for (int innerCount = 0; innerCount < (totalPalletsCount / 2); innerCount++)
				{
					final List<String> palStackProduct = new ArrayList<String>();

					for (int outerCount = 0; outerCount < 2; outerCount++)
					{
						if (matrix[outerCount][innerCount] != null)
						{
							LOG.info("Element at i=" + outerCount + "and j=" + innerCount + "value " + matrix[outerCount][innerCount]);
							palStackProduct.add(matrix[outerCount][innerCount]);
						}
						else
						{
							palStackProduct.add("NA");
						}


					}
					palStackData.put(innerCount, palStackProduct);
				}


				cartData.setPalStackData(palStackData);

			}

			LOG.info("******************************** Possible Double Stacked Products *******************************");
			LOG.info("Map size : " + doubleStackMap.size());

			final Set doubleStackMapEntrySet = doubleStackMap.entrySet();

			for (final Iterator iterator = doubleStackMapEntrySet.iterator(); iterator.hasNext();)
			{
				final Map.Entry mapEntry = (Map.Entry) iterator.next();
				LOG.info("key: " + mapEntry.getKey() + " value: " + mapEntry.getValue());
			}

			LOG.info("*********************** End *******************************************8");


			LOG.info("******************************** Floor Space Information *******************************");
			LOG.info("FloorSpaceProductsMap size : " + floorSpaceProductMap.size());

			final Set floorSpaceProductMapEntrySet = floorSpaceProductMap.entrySet();

			for (final Iterator iterator = floorSpaceProductMapEntrySet.iterator(); iterator.hasNext();)
			{
				final Map.Entry mapEntry = (Map.Entry) iterator.next();
				LOG.info("key: " + mapEntry.getKey() + " value: " + mapEntry.getValue());
			}

			LOG.info("===================================nonPallet floor List==============================");

			final Set nonPalletFloorSpaceProductMapEntrySet = nonPalletFloorSpaceProductMap.entrySet();

			for (final Iterator iterator = nonPalletFloorSpaceProductMapEntrySet.iterator(); iterator.hasNext();)
			{
				final Map.Entry mapEntry = (Map.Entry) iterator.next();
				LOG.info("key: " + mapEntry.getKey() + " value: " + mapEntry.getValue());
			}

			cartData.setAvailableVolume((Math.round((100 - cartData.getTotalProductVolumeInPercent()) * 100.0) / 100.0));
			cartData.setAvailableWeight((Math.round((100 - cartData.getTotalProductWeightInPercent()) * 100.0) / 100.0));
			cartData.setTotalPalletCount(palletCount);
			cartData.setVirtualPalletCount(virtualPallet);
			cartData.setPartialPalletCount(partialPallet);
			LOG.info("Available volume: " + cartData.getAvailableVolume() + " Available Weight: " + cartData.getAvailableWeight());

			//  ************ setting the OrderBlock  flag  ****************
			if (b2bUnit.getOrderBlock())
			{
				cartData.setIsOrderBlocked(true);
			}
			else
			{
				cartData.setIsOrderBlocked(false);
			}

			LOG.info("*********************** End *******************************************8");
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in Cart Container Utilisation ::::" + e);
			throw e;
		}
		return cartData;
	}

	/**
	 * As discussed with Kalyan, we need to show height in cms only 1 inch = 2.54cms Need to store the values in
	 * local.properties file.
	 *
	 * 20ft container - 10 floorSpaceCount 40ft container - 20 floorSpaceCount
	 *
	 */
	public double getAvailableHeight(final String packingOption, final String containerHeight)
	{
		twentyFeetContainerHeightInInches = configurationService.getConfiguration().getDouble(TWENTY_FEET_CONTAINER_HEIGHT_INCHES,
				null);
		fortyFeetContainerHeightInInches = configurationService.getConfiguration().getDouble(FORTY_FEET_CONTAINER_HEIGHT_INCHES,
				null);

		LOG.info("twentyFeetContainerHeightInInches : " + twentyFeetContainerHeightInInches);
		LOG.info("fortyFeetContainerHeightInInches : " + fortyFeetContainerHeightInInches);
		double availableHeight = 0;
		try
		{
			if (packingOption.equals("1 SLIP SHEET AND 1 WOODEN BASE")
					&& containerHeight.equals(Config.getParameter(FORTY_FEET_CONTAINER)))
			{
				availableHeight = (fortyFeetContainerHeightInInches - 5) * 2.54;
			}
			else if (packingOption.equals("2 WOODEN BASE") && containerHeight.equals(Config.getParameter(FORTY_FEET_CONTAINER)))
			{
				availableHeight = (fortyFeetContainerHeightInInches - (5 * 2)) * 2.54;
			}
			else if (packingOption.equals("1 SLIP SHEET AND 1 WOODEN BASE")
					&& containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
			{
				availableHeight = (twentyFeetContainerHeightInInches - 5) * 2.54;
			}
			else if (packingOption.equals("2 WOODEN BASE") && containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
			{
				availableHeight = (twentyFeetContainerHeightInInches - (5 * 2)) * 2.54;
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured while getting Available Height of Container:::" + e);
			throw e;
		}
		return availableHeight;
	}


	/**
	 *
	 * @param cartData
	 * @return
	 * @throws Exception
	 */
	public ArrayList<EnergizerProductPalletHeight> getPalletsCountOfProductsInCart(final CartData cartData,
			final ArrayList<EnergizerProductPalletHeight> productsListA) throws Exception
	{
		try
		{
			long palletsCount = 0;

			double palletHeight = 0;

			LOG.info("----------------------------------------" + productsListA.size());
			final CartModel cartModel = cartService.getSessionCart();
			final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

			if (productsListA != null && productsListA.size() > 0)
			{
				LOG.info("Clearing productsListA ");
				productsListA.clear();
			}

			if (nonPalletProductsList != null && nonPalletProductsList.size() > 0)
			{
				LOG.info("Clearing nonPalletProductsList: ");
				nonPalletProductsList.clear();
			}

			for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
			{
				final EnergizerProductModel enerGizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
				long casePerPallet = 0, layerPerPallet = 0, casePerLayer = 0;
				long baseUOMPal = 0, baseUOMLayer = 0, baseUOMCase = 0;
				final String erpMaterialId = enerGizerProductModel.getCode();
				final long itemQuantity = abstractOrderEntryModel.getQuantity();
				final EnergizerProductConversionFactorModel coversionFactor = null;
				String cmirUom = null;

				LOG.info("cart ProductCode: " + enerGizerProductModel.getCode());

				//	final BigDecimal baseUom = new BigDecimal(0);

				try
				{

					final String userId = userService.getCurrentUser().getUid();
					final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
					final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
							b2bUnit.getUid());
					final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
							.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());


					cmirUom = energizerCMIRModel.getUom();

					final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();


					if (cmirUom.equals("PAL") && cmirUom.equals(alternateUOM))
					{
						palletHeight = energizerProductConversionFactorModel.getPackageHeight().getMeasurement();
						LOG.info("pallet Height: " + palletHeight);

						final EnergizerProductPalletHeight energizerProductPalletHeight = new EnergizerProductPalletHeight();
						energizerProductPalletHeight.setErpMaterialId(erpMaterialId);
						energizerProductPalletHeight.setPalletHeight(palletHeight);
						energizerProductPalletHeight.setOrderedUOM(cmirUom);
						energizerProductPalletHeight.setIsVirtualPallet(false);
						energizerProductPalletHeight.setCalculatedUOM(cmirUom);

						productsListA.add(energizerProductPalletHeight);

						palletsCount = palletsCount + itemQuantity;

						if (itemQuantity > 1)
						{
							for (int i = 1; i <= itemQuantity - 1; i++)
							{
								final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
								energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
								energizerProductPalletHeightTemp.setPalletHeight(palletHeight);
								energizerProductPalletHeightTemp.setOrderedUOM(cmirUom);
								energizerProductPalletHeightTemp.setIsVirtualPallet(false);
								energizerProductPalletHeightTemp.setCalculatedUOM(cmirUom);
								productsListA.add(energizerProductPalletHeightTemp);
							}
						}
					}

					else if (!(cmirUom.equals("PAL")) && cmirUom.equals(alternateUOM))
					{
						LOG.info("----------------------- Start of nonPalletProducts calculation -----------------------------");

						double convertedPAL = 0, palFractionalPart = 0, balanceNonPal = 0;
						final double convertedLay = 0, layerFractionalPart = 0;

						/***** Calculation of all UOMs ********/
						final List<EnergizerProductConversionFactorModel> enrProductConversionList = energizerProductService
								.getAllEnergizerProductConversion(erpMaterialId);

						if (!enrProductConversionList.isEmpty())
						{
							for (final Iterator iterator = enrProductConversionList.iterator(); iterator.hasNext();)
							{
								final EnergizerProductConversionFactorModel enrProductConversionFactorModel = (EnergizerProductConversionFactorModel) iterator
										.next();

								final String altUOM = enrProductConversionFactorModel.getAlternateUOM();

								if (null != altUOM && altUOM.equalsIgnoreCase("PAL"))
								{
									baseUOMPal = enrProductConversionFactorModel.getConversionMultiplier();
									palletHeight = enrProductConversionFactorModel.getPackageHeight().getMeasurement();
									//enrProductConversionFactorModel.getPackageVolume();
									LOG.info(" &&& PAL Height &&&&&&&&" + palletHeight);
								}
								if (null != altUOM && altUOM.equalsIgnoreCase("CS"))
								{
									baseUOMCase = enrProductConversionFactorModel.getConversionMultiplier();

								}
								if (null != altUOM && altUOM.equalsIgnoreCase("LAY"))
								{
									baseUOMLayer = enrProductConversionFactorModel.getConversionMultiplier();
								}
							}
						}

						casePerPallet = baseUOMPal / baseUOMCase;
						layerPerPallet = baseUOMPal / baseUOMLayer;
						casePerLayer = baseUOMLayer / baseUOMCase;

						/***** End ********/


						LOG.info("pallet Height: " + palletHeight);
						LOG.info(" ***** UOM Conversion Multiplier details of" + erpMaterialId + " ------ casePerPallet:"
								+ casePerPallet + " -- casePerLayer: " + casePerLayer + " --layerPerPallet: " + layerPerPallet);

						if (cmirUom.equalsIgnoreCase("CS"))
						{
							LOG.info("Ordered Quantity of CS UOM: " + itemQuantity);

							convertedPAL = itemQuantity / casePerPallet;

							palFractionalPart = convertedPAL % 1;
							convertedPAL = convertedPAL - palFractionalPart;

							if (convertedPAL != 0 && convertedPAL >= 1)
							{
								for (int iPALCount = 1; iPALCount <= convertedPAL; iPALCount++)
								{
									final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
									energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
									energizerProductPalletHeightTemp.setPalletHeight(palletHeight);
									energizerProductPalletHeightTemp.setOrderedUOM(cmirUom);
									energizerProductPalletHeightTemp.setIsVirtualPallet(true);
									energizerProductPalletHeightTemp.setCalculatedUOM("PAL");
									productsListA.add(energizerProductPalletHeightTemp);
								}
							}
							balanceNonPal = itemQuantity - (casePerPallet * convertedPAL);

							LOG.info(" Remaining CS after converting CS into Pallets: " + balanceNonPal);

						}

						else if (cmirUom.equalsIgnoreCase("LAY"))
						{
							LOG.info("Ordered Quantity of LAY UOM: " + itemQuantity);

							convertedPAL = itemQuantity / layerPerPallet;

							palFractionalPart = convertedPAL % 1;
							convertedPAL = convertedPAL - palFractionalPart;

							if (convertedPAL != 0 && convertedPAL >= 1)
							{
								for (int iPALCount = 1; iPALCount <= convertedPAL; iPALCount++)
								{
									final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
									energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
									energizerProductPalletHeightTemp.setPalletHeight(palletHeight);
									energizerProductPalletHeightTemp.setOrderedUOM(cmirUom);
									energizerProductPalletHeightTemp.setIsVirtualPallet(true);
									energizerProductPalletHeightTemp.setCalculatedUOM("PAL");
									productsListA.add(energizerProductPalletHeightTemp);
								}
							}
							balanceNonPal = itemQuantity - (layerPerPallet * convertedPAL);

							LOG.info(" Remaining LAY after converting LAY into Pallets: " + balanceNonPal);

						}





						if (balanceNonPal > 0)
						{

							for (int iCaseCount = 1; iCaseCount <= balanceNonPal; iCaseCount++)
							{
								final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
								energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
								energizerProductPalletHeightTemp.setOrderedUOM(cmirUom);
								energizerProductPalletHeightTemp.setPalletHeight(0);
								energizerProductPalletHeightTemp.setCalculatedUOM(cmirUom);
								nonPalletProductsList.add(energizerProductPalletHeightTemp);
							}

						}
						LOG.info(" NonPalletProductsList size after calculating remaining CS and LAY: " + nonPalletProductsList.size());



						// end of if(orderedUOM==CS)

						if (nonPalletProductsList.size() > 0)
						{
							nonPalletProductsExists = true;
						}

						LOG.info("----------------------- End of nonPalletProducts calculation -----------------------------");

					}

				}
				catch (final Exception e)
				{
					e.printStackTrace();
					throw e;
					/*
					 * LOG.info(
					 * "*********************Exception occured in getPalletsCount method of DefaultEnergizerCartService class*************"
					 * + e.getMessage());
					 */
				}

			}

			LOG.info(" Products in nonPalletProductsList: " + nonPalletProductsList.size());
			LOG.info(" Products in ListA and palletscount: " + productsListA.size() + " : " + palletsCount);

			return productsListA;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured during pallet/nonpallet prodctslist calculation:::");
			throw e;
		}
	}

	public ArrayList<EnergizerProductPalletHeight> getPalletsCountOfProductsInCartForSlipSheet(final CartData cartData,
			final ArrayList<EnergizerProductPalletHeight> productsListA)
	{
		long palletsCount = 0;
		LOG.info("----------------------------------------" + productsListA.size());
		final CartModel cartModel = cartService.getSessionCart();
		final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

		if (productsListA != null && productsListA.size() > 0)
		{
			LOG.info("Clearing productsListA ");
			productsListA.clear();
		}

		for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
		{
			final EnergizerProductModel enerGizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
			final String erpMaterialId = enerGizerProductModel.getCode();
			final long itemQuantity = abstractOrderEntryModel.getQuantity();
			final EnergizerProductConversionFactorModel coversionFactor = null;
			String cmirUom = null;
			final BigDecimal baseUom = new BigDecimal(0);

			try
			{
				final String userId = userService.getCurrentUser().getUid();
				final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
				final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
						b2bUnit.getUid());
				final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
						.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());

				cmirUom = energizerCMIRModel.getUom();

				final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();

				if (cmirUom.equals("PAL") && cmirUom.equals(alternateUOM))
				{
					final double palletHeight = energizerProductConversionFactorModel.getPackageHeight().getMeasurement();
					final EnergizerProductPalletHeight energizerProductPalletHeight = new EnergizerProductPalletHeight();
					energizerProductPalletHeight.setErpMaterialId(erpMaterialId);
					energizerProductPalletHeight.setPalletHeight(palletHeight);
					productsListA.add(energizerProductPalletHeight);
					final BigDecimal lineItemToheight = new BigDecimal(0);

					palletsCount = palletsCount + itemQuantity;

					if (itemQuantity > 1)
					{
						for (int i = 1; i <= itemQuantity - 1; i++)
						{
							final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
							energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
							energizerProductPalletHeightTemp.setPalletHeight(palletHeight);
							productsListA.add(energizerProductPalletHeightTemp);
						}
					}
				}
				else if (!(cmirUom.equals("PAL")) && cmirUom.equals(alternateUOM))
				{
					nonPalletProductsExists = true;
				}

			}
			catch (final Exception e)
			{
				//e.printStackTrace();
				LOG.info("*********************Exception occured*************" + e.getMessage());
			}

		}

		LOG.info(" Products in ListA and palletscount: " + productsListA.size() + " : " + palletsCount);

		return productsListA;
	}

	public HashMap getPossibleDoubleStackedProducts(final String cartERPMaterialID, final double availableHeight,
			final HashMap doubleStackMap) throws Exception
	{
		try
		{
			String cmirUom = null;
			final Set<EnergizerProductModel> erpMaterialIDSetFromDB = new HashSet<EnergizerProductModel>();
			List<EnergizerProductModel> energizerERPMaterialIDList = null;
			final List<String> possibleDoubleStackList = new ArrayList();
			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			energizerERPMaterialIDList = energizerProductService.getEnergizerERPMaterialID();

			erpMaterialIDSetFromDB.addAll(energizerERPMaterialIDList);

			for (final EnergizerProductModel energizerProductModel : erpMaterialIDSetFromDB)
			{

				final EnergizerCMIRModel energizerCMIRModel = energizerProductService
						.getEnergizerCMIR(energizerProductModel.getCode(), b2bUnit.getUid());

				if (energizerCMIRModel != null && energizerCMIRModel.getIsActive())
				{
					final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
							.getEnergizerProductConversion(energizerProductModel.getCode(), b2bUnit.getUid());
					if (null != energizerProductConversionFactorModel)
					{
						cmirUom = energizerCMIRModel.getUom();
						final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();

						if (cmirUom.equals("PAL") && cmirUom.equals(alternateUOM))
						{

							final double palletHeight = energizerProductConversionFactorModel.getPackageHeight().getMeasurement();

							if (palletHeight <= availableHeight)
							{
								possibleDoubleStackList.add(energizerProductModel.getCode());
								if (possibleDoubleStackList.size() > 0)
								{
									doubleStackMap.put(cartERPMaterialID, possibleDoubleStackList);
								}
							}
						}
					}
				}
			}
			if (!doubleStackMap.containsKey(cartERPMaterialID))
			{
				doubleStackMap.put(cartERPMaterialID, null);
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while calculatind double stack product in cart:::");
			throw e;
		}
		return doubleStackMap;
	}

	/**
	 * // 2 Wooden Base
	 *
	 * @param cartData
	 * @param containerHeight
	 * @return
	 * @throws Exception
	 */
	public CartData calCartContainerUtilizationWithSlipsheets(final CartData cartData, final String containerHeight)
			throws Exception
	{
		try
		{
			final CartModel cartModel = cartService.getSessionCart();
			final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

			//final BigDecimal totalCartVolume = new BigDecimal(0);
			BigDecimal totalCartWt = new BigDecimal(0);

			BigDecimal totalVolumeDisable20FT = new BigDecimal(0);
			BigDecimal totalVolumeDisable40FT = new BigDecimal(0);

			message = new ArrayList<String>();
			/**
			 * # Container volume in M3 and weight in KG ##########################################
			 *
			 * twenty.feet.container.volume=30.44056 twenty.feet.container.weight=15961.90248 fourty.feet.container.volume=70.62209
			 * fourty.feet.container.weight=18234.3948
			 */

			//Converted the container volume from Metric unit to Cubic centimeter

			final BigDecimal twentyFeetContainerVolumInCCM = disableTwentyFeetContainerVolume.multiply(new BigDecimal(1000000));

			final BigDecimal fourtyFeetContainerVolumeInCCM = disableFourtyFeetContainerVolume.multiply(new BigDecimal(1000000));

			LOG.info("==============Disable 2 Slip Sheet=============");
			for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
			{
				final EnergizerProductModel enerGizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
				final String erpMaterialId = enerGizerProductModel.getCode();
				final BigDecimal itemQuantity = new BigDecimal((abstractOrderEntryModel.getQuantity().longValue()));
				EnergizerProductConversionFactorModel coversionFactor = null;
				String cmirUom = null;

				try
				{
					final String userId = userService.getCurrentUser().getUid();
					final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
					final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
							b2bUnit.getUid());
					if (null != energizerCMIRModel)
					{
						cmirUom = energizerCMIRModel.getUom();
					}
					final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
							.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());

					final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();
					if (null != alternateUOM && null != cmirUom && alternateUOM.equalsIgnoreCase(cmirUom))
					{
						coversionFactor = energizerProductConversionFactorModel;
					}

					//  ************ setting the OrderBlock  flag  ****************
					if (b2bUnit.getOrderBlock())
					{
						cartData.setIsOrderBlocked(true);
					}
					else
					{
						cartData.setIsOrderBlocked(false);
					}
				}
				catch (final Exception e)
				{
					LOG.error("error in retreiving the cmir!");
					throw e;
				}

				if (null != coversionFactor)
				{
					BigDecimal lineItemToVolume_20FT = new BigDecimal(0);
					BigDecimal lineItemToVolume_40FT = new BigDecimal(0);
					BigDecimal unitVolumeDisable20FT = new BigDecimal(0);
					BigDecimal unitVolumeDisable40FT = new BigDecimal(0);
					String volumeUnit = "";
					BigDecimal unitVolume = new BigDecimal(0);

					if (cmirUom.equalsIgnoreCase("LAY"))
					{
						volumeUnit = getLayerVolumeUnit(erpMaterialId);
						unitVolume = new BigDecimal(getLayerVolume(erpMaterialId));
					}
					else if (!cmirUom.equalsIgnoreCase("LAY") && null != coversionFactor.getPackageVolume())
					{
						volumeUnit = coversionFactor.getPackageVolume().getMeasuringUnits();
						unitVolume = new BigDecimal(coversionFactor.getPackageVolume().getMeasurement());
					}

					LOG.info("-------" + erpMaterialId + "    : " + coversionFactor.getAlternateUOM() + "--------------");

					/*
					 * Comparing the Pallet volume with the container volume(2.5% of container volume for 40 FT container and 5% of
					 * container volume for 20 FT container)
					 */

					if (unitVolume.compareTo(ZERO) != 0 && coversionFactor.getAlternateUOM().equals("PAL"))
					{

						LOG.info("% volume of product if placed inside 20FT container :"
								+ (unitVolume.doubleValue() * 100) / (twentyFeetContainerVolumInCCM.doubleValue()));
						if ((unitVolume.doubleValue() * 100) / (twentyFeetContainerVolumInCCM.doubleValue()) < 5)
						{
							unitVolumeDisable20FT = twentyFeetContainerVolumInCCM.multiply(new BigDecimal(0.05));

							LOG.info(enerGizerProductModel.getCode() + " " + coversionFactor.getAlternateUOM()
									+ " have volume less than 5 % when container is 20 FT");
							LOG.info("Volume in 20 FT :  " + unitVolumeDisable20FT);
						}
						else
						{
							unitVolumeDisable20FT = unitVolume;
							LOG.info(enerGizerProductModel.getCode() + " " + coversionFactor.getAlternateUOM()
									+ " Volume is greater than 5% for twenty feet");
							LOG.info("Volume in 20 FT :  " + unitVolumeDisable20FT);
						}

						LOG.info("% volume of product if placed inside 40FT container :"
								+ (unitVolume.doubleValue() * 100) / (fourtyFeetContainerVolumeInCCM.doubleValue()));
						if ((unitVolume.doubleValue() * 100) / (fourtyFeetContainerVolumeInCCM.doubleValue()) < 2.5)
						{
							unitVolumeDisable40FT = fourtyFeetContainerVolumeInCCM.multiply(new BigDecimal(0.025));
							LOG.info(enerGizerProductModel.getCode() + "" + coversionFactor.getAlternateUOM()
									+ " have volume less than 2.5% when container is 40 FT");
							LOG.info("Volume in 40 FT :  " + unitVolumeDisable40FT);
						}

						else
						{
							unitVolumeDisable40FT = unitVolume;
							LOG.info(enerGizerProductModel.getCode() + "" + coversionFactor.getAlternateUOM()
									+ " Volume is greater than 2.5% for forty feet");
							LOG.info("Volume in 40 FT :  " + unitVolumeDisable40FT);
						}

						lineItemToVolume_20FT = unitVolumeDisable20FT.multiply(itemQuantity);
						lineItemToVolume_40FT = unitVolumeDisable40FT.multiply(itemQuantity);
					}
					else if (unitVolume.compareTo(ZERO) != 0)
					{
						LOG.info("Volume of nonPallet Product:   " + unitVolume + volumeUnit);
						lineItemToVolume_20FT = unitVolume.multiply(itemQuantity);
						lineItemToVolume_40FT = unitVolume.multiply(itemQuantity);
					}
					totalVolumeDisable20FT = totalVolumeDisable20FT
							.add(EnergizerWeightOrVolumeConverter.getConversionValue(volumeUnit, lineItemToVolume_20FT));
					totalVolumeDisable40FT = totalVolumeDisable40FT
							.add(EnergizerWeightOrVolumeConverter.getConversionValue(volumeUnit, lineItemToVolume_40FT));
				}
				if (null != coversionFactor)
				{
					BigDecimal lineItemTotWt = new BigDecimal(0);
					BigDecimal unitWeight = new BigDecimal(0);
					String weightUnit = "";//coversionFactor.getPackageWeight().getMeasuringUnits();
					if (cmirUom.equalsIgnoreCase("LAY"))
					{
						weightUnit = getLayerWeightUnit(erpMaterialId);
						unitWeight = new BigDecimal(getLayerWeight(erpMaterialId));
					}
					else if (!cmirUom.equalsIgnoreCase("LAY") && null != coversionFactor.getPackageWeight())
					{
						weightUnit = coversionFactor.getPackageWeight().getMeasuringUnits();
						unitWeight = new BigDecimal(coversionFactor.getPackageWeight().getMeasurement());
					}

					if (unitWeight.compareTo(ZERO) != 0)
					{
						lineItemTotWt = unitWeight.multiply(itemQuantity);
					}

					totalCartWt = totalCartWt.add(EnergizerWeightOrVolumeConverter.getConversionValue(weightUnit, lineItemTotWt));
					LOG.info("Weight : " + lineItemTotWt);
				}
				LOG.info("==============================================");

			}
			totalVolumeDisable20FT = totalVolumeDisable20FT.setScale(6, BigDecimal.ROUND_HALF_EVEN);
			totalVolumeDisable40FT = totalVolumeDisable40FT.setScale(6, BigDecimal.ROUND_HALF_EVEN);

			LOG.info("container Volume : 20 FT: " + disableTwentyFeetContainerVolume);
			LOG.info("|| totalCartWt => " + totalCartWt + " || totalCartVolume or 20FT: " + totalVolumeDisable20FT
					+ " || totalCartVolume or 40FT: " + totalVolumeDisable40FT);

			ContainerData containerData = null;

			if (totalVolumeDisable20FT.compareTo(disableTwentyFeetContainerVolume) == 1)
			{
				containerData = getPercentageContainerUtil(totalCartWt, totalVolumeDisable40FT);
			}
			else
			{
				containerData = getPercentageContainerUtil(totalCartWt, totalVolumeDisable20FT);
			}
			cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
			cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());

			cartData.setContainerHeight(containerData.getContainerType());

			if (((cartData.getTotalProductVolumeInPercent() > 100) || (cartData.getTotalProductWeightInPercent() > 100))
					&& cartData.getContainerHeight().equalsIgnoreCase(Config.getParameter(TWENTY_FEET_CONTAINER)))
			{
				cartData.setIsContainerFull(false);
			}
			else if (((cartData.getTotalProductVolumeInPercent() > 100) || (cartData.getTotalProductWeightInPercent() > 100))
					&& cartData.getContainerHeight().equalsIgnoreCase(Config.getParameter(FORTY_FEET_CONTAINER)))
			{
				cartData.setIsContainerFull(true);

				/***** Added by Soma for blocking the cart & disabling the checkout button ******/
				//cartData.setIsFloorSpaceFull(true);
				/***** Added by Soma for blocking the cart & disabling the checkout button ******/
			}
			else
			{
				cartData.setIsContainerFull(false);
			}
			cartData.setAvailableVolume((Math.round((100 - cartData.getTotalProductVolumeInPercent()) * 100.0) / 100.0));
			cartData.setAvailableWeight((Math.round((100 - cartData.getTotalProductWeightInPercent()) * 100.0) / 100.0));

			LOG.info("Available volume: " + cartData.getAvailableVolume() + " Available Weight: " + cartData.getAvailableWeight());
		}
		catch (final Exception exception)
		{
			LOG.error("Error in calCartContainerUtilization", exception);
			throw exception;
		}
		return cartData;
	}


	/**
	 * // 2 Slip Sheets
	 *
	 * @param cartData
	 * @param containerHeight
	 * @return
	 * @throws Exception
	 */
	public CartData calCartContainerUtilizationWithEnableSlipsheets(final CartData cartData, final String containerHeight)
			throws Exception
	{
		try
		{
			final CartModel cartModel = cartService.getSessionCart();
			final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();
			final int totalPalletCountForSlipSheet = configurationService.getConfiguration()
					.getInt(TOTAL_PALLET_COUNT_FOR_2SLIPSHEET);
			productsListA = new ArrayList<EnergizerProductPalletHeight>();
			//int palletCount = 0;
			long numberOfEachInCases = 0;
			long numberOfEachInPallet = 0;
			long numberOfcasesPerPallet = 0;
			BigDecimal totalCartVolume = new BigDecimal(0);
			BigDecimal casesVolume = new BigDecimal(0);
			BigDecimal totalCartWt = new BigDecimal(0);
			BigDecimal calculatedUnitVolume = new BigDecimal(0);
			BigDecimal casesWeight = new BigDecimal(0);
			BigDecimal calculatedUnitWeight = new BigDecimal(0);


			message = new ArrayList<String>();

			productsListA = getPalletsCountOfProductsInCart(cartData, productsListA);
			palletCount = productsListA.size();

			/**
			 * # Container volume in M3 and weight in KG ##########################################
			 *
			 * twenty.feet.container.volume=30.44056 twenty.feet.container.weight=15961.90248 fourty.feet.container.volume=70.62209
			 * fourty.feet.container.weight=18234.3948
			 */

			LOG.info("==========For enable 2 Slip sheet ==========");

			//	LOG.info("|| totalCartWt => " + totalCartWt + " || totalCartVolume => " + totalCartVolume);
			//	ContainerData containerData = null;

			//	containerData = getPercentage(totalCartWt, totalCartVolume, containerHeight);
			//	cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
			//	cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());

			//	cartData.setContainerHeight(containerData.getContainerType());

			//for (final EnergizerProductPalletHeight product : productsListA)
			for (final Iterator iterator = productsListA.iterator(); iterator.hasNext();)
			{
				final EnergizerProductPalletHeight fullPallet = (EnergizerProductPalletHeight) iterator.next();
				final String erpMaterialId = fullPallet.getErpMaterialId();
				EnergizerProductConversionFactorModel coversionFactor = null;
				String cmirUom = null;
				String casesVolumeUnit = null;
				String casesWeightUnit = null;
				try
				{
					final String userId = userService.getCurrentUser().getUid();
					final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
					final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
							b2bUnit.getUid());
					cmirUom = energizerCMIRModel.getUom();
					final List<EnergizerProductConversionFactorModel> allEnergizerProductConversionFactorModel = energizerProductService
							.getAllEnergizerProductConversion(erpMaterialId);
					for (final EnergizerProductConversionFactorModel productConversioFactor : allEnergizerProductConversionFactorModel)
					{
						if (productConversioFactor.getAlternateUOM().equalsIgnoreCase("CS"))
						{
							casesVolume = new BigDecimal(productConversioFactor.getPackageVolume().getMeasurement());
							casesWeight = new BigDecimal(productConversioFactor.getPackageWeight().getMeasurement());
							numberOfEachInCases = productConversioFactor.getConversionMultiplier();
							casesVolumeUnit = productConversioFactor.getPackageVolume().getMeasuringUnits();
							casesWeightUnit = productConversioFactor.getPackageWeight().getMeasuringUnits();
						}
						if (productConversioFactor.getAlternateUOM().equalsIgnoreCase("PAL"))
						{
							numberOfEachInPallet = productConversioFactor.getConversionMultiplier();
						}
					}
					final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
							.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());

					final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();
					if (null != alternateUOM && null != cmirUom && alternateUOM.equalsIgnoreCase(cmirUom))
					{
						coversionFactor = energizerProductConversionFactorModel;
					}

					//  ************ setting the OrderBlock  flag  ****************
					if (b2bUnit.getOrderBlock())
					{
						cartData.setIsOrderBlocked(true);
					}
					else
					{
						cartData.setIsOrderBlocked(false);
					}
				}
				catch (final Exception e)
				{
					LOG.error("error in retreiving the cmir!");
					throw e;
				}
				if (null != coversionFactor && null != casesVolume)
				{
					LOG.info(erpMaterialId + "    " + coversionFactor.getAlternateUOM());
					numberOfcasesPerPallet = numberOfEachInPallet / numberOfEachInCases;
					LOG.info("Number of cases per pallet: " + numberOfcasesPerPallet);
					calculatedUnitVolume = casesVolume.multiply(new BigDecimal(numberOfcasesPerPallet));
					LOG.info("Product volume on basis of cases volume: " + calculatedUnitVolume);


					final String volumeUnit = casesVolumeUnit;
					totalCartVolume = totalCartVolume
							.add(EnergizerWeightOrVolumeConverter.getConversionValue(volumeUnit, calculatedUnitVolume));
				}
				if (null != coversionFactor && null != casesWeight)
				{
					numberOfcasesPerPallet = numberOfEachInPallet / numberOfEachInCases;
					LOG.info("Number of cases per pallet: " + numberOfcasesPerPallet);
					calculatedUnitWeight = casesWeight.multiply(new BigDecimal(numberOfcasesPerPallet));
					LOG.info("Product Weight on basis of cases Weight: " + calculatedUnitWeight);

					final String weightUnit = casesWeightUnit;
					totalCartWt = totalCartWt
							.add(EnergizerWeightOrVolumeConverter.getConversionValue(weightUnit, calculatedUnitWeight));
				}
				LOG.info("-----------------------------------------------------------------");


			}

			LOG.info("|| totalCartWt => " + totalCartWt + " || totalCartVolume => " + totalCartVolume);
			ContainerData containerData = null;

			containerData = getPercentage(totalCartWt, totalCartVolume, containerHeight);
			cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
			cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());

			cartData.setContainerHeight(containerData.getContainerType());

			LOG.info("palletCount :: " + palletCount + " , totalPalletCountForSlipSheet :: " + totalPalletCountForSlipSheet);

			if (nonPalletProductsList.size() > 0)
			{
				LOG.info("select 2 wooden base as packing option");
				message.add(
						"Dear Customer, Your order contains some products whose quantity is less than a full pallet (partial pallet). Partial pallets can be shipped only with 2 wooden base packing material. Please change your packing material or remove the partial pallet products.");
				cartData.setErrorMessage(true);
			}
			else if ((cartData.getTotalProductVolumeInPercent() > 100) || (cartData.getTotalProductWeightInPercent() > 100))
			{
				cartData.setIsContainerFull(true);
				LOG.info("Container volume/weight is FULL !!");
			}
			else if (palletCount > totalPalletCountForSlipSheet)
			{
				cartData.setIsFloorSpaceFull(true);
				LOG.info("Floor space is FULL !!");
			}
			else
			{
				cartData.setIsContainerFull(false);
			}
			cartData.setAvailableVolume((Math.round((100 - cartData.getTotalProductVolumeInPercent()) * 100.0) / 100.0));
			cartData.setAvailableWeight((Math.round((100 - cartData.getTotalProductWeightInPercent()) * 100.0) / 100.0));

			cartData.setTotalPalletCount(palletCount);
			cartData.setVirtualPalletCount(virtualPallet);
			cartData.setPartialPalletCount(partialPallet);

			LOG.info("Available volume: " + cartData.getAvailableVolume() + ", Available Weight: " + cartData.getAvailableWeight());

		}
		catch (final Exception exception)
		{
			LOG.error("Error in calCartContainerUtilization", exception);
			throw exception;
		}
		return cartData;
	}

	/**
	 *
	 * @param totalCartWt
	 * @param totalCartVolume
	 * @return
	 */
	private ContainerData getPercentageContainerUtil(BigDecimal totalCartWt, BigDecimal totalCartVolume) throws Exception
	{

		totalCartWt = totalCartWt.setScale(2, BigDecimal.ROUND_UP);
		totalCartVolume = totalCartVolume.setScale(6, BigDecimal.ROUND_UP);

		final ContainerData containerData = new ContainerData();
		try
		{
			if (totalCartWt.compareTo(twentyFeetContainerWeight) == -1
					&& (totalCartVolume.compareTo(disableTwentyFeetContainerVolume) == -1
							|| totalCartVolume.compareTo(disableTwentyFeetContainerVolume) == 0))
			{
				containerData.setContainerType("20FT");
				final double volumePercentage = (totalCartVolume.multiply(hundred))
						.divide(disableTwentyFeetContainerVolume, 2, RoundingMode.HALF_EVEN).doubleValue();
				final double weightPercentage = (totalCartWt.multiply(hundred))
						.divide(twentyFeetContainerWeight, 2, RoundingMode.HALF_EVEN).doubleValue();
				LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
				containerData.setPercentVolumeUses(volumePercentage);
				containerData.setPercentWeightUses(weightPercentage);
			}
			else
			{
				containerData.setContainerType("40FT");
				final double volumePercentage = (totalCartVolume.multiply(hundred))
						.divide(disableFourtyFeetContainerVolume, 2, RoundingMode.HALF_EVEN).doubleValue();
				final double weightPercentage = (totalCartWt.multiply(hundred))
						.divide(fourtyFeetContainerWeight, 2, RoundingMode.HALF_EVEN).doubleValue();
				LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
				containerData.setPercentVolumeUses(volumePercentage);
				containerData.setPercentWeightUses(weightPercentage);
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in getting Container Utilisation Percentage:::" + e);
			throw e;
		}
		return containerData;
	}

	private ContainerData getPercentage(BigDecimal totalCartWt, BigDecimal totalCartVolume, final String containerHeight)
			throws Exception
	{

		totalCartWt = totalCartWt.setScale(2, BigDecimal.ROUND_UP);
		totalCartVolume = totalCartVolume.setScale(6, BigDecimal.ROUND_UP);

		final ContainerData containerData = new ContainerData();
		try
		{
			if (containerHeight.equals("20FT"))
			{
				containerData.setContainerType("20FT");
				final double volumePercentage = (totalCartVolume.multiply(hundred))
						.divide(twentyFeetContainerVolume, 2, RoundingMode.HALF_EVEN).doubleValue();
				final double weightPercentage = (totalCartWt.multiply(hundred))
						.divide(twentyFeetContainerWeight, 2, RoundingMode.HALF_EVEN).doubleValue();
				LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
				containerData.setPercentVolumeUses(volumePercentage);
				containerData.setPercentWeightUses(weightPercentage);
			}
			else
			{
				containerData.setContainerType("40FT");
				final double volumePercentage = (totalCartVolume.multiply(hundred))
						.divide(fourtyFeetContainerVolume, 2, RoundingMode.HALF_EVEN).doubleValue();
				final double weightPercentage = (totalCartWt.multiply(hundred))
						.divide(fourtyFeetContainerWeight, 2, RoundingMode.HALF_EVEN).doubleValue();
				LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
				containerData.setPercentVolumeUses(volumePercentage);
				containerData.setPercentWeightUses(weightPercentage);
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception Occured in getting Container Percentage:::" + e);
			throw e;
		}
		return containerData;
	}

	private ContainerData getPercentageForTruck(BigDecimal totalCartWt, BigDecimal totalCartVolume, final String containerType)
	{


		totalCartWt = totalCartWt.setScale(2, BigDecimal.ROUND_UP);
		totalCartVolume = totalCartVolume.setScale(6, BigDecimal.ROUND_UP);

		final ContainerData containerData = new ContainerData();

		containerData.setContainerType(containerType);

		final double volumePercentage = (totalCartVolume.multiply(hundred))
				.divide(configurationService.getConfiguration().getBigDecimal("truck.volume"), 2, RoundingMode.HALF_EVEN)
				.doubleValue();
		final double weightPercentage = (totalCartWt.multiply(hundred))
				.divide(configurationService.getConfiguration().getBigDecimal("truck.max.weight"), 2, RoundingMode.HALF_EVEN)
				.doubleValue();
		//LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);

		containerData.setPercentVolumeUses(volumePercentage);
		containerData.setPercentWeightUses(weightPercentage);

		return containerData;
	}

	public ContainerData getTotalProductVolumePercentageOccupiedForEMEA(final CartModel cartModel, final String containerType,
			final String targetUOM, final Integer maxPalletsAllowedInCart, final double targetUOMMaxHeight, final String uomLAY,
			final double heightOfWoodenBase, final String freightType, final String palletType)
	{

		final ContainerData containerData = new ContainerData();

		containerData.setContainerType(containerType);

		BigDecimal totalOccupiedPalletsCount = new BigDecimal(0);
		BigDecimal totalFullPalletsCount = new BigDecimal(0);
		BigDecimal totalPartialPalletsCount = new BigDecimal(0);
		BigDecimal totalOccupiedPartialPalletsHeightInMetres = new BigDecimal(0);

		final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

		for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
		{
			final EnergizerProductModel energizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
			final String erpMaterialId = energizerProductModel.getCode();
			final BigDecimal itemQuantity = new BigDecimal((abstractOrderEntryModel.getQuantity().longValue()));
			EnergizerProductConversionFactorModel conversionFactorForCmirUOM = null;
			EnergizerProductConversionFactorModel conversionFactorForTargetUOM = null;
			EnergizerProductConversionFactorModel conversionFactorForUOMLAY = null;
			BigDecimal itemQuantityInTargetUOM = new BigDecimal(0);
			String cmirUom = null;

			try
			{
				LOG.info("************ Calculating pallets count for this material Id == " + erpMaterialId + " - START ************");

				final String userId = userService.getCurrentUser().getUid();
				final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
				final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
						b2bUnit.getUid());
				cmirUom = energizerCMIRModel.getUom();
				final List<EnergizerProductConversionFactorModel> conversionFactorModels = energizerProductService
						.getAllEnergizerProductConversionForMaterialIdAndB2BUnit(erpMaterialId, b2bUnit.getUid());

				if (!conversionFactorModels.isEmpty())
				{
					for (final Iterator iterator = conversionFactorModels.iterator(); iterator.hasNext();)
					{
						final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = (EnergizerProductConversionFactorModel) iterator
								.next();

						final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();

						if (null != alternateUOM && null != cmirUom && alternateUOM.equalsIgnoreCase(cmirUom))
						{
							conversionFactorForCmirUOM = energizerProductConversionFactorModel;
						}
						if (null != alternateUOM && null != targetUOM && alternateUOM.equalsIgnoreCase(targetUOM))
						{
							conversionFactorForTargetUOM = energizerProductConversionFactorModel;
						}

						if (null != alternateUOM && null != uomLAY && alternateUOM.equalsIgnoreCase(uomLAY))
						{
							conversionFactorForUOMLAY = energizerProductConversionFactorModel;
						}
					}
				}

				final BigDecimal conversionMultiplierForCmirUOM = new BigDecimal(
						getAlernateConversionMultiplierForUOM(conversionFactorModels, cmirUom).intValue());

				final BigDecimal conversionMultiplierForTargetUOM = new BigDecimal(
						getAlernateConversionMultiplierForUOM(conversionFactorModels, targetUOM).intValue());

				final BigDecimal conversionMultiplierForUOMLAY = new BigDecimal(
						getAlernateConversionMultiplierForUOM(conversionFactorModels, uomLAY).intValue());

				LOG.info("Total number of " + cmirUom + "s == " + itemQuantity);
				LOG.info("Conversion Multiplier For " + cmirUom + " == " + conversionMultiplierForCmirUOM);
				LOG.info("Conversion Multiplier For " + targetUOM + " == " + conversionMultiplierForTargetUOM);
				LOG.info("Conversion Multiplier For " + uomLAY + " == " + conversionMultiplierForUOMLAY);

				final BigDecimal noOfCUsPerPallet = conversionMultiplierForTargetUOM.divide(conversionMultiplierForCmirUOM,
						RoundingMode.HALF_EVEN);

				final BigDecimal noOfCUsPerLayer = conversionMultiplierForUOMLAY.divide(conversionMultiplierForCmirUOM,
						RoundingMode.HALF_EVEN);

				final BigDecimal noOfLayersPerPallet = conversionMultiplierForTargetUOM.divide(conversionMultiplierForUOMLAY,
						RoundingMode.HALF_EVEN);


				LOG.info("No. of. CUs/Pallet == " + noOfCUsPerPallet);
				LOG.info("No. of. CUs/Layer == " + noOfCUsPerLayer);
				LOG.info("No. of. Layers/Pallet == " + noOfLayersPerPallet);

				itemQuantityInTargetUOM = itemQuantity.multiply(conversionMultiplierForCmirUOM)
						.divide(conversionMultiplierForTargetUOM, 2, RoundingMode.HALF_EVEN);

				final double itemQuantityForCalculation = itemQuantityInTargetUOM.doubleValue();

				LOG.info("Item Quantity In " + targetUOM + " before segregation into full & partial == "
						+ df2.format(itemQuantityForCalculation));

				int fullPallets = 0;

				if (itemQuantityForCalculation % 1 == 0)
				{
					LOG.info(itemQuantityForCalculation
							+ " is a real number. So it is a full pallet, no partial pallets for this product ");
					totalFullPalletsCount = totalFullPalletsCount.add(itemQuantityInTargetUOM);
				}
				else
				{
					if (itemQuantityForCalculation > 1)
					{
						LOG.info(itemQuantityForCalculation
								+ " is not a real number. So it is a mix of full pallet and partial pallet for this product ");

						fullPallets = (int) itemQuantityForCalculation;
						LOG.info("Full Pallets == " + fullPallets);
					}

					if (fullPallets > 0)
					{
						totalFullPalletsCount = totalFullPalletsCount.add(new BigDecimal(fullPallets));
						LOG.info("Added " + fullPallets + " fullPallets to the count for this materialId - " + erpMaterialId);
					}

					final BigDecimal remainingCUs = itemQuantity.subtract(new BigDecimal(fullPallets).multiply(noOfCUsPerPallet));

					LOG.info("Remaining CUs == " + remainingCUs);

					double noOfLayers = (remainingCUs.doubleValue()) / (noOfCUsPerLayer.doubleValue());

					LOG.info("No. Of Layers before rounding off to next integer == " + noOfLayers);

					if (noOfLayers % 1 == 0)
					{
						LOG.info("No. of layers is a real number.");
					}
					else
					{
						noOfLayers = (int) noOfLayers + 1;
						LOG.info(
								"No. of layers is not a real number. So rounding it up to the next integer. NoOfLayers == " + noOfLayers);
					}

					final double heightOfALayer = conversionFactorForUOMLAY.getPackageHeight().getMeasurement();

					LOG.info("Height of a layer == " + heightOfALayer + " cm");

					final double totalHeightOfLayersInCMExcludingWoodenBase = noOfLayers * heightOfALayer;

					LOG.info("Total Height Of Layers In CM Excluding Wooden Base( noOfLayers * heightOfALayer) == "
							+ totalHeightOfLayersInCMExcludingWoodenBase);

					LOG.info("Adding wooden base height " + heightOfWoodenBase + " cm to the layers height");

					final double totalHeightOfLayersInCMIncludingWoodenBase = totalHeightOfLayersInCMExcludingWoodenBase
							+ heightOfWoodenBase;

					LOG.info("Total Height Of Layers In CM Including Wooden Base == " + totalHeightOfLayersInCMIncludingWoodenBase);

					final double totalHeightOfLayersInMetresIncludingWoodenBase = totalHeightOfLayersInCMIncludingWoodenBase / 100;

					LOG.info("Total Height Of Layers In Metres Including Wooden Base == "
							+ new BigDecimal(totalHeightOfLayersInMetresIncludingWoodenBase).setScale(2, RoundingMode.HALF_EVEN));

					totalOccupiedPartialPalletsHeightInMetres = totalOccupiedPartialPalletsHeightInMetres
							.add(new BigDecimal(totalHeightOfLayersInMetresIncludingWoodenBase));
				}

				LOG.info("************ Calculating pallets count for this material Id == " + erpMaterialId + " - END ************");

			}
			catch (final Exception e)
			{
				LOG.error("error in retreiving the cmir for this material Id ! " + erpMaterialId);
			}
		}

		LOG.info("Total number of Full Pallets occupied for this cart " + cartModel.getCode() + " is == " + totalFullPalletsCount);

		LOG.info("Total Occupied Partial Pallets Height In Metres == "
				+ totalOccupiedPartialPalletsHeightInMetres.setScale(2, RoundingMode.HALF_EVEN));

		LOG.info("Maximum height of a pallet in case of a partial pallet in metres == " + targetUOMMaxHeight);

		totalPartialPalletsCount = totalOccupiedPartialPalletsHeightInMetres.divide(new BigDecimal(targetUOMMaxHeight), 2,
				RoundingMode.HALF_EVEN);

		LOG.info("Total number of Partial Pallets(" + totalOccupiedPartialPalletsHeightInMetres.setScale(2, RoundingMode.HALF_EVEN)
				+ " / " + targetUOMMaxHeight + ") occupied for this cart " + cartModel.getCode() + " is == "
				+ totalPartialPalletsCount);

		totalOccupiedPalletsCount = totalFullPalletsCount.add(totalPartialPalletsCount);

		LOG.info("The total number of pallets(Full + Partial) occupied for this cart " + cartModel.getCode() + " is == "
				+ totalOccupiedPalletsCount);

		//LOG.info("Maximum number of Pallets Allowed In Cart For Truck == " + maxPalletsAllowedInCart);

		if (null != totalOccupiedPalletsCount)
		{
			if (totalOccupiedPalletsCount.intValue() > maxPalletsAllowedInCart)
			{
				LOG.info("The cart exceeded the permissible limit of " + maxPalletsAllowedInCart
						+ " pallets per cart. Currently, there are " + totalOccupiedPalletsCount
						+ " equivalent number of pallets in cart");

			}
			else
			{
				LOG.info("The total number of pallets remaining(" + maxPalletsAllowedInCart + " - " + totalOccupiedPalletsCount
						+ ") for this cart " + cartModel.getCode() + " is == "
						+ (BigDecimal.valueOf(maxPalletsAllowedInCart).subtract(totalOccupiedPalletsCount)));
			}
		}

		final double volumePercentageOccupiedForTruck = (totalOccupiedPalletsCount.multiply(hundred))
				.divide(BigDecimal.valueOf(maxPalletsAllowedInCart), 2, RoundingMode.HALF_EVEN).doubleValue();

		LOG.info("|| Truck volumePercentage occupied by products in EMEA((" + totalOccupiedPalletsCount + " * 100) / "
				+ maxPalletsAllowedInCart + ") for this cart " + cartModel.getCode() + " is ==> " + volumePercentageOccupiedForTruck);

		containerData.setPercentVolumeUses(volumePercentageOccupiedForTruck);


		// Added for Container Optimization for EMEA - START
		//totalOccupiedPalletsCount = totalFullPalletsCount.add(totalPartialPalletsCount);
		if (freightType.equalsIgnoreCase(FREIGHT_TRUCK))
		{
			LOG.info("Calculating floorSpaceProductMap & nonPalletFloorSpaceProductMap for '" + freightType + "'");
			int noOfRows = 0;
			if (palletType.equalsIgnoreCase("US"))
			{
				noOfRows = 2;
			}
			else if (palletType.equalsIgnoreCase("EU"))
			{
				noOfRows = 3;
			}
			calculateFloorSpaceMapForEMEA(totalFullPalletsCount, totalPartialPalletsCount, noOfRows);
		}
		else if (freightType.equalsIgnoreCase(FREIGHT_CONTAINER))
		{
			LOG.info("Calculating floorSpaceProductMap & nonPalletFloorSpaceProductMap for '" + freightType + "'");
			final int noOfRows = 1;
			calculateFloorSpaceMapForEMEA(totalFullPalletsCount, totalPartialPalletsCount, noOfRows);
		}

		// Added for Container Optimization for EMEA - END
		return containerData;
	}

	public BigDecimal getVolumeOfProductsInCart(final CartData cartData, final String str) throws Exception
	{
		try
		{
			BigDecimal totalCartVolume = new BigDecimal(0);
			BigDecimal VolumeOfNonPalletProduct = new BigDecimal(0);
			final CartModel cartModel = cartService.getSessionCart();
			final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

			for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
			{
				final EnergizerProductModel energizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
				final String erpMaterialId = energizerProductModel.getCode();
				final BigDecimal itemQuantity = new BigDecimal((abstractOrderEntryModel.getQuantity().longValue()));
				EnergizerProductConversionFactorModel coversionFactor = null;
				String cmirUom = null;
				try
				{
					final String userId = userService.getCurrentUser().getUid();
					final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
					final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
							b2bUnit.getUid());
					cmirUom = energizerCMIRModel.getUom();
					final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
							.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());
					final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();
					if (null != alternateUOM && null != cmirUom && alternateUOM.equalsIgnoreCase(cmirUom))
					{
						coversionFactor = energizerProductConversionFactorModel;
					}
				}
				catch (final Exception e)
				{
					LOG.error("error in retreiving the cmir!");
					throw e;
				}
				BigDecimal lineItemTotvolume = new BigDecimal(0);
				BigDecimal unitVolume = new BigDecimal(0);
				String volumeUnit = null;
				if (cmirUom.equalsIgnoreCase("LAY"))
				{
					unitVolume = new BigDecimal(getLayerVolume(erpMaterialId));
					volumeUnit = getLayerVolumeUnit(erpMaterialId);
				}
				else
				{
					unitVolume = new BigDecimal(coversionFactor.getPackageVolume().getMeasurement());
					volumeUnit = coversionFactor.getPackageVolume().getMeasuringUnits();
				}
				if (unitVolume.compareTo(ZERO) != 0)
				{
					lineItemTotvolume = unitVolume.multiply(itemQuantity);
				}
				if (str.equals("VOLUMEOFALLPRODUCTS"))
				{
					totalCartVolume = totalCartVolume
							.add(EnergizerWeightOrVolumeConverter.getConversionValue(volumeUnit, lineItemTotvolume));
				}
				if (str.equals("VOLUMEOFNONPALLETPRODUCTS") && !(coversionFactor.getAlternateUOM().equals("PAL")))
				{
					VolumeOfNonPalletProduct = VolumeOfNonPalletProduct
							.add(EnergizerWeightOrVolumeConverter.getConversionValue(volumeUnit, lineItemTotvolume));
				}
			}
			if (str.equals("VOLUMEOFALLPRODUCTS"))
			{
				return totalCartVolume;
			}
			if (str.equals("VOLUMEOFNONPALLETPRODUCTS"))
			{
				return VolumeOfNonPalletProduct;
			}
			else
			{
				return new BigDecimal(0);
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while calculating volumeOfAllProducts/volumnOfNonPalletProduct in cart:::");
			throw e;
		}
	}

	public BigDecimal getWeightOfProductsInCart(final CartData cartData, final String str) throws Exception
	{
		try
		{
			BigDecimal totalCartWeight = new BigDecimal(0);
			BigDecimal weightOfNonPalletProduct = new BigDecimal(0);
			final CartModel cartModel = cartService.getSessionCart();
			final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

			for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
			{
				final EnergizerProductModel energizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
				final String erpMaterialId = energizerProductModel.getCode();
				final BigDecimal itemQuantity = new BigDecimal((abstractOrderEntryModel.getQuantity().longValue()));
				EnergizerProductConversionFactorModel coversionFactor = null;
				String cmirUom = null;
				try
				{
					final String userId = userService.getCurrentUser().getUid();
					final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
					final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
							b2bUnit.getUid());
					cmirUom = energizerCMIRModel.getUom();
					final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
							.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());
					final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();
					LOG.info("erpMaterialId : " + erpMaterialId + " , b2bUnitId : " + b2bUnit.getUid() + " , alternateUOM : "
							+ alternateUOM + " , cmirUom : " + cmirUom);
					if (null != alternateUOM && null != cmirUom && alternateUOM.equalsIgnoreCase(cmirUom))
					{
						coversionFactor = energizerProductConversionFactorModel;
					}
				}
				catch (final Exception e)
				{
					LOG.error("error in retreiving the cmir!");
					throw e;
				}
				BigDecimal lineItemTotWeight = new BigDecimal(0);
				BigDecimal unitWeight = new BigDecimal(0);
				String weightUnit = null;
				if (cmirUom.equalsIgnoreCase("LAY"))
				{
					unitWeight = new BigDecimal(getLayerWeight(erpMaterialId));
					weightUnit = getLayerWeightUnit(erpMaterialId);
				}
				else
				{
					if (null != coversionFactor)
					{
						if (null != coversionFactor.getPackageWeight())
						{
							unitWeight = new BigDecimal(coversionFactor.getPackageWeight().getMeasurement());
						}
						else
						{
							LOG.info("package weight is null for this product : " + coversionFactor.getErpMaterialId() + " , UoM : "
									+ coversionFactor.getAlternateUOM());
						}
					}
					else
					{
						LOG.info("coversionFactor is null for this product : " + erpMaterialId);
					}
					weightUnit = coversionFactor.getPackageWeight().getMeasuringUnits();
				}

				if (unitWeight.compareTo(ZERO) != 0)
				{
					lineItemTotWeight = unitWeight.multiply(itemQuantity);
				}

				if (str.equals("WEIGHTOFALLPRODUCTS"))
				{
					totalCartWeight = totalCartWeight
							.add(EnergizerWeightOrVolumeConverter.getConversionValue(weightUnit, lineItemTotWeight));
				}
				if (str.equals("WEIGHTOFNONPALLETPRODUCTS") && !(coversionFactor.getAlternateUOM().equals("PAL")))
				{
					weightOfNonPalletProduct = weightOfNonPalletProduct
							.add(EnergizerWeightOrVolumeConverter.getConversionValue(weightUnit, lineItemTotWeight));
				}
			}
			if (str.equals("WEIGHTOFALLPRODUCTS"))
			{
				return totalCartWeight.setScale(2, RoundingMode.HALF_EVEN);
			}
			if (str.equals("WEIGHTOFNONPALLETPRODUCTS"))
			{
				return weightOfNonPalletProduct.setScale(2, RoundingMode.HALF_EVEN);
			}
			else
			{
				return new BigDecimal(0).setScale(2, RoundingMode.HALF_EVEN);
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while calculating total weightOfTheProducts/weightOfNonPalletProduct in cart ...");
			throw e;
		}
	}

	public long noOfCases(final String erpMaterialId)
	{

		final List<EnergizerProductConversionFactorModel> energizerProductConversionFactorModel = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		long multiplierForCase = 0;
		long multiplierForPAL = 0;
		for (final EnergizerProductConversionFactorModel UOM : energizerProductConversionFactorModel)
		{
			if (UOM.getAlternateUOM().equals("PAL"))
			{
				multiplierForPAL = UOM.getConversionMultiplier();
				continue;
			}
			if (UOM.getAlternateUOM().equals("CASE"))
			{
				multiplierForCase = UOM.getConversionMultiplier();
				continue;
			}
		}
		return (multiplierForPAL / multiplierForCase);
	}

	public double getVolumeOfHighestPallet(final String erpMaterialId, final String convertedUOM)
	{
		try
		{
			final List<EnergizerProductConversionFactorModel> energizerProductConversionFactorModel = energizerProductService
					.getAllEnergizerProductConversion(erpMaterialId);
			final String measuringUnit;
			final double volume;
			for (final EnergizerProductConversionFactorModel UOM : energizerProductConversionFactorModel)
			{
				if (UOM.getAlternateUOM().equals(convertedUOM))
				{
					measuringUnit = UOM.getPackageVolume().getMeasuringUnits();
					volume = UOM.getPackageVolume().getMeasurement();
					return EnergizerWeightOrVolumeConverter.getConversionValue(measuringUnit, new BigDecimal(volume)).doubleValue();
				}
			}
			return 0;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occures while update product volumeOfHeight in cart:::");
			throw e;
		}
	}

	public double getWeightOfGivenMaterial(final String erpMaterialId, final String convertedUOM)
	{
		try
		{
			final List<EnergizerProductConversionFactorModel> energizerProductConversionFactorModel = energizerProductService
					.getAllEnergizerProductConversion(erpMaterialId);
			final String measuringUnit;
			final double weight;
			for (final EnergizerProductConversionFactorModel UOM : energizerProductConversionFactorModel)
			{
				if (UOM.getAlternateUOM().equals(convertedUOM))
				{
					measuringUnit = UOM.getPackageWeight().getMeasuringUnits();
					weight = UOM.getPackageWeight().getMeasurement();
					return EnergizerWeightOrVolumeConverter.getConversionValue(measuringUnit, new BigDecimal(weight)).doubleValue();
					//return UOM.getPackageVolume().getMeasurement();
				}
			}
			return 0;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while update product weight in cart:::");
			throw e;
		}
	}

	public List<String> getMessages()
	{
		return message;
	}

	public List<EnergizerProductPalletHeight> getProductsWithOrderedUOM(final List<EnergizerProductPalletHeight> products)
	{
		final List<EnergizerProductPalletHeight> productsWithOrderedUOM = new ArrayList<EnergizerProductPalletHeight>();
		try
		{
			int casePerPallet = 0;
			int layerPerPallet = 0;
			long baseUOMPal = 0;
			long baseUOMLayer = 0, baseUOMCase = 0;
			List<EnergizerProductConversionFactorModel> enrProductConversionList;

			for (final Iterator productsWithCalculatedUOM = products.iterator(); productsWithCalculatedUOM.hasNext();)
			{
				final EnergizerProductPalletHeight tempEnergizerProductPalletHeight = (EnergizerProductPalletHeight) productsWithCalculatedUOM
						.next();
				if (tempEnergizerProductPalletHeight.getOrderedUOM()
						.equalsIgnoreCase(tempEnergizerProductPalletHeight.getCalculatedUOM()))
				{
					productsWithOrderedUOM.add(tempEnergizerProductPalletHeight);
				}
				else
				{
					final String orderedUOM = tempEnergizerProductPalletHeight.getOrderedUOM();
					final String calculatedUOM = tempEnergizerProductPalletHeight.getCalculatedUOM();
					enrProductConversionList = energizerProductService
							.getAllEnergizerProductConversion(tempEnergizerProductPalletHeight.getErpMaterialId());
					if (!enrProductConversionList.isEmpty())
					{
						for (final Iterator iterator = enrProductConversionList.iterator(); iterator.hasNext();)
						{
							final EnergizerProductConversionFactorModel enrProductConversionFactorModel = (EnergizerProductConversionFactorModel) iterator
									.next();

							final String altUOM = enrProductConversionFactorModel.getAlternateUOM();
							if (null != altUOM && altUOM.equalsIgnoreCase("PAL"))
							{
								baseUOMPal = enrProductConversionFactorModel.getConversionMultiplier();
							}

							if (null != altUOM && altUOM.equalsIgnoreCase("LAY"))
							{
								baseUOMLayer = enrProductConversionFactorModel.getConversionMultiplier();
							}
							if (null != altUOM && altUOM.equalsIgnoreCase("CS"))
							{
								baseUOMCase = enrProductConversionFactorModel.getConversionMultiplier();
							}
						}
						layerPerPallet = (int) (baseUOMPal / baseUOMLayer);
						casePerPallet = (int) (baseUOMPal / baseUOMCase);
						if (orderedUOM.equalsIgnoreCase("LAY"))
						{
							for (int layCount = 1; layCount <= layerPerPallet; layCount++)
							{
								final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
								energizerProductPalletHeightTemp.setErpMaterialId(tempEnergizerProductPalletHeight.getErpMaterialId());
								energizerProductPalletHeightTemp.setOrderedUOM(tempEnergizerProductPalletHeight.getOrderedUOM());
								energizerProductPalletHeightTemp.setPalletHeight(0);
								energizerProductPalletHeightTemp.setCalculatedUOM(tempEnergizerProductPalletHeight.getCalculatedUOM());
								productsWithOrderedUOM.add(energizerProductPalletHeightTemp);
							}
						}
						else if (orderedUOM.equalsIgnoreCase("CS"))
						{
							for (int caseCount = 1; caseCount <= casePerPallet; caseCount++)
							{
								final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
								energizerProductPalletHeightTemp.setErpMaterialId(tempEnergizerProductPalletHeight.getErpMaterialId());
								energizerProductPalletHeightTemp.setOrderedUOM(tempEnergizerProductPalletHeight.getOrderedUOM());
								energizerProductPalletHeightTemp.setPalletHeight(0);
								energizerProductPalletHeightTemp.setCalculatedUOM(tempEnergizerProductPalletHeight.getCalculatedUOM());
								productsWithOrderedUOM.add(energizerProductPalletHeightTemp);
							}
						}

					}
				}
			}
		}
		catch (final Exception e)
		{
			LOG.info("Exception occured while calculating product UOM value :::");
			throw e;
		}
		return productsWithOrderedUOM;
	}

	public HashMap getProductNotAddedToCart()
	{

		final List possibleDoubleStackList = new ArrayList();
		final HashMap possibleDoubleStackMap = new HashMap();
		int size = 0;
		try
		{
			if (products != null && products.size() > 0)
			{
				for (final EnergizerProductPalletHeight energizerProductModel : products)
				{

					if (!possibleDoubleStackMap.containsKey(energizerProductModel.getErpMaterialId()))
					{
						size = 1;
						possibleDoubleStackMap.put(energizerProductModel.getErpMaterialId(), size);
					}
					else
					{
						size = (int) possibleDoubleStackMap.get(energizerProductModel.getErpMaterialId());
						size++;
						possibleDoubleStackMap.put(energizerProductModel.getErpMaterialId(), size);
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured in cart:::");
			throw e;
		}
		return possibleDoubleStackMap;
	}

	public double getLayerVolume(final String erpMaterialId)
	{
		final List<EnergizerProductConversionFactorModel> conversionFactor = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		double layerVolume = 0.0;
		double casesVolume = 0.0;
		int casesPerLayer = 0;
		int numberOfEachInCase = 0;
		int numberOfEacInLayer = 0;
		for (final EnergizerProductConversionFactorModel factor : conversionFactor)
		{
			if (factor.getAlternateUOM().equalsIgnoreCase("CS"))
			{
				numberOfEachInCase = factor.getConversionMultiplier();
				casesVolume = factor.getPackageVolume().getMeasurement();
			}
			if (factor.getAlternateUOM().equalsIgnoreCase("LAY"))
			{
				numberOfEacInLayer = factor.getConversionMultiplier();
			}

		}
		casesPerLayer = numberOfEacInLayer / numberOfEachInCase;
		layerVolume = casesPerLayer * casesVolume;
		return layerVolume;

	}

	public String getLayerVolumeUnit(final String erpMaterialId)
	{
		final List<EnergizerProductConversionFactorModel> conversionFactor = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		String unit = null;

		for (final EnergizerProductConversionFactorModel factor : conversionFactor)
		{
			if (factor.getAlternateUOM().equalsIgnoreCase("CS"))
			{
				unit = factor.getPackageVolume().getMeasuringUnits();
			}
		}

		return unit;

	}

	public double getLayerWeight(final String erpMaterialId)
	{
		try
		{
			final List<EnergizerProductConversionFactorModel> conversionFactor = energizerProductService
					.getAllEnergizerProductConversion(erpMaterialId);
			double layerWeight = 0.0;
			double casesWeight = 0.0;
			int casesPerLayer = 0;
			int numberOfEachInCase = 0;
			int numberOfEacInLayer = 0;
			for (final EnergizerProductConversionFactorModel factor : conversionFactor)
			{
				if (factor.getAlternateUOM().equalsIgnoreCase("CS"))
				{
					numberOfEachInCase = factor.getConversionMultiplier();
					casesWeight = factor.getPackageWeight().getMeasurement();
				}
				if (factor.getAlternateUOM().equalsIgnoreCase("LAY"))
				{
					numberOfEacInLayer = factor.getConversionMultiplier();
				}

			}
			casesPerLayer = numberOfEacInLayer / numberOfEachInCase;
			layerWeight = casesPerLayer * casesWeight;
			return layerWeight;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while calculating layerWeight for each entry:::");
			throw e;
		}

	}

	public String getLayerWeightUnit(final String erpMaterialId)
	{
		try
		{
			final List<EnergizerProductConversionFactorModel> conversionFactor = energizerProductService
					.getAllEnergizerProductConversion(erpMaterialId);
			String unit = null;

			for (final EnergizerProductConversionFactorModel factor : conversionFactor)
			{
				if (factor.getAlternateUOM().equalsIgnoreCase("CS"))
				{
					unit = factor.getPackageWeight().getMeasuringUnits();
				}
			}

			return unit;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while getting measuringunits from productConversionFactory:::");
			throw e;
		}

	}

	public HashMap getProductsNotDoublestacked()
	{
		return doubleStackMap;
	}

	public LinkedHashMap<Integer, Integer> getFloorSpaceProductsMap()
	{
		return floorSpaceProductMap;
	}

	public LinkedHashMap getNonPalletFloorSpaceProductsMap()
	{
		return nonPalletFloorSpaceProductMap;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.services.order.EnergizerCartService#calCartTruckUtilization(de.hybris.platform.commercefacades.order
	 * .data.CartData, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public CartData calCartTruckUtilization(final CartData cartData, final String freightType, final String packingOption,
			final boolean enableButton, final String palletType) throws Exception
	{
		try
		{
			// YTODO Auto-generated method stub
			//final String containerType = containerType;
			floorSpaceProductMap = new LinkedHashMap<Integer, Integer>();
			nonPalletFloorSpaceProductMap = new LinkedHashMap<Integer, Double>();

			Double totalProductVolumeAvailable = 100.00;
			Double totalProductWeightAvailable = 100.00;

			Double totalProductVolumePercentageOccupiedForEMEA = 100.00;

			final BigDecimal totalCartWt = new BigDecimal(0);
			final BigDecimal totalCartVolume = new BigDecimal(0);

			final BigDecimal truckVolume = configurationService.getConfiguration().getBigDecimal("truck.volume");

			final BigDecimal truckWeight = configurationService.getConfiguration().getBigDecimal("truck.weight");

			final Integer maxPalletsAllowedInCart = getMaxPalletsAllowedInCart(freightType, palletType,
					cartData.getContainerHeight());

			LOG.info("MaxPalletsAllowedInCart ::: " + maxPalletsAllowedInCart);

			//configurationService.getConfiguration().getString("truck.maxPalletsAllowedInCart");

			final String targetUOMForCartLimit = configurationService.getConfiguration().getString("truck.targetUOMForCartLimit",
					"PAL");

			final String uomLAY = configurationService.getConfiguration().getString("truck.UOM.LAY", "LAY");

			final Double targetUOMMaxHeight = configurationService.getConfiguration().getDouble("truck.PAL.maxHeight", 1.8);

			final Double heightOfWoodenBase = configurationService.getConfiguration().getDouble("truck.woodenbase.height", 15);

			final BigDecimal volumeOfAllProducts = getVolumeOfProductsInCart(cartData, "VOLUMEOFALLPRODUCTS");

			final BigDecimal weightOfAllProducts = getWeightOfProductsInCart(cartData, "WEIGHTOFALLPRODUCTS");

			LOG.info(" Weight of the products in the cart " + weightOfAllProducts);

			final Double weight = getPercentageForTruck(weightOfAllProducts, volumeOfAllProducts, freightType)
					.getPercentWeightUses();

			LOG.info("weight of all products with packing option " + packingOption + ":" + weight);

			// Written for EMEA Truck Optimization - It overrides previous values - START
			totalProductVolumePercentageOccupiedForEMEA = getTotalProductVolumePercentageOccupiedForEMEA(
					cartService.getSessionCart(), freightType, targetUOMForCartLimit, maxPalletsAllowedInCart,
					targetUOMMaxHeight.doubleValue(), uomLAY, heightOfWoodenBase, freightType, palletType).getPercentVolumeUses();

			LOG.info("totalProductVolumePercentageOccupiedForEMEA of all products with packing option " + packingOption + ":"
					+ totalProductVolumePercentageOccupiedForEMEA);

			// Written for EMEA Truck Optimization - It overrides previous values - END

			if (weight > 100 || totalProductVolumePercentageOccupiedForEMEA > 100)
			{
				//LOG.info("Either volume or weight or totalProductVolumePercentageOccupiedForEMEA is greater than 100");
				LOG.info("Either totalProductVolumePercentageOccupiedForEMEA or weight is greater than 100. So, the truck is full.");
				cartData.setIsContainerFull(true);
				cartData.setIsFloorSpaceFull(true);

				//LOG.info("Consumed volume: " + totalProductVolumeAvailable + " Consumed Weight: " + totalProductWeightAvailable);
				LOG.info("Consumed volume: "
						+ new BigDecimal(totalProductVolumePercentageOccupiedForEMEA).setScale(2, RoundingMode.HALF_EVEN)
						+ " Consumed Weight: " + weight);

				LOG.info("Available volume: " + (100 - totalProductVolumeAvailable) + " Available Weight: "
						+ (100 - totalProductWeightAvailable));
			}
			else
			{

				totalProductVolumeAvailable = totalProductVolumeAvailable - totalProductVolumePercentageOccupiedForEMEA;
				totalProductWeightAvailable = totalProductWeightAvailable - weight;
			}

			/*
			 * cartData.setTotalProductVolumeInPercent(totalProductVolumeAvailable);
			 * cartData.setTotalProductWeightInPercent(totalProductWeightAvailable);
			 */
			cartData.setTotalProductVolumeInPercent(totalProductVolumePercentageOccupiedForEMEA);
			cartData.setTotalProductWeightInPercent(weight);

			cartData.setAvailableVolume((Math.round((100 - cartData.getTotalProductVolumeInPercent()) * 100.0) / 100.0));
			cartData.setAvailableWeight((Math.round((100 - cartData.getTotalProductWeightInPercent()) * 100.0) / 100.0));

			if (!cartData.isIsContainerFull())
			{
				LOG.info(
						"Available volume: " + cartData.getAvailableVolume() + " Available Weight: " + cartData.getAvailableWeight());
				LOG.info("Consumed volume: " + cartData.getTotalProductVolumeInPercent() + " Consumed Weight: "
						+ cartData.getTotalProductWeightInPercent());
			}

			// Added for Container Optimization for EMEA - START
			cartData.setFloorSpaceCount(maxPalletsAllowedInCart);
			cartData.setEnableFloorSpaceGraphics(true);
			cartData.setFloorSpaceProductsMap(floorSpaceProductMap);
			cartData.setNonPalletFloorSpaceProductsMap(nonPalletFloorSpaceProductMap);
			cartData.setTotalPalletCount(fullPalletCount);
			cartData.setPartialPalletCount(partialPalletCount);

			LOG.info("Full Pallets ::: " + fullPalletCount);
			LOG.info("Partial Pallets ::: " + partialPalletCount);

			LOG.info("------------------ floorSpaceProductMap -------------------");
			final Iterator<Entry<Integer, Integer>> it = floorSpaceProductMap.entrySet().iterator();
			while (it.hasNext())
			{
				final Map.Entry<Integer, Integer> entry = it.next();
				LOG.info("Key : " + entry.getKey() + " , Value : " + entry.getValue());
				//it.remove();
			}

			LOG.info("------------------ nonPalletFloorSpaceProductMap -------------------");
			final Iterator<Entry<Integer, Double>> it1 = nonPalletFloorSpaceProductMap.entrySet().iterator();
			while (it1.hasNext())
			{
				final Map.Entry<Integer, Double> entry = it1.next();
				LOG.info("Key : " + entry.getKey() + " , Value : " + entry.getValue());
				//it1.remove();
			}
			// Added for Container Optimization for EMEA - END
		}
		catch (final Exception e)
		{
			LOG.error("Error occured in calCartTruckUtilization method ::: " + e);
		}
		return cartData;
	}

	private Integer getAlernateConversionMultiplierForUOM(final List<EnergizerProductConversionFactorModel> conversionList,
			final String uom)
	{
		// YTODO Auto-generated method stub
		for (final EnergizerProductConversionFactorModel enrProdConversion : conversionList)
		{
			if (enrProdConversion.getAlternateUOM().equalsIgnoreCase(uom))
			{
				return enrProdConversion.getConversionMultiplier();
			}
		}

		return null;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.order.EnergizerCartService#updateExpectedUnitPriceForCartEntry()
	 */
	@Override
	public CommerceCartModification updateExpectedUnitPriceForCartEntry(final OrderEntryData orderEntry)
	{
		try
		{
			final CartModel cartModel = cartService.getSessionCart();

			final AbstractOrderEntryModel abstractOrderEntryModel = getEntryForNumber(cartModel, orderEntry.getEntryNumber());
			if (null == orderEntry.getExpectedUnitPrice() || StringUtils.isEmpty(orderEntry.getExpectedUnitPrice()))
			{
				abstractOrderEntryModel.setExpectedUnitPrice("0.00");
			}
			else if (abstractOrderEntryModel.getEachUnitPrice()
					.compareTo(BigDecimal.valueOf(Double.parseDouble(orderEntry.getExpectedUnitPrice()))) == 0)
			{ // If the customer enters expected unit price equal to the each unit price, then set the agree edgewell price to 'true'.
				abstractOrderEntryModel.setExpectedUnitPrice(abstractOrderEntryModel.getEachUnitPrice().toString());
				abstractOrderEntryModel.setAgreeEdgewellPrice(Boolean.valueOf(true));
			}
			else
			{
				abstractOrderEntryModel.setExpectedUnitPrice(orderEntry.getExpectedUnitPrice().toString());
				// If customer enters any expected unit price manually, it means the customer is not agreeing to the edgewell each unit price. So, set the 'agreeEdgewellPrice' flag to 'false'.
				abstractOrderEntryModel.setAgreeEdgewellPrice(Boolean.valueOf(false));
				// If the customer expected price is not empty & not equal to each price, then it means the customer has not agreed to edgewell price for this product. So, setting the 'setAgreeEdgewellUnitPriceForAllProducts' flag also to 'false'.
				cartModel.setAgreeEdgewellUnitPriceForAllProducts(Boolean.valueOf(false));
			}

			modelService.save(abstractOrderEntryModel);
			modelService.refresh(abstractOrderEntryModel);
			modelService.save(cartModel);
			modelService.refresh(cartModel);

			boolean isAgreeEdgewellPriceForAllProducts = true;
			if (null != cartModel && !cartModel.getEntries().isEmpty())
			{
				for (final AbstractOrderEntryModel entryModel : cartModel.getEntries())
				{
					if (!entryModel.getAgreeEdgewellPrice())
					{
						isAgreeEdgewellPriceForAllProducts = false;
						break;
					}
				}

				cartModel.setAgreeEdgewellUnitPriceForAllProducts(isAgreeEdgewellPriceForAllProducts);

				modelService.save(cartModel);
				modelService.refresh(cartModel);
			}

			final CommerceCartModification modification = new CommerceCartModification();
			modification.setEntry(abstractOrderEntryModel);
			modification.setStatusCode(CommerceCartModificationStatus.SUCCESS);

			return modification;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while saving update unitprice for cartEntry:::");
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.services.order.EnergizerCartService#updateEachUnitPriceForCartEntry(de.hybris.platform.
	 * commercefacades .order.data.OrderEntryData)
	 */
	@Override
	public CommerceCartModification updateEachUnitPriceForCartEntry(final OrderEntryData orderEntry)
	{
		try
		{
			final CartModel cartModel = cartService.getSessionCart();

			final AbstractOrderEntryModel abstractOrderEntryModel = getEntryForNumber(cartModel, orderEntry.getEntryNumber());

			//Set each unit price to the abstractOrderEntryModel to display it in the cart page table
			abstractOrderEntryModel
					.setEachUnitPrice(null != orderEntry.getEachUnitPrice() ? orderEntry.getEachUnitPrice().getValue() : null);
			abstractOrderEntryModel.setIsPBG(orderEntry.getProduct().isIsPBG());
			abstractOrderEntryModel.setIsWeSellProduct(orderEntry.getProduct().isIsWeSellProduct());


			modelService.save(abstractOrderEntryModel);
			modelService.refresh(abstractOrderEntryModel);
			modelService.refresh(cartModel);


			final CommerceCartModification modification = new CommerceCartModification();
			modification.setEntry(abstractOrderEntryModel);
			modification.setStatusCode(CommerceCartModificationStatus.SUCCESS);

			return modification;
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while saving update unitprice for cartEntry:::");
			throw e;
		}
	}

	@Override
	public CommerceCartModification updateAgreeEdgewellUnitPriceForCartEntry(final OrderEntryData orderEntry)
	{

		final CartModel cartModel = cartService.getSessionCart();

		final AbstractOrderEntryModel abstractOrderEntryModel = getEntryForNumber(cartModel, orderEntry.getEntryNumber());

		//Set agreeEdgewellUnitPrice flag depending upon the customer selection
		abstractOrderEntryModel.setAgreeEdgewellPrice(orderEntry.isAgreeEdgewellUnitPrice());

		/*
		 * if (!orderEntry.isAgreeEdgewellUnitPrice()) { abstractOrderEntryModel.setExpectedUnitPrice(null); } else {
		 */
		abstractOrderEntryModel.setExpectedUnitPrice(orderEntry.getExpectedUnitPrice());
		/* } */

		abstractOrderEntryModel.setEachUnitPrice(orderEntry.getEachUnitPrice().getValue());

		modelService.save(abstractOrderEntryModel);
		modelService.refresh(abstractOrderEntryModel);
		modelService.refresh(cartModel);

		// If the customer does not agree to the edgewell unit each price for any of the product, then set 'false' to the header level flag
		if (!orderEntry.isAgreeEdgewellUnitPrice())
		{
			cartModel.setAgreeEdgewellUnitPriceForAllProducts(false);
		}
		modelService.save(cartModel);
		modelService.refresh(cartModel);

		final CommerceCartModification modification = new CommerceCartModification();
		modification.setEntry(abstractOrderEntryModel);
		modification.setStatusCode(CommerceCartModificationStatus.SUCCESS);

		return modification;
	}

	@Override
	public void updateAgreeEdgewellPriceForAllProducts(final CartData cartData)
	{
		CommerceCartModification modification = null;
		try
		{
			final CartModel cartModel = cartService.getSessionCart();

			if (null != cartData && null != cartData.getEntries() && !cartData.getEntries().isEmpty())
			{
				for (final OrderEntryData orderEntry : cartData.getEntries())
				{
					final AbstractOrderEntryModel abstractOrderEntryModel = getEntryForNumber(cartModel, orderEntry.getEntryNumber());

					if (null == orderEntry.getExpectedUnitPrice() || StringUtils.isEmpty(orderEntry.getExpectedUnitPrice()))
					{
						abstractOrderEntryModel.setExpectedUnitPrice(null);
					}
					else
					{
						abstractOrderEntryModel.setExpectedUnitPrice(orderEntry.getExpectedUnitPrice());
					}

					//abstractOrderEntryModel.setExpectedUnitPrice(Double.valueOf(orderEntry.getExpectedUnitPrice()));

					//Set each unit price to the abstractOrderEntryModel to display it in the cart page table
					abstractOrderEntryModel.setEachUnitPrice(orderEntry.getEachUnitPrice().getValue());
					abstractOrderEntryModel.setAgreeEdgewellPrice(orderEntry.isAgreeEdgewellUnitPrice());

					modelService.save(abstractOrderEntryModel);
					modelService.refresh(abstractOrderEntryModel);

					modification = new CommerceCartModification();
					modification.setEntry(abstractOrderEntryModel);
					modification.setStatusCode(CommerceCartModificationStatus.SUCCESS);
				}

				modelService.refresh(cartModel);
				cartModel.setAgreeEdgewellUnitPriceForAllProducts(cartData.isAgreeEdgewellUnitPriceForAllProducts());
				modelService.save(cartModel);
				modelService.refresh(cartModel);
			}
		}
		catch (final Exception e)
		{
			modification.setStatusCode(CommerceCartModificationStatus.CONFIGURATION_ERROR);
			LOG.info("Exception occured while updating cart entry ::: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void updateCartCurrency()
	{
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final CartModel cartModel = cartService.getSessionCart();
		if (null != cartModel && null != cartModel.getCurrency()
				&& !cartModel.getCurrency().getIsocode().equalsIgnoreCase(b2bUnit.getCurrencyPreference().getIsocode()))
		{
			cartModel.setCurrency(b2bUnit.getCurrencyPreference());
			modelService.save(cartModel);
			modelService.refresh(cartModel);
			LOG.info("Currency preference '" + b2bUnit.getCurrencyPreference() + "' setup for the session cart : "
					+ cartModel.getCode());
		}
	}

	protected AbstractOrderEntryModel getEntryForNumber(final AbstractOrderModel order, final int number)
	{
		final List<AbstractOrderEntryModel> entries = order.getEntries();
		if (entries != null && !entries.isEmpty())
		{
			final Integer requestedEntryNumber = Integer.valueOf(number);
			for (final AbstractOrderEntryModel entry : entries)
			{
				if (entry != null && requestedEntryNumber.equals(entry.getEntryNumber()))
				{
					return entry;
				}
			}
		}
		return null;
	}

	protected int getMaxPalletsAllowedInCart(final String freightType, final String palletType, String containerHeight)
	{
		int maxPalletsAllowedInCart = 0;

		final String containerDefaultHeight = configurationService.getConfiguration().getString("container.default.height");

		if (null == containerHeight || StringUtils.isEmpty(containerHeight))
		{
			containerHeight = containerDefaultHeight;
		}
		if (FREIGHT_TRUCK.equalsIgnoreCase(freightType))
		{
			maxPalletsAllowedInCart = Integer
					.parseInt(configurationService.getConfiguration().getString("truck." + palletType + ".max.pallets"));
		}
		else if (FREIGHT_CONTAINER.equalsIgnoreCase(freightType))
		{
			maxPalletsAllowedInCart = Integer.parseInt(configurationService.getConfiguration()
					.getString("container." + containerHeight + "." + palletType + ".max.pallets"));
		}

		return maxPalletsAllowedInCart;
	}

	protected void calculateFloorSpaceMapForEMEA(final BigDecimal totalFullPalletsCount, final BigDecimal totalPartialPalletsCount,
			final int noOfRows)
	{
		try
		{
			// floorSpaceProductsMap
			final int fullBlockPallets = totalFullPalletsCount.intValue() / noOfRows;
			final int remainingBlockPallets = totalFullPalletsCount.intValue() % noOfRows;
			int emptyBlockCount = 0;

			LOG.info("fullBlockPallets ::: " + fullBlockPallets + " , remainingBlockPallets ::: " + remainingBlockPallets);

			if (fullBlockPallets != 0)
			{
				for (int i = 0; i < fullBlockPallets; i++)
				{
					floorSpaceProductMap.put(i, noOfRows);
				}
			}

			LOG.info("floorSpaceProductMap for fullBlockPallets ::: " + floorSpaceProductMap);

			if (remainingBlockPallets != 0)
			{
				floorSpaceProductMap.put(floorSpaceProductMap.size() != 0 ? floorSpaceProductMap.size() : 0, remainingBlockPallets);
				emptyBlockCount = noOfRows - remainingBlockPallets;
			}

			LOG.info("floorSpaceProductMap including remainingBlockPallets ::: " + floorSpaceProductMap);
			LOG.info("emptyBlockCount in floorSpaceProductMap at index " + (floorSpaceProductMap.size() - 1) + " is ::: "
					+ emptyBlockCount);


			// nonPalletFloorSpaceProductMap
			double remainingPartialPalCount = 0.0;
			final double totalPartialPallets = totalPartialPalletsCount.doubleValue();
			final int floorSpaceProductMapSize = floorSpaceProductMap.size();
			boolean flag = false;

			LOG.info("totalPartialPallets ::: " + totalPartialPallets);
			if (totalPartialPallets != 0)
			{
				LOG.info("totalPartialPallets is a non-zero value !!!");
				if (emptyBlockCount != 0)
				{
					if (totalPartialPallets >= emptyBlockCount)
					{
						LOG.info("totalPartialPallets ::: " + totalPartialPallets + " , emptyBlockCount ::: " + emptyBlockCount);
						nonPalletFloorSpaceProductMap.put(floorSpaceProductMapSize != 0 ? floorSpaceProductMapSize - 1 : 0,
								(double) emptyBlockCount);
						remainingPartialPalCount = totalPartialPallets - emptyBlockCount;
						flag = true;
					}
					else if (totalPartialPallets < emptyBlockCount)
					{
						nonPalletFloorSpaceProductMap.put(floorSpaceProductMapSize != 0 ? floorSpaceProductMapSize - 1 : 0,
								totalPartialPallets);
						remainingPartialPalCount = 0;
					}
				}
				else
				{
					remainingPartialPalCount = totalPartialPallets;
				}

				LOG.info("remainingPartialPalCount ::: " + remainingPartialPalCount);
				LOG.info("nonPalletFloorSpaceProductMap ::: " + nonPalletFloorSpaceProductMap);

				if (remainingPartialPalCount > 0)
				{
					final int remainingFullBlockPartialCount = (int) remainingPartialPalCount / noOfRows;
					LOG.info("remainingFullBlockPartialCount ::: " + remainingFullBlockPartialCount);
					if (remainingFullBlockPartialCount == 0)
					{
						nonPalletFloorSpaceProductMap.put(floorSpaceProductMapSize, remainingPartialPalCount);
						LOG.info("Added " + remainingPartialPalCount + " partial pallets to nonPalletFloorSpaceProductMap at index "
								+ floorSpaceProductMapSize + " , nonPalletFloorSpaceProductMap ::: " + nonPalletFloorSpaceProductMap);
						remainingPartialPalCount = 0;
						LOG.info(
								"remainingPartialPalCount after adding " + nonPalletFloorSpaceProductMap.get(floorSpaceProductMap.size())
										+ " partial pallets to nonPalletFloorSpaceProductMap ::: " + remainingPartialPalCount);
					}
					else if (remainingFullBlockPartialCount > 0)
					{
						int partialPalMapIndex = floorSpaceProductMapSize;
						for (int i = 0; i < remainingFullBlockPartialCount; i++)
						{
							LOG.info("partialPalMapIndex ::: " + partialPalMapIndex);
							nonPalletFloorSpaceProductMap.put(partialPalMapIndex, (double) noOfRows);
							remainingPartialPalCount = remainingPartialPalCount - noOfRows;
							LOG.info("remainingPartialPalCount inside for loop ::: " + remainingPartialPalCount);
							partialPalMapIndex += 1;
						}
						LOG.info("remainingPartialPalCount after adding full block partial pallets ::: " + remainingPartialPalCount);
						LOG.info("nonPalletFloorSpaceProductMap with full block partial pallets::: " + nonPalletFloorSpaceProductMap);
					}
				}
			}
			else
			{
				LOG.info("partialPalCount is zero !!! ");
			}

			int lastIndex = 0;
			if (flag)
			{
				lastIndex = floorSpaceProductMapSize + nonPalletFloorSpaceProductMap.size() - 1;
			}
			else
			{
				lastIndex = floorSpaceProductMapSize + nonPalletFloorSpaceProductMap.size();
			}
			if (remainingPartialPalCount > 0)
			{
				nonPalletFloorSpaceProductMap.put(lastIndex, remainingPartialPalCount);
			}

			LOG.info("nonPalletFloorSpaceProductMap including remainingPartialPalCount ::: " + nonPalletFloorSpaceProductMap);

			LOG.info("Full products map ::: " + floorSpaceProductMap + nonPalletFloorSpaceProductMap);

			fullPalletCount = totalFullPalletsCount.intValue();

			partialPalletCount = 0;
			if (totalPartialPallets != 0 && !(totalPartialPallets % 1 == 0))
			{
				partialPalletCount = (int) totalPartialPallets + 1;
			}
			else
			{
				partialPalletCount = (int) totalPartialPallets;
			}

			LOG.info("partialPalletCount ::: " + partialPalletCount);
		}
		catch (final Exception e)
		{
			LOG.error("Exception occured while calculating container pallet/nonpallet space::");
			e.printStackTrace();
		}
	}
}
