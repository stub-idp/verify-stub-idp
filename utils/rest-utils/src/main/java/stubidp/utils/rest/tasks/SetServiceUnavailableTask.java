package stubidp.utils.rest.tasks;

import io.dropwizard.servlets.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.utils.rest.configuration.ServiceStatus;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class SetServiceUnavailableTask extends Task {
    private static final Logger LOG = LoggerFactory.getLogger(SetServiceUnavailableTask.class);

    private ServiceStatus serviceStatus;

    public SetServiceUnavailableTask(final ServiceStatus serviceStatus){
        super("set-service-unavailable");
        this.serviceStatus = serviceStatus;
    }

    @Override
    public void execute(final Map<String, List<String>> parameters, final PrintWriter output) throws Exception {
        LOG.info("Setting service status to unavailable");
        serviceStatus.setServiceStatus(false);
    }
}
