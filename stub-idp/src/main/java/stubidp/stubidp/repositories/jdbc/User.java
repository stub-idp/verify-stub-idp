package stubidp.stubidp.repositories.jdbc;

public record User(Integer id,
                   String username,
                   String password,
                   String idpFriendlyId,
                   String data) {
}
