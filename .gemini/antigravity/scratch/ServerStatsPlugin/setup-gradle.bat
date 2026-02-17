@echo off
setlocal

set GRADLE_VERSION=8.5

if not exist "%~dp0gradle\wrapper" (
    mkdir "%~dp0gradle\wrapper"
)

powershell -Command "& {Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%~dp0gradle-temp.zip'}"
powershell -Command "& {Expand-Archive -Path '%~dp0gradle-temp.zip' -DestinationPath '%~dp0gradle-temp' -Force}"
xcopy /E /I /Y "%~dp0gradle-temp\gradle-%GRADLE_VERSION%\*" "%~dp0gradle-wrapper-temp\"
del /Q "%~dp0gradle-temp.zip"
rmdir /S /Q "%~dp0gradle-temp"

echo Done! Now run: gradlew.bat clean shadowJar
