@SuppressWarnings("requires-automatic")
module common.utils {
    exports stubidp.utils.common.string;

    opens stubidp.utils.common.logging; // for LevelLoggerTest

    requires org.slf4j;
    requires java.xml;
    requires org.joda.time;
    requires commons.validator;
    requires com.google.common;
    requires java.validation;
    requires org.apache.commons.codec;
    requires com.fasterxml.jackson.databind;
    requires dropwizard.configuration;
}