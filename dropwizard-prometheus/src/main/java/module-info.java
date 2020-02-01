@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.metrics.prometheus {
    opens stubidp.metrics.prometheus.config;

    exports stubidp.metrics.prometheus.bundle;
    exports stubidp.metrics.prometheus.config;

    requires transitive dropwizard.core;
    requires transitive java.servlet;

    requires com.codahale.metrics;
    requires simpleclient;
    requires simpleclient.dropwizard;
    requires simpleclient.servlet;
    requires simpleclient.hotspot;
}