//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2018.06.28 at 08:55:04 PM IST
//


package com.energizer.core.createorder.jaxb.xsd.objects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>
 * Java class for ZSD_ISOHEAD_d0b6b6 complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ZSD_ISOHEAD_d0b6b6">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DOC_NUMBER" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="10"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="DOC_TYPE" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="4"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="SALES_ORG" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="4"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="DISTR_CHAN" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="DIVISION" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="PURCH_NO" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="20"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="PURCH_NO_S" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="35"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="REQ_DATE_H" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}dateTime">
 *               &lt;pattern value="(\d\d\d\d-\d\d-\d\d)T(00:00:00)(.*)"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="CURRENCY" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="5"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="NET_VALUE" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="([\-]{0,1})(([0-9]{0,20}\.[0-9]{0,9})|([0-9]{1,20}))"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TAX_TOTAL" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="([\-]{0,1})(([0-9]{0,20}\.[0-9]{0,9})|([0-9]{1,20}))"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="STATUS" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="1"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="SHIP_COND" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="4"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ZSD_ISOHEAD_d0b6b6", propOrder =
{ "docnumber", "doctype", "salesorg", "distrchan", "division", "purchno", "purchnos", "reqdateh", "currency", "netvalue",
		"taxtotal", "status", "ship_cond" })
public class ZSDISOHEADD0B6B6
{

	@XmlElementRef(name = "DOC_NUMBER", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> docnumber;
	@XmlElementRef(name = "DOC_TYPE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> doctype;
	@XmlElementRef(name = "SALES_ORG", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> salesorg;
	@XmlElementRef(name = "DISTR_CHAN", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> distrchan;
	@XmlElementRef(name = "DIVISION", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> division;
	@XmlElementRef(name = "PURCH_NO", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> purchno;
	@XmlElementRef(name = "PURCH_NO_S", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> purchnos;
	@XmlElementRef(name = "REQ_DATE_H", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<XMLGregorianCalendar> reqdateh;
	@XmlElementRef(name = "CURRENCY", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> currency;
	@XmlElementRef(name = "NET_VALUE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> netvalue;
	@XmlElementRef(name = "TAX_TOTAL", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> taxtotal;
	@XmlElementRef(name = "STATUS", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> status;
	@XmlElementRef(name = "SHIP_COND", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
	protected JAXBElement<String> ship_cond;

	/**
	 * Gets the value of the docnumber property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getDOCNUMBER()
	{
		return docnumber;
	}

	/**
	 * Sets the value of the docnumber property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setDOCNUMBER(final JAXBElement<String> value)
	{
		this.docnumber = value;
	}

	/**
	 * Gets the value of the doctype property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getDOCTYPE()
	{
		return doctype;
	}

	/**
	 * Sets the value of the doctype property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setDOCTYPE(final JAXBElement<String> value)
	{
		this.doctype = value;
	}

	/**
	 * Gets the value of the salesorg property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getSALESORG()
	{
		return salesorg;
	}

	/**
	 * Sets the value of the salesorg property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setSALESORG(final JAXBElement<String> value)
	{
		this.salesorg = value;
	}

	/**
	 * Gets the value of the distrchan property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getDISTRCHAN()
	{
		return distrchan;
	}

	/**
	 * Sets the value of the distrchan property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setDISTRCHAN(final JAXBElement<String> value)
	{
		this.distrchan = value;
	}

	/**
	 * Gets the value of the division property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getDIVISION()
	{
		return division;
	}

	/**
	 * Sets the value of the division property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setDIVISION(final JAXBElement<String> value)
	{
		this.division = value;
	}

	/**
	 * Gets the value of the purchno property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getPURCHNO()
	{
		return purchno;
	}

	/**
	 * Sets the value of the purchno property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setPURCHNO(final JAXBElement<String> value)
	{
		this.purchno = value;
	}

	/**
	 * Gets the value of the purchnos property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getPURCHNOS()
	{
		return purchnos;
	}

	/**
	 * Sets the value of the purchnos property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setPURCHNOS(final JAXBElement<String> value)
	{
		this.purchnos = value;
	}

	/**
	 * Gets the value of the reqdateh property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
	 *
	 */
	public JAXBElement<XMLGregorianCalendar> getREQDATEH()
	{
		return reqdateh;
	}

	/**
	 * Sets the value of the reqdateh property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
	 *
	 */
	public void setREQDATEH(final JAXBElement<XMLGregorianCalendar> value)
	{
		this.reqdateh = value;
	}

	/**
	 * Gets the value of the currency property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getCURRENCY()
	{
		return currency;
	}

	/**
	 * Sets the value of the currency property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setCURRENCY(final JAXBElement<String> value)
	{
		this.currency = value;
	}

	/**
	 * Gets the value of the netvalue property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getNETVALUE()
	{
		return netvalue;
	}

	/**
	 * Sets the value of the netvalue property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setNETVALUE(final JAXBElement<String> value)
	{
		this.netvalue = value;
	}

	/**
	 * Gets the value of the taxtotal property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getTAXTOTAL()
	{
		return taxtotal;
	}

	/**
	 * Sets the value of the taxtotal property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setTAXTOTAL(final JAXBElement<String> value)
	{
		this.taxtotal = value;
	}

	/**
	 * Gets the value of the status property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getSTATUS()
	{
		return status;
	}

	/**
	 * Sets the value of the status property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setSTATUS(final JAXBElement<String> value)
	{
		this.status = value;
	}

	/**
	 * Gets the value of the ship_cond property.
	 *
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public JAXBElement<String> getSHIPCOND()
	{
		return ship_cond;
	}

	/**
	 * Sets the value of the ship_cond property.
	 *
	 * @param value
	 *           allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 *
	 */
	public void setSHIPCOND(final JAXBElement<String> value)
	{
		this.ship_cond = value;
	}

}
