call antlr4 tnsnames.g4
javac *.java

jar -cvf test\tnsnames_checker.jar *.class > nul

del *.class