//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.06.28 at 08:55:04 PM IST 
//


package com.energizer.core.createorder.jaxb.xsd.objects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfBAPIINCOMP_d0b6b6 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfBAPIINCOMP_d0b6b6">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BAPIINCOMP" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}BAPIINCOMP_d0b6b6" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfBAPIINCOMP_d0b6b6", propOrder = {
    "bapiincomp"
})
public class ArrayOfBAPIINCOMPD0B6B6 {

    @XmlElement(name = "BAPIINCOMP")
    protected List<BAPIINCOMPD0B6B6> bapiincomp;

    /**
     * Gets the value of the bapiincomp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bapiincomp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBAPIINCOMP().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BAPIINCOMPD0B6B6 }
     * 
     * 
     */
    public List<BAPIINCOMPD0B6B6> getBAPIINCOMP() {
        if (bapiincomp == null) {
            bapiincomp = new ArrayList<BAPIINCOMPD0B6B6>();
        }
        return this.bapiincomp;
    }

}