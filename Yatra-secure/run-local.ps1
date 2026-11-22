Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$configPath = Join-Path $PSScriptRoot 'application-local.properties'
if (-not (Test-Path -LiteralPath $configPath)) { & (Join-Path $PSScriptRoot 'setup-local.ps1') }
$hasPublicBaseUrl = Select-String -LiteralPath $configPath -Pattern '^\s*public\.app\.base-url\s*=' -Quiet
if (-not $hasPublicBaseUrl -and [string]::IsNullOrWhiteSpace($env:PUBLIC_APP_BASE_URL) -and [string]::IsNullOrWhiteSpace($env:FRONTEND_URL)) {
    $lanAddress = Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway -and $_.NetAdapter.Status -eq 'Up' } | ForEach-Object { $_.IPv4Address.IPAddress } | Where-Object { $_ -and $_ -notlike '169.254.*' } | Select-Object -First 1
    if ([string]::IsNullOrWhiteSpace($lanAddress)) { $lanAddress = Read-Host 'Laptop Wi-Fi IPv4 address for QR links (find it with ipconfig)' }
    if ([string]::IsNullOrWhiteSpace($lanAddress)) { throw 'Set PUBLIC_APP_BASE_URL or configure public.app.base-url in application-local.properties.' }
    Add-Content -LiteralPath $configPath -Value "public.app.base-url=http://$lanAddress`:5173"
}
Set-Location $PSScriptRoot
& .\mvnw.cmd spring-boot:run
exit $LASTEXITCODE
