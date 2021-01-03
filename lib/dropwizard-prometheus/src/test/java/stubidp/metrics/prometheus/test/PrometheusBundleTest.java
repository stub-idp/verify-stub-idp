package stubidp.metrics.prometheus.test;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.metrics.prometheus.config.TestApplication;
import stubidp.metrics.prometheus.config.TestConfiguration;
import stubidp.metrics.prometheus.bundle.PrometheusBundle;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.metrics.prometheus.config.TestResource.TEST_RESOURCE_PATH;

@ExtendWith(DropwizardExtensionsSupport.class)
public class PrometheusBundleTest {

    private static final DropwizardAppExtension<TestConfiguration> appRuleWithMetrics = new DropwizardAppExtension<>(TestApplication.class, null,
            ConfigOverride.config("logging.level", "WARN"),
            ConfigOverride.config("server.applicationConnectors[0].port", "0"),
            ConfigOverride.config("server.adminConnectors[0].port", "0"),
            ConfigOverride.config("prometheusEnabled", "true"));

    private static final DropwizardAppExtension<TestConfiguration> appRuleWithoutMetrics = new DropwizardAppExtension<>(TestApplication.class, null,
            ConfigOverride.config("logging.level", "WARN"),
            ConfigOverride.config("server.applicationConnectors[0].port", "0"),
            ConfigOverride.config("server.adminConnectors[0].port", "0"),
            ConfigOverride.config("prometheusEnabled", "false"));

    private final Client client = new JerseyClientBuilder().build();

    @Test
    void aDropwizardResourceTimerMetricIsLogged() {
        Response response = client.target("http://localhost:" + appRuleWithMetrics.getAdminPort() + PrometheusBundle.PROMETHEUS_METRICS_RESOURCE)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("stubidp_metrics_prometheus_config_TestResource_get_count 0");

        response = client.target("http://localhost:" + appRuleWithMetrics.getLocalPort() + TEST_RESOURCE_PATH)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("hello");

        response = client.target("http://localhost:" + appRuleWithMetrics.getAdminPort() + PrometheusBundle.PROMETHEUS_METRICS_RESOURCE)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("stubidp_metrics_prometheus_config_TestResource_get_count 1.0");
    }

    @Test
    void noDropwizardJvmMetricsAreLogged() {
        final Response response = client.target("http://localhost:" + appRuleWithMetrics.getAdminPort() + PrometheusBundle.PROMETHEUS_METRICS_RESOURCE)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);

        // see comment on PrometheusBundle.isNotJvmMetric()
        String entity = response.readEntity(String.class);
        assertThat(entity).doesNotContain("jvm_threads_daemon_count");
        assertThat(entity).doesNotContain("Generated from Dropwizard metric import (metric=jvm.threads.daemon.count");
    }

    @Test
    void metricsAreNotPresentWhenMetricsAreDisabled() {
        final Response response = client.target("http://localhost:" + appRuleWithoutMetrics.getAdminPort() + PrometheusBundle.PROMETHEUS_METRICS_RESOURCE)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(404);
    }

}
