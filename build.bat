call antlr4 tnsnamesLexer.g4
call antlr4 tnsnamesParser.g4
javac *.java

jar -cvf test\tnsnames_checker.jar *.class > nul

del *.class
