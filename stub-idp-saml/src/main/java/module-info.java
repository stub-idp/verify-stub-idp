@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stub.idp.saml {
    exports stubidp.saml.stubidp.configuration;
    exports stubidp.saml.stubidp.stub.transformers.outbound;
    exports stubidp.saml.stubidp.stub.transformers.inbound;
    
    requires transitive jakarta.inject;
    requires transitive org.opensaml.saml;
    requires transitive saml.utils;
    requires transitive saml.security;
    requires transitive java.validation;

    requires org.joda.time;
    requires saml.extensions;
    requires com.fasterxml.jackson.annotation;
    requires saml.serializers; // compile dependency, but for tests
}