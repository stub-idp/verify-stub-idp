package stubidp.stubidp.repositories.jdbc;

import io.dropwizard.jackson.Jackson;
import io.prometheus.client.CollectorRegistry;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.builders.IdpUserBuilder;
import stubidp.stubidp.domain.DatabaseEidasUser;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.repositories.jdbc.migrations.DatabaseMigrationRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JDBIUserRepositoryTest {

    private Jdbi jdbi;
    private JDBIUserRepository repository;
    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        String url = "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        jdbi = Jdbi.create(url);
        new DatabaseMigrationRunner().runMigration(url);
        userMapper = new UserMapper(Jackson.newObjectMapper());
        repository = new JDBIUserRepository(jdbi, userMapper);
    }

    @AfterEach
    public void afterEach() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void addOrUpdateUserForIdpShouldAddRecordIfUserDoesNotExist() {
        ensureNoUserExistsFor("some-idp-friendly-id");

        DatabaseIdpUser idpUser = IdpUserBuilder.anIdpUser()
            .withUsername("some-username")
            .build();

        repository.addOrUpdateUserForIdp("some-idp-friendly-id", idpUser);

        List<DatabaseIdpUser> idpUsers = new ArrayList<>(repository.getUsersForIdp("some-idp-friendly-id"));

        assertThat(idpUsers).size().isEqualTo(1);
        assertThat(idpUsers.get(0)).isEqualTo(idpUser);
    }

    @Test
    public void addOrUpdateUserForIdpShouldUpdateRecordIfUserAlreadyExists() {
        DatabaseIdpUser someUser = IdpUserBuilder.anIdpUser()
            .withUsername("some-username")
            .withPassword("some-password")
            .build();

        ensureNoUserExistsFor("some-idp-friendly-id");
        ensureUserExistsFor("some-idp-friendly-id", someUser);

        DatabaseIdpUser sameUserDifferentPassword = IdpUserBuilder.anIdpUser()
            .withUsername("some-username")
            .withPassword("another-password")
            .build();

        repository.addOrUpdateUserForIdp("some-idp-friendly-id", sameUserDifferentPassword);

        List<DatabaseIdpUser> idpUsers = new ArrayList<>(repository.getUsersForIdp("some-idp-friendly-id"));

        assertThat(idpUsers).size().isEqualTo(1);
        assertThat(idpUsers.get(0)).isEqualTo(sameUserDifferentPassword);
    }

    @Test
    public void deleteUserFromIdpShouldDeleteGivenUserFromGivenIdp() {
        DatabaseIdpUser someUser = IdpUserBuilder.anIdpUser()
            .withUsername("some-username")
            .build();

        ensureUserExistsFor("some-idp-friendly-id", someUser);

        repository.deleteUserFromIdp("some-idp-friendly-id", "some-username");

        List<DatabaseIdpUser> idpUsers = new ArrayList<>(repository.getUsersForIdp("some-idp-friendly-id"));

        assertThat(idpUsers).size().isEqualTo(0);
    }

    @Test
    public void getUsersForIdpShouldReturnAllUsersForGivenIdp() {
        ensureNoUserExistsFor("some-idp-friendly-id");

        DatabaseIdpUser firstUser = IdpUserBuilder.anIdpUser().withUsername("first-username").build();
        DatabaseIdpUser secondUser = IdpUserBuilder.anIdpUser().withUsername("second-username").build();

        ensureUserExistsFor("some-idp-friendly-id", firstUser);
        ensureUserExistsFor("some-idp-friendly-id", secondUser);

        List<DatabaseIdpUser> idpUsers = new ArrayList<>(repository.getUsersForIdp("some-idp-friendly-id"));

        assertThat(idpUsers).size().isEqualTo(2);
    }

    @Test
    public void addOrUpdateUserForStubCountryShouldAddRecordIfUserDoesNotExist(){
        ensureNoUserExistsFor("stub-country-friendly-id");

        DatabaseEidasUser eidasUser = new DatabaseEidasUser("some-username", null, "some-password", createMdsValue("firstName"), Optional.of(createMdsValue("firstNameNonLatin")), createMdsValue("surname"), Optional.of(createMdsValue("surnameNonLatin")), createMdsValue(Instant.now()), AuthnContext.LEVEL_2);

        repository.addOrUpdateEidasUserForStubCountry("stub-country-friendly-id", eidasUser);

        Collection<DatabaseEidasUser> users = repository.getUsersForCountry("stub-country-friendly-id");

        assertThat(users).size().isEqualTo(1);
        assertThat(users).contains(eidasUser);

    }

    private void ensureNoUserExistsFor(String idpFriendlyId) {
        jdbi.withHandle(handle ->
            handle.createUpdate("DELETE FROM users WHERE identity_provider_friendly_id = :idpFriendlyId")
                .bind("idpFriendlyId", idpFriendlyId)
                .execute()
        );
    }

    private void ensureUserExistsFor(String idpFriendlyId, DatabaseIdpUser idpUser) {
        repository.deleteUserFromIdp(idpFriendlyId, idpUser.getUsername());

        User user = userMapper.mapFrom(idpFriendlyId, idpUser);

        jdbi.withHandle(handle -> {
            final String sqlStatement = "INSERT INTO users(username, password, identity_provider_friendly_id, \"data\") " +
                    "VALUES (:username, :password, :idpFriendlyId, to_json(:json))";

            return handle.createUpdate(sqlStatement)
                    .bind("username", user.getUsername())
                    .bind("password", user.getPassword())
                    .bind("idpFriendlyId", idpFriendlyId)
                    .bind("json", user.getData())
                    .execute();
            }
        );
    }

    private <T> SimpleMdsValue<T> createMdsValue(T value) {
        return new SimpleMdsValue<>(value, null, null, true);
    }
}
