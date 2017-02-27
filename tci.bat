@echo off

setlocal
cd /d %~dp0 

set dmFile=pcornet_cdm_v3.xlsx
set appProps=application.properties
set fileToUpload = tciTemplate.xlsx
java -jar tci.jar -cfg %appProps% -dmfile %dmFile% %*
exit /b %errorlevel%
