# izpack
Wiki: <a href="https://izpack.atlassian.net/wiki/spaces/IZPACK/pages/491528/IzPack+5"> Izpack documentation </a><br/>


Were're based on the original IzPack 5.2.0 binaries. <br/>
Note that we have NOT modified any source in this version. <br/>
Personnalization has exclusively been made in the library named `"com.sage.izpack.jar"` placed in directory "./izPackCustomActions". <br/>
<br/>

Official documentation izPack 5:<br/>
https://izpack.atlassian.net/wiki/spaces/IZPACK/pages/491528/IzPack+5<br/>

<br/>

# Version 5.2.1


## Version 5.2.2.0: 2024-10

- Include APF within IzPack


## Version 5.2.1.1: 2024-09

- X3-293749: Migration of Syracuse, MongoDb to IzPack 5

- X3-309977:  mongo-java-driver-3.12.5.jar to support MongoDb 7.0 (previous: mongo-java-driver-3.4.2.jar) <br>
Add driver mongodb-driver-core-5.1.0.jar. <br>
Source: https://repo.maven.apache.org/maven2/org/mongodb/mongodb-driver-core/5.1.0/

- X3-293668: Migration of installers to IZPack 5 <br>
  Changes made during migration: https://confluence.sage.com/display/CLX3IZPACK/Changes+made+during+migration

<br>

## Version 5.2.1.0: 2024-05

- X3-311594 Migration of installers: Support Java 11

- X3-311594 : Upgrade from izpack 5.2.0 to the 5.2.1 release
  Source: Clone https://github.com/izpack/izpack.git and build project with Maven: `mvn verify install`


# Version 5.2.0

## Version 5.2.0.4: 2023-11

- X3-293748: Upgrade from izpack 5.2.0 M2 to the official 5.2.0 release  (http://izpack.org/downloads/)
- X3-293748: Implement com.sage.izpack.jar to support X3 AdxAdmin & Runtime and IzPack 5
- X3-302700: Print Server 2.29 installer displays message twice when updating

## Version 5.2.0.3: 2023-08

- X3-301654: [ ERROR: compFoundAskUpdate ]
- X3-287600 : Console update from 2.42.2.1  (so 19R3) to 2.56.0.19 (so 2022R4) doesn't detect the existing version

## Version 5.2.0.2: 2023-05

- X3-278420 : When updating print server to 2.26 with print-server-2.26.0.2-win.jar does not update record in Windows Programs and Features

## Version 5.2.0.1: 2022-01

- update to IZPACK 5.2.0 to fix uninstaller process. <br/>



# Version 5.1.3

## Version 5.1.3.1: 2021-10

- update to IZPACK 5.1.3 to support `adxInstall.xml` management. <br/>
  This version has been introduced for X3 PrintServer 2.25 <br/>

- Add management of PowerShell scripts for "beforepacks" actions
  Can add resource file "BeforeUpdateScriptPs_windows"
  Add variable `BEFORE_UPDATE_SCRIPT_PS_PATH` and `BEFORE_UPDATE_SCRIPT_PS_PATH`

Example beforepacks.cmd :
```xml
Powershell.exe -executionpolicy remotesigned -File  "${BEFORE_UPDATE_SCRIPT_PS_PATH}" -InstallPath  "${INSTALL_PATH}"
```

```xml
    <res id="BeforeUpdateScriptPs_unix" src="updatescripts/beforepacks.ps1" />
    <res id="BeforeUpdateScriptPs_windows" src="updatescripts/beforepacks.ps1" />
```


## Version 5.1.3: 2021-01

- update to IZPACK 5.1.3 <br/>
  This version has been introduced for X3 Console Manage 2.51. <br/>

<br>



# Version 4.3

## Version 4.3.11 : 15.05.2024

- X3-311594: Compliancy with Java 11 - Migration of installers: Support Java 11

- X3-309977:  mongo-java-driver-3.12.5.jar (previous: mongo-java-driver-3.4.2.jar)
Add driver mongodb-driver-core-5.1.0.jar. 
Source: https://repo.maven.apache.org/maven2/org/mongodb/mongodb-driver-core/5.1.0/


## Version 4.3.10 : 15.02.2022

- X3-285099: Elastic Search as optional in Syracuse
- X3-270151: inability to configure the  WebServer 2.40.0.1 with a response file.<br>
java.lang.Exception: ERROR: Unable to set the final field [SimpleFormatter.format]
        at com.izforge.izpack.util.sage.CLoggerUtils.setPrivateStaticFinalString(CLoggerUtils.java:240)


## Version 4.3.9 : 05.11.2021

* X3-250275 : move prerequisites resources within the project
  Fix WinRegistry Java11API

* X3-264457 : Wrong phrasing in last phase of IZpack installers
  Former wording: "Automatic installation script"
  New wording: "Save response file"


## Version 4.3.8 : 01.08.2020

* X3-233874 - Fix hostname redhat 8
* X3-235605 - Fix hostname
* X3-220056 - Create p12 from pem
* X3-183609:Safe X3 V2 Console IZpack installer does not save option for shortcuts location in script file
* X3-125503 : AdxAdmin uninstallation cryptic error message
  Add missing resource "uninstaller.adxadmin.remainingmodules"
* X3-203051 - Spelling
* X3-196435 - Handle mongodb.ssl.pemkeypassword,  Update mongodb driver to 3.12.5,  Add dnsName

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




<br/>
