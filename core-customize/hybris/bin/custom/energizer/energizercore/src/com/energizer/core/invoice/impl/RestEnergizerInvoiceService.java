/**
 * 
 */
package com.energizer.core.invoice.impl;

import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.energizer.core.invoice.EnergizerInvoiceService;
import com.energizer.core.invoicepdf.jaxb.xsd.objects.ObjectFactory;
import com.energizer.core.invoicepdf.jaxb.xsd.objects.ZBDSBUILDURLFORAL;
import com.energizer.core.invoicepdf.jaxb.xsd.objects.ZBDSBUILDURLFORALResponse;


/**
 * Fetch PDF from a file
 * 
 * @author kaushik.ganguly
 * 
 */
public class RestEnergizerInvoiceService implements EnergizerInvoiceService
{

	protected static final Logger LOG = Logger.getLogger(RestEnergizerInvoiceService.class);

	public static final String INVOICE_URL_PATH = "invoice.urlpath";

	public static final String INVOICE_FILE_EXTENSION = ".pdf";

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.invoice.EnergizerInvoiceService#getPDFInvoiceAsBytes()
	 */
	@Override
	public byte[] getPDFInvoiceAsBytes(final OrderData orderData) throws Exception
	{
		final String parsedXML = invoiceMarshal(orderData);
		final String restCallResponse = invokeRESTCall(parsedXML, "invoicePdf");
		final String invoiceURL = unMarshelResponse(restCallResponse);
		LOG.info("Before decoding the pdf url" + invoiceURL + "\n");
		final String decodedURL = URLDecoder.decode(invoiceURL, "UTF-8");
		LOG.info("After decoding the pdf url" + decodedURL);
		return getPDFFromFilePath(decodedURL);
	}

	/**
	 * @param restCallResponse
	 * @return
	 */
	private String unMarshelResponse(final String restCallResponse)
	{
		final JAXBContext jaxbContext;
		String bdsURL = "";
		try
		{
			jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final InputStream stream = new ByteArrayInputStream(restCallResponse.getBytes(StandardCharsets.UTF_8));
			final ZBDSBUILDURLFORALResponse unmarshalledInvoiceObject = (ZBDSBUILDURLFORALResponse) unmarshaller.unmarshal(stream);
			bdsURL = unmarshalledInvoiceObject.getBDSURL();

		}
		catch (final Exception exc)
		{
			LOG.error(exc.getMessage());
		}
		return bdsURL;
	}

	/**
	 * @param parsedXML
	 * @param string
	 * @return
	 */
	private String invokeRESTCall(final String parsedXML, final String option)
	{
		try
		{
			final RestTemplate restTemplate = new RestTemplate();
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", MediaType.APPLICATION_XML.toString());
			headers.add("Accept", MediaType.APPLICATION_XML.toString());
			final HttpEntity formEntity = new HttpEntity<>(parsedXML, headers);
			if (option.equalsIgnoreCase("invoicePdf"))
			{
				return getResponse(restTemplate, configurationService.getConfiguration().getString("invoicePdfURL"), formEntity);
			}
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage());
		}
		return null;
	}

	private String getResponse(final RestTemplate restTemplate, final String url, final HttpEntity formEntity)
	{

		ResponseEntity<String> response = null;
		try
		{
			response = restTemplate.exchange(url, HttpMethod.POST, formEntity, String.class);
			return response.getBody();
		}
		catch (final Exception ex)
		{
			LOG.error(ex.getMessage());
		}

		return null;
	}

	private byte[] getPDFFromFilePath(final String invoiceURL)
	{
		byte retVal[] = null;
		try
		{
			final RestTemplate restTemplate = new RestTemplate();
			//retVal = restTemplate.getForObject(Config.getParameter(INVOICE_URL_PATH) + invoiceNumber + INVOICE_FILE_EXTENSION,
			//	byte[].class);
			retVal = restTemplate.getForObject(invoiceURL, byte[].class);

		}
		catch (final Exception ex)
		{
			LOG.error(" ERROR invoking opentext PDF url ", ex);
			retVal = null;
		}
		return retVal;
	}

	private String invoiceMarshal(final OrderData orderData)
	{
		final ObjectFactory objectFactory = new ObjectFactory();
		StringWriter stringWriter = new StringWriter();
		String parsedXML = null;
		JAXBContext context;
		try
		{
			context = JAXBContext.newInstance("com.energizer.core.invoicepdf.jaxb.xsd.objects");
			final ZBDSBUILDURLFORAL xmlRoot = objectFactory.createZBDSBUILDURLFORAL();

			final JAXBElement<String> documentID = objectFactory.createZBDSBUILDURLFORALBDSDOCID(orderData.getDocumentID());
			final JAXBElement<String> documentClass = objectFactory
					.createZBDSBUILDURLFORALBDSDOCUCLASS(orderData.getDocumentClass());
			final JAXBElement<String> contrEP = objectFactory.createZBDSBUILDURLFORALBDSCONTREP(orderData.getContrEP());


			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
			stringWriter = new StringWriter();
			xmlRoot.setBDSCONTREP(contrEP);
			xmlRoot.setBDSDOCUCLASS(documentClass);
			xmlRoot.setBDSDOCID(documentID);
			marshaller.marshal(xmlRoot, stringWriter);
			parsedXML = stringWriter.toString();
		}
		catch (final JAXBException e)
		{
			LOG.error(e.getMessage());
		}
		return parsedXML;
	}

}
