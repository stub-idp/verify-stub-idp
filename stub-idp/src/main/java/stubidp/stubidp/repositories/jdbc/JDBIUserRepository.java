package stubidp.stubidp.repositories.jdbc;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.jdbi.v3.core.Jdbi;
import stubidp.stubidp.domain.DatabaseEidasUser;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.repositories.UserRepository;
import stubidp.stubidp.repositories.jdbc.rowmappers.UserRowMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Singleton
public class JDBIUserRepository implements UserRepository {

    private final Jdbi jdbi;
    private final UserMapper userMapper;
    private final UserDBCollector userDBCollector;

    @Inject
    public JDBIUserRepository(
            Jdbi jdbi,
            UserMapper userMapper
    ) {
        this.jdbi = jdbi;
        this.userMapper = userMapper;
        this.userDBCollector = new UserDBCollector(this).register();
    }

    public JDBIUserRepository(
            Jdbi jdbi,
            UserMapper userMapper,
            boolean isTest
    ) {
        this.jdbi = jdbi;
        this.userMapper = userMapper;
        this.userDBCollector = null;
    }

    @Override
    public Collection<DatabaseIdpUser> getUsersForIdp(String idpFriendlyName) {
        List<User> users = jdbi.withHandle(handle ->
                handle.createQuery(
                        "select * from users " +
                                "where identity_provider_friendly_id = :idpFriendlyName")
                        .bind("idpFriendlyName", idpFriendlyName)
                        .map(new UserRowMapper())
                        .list()
        );

        return users.stream()
                .map(userMapper::mapToIdpUser)
                .collect(toList());
    }

    @Override
    public void addOrUpdateUserForIdp(String idpFriendlyId, DatabaseIdpUser idpUser) {
        deleteUserFromIdp(idpFriendlyId, idpUser.getUsername());

        User user = userMapper.mapFrom(idpFriendlyId, idpUser);

        jdbi.withHandle(handle ->
                handle.createUpdate(
                        "INSERT INTO users(username, password, identity_provider_friendly_id, \"data\") " +
                                "VALUES (:username, :password, :idpFriendlyId, to_json(:json))")
                        .bind("username", user.getUsername())
                        .bind("password", user.getPassword())
                        .bind("idpFriendlyId", idpFriendlyId)
                        .bind("json", user.getData())
                        .execute()
        );
    }

    @Override
    public void addOrUpdateEidasUserForStubCountry(String stubCountryName, DatabaseEidasUser eidasUser) {
        deleteUserFromIdp(stubCountryName, eidasUser.getUsername());

        User user = userMapper.mapFrom(stubCountryName, eidasUser);

        jdbi.withHandle(handle ->
                handle.createUpdate(
                        "INSERT INTO users(username, password, identity_provider_friendly_id, \"data\") " +
                                "VALUES (:username, :password, :idpFriendlyId, to_json(:json))")
                        .bind("username", user.getUsername())
                        .bind("password", user.getPassword())
                        .bind("idpFriendlyId", stubCountryName)
                        .bind("json", user.getData())
                        .execute()
        );
    }

    @Override
    public void deleteUserFromIdp(String idpFriendlyId, String username) {
        jdbi.withHandle(handle ->
                handle.createUpdate(
                        "DELETE FROM users " +
                                "WHERE identity_provider_friendly_id = :idpFriendlyId " +
                                "AND username = :username")
                        .bind("idpFriendlyId", idpFriendlyId)
                        .bind("username", username)
                        .execute()
        );
    }

    @Override
    public Collection<DatabaseEidasUser> getUsersForCountry(String friendlyName) {
        List<User> users = jdbi.withHandle(handle ->
                handle.createQuery(
                        "select * from users " +
                                "where identity_provider_friendly_id = :idpFriendlyName")
                        .bind("idpFriendlyName", friendlyName)
                        .map(new UserRowMapper())
                        .list()
        );

        return users.stream()
                .map(userMapper::mapToEidasUser)
                .collect(toList());
    }

    private long getTotalUserCount() {
        return jdbi.withHandle(handle -> handle.select("select count(*) from users")
                .mapTo(Long.class)
                .one());
    }

    private static class UserDBCollector extends Collector {
        private final JDBIUserRepository jdbiUserRepository;

        UserDBCollector(JDBIUserRepository jdbiUserRepository) {
            this.jdbiUserRepository = jdbiUserRepository;
        }

        @Override
        public List<MetricFamilySamples> collect() {
            GaugeMetricFamily userGauge = new GaugeMetricFamily("stubidp_db_users_total", "Total number of registered users (idp + eidas).", jdbiUserRepository.getTotalUserCount());
            return List.of(userGauge);
        }
    }
}
