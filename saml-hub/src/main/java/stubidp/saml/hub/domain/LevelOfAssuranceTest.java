package stubidp.saml.hub.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LevelOfAssuranceTest {

    @Test
    public void checkOrdering() {
        assertThat(LevelOfAssurance.HIGH.compareTo(LevelOfAssurance.SUBSTANTIAL) > 0).isTrue();
        assertThat(LevelOfAssurance.HIGH.compareTo(LevelOfAssurance.LOW) > 0).isTrue();
        assertThat(LevelOfAssurance.SUBSTANTIAL.compareTo(LevelOfAssurance.LOW) > 0).isTrue();

        assertThat(LevelOfAssurance.SUBSTANTIAL.compareTo(LevelOfAssurance.HIGH) < 0).isTrue();
        assertThat(LevelOfAssurance.LOW.compareTo(LevelOfAssurance.HIGH) < 0).isTrue();
        assertThat(LevelOfAssurance.LOW.compareTo(LevelOfAssurance.SUBSTANTIAL) < 0).isTrue();
    }

}