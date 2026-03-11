/**
 *
 */
package com.energizer.facades.catalogdownload;

import de.hybris.platform.impex.model.ImpExMediaModel;
import de.hybris.platform.servicelayer.impex.ExportResult;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * @author m1030110
 *
 */
public interface EnergizerCatalogDownloadFacade
{

	public String generateScript();

	public void copyExportedMediaToExportDir(final ExportResult result);

	public void copyExportedMediaFile(final Path targetDir, final ImpExMediaModel impexModel) throws IOException;

	public void convertCSVToExcel(final String fileName, final String filePath, final Character fieldSeparator);

	public String findRealMediaPath(final ImpExMediaModel impexModel);

	public void saveFile(final HttpServletRequest request, final HttpServletResponse response, boolean iscatalogDownload)
			throws FileNotFoundException, IOException, Exception;

	public List<Object> catalogDownloadList(String uid, String siteId) throws Exception;

	public void writingToExcel(List<Object> catalogList, boolean isCatalogDownload, String currencyCode, String site)
			throws Exception;


}
