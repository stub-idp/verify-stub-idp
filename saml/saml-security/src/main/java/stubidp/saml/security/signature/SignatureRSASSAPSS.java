package stubidp.saml.security.signature;

import org.apache.xml.security.signature.XMLSignature;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;

public final class SignatureRSASSAPSS implements SignatureAlgorithm {

    /** {@inheritDoc} */
    @NonNull public String getKey() {
        return JCAConstants.KEY_ALGO_RSA;
    }

    /** {@inheritDoc} */
    @NonNull public String getURI() {
        return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1;
    }

    /** {@inheritDoc} */
    @NonNull public AlgorithmType getType() {
        return AlgorithmType.Signature;
    }

    /** {@inheritDoc} */
    @NonNull public String getJCAAlgorithmID() {
        return "RSAwithSHA256andMGF1";
    }

    /** {@inheritDoc} */
    @NonNull public String getDigest() {
        return JCAConstants.DIGEST_SHA256;
    }

}
