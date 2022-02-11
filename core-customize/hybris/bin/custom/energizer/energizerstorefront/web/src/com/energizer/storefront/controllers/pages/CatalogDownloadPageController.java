/**
 *
 */
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.storesession.StoreSessionFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.impex.ExportService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.facades.catalogdownload.EnergizerCatalogDownloadFacade;
import com.energizer.facades.order.impl.DefaultEnergizerB2BOrderHistoryFacade;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;


/**
 * @author M1028886
 *
 */
@Controller
@Scope("tenant")
@RequestMapping("/my_account")
public class CatalogDownloadPageController extends AbstractSearchPageController
{
	protected static final Logger LOG = Logger.getLogger(CatalogDownloadPageController.class);

	private static final String REDIRECT_PREFIX = "redirect:";
	@Resource(name = "accountBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder accountBreadcrumbBuilder;

	private static final String CATALOG_DOWNLOAD_PAGE = "catalogdownload";

	@Resource(name = "defaultEnergizerCatalogDownloadFacade")
	private EnergizerCatalogDownloadFacade defaultEnergizerCatalogDownloadFacade;

	@Resource
	private ExportService exportService;

	@Autowired
	private ConfigurationService configurationService;

	@Resource
	UserService userService;

	@Resource(name = "enumerationService")
	private EnumerationService enumerationService;

	@Resource(name = "storeSessionFacade")
	private StoreSessionFacade storeSessionFacade;

	@Resource(name = "defaultEnergizerB2BOrderHistoryFacade")
	private DefaultEnergizerB2BOrderHistoryFacade orderHistoryFacade;


	@RequestMapping(value = "/catalogDownload")
	//, method = RequestMethod.POST)
	@RequireHardLogIn
	public void downloadCatalog(final Model model, final RedirectAttributes redirectAttributes, final HttpServletRequest request,
			final HttpServletResponse response) throws CMSItemNotFoundException
	{
		try
		{
			LOG.info("catalog Download");
			final Long startTime = System.currentTimeMillis();
			LOG.info("Before catalog download :  " + startTime);

			final CustomerData customerData = this.getUser();

			if (customerData.getUnit().getUid() != null)
			{
				LOG.info("UID " + customerData.getUnit().getUid());
				LOG.info("SiteID" + this.getSiteUid());

				final List<Object> catalogList = defaultEnergizerCatalogDownloadFacade
						.catalogDownloadList(customerData.getUnit().getUid(), this.getSiteUid());
				final boolean iscatalogDownload = true;
				final String currencyCode = populateCurrency();
				defaultEnergizerCatalogDownloadFacade.writingToExcel(catalogList, iscatalogDownload, currencyCode, this.getSiteUid());
				defaultEnergizerCatalogDownloadFacade.saveFile(request, response, iscatalogDownload);

				final Long downloadedTime = System.currentTimeMillis();
				LOG.info("Catalog Download took " + (downloadedTime - startTime) + " milliseconds");
			}

		}

		catch (final Exception e)
		{
			LOG.info("Expection Caught::::" + e.getMessage());
			e.printStackTrace();

		}


		/*
		 * try {
		 *
		 * final String exportString = defaultEnergizerCatalogDownloadFacade.generateScript();
		 *
		 * LOG.info("In Controller class, script generator result  ---  " + exportString);
		 *
		 *
		 * final ImpExResource exportResource = new StreamBasedImpExResource(new StringBufferInputStream(exportString),
		 * CSVConstants.HYBRIS_ENCODING);
		 *
		 *
		 *
		 * final ExportConfig exportConfig = new ExportConfig(); exportConfig.setFailOnError(false);
		 * exportConfig.setValidationMode(ValidationMode.RELAXED); exportConfig.setScript(exportResource);
		 * exportConfig.setSingleFile(true);
		 *
		 *
		 * if (exportConfig != null) { LOG.info("Export Config Object " + exportConfig.getScript()); } else {
		 * LOG.info("Export Config Object is null"); }
		 *
		 * final ExportResult result = exportService.exportData(exportConfig); if (result.isSuccessful()) {
		 * defaultEnergizerCatalogDownloadFacade.copyExportedMediaToExportDir(result);
		 * defaultEnergizerCatalogDownloadFacade.saveFile(request, response); }
		 *
		 * } catch (final Exception e) { LOG.info("Error: " + e.getMessage()); LOG.info("Exception cause: " + e.getCause());
		 * e.printStackTrace(); }
		 *
		 */

	}

	/**
	 * @return
	 */
	protected String populateCurrency()
	{
		return storeSessionFacade.getCurrentCurrency().getIsocode();
	}

	@RequestMapping(value = "/orderHistoryDownload")
	//, method = RequestMethod.POST)
	@RequireHardLogIn
	public void downloadOrderHistory(final Model model, final RedirectAttributes redirectAttributes,
			final HttpServletRequest request, final HttpServletResponse response) throws CMSItemNotFoundException
	{
		try
		{
			LOG.info("order History Download");

			final CustomerData customerData = this.getUser();

			final boolean iscatalogDownload = false;

			final List<OrderStatus> validStates = new ArrayList<OrderStatus>();
			validStates.add(OrderStatus.PENDING_QUOTE);
			validStates.add(OrderStatus.APPROVED_QUOTE);
			validStates.add(OrderStatus.REJECTED_QUOTE);

			final StringBuilder sb = new StringBuilder();
			final List orderStatusList = new ArrayList();

			for (final OrderStatus status : validStates)
			{
				sb.append("'").append(status).append("'").append(",");
			}

			final String status = sb.toString();
			String Statuslist = null;
			if (status.endsWith(","))
			{
				Statuslist = status.substring(0, status.length() - 1);
			}
			orderStatusList.add(Statuslist);

			if (customerData.getUnit().getUid() != null)
			{
				final List<Object> orderHistoryList = orderHistoryFacade.getOrderHistoryForB2BUnit(customerData.getUnit().getUid(),
						orderStatusList);

				final String currencyCode = populateCurrency();

				defaultEnergizerCatalogDownloadFacade.writingToExcel(orderHistoryList, iscatalogDownload, currencyCode,
						this.getSiteUid());
				defaultEnergizerCatalogDownloadFacade.saveFile(request, response, iscatalogDownload);
			}
		}
		catch (final Exception e)
		{
			LOG.info("Expection Caught" + e.getMessage());
			e.printStackTrace();
		}
	}


}
