#!/bin/sh
# Replace these values with your actual Keycloak configuration
KEYCLOAK_URL="https://uat-auth-esc.dvbern.ch"
REALM="bs"
CLIENT_ID="escada-client"
CLIENT_SECRET="myClientSecret"
USERNAME="escada-client@dvbern.ch"
PASSWORD="myUserPassword"

# Get token using direct access grant
curl -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "grant_type=password" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}"

# store token to var

# Using grep and cut
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

############################################################
#!/bin/sh

#Send data
# Replace these values with  the actual urls
API_SERVER_URL="https://uat-api-esc-bs.dvbern.ch"
FILE_TO_SEND="testdata.csv"

curl  -X 'POST' \
  "${API_SERVER_URL}/api/escada" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'accept: */*' \
  -H 'Content-Type: multipart/form-data' \
  -F "file=@${FILE_TO_SEND}"

