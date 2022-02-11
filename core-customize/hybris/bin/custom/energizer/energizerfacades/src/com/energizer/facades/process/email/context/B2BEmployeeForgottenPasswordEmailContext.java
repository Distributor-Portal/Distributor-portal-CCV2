/**
 *
 */
package com.energizer.facades.process.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.commerceservices.model.process.EmployeeForgottenPasswordProcessModel;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.facades.employee.data.EnergizerB2BEmployeeData;


/**
 * @author Srivenkata_N
 *
 */
public class B2BEmployeeForgottenPasswordEmailContext extends CustomerEmailContext
{
	private static final Logger LOG = Logger.getLogger(B2BEmployeeForgottenPasswordEmailContext.class);
	private Converter<EnergizerB2BEmployeeModel, EnergizerB2BEmployeeData> energizerB2BEmployeeConverter;
	private EnergizerB2BEmployeeData employeeData;

	/**
	 * set the DEFAULT_TIMEOUT_IN_MINUTES
	 */
	private static final int DEFAULT_TIMEOUT_IN_MINUTES = 30;
	private static final String EXP_IN_MIN = "b2bemployeeforgottenPassword.emailContext.expiresInMinutes";
	/**
	 * This is to set the password expires time done through the local.properties
	 **/
	private int expiresInMinutes = 0;


	private String token;
	@Autowired
	private ConfigurationService configurationService;



	@Override
	public void init(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(storeFrontCustomerProcessModel, emailPageModel);
		if (storeFrontCustomerProcessModel instanceof EmployeeForgottenPasswordProcessModel)
		{
			setToken(((EmployeeForgottenPasswordProcessModel) storeFrontCustomerProcessModel).getToken());
		}

		if (null != getEmployee((EmployeeForgottenPasswordProcessModel) storeFrontCustomerProcessModel))
		{
		employeeData = getEnergizerB2BEmployeeConverter()
				.convert(getEmployee((EmployeeForgottenPasswordProcessModel) storeFrontCustomerProcessModel));
		}
		else
		{
			LOG.info("Employee Model is null ...");
		}

		final EnergizerB2BEmployeeData employeeData = getEmployeeData();

		if (employeeData != null)
		{
			//put(TITLE, (employeeData.getName()));
			put(DISPLAY_NAME, employeeData.getName());
			put(EMAIL, employeeData.getEmail());
		}

	}

	public int getExpiresInMinutes()
	{
		expiresInMinutes = configurationService.getConfiguration()
				.getBigInteger(EXP_IN_MIN, BigInteger.valueOf(DEFAULT_TIMEOUT_IN_MINUTES)).intValue();
		return expiresInMinutes;
	}

	public void setExpiresInMinutes(final int expiresInMinutes)
	{
		this.expiresInMinutes = expiresInMinutes;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(final String token)
	{
		this.token = token;
	}

	public String getURLEncodedToken() throws UnsupportedEncodingException
	{
		return URLEncoder.encode(token, "UTF-8");
	}

	public String getRequestResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), false,
				"/login/pw/request");
	}

	@Override
	public String getSecureRequestResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/login/pw/request-page", "uid=" + getEmployeeData().getUid());
	}

	public String getResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), false,
				"/login/pw/change", "token=" + getURLEncodedToken());
	}

	public String getSecureResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/login/pw/change", "token=" + getURLEncodedToken());
	}

	public String getDisplayResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), false,
				"/my-account/update-password");
	}

	public String getDisplaySecureResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/my-account/update-password");
	}

	protected EnergizerB2BEmployeeModel getEmployee(
			final EmployeeForgottenPasswordProcessModel employeeForgottenPasswordProcessModel)
	{
		return employeeForgottenPasswordProcessModel.getEmployee();
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


}
