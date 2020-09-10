package stubsp.stubsp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.jackson.Jackson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.response.InboundResponseFromIdp;
import stubsp.stubsp.domain.SamlResponseFromIdpDto;
import stubsp.stubsp.saml.response.SamlResponseDecrypter;

import javax.inject.Inject;
import java.util.Base64;

public class SamlResponseService {

    private final SamlResponseDecrypter samlResponseDecrypter;

    @Inject
    public SamlResponseService(SamlResponseDecrypter samlResponseDecrypter) {
        this.samlResponseDecrypter = samlResponseDecrypter;
    }

    public SamlResponseFromIdpDto processResponse(String samlResponse, String relayState) {
        Document document = Jsoup.parse(new String(Base64.getMimeDecoder().decode(samlResponse)));
        document.outputSettings().prettyPrint(true);
        InboundResponseFromIdp<IdentityProviderAssertion> inboundResponseFromIdp = samlResponseDecrypter.decryptSaml(samlResponse);
        switch(inboundResponseFromIdp.getStatus().getStatusCode()) {
            case Success: {
                try {
                    return new SamlResponseFromIdpDto(ResponseStatus.SUCCESS,
                            Jackson.newObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(inboundResponseFromIdp),
                            document.toString(),
                            relayState);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            default: {
                try {
                    return new SamlResponseFromIdpDto(ResponseStatus.AUTHENTICATION_FAILED,
                    Jackson.newObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(inboundResponseFromIdp),
                            document.toString(),
                            relayState);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
