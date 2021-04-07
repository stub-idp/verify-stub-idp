package stubidp.stubidp.repositories.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import stubidp.stubidp.domain.DatabaseEidasUser;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.exceptions.InvalidUserInDatabaseException;

import javax.inject.Singleton;

@Singleton
public class UserMapper {

    private final ObjectMapper mapper;

    public UserMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public User mapFrom(String idpFriendlyName, DatabaseIdpUser idpUser) {
        try {
            String idpUserAsJson = mapper.writeValueAsString(idpUser);
            return new User(
                    null,
                    idpUser.getUsername(),
                    idpUser.getPassword(),
                    idpFriendlyName,
                    idpUserAsJson
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public User mapFrom(String stubCountryFriendlyName, DatabaseEidasUser eidasUser) {
        try {
            String eidasUserAsJson = mapper.writeValueAsString(eidasUser);
            return new User(
                    null,
                    eidasUser.getUsername(),
                    eidasUser.getPassword(),
                    stubCountryFriendlyName,
                    eidasUserAsJson
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public DatabaseIdpUser mapToIdpUser(User user) {
        try {
            return mapper.readValue(user.data(), DatabaseIdpUser.class);
        } catch (JsonProcessingException e) {
            throw new InvalidUserInDatabaseException(e);
        }
    }

    public DatabaseEidasUser mapToEidasUser(User user) {
        try {
            return mapper.readValue(user.data(), DatabaseEidasUser.class);
        } catch (JsonProcessingException e) {
            throw new InvalidUserInDatabaseException(e);
        }
    }
}
