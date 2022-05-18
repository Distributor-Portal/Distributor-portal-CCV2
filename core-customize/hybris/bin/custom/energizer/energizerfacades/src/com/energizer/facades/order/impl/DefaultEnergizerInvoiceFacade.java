/**
 *
 */
package com.energizer.facades.order.impl;

import de.hybris.platform.b2bacceleratorfacades.order.B2BOrderFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import javax.annotation.Resource;

import com.energizer.core.invoice.impl.DefaultEnergizerInvoiceService;
import com.energizer.core.invoice.impl.RestEnergizerInvoiceService;
import com.energizer.facades.order.EnergizerInvoiceFacade;


/**
 * @author M1023278
 *
 */
public class DefaultEnergizerInvoiceFacade implements EnergizerInvoiceFacade
{

	@Resource(name = "invoiceService")
	private RestEnergizerInvoiceService invoiceService;

	@Resource(name = "b2bOrderFacade")
	private B2BOrderFacade orderFacade;

	@Resource(name = "defaultEnergizerInvoiceService")
	private DefaultEnergizerInvoiceService defaultEnergizerInvoiceService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerInvoiceFacade#getPDFInvoiceAsBytes(java.lang.String)
	 */
	@Override
	public byte[] getPDFInvoiceAsBytes(final String siteUid, final String orderNumber)
	{
		System.out.println("EntergetPDFInvoiceAsBytes");
		System.out.println("orderNumber-->" + orderNumber);
		final OrderData orderData = orderFacade.getOrderDetailsForCode(orderNumber);
		if (orderData.getErpOrderNumber().isEmpty())
		{
			System.out.println("getErpOrderNumber-->empty");
		}
		else
		{
			System.out.println("getErpOrderNumber-->" + orderData.getErpOrderNumber());
		}

		try
		{

			final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");

			if (PERSONALCARE_EMEA.equalsIgnoreCase(siteUid))
			{
				return (defaultEnergizerInvoiceService.getPDFInvoiceAsBytes(orderData));
			}
			else
			{
				return (invoiceService.getPDFInvoiceAsBytes(orderData));
			}
		}
		catch (final Exception e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}

}
