# izpack
Wiki: <a href="https://izpack.atlassian.net/wiki/spaces/IZPACK/pages/491528/IzPack+5"> Izpack documentation </a><br/>


Were're based on the original IzPack 5.1.3 binaries. <br/>
Note that we have NOT modified any source in this version. <br/>
Personnalization has exclusively been made in the library named `"com.sage.izpack.jar"` placed in directory ".\izPackCustomActions". <br/>
<br/>

Official documentation izPack 5:<br/>
https://izpack.atlassian.net/wiki/spaces/IZPACK/pages/491528/IzPack+5<br/>

<br/>

# Version 5.2.0

## Version 5.2.0.4: 2023-11

- Implement com.sage.izpack.jar to support X3 AdxAdmin & Runtime and IzPack 5


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





### Misc


### Bug fix


## Version 5.1.3: 2021-01

- update to IZPACK 5.1.3 <br/>
  This version has been introduced for X3 Console Manage 2.51. <br/>



# Version 4.3

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
