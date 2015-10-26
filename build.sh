# Assumes that the antlr_shell.sh script has been sourced prior
# to calling this build file:
#
# . ./antlr_shell.sh
#

echo " "
echo "Compiling grammar ..."
java org.antlr.v4.Tool tnsnames.g4

echo "Compiling java ..."
javac *.java

echo "Creating jar ..."
jar -cvf tnsnames_checker.jar *.class > /dev/null

echo "Cleaning up ..."
rm *.class

echo "Done."
echo " "
