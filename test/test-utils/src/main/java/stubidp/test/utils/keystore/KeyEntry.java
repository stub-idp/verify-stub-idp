package stubidp.test.utils.keystore;

public record KeyEntry(String alias, String key, String... certificates) {
}
