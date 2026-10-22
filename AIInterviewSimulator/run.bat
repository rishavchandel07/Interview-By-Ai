@echo off
REM Windows: compile and run
if not exist out mkdir out
javac -d out -sourcepath src src\Main.java
if %errorlevel% neq 0 pause & exit /b
java -cp out Main
