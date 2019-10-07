package stubsp.stubsp.services;

import stubsp.stubsp.configuration.StubSpConfiguration;
import stubsp.stubsp.domain.AvailableServiceDto;

import javax.inject.Inject;
import java.util.List;

public class AvailableServicesService {

    private final StubSpConfiguration stubSpConfiguration;

    @Inject
    public AvailableServicesService(StubSpConfiguration stubSpConfiguration) {

        this.stubSpConfiguration = stubSpConfiguration;
    }

    public List<AvailableServiceDto> getAvailableServices() {
        return List.of(new AvailableServiceDto(stubSpConfiguration.getServiceName(), "LEVEL_2", "", "Test Services"));
    }
}
