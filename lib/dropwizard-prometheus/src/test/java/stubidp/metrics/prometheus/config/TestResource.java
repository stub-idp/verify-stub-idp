package stubidp.metrics.prometheus.config;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static stubidp.metrics.prometheus.config.TestResource.TEST_RESOURCE_PATH;

@Path(TEST_RESOURCE_PATH)
public class TestResource {

    public static final String TEST_RESOURCE_PATH = "/";

    @Timed
    @GET
    public String get() {
        return "hello";
    }
}
