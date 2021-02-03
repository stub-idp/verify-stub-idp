package uk.gov.ida.matchingserviceadapter.rest.soap;

import net.shibboleth.utilities.java.support.xml.SimpleNamespaceContext;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SoapMessageManagerTest {

    @Test
    public void wrapWithSoapEnvelope_shouldWrapElementInsideSoapMessageBody() throws Exception {
        Element element = getTestElement();

        SoapMessageManager manager = new SoapMessageManager();
        Document soapMessage = manager.wrapWithSoapEnvelope(element);

        assertThat(getAttributeQuery(soapMessage)).isNotNull();
    }

    @Test
    public void unwrapSoapMessage_shouldUnwrapElementInsideSoapMessageBody() throws Exception {
        Element element = getTestElement();

        SoapMessageManager manager = new SoapMessageManager();
        Document soapMessage = manager.wrapWithSoapEnvelope(element);

        Element unwrappedElement = manager.unwrapSoapMessage(soapMessage, SamlElementType.Response);

        assertThat(unwrappedElement).isNotNull();
        assertThat(unwrappedElement.getTagName()).isEqualTo("samlp:Response");
    }

    @Test
    public void unwrapSoapMessage_shouldThrowExceptionIfSoapUnwrappingFails() throws Exception {
        Element element = XmlUtils.newDocumentBuilder().newDocument().createElement("foo");

        SoapMessageManager manager = new SoapMessageManager();
        Document soapMessage = manager.wrapWithSoapEnvelope(element);

        assertThrows(SoapUnwrappingException.class, () -> manager.unwrapSoapMessage(soapMessage, SamlElementType.Response));
    }

    private Element getTestElement() throws ParserConfigurationException {
        DocumentBuilder documentBuilder = XmlUtils.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        return document.createElementNS("urn:oasis:names:tc:SAML:2.0:protocol", "samlp:Response");
    }

    private Element getAttributeQuery(Document document) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        SimpleNamespaceContext context = new SimpleNamespaceContext(Map.of(
                "soapenv", "http://schemas.xmlsoap.org/soap/envelope/",
                "samlp", "urn:oasis:names:tc:SAML:2.0:protocol"
        ));
        xpath.setNamespaceContext(context);

        return (Element) xpath.evaluate("//samlp:Response", document, XPathConstants.NODE);
    }
}
