package stubidp.stubidp.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.stubidp.configuration.SingleIdpConfiguration;
import stubidp.stubidp.domain.Service;
import stubidp.stubidp.exceptions.FeatureNotEnabledException;
import stubidp.stubidp.repositories.AllIdpsUserRepository;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.resources.singleidp.SingleIdpStartPromptPageResource;
import stubidp.stubidp.services.ServiceListService;
import stubidp.stubidp.views.SingleIdpPromptPageView;
import stubidp.test.devpki.TestEntityIds;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SingleIdpPromptResourceTest {

    private final URI verifySubmissionUri = URI.create("http://localhost/initiate-single-idp-journey");
    private final Service service1 = new Service("Service 1", "LEVEL_2", "service-id-1", "Category A");
    private final Service service2 = new Service("Service 2", "LEVEL_1", "service-id-2", "Category B");
    private final Service service3 = new Service("Service 3", "LEVEL_2", "service-id-3", "Category A");

    @Mock
    private IdpStubsRepository idpStubsRepository;

    @Mock
    private SingleIdpConfiguration singleIdpConfiguration;

    @Mock
    private ServiceListService serviceListService;

    @Mock
    private AllIdpsUserRepository allIdpsUserRepository;

    @Mock
    private IdpSessionRepository idpSessionRepository;

    private SingleIdpStartPromptPageResource singleIdpStartPromptPageResource;

    private final String idpName = "idpName";
    private final Idp idp = new Idp(idpName, "Test Idp", "test-idp-asset-id", true, TestEntityIds.STUB_IDP_ONE, null);

    @BeforeEach
    void setUp() {
        singleIdpStartPromptPageResource = new SingleIdpStartPromptPageResource(idpStubsRepository, serviceListService, singleIdpConfiguration, idpSessionRepository);
    }

    @Test
    void shouldThrowIfFeatureDisabled() {
        when(singleIdpConfiguration.isEnabled()).thenReturn(false);

        assertThatExceptionOfType(FeatureNotEnabledException.class)
            .isThrownBy(()-> singleIdpStartPromptPageResource.get(idpName, Optional.empty(),Optional.empty(), null));
    }

    @Test
    void shouldReturnPageViewWithEmptyListIfHubReturnsNoServices() throws FeatureNotEnabledException {
        when(singleIdpConfiguration.isEnabled()).thenReturn(true);
        when(singleIdpConfiguration.getVerifySubmissionUri()).thenReturn(verifySubmissionUri);
        when(idpStubsRepository.getIdpWithFriendlyId(idpName)).thenReturn(idp);
        when(serviceListService.getServices()).thenReturn(new ArrayList<>());

        Response response = singleIdpStartPromptPageResource.get(idpName, Optional.empty(), Optional.empty(), null);

        assertThat(response.getEntity()).isInstanceOf(SingleIdpPromptPageView.class);

        SingleIdpPromptPageView promptView = (SingleIdpPromptPageView) response.getEntity();
        assertThat(promptView.getServices().size()).isEqualTo(0);
        assertThat(promptView.getVerifySubmissionUrl()).isEqualTo(verifySubmissionUri);
    }

    @Test
    void shouldReturnPageViewWithSortedListIfHubReturnsPopulatedListOfServices() throws FeatureNotEnabledException {
        when(singleIdpConfiguration.isEnabled()).thenReturn(true);
        when(singleIdpConfiguration.getVerifySubmissionUri()).thenReturn(verifySubmissionUri);
        when(idpStubsRepository.getIdpWithFriendlyId(idpName)).thenReturn(idp);
        when(serviceListService.getServices()).thenReturn(Arrays.asList(service1, service2, service3));

        Response response = singleIdpStartPromptPageResource.get(idpName, Optional.empty(), Optional.empty(),null);

        assertThat(response.getEntity()).isInstanceOf(SingleIdpPromptPageView.class);

        SingleIdpPromptPageView promptView = (SingleIdpPromptPageView) response.getEntity();
        assertThat(promptView.getServices().size()).isEqualTo(3);
        assertThat(promptView.getVerifySubmissionUrl()).isEqualTo(verifySubmissionUri);
        assertThat(promptView.getIdpId()).isEqualTo(idp.getIssuerId());
        assertThat(promptView.getUniqueId()).isNotNull();

        // List should be sorted by category
        assertThat(promptView.getServices().get(0).getServiceCategory()).isEqualTo("Category A");
        assertThat(promptView.getServices().get(1).getServiceCategory()).isEqualTo("Category A");
        assertThat(promptView.getServices().get(2).getServiceCategory()).isEqualTo("Category B");
    }

}
