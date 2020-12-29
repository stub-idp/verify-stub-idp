package stubidp.utils.rest.resources;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import stubidp.utils.rest.configuration.ServiceStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceStatusResourceTest {

    final ServiceStatus instance  = ServiceStatus.getInstance();

    @Test
    public void shouldReturn200WhenInitialised() {
        ServiceStatusResource serviceStatusResource = new ServiceStatusResource();
        assertThat(serviceStatusResource.isOnline().getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldReturn503WhenServiceStatusIsFalse() {
        ServiceStatusResource serviceStatusResource = new ServiceStatusResource();
        instance.setServiceStatus(false);
        assertThat(serviceStatusResource.isOnline().getStatus()).isEqualTo(503);
    }

    @AfterEach
    public void tearDown(){
        instance.setServiceStatus(true);
    }
}
