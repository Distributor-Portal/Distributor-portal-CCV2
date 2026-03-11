/**
 *
 */
package com.energizer.storefront.forms;

import java.util.List;


/**
 * @author m9005673
 *
 */
public class ExcelUploadForm
{
	private List<String> shippingPoint;
	private List<String> shippingPointLocation;

	/**
	 * @return the shippingPoint
	 */
	public List<String> getShippingPoint()
	{
		return shippingPoint;
	}

	/**
	 * @param shippingPoint
	 *                         the shippingPoint to set
	 */
	public void setShippingPoint(final List<String> shippingPoint)
	{
		this.shippingPoint = shippingPoint;
	}

	/**
	 * @return the shippingPointLocation
	 */

	public List<String> getShippingPointLocation()
	{
		return shippingPointLocation;
	}

	/**
	 * @param shippingPointLocation
	 *                                 the shippingPointLocation to set
	 */
	public void setShippingPointLocation(final List<String> shippingPointLocation)
	{
		this.shippingPointLocation = shippingPointLocation;
	}

}
