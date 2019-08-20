package stubidp.saml.metadata.factories;

import com.google.common.base.Predicate;
import net.shibboleth.utilities.java.support.resolver.CriterionPredicateRegistry;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.saml.metadata.resolver.impl.AbstractBatchMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import stubidp.saml.metadata.EntitiesDescriptorNameCriterion;
import stubidp.saml.metadata.EntitiesDescriptorNamePredicate;
import stubidp.saml.metadata.ExpiredCertificateMetadataFilter;

import javax.ws.rs.client.Client;
import java.net.URI;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class MetadataResolverFactoryTest {

    @Mock
    ExpiredCertificateMetadataFilter expiredCertificateMetadataFilter;

    @Mock
    SignatureValidationFilter signatureValidationFilter;

    @Test
    public void shouldProvideMetadataResolver() throws Exception {
        Client client = new JerseyClientBuilder().build();
        MetadataResolverFactory metadataResolverFactory = new MetadataResolverFactory();
        MetadataResolver metadataResolver = metadataResolverFactory.create(client, URI.create("http://example.com"), asList(signatureValidationFilter,  expiredCertificateMetadataFilter),20, 60);
        assertThat(metadataResolver).isNotNull();

        AbstractBatchMetadataResolver batchMetadataResolver = (AbstractBatchMetadataResolver) metadataResolver;
        CriterionPredicateRegistry<EntityDescriptor> criterionPredicateRegistry = batchMetadataResolver.getCriterionPredicateRegistry();
        Predicate<EntityDescriptor> predicate = criterionPredicateRegistry.getPredicate(new EntitiesDescriptorNameCriterion("some-name"));
        assertThat(predicate.getClass()).isEqualTo(EntitiesDescriptorNamePredicate.class);
        assertThat(batchMetadataResolver.isResolveViaPredicatesOnly()).isTrue();
    }
}
