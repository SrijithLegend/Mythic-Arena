@echo off
rem Runs the self-test against a throwaway database (your real save is not touched).
cd /d "%~dp0"
if not exist out mkdir out
javac -J-Xmx192m -encoding UTF-8 -d out -cp sqlite-jdbc-3.53.2.1.jar *.java tests\SelfTest.java || exit /b 1
java -Xmx128m -XX:ReservedCodeCacheSize=48m --enable-native-access=ALL-UNNAMED -ea -Drpg.db=out\selftest.db -cp "out;sqlite-jdbc-3.53.2.1.jar" SelfTest
