/**
 *
 */
package com.energizer.core.business.impl;

import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.localization.Localization;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.constants.EnergizerCoreConstants;


/**
 * @author M9005672
 *
 */
@Component("shippingPointsEnergizerBusinessRuleValidator")
public class ShippingPointsEnergizerBusinessRuleValidator extends AbstractEnergizerOrderEntryBusinessRulesValidator
{
	private static final Logger LOG = Logger.getLogger(ShippingPointsEnergizerBusinessRuleValidator.class.getName());

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource
	private UserService userService;

	@Override
	public void validate(final OrderEntryData orderEntryData)
	{
		LOG.info("Starting the shippingPoint validation process...!!! ");

		final String shippingPointNo = orderEntryData.getShippingPoint();
		final String referenceShippingPoint = orderEntryData.getReferenceShippingPoint();
		final String referenceShippingPointLocation = orderEntryData.getReferenceShippingPointLocation();
		final String shippingPointLocation = orderEntryData.getShippingPointLocation();


		/*
		 * final String referenceShippingPointLocation = orderEntryData.getReferenceShippingPointLocation(); final String
		 * shippingPointLocation = orderEntryData.getShippingPointLocation();
		 */


		final String productCode = orderEntryData.getProduct().getCode();
		final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");
		final String PERSONALCARE = "personalCare";

		LOG.info("The shippingPointNo of product : " + productCode + " is " + shippingPointNo);
		LOG.info("The shippingPointNo of the product in the cart: " + referenceShippingPoint);

		if (hasErrors())
		{
			errors.clear();
		}

		/***** LATAM & WESELL - START ******/
		/***** Compare Plants *****/
		if (getSiteUid().equalsIgnoreCase(PERSONALCARE) && null == shippingPointNo)
		{
			LOG.info("LATAM/WESELL : The shipping point of this product is not found!");
			final BusinessRuleError error = new BusinessRuleError();
			error.setMessage(Localization.getLocalizedString(EnergizerCoreConstants.SHIPPING_POINT_NOT_FOUND, new Object[]
			{ productCode }));
			addError(error);
		}
		else if (getSiteUid().equalsIgnoreCase(PERSONALCARE) && null != referenceShippingPoint && !referenceShippingPoint.isEmpty()
				&& !shippingPointNo.equalsIgnoreCase(referenceShippingPoint))
		{
			LOG.info("LATAM/WESELL : The shipping point of this product must be similar to that of other product(s) in Cart!!!");
			final BusinessRuleError error = new BusinessRuleError();
			error.setMessage(Localization.getLocalizedString(EnergizerCoreConstants.SHIPPING_POINT_MISMATCH, new Object[]
			{ productCode }));
			addError(error);
		}
		/***** LATAM & WESELL - END ******/

		/***** EMEA - START ******/
		/***** Compare Shipping Point Locations *****/
		if (getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA) && null == shippingPointLocation)
		{
			LOG.info("EMEA : The shipping point location of this product is not found!");
			final BusinessRuleError error = new BusinessRuleError();
			error.setMessage(Localization.getLocalizedString(EnergizerCoreConstants.SHIPPING_POINT_LOCATION_NOT_FOUND, new Object[]
			{ productCode }));
			addError(error);
		}
		else if (getSiteUid().equalsIgnoreCase(PERSONALCARE_EMEA) && null != shippingPointLocation
				&& null != referenceShippingPointLocation && !shippingPointLocation.equalsIgnoreCase(referenceShippingPointLocation))
		{
			LOG.info("EMEA : The shipping point location of this product must be similar to that of other product(s) in Cart!!!");
			final BusinessRuleError error = new BusinessRuleError();
			error.setMessage(Localization.getLocalizedString(EnergizerCoreConstants.SHIPPING_LOCATION_MISMATCH, new Object[]
			{ productCode }));
			addError(error);
		}
		/***** EMEA - END ******/
	}

	/**
	 * @param string
	 * @return
	 */
	public String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}
}

