# IzPack
=============

## Version 4.3.11 : 15.05.2024

- X3-311594: Compliancy with Java 11 - Migration of installers: Support Java 11

- X3-309977: update driver mongodb-driver-core-5.1.0.jar (previous: mongo-java-driver-3.4.2.jar)
Source: https://repo.maven.apache.org/maven2/org/mongodb/mongodb-driver-core/5.1.0/


## Version 4.3.10 : 15.02.2022

- X3-285099: Elastic Search as optional in Syracuse
- X3-270151: inability to configure the  WebServer 2.40.0.1 with a response file.<br>
java.lang.Exception: ERROR: Unable to set the final field [SimpleFormatter.format]
        at com.izforge.izpack.util.sage.CLoggerUtils.setPrivateStaticFinalString(CLoggerUtils.java:240)


## Version 4.3.9 : 05.11.2021

* X3-250275: move prerequisites resources within the project
  Fix WinRegistry Java11API

* X3-264457: Wrong phrasing in last phase of IZpack installers
  Former wording: "Automatic installation script"
  New wording: "Save response file"

## Version 4.3.8 : 01.08.2020

* X3-233874: Fix hostname redhat 8
* X3-235605: Fix hostname
* X3-220056: Create p12 from pem
* X3-183609:Safe X3 V2 Console IZpack installer does not save option for shortcuts location in script file
* X3-125503: AdxAdmin uninstallation cryptic error message
  Add missing resource "uninstaller.adxadmin.remainingmodules"
* X3-203051: Spelling
* X3-196435: Handle mongodb.ssl.pemkeypassword,  Update mongodb driver to 3.12.5,  Add dnsName


## Version 4.3.7 : 

* Update CreateCertsValidator.java
* X3-182904 : Error messages when using scripted install of Safe X3 V2 Print Server on blank machine
* X3-146220 - Elastic Search 6.4 validator
* Fix Registry read value DWord type
  Add WinRegistry.java  and new Instruction <variable value="regkey[HKLM:RegistryPath:Key]">

## Version 4.3.6

* X3-129592 - Delete "!" after version number FR
* X3-129592 - Delete "!" after version number
* X3-120130 - Use openjdk 8, Ant 1.9
* X3-93962 - Open license link
* nodedbhome and nodedblink no more reset nor hardcoded in update mode - managed by console

Community
=========

IzPack is part of the Codehaus <http://codehaus.org/>

* Web site: <http://izpack.org/>
* Confluence wiki: <http://docs.codehaus.org/display/IZPACK>
* News feed: <http://feeds.feedburner.com/IzPack>
* Subversion repository: <http://svn.codehaus.org/izpack/>
* Git repository (synchronized from svn): <http://github.com/jponge/izpack/tree/master>
* JIRA issues tracker: <http://jira.codehaus.org/browse/IZPACK>
