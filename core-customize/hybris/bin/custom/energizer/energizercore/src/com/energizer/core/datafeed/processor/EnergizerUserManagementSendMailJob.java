/**
 *
 */
package com.energizer.core.datafeed.processor;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.constants.B2BConstants;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.b2bacceleratorfacades.company.CompanyB2BCommerceFacade;
import de.hybris.platform.b2bcommercefacades.company.B2BUnitFacade;
import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BEmployeeModel;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.services.b2bemployee.EnergizerB2BEmployeeService;
import com.energizer.services.b2bemployee.impl.DefaultB2BEmployeeAccountService;


/**
 * @author Ravikiran Pise
 * @author m9005673/selvaraja savarimuthu
 */
public class EnergizerUserManagementSendMailJob extends AbstractJobPerformable<EnergizerCronJobModel>
{
	Logger LOG = Logger.getLogger(EnergizerUserManagementSendMailJob.class);

	@Resource(name = "customerAccountService")
	private CustomerAccountService customerAccountService;
	private final static String DEFAULT_PASSWORD = "energizer.default.password";
	@Resource(name = "userService")
	private UserService userService;
	private final StringBuilder message = new StringBuilder();
	@Resource(name = "b2bCommerceUserService")
	private B2BCommerceUserService b2bCommerceUserService;
	@Resource
	I18NService i18nService;
	@Resource(name = "b2bCommerceUnitService")
	private B2BCommerceUnitService b2BCommerceUnitService;
	public static final String EMAIL_REPLY_TO = "mail.smtp.user";
	@SuppressWarnings("rawtypes")
	@Resource(name = "defaultB2BUnitService")
	private B2BUnitService defaultB2BUnitService;

	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;
	@Resource(name = "b2BCustomerConverter")
	private Converter<B2BCustomerModel, CustomerData> b2BCustomerConverter;
	@Resource(name = "baseMessageSource")
	private ReloadableResourceBundleMessageSource messageSource;
	@Resource
	EmailService emailService;
	@Resource
	CompanyB2BCommerceFacade b2bCommerceFacade;
	private String subject;

	/*
	 * @Resource B2BCommerceUnitFacade b2bCommerceUnitFacade;
	 */

	@Resource(name = "b2bUnitFacade")
	B2BUnitFacade b2bCommerceUnitFacade;
	@Resource(name = "defaultEnergizerB2BEmployeeService")
	private EnergizerB2BEmployeeService defaultEnergizerB2BEmployeeService;

	@Resource(name = "defaultB2BEmployeeAccountService")
	private DefaultB2BEmployeeAccountService defaultB2BEmployeeAccountService;

	private CronJobService cronJobService;

