package stubidp.saml.metadata;

import com.nimbusds.jose.jwk.JWK;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DisabledMetadataResolverRepository implements MetadataResolverRepository{
    @Override
    public Optional<MetadataResolver> getMetadataResolver(String entityId) {
        return Optional.empty();
    }

    @Override
    public List<String> getResolverEntityIds() {
        return List.of();
    }

    @Override
    public Optional<ExplicitKeySignatureTrustEngine> getSignatureTrustEngine(String entityId) {
        return Optional.empty();
    }

    @Override
    public Map<String, MetadataResolver> getMetadataResolvers() {
        return Map.of();
    }

    @Override
    public List<String> getTrustAnchorsEntityIds() {
        return List.of();
    }

    @Override
    public void refresh() { }

    @Override
    public List<X509Certificate> sortCertsByDate(JWK trustAnchor) {
        return List.of();
    }
}
