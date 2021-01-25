package stubidp.stubidp.repositories.jdbc;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.Gender;
import stubidp.stubidp.domain.EidasAddress;
import stubidp.stubidp.domain.EidasAuthnRequest;
import stubidp.stubidp.domain.EidasUser;
import stubidp.stubidp.domain.IdpHint;
import stubidp.stubidp.domain.IdpLanguageHint;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.jdbc.migrations.DatabaseMigrationRunner;
import stubidp.utils.rest.common.SessionId;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JDBIEidasSessionRepositoryTest {

	private Jdbi jdbi;
	private JDBIEidasSessionRepository repository;
	private final String DATABASE_URL = "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

	@BeforeEach
	void setUp() {
		new DatabaseMigrationRunner().runMigration(DATABASE_URL);

		jdbi = Jdbi.create(DATABASE_URL);
		repository = new JDBIEidasSessionRepository(jdbi);
	}
	
	@Test
	void createSession_shouldCreateEidasSessionAndStoreInDatabase() {
		EidasAuthnRequest authnRequest = new EidasAuthnRequest("7cb0ba32-4ebd-4291-8901-c647d4687572", "test-issuer", "", "", Collections.emptyList());

		SessionId eidasSessionId = SessionId.createNewSessionId();
		Instant startTime = Instant.now();
		EidasSession session = new EidasSession(eidasSessionId, startTime, authnRequest, "test-relay-state", Collections.singletonList(IdpHint.has_ukphotolicence), Collections.singletonList("invalid hint"), Optional.of(IdpLanguageHint.cy), Optional.of(true), null, true);
		session.setEidasUser(new EidasUser("Joe", Optional.empty(), "Bloggs", Optional.empty(), "persistentId", Instant.ofEpochMilli(1524655440000L), Optional.of(new EidasAddress("PO Box 123", "", "", "", "", "", "", "", "AB1 2YZ")), Optional.of(Gender.MALE)));
		repository.createSession(session);
		
		String expectedSerializedSession = "{{\"sessionId\":\""+ eidasSessionId.getSessionId() +"\",\"startTime\":\""+startTime+"\",\"eidasAuthnRequest\":{\"requestId\":\"7cb0ba32-4ebd-4291-8901-c647d4687572\",\"issuer\":\"test-issuer\",\"destination\":\"\",\"requestedLoa\":\"\",\"attributes\":[]},\"relayState\":\"test-relay-state\",\"validHints\":[\"has_ukphotolicence\"],\"invalidHints\":[\"invalid hint\"],\"languageHint\":\"cy\",\"registration\":true,\"csrfToken\":null,\"eidasUser\":{\"firstName\":\"Joe\",\"familyName\":\"Bloggs\",\"persistentId\":\"persistentId\",\"dateOfBirth\":1524655440.000000000,\"address\":{\"poBox\":\"PO Box 123\",\"locatorDesignator\":\"\",\"locatorName\":\"\",\"cvAddressArea\":\"\",\"thoroughfare\":\"\",\"postName\":\"\",\"adminunitFirstLine\":\"\",\"adminunitSecondLine\":\"\",\"postCode\":\"AB1 2YZ\"},\"gender\":\"MALE\"},\"signAssertions\":true}}";

		jdbi.useHandle(handle -> {
			Optional<String> result = handle.select("select session_data from stub_idp_session where session_id = ?", eidasSessionId.toString())
					.mapTo(String.class)
					.findFirst();

			assertThat(result.isPresent()).isEqualTo(true);
			assertThat(result.get()).isEqualTo(expectedSerializedSession);
		});
	}

	@Test
	void get_shouldReturnPopulatedEidasSession_whenSessionExists() {
		EidasAuthnRequest authnRequest = new EidasAuthnRequest("7cb0ba32-4ebd-4291-8901-c647d4687572", "test-issuer", "", "", Collections.emptyList());

		EidasSession expectedSession = new EidasSession(SessionId.createNewSessionId(), Instant.ofEpochSecond(12345678), authnRequest, "test-relay-state", Collections.singletonList(IdpHint.has_ukphotolicence), Collections.singletonList("invalid hint"), Optional.of(IdpLanguageHint.cy), Optional.of(true), null, true);
		expectedSession.setEidasUser(new EidasUser("Joe", Optional.empty(), "Bloggs", Optional.empty(), "persistentId", Instant.ofEpochMilli(1524655440000L), Optional.of(new EidasAddress("PO Box 123", "", "", "", "", "", "", "", "AB1 2YZ")), Optional.of(Gender.MALE)));
		SessionId insertedSessionId = repository.createSession(expectedSession);

		Optional<EidasSession> actualSession = repository.get(insertedSessionId);

		assertThat(actualSession.isPresent()).isEqualTo(true);
		assertThat(actualSession.get()).isInstanceOf(EidasSession.class);
		assertThat(actualSession.get()).usingRecursiveComparison().isEqualTo(expectedSession);
	}
}
