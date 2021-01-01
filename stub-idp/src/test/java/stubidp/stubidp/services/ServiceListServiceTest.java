package stubidp.stubidp.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.stubidp.configuration.SingleIdpConfiguration;
import stubidp.stubidp.domain.Service;
import stubidp.stubidp.exceptions.FeatureNotEnabledException;
import stubidp.utils.rest.jerseyclient.JsonClient;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.GenericType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceListServiceTest {

    private final Service service1 = new Service("Service Description 1", "LEVEL_2", "service-description-1-entity-id", "Service Category A");
    private final Service service2 = new Service("Service Description 2", "LEVEL_1", "service-description-2-entity-id", "Service Category A");
    private final Service service3 = new Service("Service Description 3", "LEVEL_2", "service-description-3-entity-id", "Service Category B");
    private final Service service4 = new Service("Service Description 4", "LEVEL_2", "service-description-4-entity-id", "Service Category B");
    private final Service service5 = new Service("Service Description 5", "LEVEL_1", "service-description-5-entity-id", "Service Category B");

    private final URI uri = URI.create("http://localhost/get-available-services");

    @Mock
    private SingleIdpConfiguration singleIdpConfiguration;

    @Mock
    private JsonClient jsonClient;

    @Test
    public void getServiceListWhenFeatureDisabled_ThrowsFeatureNotEnabledException() throws FeatureNotEnabledException {
        when(singleIdpConfiguration.isEnabled()).thenReturn(false);

        ServiceListService service = new ServiceListService(singleIdpConfiguration, jsonClient);

        Assertions.assertThrows(FeatureNotEnabledException.class, service::getServices);
    }

    @Test
    public void getServiceListWhenFeatureEnabled_ReturnsAListOfServices() throws FeatureNotEnabledException {
        when(singleIdpConfiguration.getServiceListUri()).thenReturn(uri);
        when(singleIdpConfiguration.isEnabled()).thenReturn(true);
        when(jsonClient.get(eq(uri), ArgumentMatchers.<GenericType<List<Service>>>any())).thenReturn(Arrays.asList(service1, service2, service3, service4, service5));

        ServiceListService service = new ServiceListService(singleIdpConfiguration, jsonClient);

        List<Service> services = service.getServices();

        assertThat(services.size()).isEqualTo(5);
    }

    @Test
    public void getServiceListWhenFeatureEnabledButErrorReadingFromHub_ReturnEmptyArray() throws FeatureNotEnabledException {
        when(singleIdpConfiguration.getServiceListUri()).thenReturn(uri);
        when(singleIdpConfiguration.isEnabled()).thenReturn(true);
        when(jsonClient.get(eq(uri), ArgumentMatchers.<GenericType<List<Service>>>any())).thenThrow(new ProcessingException("No Response from Server"));

        ServiceListService service = new ServiceListService(singleIdpConfiguration, jsonClient);

        List<Service> services = service.getServices();

        assertThat(services.size()).isEqualTo(0);
    }

    @Test
    public void getServiceListWhenFeatureEnabledButNoServicesReturned_ReturnEmptyArray() throws FeatureNotEnabledException {
        when(singleIdpConfiguration.getServiceListUri()).thenReturn(uri);
        when(singleIdpConfiguration.isEnabled()).thenReturn(true);
        when(jsonClient.get(eq(uri), ArgumentMatchers.<GenericType<List<Service>>>any())).thenReturn(new ArrayList<>());

        ServiceListService service = new ServiceListService(singleIdpConfiguration, jsonClient);

        List<Service> services = service.getServices();

        assertThat(services.size()).isEqualTo(0);
    }

}
