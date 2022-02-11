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
package com.energizer.energizeraccountsummary.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.energizer.energizeraccountsummary.constants.AccountsummaryaddonConstants;

import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.commerceservices.setup.events.SampleDataImportedEvent;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import de.hybris.platform.servicelayer.cronjob.PerformResult;


/**
 * This class provides hooks into the system's initialization and update processes.
 * 
 * @see "https://wiki.hybris.com/display/release4/Hooks+for+Initialization+and+Update+Process"
 */
@SystemSetup(extension = AccountsummaryaddonConstants.EXTENSIONNAME)
public class InitialDataSystemSetup extends AbstractSystemSetup
{

	public static final String IMPORT_SYNC_CATALOGS = "contentCatalogs";
	public static final String ENERGIZER = "energizer";

	/**
	 * Generates the Dropdown and Multi-select boxes for the project data import
	 */
	@Override
	@SystemSetupParameterMethod
	public List<SystemSetupParameter> getInitializationOptions()
	{
		final List<SystemSetupParameter> params = new ArrayList<SystemSetupParameter>();

		params.add(createBooleanSystemSetupParameter(IMPORT_SYNC_CATALOGS, "Sync Content Catalogs", true));

		return params;
	}

	/**
	 * This method will be called during the system initialization.
	 * 
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.PROJECT, process = Process.ALL)
	public void createProjectData(final SystemSetupContext context)
	{

		if (getBooleanSystemSetupParameter(context, IMPORT_SYNC_CATALOGS))
		{
			importContentCatalog(context, ENERGIZER, Collections.singletonList(ENERGIZER));

			final ImportData powertoolsImportData = new ImportData();
			powertoolsImportData.setProductCatalogName(ENERGIZER);
			powertoolsImportData.setContentCatalogNames(Arrays.asList(ENERGIZER));
			powertoolsImportData.setStoreNames(Arrays.asList(ENERGIZER));
			// Send an event to notify any AddOns that the initial data import is complete
			getEventService().publishEvent(new SampleDataImportedEvent(context, Arrays.asList(powertoolsImportData)));
		}
	}

	protected boolean synchronizeContentCatalog(final SystemSetupContext context, final String catalogName, final boolean sync)
	{
		logInfo(context, "Begin synchronizing Content Catalog [" + catalogName + "] - "
				+ (sync ? "synchronizing" : "initializing job"));

		createContentCatalogSyncJob(context, catalogName + "ContentCatalog");

		boolean result = true;

		if (sync)
		{
			final PerformResult syncCronJobResult = executeCatalogSyncJob(context, catalogName + "ContentCatalog");
			if (isSyncRerunNeeded(syncCronJobResult))
			{
				logInfo(context, "Catalog catalog [" + catalogName + "] sync has issues.");
				result = false;
			}
		}

		logInfo(context, "Done " + (sync ? "synchronizing" : "initializing job") + " Content Catalog [" + catalogName + "]");
		return result;
	}

	protected void importContentCatalog(final SystemSetupContext context, final String catalogName,
			final List<String> contentCatalogs)
	{
		logInfo(context, "Begin importing catalog [" + catalogName + "]");

		importImpexFile(context, "/energizeraccountsummary/import/contentCatalogs/common/cms-content.impex",
				true);
		importImpexFile(context, "/energizeraccountsummary/import/contentCatalogs/common/cms-content_en.impex",
				true);
		importImpexFile(context, "/energizeraccountsummary/import/contentCatalogs/common/cms-content_es.impex",
				true);
		importImpexFile(context, "/energizeraccountsummary/import/contentCatalogs/" + catalogName + "ContentCatalog/cms-content.impex",
				true);
		importImpexFile(context, "/energizeraccountsummary/import/contentCatalogs/" + catalogName + "ContentCatalog/cms-content_es.impex",
				true);
		importImpexFile(context, "/energizeraccountsummary/import/contentCatalogs/" + catalogName + "ContentCatalog/cms-content_en.impex",
				true);
		logInfo(context, "Done importing catalog [" + catalogName + "]");

		// perform content sync jobs
		for (final String contentCatalog : contentCatalogs)
		{
			synchronizeContentCatalog(context, contentCatalog, true);
		}
	}

}
