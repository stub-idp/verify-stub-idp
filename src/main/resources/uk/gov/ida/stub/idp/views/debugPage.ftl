<#-- @ftlvariable name="" type="uk.gov.ida.stub.idp.views.DebugPageView" -->
<div class="main">
    <div class="tabs">
        <ul>
            <li>
                <a id="tab-login" class="tab-text" href="/${idpId}/login.php">Login</a>
            </li>
            <li>
                <a id="tab-register" class="tab-text" href="/${idpId}/register.php">Register</a>
            </li>
            <li class="on" id="tab-debug">
                <span class="tab-text">System information</span>
            </li>
        </ul>
    </div>

    <h2>System information</h2>

    <p id="registration">
        <#if registration.isPresent() >
            "registration" hint is "${registration.get()?string}"
        <#else>
            "registration" hint not received
        </#if>
    </p>

    <p id="language-hint">
        <#if languageHint?has_content>
            The language hint was set to "${languageHint}".
        <#else>
            No language hint was set.
        </#if>
    </p>

    <#list knownHints>
      <p>The following known hints were sent from the GOV.UK Verify hub:</p>
      <ul class="known-hints">
        <#items as hint>
        <li>${hint}</li>
        </#items>
      </ul>
    </#list>

    <#list unknownHints>
      <p>The following unknown hints were sent:</p>
      <ul class="unknown-hints">
        <#items as hint>
        <li>${hint}</li>
        </#items>
      </ul>
    </#list>

    <#if !knownHints?has_content && !unknownHints?has_content>
      <p>No answer hints were sent.</p>
    </#if>

    <p id="authn-request-comparision-type">
        AuthnRequest comparison type is "${comparisonType}".
    </p>

    <#list authnContexts>
        <p>The following AuthnContexts were sent:</p>
        <ul class="authn-contexts">
        <#items as authnContext>
        <li>${authnContext}</li>
        </#items>
        </ul>
    </#list>

    <p id="authn-request-issuer">
        Request issuer is "${authnRequestIssuer}".
    </p>

    <p id="saml-request-id">
        Request Id is "${samlRequestId}".
    </p>

    <p id="idp-session-id">
        Stub-IDP sessionId is "${sessionId}".
    </p>

    <p id="relay-state">
        Relay state is "${relayState}".
    </p>
</div>
