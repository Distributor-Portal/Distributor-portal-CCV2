/**
 *
 */
package com.energizer.core.util;

/**
 * @author M1028886
 * 
 */
public class EnergizerProductPalletHeight implements Comparable
{

	private String erpMaterialId;
	private double palletHeight;

	private int quantity;
	private String orderedUOM;
	private boolean isVirtualPallet;
	private String calculatedUOM;


	/**
	 * @return the erpMaterialId
	 */
	public String getErpMaterialId()
	{
		return erpMaterialId;
	}

	/**
	 * @param erpMaterialId
	 *           the erpMaterialId to set
	 */
	public void setErpMaterialId(final String erpMaterialId)
	{
		this.erpMaterialId = erpMaterialId;
	}

	/**
	 * @return the palletHeight
	 */
	public double getPalletHeight()
	{
		return palletHeight;
	}

	/**
	 * @param palletHeight
	 *           the palletHeight to set
	 */
	public void setPalletHeight(final double palletHeight)
	{
		this.palletHeight = palletHeight;
	}

	@Override
	public int compareTo(final Object o)
	{

		final double comparePalletHeight = ((EnergizerProductPalletHeight) o).getPalletHeight();
		return (int) ((comparePalletHeight * 1000) - (this.palletHeight * 1000));
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity()
	{
		return quantity;
	}

	/**
	 * @param quantity
	 *           the quantity to set
	 */
	public void setQuantity(final int quantity)
	{
		this.quantity = quantity;
	}

	/**
	 * @return the orderedUOM
	 */
	public String getOrderedUOM()
	{
		return orderedUOM;
	}

	/**
	 * @param orderedUOM
	 *           the orderedUOM to set
	 */
	public void setOrderedUOM(final String orderedUOM)
	{
		this.orderedUOM = orderedUOM;
	}

	/**
	 * @return the isVirtualPallet
	 */
	public boolean isVirtualPallet()
	{
		return isVirtualPallet;
	}

	/**
	 * @param isVirtualPallet
	 *           the isVirtualPallet to set
	 */
	public void setIsVirtualPallet(final boolean isVirtualPallet)
	{
		this.isVirtualPallet = isVirtualPallet;
	}

	/**
	 * @return the calculatedUOM
	 */
	public String getCalculatedUOM()
	{
		return calculatedUOM;
	}

	/**
	 * @param calculatedUOM
	 *           the calculatedUOM to set
	 */
	public void setCalculatedUOM(final String calculatedUOM)
	{
		this.calculatedUOM = calculatedUOM;
	}
}
