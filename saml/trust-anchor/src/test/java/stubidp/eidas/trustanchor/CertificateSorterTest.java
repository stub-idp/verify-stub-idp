package stubidp.eidas.trustanchor;

import org.junit.jupiter.api.Test;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CertificateSorterTest {

    @Test
    void shouldReturnEmptyListWhenGivenEmptyList() {
        List<X509Certificate> sortedCerts = CertificateSorter.sort(List.of());
        assertThat(sortedCerts).isEmpty();
    }

    @Test
    void shouldReturnListWhenGivenSingleCert() {
        List<X509Certificate> testCertificates = List.of(mock(X509Certificate.class));
        List<X509Certificate> sortedCerts = CertificateSorter.sort(testCertificates);

        assertThat(sortedCerts).isEqualTo(testCertificates);
    }

    @Test
    void shouldReturnSameTwoCertListWhenGivenTwoCertsInOrder() {
        X509Certificate leafCert = mock(X509Certificate.class);
        X509Certificate parentCert = mock(X509Certificate.class);
        X500Principal x500Parent = new X500Principal(principalName("parent"));
        X500Principal x500Leaf = new X500Principal(principalName("leaf"));

        when(leafCert.getIssuerX500Principal()).thenReturn(x500Parent);
        when(leafCert.getSubjectX500Principal()).thenReturn(x500Leaf);
        when(parentCert.getSubjectX500Principal()).thenReturn(x500Parent);

        List<X509Certificate> testCertificates = List.of(leafCert, parentCert);
        List<X509Certificate> sortedCerts = CertificateSorter.sort(testCertificates);

        assertThat(sortedCerts).isEqualTo(testCertificates);
    }

    @Test
    void shouldReturnSortedTwoCertListWhenGivenTwoCertsNotInOrder() {
        X509Certificate leafCert = mock(X509Certificate.class);
        X509Certificate parentCert = mock(X509Certificate.class);
        X500Principal x500Parent = new X500Principal(principalName("parent"));
        X500Principal x500Leaf = new X500Principal(principalName("leaf"));

        when(leafCert.getIssuerX500Principal()).thenReturn(x500Parent);
        when(leafCert.getSubjectX500Principal()).thenReturn(x500Leaf);
        when(parentCert.getSubjectX500Principal()).thenReturn(x500Parent);

        List<X509Certificate> testCertificates = List.of(parentCert, leafCert);
        List<X509Certificate> sortedCerts = CertificateSorter.sort(testCertificates);

        List<X509Certificate> controlCertificates = List.of(leafCert, parentCert);
        assertThat(sortedCerts).isEqualTo(controlCertificates);
    }

    @Test
    void shouldReturnSortedThreeCertListWhenGivenThreeCertsInOrder() {
        X509Certificate leafCert = mock(X509Certificate.class);
        X509Certificate intermediaryCert = mock(X509Certificate.class);
        X509Certificate parentCert = mock(X509Certificate.class);
        X500Principal x500Parent = new X500Principal(principalName("parent"));
        X500Principal x500Intermediary = new X500Principal(principalName("intermediary"));
        X500Principal x500Leaf = new X500Principal(principalName("leaf"));

        when(leafCert.getIssuerX500Principal()).thenReturn(x500Intermediary);
        when(leafCert.getSubjectX500Principal()).thenReturn(x500Leaf);
        when(intermediaryCert.getIssuerX500Principal()).thenReturn(x500Parent);
        when(intermediaryCert.getSubjectX500Principal()).thenReturn(x500Intermediary);
        when(parentCert.getSubjectX500Principal()).thenReturn(x500Parent);

        List<X509Certificate> testCertificates = List.of(leafCert, intermediaryCert, parentCert);
        List<X509Certificate> sortedCerts = CertificateSorter.sort(testCertificates);

        assertThat(sortedCerts).isEqualTo(testCertificates);
    }

    @Test
    void shouldReturnSortedThreeCertListWhenGivenThreeCertsNotInOrderWithIntermediaryAndParentSwapped() {
        X509Certificate leafCert = mock(X509Certificate.class);
        X509Certificate intermediaryCert = mock(X509Certificate.class);
        X509Certificate parentCert = mock(X509Certificate.class);
        X500Principal x500Parent = new X500Principal(principalName("parent"));
        X500Principal x500Intermediary = new X500Principal(principalName("intermediary"));
        X500Principal x500Leaf = new X500Principal(principalName("leaf"));

        when(leafCert.getIssuerX500Principal()).thenReturn(x500Intermediary);
        when(leafCert.getSubjectX500Principal()).thenReturn(x500Leaf);
        when(intermediaryCert.getIssuerX500Principal()).thenReturn(x500Parent);
        when(intermediaryCert.getSubjectX500Principal()).thenReturn(x500Intermediary);
        when(parentCert.getSubjectX500Principal()).thenReturn(x500Parent);

        List<X509Certificate> testCertificates = List.of(leafCert, parentCert, intermediaryCert);
        List<X509Certificate> sortedCerts = CertificateSorter.sort(testCertificates);

        List<X509Certificate> controlCertificates = List.of(leafCert, intermediaryCert, parentCert);
        assertThat(sortedCerts).isEqualTo(controlCertificates);
    }

    @Test
    void shouldReturnSortedThreeCertListWhenGivenThreeCertsNotInOrderWithLeafAndParentSwapped() {
        X509Certificate leafCert = mock(X509Certificate.class);
        X509Certificate intermediaryCert = mock(X509Certificate.class);
        X509Certificate parentCert = mock(X509Certificate.class);
        X500Principal x500Parent = new X500Principal(principalName("parent"));
        X500Principal x500Intermediary = new X500Principal(principalName("intermediary"));
        X500Principal x500Leaf = new X500Principal(principalName("leaf"));

        when(leafCert.getIssuerX500Principal()).thenReturn(x500Intermediary);
        when(leafCert.getSubjectX500Principal()).thenReturn(x500Leaf);
        when(intermediaryCert.getIssuerX500Principal()).thenReturn(x500Parent);
        when(intermediaryCert.getSubjectX500Principal()).thenReturn(x500Intermediary);
        when(parentCert.getSubjectX500Principal()).thenReturn(x500Parent);

        List<X509Certificate> testCertificates = List.of(parentCert, intermediaryCert, leafCert);
        List<X509Certificate> sortedCerts = CertificateSorter.sort(testCertificates);

        List<X509Certificate> controlCertificates = List.of(leafCert, intermediaryCert, parentCert);
        assertThat(sortedCerts).isEqualTo(controlCertificates);
    }

    @Test
    void shouldReturnSortedThreeCertListWhenGivenThreeCertsNotInOrderWithLeafAndIntermediarySwapped() {
        X509Certificate leafCert = mock(X509Certificate.class);
        X509Certificate intermediaryCert = mock(X509Certificate.class);
        X509Certificate parentCert = mock(X509Certificate.class);
        X500Principal x500Parent = new X500Principal(principalName("parent"));
        X500Principal x500Intermediary = new X500Principal(principalName("intermediary"));
        X500Principal x500Leaf = new X500Principal(principalName("leaf"));

        when(leafCert.getIssuerX500Principal()).thenReturn(x500Intermediary);
        when(leafCert.getSubjectX500Principal()).thenReturn(x500Leaf);
        when(intermediaryCert.getIssuerX500Principal()).thenReturn(x500Parent);
        when(intermediaryCert.getSubjectX500Principal()).thenReturn(x500Intermediary);
        when(parentCert.getSubjectX500Principal()).thenReturn(x500Parent);

        List<X509Certificate> testCertificates = List.of(intermediaryCert, leafCert, parentCert);
        List<X509Certificate> sortedCerts = CertificateSorter.sort(testCertificates);

        List<X509Certificate> controlCertificates = List.of(leafCert, intermediaryCert, parentCert);
        assertThat(sortedCerts).isEqualTo(controlCertificates);
    }

    @Test
    void shouldThrowAnExceptionIfMoreThanOneLeafCerts() {
        X509Certificate leafCert = mock(X509Certificate.class);
        X509Certificate surplusLeafCert = mock(X509Certificate.class);
        X509Certificate parentCert = mock(X509Certificate.class);
        X500Principal x500Parent = new X500Principal(principalName("parent"));
        X500Principal x500Surplus = new X500Principal(principalName("surplus"));
        X500Principal x500Leaf = new X500Principal(principalName("leaf"));

        when(leafCert.getIssuerX500Principal()).thenReturn(x500Parent);
        when(leafCert.getSubjectX500Principal()).thenReturn(x500Leaf);
        when(surplusLeafCert.getIssuerX500Principal()).thenReturn(x500Parent);
        when(surplusLeafCert.getSubjectX500Principal()).thenReturn(x500Surplus);
        when(parentCert.getSubjectX500Principal()).thenReturn(x500Parent);

        List<X509Certificate> testCertificates = List.of(surplusLeafCert, leafCert, parentCert);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> CertificateSorter.sort(testCertificates));
    }

    @Test
    void shouldThrowAnExceptionIfDuplicateIssuerCerts() {
        X509Certificate leafCert = mock(X509Certificate.class);
        X509Certificate duplicateParentCert = mock(X509Certificate.class);
        X509Certificate parentCert = mock(X509Certificate.class);
        X500Principal x500Parent = new X500Principal(principalName("parent"));
        X500Principal x500Leaf = new X500Principal(principalName("leaf"));

        when(leafCert.getIssuerX500Principal()).thenReturn(x500Parent);
        when(leafCert.getSubjectX500Principal()).thenReturn(x500Leaf);
        when(parentCert.getSubjectX500Principal()).thenReturn(x500Parent);
        when(duplicateParentCert.getSubjectX500Principal()).thenReturn(x500Parent);

        List<X509Certificate> testCertificates = List.of(duplicateParentCert, leafCert, parentCert);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> CertificateSorter.sort(testCertificates));
    }

    private String principalName(String id) {
        return "CN=" + id + ", OU=GDS, O=CO, C=UK";
    }
}
