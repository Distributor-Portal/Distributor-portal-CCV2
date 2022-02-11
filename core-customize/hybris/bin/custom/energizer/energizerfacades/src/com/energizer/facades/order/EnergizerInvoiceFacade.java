/**
 *
 */
package com.energizer.facades.order;

/**
 * @author M1023278
 *
 */
public interface EnergizerInvoiceFacade
{

	public byte[] getPDFInvoiceAsBytes(String siteUid, String orderNumber);
}
