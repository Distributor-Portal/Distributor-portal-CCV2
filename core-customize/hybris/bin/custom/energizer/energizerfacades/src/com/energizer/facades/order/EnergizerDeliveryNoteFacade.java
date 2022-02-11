/**
 *
 */
package com.energizer.facades.order;

import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.media.NoDataAvailableException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.energizer.core.data.EnergizerDeliveryNoteData;


/**
 * @author AS398193
 *
 */
public interface EnergizerDeliveryNoteFacade
{
	public void createUploadOrderMedia(File serverFile, String fileName, String mediaCode, String catalogName, String orderCode)
			throws FileNotFoundException, IOException;

	public EnergizerDeliveryNoteData getDeliveryNoteFileData(String orderCode, String fileName);

	public EnergizerDeliveryNoteData getDeliveryNoteFileDataForCart(String cartID, String fileName);

	public byte[] getDeliveryNoteFileAsBytes(String mediaModelCode)
			throws NoDataAvailableException, IllegalArgumentException, IOException;

	public void deleteDeliveryNoteFileMedia(final String mediaCode, final String catalogName, final String orderCode)
			throws FileNotFoundException, IOException, ModelRemovalException;

	public boolean deleteDeliveryNoteFileMediaForCart(final String mediaCode, final String catalogName, final String cartID)
			throws FileNotFoundException, IOException;

	public boolean setMediaRemovableForCart(final String mediaCode, final String catalogName, final String cartID)
			throws ModelSavingException;
}
