$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$jdkLocal = Join-Path $env:USERPROFILE 'Documents\Codex\tools\jdk-21.0.13+9'
if (-not $env:JAVA_HOME -and (Test-Path "$jdkLocal\bin\java.exe")) { $env:JAVA_HOME = $jdkLocal }
if ($env:JAVA_HOME) { $env:Path = "$env:JAVA_HOME\bin;" + $env:Path }
$dockerBin = 'C:\Program Files\Docker\Docker\resources\bin'
if (Test-Path $dockerBin) { $env:Path = "$dockerBin;" + $env:Path }
# Evita gravar caches fora do workspace e facilita reproduzir o ambiente.
$env:MAVEN_USER_HOME = Join-Path (Get-Location) '.cache\maven'
$env:TEMP = Join-Path (Get-Location) '.cache\tmp'
$env:TMP = $env:TEMP
New-Item -ItemType Directory -Force -Path $env:TEMP | Out-Null
if (Test-Path '.env') {
    foreach ($line in Get-Content '.env') {
        if ($line -match '^DB_PASSWORD=(.+)$') { $env:DB_PASSWORD = $Matches[1] }
    }
}
