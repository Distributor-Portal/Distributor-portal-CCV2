/**
 *
 */
package com.energizer.storefront.forms;

/**
 * @author KA289322
 *
 */
public class UpdateExpectedUnitPriceForm
{
	private String expectedUnitPrice;

	private boolean agreeEdgewellUnitPrice;

	private boolean agreeEdgewellUnitPriceForAllProducts;

	/**
	 * @return the expectedUnitPrice
	 */
	public String getExpectedUnitPrice()
	{
		return expectedUnitPrice;
	}

	/**
	 * @param expectedUnitPrice
	 *           the expectedUnitPrice to set
	 */
	public void setExpectedUnitPrice(final String expectedUnitPrice)
	{
		this.expectedUnitPrice = expectedUnitPrice;
	}

	/**
	 * @return the agreeEdgewellUnitPrice
	 */
	public boolean getAgreeEdgewellUnitPrice()
	{
		return agreeEdgewellUnitPrice;
	}

	/**
	 * @param agreeEdgewellUnitPrice
	 *           the agreeEdgewellUnitPrice to set
	 */
	public void setAgreeEdgewellUnitPrice(final boolean agreeEdgewellUnitPrice)
	{
		this.agreeEdgewellUnitPrice = agreeEdgewellUnitPrice;
	}

	/**
	 * @return the agreeEdgewellUnitPriceForAllProducts
	 */
	public boolean isAgreeEdgewellUnitPriceForAllProducts()
	{
		return agreeEdgewellUnitPriceForAllProducts;
	}

	/**
	 * @param agreeEdgewellUnitPriceForAllProducts
	 *           the agreeEdgewellUnitPriceForAllProducts to set
	 */
	public void setAgreeEdgewellUnitPriceForAllProducts(final boolean agreeEdgewellUnitPriceForAllProducts)
	{
		this.agreeEdgewellUnitPriceForAllProducts = agreeEdgewellUnitPriceForAllProducts;
	}

}
