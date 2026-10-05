#!/usr/bin/env sh
# Compiles and starts Mystic Arena. Requires JDK 17+.
cd "$(dirname "$0")" || exit 1
SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
mkdir -p out
javac -J-Xmx192m -encoding UTF-8 -d out -cp sqlite-jdbc-3.53.2.1.jar *.java || exit 1
exec java -Xmx128m -XX:ReservedCodeCacheSize=48m --enable-native-access=ALL-UNNAMED -cp "out${SEP}sqlite-jdbc-3.53.2.1.jar" Main
