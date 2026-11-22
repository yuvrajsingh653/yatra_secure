Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$configPath = Join-Path $PSScriptRoot 'application-local.properties'
if (Test-Path -LiteralPath $configPath) {
    Write-Host 'Local configuration already exists; keeping its signing key and credentials.'
    return
}

function Read-OptionalSecret([string]$Prompt) {
    $secure = Read-Host $Prompt -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
function Escape-Property([string]$Value) {
    return $Value.Replace('\', '\\').Replace("`r", '\r').Replace("`n", '\n').Replace(':', '\:').Replace('=', '\=').Replace('#', '\#').Replace('!', '\!')
}

$randomBytes = New-Object byte[] 32
$randomGenerator = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $randomGenerator.GetBytes($randomBytes) } finally { $randomGenerator.Dispose() }
$key = [Convert]::ToBase64String($randomBytes)
$dbUser = Read-Host 'Local MySQL username [root]'
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = 'root' }
$dbPassword = Read-OptionalSecret 'Local MySQL password (press Enter if none)'
$adminEmail = Read-Host 'Optional admin bootstrap email (press Enter to skip)'
$adminPassword = ''
if (-not [string]::IsNullOrWhiteSpace($adminEmail)) {
    $adminPassword = Read-OptionalSecret 'Admin bootstrap password'
    if ([string]::IsNullOrWhiteSpace($adminPassword)) { throw 'Admin password cannot be empty when an admin email is set.' }
}
$lanAddress = Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway -and $_.NetAdapter.Status -eq 'Up' } | ForEach-Object { $_.IPv4Address.IPAddress } | Where-Object { $_ -and $_ -notlike '169.254.*' } | Select-Object -First 1
if ([string]::IsNullOrWhiteSpace($lanAddress)) { $lanAddress = Read-Host 'Laptop Wi-Fi IPv4 address for QR links (find it with ipconfig)' }
if ([string]::IsNullOrWhiteSpace($lanAddress)) { throw 'A public application base URL is required. Set PUBLIC_APP_BASE_URL or provide the laptop LAN IPv4 address.' }

$lines = @(
    '# Local only. This file is ignored by Git; do not copy it to production.'
    "jwt.secret=$(Escape-Property $key)"
    "spring.datasource.username=$(Escape-Property $dbUser)"
    "spring.datasource.password=$(Escape-Property $dbPassword)"
    "app.admin.email=$(Escape-Property $adminEmail)"
    "app.admin.password=$(Escape-Property $adminPassword)"
    "public.app.base-url=http://$lanAddress`:5173"
)
[System.IO.File]::WriteAllLines($configPath, $lines, (New-Object System.Text.UTF8Encoding($false)))
Write-Host 'Created ignored application-local.properties with a persistent random JWT key.'
