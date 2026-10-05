#!/usr/bin/env sh
# Runs the self-test against a throwaway database (your real save is not touched).
cd "$(dirname "$0")" || exit 1
SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
mkdir -p out
javac -J-Xmx192m -encoding UTF-8 -d out -cp sqlite-jdbc-3.53.2.1.jar *.java tests/SelfTest.java || exit 1
exec java -Xmx128m -XX:ReservedCodeCacheSize=48m --enable-native-access=ALL-UNNAMED -ea -Drpg.db=out/selftest.db -cp "out${SEP}sqlite-jdbc-3.53.2.1.jar" SelfTest
