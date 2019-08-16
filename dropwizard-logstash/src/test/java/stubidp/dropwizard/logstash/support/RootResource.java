package stubidp.dropwizard.logstash.support;

import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class RootResource {
    private static final Logger LOG = Logger.getLogger(RootResource.class);
    public static String TEST_LOG_LINE = "test log line";

    @GET
    public String get() {
        return "hello!";
    }
    @GET
    @Path("log")
    public void log() {
        LOG.info(TEST_LOG_LINE);
    }
}
