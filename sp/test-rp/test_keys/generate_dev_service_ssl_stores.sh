#!/usr/bin/env sh

# generate keystore
keytool -genkeypair -keystore dev_service_ssl.ks -storepass marshmallow -keypass marshmallow -alias testrp_msa_tls -dname "CN=test-rp, OU=test, O=test, L=London, C=GB" -validity 3650

# export cert
keytool -exportcert -alias testrp_msa_tls -keystore dev_service_ssl.ks -storepass marshmallow | base64 | xargs ~/git/verify-hub/bin/str2crt > testrp_msa_tls.crt

# generate truststore
keytool -importcert -file testrp_msa_tls.crt -keystore dev_service_ssl.ts -storepass marshmallow -alias testrp_msa_tls -noprompt

