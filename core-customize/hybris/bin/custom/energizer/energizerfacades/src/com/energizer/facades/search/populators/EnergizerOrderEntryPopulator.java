/**
 *
 */
package com.energizer.facades.search.populators;

import de.hybris.platform.commercefacades.order.converters.populator.OrderEntryPopulator;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

import org.apache.commons.lang.StringUtils;


/**
 * @author M1023097
 *
 */
public class EnergizerOrderEntryPopulator extends OrderEntryPopulator
{
	@Override
	public void populate(final AbstractOrderEntryModel source, final OrderEntryData target)
	{
		super.populate(source, target);
		target.setAdjustedQty(source.getAdjustedQty());
		addTotals(source, target);
		addExpectedUnitPrice(source, target);
		//target.setAdjustedLinePrice(source.getAdjustedLinePrice());
		target.setRejectedStatus(source.getRejectedStatus());
		if (source.getCustomerMaterialId() != null)
		{
			target.setCustomerMaterialId(source.getCustomerMaterialId());

		}
		if (null != source.getIsNewEntry())
		{
			target.setIsNewEntry(source.getIsNewEntry());
		}
		else
		{
			target.setIsNewEntry("N");
		}
		target.setAgreeEdgewellUnitPrice(
				null != source.getAgreeEdgewellPrice() ? source.getAgreeEdgewellPrice().booleanValue() : Boolean.valueOf(false));
		if (null != source.getInventoryAvailable())
		{
			target.setInventoryAvailable(NumberFormat.getIntegerInstance().format(source.getInventoryAvailable().intValue()));
		}
		target.setDiscountAmount(String.format("%.2f", source.getDiscountAmount()));
		target.setDiscountPercent(String.valueOf(source.getDiscountPercent()));
	}

	@Override
	protected void addTotals(final AbstractOrderEntryModel orderEntry, final OrderEntryData entry)
	{
		if (orderEntry.getBasePrice() != null)
		{
			entry.setBasePrice(createPrice(orderEntry, orderEntry.getBasePrice()));
		}
		if (orderEntry.getAdjustedItemPrice() != null)
		{
			entry.setAdjustedItemPrice(createPrice(orderEntry, orderEntry.getAdjustedItemPrice().doubleValue()));
		}
		if (orderEntry.getTotalPrice() != null)
		{
			entry.setTotalPrice(createPrice(orderEntry, orderEntry.getTotalPrice()));
		}
		if (orderEntry.getAdjustedLinePrice() != null)
		{
			entry.setAdjustedLinePrice(createPrice(orderEntry, orderEntry.getAdjustedLinePrice().doubleValue()));
		}
		// Setting up each unit price to display it in the cart page
		if (orderEntry.getEachUnitPrice() != null)
		{
			entry.setEachUnitPrice(createPrice(orderEntry, orderEntry.getEachUnitPrice().doubleValue()));
			entry.getEachUnitPrice().setValue(entry.getEachUnitPrice().getValue().setScale(2, RoundingMode.CEILING));
		}
	}



	public void addExpectedUnitPrice(final AbstractOrderEntryModel orderEntry, final OrderEntryData entry)
	{
		if (orderEntry.getExpectedUnitPrice() != null)
		{
			if (null == orderEntry.getExpectedUnitPrice() || StringUtils.isEmpty(orderEntry.getExpectedUnitPrice())
					|| "0.00".equalsIgnoreCase(
							new BigDecimal(orderEntry.getExpectedUnitPrice()).setScale(2, RoundingMode.CEILING).toPlainString()))
			{
				entry.setExpectedUnitPrice(StringUtils.EMPTY);
			}
			else
			{

				//entry.setExpectedUnitPrice(orderEntry.getExpectedUnitPrice().toString());
				//System.out.println("orderEntry.getExpectedUnitPrice() ::: " + orderEntry.getExpectedUnitPrice());
				/*
				 * System.out.println("new bd.setScale(2, RoundingMode.CEILING) ::: " + new
				 * BigDecimal(orderEntry.getExpectedUnitPrice()).setScale(2, RoundingMode.CEILING));
				 */
				entry.setExpectedUnitPrice(
						new BigDecimal(orderEntry.getExpectedUnitPrice()).setScale(2, RoundingMode.CEILING).toPlainString());
				/*
				 * System.out.println("new bigDecimal.scale plain  ::: " + new
				 * BigDecimal(orderEntry.getExpectedUnitPrice()).setScale(2, RoundingMode.CEILING).toPlainString());
				 */

			}
		}

	}
}