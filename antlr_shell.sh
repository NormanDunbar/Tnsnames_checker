# Change this to suit your system:
export JAVA_HOME='/opt/java6'

# You need to set this to the location of the ANTLR4 jar file.
export CLASSPATH="./antlr-4.4-complete.jar"

# Alias to compile an ANTLR4 grammar.
alias antlr4='java org.antlr.v4.Tool'

# Alias to test run a parser or parser rule.
alias grun='java org.antlr.v4.runtime.misc.TestRig'

# Aliases to jar, run and compile Java files.
alias jar='${JAVA_HOME}/bin/jar'
alias java='${JAVA_HOME}/bin/java'
alias javac='${JAVA_HOME}/bin/javac'
