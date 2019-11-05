#!/usr/bin/env bash

script_dir="$(cd $(dirname "${BASH_SOURCE[0]}") && pwd)"

sources="metadata"
output="metadata/output"
cadir="$PWD/ca-certificates"
certdir="$PWD/pki"

mkdir -p "$sources/dev/idps"
mkdir -p "$sources/dev-connector"
rm -f "$output/*"

# generate
bundle >/dev/null

echo "$(tput setaf 3)Generating metadata sources$(tput sgr0)"
$script_dir/metadata-sources.rb \
  "$certdir"/sp_signing_primary.crt \
  "$certdir"/sp_encryption_primary.crt \
  "$certdir"/stub_idp_signing_primary.crt \
  "$sources/dev" || exit 1

$script_dir/connector-metadata-sources.rb \
  "$certdir"/sp_signing_primary.crt \
  "$certdir"/sp_encryption_primary.crt \
  "$sources/dev-connector" || exit 1

echo "$(tput setaf 3)Generating Sp federation metadata XML$(tput sgr0)"
bundle exec generate_metadata -c "$sources" -e dev -w -o "$output" --valid-until=36500 \
  --hubCA "$cadir"/dev-root-ca.pem.test \
  --hubCA "$cadir"/dev-sp-ca.pem.test \
  --idpCA "$cadir"/dev-root-ca.pem.test \
  --idpCA "$cadir"/dev-idp-ca.pem.test

echo "$(tput setaf 3)Generating eIDAS connector metadata XML$(tput sgr0)"
bundle exec generate_metadata --connector -c "$sources" -e dev-connector -w -o "$output" --valid-until=36500 \
  --hubCA "$cadir"/dev-root-ca.pem.test \
  --hubCA "$cadir"/dev-sp-ca.pem.test \
  --idpCA "$cadir"/dev-root-ca.pem.test \
  --idpCA "$cadir"/dev-idp-ca.pem.test

for src in dev dev-connector; do
  if test ! -f "$output"/$src/metadata.xml; then
    echo "$(tput setaf 1)Failed to generate metadata$(tput sgr0)"
    exit 1
  fi
  
  # sign
  XMLSECTOOL="xmlsectool"
  if test -z `which xmlsectool`; then
      if [ "$(uname)" == "Darwin" ]; then
          echo "$(tput setaf 3)Detected macOS - installing xmlsectool via brew$(tput sgr0)"
          brew install xmlsectool
      else
          echo "$(tput setaf 3)Detected a host OS that is not macOS - installing xmlsectool manually$(tput sgr0)"
          if [ ! -f xmlsectool-2.0.0-bin.zip ]; then
              set -e
              curl -o xmlsectool-2.0.0-bin.zip http://shibboleth.net/downloads/tools/xmlsectool/latest/xmlsectool-2.0.0-bin.zip >/dev/null 2>/dev/null
              echo "9169b27479d9d8c4fcbf31434cb1567c  xmlsectool-2.0.0-bin.zip" > xmlsectool-2.0.0-bin.zip.md5
              md5sum -c xmlsectool-2.0.0-bin.zip.md5
              unzip -n xmlsectool-2.0.0-bin.zip >/dev/null 2>/dev/null
          fi
          XMLSECTOOL="xmlsectool-2.0.0/xmlsectool.sh"
      fi
  fi
  
  echo "$(tput setaf 3)Signing metadata$(tput sgr0)"
  JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64/" $XMLSECTOOL \
    --sign \
    --inFile "$output"/$src/metadata.xml \
    --outFile "$output"/$src/metadata.signed.xml \
    --certificate "$certdir"/metadata_signing_a.crt \
    --key "$certdir"/metadata_signing_a.pk8 \
    --digest SHA-256

  cp metadata/output/$src/metadata.signed.xml metadata/$src.xml
done
