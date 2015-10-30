# Change this to suit your system:
if [ -z "${JAVA_HOME}" ]
then
	export JAVA_HOME='/opt/java6'
fi

# You need to set this to the location of your ANTLR4 jar file.
ANTLR4="/home/norman/bin/antlr-4.4-complete.jar"
if [ ! -f "${ANTLR4}" ]
then
	echo "Cannot locate file \""${ANTLR4}"\""
	exit 1
fi
export CLASSPATH=".:${CLASSPATH}:${ANTLR4}"

# Alias to compile an ANTLR4 grammar.
alias antlr4='java org.antlr.v4.Tool'

# Alias to test run a parser or parser rule.
alias grun='java org.antlr.v4.runtime.misc.TestRig'

# Aliases to jar, run and compile Java files.
alias jar='${JAVA_HOME}/bin/jar'
alias java='${JAVA_HOME}/bin/java'
alias javac='${JAVA_HOME}/bin/javac'
