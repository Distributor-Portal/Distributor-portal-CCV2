/**
 *
 */
package com.energizer.core.util;

import de.hybris.platform.acceleratorservices.email.impl.DefaultEmailGenerationService;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BPermissionResultModel;
import de.hybris.platform.b2b.model.B2BUserGroupModel;
import de.hybris.platform.b2bcommercefacades.company.B2BUnitFacade;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;




/**
 * @author m1023278
 *
 */
public class EnergizerEmailGenerationService extends DefaultEmailGenerationService
{
	private static final Logger LOG = Logger.getLogger(EnergizerEmailGenerationService.class);

	private String salesPersonEmailId;

	private String displayName;

	private String enviorment;

	B2BUserGroupModel b2bUserGroupModel;

	private List<B2BCustomerModel> b2bCustomerModelList;
	private Set<B2BCustomerModel> b2bCustomerModels;
	private B2BCustomerModel orderApprover;

	private List<String> emailList;

	private Set<String> emailSet;

	private List<String> emailCCList;
	@Resource
	ModelService modelService;

	@Resource
	private B2BCommerceUnitService b2bCommerceUnitService;

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Resource
	private ConfigurationService configurationService;

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	OrderModel order;
	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.acceleratorservices.email.impl.DefaultEmailGenerationService#generate(de.hybris.platform.
	 * processengine.model.BusinessProcessModel, de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel)
	 */

	@Resource(name = "b2bUnitFacade")
	protected B2BUnitFacade b2bUnitFacade;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Override
	public EmailMessageModel generate(final BusinessProcessModel businessProcessModel, final EmailPageModel emailPageModel)
			throws RuntimeException
	{

		if (businessProcessModel instanceof OrderProcessModel)
		{
			order = ((OrderProcessModel) businessProcessModel).getOrder();
			final String salesPersonEmailId = order.getDeliveryAddress().getSalesPersonEmailId();
			final String displayName = order.getDeliveryAddress().getDisplayName();

			//Added code changes for WeSell Implementation - START
			//Add 'Sales Person Email ID ' ONLY for Non Sales Rep login
			if (null != order.getPlacedBySalesRep() && !order.getPlacedBySalesRep())
			{
				setSalesPersonEmailId(salesPersonEmailId);
				setDisplayName(displayName);
			}
			//Added code changes for WeSell Implementation - END

			emailSet = new HashSet<String>();
			emailList = new ArrayList<String>();
			emailCCList = new ArrayList<String>();
			b2bCustomerModels = new HashSet<B2BCustomerModel>();
			b2bCustomerModels = order.getB2bUnit().getApprovers();


			final List<B2BPermissionResultModel> b2bPermissionResultModels = (List<B2BPermissionResultModel>) order
					.getPermissionResults();
			for (final B2BPermissionResultModel b2bPermissionResultModel : b2bPermissionResultModels)
			{
				if (emailSet.size() > 0)
				{


					emailSet.add(b2bPermissionResultModel.getApprover().getEmail());


				}
				else
				{
					emailSet.add(b2bPermissionResultModel.getApprover().getEmail());
				}

				emailSet.add(b2bPermissionResultModel.getApprover().getEmail());
			}
			emailList.addAll(emailSet);
			b2bCustomerModelList = new ArrayList<B2BCustomerModel>();
			orderApprover = ((OrderProcessModel) businessProcessModel).getOrder().getOrderApprover();
			b2bCustomerModelList.addAll(b2bCustomerModels);

			//Added code changes for WeSell Implementation - START
			//Add 'cc' ONLY for Non Sales Rep login
			if (null != order.getPlacedBySalesRep() && !order.getPlacedBySalesRep())
			{
				if (null != b2bCustomerModelList && b2bCustomerModelList.size() > 0)
				{
					for (final B2BCustomerModel customer : b2bCustomerModelList)
					{
						emailCCList.add(customer.getEmail());
					}
				}
			}
			//Added code changes for WeSell Implementation - END
		}
		return super.generate(businessProcessModel, emailPageModel);
	}

	@Override
	protected EmailMessageModel createEmailMessage(final String emailSubject, final String emailBody,
			final AbstractEmailContext<BusinessProcessModel> emailContext)
	{
		EmailMessageModel emailMessageModel;
		EmailAddressModel ccAddress = null;
		EmailAddressModel ccIcsAddress1 = null;
		enviorment = configurationService.getConfiguration().getString("mail.enviorment");
		LOG.info("THE MAIL ENVIORMENT IS : " + enviorment);
		if (emailSubject.indexOf("approved") != -1)
		{
			//Added if condition code changes for WeSell Implementation - START
			if (null != order.getPlacedBySalesRep() && !order.getPlacedBySalesRep())
			{
				ccAddress = getEmailService().getOrCreateEmailAddressForEmail(getSalesPersonEmailId(), getSalesPersonEmailId());
				ccAddress.setEmailAddress(getSalesPersonEmailId());
				ccAddress.setDisplayName(getDisplayName());
			}
			//Added code changes for WeSell Implementation - END

			emailMessageModel = super.createEmailMessage(emailSubject, emailBody, emailContext);
			final List<EmailAddressModel> ccEmails = new ArrayList<EmailAddressModel>();

			final String currentSiteId = this.cmsSiteService.getCurrentSite().getUid();

			final String salesPersonId = getSalesPersonId();
			final String displayName = "Edgewell France Customer Service";

			final String ICS_EMAILID = "ICS.EMAILID." + currentSiteId;
			final String ICS_DISPLAYNAME = "ICS.DISPLAYNAME." + currentSiteId;

			LOG.info("THE ICS PROP VALS ARE" + "\t" + configurationService.getConfiguration().getString(ICS_EMAILID) + "\t"
					+ configurationService.getConfiguration().getString(ICS_DISPLAYNAME));

			//Added if condition code changes for WeSell Implementation - START
			if (null != order.getPlacedBySalesRep() && !order.getPlacedBySalesRep())
			{
				/*-ccIcsAddress1 = getEmailService().getOrCreateEmailAddressForEmail(getConfigValue(ICS_EMAILID),
						getConfigValue(ICS_DISPLAYNAME));*/
				// Added code changes by Pavani to send out order confirmation email to different Customer Service Desk(s) for EMEA
				if (currentSiteId.contains("EMEA"))
				{

					ccIcsAddress1 = getEmailService().getOrCreateEmailAddressForEmail(salesPersonId, displayName);
				}
				else
				{
					ccIcsAddress1 = getEmailService().getOrCreateEmailAddressForEmail(getConfigValue(ICS_EMAILID),
							getConfigValue(ICS_DISPLAYNAME));
				}

				ccIcsAddress1.setEmailAddress(getConfigValue(ICS_EMAILID));
				ccIcsAddress1.setDisplayName(getConfigValue(ICS_DISPLAYNAME));
			}
			//Added code changes for WeSell Implementation - END

			if (null != ccAddress)
			{
				ccEmails.add(ccAddress);
			}

			if (null != ccIcsAddress1)
			{
				ccEmails.add(ccIcsAddress1);
			}

			//Added Selected Employee(The One who is on Vacation) To the cc list - START
			final List<EmailAddressModel> toEmails = new ArrayList<EmailAddressModel>();
			if (null != order.getPlacedBySalesRep() && order.getPlacedBySalesRep())
			{
				if (null != order.getSelectedEmpUid() && !order.getSelectedEmpUid().equalsIgnoreCase(order.getSalesRepUid()))
				{
					final EmailAddressModel selectedCCAddress = getEmailService()
							.getOrCreateEmailAddressForEmail(order.getSelectedEmpEmailID(), order.getSelectedEmpName());
					ccEmails.add(selectedCCAddress);
				}
			}
			//Added Selected Employee(The One who is on Vacation) To the cc list - END
			final EmailAddressModel toAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getToEmail(),
					emailContext.getToDisplayName());
			toEmails.add(toAddress);
			final EmailAddressModel fromAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getFromEmail(),
					emailContext.getFromDisplayName());
			return getEmailService().createEmailMessage(toEmails, ccEmails, new ArrayList<EmailAddressModel>(), fromAddress,
					emailContext.getFromEmail(), emailSubject, emailBody, null);

		} //order pending approval email : add List of reviewers in cc address field
		else if (emailSubject.indexOf("Edgewell Reference Number Pending Approval") != -1)
		{
			final List<EmailAddressModel> toEmails = new ArrayList<EmailAddressModel>();
			if (null != emailList && emailList.size() > 0)
			{
				// get the list of approver emails and search for the same in the EmailAddress table by flexiblesearchquery,
				//if emailAddress is found then that is the cc address and set the email id
				//if no emailaddress create the new EmailAddressModel and make it as cc address
				//add the cc address to list of EmailAddressModel
				for (int i = 0; i < emailList.size(); i++)
				{
					final String emailAddress = emailList.get(i);
					final EmailAddressModel toAddress = getEmailService().getOrCreateEmailAddressForEmail(emailAddress, emailAddress);
					toAddress.setEmailAddress(emailAddress);
					toAddress.setDisplayName(getDisplayName());

					if (null != toAddress)
					{
						toEmails.add(toAddress);
					}
				} //end of for loop
				  //emailList = null;
				final List<EmailAddressModel> ccEmails = new ArrayList<EmailAddressModel>();
				if (null != emailCCList && emailCCList.size() > 0)
				{
					for (int i = 0; i < emailCCList.size(); i++)
					{
						final String emailAddress = emailCCList.get(i);
						ccAddress = getEmailService().getOrCreateEmailAddressForEmail(emailAddress, emailAddress);
						ccAddress.setEmailAddress(emailAddress);
						ccAddress.setDisplayName(getDisplayName());
						if (null != ccAddress)
						{
							ccEmails.add(ccAddress);
						}
					}
				}

				final EmailAddressModel fromAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getFromEmail(),
						emailContext.getFromDisplayName());
				return getEmailService().createEmailMessage(toEmails, ccEmails, new ArrayList<EmailAddressModel>(), fromAddress,
						emailContext.getFromEmail(), emailSubject, emailBody, null);
			} //end of if loop
		} //end of order pending approval email : add List of reviewers in cc address field
		else if (emailSubject.indexOf("Energizer Approval Failed for Reference no") != -1)
		{
			final List<EmailAddressModel> toEmails = new ArrayList<EmailAddressModel>();
			final EmailAddressModel toAddress = getEmailService().getOrCreateEmailAddressForEmail(orderApprover.getEmail(),
					orderApprover.getEmail());
			toEmails.add(toAddress);
			final EmailAddressModel fromAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getFromEmail(),
					emailContext.getFromDisplayName());
			return getEmailService().createEmailMessage(toEmails, new ArrayList<EmailAddressModel>(),
					new ArrayList<EmailAddressModel>(), fromAddress, emailContext.getFromEmail(), emailSubject, emailBody, null);
		}
		else
		{

			return super.createEmailMessage(emailSubject, emailBody, emailContext);
		}
		return super.createEmailMessage(emailSubject, emailBody, emailContext);
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * @param displayName
	 *           the displayName to set
	 */
	public void setDisplayName(final String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * @return the salesPersonEmailId
	 */
	public String getSalesPersonEmailId()
	{
		return salesPersonEmailId;
	}

	/**
	 * @param salesPersonEmailId
	 *           the salesPersonEmailId to set
	 */
	public void setSalesPersonEmailId(final String salesPersonEmailId)
	{
		this.salesPersonEmailId = salesPersonEmailId;
	}

	public String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}

	private String getSalesPersonId()
	{
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);

		return b2bUnit.getSalesPersonEmailId();

	}
}
