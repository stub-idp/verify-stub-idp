@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml.domain {
    exports stubidp.saml.domain.assertions;
    exports stubidp.saml.domain.matching.assertions;
    exports stubidp.saml.domain.matching;
    exports stubidp.saml.domain.request;
    exports stubidp.saml.domain;
    exports stubidp.saml.domain.response;

    requires transitive org.opensaml.saml;

    requires com.fasterxml.jackson.annotation;
    requires stubidp.saml.extensions;
    requires org.opensaml.xmlsec;
}