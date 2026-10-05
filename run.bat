@echo off
rem Compiles and starts Mystic Arena. Requires JDK 17+.
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
javac -J-Xmx192m -encoding UTF-8 -d out -cp sqlite-jdbc-3.53.2.1.jar *.java || exit /b 1
java -Xmx128m -XX:ReservedCodeCacheSize=48m --enable-native-access=ALL-UNNAMED -cp "out;sqlite-jdbc-3.53.2.1.jar" Main
