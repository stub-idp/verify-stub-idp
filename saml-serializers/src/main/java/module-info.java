@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml.serializers {
    opens stubidp.saml.serializers.deserializers.validators;

    exports stubidp.saml.serializers.deserializers.validators;
    exports stubidp.saml.serializers.serializers;
    exports stubidp.saml.serializers.deserializers;
    exports stubidp.saml.serializers.deserializers.parser;

    requires stubidp.saml.extensions;

    requires transitive org.opensaml.core;
    requires transitive net.shibboleth.utilities.java.support;
    requires transitive java.xml;
}
