package stubidp.stubidp.repositories.jdbc;

import io.prometheus.client.CollectorRegistry;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.core.domain.Gender;
import stubidp.saml.utils.hub.domain.IdaAuthnRequestFromHub;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.MatchingDatasetValue;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.jdbc.migrations.DatabaseMigrationRunner;
import stubidp.utils.rest.common.SessionId;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JDBIIdpSessionRepositoryTest {

	private Jdbi jdbi;
	private JDBIIdpSessionRepository repository;
	private final String DATABASE_URL = "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
	
	@BeforeEach
	public void setUp() {
		new DatabaseMigrationRunner().runMigration(DATABASE_URL);

		jdbi = Jdbi.create(DATABASE_URL);
		repository = new JDBIIdpSessionRepository(jdbi);
	}

	@AfterEach
	public void afterEach() {
		CollectorRegistry.defaultRegistry.clear();
	}

	@Test
	public void testSessionDeletion() throws InterruptedException {
		// force clear all sessions from the test database
		repository.deleteSessionsOlderThan(Duration.ZERO);
		// this is not ideal - and could be flaky.
		// DateTimeFreezer could be updated and used but that has proved difficult...
		final long sleepSeconds = 5;

		assertThat(repository.countSessionsInDatabase()).isZero().withFailMessage("found %d sessions in the database when it should be empty", repository.countSessionsInDatabase());
		repository.createSession(createSession());
		assertThat(repository.countSessionsInDatabase()).isEqualTo(1);
		Thread.sleep(sleepSeconds*1000);
		repository.createSession(createSession());
		assertThat(repository.countSessionsInDatabase()).isEqualTo(2);
		assertThat(repository.countSessionsOlderThan(Duration.ofHours(1))).isEqualTo(0);
		assertThat(repository.countSessionsOlderThan(Duration.ofSeconds(sleepSeconds/2))).isEqualTo(1);
		repository.deleteSessionsOlderThan(Duration.ofSeconds(sleepSeconds/2));
		assertThat(repository.countSessionsInDatabase()).isEqualTo(1);
	}

	private IdpSession createSession() {
		Instant authnRequestIssueTime = getLocalDateTime(2018, 4, 25, 11, 24, 0);
		IdaAuthnRequestFromHub authnRequest = new IdaAuthnRequestFromHub("155a37d3-5a9d-4cd0-b68a-158717b85202", "test-issuer", authnRequestIssueTime, Collections.emptyList(), Optional.empty(), null, null, AuthnContextComparisonTypeEnumeration.EXACT);
		return createSession(authnRequest);
	}

	@Test
	public void createSession_shouldCreateIdpSessionAndStoreInDatabase() {
		Instant authnRequestIssueTime = getLocalDateTime(2018, 4, 25, 11, 24, 0);
		IdaAuthnRequestFromHub authnRequest = new IdaAuthnRequestFromHub("155a37d3-5a9d-4cd0-b68a-158717b85202", "test-issuer", authnRequestIssueTime, Collections.emptyList(), Optional.empty(), null, null, AuthnContextComparisonTypeEnumeration.EXACT);
		IdpSession session = createSession(authnRequest);
		SessionId insertedSessionId = repository.createSession(session);
		String expectedSerializedSessionStart = "{{\"sessionId\":\""+ insertedSessionId.getSessionId() +"\",\"idaAuthnRequestFromHub\":{\"id\":\"155a37d3-5a9d-4cd0-b68a-158717b85202\",\"issuer\":\"test-issuer\",\"issueInstant\":1524655440.000000000,\"levelsOfAssurance\":[],\"forceAuthentication\":null,\"sessionExpiryTimestamp\":null,\"comparisonType\":\"EXACT\",\"destination\":null},\"relayState\":\"test-relay-state\",\"validHints\":[],\"invalidHints\":[],\"languageHint\":null,\"registration\":null,\"singleIdpJourneyId\":null,\"csrfToken\":null,\"idpUser\":{\"username\":\"jobloggs\",\"persistentId\":\"persistentId\",\"password\":";
		String expectedSerializedSessionEnd = ",\"firstnames\":[{\"value\":\"Joe\",\"from\":null,\"to\":null,\"verified\":true}],\"middleNames\":[],\"surnames\":[{\"value\":\"Bloggs\",\"from\":null,\"to\":null,\"verified\":true}],\"gender\":{\"value\":\"MALE\",\"from\":null,\"to\":null,\"verified\":true},\"dateOfBirths\":[{\"value\":1524655440.000000000,\"from\":null,\"to\":null,\"verified\":true}],\"addresses\":[],\"levelOfAssurance\":\"LEVEL_1\",\"currentAddress\":null}}}";

		jdbi.useHandle(handle -> {
			Optional<String> result = handle.select("select session_data from stub_idp_session where session_id = ?", insertedSessionId.toString())
				.mapTo(String.class)
				.findFirst();
				
			assertThat(result.isPresent()).isEqualTo(true);
			// skip the password
			assertThat(result.get()).startsWith(expectedSerializedSessionStart);
			assertThat(result.get()).endsWith(expectedSerializedSessionEnd);
			assertThat(result.get()).doesNotContain("12345678"); //password should be hashed
		});
	}
	
	@Test
	public void get_shouldReturnEmptyOptional_whenSessionDoesNotExist() {
		SessionId nonExistentSessionId = SessionId.createNewSessionId();
		
		Optional<IdpSession> result = repository.get(nonExistentSessionId);
		
		assertThat(result.isPresent()).isEqualTo(false);
	}
	
	@Test
	public void get_shouldReturnPopulatedIdpSession_whenSessionExists() {
		Instant authnRequestIssueTime = getLocalDateTime(2018, 4, 25, 11, 24, 0);
		IdaAuthnRequestFromHub authnRequest = new IdaAuthnRequestFromHub("155a37d3-5a9d-4cd0-b68a-158717b85202", "test-issuer", authnRequestIssueTime, Arrays.asList(), Optional.empty(), null, null, AuthnContextComparisonTypeEnumeration.EXACT);
		IdpSession expectedSession = createSession(authnRequest);
		SessionId insertedSessionId = repository.createSession(expectedSession);
		
		Optional<IdpSession> actualSession = repository.get(insertedSessionId);
		
		assertThat(actualSession.isPresent()).isEqualTo(true);
		assertThat(actualSession.get()).isInstanceOf(IdpSession.class);
		assertThat(actualSession.get()).usingRecursiveComparison().isEqualTo(expectedSession);
	}
	
	@Test
	public void updateSession_shouldNotThrowException_whenSessionDoesNotExist() {
		Instant authnRequestIssueTime = getLocalDateTime(2018, 4, 25, 11, 24, 0);
		IdaAuthnRequestFromHub authnRequest = new IdaAuthnRequestFromHub("155a37d3-5a9d-4cd0-b68a-158717b85202", "test-issuer", authnRequestIssueTime, Arrays.asList(), Optional.empty(), null, null, AuthnContextComparisonTypeEnumeration.EXACT);
		IdpSession session = createSession(authnRequest);
		SessionId insertedSessionId = repository.createSession(session);
		session = repository.get(insertedSessionId).get();
		session.getIdaAuthnRequestFromHub().getLevelsOfAssurance().add(AuthnContext.LEVEL_4);
		
		repository.updateSession(SessionId.createNewSessionId(), session);
	}
	
	@Test
	public void updateSession_shouldUpdateStoredSessionInDatabase_whenSessionExists() {
		Instant authnRequestIssueTime = getLocalDateTime(2018, 4, 25, 11, 24, 0);
		IdaAuthnRequestFromHub authnRequest = new IdaAuthnRequestFromHub("155a37d3-5a9d-4cd0-b68a-158717b85202", "test-issuer", authnRequestIssueTime, Collections.emptyList(), Optional.empty(), null, null, AuthnContextComparisonTypeEnumeration.EXACT);
		IdpSession session = createSession(authnRequest);
		SessionId insertedSessionId = repository.createSession(session);
		IdpSession expectedSession = repository.get(insertedSessionId).get();
		expectedSession.getIdaAuthnRequestFromHub().getLevelsOfAssurance().add(AuthnContext.LEVEL_4);
		repository.updateSession(insertedSessionId, expectedSession);
		IdpSession actualSession = repository.get(insertedSessionId).get();
		
		assertThat(actualSession).usingRecursiveComparison().isEqualTo(expectedSession);
	}

	@Test
	public void deleteSession_shouldDeleteSessionFromDatabase_whenSessionExists() {
		Instant authnRequestIssueTime = getLocalDateTime(2018, 4, 25, 11, 24, 0);
		IdaAuthnRequestFromHub authnRequest = new IdaAuthnRequestFromHub("155a37d3-5a9d-4cd0-b68a-158717b85202", "test-issuer", authnRequestIssueTime, Collections.emptyList(), Optional.empty(), null, null, AuthnContextComparisonTypeEnumeration.EXACT);
		IdpSession session = createSession(authnRequest);
		SessionId insertedSessionId = repository.createSession(session);
		
		repository.deleteSession(insertedSessionId);
		
		assertThat(repository.containsSession(insertedSessionId)).isEqualTo(false);
	}
	
	private IdpSession createSession(IdaAuthnRequestFromHub authnRequestFromHub) {
		IdpSession session = new IdpSession(SessionId.createNewSessionId(), authnRequestFromHub, "test-relay-state", Collections.emptyList(), Collections.emptyList(), Optional.empty(), Optional.empty(), Optional.empty(), null);
		// TODO: add addresses to IdpUser below once Address has a equals() method implemented.
		session.setIdpUser(Optional.of(new DatabaseIdpUser("jobloggs", "persistentId", "12345678",
				Collections.singletonList(new MatchingDatasetValue<>("Joe", null, null, true)),
				Collections.emptyList(),
				Collections.singletonList(new MatchingDatasetValue<>("Bloggs", null, null, true)),
				Optional.of(new MatchingDatasetValue<>(Gender.MALE, null, null, true)),
				Collections.singletonList(new MatchingDatasetValue<>(authnRequestFromHub.getIssueInstant(), null, null, true)),
				Collections.emptyList(),
				AuthnContext.LEVEL_1)));
		return session;
	}

	private Instant getLocalDateTime(int year, int month, int day, int hour, int minute, int seconds) {
		return LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute, seconds)).atZone(ZoneId.of("UTC")).toInstant();
	}
}
