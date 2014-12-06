
REM Change the next few lines to suit your system.
set JAVA_HOME="c:\program files\java\jdk1.7.0_60"
set PATH=%PATH%;%JAVA_HOME%\bin;c:\software\antlr
set CLASSPATH=".;c:\software\antlr\antlr-4.4-complete.jar;%CLASSPATH%"
c:
cd \software\antlr

cls
@echo "Use antlr4 <grammar_file> to compile the grammar and generate Java code."
@echo "use grun Grammar_name top_rule [-tree -gui] to test the gramnmer on stdin."
@echo "Use CTRL-Z to signal EOF on stdin!"
@echo " "
