#!/usr/bin/env bash
cd "$(dirname "$0")"

if [ -z "$JAVA_HOME" ]; then
  JDK=$(ls -d tools/jdk-* 2>/dev/null | head -1)
  [ -n "$JDK" ] && export JAVA_HOME="$JDK" && export PATH="$JAVA_HOME/bin:$PATH"
fi

command -v javac >/dev/null || { echo "Java JDK not found. Put a JDK in tools/ or install one."; exit 1; }

find src -name '*.java' > sources.txt
javac -encoding UTF-8 -d bin @sources.txt || exit 1
java -cp bin game.Main "$@"