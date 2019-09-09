package stubidp.saml.utils.hub.transformers.outbound;

import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.DetailedStatusCode;
import stubidp.saml.utils.core.domain.UnknownUserCreationIdaStatus;
import stubidp.saml.utils.core.transformers.outbound.IdaStatusMarshaller;

import java.util.AbstractMap;
import java.util.Map;

public class UnknownUserCreationIdaStatusMarshaller extends IdaStatusMarshaller<UnknownUserCreationIdaStatus> {

    private static final Map<UnknownUserCreationIdaStatus, DetailedStatusCode> REST_TO_SAML_CODES =
            Map.<UnknownUserCreationIdaStatus, DetailedStatusCode>ofEntries(
                    Map.entry(UnknownUserCreationIdaStatus.CreateFailure, DetailedStatusCode.UnknownUserCreateFailure),
                    Map.entry(UnknownUserCreationIdaStatus.Success, DetailedStatusCode.UnknownUserCreateSuccess),
                    Map.entry(UnknownUserCreationIdaStatus.NoAttributeFailure, DetailedStatusCode.UnknownUserNoAttributeFailure)
            );

    public UnknownUserCreationIdaStatusMarshaller(OpenSamlXmlObjectFactory samlObjectFactory) {
        super(samlObjectFactory);
    }

    @Override
    protected DetailedStatusCode getDetailedStatusCode(UnknownUserCreationIdaStatus originalStatus) {
        return REST_TO_SAML_CODES.get(originalStatus);
    }
}
