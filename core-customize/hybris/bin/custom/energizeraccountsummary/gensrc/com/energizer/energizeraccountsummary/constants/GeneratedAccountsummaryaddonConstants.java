/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Mar 13, 2015 5:54:37 PM                     ---
 * ----------------------------------------------------------------
 */
package com.energizer.energizeraccountsummary.constants;

/**
 * @deprecated use constants in Model classes instead
 */
@Deprecated
@SuppressWarnings({"unused","cast","PMD"})
public class GeneratedAccountsummaryaddonConstants
{
	public static final String EXTENSIONNAME = "energizeraccountsummary";
	public static class TC
	{
		public static final String ACCOUNTSUMMARYACCOUNTSTATUSCOMPONENT = "AccountSummaryAccountStatusComponent".intern();
		public static final String ACCOUNTSUMMARYUNITTREECOMPONENT = "AccountSummaryUnitTreeComponent".intern();
		public static final String B2BDOCUMENT = "B2BDocument".intern();
		public static final String B2BDOCUMENTPAYMENTINFO = "B2BDocumentPaymentInfo".intern();
		public static final String B2BDOCUMENTTYPE = "B2BDocumentType".intern();
		public static final String DELETEDOCUMENTFILECRONJOB = "DeleteDocumentFileCronJob".intern();
		public static final String DOCUMENTMEDIA = "DocumentMedia".intern();
		public static final String DOCUMENTPAYABLEORUSABLE = "DocumentPayableOrUsable".intern();
		public static final String DOCUMENTSORT = "DocumentSort".intern();
		public static final String DOCUMENTSTATUS = "DocumentStatus".intern();
	}
	public static class Attributes
	{
		public static class AbstractOrder
		{
			public static final String DOCUMENT = "document".intern();
		}
		public static class B2BUnit
		{
			public static final String DOCUMENT = "document".intern();
		}
	}
	public static class Enumerations
	{
		public static class DocumentPayableOrUsable
		{
			public static final String PAY = "PAY".intern();
			public static final String USE = "USE".intern();
		}
		public static class DocumentSort
		{
			public static final String DOCUMENTNUMBER = "documentNumber".intern();
			public static final String DOCUMENTTYPE = "documentType".intern();
			public static final String STATUS = "status".intern();
			public static final String DATE = "date".intern();
			public static final String DUEDATE = "dueDate".intern();
			public static final String AMOUNT = "amount".intern();
			public static final String OPENAMOUNT = "openAmount".intern();
		}
		public static class DocumentStatus
		{
			public static final String OPEN = "open".intern();
			public static final String CLOSED = "closed".intern();
		}
	}
	public static class Relations
	{
		public static final String B2BDOCUMENT2ABSTRACTORDER = "B2BDocument2AbstractOrder".intern();
		public static final String B2BDOCUMENT2B2BDOCUMENTPAYINFO = "B2BDocument2B2BDocumentPayInfo".intern();
		public static final String B2BDOCUMENT2B2BDOCUMENTUSEINFO = "B2BDocument2B2BDocumentUseInfo".intern();
		public static final String B2BDOCUMENTB2BDOCUMENTYPERELATION = "B2BDocumentB2BDocumenTypeRelation".intern();
		public static final String B2BUNIT2B2BDOCUMENT = "B2BUnit2B2BDocument".intern();
	}
	
	protected GeneratedAccountsummaryaddonConstants()
	{
		// private constructor
	}
	
	
}
