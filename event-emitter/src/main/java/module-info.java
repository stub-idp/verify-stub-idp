@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module event.emitter {
    exports stubidp.eventemitter;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.joda;
    requires org.apache.commons.codec;
    requires org.slf4j;

    requires transitive jakarta.inject;
    requires transitive org.joda.time;
    requires transitive jsr305; // https://github.com/google/guava/issues/2960
    requires transitive aws.java.sdk.osgi; // https://github.com/aws/aws-sdk-java/issues/1658
    requires transitive com.fasterxml.jackson.databind;
}