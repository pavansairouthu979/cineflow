@echo off
title CineFlow - Movie Ticket Reservation System
cd /d "%~dp0"
set "PATH=%PATH%;C:\Users\routh\tools\apache-maven-3.9.9\bin"
echo ========================================================
echo   Launching CineFlow - Multiplex Ticket System...
echo ========================================================
call mvn javafx:run
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error.
    pause
)

