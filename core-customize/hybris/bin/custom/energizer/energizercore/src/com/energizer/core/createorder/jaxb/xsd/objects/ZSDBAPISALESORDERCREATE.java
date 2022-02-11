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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="I_SOHEAD" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ZSD_ISOHEAD_d0b6b6" minOccurs="0"/>
 *         &lt;element name="MESSAGETABLE" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfBAPIRET2_d0b6b6" minOccurs="0"/>
 *         &lt;element name="ORDER_INCOMPLETE" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfBAPIINCOMP_d0b6b6" minOccurs="0"/>
 *         &lt;element name="T_SOITEM" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfZSD_TSOITEM_d0b6b6" minOccurs="0"/>
 *         &lt;element name="T_SOPARTNER" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfZSD_TSOPART_d0b6b6" minOccurs="0"/>
 *         &lt;element name="T_TEXTS" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfBAPISDTEXT_d0b6b6" minOccurs="0"/>
 *         &lt;element name="T_TSOCONDITIONS" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfZSD_TSOCONDITIONS_d0b6b6" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "isohead",
    "messagetable",
    "orderincomplete",
    "tsoitem",
    "tsopartner",
    "ttexts",
    "ttsoconditions"
})
@XmlRootElement(name = "ZSD_BAPI_SALESORDER_CREATE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/")
public class ZSDBAPISALESORDERCREATE {

    @XmlElementRef(name = "I_SOHEAD", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ZSDISOHEADD0B6B6> isohead;
    @XmlElementRef(name = "MESSAGETABLE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfBAPIRET2D0B6B6> messagetable;
    @XmlElementRef(name = "ORDER_INCOMPLETE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfBAPIINCOMPD0B6B6> orderincomplete;
    @XmlElementRef(name = "T_SOITEM", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfZSDTSOITEMD0B6B6> tsoitem;
    @XmlElementRef(name = "T_SOPARTNER", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfZSDTSOPARTD0B6B6> tsopartner;
    @XmlElementRef(name = "T_TEXTS", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfBAPISDTEXTD0B6B6> ttexts;
    @XmlElementRef(name = "T_TSOCONDITIONS", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfZSDTSOCONDITIONSD0B6B6> ttsoconditions;

    /**
     * Gets the value of the isohead property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ZSDISOHEADD0B6B6 }{@code >}
     *     
     */
    public JAXBElement<ZSDISOHEADD0B6B6> getISOHEAD() {
        return isohead;
    }

    /**
     * Sets the value of the isohead property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ZSDISOHEADD0B6B6 }{@code >}
     *     
     */
    public void setISOHEAD(JAXBElement<ZSDISOHEADD0B6B6> value) {
        this.isohead = value;
    }

    /**
     * Gets the value of the messagetable property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPIRET2D0B6B6 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfBAPIRET2D0B6B6> getMESSAGETABLE() {
        return messagetable;
    }

    /**
     * Sets the value of the messagetable property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPIRET2D0B6B6 }{@code >}
     *     
     */
    public void setMESSAGETABLE(JAXBElement<ArrayOfBAPIRET2D0B6B6> value) {
        this.messagetable = value;
    }

    /**
     * Gets the value of the orderincomplete property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPIINCOMPD0B6B6 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfBAPIINCOMPD0B6B6> getORDERINCOMPLETE() {
        return orderincomplete;
    }

    /**
     * Sets the value of the orderincomplete property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPIINCOMPD0B6B6 }{@code >}
     *     
     */
    public void setORDERINCOMPLETE(JAXBElement<ArrayOfBAPIINCOMPD0B6B6> value) {
        this.orderincomplete = value;
    }

    /**
     * Gets the value of the tsoitem property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSDTSOITEMD0B6B6 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfZSDTSOITEMD0B6B6> getTSOITEM() {
        return tsoitem;
    }

    /**
     * Sets the value of the tsoitem property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSDTSOITEMD0B6B6 }{@code >}
     *     
     */
    public void setTSOITEM(JAXBElement<ArrayOfZSDTSOITEMD0B6B6> value) {
        this.tsoitem = value;
    }

    /**
     * Gets the value of the tsopartner property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSDTSOPARTD0B6B6 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfZSDTSOPARTD0B6B6> getTSOPARTNER() {
        return tsopartner;
    }

    /**
     * Sets the value of the tsopartner property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSDTSOPARTD0B6B6 }{@code >}
     *     
     */
    public void setTSOPARTNER(JAXBElement<ArrayOfZSDTSOPARTD0B6B6> value) {
        this.tsopartner = value;
    }

    /**
     * Gets the value of the ttexts property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPISDTEXTD0B6B6 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfBAPISDTEXTD0B6B6> getTTEXTS() {
        return ttexts;
    }

    /**
     * Sets the value of the ttexts property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPISDTEXTD0B6B6 }{@code >}
     *     
     */
    public void setTTEXTS(JAXBElement<ArrayOfBAPISDTEXTD0B6B6> value) {
        this.ttexts = value;
    }

    /**
     * Gets the value of the ttsoconditions property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSDTSOCONDITIONSD0B6B6 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfZSDTSOCONDITIONSD0B6B6> getTTSOCONDITIONS() {
        return ttsoconditions;
    }

    /**
     * Sets the value of the ttsoconditions property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSDTSOCONDITIONSD0B6B6 }{@code >}
     *     
     */
    public void setTTSOCONDITIONS(JAXBElement<ArrayOfZSDTSOCONDITIONSD0B6B6> value) {
        this.ttsoconditions = value;
    }

}
