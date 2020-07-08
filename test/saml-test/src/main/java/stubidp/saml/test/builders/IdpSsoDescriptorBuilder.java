package stubidp.saml.test.builders;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.IDPSSODescriptorBuilder;
import stubidp.test.devpki.TestEntityIds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static stubidp.saml.test.builders.KeyDescriptorBuilder.aKeyDescriptor;
import static stubidp.saml.test.builders.X509CertificateBuilder.aX509Certificate;
import static stubidp.saml.test.builders.X509DataBuilder.aX509Data;

public class IdpSsoDescriptorBuilder {
    private Optional<String> protocol = Optional.of(SAMLConstants.SAML20P_NS);
    private Optional<SingleSignOnService> singleSignOnService = Optional.ofNullable(EndpointBuilder.anEndpoint().buildSingleSignOnService());
    private List<KeyDescriptor> keyDescriptors = new ArrayList<>();
    private boolean addDefaultSigningKey = true;
    private KeyDescriptor defaultSigningKeyDescriptor = aKeyDescriptor()
            .withKeyInfo(KeyInfoBuilder.aKeyInfo()
                    .withKeyName(TestEntityIds.HUB_ENTITY_ID)
                    .withX509Data(aX509Data()
                            .withX509Certificate(aX509Certificate().build())
                            .build())
                    .build())
            .build();

    private IdpSsoDescriptorBuilder() {}

    public static IdpSsoDescriptorBuilder anIdpSsoDescriptor() {
        return new IdpSsoDescriptorBuilder();
    }

    public IDPSSODescriptor build() {
        IDPSSODescriptor descriptor = new IDPSSODescriptorBuilder().buildObject();

        if (protocol.isPresent()) {
            descriptor.addSupportedProtocol(protocol.get());
        }

        if (singleSignOnService.isPresent()) {
            descriptor.getSingleSignOnServices().add(singleSignOnService.get());
        }

        if (addDefaultSigningKey) {
            descriptor.getKeyDescriptors().add(defaultSigningKeyDescriptor);
        }
        for (KeyDescriptor keyDescriptor : keyDescriptors) {
            descriptor.getKeyDescriptors().add(keyDescriptor);
        }
        return descriptor;
    }

    public IdpSsoDescriptorBuilder withSupportedProtocol(String protocol) {
        this.protocol = Optional.ofNullable(protocol);
        return this;
    }

    public IdpSsoDescriptorBuilder withSingleSignOnService(SingleSignOnService singleSignOnService) {
        this.singleSignOnService = Optional.ofNullable(singleSignOnService);
        return this;
    }

    public IdpSsoDescriptorBuilder addKeyDescriptor(KeyDescriptor keyDescriptor) {
        this.keyDescriptors.add(keyDescriptor);
        return this;
    }

    public IdpSsoDescriptorBuilder withoutDefaultSigningKey() {
        this.addDefaultSigningKey = false;
        return this;
    }
}
