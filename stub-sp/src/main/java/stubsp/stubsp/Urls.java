package stubsp.stubsp;

public interface Urls {

    String STUB_SP_ROOT = "/";

    String STUB_SP_PREFIX = "/stub/sp";

    String ROOT_RESOURCE = STUB_SP_ROOT;
    String AVAILABLE_SERVICE_RESOURCE = STUB_SP_PREFIX + "/available-services";
    String SECURE_RESOURCE = STUB_SP_PREFIX + "/secure";
    String SAML_SSO_RESPONSE_RESOURCE = STUB_SP_PREFIX + "/SAML2/SSO/Response";
    String SAML_FEDERATION_METADATA_RESOURCE = STUB_SP_PREFIX + "/SAML2/metadata/federation";
    String INITIATE_SINGLE_IDP_JOURNEY_RESOURCE = STUB_SP_PREFIX + "/initiate-single-idp-service";
}
