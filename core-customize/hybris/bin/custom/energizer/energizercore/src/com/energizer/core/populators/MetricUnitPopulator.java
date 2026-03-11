/**
 * 
 */
package com.energizer.core.populators;

import de.hybris.platform.converters.Populator;

import com.energizer.core.data.MetricUnitData;
import com.energizer.core.model.MetricUnitModel;



/**
 * @author M1023097
 * 
 */
public class MetricUnitPopulator implements Populator<MetricUnitModel, MetricUnitData>
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.converters.Populator#populate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void populate(final MetricUnitModel source, final MetricUnitData target)
	{
		target.setMeasurement(source.getMeasurement());
		target.setMeasuringUnits(source.getMeasuringUnits());
	}

}
