//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.21 at 06:51:43 PM IST 
//


package com.energizer.core.invoicepdf.jaxb.xsd.objects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.energizer.core.invoicepdf.jaxb.xsd.objects package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _BAPIRETURN_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "BAPIRETURN");
    private final static QName _ZBDSBUILDURLFORALBDSCONTREP_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Rfc/", "BDS_CONTREP");
    private final static QName _ZBDSBUILDURLFORALBDSDOCID_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Rfc/", "BDS_DOCID");
    private final static QName _ZBDSBUILDURLFORALBDSDOCUCLASS_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Rfc/", "BDS_DOCUCLASS");
    private final static QName _BAPIRETURNEaf8C4MESSAGE_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "MESSAGE");
    private final static QName _BAPIRETURNEaf8C4LOGNO_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "LOG_NO");
    private final static QName _BAPIRETURNEaf8C4LOGMSGNO_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "LOG_MSG_NO");
    private final static QName _BAPIRETURNEaf8C4MESSAGEV1_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "MESSAGE_V1");
    private final static QName _BAPIRETURNEaf8C4MESSAGEV4_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "MESSAGE_V4");
    private final static QName _BAPIRETURNEaf8C4CODE_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "CODE");
    private final static QName _BAPIRETURNEaf8C4TYPE_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "TYPE");
    private final static QName _BAPIRETURNEaf8C4MESSAGEV2_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "MESSAGE_V2");
    private final static QName _BAPIRETURNEaf8C4MESSAGEV3_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "MESSAGE_V3");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.energizer.core.invoicepdf.jaxb.xsd.objects
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ZBDSBUILDURLFORALResponse }
     * 
     */
    public ZBDSBUILDURLFORALResponse createZBDSBUILDURLFORALResponse() {
        return new ZBDSBUILDURLFORALResponse();
    }

    /**
     * Create an instance of {@link BAPIRETURNEaf8C4 }
     * 
     */
    public BAPIRETURNEaf8C4 createBAPIRETURNEaf8C4() {
        return new BAPIRETURNEaf8C4();
    }

    /**
     * Create an instance of {@link ZBDSBUILDURLFORAL }
     * 
     */
    public ZBDSBUILDURLFORAL createZBDSBUILDURLFORAL() {
        return new ZBDSBUILDURLFORAL();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BAPIRETURNEaf8C4 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "BAPIRETURN")
    public JAXBElement<BAPIRETURNEaf8C4> createBAPIRETURN(BAPIRETURNEaf8C4 value) {
        return new JAXBElement<BAPIRETURNEaf8C4>(_BAPIRETURN_QNAME, BAPIRETURNEaf8C4 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", name = "BDS_CONTREP", scope = ZBDSBUILDURLFORAL.class)
    public JAXBElement<String> createZBDSBUILDURLFORALBDSCONTREP(String value) {
        return new JAXBElement<String>(_ZBDSBUILDURLFORALBDSCONTREP_QNAME, String.class, ZBDSBUILDURLFORAL.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", name = "BDS_DOCID", scope = ZBDSBUILDURLFORAL.class)
    public JAXBElement<String> createZBDSBUILDURLFORALBDSDOCID(String value) {
        return new JAXBElement<String>(_ZBDSBUILDURLFORALBDSDOCID_QNAME, String.class, ZBDSBUILDURLFORAL.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", name = "BDS_DOCUCLASS", scope = ZBDSBUILDURLFORAL.class)
    public JAXBElement<String> createZBDSBUILDURLFORALBDSDOCUCLASS(String value) {
        return new JAXBElement<String>(_ZBDSBUILDURLFORALBDSDOCUCLASS_QNAME, String.class, ZBDSBUILDURLFORAL.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "MESSAGE", scope = BAPIRETURNEaf8C4 .class)
    public JAXBElement<String> createBAPIRETURNEaf8C4MESSAGE(String value) {
        return new JAXBElement<String>(_BAPIRETURNEaf8C4MESSAGE_QNAME, String.class, BAPIRETURNEaf8C4 .class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "LOG_NO", scope = BAPIRETURNEaf8C4 .class)
    public JAXBElement<String> createBAPIRETURNEaf8C4LOGNO(String value) {
        return new JAXBElement<String>(_BAPIRETURNEaf8C4LOGNO_QNAME, String.class, BAPIRETURNEaf8C4 .class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "LOG_MSG_NO", scope = BAPIRETURNEaf8C4 .class)
    public JAXBElement<Integer> createBAPIRETURNEaf8C4LOGMSGNO(Integer value) {
        return new JAXBElement<Integer>(_BAPIRETURNEaf8C4LOGMSGNO_QNAME, Integer.class, BAPIRETURNEaf8C4 .class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "MESSAGE_V1", scope = BAPIRETURNEaf8C4 .class)
    public JAXBElement<String> createBAPIRETURNEaf8C4MESSAGEV1(String value) {
        return new JAXBElement<String>(_BAPIRETURNEaf8C4MESSAGEV1_QNAME, String.class, BAPIRETURNEaf8C4 .class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "MESSAGE_V4", scope = BAPIRETURNEaf8C4 .class)
    public JAXBElement<String> createBAPIRETURNEaf8C4MESSAGEV4(String value) {
        return new JAXBElement<String>(_BAPIRETURNEaf8C4MESSAGEV4_QNAME, String.class, BAPIRETURNEaf8C4 .class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "CODE", scope = BAPIRETURNEaf8C4 .class)
    public JAXBElement<String> createBAPIRETURNEaf8C4CODE(String value) {
        return new JAXBElement<String>(_BAPIRETURNEaf8C4CODE_QNAME, String.class, BAPIRETURNEaf8C4 .class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "TYPE", scope = BAPIRETURNEaf8C4 .class)
    public JAXBElement<String> createBAPIRETURNEaf8C4TYPE(String value) {
        return new JAXBElement<String>(_BAPIRETURNEaf8C4TYPE_QNAME, String.class, BAPIRETURNEaf8C4 .class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "MESSAGE_V2", scope = BAPIRETURNEaf8C4 .class)
    public JAXBElement<String> createBAPIRETURNEaf8C4MESSAGEV2(String value) {
        return new JAXBElement<String>(_BAPIRETURNEaf8C4MESSAGEV2_QNAME, String.class, BAPIRETURNEaf8C4 .class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "MESSAGE_V3", scope = BAPIRETURNEaf8C4 .class)
    public JAXBElement<String> createBAPIRETURNEaf8C4MESSAGEV3(String value) {
        return new JAXBElement<String>(_BAPIRETURNEaf8C4MESSAGEV3_QNAME, String.class, BAPIRETURNEaf8C4 .class, value);
    }

}