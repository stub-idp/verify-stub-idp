package stubidp.saml.hub.core.transformers.outbound;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.w3c.dom.Element;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.AuthnRequestBuilder.anAuthnRequest;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;

public class EntitiesDescriptorToElementTransformerTest extends OpenSAMLRunner {

    @Test
    void transform_shouldTransformASamlObjectIntoAnElement() {
        AuthnRequest authnRequest = anAuthnRequest().withIssuer(anIssuer().build()).build();
        XmlObjectToElementTransformer<AuthnRequest> transformer = new XmlObjectToElementTransformer<>();

        Element result = transformer.apply(authnRequest);

        assertThat(result).isNotNull();
    }
}
