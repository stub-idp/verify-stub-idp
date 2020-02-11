# Stub IDP
![Stub IDP logo](././stub-idp/src/main/resources/assets/images/providers/stub-idp-one.png)

This microservice is a stub IDP that can be white-labelled to simulate any IDP, or used as a Stub eIDAS Proxy Service Node.

[![Build Status](https://travis-ci.org/stub-idp/stub-idp.svg?branch=monorepo)](https://travis-ci.org/stub-idp/stub-idp)

## Changes vs alphagov/verify-stub-idp

* Monorepo containing all code apart from third party dependencies
* Minimized use of third party dependencies
* Guava -> stdlib wherever possible
* Dependabot updates
* Dropwizard 2
* Junit 5 Jupiter
* No guice, only hk2 for DI
* No deprecated code and warnings-as-errors when compiling

* More testing and security features
* Prometheus metrics
* Configurable features - idp/singleidp/eidas/headless can be toggled on & off
* Secure cookie on by default
* JPMS partially working (needs changes in Dropwizard to be fully enabled)
* CSRF enabled on all forms
* No Gradle, only Maven
* Adding in changes from alphagov
* All user passwords are hashed before they go into the database (no plaintext passwords) 
* stub-sp service

## Running

*First* generate a set of test PKI files `./configuration/pki-gen/go.sh`
Requires: cfssl, openssl, ruby

*Second* generate dist zip packages that will contain startup scripts and test PKI

*Third* run the stub-idp & stub-sp apps - they will use each other's dynamic metadata

## Configuring the app

A yaml file containing IDPs needs to be created and the file location set as  `stubIdpsYmlFileLocation` in the app yml config file

The format is as follows:

```yaml
stubIdps:
  - friendlyId: stub-idp-one-for-rp1
    displayName: Stub IDP One
    assetId: stub-idp-one
    sendKeyInfo: true
    idpUserCredentials:
        - user: userName
          password: <password hash>
```

The `friendlyId` can be used to enable multiple IDPs but using the same `displayName` and `assetId` e.g. for different on-boarding relying parties.

Header images for IDPs should be placed into `ida-stub-idp/src/main/resources/assets/images/providers/` and are referenced as `assetId` e.g. stub-idp-one.png is referenced as `stub-idp-one`

## Connecting to a hub/using as an IDP

You need to use the entityId `http://stub_idp.acme.org/{friendlyId}/SSO/POST` or as the template configured in the main config file, with the shared key/cert configured in the main config file (`stub-idp.yml`)

The SSO URI for that IDP will be `http://localhost:50140/{friendlyId}/SAML2/SSO` or equivalent.

You need to set the hub (or service) entityId that messages will be received from/sent back to, as well as where the hub/service metadata can be received from -> see the `hubEntityId` and `metadata` configuration blocks.

# Using as a Stub eIDAS Proxy Service Node

Set the values in `europeanIdentity` including HUB_CONNECTOR_ENTITY_ID for the consuming relying party/service provider/hub.

Several countries can be dynamically stubbed at once - see the full list in [EidasScheme](src/main/java/uk/gov/ida/stub/idp/domain/EidasScheme.java) (use the values, not the enum keys).  Once the metadata is retrieved for each stub it can be used by the consuming relying party/service provider/hub.  Metadata is at http://localhost:50140/[scheme]/ServiceMetadata  Test users are all `stub-country*`.

## Test Users

Test users can be uploaded to the IDPs [docs](https://alphagov.github.io/rp-onboarding-tech-docs/pages/env/envEndToEndTests.html#createtestusers)

Basic auth for the `/{friendlyId}/users` endpoint is enabled by default and can be configured using `basicAuthEnabledForUserResource`

# Licence

[MIT Licence](LICENCE)
