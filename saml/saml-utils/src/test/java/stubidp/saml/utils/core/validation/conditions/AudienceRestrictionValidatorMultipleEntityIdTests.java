package stubidp.saml.utils.core.validation.conditions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.test.builders.AudienceRestrictionBuilder;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AudienceRestrictionValidatorMultipleEntityIdTests {

    private AudienceRestrictionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AudienceRestrictionValidator();
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    void audienceRestrictionValidatorShouldAcceptOneAudienceRestriction() {
        String[] acceptableAudiences = new String[]{"audience1", "audience2"};
        List<AudienceRestriction> restrictions = new LinkedList<>();
        restrictions.add(AudienceRestrictionBuilder.anAudienceRestriction().withAudienceId("audience1").build());

        validator.validate(restrictions, acceptableAudiences);
    }

    @Test
    void audienceRestrictionValidatorShouldRejectMoreThanOneAudienceRestriction() {
        String[] acceptableAudiences = new String[]{ "audience1", "audience2" };
        List<AudienceRestriction> restrictions = new LinkedList<>();
        restrictions.add(AudienceRestrictionBuilder.anAudienceRestriction().withAudienceId("audience1").build());
        restrictions.add(AudienceRestrictionBuilder.anAudienceRestriction().withAudienceId("audience2").build());

        assertThatThrownBy(() -> {
                validator.validate(restrictions, acceptableAudiences);
            })
            .isInstanceOf(SamlResponseValidationException.class)
            .hasMessageContaining("Exactly one audience restriction is expected.");
    }

    @Test
    void audienceRestrictionValidatorShouldRejectUnacceptableEntityIds() {
        String[] unacceptableAudiences = new String[]{ "audience2" };
        List<AudienceRestriction> restrictions = new LinkedList<>();
        restrictions.add(AudienceRestrictionBuilder.anAudienceRestriction().withAudienceId("audience1").build());

        assertThatThrownBy(() -> {
                validator.validate(restrictions, unacceptableAudiences);
            })
            .isInstanceOf(SamlResponseValidationException.class)
            .hasMessageContaining("Audience must match an acceptable entity ID.");
    }

    @Test
    void audienceRestrictionValidatorShouldMatchOnAcceptableEntityIds() {
        List<AudienceRestriction> restrictions = new LinkedList<>();
        restrictions.add(AudienceRestrictionBuilder.anAudienceRestriction().withAudienceId("audience1").build());
        String[] acceptableAudiences = new String[] { "audience1", "audience2" };
        validator.validate(restrictions, acceptableAudiences);
    }

}