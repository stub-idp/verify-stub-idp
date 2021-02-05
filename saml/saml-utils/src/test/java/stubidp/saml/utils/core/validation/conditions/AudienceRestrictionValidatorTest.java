package stubidp.saml.utils.core.validation.conditions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.AudienceRestrictionBuilder.anAudienceRestriction;

@ExtendWith(MockitoExtension.class)
class AudienceRestrictionValidatorTest {

    private AudienceRestrictionValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new AudienceRestrictionValidator();
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldNotComplainWhenCorrectDataIsPassed() {
        Audience audience = new AudienceBuilder().buildObject();
        audience.setURI("some-entity-id");

        AudienceRestriction audienceRestriction = mock(AudienceRestriction.class, Answers.RETURNS_DEEP_STUBS);
        when(audienceRestriction.getAudiences()).thenReturn(List.of(audience));

        validator.validate(List.of(audienceRestriction), "some-entity-id");
    }

    @Test
    public void shouldThrowExceptionWhenAudienceRestrictionsIsNull() {
        List<AudienceRestriction> audienceRestrictions = null;

        assertThatThrownBy(() -> {
            validator.validate(audienceRestrictions, "any-entity-id");
        })
                .isInstanceOf(SamlResponseValidationException.class)
                .hasMessageContaining("Exactly one audience restriction is expected.");
    }

    @Test
    public void shouldThrowExceptionWhenAudienceRestrictionsHasMoreThanOneElements() {
        List<AudienceRestriction> audienceRestrictions = List.of(
                anAudienceRestriction().build(),
                anAudienceRestriction().build()
        );

        assertThatThrownBy(() -> {
            validator.validate(audienceRestrictions, "any-entity-id");
        })
                .isInstanceOf(SamlResponseValidationException.class)
                .hasMessageContaining("Exactly one audience restriction is expected.");
    }

    @Test
    public void shouldThrowExceptionWhenAudiencesIsNull() {
        AudienceRestriction audienceRestriction = mock(AudienceRestriction.class, Answers.RETURNS_DEEP_STUBS);
        when(audienceRestriction.getAudiences()).thenReturn(null);

        assertThatThrownBy(() -> {
            validator.validate(List.of(audienceRestriction), "any-entity-id");
        })
                .isInstanceOf(SamlResponseValidationException.class)
                .hasMessageContaining("Exactly one audience is expected.");
    }

    @Test
    public void shouldThrowExceptionWhenAudiencesIsMoreThanOne() {
        AudienceRestriction audienceRestriction = anAudienceRestriction().build();
        audienceRestriction.getAudiences().add(new AudienceBuilder().buildObject());
        audienceRestriction.getAudiences().add(new AudienceBuilder().buildObject());

        assertThatThrownBy(() -> {
            validator.validate(List.of(audienceRestriction), "any-entity-id");
        })
                .isInstanceOf(SamlResponseValidationException.class)
                .hasMessageContaining("Exactly one audience is expected.");
    }

    @Test
    public void shouldThrowExceptionWhenAudienceUriDoesNotMatchTheEntityId() {
        Audience audience = new AudienceBuilder().buildObject();
        audience.setURI("some-entity-id");

        AudienceRestriction audienceRestriction = mock(AudienceRestriction.class, Answers.RETURNS_DEEP_STUBS);
        when(audienceRestriction.getAudiences()).thenReturn(List.of(audience));

        assertThatThrownBy(() -> {
            validator.validate(List.of(audienceRestriction), "unknown-entity-id");
        })
                .isInstanceOf(SamlResponseValidationException.class)
                .hasMessageContaining(String.format(
                        "Audience must match an acceptable entity ID. Acceptable entity IDs are: %s but audience was: %s",
                        "unknown-entity-id",
                        "some-entity-id"));
    }
}