package stubidp.stubidp.listeners;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.repositories.IdpStubsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Singleton
public class ManagedStubIdpsFileListener implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedStubIdpsFileListener.class);
    private static final long terminationTimeoutSeconds = 5;

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private final IdpStubsRepository idpStubsRepository;
    private final String stubIdpsYmlFileLocation;
    private final Duration stubIdpYmlFileRefresh;

    @Inject
    public ManagedStubIdpsFileListener(StubIdpConfiguration stubIdpConfiguration,
                                       IdpStubsRepository idpStubsRepository) {
        this.stubIdpsYmlFileLocation = stubIdpConfiguration.getStubIdpsYmlFileLocation();
        this.stubIdpYmlFileRefresh = stubIdpConfiguration.getStubIdpYmlFileRefresh();
        this.idpStubsRepository = idpStubsRepository;
    }

    @Override
    public void start() {
        LOGGER.info("installing stubIdpsFileListener");
        final StubIdpsFileListener stubIdpsFileListener = new StubIdpsFileListener(stubIdpsYmlFileLocation, idpStubsRepository);
        scheduledExecutorService.scheduleWithFixedDelay(stubIdpsFileListener,
                stubIdpYmlFileRefresh.toMilliseconds(),
                stubIdpYmlFileRefresh.toMilliseconds(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        scheduledExecutorService.shutdown();
        try {
            scheduledExecutorService.awaitTermination(terminationTimeoutSeconds, TimeUnit.SECONDS);
        } catch(InterruptedException e) {
            LOGGER.warn("stubIdpsFileListener was terminated before it had completed running");
        }
    }
}
