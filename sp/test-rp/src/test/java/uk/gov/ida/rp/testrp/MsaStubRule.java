package uk.gov.ida.rp.testrp;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.dropwizard.testing.ResourceHelpers;
import org.apache.commons.io.FileUtils;
import stubidp.test.devpki.TestCertificateStrings;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class MsaStubRule {

    private final WireMockServer server;

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    public MsaStubRule(String metadataFilename) {
        server = new WireMockServer(wireMockConfig()
                .dynamicHttpsPort()
                .dynamicPort()
                .keystorePath("test_keys/dev_service_ssl.ks")
                .keyManagerPassword("marshmallow")
                .keystorePassword("marshmallow"));

        server.stubFor(WireMock.get(urlEqualTo("/metadata"))
                .willReturn(aResponse()
                        .withBody(getMetadata(metadataFilename))));
        start();
    }

    private String getMetadata(String metadataFilename) {
        try {
            String metadataContent = FileUtils.readFileToString(new File(ResourceHelpers.resourceFilePath(metadataFilename)));
            metadataContent = metadataContent.replaceAll("%MSA_SIGNING%", TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);
            metadataContent = metadataContent.replaceAll("%MSA_ENCRYPTION%", TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT);
            metadataContent = metadataContent.replaceAll("%HUB_SIGNING_ONE%", TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT);
            metadataContent = metadataContent.replaceAll("%HUB_SIGNING_TWO%", TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT);
            metadataContent = metadataContent.replaceAll("%HUB_ENCRYPTION%", TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT);
            return metadataContent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        start();
        return server.port();
    }

    public int getSecurePort() {
        start();
        return server.httpsPort();
    }
}
