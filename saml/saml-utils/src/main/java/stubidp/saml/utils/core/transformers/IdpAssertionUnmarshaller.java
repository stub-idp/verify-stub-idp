package stubidp.saml.utils.core.transformers;

import org.opensaml.saml.saml2.core.Assertion;

public interface IdpAssertionUnmarshaller<T> {
    T fromAssertion(Assertion assertion);
}
