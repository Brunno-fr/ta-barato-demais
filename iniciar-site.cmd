@echo off
cd /d "%~dp0"
echo Iniciando Ta barato Demais. Mantenha esta janela aberta.
echo Quando aparecer Started TabaratoDemaisApplication, abra http://localhost:8080
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\iniciar.ps1"
pause
