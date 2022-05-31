/**
 *
 */
package com.energizer.core.invoice.impl;

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
import java.net.URISyntaxException;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.energizer.core.invoice.EnergizerInvoiceService;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;


/**
 * Fetch PDF from a file
 *
 * @author kaushik.ganguly
 *
 */
public class DefaultEnergizerInvoiceService implements EnergizerInvoiceService
{


	public static final String INVOICE_FILE_PATH = "invoice.filepath";
	private static final String INVOICE_FILE_PATH_EMEA = "invoice.filepath.EMEA";
	public static final String INVOICE_FILE_EXTENSION = ".pdf";

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;

	@Autowired
	private SessionService sessionService;



	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.core.invoice.EnergizerInvoiceService#getPDFInvoiceAsBytes()
	 */
	@Override
	public byte[] getPDFInvoiceAsBytes(final OrderData orderData)
	{
		System.out.println("Enter in getPDFInvoiceAsBytes");
		// YTODO Auto-generated method stub
		System.out.println("erpOrderNumber1-->" + orderData.getErpOrderNumber());
		return getPDFFromFilePath(orderData.getErpOrderNumber());

	}

	private byte[] getPDFFromFilePath(final String erpOrderNumber)
	{

		byte retVal[] = null;
		try
		{
			String filePath = Config.getParameter(INVOICE_FILE_PATH);

			final String PERSONALCARE_EMEA = getConfigValue("site.personalCareEMEA");

			if (PERSONALCARE_EMEA.equalsIgnoreCase(cmsSiteService.getCurrentSite().getUid()))
			{
				filePath = Config.getParameter(INVOICE_FILE_PATH_EMEA);

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

	/**
	 * getInvoiceFile method will return the invoice file from
	 *
	 * @param directoryName
	 * @return
	 */
	public File getInvoiceFile(final String directoryPath, final String erpOrderNo)
	{
		File invoiceFile = null;
		if (StringUtils.isNotEmpty(erpOrderNo))
		{
			final File directory = new File(directoryPath);
			//get all the files from a directory
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
			CloudBlobDirectory blobDirectory = null;
			final CloudBlobContainer container = energizerWindowsAzureBlobStorageStrategy.getBlobContainer();
			final String filePath = Config.getParameter(INVOICE_FILE_PATH_EMEA);

			try
			{
				blobDirectory = container.getDirectoryReference(filePath);

				for (final ListBlobItem blobItem : blobDirectory.listBlobs())
				{

					final String subfullFilePath = blobItem.getStorageUri().getPrimaryUri().getPath();

					final String fullFilePath = subfullFilePath.substring(8);

					final String fileName = StringUtils.substringAfterLast(fullFilePath, "/");

					if (fileName.contains(erpOrderNo))
					{
						final CloudBlockBlob blob2 = container.getBlockBlobReference(fullFilePath);
						invoiceFile = new DataInputStream(blob2.getSnapshotQualifiedUri().toURL().openStream());
						break;
					}

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
