@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml {
    opens stubidp.saml.stubidp;
    opens stubidp.saml.stubidp.configuration;
    opens stubidp.saml.stubidp.stub.transformers.inbound;
    opens stubidp.saml.stubidp.stub.transformers.outbound;

    exports stubidp.saml.stubidp;
    exports stubidp.saml.stubidp.configuration;
    exports stubidp.saml.stubidp.stub.transformers.outbound;
    exports stubidp.saml.stubidp.stub.transformers.inbound;
    
    requires transitive jakarta.inject;
    requires transitive org.opensaml.saml;
    requires transitive stubidp.saml.utils;
    requires transitive stubidp.saml.security;
    requires transitive java.validation;

    requires stubidp.saml.extensions;
    requires com.fasterxml.jackson.annotation;
    requires stubidp.saml.serializers; // compile dependency, but for tests
}