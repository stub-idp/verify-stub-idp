package stubidp.stubidp;

public interface Urls {
    // params
    String SAML_REQUEST_PARAM = "SAMLRequest";
    String HINTS_PARAM = "hint";
    String RELAY_STATE_PARAM = "RelayState";
    String LANGUAGE_HINT_PARAM = "language";
    String IDP_ID_PARAM = "idpId";
    String SCHEME_ID_PARAM = "schemeId";
    String REGISTRATION_PARAM = "registration";
    String SUBMIT_PARAM = "submit";
    String ERROR_MESSAGE_PARAM = "errorMessage";
    String SINGLE_IDP_JOURNEY_ID_PARAM = "singleIdpJourneyIdentifier";
    @SuppressWarnings("squid:S2068")
    String PASSWORD_PARAM = "password";
    String USERNAME_PARAM = "username";
    String LOGIN_FAILURE_STATUS_PARAM = "failureStatus";
    String REQUESTER_ERROR_MESSAGE_PARAM = "requesterErrorMessage";
    String DATE_OF_BIRTH_PARAM = "dateOfBirth";
    String INCLUDE_GENDER_PARAM = "includeGender";
    String GENDER_PARAM = "gender";
    String ADDRESS_POST_CODE_PARAM = "addressPostCode";
    String ADDRESS_TOWN_PARAM = "addressTown";
    String ADDRESS_LINE2_PARAM = "addressLine2";
    String ADDRESS_LINE1_PARAM = "addressLine1";
    String SURNAME_PARAM = "surname";
    String NON_LATIN_SURNAME_PARAM = "nonLatinSurname";
    String FIRSTNAME_PARAM = "firstname";
    String NON_LATIN_FIRSTNAME_PARAM = "nonLatinFirstname";
    String RANDOMISE_PID_PARAM = "randomPid";
    String CYCLE3_PARAM = "c3";
    String LEVEL_OF_ASSURANCE_PARAM = "loa";
    String SIGNING_ALGORITHM_PARAM = "signingAlgorithm";
    String SOURCE_PARAM = "source";
    String SOURCE_PARAM_PRE_REG_VALUE = "pre-reg";
    String SIGN_ASSERTIONS_PARAM = "assertionOptions";

    String LOGIN_PATH = "/login";
    String REGISTER_PATH = "/register";
    String DEBUG_PATH = "/debug";
    String CONSENT_PATH = "/consent";

    // root routes
    String ROOT_PREFIX = "/stub"; //can be left blank, needs no trailing slash if not blank
    String ROUTE_SUFFIX = ".idp"; // can be left blank

    // paths and resources
    String HEADLESS_ROOT = "/headless";
    String IDP_ROOT_PATH = ROOT_PREFIX + "/idp" + "/{" + IDP_ID_PARAM + "}";
    String EIDAS_ROOT_PATH = ROOT_PREFIX + "/country" + "/{" + SCHEME_ID_PARAM + "}";

    String SAML_SSO_PATH = "/SAML2/SSO";
    String IDP_SAML2_SSO_RESOURCE = IDP_ROOT_PATH + SAML_SSO_PATH;
    String EIDAS_SAML2_SSO_RESOURCE = EIDAS_ROOT_PATH + SAML_SSO_PATH;

    // idp
    String LOGIN_AUTHN_FAILURE_PATH = "/authn-failure";
    String LOGIN_NO_AUTHN_CONTEXT_PATH = "/no-authn-context";
    String LOGIN_FRAUD_FAILURE_PATH = "/fraud-failure";
    String LOGIN_UPLIFT_FAILED_PATH = "/uplift-failed";
    String LOGIN_REQUESTER_ERROR_PATH = "/requester-error";
    String LOGIN_AUTHN_PENDING_PATH = "/authn-pending";

    String IDP_LOGIN_RESOURCE = IDP_ROOT_PATH + LOGIN_PATH + ROUTE_SUFFIX;
    String IDP_REGISTER_RESOURCE = IDP_ROOT_PATH + REGISTER_PATH + ROUTE_SUFFIX;
    String IDP_DEBUG_RESOURCE = IDP_ROOT_PATH + DEBUG_PATH + ROUTE_SUFFIX;
    String IDP_CONSENT_RESOURCE = IDP_ROOT_PATH + CONSENT_PATH + ROUTE_SUFFIX;

    String IDP_AUTHN_FAILURE_RESOURCE = IDP_LOGIN_RESOURCE + LOGIN_AUTHN_FAILURE_PATH;
    String IDP_NO_AUTHN_CONTEXT_RESOURCE = IDP_LOGIN_RESOURCE + LOGIN_NO_AUTHN_CONTEXT_PATH;
    String IDP_FRAUD_FAILURE_RESOURCE = IDP_LOGIN_RESOURCE + LOGIN_FRAUD_FAILURE_PATH;
    String IDP_UPLIFT_FAILED_RESOURCE = IDP_LOGIN_RESOURCE + LOGIN_UPLIFT_FAILED_PATH;
    String IDP_REQUESTER_ERROR_RESOURCE = IDP_LOGIN_RESOURCE + LOGIN_REQUESTER_ERROR_PATH;
    String IDP_AUTHN_PENDING_RESOURCE = IDP_LOGIN_RESOURCE + LOGIN_AUTHN_PENDING_PATH;

    // test user management
    String USERS_RESOURCE = IDP_ROOT_PATH + "/users";
    String DELETE_USER_PATH = "/delete";
    String GET_USER_PATH = "/{" + USERNAME_PARAM + "}";

    @SuppressWarnings("squid:S2068")
    String PASSWORD_GEN_RESOURCE = USERS_RESOURCE + "/password-gen" + ROUTE_SUFFIX;

    // standalone idp
    String SINGLE_IDP_HOMEPAGE_RESOURCE = IDP_ROOT_PATH;
    String SINGLE_IDP_LOGOUT_RESOURCE = IDP_ROOT_PATH + "/logout" + ROUTE_SUFFIX;
    String SINGLE_IDP_PRE_REGISTER_RESOURCE = IDP_ROOT_PATH + "/pre-register" + ROUTE_SUFFIX;
    String SINGLE_IDP_PRE_REGISTER_CANCEL_PATH = "/cancel";
    String SINGLE_IDP_CANCEL_PRE_REGISTER_RESOURCE = SINGLE_IDP_PRE_REGISTER_RESOURCE + SINGLE_IDP_PRE_REGISTER_CANCEL_PATH;
    String SINGLE_IDP_START_PROMPT_RESOURCE = IDP_ROOT_PATH + "/start-prompt" + ROUTE_SUFFIX;

    // stub country
    String EIDAS_METADATA_RESOURCE = EIDAS_ROOT_PATH + "/ServiceMetadata";
    String EIDAS_LOGIN_RESOURCE = EIDAS_ROOT_PATH + LOGIN_PATH + ROUTE_SUFFIX;
    String EIDAS_AUTHN_FAILURE_RESOURCE = EIDAS_LOGIN_RESOURCE + LOGIN_AUTHN_FAILURE_PATH;
    String EIDAS_REGISTER_RESOURCE = EIDAS_ROOT_PATH + REGISTER_PATH + ROUTE_SUFFIX;
    String EIDAS_CONSENT_RESOURCE = EIDAS_ROOT_PATH + CONSENT_PATH + ROUTE_SUFFIX;
    String EIDAS_DEBUG_RESOURCE = EIDAS_ROOT_PATH + DEBUG_PATH + ROUTE_SUFFIX;

}
