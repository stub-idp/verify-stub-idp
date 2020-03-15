package stubidp.stubidp.repositories.reaper;

import io.dropwizard.lifecycle.Managed;
import org.apache.log4j.Logger;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.repositories.IdpSessionRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Delete stale sessions
 */
@Singleton
public class ManagedStaleSessionReaper implements Managed {

    private static final Logger LOGGER = Logger.getLogger(ManagedStaleSessionReaper.class);

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private final StaleSessionReaperConfiguration staleSessionReaperConfiguration;
    private final IdpSessionRepository verifySessionRepository;
    private final long terminationTimeoutSeconds;
    private final long reaperFrequencyInSeconds;

    @Inject
    public ManagedStaleSessionReaper(StubIdpConfiguration stubIdpConfiguration,
                                     IdpSessionRepository verifySessionRepository) {
        this.staleSessionReaperConfiguration = stubIdpConfiguration.getStaleSessionReaperConfiguration();
        this.verifySessionRepository = verifySessionRepository;
        this.terminationTimeoutSeconds = staleSessionReaperConfiguration.getTerminationTimeout().toSeconds();
        this.reaperFrequencyInSeconds = staleSessionReaperConfiguration.getReaperFrequency().getSeconds();
    }

    @Override
    public void start() {
        LOGGER.info("installing stale session reaper");
        final StaleSessionReaper staleSessionReaper = new StaleSessionReaper(verifySessionRepository, staleSessionReaperConfiguration);
        scheduledExecutorService.scheduleWithFixedDelay(staleSessionReaper,
                reaperFrequencyInSeconds,
                reaperFrequencyInSeconds,
                TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        LOGGER.info("shutting down; waiting for any active reapers to finish");
        scheduledExecutorService.shutdown();
        try {
            scheduledExecutorService.awaitTermination(terminationTimeoutSeconds, TimeUnit.SECONDS);
        } catch(InterruptedException e) {
            LOGGER.warn("reaper was terminated before it had completed running");
        }
    }
}
