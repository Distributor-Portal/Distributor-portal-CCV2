package com.energizer.core.invoice.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.Config;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.energizer.core.invoice.EnergizerInvoiceService;

public class DefaultEnergizerInvoiceService implements EnergizerInvoiceService
{
	public static final String INVOICE_FILE_PATH = "invoice.filepath";
	private static final String INVOICE_FILE_PATH_EMEA = "invoice.filepath.EMEA";
	public static final String INVOICE_FILE_EXTENSION = ".pdf";
	private static final Logger LOG = Logger.getLogger(DefaultEnergizerInvoiceService.class);
	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;

	@Autowired
	private SessionService sessionService;

	@Override
	public byte[] getPDFInvoiceAsBytes(final OrderData orderData)
	{
		System.out.println("Enter in getPDFInvoiceAsBytes");
		System.out.println("erpOrderNumber1-->" + orderData.getErpOrderNumber());
		return getPDFFromFilePath(orderData.getErpOrderNumber());
	}

	private byte[] getPDFFromFilePath(final String erpOrderNumber)
	{
		byte[] retVal = null;
		try
		{
			String filePath = Config.getParameter(INVOICE_FILE_PATH);
			final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");

			if (PERSONALCARE_EMEA.equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()))
			{
				filePath = Config.getParameter(INVOICE_FILE_PATH_EMEA);
				System.out.println("filePath-1->" + filePath);
				System.out.println("erpOrderNumber-1->" + erpOrderNumber);

				final InputStream invoiceFileretVal = getInvoiceFileFromBlob(filePath, erpOrderNumber);
				retVal = IOUtils.toByteArray(new DataInputStream(invoiceFileretVal));
			}
			else
			{
				retVal = IOUtils.toByteArray(new FileInputStream(new File(filePath + erpOrderNumber + INVOICE_FILE_EXTENSION)));
			}
		}
		catch (final IOException ex)
		{
			retVal = null;
		}
		return retVal;
	}

	public String getConfigValue(final String key)
	{
		return configurationService.getConfiguration().getString(key);
	}

	public File getInvoiceFile(final String directoryPath, final String erpOrderNo)
	{
		File invoiceFile = null;
		if (StringUtils.isNotEmpty(erpOrderNo))
		{
			final File directory = new File(directoryPath);
			final File[] fList = directory.listFiles();

			for (final File file : fList)
			{
				if (file.isFile() && file.getName().contains(erpOrderNo))
				{
					invoiceFile = file;
					break;
				}
			}
		}
		return invoiceFile;
	}

	public InputStream getInvoiceFileFromBlob(final String directoryPath, final String erpOrderNo)
	{
		InputStream invoiceFile = null;
		if (StringUtils.isNotEmpty(erpOrderNo))
		{
			final BlobContainerClient container = energizerWindowsAzureBlobStorageStrategy.getBlobContainer();
			// Use prefix to list blobs in the "directory"
			String prefix = directoryPath.endsWith("/") ? directoryPath : directoryPath + "/";
			try
			{
				for (BlobItem blobItem : container.listBlobsByHierarchy(prefix))
				{
					String blobName = blobItem.getName();
					String fileName = blobName.substring(blobName.lastIndexOf('/') + 1);
					if (fileName.contains(erpOrderNo) && fileName.toLowerCase(Locale.ROOT).endsWith(INVOICE_FILE_EXTENSION))
					{
						BlobClient blobClient = container.getBlobClient(blobName);
						// Download blob content as InputStream
						invoiceFile = new DataInputStream(blobClient.openInputStream());
						break;
					}
				}
			}
			catch (final RuntimeException e)
			{
				LOG.info("Exception occurred while fetching invoice from blob storage: " + e.getMessage());
			}
		}
		return invoiceFile;
	}
}
