//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.06.28 at 08:37:42 PM IST 
//


package com.energizer.core.jaxb.xsd.objects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfZSD_TSOITEM_7888da complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfZSD_TSOITEM_7888da">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ZSD_TSOITEM" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ZSD_TSOITEM_7888da" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfZSD_TSOITEM_7888da", propOrder = {
    "zsdtsoitem"
})
public class ArrayOfZSDTSOITEM7888Da {

    @XmlElement(name = "ZSD_TSOITEM")
    protected List<ZSDTSOITEM7888Da> zsdtsoitem;

    /**
     * Gets the value of the zsdtsoitem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the zsdtsoitem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getZSDTSOITEM().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ZSDTSOITEM7888Da }
     * 
     * 
     */
    public List<ZSDTSOITEM7888Da> getZSDTSOITEM() {
        if (zsdtsoitem == null) {
            zsdtsoitem = new ArrayList<ZSDTSOITEM7888Da>();
        }
        return this.zsdtsoitem;
    }

}
