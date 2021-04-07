package stubidp.utils.common.xml;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;
import stubidp.utils.common.xml.XmlUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class XmlUtilsTest {
    /**
     * Test for protection against the Billion Laughs attack.
     * @see https://en.wikipedia.org/wiki/Billion_laughs
     */
    @Test
    void convertToElement_shouldDealWithEntityExpansionAttacks() throws Exception {
        String xmlString = """
                <?xml version="1.0"?>
                <!DOCTYPE lolz [
                 <!ENTITY lol "lol">
                 <!ELEMENT lolz (#PCDATA)>
                 <!ENTITY lol1 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
                 <!ENTITY lol2 "&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;">
                 <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
                 <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
                 <!ENTITY lol5 "&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;">
                 <!ENTITY lol6 "&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;">
                 <!ENTITY lol7 "&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;">
                 <!ENTITY lol8 "&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;">
                 <!ENTITY lol9 "&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;">
                ]>
                <lolz>&lol9;</lolz>""";

        try {
            XmlUtils.convertToElement(xmlString);
            fail("fail");
        } catch (SAXParseException e) {
            // Exception was expected.
        }
    }

    /**
     * Test to prevent XML External Entity processing (XXE attacks), i.e. access
     * to arbitrary files etc. on the processing system.
     * @see https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
     */
    @Test
    void convertToElement_shouldThrowExceptionIfProvidedWithDoctypeDeclaration() throws Exception {
        String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
                "<!DOCTYPE foo [" +
                "  <!ELEMENT foo ANY >" +
                "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]><foo>&xxe;</foo>";
        try {
            XmlUtils.convertToElement(xmlString);
            fail("expected exception not thrown");
        } catch (SAXParseException e) {
            assertThat(e.getMessage()).contains("DOCTYPE is disallowed");
        }
    }
}

