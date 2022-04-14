REM
REM Compile izpack 4.3 SAGE
REM 

REM We need JAVA 1.8 : wont compile with JAVA > 1.8
set JAVA_HOME=C:\Program Files\java\jdk-8.0.302

cd src
ant -file build.xml