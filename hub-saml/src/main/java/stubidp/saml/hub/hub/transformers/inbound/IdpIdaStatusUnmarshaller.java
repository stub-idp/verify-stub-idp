package stubidp.saml.hub.hub.transformers.inbound;

import stubidp.saml.domain.assertions.IdpIdaStatus;

public class IdpIdaStatusUnmarshaller extends AuthenticationStatusUnmarshallerBase<IdpIdaStatus.Status, IdpIdaStatus> {
    public IdpIdaStatusUnmarshaller() {
        super(new SamlStatusToIdaStatusCodeMapper(), new IdpIdaStatus.IdpIdaStatusFactory());
    }
}
