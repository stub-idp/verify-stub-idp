package stubidp.utils.security.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CryptoHelperTest {

    private static final String EXAMPLE_IDP = "http://example.local/idp";
    private static final String KEY = base64(new byte[CryptoHelper.KEY_AND_NONCE_AND_IV_LENGTH_IN_BYTES]);
    private static CryptoHelper cryptoHelper;

    private static String base64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static byte[] unbase64(String data) {
        return Base64.getDecoder().decode(data);
    }

    @BeforeAll
    static void setUp() {
        cryptoHelper = new CryptoHelper(KEY);
    }

    @Test
    void testInitializationVectorIsCorrectLength() {
        assertThat(CryptoHelper.KEY_AND_NONCE_AND_IV_LENGTH_IN_BYTES).isEqualTo(16);
    }

    @Test
    void testShouldDecryptToTheOriginalValue() {
        assertThat(EXAMPLE_IDP).isEqualTo(
                cryptoHelper.decrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(EXAMPLE_IDP).get()).get());
    }

    @Test
    void testMultipleEncryptionsOfSameIDPEntityIDResultInDifferentValues() {
        assertThat(cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(EXAMPLE_IDP).get()).isNotEqualTo(
                cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(EXAMPLE_IDP).get()
        );
    }

    @Test
    void testEncryptedDataShouldNotContainUnencryptedData() {
        final String encrypted = Arrays.toString(unbase64(cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(EXAMPLE_IDP).get()));
        assertThat(encrypted).doesNotContain(EXAMPLE_IDP);
    }

    @Test
    void testOrderOfEncryptedNotImportantByDecryptngInADifferentOrder() {
        final int count = 100;
        Map<String, String> encryptedValues = new HashMap<>();
        for (int i=0; i<count; i++) {
            String idpEntityId = EXAMPLE_IDP + "/" + i;
            encryptedValues.put(idpEntityId, cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(idpEntityId).get());
        }
        List<String> shuffledKeys = new ArrayList<>(encryptedValues.keySet());
        Collections.shuffle(shuffledKeys);
        for(String key:shuffledKeys) {
            assertThat(key).isEqualTo(cryptoHelper.decrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(encryptedValues.get(key)).get());
        }

    }

    @Test
    void testEncryptedValuesShouldDecryptRepeatedly() {
        String encryptedValue = cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(EXAMPLE_IDP).get();
        for (int i=0; i<100; i++) {
            assertThat(EXAMPLE_IDP).isEqualTo(cryptoHelper.decrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(encryptedValue).get());
        }
     }

    @Test
    void testCannotDecryptWithChangedKey() {
        String encryptedValue = cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(EXAMPLE_IDP).get();
        CryptoHelper otherCryptoHelper = new CryptoHelper(base64("sixteenbyteslong".getBytes()));
        // Catch a BadPaddingException in decrypt
        assertThat(otherCryptoHelper.decrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(encryptedValue).isPresent()).isFalse();
    }

    @Test
    void testCannotConstructCryptoHelperWithIncorrectKeyLength() {
        assertThrows(IllegalArgumentException.class, () -> new CryptoHelper(base64(new byte[CryptoHelper.KEY_AND_NONCE_AND_IV_LENGTH_IN_BYTES+1])));
    }

    @Test
    void testShortEntityIDProducesSameLengthEncryptedOutput() {
        String veryLongIdpEntityId = "http://example.local/something/incredibly/long/which/wont/be/beaten/by/a/real/life/entityId/like_______________________________this";
        String standedEntityIDEncryptionValue = cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(veryLongIdpEntityId).get();
        String shortEntityIDEncryptionValue = cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited("h").get();
        assertThat(unbase64(standedEntityIDEncryptionValue).length).isEqualTo(unbase64(shortEntityIDEncryptionValue).length);
    }

    @Test
    void testExtremelyLongEntityIDsShouldNotBeAccepted() {
        assertThrows(IllegalArgumentException.class, () -> cryptoHelper.encrypt_yesIKnowThisCryptoCodeHasNotBeenAudited(new String(new byte[2000])));
    }

    @Test
    void testDecryptingEmptyStringReturnsAbsent() {
        assertThat(cryptoHelper.decrypt_yesIKnowThisCryptoCodeHasNotBeenAudited("").isPresent()).isFalse();
    }
}