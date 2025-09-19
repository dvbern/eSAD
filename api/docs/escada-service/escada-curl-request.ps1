# Best practice:
# Copy File and rename to "prepared-escada-curl-request(.sh/.ps1)" so it's ignored by git
# Copy File second time and rename to "prepared-escada-curl-request-dev(.sh/.ps1)" to save the import for dev
# Change Client Secret and Password to according values
# Potentially change testdata.csv to prefered file

# Define Keycloak parameters
$KEYCLOAK_URL="https://local-esc-bs.localhost:8989"
$REALM="bs"
$CLIENT_ID="escada-client"
$CLIENT_SECRET="myClientSecret"
$USERNAME="escada-client@dvbern.ch"
$PASSWORD="myUserPassword"

# Define API parameters, specify path to  file you want to import
$API_SERVER_URL = "https://local-esc-bs.localhost:8443"
$FILE_TO_SEND = "./testdata.csv"

# Get access token using Direct Access Grant
$tokenResponse = Invoke-RestMethod -Method Post -Uri "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{
        client_id     = $CLIENT_ID
        client_secret = $CLIENT_SECRET
        grant_type    = "password"
        username      = $USERNAME
        password      = $PASSWORD
    }

$ACCESS_TOKEN = $tokenResponse.access_token

# Output token
Write-Host "`n-------------"
Write-Host "Token to pass in Authorization Header:"
Write-Host "Token: $ACCESS_TOKEN"

# Send file with Authorization Header
$response = Invoke-WebRequest -Uri "$API_SERVER_URL/api/escada" `
    -Method Post `
    -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } `
    -ContentType "multipart/form-data" `
    -Form @{ file = Get-Item $FILE_TO_SEND }

# Output response
$response.StatusCode
$response.StatusDescription
$response.Content