	/**
	 * @return the cronJobService
	 */
	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	/**
	 * @param cronJobService
	 *                          the cronJobService to set
	 */
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	// Added by Soma - To abort those cronjobs during run time that take more time for processing.
	@Override
	public boolean isAbortable()
	{
		return true;
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel )
	 */
	@Override
	@SuppressWarnings("unchecked")
	public PerformResult perform(final EnergizerCronJobModel cronjob)
	{
		PerformResult result = null;
		final String userType = cronjob.getUserType();
		try
		{
			if (null != userType && userType.equals(EnergizerCoreConstants.CUSTOMER))
			{
				final Set<EnergizerB2BCustomerModel> b2bCustomerModels = defaultB2BUnitService.getAllUserGroupMembersForType(
						userService.getUserGroupForUID(B2BConstants.B2BADMINGROUP), EnergizerB2BCustomerModel.class);
				int currentRegistrationCount = 0;
				int alreadyRegisteredCount = 0;
				final String password = Config.getParameter(DEFAULT_PASSWORD);
				for (final EnergizerB2BCustomerModel customerModel : b2bCustomerModels)
				{
					if (null == customerModel.getRegistrationEmailFlag())
					{
						b2BCustomerConverter.convert(customerModel);
						customerModel.setRegistrationEmailFlag(Boolean.TRUE);
						customerAccountService.register(customerModel, password);
						currentRegistrationCount += 1;
					}
					if (null != customerModel.getRegistrationEmailFlag() && customerModel.getRegistrationEmailFlag())
					{
						alreadyRegisteredCount += 1;

					}

					// If the cronjob abort is requested, then perform clean up and return the PerformResult
					this.modelService.refresh(cronjob);
					if (clearAbortRequestedIfNeeded(cronjob))
					{
						LOG.info(cronjob.getRegion() + " : Cronjob is ABORTED while performing ...");
						//abort the job
						cronJobService.requestAbortCronJob(cronjob);

						return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
					}

				}
				//LOG.info("Customers already registered: " + alreadyRegisteredCount);
				//LOG.info("Customers Registration Email Sent to: " + currentRegistrationCount);
			}
			else if (null != userType && userType.equals(EnergizerCoreConstants.EMPLOYEE))
			{
				final List<EnergizerB2BEmployeeModel> b2bEmployeeModels = defaultEnergizerB2BEmployeeService
						.getEnergizerB2BEmployeeList();
				int currentRegistrationCount = 0;
				int alreadyRegisteredCount = 0;
				for (final EnergizerB2BEmployeeModel employeeModel : b2bEmployeeModels)
				{
					if (null == employeeModel.getRegistrationEmailFlag()
							|| (null != employeeModel.getRegistrationEmailFlag() && !employeeModel.getRegistrationEmailFlag()))
					{
						employeeModel.setRegistrationEmailFlag(Boolean.TRUE);
						defaultB2BEmployeeAccountService.register(employeeModel);
						LOG.info("Registration email sent to the sales rep :: " + employeeModel.getUid() + " , email ID :: "
								+ employeeModel.getEmail());
						currentRegistrationCount += 1;
					}
					if (null != employeeModel.getRegistrationEmailFlag() && employeeModel.getRegistrationEmailFlag())
					{
						alreadyRegisteredCount += 1;

					}

					// If the cronjob abort is requested, then perform clean up and return the PerformResult
					this.modelService.refresh(cronjob);
					if (clearAbortRequestedIfNeeded(cronjob))
					{
						LOG.info(cronjob.getRegion() + " : Cronjob is ABORTED while performing ...");
						//abort the job
						cronJobService.requestAbortCronJob(cronjob);

						return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
					}
				}
				//LOG.info("Sales Reps already registered: " + alreadyRegisteredCount);
				//LOG.info("Sales Rep Registration Email Sent to: " + currentRegistrationCount);
			}
			else
			{
				LOG.info("No userType found !!");
			}

			/*
			 * final List<String> list = new ArrayList<String>(); list.add(Config.getParameter("energizer.default.admin.emailid"));
			 * list.add(Config.getParameter("energizer.default.helpdesk.emailid")); sendEmail(list);
			 */
			result = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		catch (final Exception execption)
		{
			result = new PerformResult(CronJobResult.ERROR, CronJobStatus.UNKNOWN);
			LOG.error("Error : " + execption.getCause());
		}
		return result;
	}

	private void createEmailBody()
	{
		final Locale locale = i18nService.getCurrentLocale();
		setSubject("Error: ");
		message.append(messageSource.getMessage("text.error.message.email.template.section7", new Object[]
		{ getSubject() }, locale));
		final String actionmsg = messageSource.getMessage("text.error.message.email.template.section10", null, locale);
		int lineNumber = 0;
		final List<String> unitList = getAllUnitsOfOrganization();
		for (final String unitName : unitList)
		{
			lineNumber++;
			message.append(messageSource.getMessage("text.error.message.email.template.section8", new Object[]
			{ lineNumber, unitName, actionmsg }, locale));
		}
		message.append(messageSource.getMessage("text.error.message.email.template.section9", null, locale));
		message.append("\n");
	}

	private void sendEmail(final List<String> toAddresses)
	{
		try
		{
			createEmailBody();
			EmailMessageModel emailMessageModel = null;
			EmailAddressModel emailAddress = null;
			final List<EmailAddressModel> toAddressModels = new ArrayList<EmailAddressModel>();
			for (final String toAddress : toAddresses)
			{
				emailAddress = emailService.getOrCreateEmailAddressForEmail(toAddress, "Error Message");
				toAddressModels.add(emailAddress);
			}
			emailMessageModel = emailService.createEmailMessage(toAddressModels, null, null, emailAddress,
					Config.getParameter(EMAIL_REPLY_TO), getSubject(), message.toString(), null);
			emailService.send(emailMessageModel);
			message.setLength(0);
		}
		catch (final Exception e)
		{
			LOG.error("Exception in Mail Errors", e);
		}
	}

	private List<String> getAllUnitsOfOrganization()
	{
		final List<String> getAllActiveUnits = b2bCommerceUnitFacade.getAllActiveUnitsOfOrganization();
		final List<String> details = new ArrayList<String>();
		for (final String unitName : getAllActiveUnits)
		{
			final B2BUnitData unit = b2bCommerceFacade.getUnitForUid(unitName);
			final Collection<CustomerData> collections = unit.getAdministrators();
			if (collections == null)
			{
				details.add(unit.getUid());
			}
		}
		return details;
	}

	/**
	 * @return the subject
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	 * @param subject
	 *                   the subject to set
	 */
	public void setSubject(final String subject)
	{
		this.subject = subject;
	}

}
