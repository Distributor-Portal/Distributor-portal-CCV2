/**
 *
 */
package com.energizer.facades.process.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.commerceservices.model.process.EmployeeRegistrationProcessModel;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.facades.employee.data.EnergizerB2BEmployeeData;


/**
 * @author Srivenkata_N
 *
 */
public class B2BEmployeeRegistrationEmailContext extends CustomerEmailContext
{
	private static final Logger LOG = Logger.getLogger(B2BEmployeeRegistrationEmailContext.class);

	private Converter<EnergizerB2BEmployeeModel, EnergizerB2BEmployeeData> energizerB2BEmployeeConverter;
	private EnergizerB2BEmployeeData employeeData;

	protected ConfigurationService configurationService;

	@Override
	public String getSecureRequestResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/login/pw/reset-password", "uid=" + getEmployeeData().getUid());
	}


	@Override
	public void init(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(storeFrontCustomerProcessModel, emailPageModel);
		if (storeFrontCustomerProcessModel instanceof EmployeeRegistrationProcessModel)
		{
		if (null != getEmployee((EmployeeRegistrationProcessModel) storeFrontCustomerProcessModel))
		{
			employeeData = getEnergizerB2BEmployeeConverter()
					.convert(getEmployee((EmployeeRegistrationProcessModel) storeFrontCustomerProcessModel));
		}

		else
		{
			LOG.info("Employee Model is null ...");
		}

		}
		final EnergizerB2BEmployeeData employeeData = getEmployeeData();

		if (employeeData != null)
		{
			//put(TITLE, (employeeData.getName()));
			put(DISPLAY_NAME, employeeData.getName());
			put(EMAIL, employeeData.getEmail());
		}

	}

	protected EnergizerB2BEmployeeModel getEmployee(
			final EmployeeRegistrationProcessModel employeeRegistrationProcessModel)
	{
		return employeeRegistrationProcessModel.getEmployee();
	}

	/**
	 * @return the energizerB2BEmployeeConverter
	 */
	public Converter<EnergizerB2BEmployeeModel, EnergizerB2BEmployeeData> getEnergizerB2BEmployeeConverter()
	{
		return energizerB2BEmployeeConverter;
	}

	/**
	 * @param energizerB2BEmployeeConverter
	 *           the energizerB2BEmployeeConverter to set
	 */
	public void setEnergizerB2BEmployeeConverter(
			final Converter<EnergizerB2BEmployeeModel, EnergizerB2BEmployeeData> energizerB2BEmployeeConverter)
	{
		this.energizerB2BEmployeeConverter = energizerB2BEmployeeConverter;
	}

	/**
	 * @return the employeeData
	 */
	public EnergizerB2BEmployeeData getEmployeeData()
	{
		return employeeData;
	}

	/**
	 * @param employeeData
	 *           the employeeData to set
	 */
	public void setEmployeeData(final EnergizerB2BEmployeeData employeeData)
	{
		this.employeeData = employeeData;
	}


	@Override
	protected LanguageModel getEmailLanguage(final StoreFrontCustomerProcessModel businessProcessModel)
	{
		return businessProcessModel.getLanguage();
	}


}
