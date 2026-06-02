#!/bin/sh
# Replace these values with your actual Keycloak configuration
KEYCLOAK_URL="https://dev-auth-esc.apps.mercury.ocp.dvbern.ch"
REALM="bs"
# Der Client braucht "Standard Flow" und "Direct Access Grants" aktiviert
# Client -> ClientScopes -> add ESC-FACHROLLE scope, damit der tenant gemappt wird
CLIENT_ID="file-ingress-client"
CLIENT_SECRET="<CHANGEME>"
# Benutzer braucht tenant = BS
USERNAME="file-ingress-client@dvbern.ch"
PASSWORD="<CHANGEME>"

API_SERVER_URL="https://dev-api-esc-bs.apps.mercury.ocp.dvbern.ch"
# Get token using direct access grant

ACCESS_TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "grant_type=password" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
# Verify token was extracted

echo  "\n-------------"
echo "token to pass in Atuhorization Header"
echo "Token: ${ACCESS_TOKEN}"
#
#
FILE_TO_SEND="./2010-02-02__A01300025.pdf"
ESAD_ID="A01300025"
ANLASS_ID="724778e1-77ed-4b0d-95c6-0b0a95e099b6"
NOTIZ_TYP="FRAGEBOGEN"

curl  -v -X 'POST' \
  "${API_SERVER_URL}/api/file-ingress/dossier/${ESAD_ID}/anlass/${ANLASS_ID}/${NOTIZ_TYP}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'accept: */*' \
  -H 'Content-Type: multipart/form-data' \
  -F "file=@${FILE_TO_SEND}"
