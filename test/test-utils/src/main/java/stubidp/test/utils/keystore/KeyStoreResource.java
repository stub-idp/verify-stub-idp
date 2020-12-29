package stubidp.test.utils.keystore;

import stubidp.test.utils.helpers.ManagedFileResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class KeyStoreResource implements ManagedFileResource {
    private final static String password = "password";
    private final File file;
    private final List<KeyEntry> keys;
    private final List<CertificateEntry> certificates;

    private KeyStore keyStore;

    public KeyStoreResource(File file, List<KeyEntry> keys, List<CertificateEntry> certificates) {
        this.file = file;
        this.keys = keys;
        this.certificates = certificates;
    }
    public String getPassword() {
        return password;
    }

    public File getFile() {
        return file;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    @Override
    public void create() {
        loadKeyStore();
        setKeys();
        setCertificates();
        writeKeyStore();
    }

    @Override
    public void delete() {
        file.delete();
    }

    private KeyStore loadKeyStore() {
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException e) {
            throw new RuntimeException(e);
        }
        return keyStore;
    }

    private void setKeys() {
        X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
        PrivateKeyFactory privateKeyFactory = new PrivateKeyFactory();
        keys.forEach((entry) -> {
            X509Certificate[] x509Certificates = Arrays.stream(entry.getCertificates())
                    .map(x509CertificateFactory::createCertificate)
                    .toArray(X509Certificate[]::new);
            PrivateKey key = privateKeyFactory.createPrivateKey(entry.getKey().getBytes());
            try {
                keyStore.setKeyEntry(entry.getAlias(), key, getPassword().toCharArray(), x509Certificates);
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setCertificates() {
        X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
        certificates.forEach((entry) -> {
            X509Certificate x509Certificate = x509CertificateFactory.createCertificate(entry.getCertificate());
            try {
                keyStore.setCertificateEntry(entry.getAlias(), x509Certificate);
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void writeKeyStore() {
        try (FileOutputStream fos = new FileOutputStream(getFile());) {
            keyStore.store(fos, getPassword().toCharArray());
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
