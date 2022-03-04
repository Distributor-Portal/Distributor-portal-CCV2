/**
 *
 */
package com.energizer.core.azure.blob;

import de.hybris.platform.azure.media.storage.WindowsAzureBlobStorageStrategy;
import de.hybris.platform.media.exceptions.ExternalStorageServiceException;
import de.hybris.platform.util.Config;

import org.apache.log4j.Logger;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.ListBlobItem;


/**
 * @author MU20325022
 *
 */
public class EnergizerWindowsAzureBlobStorageStrategy extends WindowsAzureBlobStorageStrategy
{
	Logger LOG = Logger.getLogger(EnergizerWindowsAzureBlobStorageStrategy.class);
	public static final String connectionString = Config.getParameter("azure.hotfolder.storage.account.connection-string");

	public CloudBlobContainer getContainer(final String connectionString)
	{


		final String storageConnectionString = "DefaultEndpointsProtocol=https;" + "AccountName=p7wae5gn35jcjcyaefo3gwc;"
				+ "AccountKey=PsXlnaTuGi9Vwq3g+n/yV6dqQeBk1d7nTbNm6XYIx3qjkAnuma5RYamdEyD0QN99DniPopetLdiXm5jkJqeVVQ==";


		final CloudStorageAccount storageAccount;
		CloudBlobClient edgewellBlobClient = null;
		CloudBlobContainer container = null;

		try
		{
			storageAccount = CloudStorageAccount.parse(storageConnectionString);

			LOG.info("before ---> account");

			LOG.info("before ---> account" + Config.getParameter("azure.hotfolder.storage.account.connection-string"));

			edgewellBlobClient = storageAccount.createCloudBlobClient();

			container = edgewellBlobClient.getContainerReference("hybris");
			container.createIfNotExists();
			LOG.info("after ---> account");

			System.out.println("Creating container: " + container.getName());


			final String toProcessDirectoryPath = "CSVFeedFolder/WESELL/energizerB2BEmployeeCSVProcessor/toProcess";
			final CloudBlobDirectory blobDirectory = container.getDirectoryReference(toProcessDirectoryPath);



			for (final ListBlobItem blobDir1 : blobDirectory.getDirectoryReference(toProcessDirectoryPath).listBlobs())
			{
				// not working
				System.out.println("getDirectoryReference :::::::::: " + " :: " + blobDir1.getStorageUri() + "" + blobDir1.getUri());
			}



			return container;




		}

		catch (final Exception var3)
		{
			throw new ExternalStorageServiceException(var3.getMessage(), var3);
		}



	}











}
