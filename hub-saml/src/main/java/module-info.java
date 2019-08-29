@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml.hub {
    requires stubidp.saml.utils;
    requires stubidp.saml.security;
    requires stubidp.saml.extensions;
    requires org.joda.time;
    requires jakarta.inject;
    requires dropwizard.util;
    requires commons.lang;
}
