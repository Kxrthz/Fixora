param(
  [string]$ApiUrl = "http://localhost:8080",
  [string]$FrontendUrl = "http://localhost:5173"
)

$ErrorActionPreference = "Stop"

function Assert-Ok([string]$Name, [scriptblock]$Action) {
  try {
    & $Action
    Write-Host "PASS  $Name" -ForegroundColor Green
  } catch {
    Write-Host "FAIL  $Name" -ForegroundColor Red
    throw $_
  }
}

Write-Host "Testing Fixora..." -ForegroundColor Cyan

Assert-Ok "Frontend is reachable" {
  $page = Invoke-WebRequest -Uri $FrontendUrl
  if ($page.StatusCode -ne 200 -or $page.Content -notmatch "Fixora") { throw "Frontend did not return the Fixora app." }
}

Assert-Ok "Backend health is UP" {
  $health = Invoke-RestMethod -Uri "$ApiUrl/actuator/health"
  if ($health.status -ne "UP") { throw "Backend health is not UP." }
}

Assert-Ok "Public services API returns data" {
  $services = Invoke-RestMethod -Uri "$ApiUrl/api/v1/services"
  if ($services.Count -lt 1) { throw "No services were returned." }
}

$email = "smoke.$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())@fixora.test"
$password = "FixoraSmoke!2026"
$registerBody = @{ name = "Smoke Test"; email = $email; password = $password; role = "CUSTOMER" } | ConvertTo-Json

$auth = $null
Assert-Ok "Customer registration returns JWT tokens" {
  $auth = Invoke-RestMethod -Method Post -Uri "$ApiUrl/api/v1/auth/register" -ContentType "application/json" -Body $registerBody
  if ([string]::IsNullOrWhiteSpace($auth.accessToken) -or $auth.user.role -ne "CUSTOMER") { throw "Registration did not return a customer access token." }
}

$headers = @{ Authorization = "Bearer $($auth.accessToken)" }
Assert-Ok "Authenticated bookings endpoint is protected and available" {
  $bookings = Invoke-RestMethod -Uri "$ApiUrl/api/v1/bookings/me" -Headers $headers
  if ($null -eq $bookings) { throw "Bookings endpoint returned no response." }
}

Assert-Ok "Authenticated address endpoint is available" {
  $addresses = Invoke-RestMethod -Uri "$ApiUrl/api/v1/addresses" -Headers $headers
  if ($null -eq $addresses) { throw "Addresses endpoint returned no response." }
}

Write-Host "`nFixora smoke test passed." -ForegroundColor Green
