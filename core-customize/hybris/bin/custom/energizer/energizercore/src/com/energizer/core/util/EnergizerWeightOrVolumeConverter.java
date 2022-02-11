/**
 * 
 */
package com.energizer.core.util;

import java.math.BigDecimal;


/**
 * @author Bivash Pandit
 * 
 */
public class EnergizerWeightOrVolumeConverter
{
	/**
	 * Cubic cm - CCM = 0.0000001 M3, Meter cube - M3 = 1 M3. Cubic decimeter - CD3 = 0.001 M3, HectoLitre - HL = 0.1 M3,
	 * Cubic ft - FT3 = 0.0283168 M3
	 * 
	 * KG = 1KG, G = 0.001 KG, LB = 0.453592 KG
	 */

	private static String CUBIC_CM = "CCM";
	private static String CUBIC_DECEMETER = "CDM";
	private static String HECTO_LITRE = "HL";
	private static String CUBIC_FEET = "FT3";
	private static String GRAM = "G";
	private static String LB = "LB";
	private static String KG = "KG";
	private static String METER_CUBE = "M3";


	public static BigDecimal getConversionValue(final String unit, final BigDecimal value)
	{
		BigDecimal convertedValue = new BigDecimal(1);
		if (unit.equalsIgnoreCase(CUBIC_CM))
		{
			convertedValue = convertCubicCMtoMeterCube(value);
		}
		else if (unit.equalsIgnoreCase(CUBIC_DECEMETER))
		{
			convertedValue = convertCubicDecimeterToMeterCube(value);
		}
		else if (unit.equalsIgnoreCase(HECTO_LITRE))
		{
			convertedValue = convertHectoLitreToMeterCube(value);
		}
		else if (unit.equalsIgnoreCase(CUBIC_FEET))
		{
			convertedValue = convertCubicFtToMeterCube(value);
		}
		else if (unit.equalsIgnoreCase(GRAM))
		{
			convertedValue = convertGramToKG(value);
		}
		else if (unit.equalsIgnoreCase(LB))
		{
			convertedValue = convertLBtoKG(value);
		}
		else if (unit.equalsIgnoreCase(KG))
		{
			convertedValue = value;
		}
		else if (unit.equalsIgnoreCase(METER_CUBE))
		{
			convertedValue = value;
		}

		return convertedValue;
	}


	private static BigDecimal convertGramToKG(final BigDecimal gram)
	{
		final BigDecimal valueInKg = gram.multiply(new BigDecimal(.001));
		return valueInKg;
	}

	private static BigDecimal convertLBtoKG(final BigDecimal lb)
	{
		final BigDecimal valueInKg = lb.multiply(new BigDecimal(0.453592));
		return valueInKg;

	}

	private static BigDecimal convertCubicCMtoMeterCube(final BigDecimal CubicCM)
	{
		final BigDecimal valueInMeterCube = CubicCM.multiply(new BigDecimal(0.000001));
		return valueInMeterCube;

	}

	private static BigDecimal convertCubicDecimeterToMeterCube(final BigDecimal cubicDecimeter)
	{
		final BigDecimal valueInMeterCube = cubicDecimeter.multiply(new BigDecimal(0.001));
		return valueInMeterCube;
	}

	private static BigDecimal convertHectoLitreToMeterCube(final BigDecimal hectoLitre)
	{
		final BigDecimal valueInMeterCube = hectoLitre.multiply(new BigDecimal(0.01));
		return valueInMeterCube;
	}

	private static BigDecimal convertCubicFtToMeterCube(final BigDecimal cubicFt)
	{
		final BigDecimal valueInMeterCube = cubicFt.multiply(new BigDecimal(0.0283168));
		return valueInMeterCube;
	}
}
