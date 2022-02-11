/**
 *
 */
package com.energizer.services.order.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.order.impl.DefaultCalculationService;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.PriceValue;
import de.hybris.platform.util.TaxValue;

import java.util.Collection;
import java.util.List;

/**
 * @author kaki.rajasekhar
 *
 */
public class DefaultEnergizerCalculationService extends DefaultCalculationService
{


	@SuppressWarnings("deprecation")
	@Override
	protected void resetAllValues(final AbstractOrderEntryModel entry) throws CalculationException
	{
		// taxes
		final Collection<TaxValue> entryTaxes = findTaxValues(entry);
		entry.setTaxValues(entryTaxes);
		final PriceValue pv = findBasePrice(entry);
		final AbstractOrderModel order = entry.getOrder();
		if (null == pv && (boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
		{
			entry.setBasePrice(Double.valueOf(0.00));
		}
		else
		{
			final PriceValue basePrice = convertPriceIfNecessary(pv, order.getNet().booleanValue(), order.getCurrency(), entryTaxes);
			entry.setBasePrice(Double.valueOf(basePrice.getValue()));
		}

		final List<DiscountValue> entryDiscounts = findDiscountValues(entry);
		entry.setDiscountValues(entryDiscounts);
	}

}
