# izpack
Wiki: <a href="https://izpack.atlassian.net/wiki/spaces/IZPACK/pages/491528/IzPack+5"> Izpack documentation </a><br/>


Were're based on the original IzPack 5.1.3 binaries. <br/>
Note that we have NOT modified any source in this version. <br/>
Personnalization has exclusively been made in the library named `"com.sage.izpack.jar"` placed in directory ".\izPackCustomActions". <br/>
<br/>

Official documentation izPack 5:<br/>
https://izpack.atlassian.net/wiki/spaces/IZPACK/pages/491528/IzPack+5<br/>

<br/>

## Version 5.1.3.1: 2021-10

- update to IZPACK 5.1.3 to support `adxInstall.xml` management. <br/>
  This version has been introduced for X3 PrintServer 2.25 <br/>

- Add management of PowerShell scripts for "beforepacks" actions
  Can add resource file "BeforeUpdateScriptPs_windows"
  Add variable `BEFORE_UPDATE_SCRIPT_PS_PATH` and `BEFORE_UPDATE_SCRIPT_PS_PATH`

```xml
    <res id="BeforeUpdateScriptPs_unix" src="updatescripts/beforepacks.ps1" />
    <res id="BeforeUpdateScriptPs_windows" src="updatescripts/beforepacks.ps1" />
```


### Misc


### Bug fix


## Version 5.1.3: 2021-01

- update to IZPACK 5.1.3 <br/>
  This version has been introduced for X3 Console Manage 2.51. <br/>




<br/>
