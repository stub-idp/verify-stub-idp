package uk.gov.ida.matchingserviceadapter.builders;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.matching.assertions.MatchingServiceAuthnStatement;

import static stubidp.saml.domain.matching.assertions.MatchingServiceAuthnStatement.createIdaAuthnStatement;

public class MatchingServiceAuthnStatementBuilder {

    private AuthnContext levelOfAssurance = AuthnContext.LEVEL_1;

    public static MatchingServiceAuthnStatementBuilder anIdaAuthnStatement() {
        return new MatchingServiceAuthnStatementBuilder();
    }

    public MatchingServiceAuthnStatement build() {
        return createIdaAuthnStatement(levelOfAssurance);
    }
}
