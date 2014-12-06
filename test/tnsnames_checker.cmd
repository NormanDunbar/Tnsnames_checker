@echo off

REM ----------------------------------------------------------------------
REM Run the tnsnames_checker Java code to validate a file passed on stdin.
REM ----------------------------------------------------------------------
REM For example, errors to the screen:
REM
REM 	tnsnames_checker standard.tnsnames.ora 
REM ----------------------------------------------------------------------
REM Redirecting error messages to a file:
REM
REM 	tnsnames_checker standard.tnsnames.ora 2>errors.log
REM ----------------------------------------------------------------------
REM Redirecting everything to a file:
REM
REM 	tnsnames_checker standard.tnsnames.ora >all_output.log 2>&1
REM ----------------------------------------------------------------------
REM Have fun. (But  that's not mandatory!)
REM Norman Dunbar, August 2014.
REM ----------------------------------------------------------------------


@java -cp .\tnsnames_checker.jar;.\antlr-4.4-complete.jar  tnsnames_checker %*