package uk.gov.ida.rp.testrp.tokenservice;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.configuration.DeserializablePublicKeyConfiguration;
import stubidp.utils.security.configuration.PrivateKeyConfiguration;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.exceptions.CouldNotParseTokenException;
import uk.gov.ida.rp.testrp.exceptions.CouldNotParseTokenPayloadException;
import uk.gov.ida.rp.testrp.exceptions.InvalidAccessTokenException;
import uk.gov.ida.rp.testrp.exceptions.TokenGenerationException;
import uk.gov.ida.rp.testrp.exceptions.TokenHasInvalidSignatureException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestEntityIds.STUB_IDP_ONE;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    private TokenService tokenService;
    private BadTokenService badTokenService;

    @Mock
    private TestRpConfiguration configuration;
    @Mock
    private DeserializablePublicKeyConfiguration deserializablePublicKeyConfiguration;
    @Mock
    private PrivateKeyConfiguration privateKeyConfiguration;

    private static PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TEST_RP_PRIVATE_SIGNING_KEY));
    private static PublicKey publicKey = new X509CertificateFactory().createCertificate(TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT).getPublicKey();

    @BeforeEach
    public void setUp() {
        tokenService = new TokenService(configuration);
        badTokenService = new BadTokenService(configuration);
    }

    @Test
    public void shouldValidateWhenSignedProperlyAndInDateAndEpoch() {
        when(deserializablePublicKeyConfiguration.getPublicKey()).thenReturn(publicKey);
        when(configuration.getPublicSigningCert()).thenReturn(deserializablePublicKeyConfiguration);
        when(configuration.getTokenEpoch()).thenReturn(1);
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(true);

        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6NDEwMjQ0NDgwMDAwMCwiaXNzdWVkVG8iOiJodHRwOi8vc3R1Yl9pZHAuYWNtZS5vcmcvc3R1Yi1pZHAtb25lL1NTTy9QT1NUIn0.Ad7qOHJ-ZJe3feUB9-Rq9nNsGal5wSqgmHWVNZpnASKdMfUZYHCBoz_TAZHZL9WeG9HkC_8INRw3o10Q8wBZyC662X0-Fif149p6eP5vld54nfIKJ1fRFxo7yTDaIw3sKyaXDIQ6IGGXyEsgg7kMwNyID1eTp9X5jV0EOIoEilyo-Lr5qhBAR7ZBEj5L7tM15b34UalUjaqz3_5i8ErSvumEAiU1DQWA66-uMP5bGn3dpwiSaP_8egU2IXXOkU78fGgUmckqkWFbRbgv4KA0nioYX7BS1GFh0QSzLiXjLXO33YYEapx8tNh3UetgZ1jmUBAqQFUireg3t8Onx_DKfQ");

        tokenService.validate(Optional.of(accessToken));
    }

    @Test
    public void shouldValidateWhenTokenIsEmptyAndPrivateBetaDisabled() {
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(false);
        tokenService.validate(Optional.empty());
    }

    @Test
    public void shouldNotValidateWhenTokenIsEmptyAndPrivateBetaEnabled() {
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(true);

        assertThrows(InvalidAccessTokenException.class, () -> tokenService.validate(Optional.empty()));
    }

    @Test
    public void shouldNotValidateWhenEpochIsTooOld() {
        when(deserializablePublicKeyConfiguration.getPublicKey()).thenReturn(publicKey);
        when(configuration.getPublicSigningCert()).thenReturn(deserializablePublicKeyConfiguration);
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(true);

        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MCwidmFsaWRVbnRpbCI6IjIxMDAtMDEtMDFUMDA6MDA6MDAuMDAwWiIsImlzc3VlZFRvIjoiaHR0cDovL3N0dWJfaWRwLmFjbWUub3JnL3N0dWItaWRwLW9uZS9TU08vUE9TVCJ9.sWUh005abGbPHJ9xJwCytGeuNZCKtRyHVLL3_MMSXSo8gZKnRRO19TqcfFwsE9pbhbcFqXzrxyc5hkI93JmeCfIafVhV8vBvSELAgs9prZlujSv_BD1ggOGj1d5uUpD_5I8K5ppGG1IrGsVh-FwdNtFmQr0fIGK6AbWLl-hf6ffFia17E-sXwRBJy139pZlMVqVtW9B-s1qfkdsTo5X7MuL9IuiOHhy24Y4YsgzFL7EoqZK3sBstMwX7AhlycNlLnqKMvmnlYOIBEMuHep3hCve6EkD47z9jO2UtiYhv3XuiI9C7izWPc6dTlLM-y8C6K2_WJ9yrLSXaIUC-vL2opg");

        assertThrows(InvalidAccessTokenException.class, () -> tokenService.validate(Optional.of(accessToken)));
    }

    @Test
    public void shouldNotValidateWhenTokenIsExpired() {
        when(deserializablePublicKeyConfiguration.getPublicKey()).thenReturn(publicKey);
        when(configuration.getPublicSigningCert()).thenReturn(deserializablePublicKeyConfiguration);
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(true);

        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6IjIwMDAtMDEtMDFUMDA6MDA6MDAuMDAwWiIsImlzc3VlZFRvIjoiaHR0cDovL3N0dWJfaWRwLmFjbWUub3JnL3N0dWItaWRwLW9uZS9TU08vUE9TVCJ9.lQ2dwUcNCTtLwsXnkjMpsXPonWmDpAJxXgGgbuDvSbD8uiSASptrUpfIUsKO33PcK2i2yPnE5Uxg-Js0-L-d5fUeH901dBGA3Cr5aRc8stIndMyOSvGcnaiVrRSDtiB5WUvEMYhEW30AS6n18ygLvUzykfKPhrje735YNNPCeUD0enXG1ljiq23TdTukPNNvWuzz2eOJGTZeHEVLOpHQOwfqkin3OkzeGzwDpuozDSBs4jS2rQt86BD1RPnYFFOcsvkFXzwrTStR7ghONfoisJbEobxITeCAc3mf5M9ZBNs4iTg5dvTd-0dQbUawLo7p0XD_thB2g6feOJMXdXHwUg");

        assertThrows(InvalidAccessTokenException.class, () -> tokenService.validate(Optional.of(accessToken)));
    }

    @Test
    public void shouldNotValidateWhenSignedByTheWrongKey() {
        when(deserializablePublicKeyConfiguration.getPublicKey()).thenReturn(publicKey);
        when(configuration.getPublicSigningCert()).thenReturn(deserializablePublicKeyConfiguration);
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(true);

        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6IjIxMDAtMDEtMDFUMDA6MDA6MDAuMDAwWiIsImlzc3VlZFRvIjoiaHR0cDovL3N0dWJfaWRwLmFjbWUub3JnL3N0dWItaWRwLW9uZS9TU08vUE9TVCJ9.AMtcyxhzpyzAJ_tMxPZx9mDnwCP8_vXCjgRUEXhIu-VGWWCoJgxxPR4C9vWeDaKFfE25nYQ_ifwQG8u6W3eJ7tT5-DRpBTwcjQgBUnQ6k0zy2fTY9MQKxaZL6NiZHnRLXJRAagXR9yrntTrenxhEo3I9HZFt_gnG3CP-YeGx-hUC6aXlXWQ5D0dhlhHVAUSHPDpt5cBX8bKET1DVqvlvQeq7FmC9zr03M-_f_AhVw5MSm03wyB7Y94vhOibgu9eP-zzke5agrMQZJecZMUMqLtqRx1TCmIUeohD6vM3hArXw3gtkiKxZuPMgzBXsPy7WMEms0N32Pdq8YjD66hqfyw");

        assertThrows(TokenHasInvalidSignatureException.class, () -> tokenService.validate(Optional.of(accessToken)));
    }

    @Test
    public void shouldNotValidateWhenTokenIsJunk() {
        when(deserializablePublicKeyConfiguration.getPublicKey()).thenReturn(publicKey);
        when(configuration.getPublicSigningCert()).thenReturn(deserializablePublicKeyConfiguration);
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(true);

        AccessToken accessToken = new AccessToken("foo");

        assertThrows(CouldNotParseTokenException.class, () -> tokenService.validate(Optional.of(accessToken)));
    }

    @Test
    public void shouldNotValidateWhenTokenPayloadDoesNotHaveRequiredEntries() {
        when(deserializablePublicKeyConfiguration.getPublicKey()).thenReturn(publicKey);
        when(privateKeyConfiguration.getPrivateKey()).thenReturn(privateKey);
        when(configuration.getPrivateSigningKeyConfiguration()).thenReturn(privateKeyConfiguration);
        when(configuration.getPublicSigningCert()).thenReturn(deserializablePublicKeyConfiguration);
        when(configuration.getTokenEpoch()).thenReturn(1);
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(true);

        AccessToken accessToken = badTokenService.generate(STUB_IDP_ONE);

        assertThrows(CouldNotParseTokenPayloadException.class, () -> tokenService.validate(Optional.of(accessToken)));
    }

    @Test
    public void shouldGenerateAValidToken() {
        when(privateKeyConfiguration.getPrivateKey()).thenReturn(privateKey);
        when(configuration.getPrivateSigningKeyConfiguration()).thenReturn(privateKeyConfiguration);
        when(configuration.getTokenEpoch()).thenReturn(1);

        AccessToken expectedAccessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6NDEwMjQ0NDgwMC4wMDAwMDAwMDAsImlzc3VlZFRvIjoiaHR0cDovL3N0dWJfaWRwLmFjbWUub3JnL3N0dWItaWRwLW9uZS9TU08vUE9TVCJ9.LaTzYCgPdiy1i-qCiMYyuh1jbstuBAkgaEV2R0-w-QcXtHIAsgfaOgO00jnUFhH5kaUp5DvGI-6Lg1yatVc7Q6vo6Oz6x-9pS5OhQnn2XcBvDogRFzBRvoio-ogljTG--VRXEEBcoiPHrF38E4gDq_MkdYSwRGYJaP-PyJFvBVbYu-WmJE45q2ln9AuNJxW_36x9y0myvjyN2oL2SjGTrSGgnXMxrIzA-bD8xwZzTS7PhyjqJmJx88MG-mRXuuxqXQam_zTTGoJr05X8mGEOBlZQqD8tORBpOIlNgL-dSYiycmtgFzsNIl55D1CLSRlJaAyzWkMGujim-g4WPCAwVg");
        GenerateTokenRequestDto tokenDto = new GenerateTokenRequestDto(LocalDate.parse("2100-01-01").atStartOfDay().atZone(ZoneId.of("UTC")).toInstant(), STUB_IDP_ONE);

        AccessToken accessToken = tokenService.generate(tokenDto);
        assertThat(accessToken).isEqualTo(expectedAccessToken);
    }

    @Test
    public void shouldNotGenerateATokenThatExpiresInThePast() {
        GenerateTokenRequestDto tokenDto = new GenerateTokenRequestDto(Instant.now().minus(1, ChronoUnit.DAYS), STUB_IDP_ONE);

        assertThrows(TokenGenerationException.class, () -> tokenService.generate(tokenDto));
    }

    @Test
    public void shouldGenerateTokensThatAreValidatedCorrectly() {
        when(deserializablePublicKeyConfiguration.getPublicKey()).thenReturn(publicKey);
        when(privateKeyConfiguration.getPrivateKey()).thenReturn(privateKey);
        when(configuration.getPrivateSigningKeyConfiguration()).thenReturn(privateKeyConfiguration);
        when(configuration.getPublicSigningCert()).thenReturn(deserializablePublicKeyConfiguration);
        when(configuration.getTokenEpoch()).thenReturn(1);
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(true);

        GenerateTokenRequestDto tokenDto = new GenerateTokenRequestDto(Instant.now().plus(1, ChronoUnit.DAYS), STUB_IDP_ONE);

        AccessToken accessToken = tokenService.generate(tokenDto);
        tokenService.validate(Optional.of(accessToken));
    }
}
