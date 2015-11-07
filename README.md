# Tnsnames_checker

Validate and syntax check your tnsnames.ora file with tnsnames_checker. This utility will perform the following checks on your tnsnames.ora file and report anything it doesn't like.

The checks carried out are:

  - Missing or invalid characters;
  - Syntax errors;
  - Semantic errors;
  - Redefinition of parameters;
  - IPv4 host address validation;
  - Port number validation;
  - Duplicate entries;
  - IFILE;
  - Listener only entries;
  - SDU parameter in range;
  - Missing DESCRIPTION_LISTs;
  - Missing ADDRESS_LISTs;

## File List

  - LICENSE - The MIT License file. Basically, you do what you like!
  - README.md - This file in Markdown format.
  - README.html - This file, in HTML format.


### Windows Only Files

  - antlr4.bat - A batch file to compile a *.g4 file into various Java classes, lexers and parsers.
  - grun.bat - A file to test run parser rules. Read the ANTLR4 docs for details on the [ANTLR][antlr] website. You might need to use this in debugging your grammar.
  - build.bat - A batch file to compile the grammar, compile the Java files, build a jarfile (in the test subdirectory) and clean up after itself. 
  - antlr_shell.cmd - A batch file to set the Java and ALTLR4 environment. Run this first, before you try to do anything. You will need to edit the various folder names to match your system before doing anything though!
  - test\tnsnames_checker.cmd - a batch file to run the generated checker application.

### Linux/Unix Only Files

  - antlr_shell.sh - You need to "source" this file to set up the ANTLR4 environment *before* attempting to build anything. Edit this to set your own JAVA_HOME and CLASSPATH before use. 
  - build.sh - Carries out a full compile and build from the grammar onwards. Creates a jar file in the current (source) direcory.
  - test/tnsnames_checker.sh - a script to run the generated checker application. Uses the CLASSPATH set in antlr_shell.sh and the newly built tnsnames_checker.jar file.

### Something for Everyone!

  - tnsnames.g4 - this is the ANTLR4 grammar that defines the structure and semantics of a tnsnames.ora file as per the Oracle docs for 11gR2. However, it may be valid for previous, and subsequent versions too.
  - LineNumber.java - A Java class to get around a *serious bug* in Java. You cannot swap two integers, in a function or class method, unless you wrap them in a class with a getter and setter. Sigh! (Don't argue with me, it's a bug, right!!!)
  - tnsnames_checker.java - This is the small, but almost perfectly formed Java class that kicks off and controls everything when the final build is complete.
  - tnsnamesInterfaceListener.java - This class extends one of the Java classes created by the ANTLR4 build process and carries out all the checks listed above on the parse tree created by parsing your tnsnames.ora file.
  - tnsnames.test.ora - Probably the world's worst tnsnames.ora file. Use it to test your version when you have compiled and built it. It is full of errors that should exercise all code paths through the parser and checker.


## The Build

### Dependencies
  - Java 1.6 (aka Java 6) minimum. Has been built and tested with Java 6 and Java 7.
  - antlr-4.4-complete.jar - downloaded from the [ANTLR][antlr] Web site. There may be a later version.


### Windows
  - Open a cmd window.
  - cd to wherever the source is to be found.
  - Execute the antlr_shell program.
  - Execute the build program. A tnsnames_checker.jar file will be created in the "test" folder.
  - Test - see below.

### Linux and Unix.
  - Open a shell session.
  - cd to wherever the source lives.
  - . ./antlr_shell.sh to set the aliases and the environment for ANTLR4.
  - ./build.sh to perform a full build. A tnsnames_checker.jar will be created in the current directory.
  - Test. See below.

## Testing

From the build directory:

  - cd test
  - On Windows, execute the tnsnames_checker.cmd passing parameters as described below. You might need to amend this file to correct the path to the ANTLR4 jar file(s).
  - On Linux and Unix, execute the tnsnames_checker.sh script passing parameters as described below.

### Parameters
Output from the checker is passed to both stdout and stderr. General messages and flagrant *blowing my own trumpet* messages go to stdout, while warnings and errors go to stderr. You can either ignore this and catch everything on screen, in a file, or redirect the individual channels to different files, as per the examples below.

**Note**: if you wish to use a pretty mangled tnsnames file for testing, there's one in the source directory named tnsnames.test.ora. Simply replace "tnsnames.ora" in the examples below with "../tnsnames.test.ora" if, as expected, you are running the test from the test directory.

#### Everything to the Screen

Windows:
```
tnsnames_checker tnsnames.ora
```

Linux/Unix:

```
./tnsnames_checker.sh tnsnames.ora
```

#### Everyting to a single file

Windows:

```
tnsnames_checker tnsnames.ora >tnsnames.log 2>&1
```

Linux/Unix:
```
./tnsnames_checker.sh tnsnames.ora >tnsnames.log 2>&1
```


#### Split over Two Files

Windows:
```
tnsnames_checker tnsnames.ora >tnsnames.log 2>tnsnames.errors.log
```

Linux/Unix:
```
./tnsnames_checker.sh tnsnames.ora >tnsnames.log 2>tnsnames.errors.log
```



[antlr]: http://www.antlr.org
