#!/bin/bash
set -Eeuo pipefail

domain=local-esc-bs.localhost
cert_alias="${domain}"

project_root=$(dirname "$0")/../..
certs_dir="${project_root}/developer-local-settings/config/certs"

function exit_error {
	local msg="$*"
	echo "ERROR: ${msg}" >&2
	exit 1
}

which mkcert || exit_error "Please install mkcert and run 'mkcert -install' (see: https://github.com/FiloSottile/mkcert)"

if [ ! -d "${certs_dir}" ]; then
	mkdir -p "${certs_dir}"
fi

cert_file="${certs_dir}/${domain}.pem"
key_file="${certs_dir}/${domain}.key"
pkcs12_file="${certs_dir}/${domain}.p12"

echo "Generating the keypair. This might take a while..."
# Also generate for localhost/127.0.0.1 to make Quarkus Dev-UI work properly using https://localhost:8443/q/dev-ui
mkcert \
	-cert-file "${cert_file}" \
	-key-file "${key_file}" \
	-ecdsa \
	"${domain}" \
	localhost \
	127.0.0.1

# change permissions since we will mount the key into keycloak and the user will not match
chmod go+r key_file

echo "Importing into local java truststore: ${pkcs12_file}"
if [ -f "${pkcs12_file}" ]; then
	echo "Removing old trusted cert: ${cert_alias}"
	keytool -delete \
		-noprompt \
		-v \
		-alias "${cert_alias}" \
		-keystore "${pkcs12_file}" \
		-storetype PKCS12 \
		-storepass changeit \
		1>/dev/null 2>&1 \
		|| true
fi

echo "Adding new trusted cert: ${cert_alias}"
keytool -importcert \
    -noprompt \
    -v \
    -trustcacerts \
    -alias "${cert_alias}" \
    -keystore "${pkcs12_file}" \
    -storetype PKCS12 \
    -storepass changeit \
    -file "${cert_file}"
