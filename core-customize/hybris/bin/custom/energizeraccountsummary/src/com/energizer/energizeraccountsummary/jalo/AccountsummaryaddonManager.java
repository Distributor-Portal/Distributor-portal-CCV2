package com.energizer.energizeraccountsummary.jalo;

import org.apache.log4j.Logger;

import com.energizer.energizeraccountsummary.constants.AccountsummaryaddonConstants;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;

@SuppressWarnings("PMD")
public class AccountsummaryaddonManager extends GeneratedAccountsummaryaddonManager
{
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger( AccountsummaryaddonManager.class.getName() );
	
	public static final AccountsummaryaddonManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (AccountsummaryaddonManager) em.getExtension(AccountsummaryaddonConstants.EXTENSIONNAME);
	}
	
}
