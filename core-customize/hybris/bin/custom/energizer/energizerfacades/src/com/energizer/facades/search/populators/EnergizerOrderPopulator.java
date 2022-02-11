/**
 *
 */
package com.energizer.facades.search.populators;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.order.converters.populator.OrderPopulator;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.data.EnergizerB2BUnitData;
import com.energizer.core.data.EnergizerDeliveryNoteData;
import com.energizer.core.datafeed.service.impl.DefaultEnergizerAddressService;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author M1023097
 *
 */
public class EnergizerOrderPopulator extends OrderPopulator
{

	protected static final Logger LOG = Logger.getLogger(EnergizerOrderPopulator.class);

	@Resource(name = "b2BCustomerConverter")
	private Converter<B2BCustomerModel, CustomerData> b2BCustomerConverter;

	@Resource(name = "energizerB2BUnitConverter")
	private Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> energizerB2BUnitConverter;

	@Resource
	DefaultEnergizerAddressService defaultEnergizerAddressService;

	@Override
	public void populate(final OrderModel source, final OrderData target)
	{
		Map<Integer, List<String>> palStackData = new TreeMap<Integer, List<String>>();
		final Map<Integer, List<String>> sortedPalStackData = new TreeMap<Integer, List<String>>();

		if (null == source)
		{
			LOG.info("Source/Order Model is NULL !");
		}
		else
		{
			// If the delivery address is null/ no reference, then set the default address of that b2b unit to the order's delivery address.
			if (null == source.getDeliveryAddress())
			{
				LOG.info("Delivery address null for this order ::: '" + source.getCode()
						+ "' . So setting the default address of the b2b unit to the order !");
				final List<AddressModel> energizerAddressModelList = defaultEnergizerAddressService
						.fetchAddressForB2BUnit(source.getB2bUnit().getUid());
				if (null != energizerAddressModelList && !energizerAddressModelList.isEmpty())
				{
					LOG.info("No. of adresses for this b2b unit '" + source.getB2bUnit().getUid() + "' is ::: "
							+ energizerAddressModelList.size());

					if (energizerAddressModelList.size() > 0)
					{
						source.setDeliveryAddress(energizerAddressModelList.get(0));
					}
				}
				else
				{
					LOG.info("Delivery address null for this b2b unit !");
				}
			}

			super.populate(source, target);
			target.setErpOrderNumber(source.getErpOrderNumber());
			target.setInvoiceNumber(source.getInvoiceNumber());
			target.setInvoicePDF(source.getInvoicePDF());
			target.setErpOrderCreator(source.getErpOrderCreator());
			target.setShipmentTrackingURL(source.getShipmentTrackingURL());
			addTotals(source, target);
			target.setRequestedDeliveryDate(source.getRequestedDeliveryDate());
			if (null != source.getOrderApprover())
			{
				//LOG.info("Approver for the order '" + source.getCode() + "' ::: " + source.getOrderApprover().getUid());
				target.setOrderApprover(b2BCustomerConverter.convert(source.getOrderApprover()));
			}
			target.setContainerId(source.getContainerId());
			target.setSealNumber(source.getSealNumber());
			target.setVesselNumber(source.getVesselNumber());
			target.setDocumentID(source.getDocumentID());
			target.setDocumentClass(source.getDocumentClass());
			target.setContrEP(source.getContrEP());
			if (null != source.getB2bUnit())
			{
				target.setB2bUnit(energizerB2BUnitConverter.convert(source.getB2bUnit()));

				if (null != source.getB2bUnit().getOrderBlock())
				{
					target.setIsOrderBlock(source.getB2bUnit().getOrderBlock());
				}
			}
			if (null != source.getRejectionComment())
			{
				target.setRejectionComments(source.getRejectionComment());
			}

			//target.setArchiveID(source.getArchiveID());
			target.setContainerHeight(source.getContainerHeight());
			target.setContainerPackingType(source.getContainerPackingType());
			target.setTotalPalletCount(source.getTotalPalletCount());
			target.setVirtualPalletCount(source.getVirtualPalletCount());
			target.setPartialPalletCount(source.getPartialPalletCount());
			target.setOrderComments(source.getOrderComments());
			target.setAgreeEdgewellUnitPriceForAllProducts(null != source.getAgreeEdgewellUnitPriceForAllProducts()
					? source.getAgreeEdgewellUnitPriceForAllProducts().booleanValue()
					: false);

			palStackData = source.getPalStackData();
			final SortedSet<Integer> keys = new TreeSet<Integer>(palStackData.keySet());
			for (final Integer key : keys)
			{
				final List<String> value = palStackData.get(key);
				sortedPalStackData.put(key, value);
			}
			target.setPalStackData(sortedPalStackData);

			target.setContainerVolumeUtilization(source.getContainerVolumeUtilization());
			target.setContainerWeightUtilization(source.getContainerWeightUtilization());

			// Delivery Notes
			target.setCartCode(source.getCartCode());
			setDeliveryNoteFiles(source, target);

			//Added code changes for WeSell Implementation - START
			target.setSalesRepUid(source.getSalesRepUid());
			target.setSalesRepName(source.getSalesRepName());
			target.setSelectedEmpUid(source.getSelectedEmpUid());
			target.setSelectedEmpName(source.getSelectedEmpName());
			target.setSalesRepEmailID(source.getSalesRepEmailID());
			target.setSelectedEmpEmailID(source.getSelectedEmpEmailID());
			target.setPlacedBySalesRep(null != source.getPlacedBySalesRep() ? source.getPlacedBySalesRep().booleanValue() : false);
			target.setSalesRepCurrencyIsoCode(source.getCurrency().getIsocode());
			//Added code changes for WeSell Implementation - END
		}
	}

