package uk.gov.ida.matchingserviceadapter.rest.soap;

import net.shibboleth.utilities.java.support.xml.SimpleNamespaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import stubidp.utils.common.xml.XmlUtils;
import uk.gov.ida.matchingserviceadapter.exceptions.SoapUnwrappingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Map;

import static java.text.MessageFormat.format;
import static stubidp.utils.common.xml.XmlUtils.newDocumentBuilder;

public class SoapMessageManager {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final Logger LOG = LoggerFactory.getLogger(SoapMessageManager.class);

    public Document wrapWithSoapEnvelope(Element element) {
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOG.error("*** ALERT: Failed to create a document builder when trying to construct the the soap message. ***", e);
            throw new RuntimeException(e);
        }
        Document document = documentBuilder.newDocument();
        document.adoptNode(element);
        Element envelope = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Envelope");
        Element body = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Body");
        envelope.appendChild(body);
        body.appendChild(element);
        document.appendChild(envelope);

        return document;
    }

    public Element unwrapSoapMessage(Document document, SamlElementType samlElementType) {
        return unwrapSoapMessage(document.getDocumentElement(), samlElementType);
    }

    private Element unwrapSoapMessage(Element soapElement, SamlElementType samlElementType) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        SimpleNamespaceContext context = new SimpleNamespaceContext(Map.of(
                "soapenv", "http://schemas.xmlsoap.org/soap/envelope/",
                "samlp", "urn:oasis:names:tc:SAML:2.0:protocol",
                "saml", "urn:oasis:names:tc:SAML:2.0:assertion",
                "ds", "http://www.w3.org/2000/09/xmldsig#"
        ));
        xpath.setNamespaceContext(context);
        try {
            String expression = "//samlp:" + samlElementType.getElementName();
            Element element = (Element) xpath.evaluate(expression, soapElement, XPathConstants.NODE);

            if (element == null) {
                String errorMessage = format("Document{0}{1}{0}does not have element {2} inside it.", NEW_LINE,
                        XmlUtils.writeToString(soapElement), expression);
                LOG.error(errorMessage);
                throw new SoapUnwrappingException(errorMessage);
            }

            return element;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

}
