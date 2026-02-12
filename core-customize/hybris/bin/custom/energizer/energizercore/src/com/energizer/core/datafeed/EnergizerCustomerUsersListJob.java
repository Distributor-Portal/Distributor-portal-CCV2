package com.energizer.core.datafeed;

import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.energizer.core.azure.blob.EnergizerWindowsAzureBlobStorageStrategy;
import com.energizer.core.model.EnergizerB2BUnitModel;

// Azure SDK v12 imports
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;

public class EnergizerCustomerUsersListJob extends AbstractJobPerformable<CronJobModel>
{
	public static final String FILENAME = "CustomerUsersList.xls";
	private static final Logger LOG = Logger.getLogger(EnergizerCustomerUsersListJob.class);

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "energizerWindowsAzureBlobStorageStrategy")
	private EnergizerWindowsAzureBlobStorageStrategy energizerWindowsAzureBlobStorageStrategy;

	@Override
	public PerformResult perform(final CronJobModel cronjob)
	{
		final String flexiSearchQuery = "SELECT {PK} FROM {EnergizerB2BUnit}";
		final SearchResult<EnergizerB2BUnitModel> result = flexibleSearchService.search(flexiSearchQuery);
		final List<EnergizerB2BUnitModel> energizerB2BUnitModels = result.getResult();
		final String path = configurationService.getConfiguration().getString("customerUserListPath");

		try
		{
			writeToExcelfile(energizerB2BUnitModels, path);
		}
		catch (final IOException e)
		{
			LOG.error("Error writing Excel file", e);
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	/**
	 * Writes the customer users list to an Excel file and uploads it to Azure Blob Storage using SDK v12.
	 */
	public void writeToExcelfile(final List<EnergizerB2BUnitModel> energizerB2BUnitModels, final String path) throws IOException
	{
		final Workbook workbook = new HSSFWorkbook();
		final Sheet sheet = workbook.createSheet();
		Row row = sheet.createRow(0);
		final CellStyle style = workbook.createCellStyle();
		style.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
		style.setAlignment(HorizontalAlignment.CENTER);

		final Cell cell00 = row.createCell(0);
		cell00.setCellStyle(style);
		cell00.setCellValue("CUSTOMER");

		final Cell cell11 = row.createCell(1);
		cell11.setCellStyle(style);
		cell11.setCellValue("USERS");

		final Cell cell22 = row.createCell(2);
		cell22.setCellStyle(style);
		cell22.setCellValue("USERS EMAIL ID");

		final Cell cell33 = row.createCell(3);
		cell33.setCellStyle(style);
		cell33.setCellValue("USERS CREATED DATE");

		int rownum = 1;
		for (final EnergizerB2BUnitModel unit : energizerB2BUnitModels)
		{
			// Fill in your logic to populate the Excel rows
			// Example:
			// Row dataRow = sheet.createRow(rownum++);
			// dataRow.createCell(0).setCellValue(unit.getName());
			// ... etc.
		}

		// Write to local file
		try (FileOutputStream out = new FileOutputStream(new File(FILENAME)))
		{
			workbook.write(out);
		}

		// Upload to Azure Blob Storage using SDK v12
		try
		{
			BlobContainerClient container = energizerWindowsAzureBlobStorageStrategy.getBlobContainerClient();
			BlobClient blobClient = container.getBlobClient(path + "/" + FILENAME);
			blobClient.uploadFromFile(FILENAME, true);
		}
		catch (BlobStorageException e)
		{
			LOG.error("Azure Blob Storage error", e);
		}
		catch (Exception e)
		{
			LOG.error("URI Syntax error", e);
		}
	}
}
