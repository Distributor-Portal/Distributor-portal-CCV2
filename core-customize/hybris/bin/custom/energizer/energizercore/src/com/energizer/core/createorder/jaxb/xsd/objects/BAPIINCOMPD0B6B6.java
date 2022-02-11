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


/**
 * <p>Java class for BAPIINCOMP_d0b6b6 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BAPIINCOMP_d0b6b6">
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
 *         &lt;element name="ITM_NUMBER" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *               &lt;totalDigits value="6"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="SCHED_LINE" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *               &lt;totalDigits value="4"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="PARTN_ROLE" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TABLE_NAME" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="30"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="FIELD_NAME" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="30"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="FIELD_TEXT" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="40"/>
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
@XmlType(name = "BAPIINCOMP_d0b6b6", propOrder = {
    "docnumber",
    "itmnumber",
    "schedline",
    "partnrole",
    "tablename",
    "fieldname",
    "fieldtext"
})
public class BAPIINCOMPD0B6B6 {

    @XmlElementRef(name = "DOC_NUMBER", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> docnumber;
    @XmlElementRef(name = "ITM_NUMBER", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> itmnumber;
    @XmlElementRef(name = "SCHED_LINE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> schedline;
    @XmlElementRef(name = "PARTN_ROLE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> partnrole;
    @XmlElementRef(name = "TABLE_NAME", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> tablename;
    @XmlElementRef(name = "FIELD_NAME", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> fieldname;
    @XmlElementRef(name = "FIELD_TEXT", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> fieldtext;

    /**
     * Gets the value of the docnumber property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDOCNUMBER() {
        return docnumber;
    }

    /**
     * Sets the value of the docnumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDOCNUMBER(JAXBElement<String> value) {
        this.docnumber = value;
    }

    /**
     * Gets the value of the itmnumber property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getITMNUMBER() {
        return itmnumber;
    }

    /**
     * Sets the value of the itmnumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setITMNUMBER(JAXBElement<Integer> value) {
        this.itmnumber = value;
    }

    /**
     * Gets the value of the schedline property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getSCHEDLINE() {
        return schedline;
    }

    /**
     * Sets the value of the schedline property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setSCHEDLINE(JAXBElement<Integer> value) {
        this.schedline = value;
    }

    /**
     * Gets the value of the partnrole property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPARTNROLE() {
        return partnrole;
    }

    /**
     * Sets the value of the partnrole property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPARTNROLE(JAXBElement<String> value) {
        this.partnrole = value;
    }

    /**
     * Gets the value of the tablename property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTABLENAME() {
        return tablename;
    }

    /**
     * Sets the value of the tablename property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTABLENAME(JAXBElement<String> value) {
        this.tablename = value;
    }

    /**
     * Gets the value of the fieldname property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFIELDNAME() {
        return fieldname;
    }

    /**
     * Sets the value of the fieldname property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFIELDNAME(JAXBElement<String> value) {
        this.fieldname = value;
    }

    /**
     * Gets the value of the fieldtext property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFIELDTEXT() {
        return fieldtext;
    }

    /**
     * Sets the value of the fieldtext property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFIELDTEXT(JAXBElement<String> value) {
        this.fieldtext = value;
    }

}
