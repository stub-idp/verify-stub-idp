package stubidp.saml.test.builders;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import stubidp.test.devpki.TestEntityIds;

import java.util.ArrayList;
import java.util.List;

import static stubidp.saml.test.builders.X509CertificateBuilder.aX509Certificate;
import static stubidp.saml.test.builders.X509DataBuilder.aX509Data;

public class SPSSODescriptorBuilder {
    private final List<String> supportedProtocols = new ArrayList<>();
    private final List<KeyDescriptor> keyDescriptors = new ArrayList<>();
    private boolean addDefaultSigningKey = true;
    private boolean addDefaultEncryptionKey = true;
    private final KeyDescriptor defaultSigningKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(KeyInfoBuilder.aKeyInfo().withKeyName(TestEntityIds.HUB_ENTITY_ID).withX509Data(aX509Data().withX509Certificate(aX509Certificate().build()).build()).build()).withUse(UsageType.SIGNING.toString()).build();
    private final KeyDescriptor defaultEncryptionKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(KeyInfoBuilder.aKeyInfo().withKeyName(TestEntityIds.HUB_ENTITY_ID).withX509Data(aX509Data().withX509Certificate(aX509Certificate().build()).build()).build()).withUse(UsageType.ENCRYPTION.toString()).build();
    private final List<AssertionConsumerService> assertionConsumerServices = new ArrayList<>();
    private final AssertionConsumerService defaultAssertionConsumerService = AssertionConsumerServiceBuilder.anAssertionConsumerService().build();
    private boolean addDefaultAssertionConsumerService = true;
    private boolean addDefaultSupportedProtocol = true;
    private final String defaultSupportedProtocol = SAMLConstants.SAML20P_NS;

    private SPSSODescriptorBuilder() {}

    public static SPSSODescriptorBuilder anSpServiceDescriptor() {
        return new SPSSODescriptorBuilder();
    }

    public SPSSODescriptor build() {
        SPSSODescriptor descriptor = new org.opensaml.saml.saml2.metadata.impl.SPSSODescriptorBuilder().buildObject();

        if (addDefaultSupportedProtocol) {
            descriptor.addSupportedProtocol(defaultSupportedProtocol);
        }
        for (String protocol : supportedProtocols) {
            descriptor.addSupportedProtocol(protocol);
        }

        if (addDefaultAssertionConsumerService) {
            descriptor.getAssertionConsumerServices().add(defaultAssertionConsumerService);
        }
        for (AssertionConsumerService service : this.assertionConsumerServices) {
            descriptor.getAssertionConsumerServices().add(service);
        }

        if (addDefaultSigningKey) {
            descriptor.getKeyDescriptors().add(defaultSigningKeyDescriptor);
        }
        if (addDefaultEncryptionKey) {
            descriptor.getKeyDescriptors().add(defaultEncryptionKeyDescriptor);
        }
        for (KeyDescriptor keyDescriptor : keyDescriptors) {
            descriptor.getKeyDescriptors().add(keyDescriptor);
        }

        return descriptor;
    }

    public SPSSODescriptorBuilder addSupportedProtocol(String protocol) {
        this.supportedProtocols.add(protocol);
        this.addDefaultSupportedProtocol = false;
        return this;
    }

    public SPSSODescriptorBuilder withoutDefaultSupportingProtocol() {
        this.addDefaultSupportedProtocol = false;
        return this;
    }

    public SPSSODescriptorBuilder addKeyDescriptor(KeyDescriptor keyDescriptor) {
        this.keyDescriptors.add(keyDescriptor);
        return this;
    }

    public SPSSODescriptorBuilder withoutDefaultSigningKey() {
        this.addDefaultSigningKey = false;
        return this;
    }

    public SPSSODescriptorBuilder withoutDefaultEncryptionKey() {
        this.addDefaultEncryptionKey = false;
        return this;
    }

    public SPSSODescriptorBuilder addAssertionConsumerService(AssertionConsumerService service) {
        this.addDefaultAssertionConsumerService = false;
        this.assertionConsumerServices.add(service);
        return this;
    }

    public SPSSODescriptorBuilder withoutDefaultAssertionConsumerService() {
        this.addDefaultAssertionConsumerService = false;
        return this;
    }
}
