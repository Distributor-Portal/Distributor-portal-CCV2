package com.energizer.core.azure.blob;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import de.hybris.platform.azure.media.storage.WindowsAzureBlobStorageStrategy;
import de.hybris.platform.util.Config;
import org.apache.log4j.Logger;

public class EnergizerWindowsAzureBlobStorageStrategy extends WindowsAzureBlobStorageStrategy
{
	Logger LOG = Logger.getLogger(EnergizerWindowsAzureBlobStorageStrategy.class);
	public static final String connectionString = Config.getParameter("azure.blob.storage.account.connection-string");
	public static final String containerName = Config.getParameter("azure.blob.storage.container.name");

	/**
	 * @return BlobContainerClient
	 */
	public BlobContainerClient getBlobContainer()
	{
		try
		{
			return getBlobClient().getBlobContainerClient(containerName);
		}
		catch (Exception e)
		{
			LOG.error("Failed to get BlobContainerClient", e);
			return null;
		}
	}

	/**
	 * @return BlobServiceClient
	 */
	public BlobServiceClient getBlobClient()
	{
		try
		{
			return new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
		}
		catch (Exception e)
		{
			LOG.error("Failed to create BlobServiceClient", e);
			return null;
		}
	}
}