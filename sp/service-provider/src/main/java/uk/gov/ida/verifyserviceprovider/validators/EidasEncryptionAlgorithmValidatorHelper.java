package uk.gov.ida.verifyserviceprovider.validators;

import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;

import java.util.Set;

public class EidasEncryptionAlgorithmValidatorHelper {
    public static EncryptionAlgorithmValidator anEidasEncryptionAlgorithmValidator() {
        return new EncryptionAlgorithmValidator(
                Set.of(
                        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM,
                        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192_GCM,
                        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM
                ),
                Set.of(
                        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP,
                        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP11
                )
        );
    }
}
