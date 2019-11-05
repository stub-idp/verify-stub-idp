#!/usr/bin/env ruby

require 'yaml'

def block_cert(cert)
  IO.readlines(cert).map(&:strip).reject(&:empty?)[1 ... -1].join("\n")
end

def inline_cert(cert)
  IO.readlines(cert).map(&:strip).reject(&:empty?)[1 ... -1].join
end

if ARGV.size < 3
  puts "Usage: connector-metadata-sources.rb hub_signing_cert hub_encryption_cert output_dir"
  exit 1
end

hub_signing_cert = ARGV[0]
hub_encryption_cert = ARGV[1]
output_dir = ARGV[2]

hub_yaml = {
  'id' => 'VERIFY-HUB',
  'entity_id' => "http://#{ENV.fetch('EXTERNAL_HOST')}:40000/stub/sp/SAML2/metadata/federation",
  'assertion_consumer_service_uri' => "http://#{ENV.fetch('EXTERNAL_HOST')}:40000/stub/sp/SAML2/SSO/eIDAS/Response",
  'organization' => { 'name' => 'Stub Sp Connector', 'url' => 'http://localhost', 'display_name' => 'Stub Sp Connector' },
  'signing_certificates' => [
    { 'name' => 'signing_primary', 'x509' => block_cert(hub_signing_cert) }
  ],
  'encryption_certificate' => { 'name' => 'encryption', 'x509' => block_cert(hub_encryption_cert) }
}

Dir::chdir(output_dir) do
  File.open('hub.yml', 'w') { |f| f.write(YAML.dump(hub_yaml)) }
end
