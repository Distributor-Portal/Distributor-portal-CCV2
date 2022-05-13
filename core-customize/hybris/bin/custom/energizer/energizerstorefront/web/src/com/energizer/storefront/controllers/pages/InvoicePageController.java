/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.b2bacceleratorfacades.order.B2BOrderFacade;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.util.Config;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.energizer.facades.order.impl.DefaultEnergizerInvoiceFacade;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;


/**
 * Controller for home page.
 */
@Controller
@Scope("tenant")
@RequestMapping("/my-account/invoice")
public class InvoicePageController extends AbstractSearchPageController
{
	private static final String INVOICE_NUMBER_PATTERN = "{invoiceNumber:.*}";

	private static final String INVOICE_FILE_MIME = "application/pdf";

	public static final String INVOICE_FILE_EXTENSION = ".pdf";

	public static final String CONTENT_TYPE = "text/html";

	public static final String FILE_PATH = Config.getParameter("invoice.filepath.EMEA");


	@Resource(name = "defaultInvoiceFacade")
	private DefaultEnergizerInvoiceFacade defaultInvoiceFacade;

	@Resource
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;

	@Resource(name = "b2bOrderFacade")
	private B2BOrderFacade orderFacade;

	@RequestMapping(value = "/invoicePdfDisplay", method = RequestMethod.GET)
	@RequireHardLogIn
	public void invoice(@RequestParam("orderCode")
	final String invoiceNumber, @RequestParam(value = "inline", required = false)
	final Boolean inline, final Model model, final HttpServletRequest request, final HttpServletResponse response)
			throws CMSItemNotFoundException, IOException
	{

		//final byte pdfFile[] = defaultInvoiceFacade.getPDFInvoiceAsBytes(this.getCmsSiteService().getCurrentSite().getUid(),
		//invoiceNumber.trim());

		//final byte pdfFile[] = defaultEnergizerInvoiceService.getPDFInvoiceAsBytes(invoiceNumber);
		final OrderData orderData = orderFacade.getOrderDetailsForCode(invoiceNumber.trim());
		final byte pdfFile[] = getInvoiceFileFromBlob(orderData.getErpOrderNumber());

		if (null != pdfFile)
		{
			if (inline == null)
			{
				response.addHeader("Content-Disposition", "attachment;filename=" + invoiceNumber + INVOICE_FILE_EXTENSION);
			}

			if (inline != null && inline.booleanValue())
			{
				response.addHeader("Content-Disposition", "inline;filename=" + invoiceNumber + INVOICE_FILE_EXTENSION);
			}
			else if (inline != null && !inline.booleanValue())
			{
				response.addHeader("Content-Disposition", "attachment;filename=" + invoiceNumber + INVOICE_FILE_EXTENSION);
			}
			final OutputStream responseOutputStream = response.getOutputStream();
			response.setContentType(INVOICE_FILE_MIME);
			response.setContentLength(pdfFile.length);
			responseOutputStream.write(pdfFile);
		}
		else
		{
			response.setContentType(CONTENT_TYPE);
			final PrintWriter pw = response.getWriter();
			final String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";
			pw.println(docType + "<html>");
			pw.println("<head><title>Error</title>");
			pw.println("<body>");
			pw.println("<h1>Failed To Load Invoice PDF</h1>");
			pw.println("</body></html>");
		}


	}

	public byte[] getInvoiceFileFromBlob(final String erpOrderNo)
	{
		byte[] invoiceFile = null;
		System.out.println("erpOrderNo--->" + erpOrderNo);
		if (StringUtils.isNotEmpty(erpOrderNo))
		{
			CloudBlobDirectory blobDirectory = null;
			final CloudBlobContainer container = energizerWindowsAzureBlobStorageStrategy.getBlobContainer();

			System.out.println("filePath--->" + FILE_PATH);
			try
			{
				blobDirectory = container.getDirectoryReference(FILE_PATH);

				for (final ListBlobItem blobItem : blobDirectory.listBlobs())
				{
					System.out.println("Method--> getInvoiceFileFromBlob--->Start");
					final String subfullFilePath = blobItem.getStorageUri().getPrimaryUri().getPath();
					System.out.println("subfullFilePath-->" + subfullFilePath);
					final String fullFilePath = subfullFilePath.substring(8);
					System.out.println("fullFilePath-->" + fullFilePath);
					final String fileName = StringUtils.substringAfterLast(fullFilePath, "/");
					System.out.println("fileName-->" + fileName);
					if (fileName.contains(erpOrderNo))
					{
						System.out.println("erpOrderNo-->" + erpOrderNo);
						final CloudBlockBlob blob2 = container.getBlockBlobReference(FILE_PATH.toString());
						invoiceFile = blob2.downloadText().getBytes();

						break;

					}
					System.out.println("Method--> getInvoiceFileFromBlob--->End");

				}
			}
			catch (StorageException | URISyntaxException e)
			{
				// YTODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (final IOException e)
			{
				// YTODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return invoiceFile;
	}






}
