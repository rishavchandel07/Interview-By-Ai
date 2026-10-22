#!/bin/sh
# Mac / Linux: compile and run
mkdir -p out
javac -d out -sourcepath src src/Main.java && java -cp out Main