	protected void addTotals(final OrderModel orderEntry, final OrderData orderData)
	{
		if (orderEntry.getAdjustedTotalPrice() != null)
		{
			orderData.setAdjustedTotalPrice(createPrice(orderEntry, orderEntry.getAdjustedTotalPrice().doubleValue()));
		}
		if (orderEntry.getAdjustedShippingCharge() != null)
		{
			orderData.setAdjustedShippingCharge(createPrice(orderEntry, orderEntry.getAdjustedShippingCharge().doubleValue()));
		}
		if (orderEntry.getAdjustedTaxCharges() != null)
		{
			orderData.setAdjustedTaxCharges(createPrice(orderEntry, orderEntry.getAdjustedTaxCharges().doubleValue()));
		}
		if (orderEntry.getDeliveryCost() != null)
		{
			orderData.setDeliveryCost(createPrice(orderEntry, orderEntry.getDeliveryCost().doubleValue()));
		}

	}

	protected void setDeliveryNoteFiles(final OrderModel orderEntry, final OrderData orderData)
	{

		if (null != orderEntry.getDeliveryNoteFiles() && !orderEntry.getDeliveryNoteFiles().getMedias().isEmpty())
		{

			final List<EnergizerDeliveryNoteData> deliveryNoteFilesList = new ArrayList<EnergizerDeliveryNoteData>();
			final Collection<MediaModel> mediaModels = orderEntry.getDeliveryNoteFiles().getMedias();

			EnergizerDeliveryNoteData deliveryNoteFileData = null;
			for (final MediaModel media : mediaModels)
			{
				deliveryNoteFileData = new EnergizerDeliveryNoteData();
				if (null != media)
				{
					deliveryNoteFileData.setFileName(media.getAltText());
					deliveryNoteFileData.setFileSize(media.getSize());
					deliveryNoteFileData.setUrl(media.getURL());
					deliveryNoteFileData.setMediaCode(media.getCode());
					deliveryNoteFileData.setMimeType(media.getMime());
				}
				deliveryNoteFilesList.add(deliveryNoteFileData);
			}
			orderData.setDeliveryNoteFiles(deliveryNoteFilesList);
			LOG.info(
					"Number of files uploaded for this order '" + orderEntry.getCode() + "' is ::: " + deliveryNoteFilesList.size());
		}
	}
}
