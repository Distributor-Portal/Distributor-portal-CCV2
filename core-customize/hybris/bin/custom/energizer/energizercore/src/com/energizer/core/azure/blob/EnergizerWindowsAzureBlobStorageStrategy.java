/**
 *
 */
package com.energizer.core.azure.blob;

import de.hybris.platform.azure.media.storage.WindowsAzureBlobStorageStrategy;
import de.hybris.platform.util.Config;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.apache.log4j.Logger;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;


/**
 * @author MU20325022
 *
 */
public class EnergizerWindowsAzureBlobStorageStrategy extends WindowsAzureBlobStorageStrategy
{
	Logger LOG = Logger.getLogger(EnergizerWindowsAzureBlobStorageStrategy.class);
	public static final String connectionString = Config.getParameter("azure.blob.storage.account.connection-string");
	public static final String containerName = Config.getParameter("azure.blob.storage.container.name");

	/**
	 * @return
	 */
	public CloudBlobContainer getBlobContainer()
	{
		CloudBlobContainer container = null;
		try
		{
			container = getBlobClient().getContainerReference(containerName);

		}
		catch (URISyntaxException | StorageException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}
		return container;
	}

	/**
	 * TODO: Move this method to separate file while refratoring
	 *
	 * @return
	 */
	public CloudBlobClient getBlobClient()
	{
		CloudStorageAccount storageAccount;
		CloudBlobClient blobClient = null;

		try
		{
			storageAccount = CloudStorageAccount.parse(connectionString);
			blobClient = storageAccount.createCloudBlobClient();
		}
		catch (InvalidKeyException | URISyntaxException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}
		return blobClient;


	}

}