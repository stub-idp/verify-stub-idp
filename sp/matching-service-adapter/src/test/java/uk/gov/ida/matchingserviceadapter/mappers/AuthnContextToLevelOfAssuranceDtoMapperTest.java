package uk.gov.ida.matchingserviceadapter.mappers;

import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.AuthnContext;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthnContextToLevelOfAssuranceDtoMapperTest {

    @Test
    public void test_expectedLevels() {
        assertThat(AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_1)).isEqualTo(LevelOfAssuranceDto.LEVEL_1);
        assertThat(AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_2)).isEqualTo(LevelOfAssuranceDto.LEVEL_2);
        assertThat(AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_3)).isEqualTo(LevelOfAssuranceDto.LEVEL_3);
        assertThat(AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_4)).isEqualTo(LevelOfAssuranceDto.LEVEL_4);
    }

    @Test
    public void test_unExpectedLevels() {
        assertThrows(IllegalArgumentException.class,
                () -> AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.valueOf("LEVEL_11")));
    }

    @Test
    public void test_unExpectedLevel_X() {
        assertThrows(IllegalArgumentException.class,
                () -> AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_X));
    }

}