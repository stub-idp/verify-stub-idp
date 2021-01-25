<#-- @ftlvariable name="" type="stubidp.stubidp.views.EidasDebugPageView" -->
<div class="main">
    <div class="tabs">
        <ul>
            <li>
               <a id="tab-login" class="tab-text" href="${eidasLoginResource}">Login</a>
            </li>
            <li>
                <a id="tab-register" class="tab-text" href="${eidasRegistrationResource}">Register</a>
            </li>
            <li class="on" id="tab-debug">
                <span class="tab-text">System information</span>
            </li>
        </ul>
    </div>

    <h2>System information</h2>

    <p id="language-hint">
        <#if languageHint?has_content>
            The language hint was set to "${languageHint}".
        <#else>
            No language hint was set.
        </#if>
    </p>

    <#list requestedAttributes>
        <p>The following RequestedAttributes were requested:</p>
        <ul class="requested-attributes">
        <#items as reqattr>
        <li>${reqattr}</li>
        </#items>
        </ul>
    </#list>

    <p id="authn-request-issuer">
        Request issuer is "${authnRequestIssuer}".
    </p>

    <p id="authn-request-requested-level-of-assurance">
        Requested level of assurance is "${requestedLevelOfAssurance}".
    </p>

    <p id="saml-request-id">
        Request Id is "${samlRequestId}".
    </p>

    <p id="idp-session-id">
        Stub-IDP sessionId is "${sessionId}".
    </p>

    <p id="idp-session-start-time">
        Stub-IDP session start time is "${startTime}".
    </p>

    <p id="relay-state">
        Relay state is "${relayState}".
    </p>
</div>
