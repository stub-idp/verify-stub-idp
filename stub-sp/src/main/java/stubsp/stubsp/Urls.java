package stubsp.stubsp;

public interface Urls {
    String SAML_REQUEST_PARAM = "SAMLRequest";
    String SAML_RESPONSE_PARAM = "SAMLResponse";
    String RELAY_STATE_PARAM = "RelayState";

    String STUB_SP_ROOT = "/";

    String STUB_SP_PREFIX = "/stub/sp";

    String ROOT_RESOURCE = STUB_SP_ROOT;
    String AVAILABLE_SERVICE_RESOURCE = STUB_SP_PREFIX + "/available-services";
    String SECURE_RESOURCE = STUB_SP_PREFIX + "/secure";
    String AUTHENTICATION_FAILURE_RESOURCE = STUB_SP_PREFIX + "/authentication-failure";
    String SUCCESS_RESOURCE = STUB_SP_PREFIX + "/authentication-success";
    String SAML_SSO_RESPONSE_RESOURCE = STUB_SP_PREFIX + "/SAML2/SSO/Response";
    String SAML_FEDERATION_METADATA_RESOURCE = STUB_SP_PREFIX + "/SAML2/metadata/federation";
    String INITIATE_SINGLE_IDP_JOURNEY_RESOURCE = STUB_SP_PREFIX + "/initiate-single-idp-service";
}
