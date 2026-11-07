. "$PSScriptRoot\ambiente.ps1"
if (-not (Test-Path '.env')) {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $password = [Convert]::ToBase64String($bytes)
    Set-Content -Path '.env' -Value "DB_PASSWORD=$password" -Encoding ascii
    $env:DB_PASSWORD = $password
}
docker compose up -d --wait db
if ($LASTEXITCODE -ne 0) { throw 'O PostgreSQL não iniciou. Confira se o Docker Desktop está aberto.' }
Write-Host 'PostgreSQL pronto em localhost:5433. Os dados ficam no volume postgres_data.'
