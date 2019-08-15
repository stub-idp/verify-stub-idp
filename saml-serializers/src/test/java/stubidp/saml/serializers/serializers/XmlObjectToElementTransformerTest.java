package stubidp.saml.serializers.serializers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.w3c.dom.Element;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlObjectToElementTransformerTest {

    @BeforeEach
    public void setup() throws InitializationException {
        InitializationService.initialize();
    }

    @Test
    public void shouldTransformObjectToElement() {
        AuthnRequest authnRequest = new AuthnRequestBuilder().buildObject();
        Element element = new XmlObjectToElementTransformer<>().apply(authnRequest);
        assertThat(element.getTagName()).isEqualTo("saml2p:AuthnRequest");
    }

}