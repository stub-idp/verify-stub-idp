package stubidp.stubidp.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.configuration.SingleIdpConfiguration;
import stubidp.stubidp.domain.Service;
import stubidp.stubidp.exceptions.FeatureNotEnabledException;
import stubidp.utils.rest.jerseyclient.JsonClient;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class ServiceListService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceListService.class);
    private enum Source { Hub }

    private final SingleIdpConfiguration singleIdpConfiguration;
    private final JsonClient jsonClient;
    private final LoadingCache<Source, List<Service>> servicesCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(5))
            .build(new CacheLoader<>() {
                @Override
                public List<Service> load(Source key) {
                    return readServices();
                }
            });

    @Inject
    public ServiceListService(SingleIdpConfiguration singleIdpConfiguration, JsonClient jsonClient) {
        this.singleIdpConfiguration = singleIdpConfiguration;
        this.jsonClient = jsonClient;
    }

    public List<Service> getServices() {
        if (!singleIdpConfiguration.isEnabled()) {
            throw new FeatureNotEnabledException();
        }

        return servicesCache.getUnchecked(Source.Hub);
    }

    private List<Service> readServices() {
        try {
            return jsonClient.get(singleIdpConfiguration.getServiceListUri(), new GenericType<List<Service>>() {});
        } catch (RuntimeException ex) {
            LOG.error(MessageFormat.format("Error getting service list from {0}", singleIdpConfiguration.getServiceListUri().toString()), ex);
        }
        return Collections.emptyList();
    }
}
