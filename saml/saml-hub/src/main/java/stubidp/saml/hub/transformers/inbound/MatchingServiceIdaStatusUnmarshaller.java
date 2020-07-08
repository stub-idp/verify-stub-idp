package stubidp.saml.hub.transformers.inbound;

import java.util.Map;

public class MatchingServiceIdaStatusUnmarshaller extends IdaStatusUnmarshaller<MatchingServiceIdaStatus> {

    private static final Map<IdaStatusMapperStatus, MatchingServiceIdaStatus> SAML_TO_REST_CODES =
            Map.<IdaStatusMapperStatus, MatchingServiceIdaStatus>ofEntries(
                    Map.entry(IdaStatusMapperStatus.RequesterError, MatchingServiceIdaStatus.RequesterError),
                    Map.entry(IdaStatusMapperStatus.NoMatchingServiceMatchFromMatchingService, MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService),
                    Map.entry(IdaStatusMapperStatus.MatchingServiceMatch, MatchingServiceIdaStatus.MatchingServiceMatch),
                    Map.entry(IdaStatusMapperStatus.Healthy, MatchingServiceIdaStatus.Healthy),
                    Map.entry(IdaStatusMapperStatus.Created, MatchingServiceIdaStatus.UserAccountCreated),
                    Map.entry(IdaStatusMapperStatus.CreateFailed, MatchingServiceIdaStatus.UserAccountCreationFailed)
            );

    public MatchingServiceIdaStatusUnmarshaller() {
        super(SAML_TO_REST_CODES);
    }
}
