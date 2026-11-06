$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if ($scriptDir) { Set-Location $scriptDir } else { Set-Location "C:\Users\routh\Desktop\java_project" }

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User") + ";C:\Users\routh\tools\apache-maven-3.9.9\bin"
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Current Directory: $(Get-Location)" -ForegroundColor Yellow
Write-Host "  Launching CineFlow - Multiplex Ticket System...       " -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan
mvn javafx:run
