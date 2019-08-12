@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module trust.anchor {
    exports stubidp.eidas.trustanchor;

    requires transitive nimbus.jose.jwt;

    requires org.opensaml.core;
    requires org.opensaml.saml;
    requires org.opensaml.security;
    requires org.opensaml.xmlsec;
    requires java.xml;
    requires xmlsec;
    requires net.shibboleth.utilities.java.support;
    requires saml.serializers;
    requires org.opensaml.security.impl;
    requires org.opensaml.xmlsec.impl;
    requires org.opensaml.saml.impl;
    requires com.google.common;
}