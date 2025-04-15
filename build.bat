@echo off
set "JAVA_FX_SDK=C:\javafx-sdk-21"
set "INPUT_DIR=C:\WORK\Pomodoro\Pomodoro\out\artifacts\Pomodoro_jar"
set "OUTPUT_DIR=C:\WORK\Pomodoro\"
set "MAIN_JAR=Pomodoro.jar"
set "MAIN_CLASS=com.buchta.pomodoro.Application"


echo Using main jar: %MAIN_JAR%
echo Using main class: %MAIN_CLASS%

jpackage --input "%INPUT_DIR%" ^
  --name "Pomodoro" ^
  --main-jar "%INPUT_DIR%\%MAIN_JAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --type app-image ^
  --dest "%OUTPUT_DIR%" ^
  --module-path "%JAVA_FX_SDK%\lib" ^
  --add-modules javafx.controls,javafx.fxml ^
  --icon "%SystemRoot%\System32\shell32.dll" ^
  --win-console

pause