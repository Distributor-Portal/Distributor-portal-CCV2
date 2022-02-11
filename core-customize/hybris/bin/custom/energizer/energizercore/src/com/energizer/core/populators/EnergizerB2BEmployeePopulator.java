/**
 *
 */
package com.energizer.core.populators;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;

import com.energizer.core.data.EnergizerB2BUnitData;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.employee.data.EnergizerB2BEmployeeData;
import com.energizer.services.b2bemployee.EnergizerB2BEmployeeService;


/**
 * @author kaki.rajasekhar
 *
 */
public class EnergizerB2BEmployeePopulator implements Populator<EnergizerB2BEmployeeModel, EnergizerB2BEmployeeData>
{
	@Resource(name = "userService")
	private UserService userService;
	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource
	private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService;

	private Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> energizerB2BUnitConverter;

	@Resource(name = "defaultEnergizerB2BEmployeeService")
	private EnergizerB2BEmployeeService defaultEnergizerB2BEmployeeService;


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.converters.Populator#populate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void populate(final EnergizerB2BEmployeeModel source, final EnergizerB2BEmployeeData target) throws ConversionException
	{
		target.setUid(source.getUid());
		target.setName(source.getName());
		target.setActive(source.getActive());
		target.setIsSalesRep(source.getIsSalesRep());
		target.setEmail(source.getEmail());
		target.setContactNumber(source.getContactNumber());
		target.setPriority(source.getPriority());
		target.setSalesRepCode(source.getSalesRepCode());
		target.setFirstName(source.getFirstName());
		target.setLastName(source.getLastName());
		target.setTitleCode(null != source.getTitle() ? source.getTitle().getCode() : null);
		target.setPasswordQuestion(source.getPasswordQuestion());
		target.setPasswordAnswer(source.getPasswordAnswer());
		final String hybrisUserId = userService.getCurrentUser().getUid();
		if (null != hybrisUserId && !hybrisUserId.equalsIgnoreCase("anonymous")
				&& ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
				&& null != (String) sessionService.getAttribute("b2bunitID")
				&& !((String) sessionService.getAttribute("b2bunitID")).isEmpty())
		{
			final EnergizerB2BUnitModel b2bUnitModel = (EnergizerB2BUnitModel) b2bUnitService
					.getUnitForUid((String) sessionService.getAttribute("b2bunitID"));
			target.setSelectedB2BUnit(getEnergizerB2BUnitConverter().convert(b2bUnitModel));
		}
		if (null != hybrisUserId && !hybrisUserId.equalsIgnoreCase("anonymous")
				&& ((boolean) sessionService.getAttribute("isSalesRepUserLoggedIn"))
				&& null != (String) sessionService.getAttribute("salesRepLogin")
				&& !((String) sessionService.getAttribute("salesRepLogin")).isEmpty())
		{
			final List<EnergizerB2BUnitModel> energizerB2BUnitList = defaultEnergizerB2BEmployeeService
					.getEnergizerB2BUnitList(((String) sessionService.getAttribute("salesRepLogin")).trim());
			if (null != energizerB2BUnitList && CollectionUtils.isNotEmpty(energizerB2BUnitList))
			{
				target.setB2bUnitList(energizerB2BUnitConverter.convertAll(energizerB2BUnitList));
			}
		}
		target.setSalesOrganisation(source.getSalesOrganisation());
		target.setCurrency(source.getCurrency());
	}

	/**
	 * @return the energizerB2BUnitConverter
	 */
	public Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> getEnergizerB2BUnitConverter()
	{
		return energizerB2BUnitConverter;
	}

	/**
	 * @param energizerB2BUnitConverter
	 *           the energizerB2BUnitConverter to set
	 */
	public void setEnergizerB2BUnitConverter(
			final Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> energizerB2BUnitConverter)
	{
		this.energizerB2BUnitConverter = energizerB2BUnitConverter;
	}

}
