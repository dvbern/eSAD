# Best practice:
# Copy File and rename to "prepared-file-ingress-curl-request(.sh/.ps1)" so it's ignored by git
# Copy File second time and rename to "prepared-file-ingress-curl-request-dev(.sh/.ps1)" to save the import for dev
# Change Client Secret and Password to according values
# Potentially change 2010-11-25__A01300025.pdf to prefered file
# Client -> ClientScopes -> add ESC-FACHROLLE scope, damit der tenant gemappt wird

# Define Keycloak parameters
$KEYCLOAK_URL="https://dev-auth-esc.apps.mercury.ocp.dvbern.ch"
$REALM="bs"
$CLIENT_ID="file-ingress-client"
$CLIENT_SECRET="<CLIENT_SECRET>"
$USERNAME="file-ingress-client@dvbern.ch"
$PASSWORD="<PASSWORD>"

# Define API parameters, specify path to file you want to import
$API_SERVER_URL = "https://dev-api-esc-bs.apps.mercury.ocp.dvbern.ch"
$FILE_TO_SEND = "./2010-11-25__A01300025.pdf"
$ESAD_ID = "A01300025"
$ANLASS_ID = "724778e1-77ed-4b0d-95c6-0b0a95e099b6"
$NOTIZ_TYP = "IMPFAUSWEIS"

# Fetch token
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

# --- BEGIN: Build multipart request with explicit application/pdf content type ---
Add-Type -AssemblyName System.Net.Http

$httpClient = [System.Net.Http.HttpClient]::new()
$httpClient.DefaultRequestHeaders.Authorization =
    [System.Net.Http.Headers.AuthenticationHeaderValue]::new("Bearer", $ACCESS_TOKEN)

$multipart = [System.Net.Http.MultipartFormDataContent]::new()

$fileBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $FILE_TO_SEND))
$fileContent = [System.Net.Http.ByteArrayContent]::new($fileBytes)
$fileContent.Headers.ContentType =
    [System.Net.Http.Headers.MediaTypeHeaderValue]::new("application/pdf")

$multipart.Add($fileContent, "file", [System.IO.Path]::GetFileName($FILE_TO_SEND))

# Send file with Authorization Header
$response = $httpClient.PostAsync(
    "$API_SERVER_URL/api/file-ingress/dossier/${ESAD_ID}/anlass/${ANLASS_ID}/${NOTIZ_TYP}",
    $multipart
).Result
# --- END: Build multipart request with explicit application/pdf content type ---

# Output response
$statusCode = [int]$response.StatusCode
$statusDescription = $response.StatusCode
$content = $response.Content.ReadAsStringAsync().Result

Write-Host "`n-------------"
Write-Host "Status: $statusCode $statusDescription"
Write-Host "Response: $content"

$httpClient.Dispose()
