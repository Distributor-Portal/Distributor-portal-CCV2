/**
 *
 */
package com.energizer.facades.order.impl;

import de.hybris.platform.b2bacceleratorfacades.order.B2BOrderFacade;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.session.SessionService;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

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

	@Autowired
	private SessionService sessionService;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerInvoiceFacade#getPDFInvoiceAsBytes(java.lang.String)
	 */
	@Override
	public byte[] getPDFInvoiceAsBytes(final String siteUid, final String orderNumber)
	{
		System.out.println("Enter in getPDFInvoiceAsBytes method-->1 ");
		System.out.println("orderNumber-->" + orderNumber);

		//final OrderData orderData = orderFacade.getOrderDetailsForCode(orderNumber);

		try
		{
			final String erpOrderNumber = sessionService.getAttribute("erpOrderNumber");
			System.out.println("erpOrderNumber-->" + erpOrderNumber);

			final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");
			System.out.println("PERSONALCARE_EMEA-->" + PERSONALCARE_EMEA);
			System.out.println("siteUidTest-->" + siteUid);

			if (PERSONALCARE_EMEA.equalsIgnoreCase(siteUid))
			{
				System.out.println("Enter in personalCareEMEA");
				return (defaultEnergizerInvoiceService.getPDFInvoiceAsBytes(erpOrderNumber));
			}
			/*
			 * else { return (invoiceService.getPDFInvoiceAsBytes(orderData)); }
			 */
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
