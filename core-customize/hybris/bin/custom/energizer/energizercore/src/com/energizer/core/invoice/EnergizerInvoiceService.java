/**
 * 
 */
package com.energizer.core.invoice;

import de.hybris.platform.commercefacades.order.data.OrderData;


/**
 * @author kaushik.ganguly
 * 
 */
public interface EnergizerInvoiceService
{
	public byte[] getPDFInvoiceAsBytes(OrderData orderData) throws Exception;

}
