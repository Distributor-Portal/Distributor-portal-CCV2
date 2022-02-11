/**
 * 
 */
package com.energizer.core.util;



/**
 * @author BivashPandit
 * 
 */
public class ContainerData
{
	public String containerType = "";

	public double percentVolumeUses;

	public double percentWeightUses;

	/**
	 * @return the containerType
	 */
	public String getContainerType()
	{
		return containerType;
	}

	/**
	 * @param containerType
	 *           the containerType to set
	 */
	public void setContainerType(final String containerType)
	{
		this.containerType = containerType;
	}

	/**
	 * @return the percentVolumeUses
	 */
	public double getPercentVolumeUses()
	{
		return percentVolumeUses;
	}

	/**
	 * @param percentVolumeUses
	 *           the percentVolumeUses to set
	 */
	public void setPercentVolumeUses(final double percentVolumeUses)
	{
		this.percentVolumeUses = percentVolumeUses;
	}

	/**
	 * @return the percentWeightUses
	 */
	public double getPercentWeightUses()
	{
		return percentWeightUses;
	}

	/**
	 * @param percentWeightUses
	 *           the percentWeightUses to set
	 */
	public void setPercentWeightUses(final double percentWeightUses)
	{
		this.percentWeightUses = percentWeightUses;
	}


}
