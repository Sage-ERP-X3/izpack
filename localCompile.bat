REM
REM Compile izpack 4.3 SAGE
REM 

REM We need JAVA 1.8 : wont compile with JAVA > 1.8
REM set JAVA_HOME=C:\Program Files\java\jdk-8.0.302
REM 
REM IzPack 4.3.11: has been migrated to Java 11
set JAVA_HOME=C:\Program Files\Zulu\zulu-11

ant -file src\build.xml all