package uk.gov.ida.verifyserviceprovider.factories.saml;

import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import stubidp.saml.security.MetadataBackedSignatureValidator;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.SamlMessageSignatureValidator;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public class SignatureValidatorFactory {

    public SamlAssertionsSignatureValidator getSignatureValidator(@NotNull ExplicitKeySignatureTrustEngine trustEngine) {
        return Optional.of(trustEngine)
            .map(MetadataBackedSignatureValidator::withoutCertificateChainValidation)
            .map(SamlMessageSignatureValidator::new)
            .map(SamlAssertionsSignatureValidator::new)
            .get();
    }
}
