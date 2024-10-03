#Requires -RunAsAdministrator
#Requires -Version 5.1
<#PSScriptInfo

    .VERSION 1.0
    .GUID bobfrdep-10a9-481d-a27d-188f4e0e1344
    .AUTHOR Sage X3 R&D
    .COMPANYNAME Sage
    .COPYRIGHT (c) Copyright SAGE 2006-2025. All Rights Reserved.
    .TAGS MS Windows Linux
    .EXTERNALMODULEDEPENDENCIES
    .REQUIREDSCRIPTS 
    .EXTERNALSCRIPTDEPENDENCIES Powershell-5.1 / PWSH 7.2
    .RELEASENOTES
    .PRIVATEDATA
    .DESCRIPTION 
#>   

param(
    [Parameter(Mandatory=$false)][string][ValidateSet('adxadmin', 'runtime')]$componentRequirement = 'runtime',
    [Parameter(Mandatory=$false)][switch]$preinstallation
)
enum FileTypes{
    exe
    msi
    zip
    nupkg
    PsScriptBlock
    ps1
}

enum ControlReturnCodes{
    ShouldInstall
    ShouldNotInstall
}
enum LogMessageType{
    Info
    Warning
    Error
    Debug
}
$PackageJsonStringSubstitutions = @{
    "{##LOCAL_DIRECTORY##}"                 = { Resolve-Path -Path $PSScriptRoot }
    "{##DEPENDENCY_DIRECTORY##}"            = { $gDependencyFolder }
    "{##EBIN_DIRECTORY##}"                  = { $gEbinFolder }
    "{##INSTANTCLIENT_DIRECTORY##}"         = { $gInstantClient }
    "{##TIMESTAMP   ##}"                    = { Get-Timestamp }
    "{##FORMATTED_DATE##}"                  = { Get-FormattedDate }
    "{##APFPrefix##}"                       = { "SageAPF" }
    "{##PACKAGE_PROVIDER_DIRECTORY##}"      = { Get-PsPackageInstallBasePath }
}

#region LogFunctions
function Initialize {
    $Error.Clear()
    Import-PackageProvider -Name NuGet -ErrorAction SilentlyContinue -WarningAction SilentlyContinue
    Set-Variable -Name "gOs"                        -Value (Get-OsType)                                                                                                 -Scope Global
    Set-Variable -Name "gkPackageJson"              -Value (Join-Path -Path $PSScriptRoot -ChildPath "../../../package.json")                                           -Scope Global
    Set-Variable -Name "gScriptName"                -Value (Get-Item $PSCommandPath).BaseName                                                                           -Scope Global
    Set-Variable -Name "gScriptFullPath"            -Value (Get-Item $PSCommandPath).FullName                                                                           -Scope Global
    Set-Variable -Name "gBaseDir"                   -Value (Resolve-Path -Path (Join-Path -Path $PSScriptRoot -ChildPath "/../../../"))                                 -Scope Global
    Set-Variable -Name "gStartDatetime"             -Value (Get-FormattedDate)                                                                                          -Scope Global
    Set-Variable -Name "gLogPrefix"                 -Value (Resolve-JsonStringSubstitutions -original_string "{##APFPrefix##}")                                         -Scope Global
    Set-Variable -Name "gDependencyFolder"          -Value (Resolve-Path -Path "$gBaseDir/dependencies")                                                                -Scope Global    
    Set-Variable -Name "gLogFileDir"                -Value $gDependencyFolder                                                                                           -Scope Global    
    Set-Variable -Name "gInstantClient"             -Value (Join-Path -Path $gBaseDir -ChildPath "instantclient")                                                       -Scope Global    
    Set-Variable -Name "gLogFullPath"               -Value (Join-Path -Path $gDependencyFolder -ChildPath "$($gLogPrefix)_$($gScriptName)_$($gStartDatetime).log")      -Scope Global
    Set-Variable -Name "gUser"                      -Value ([Environment]::UserName)                                                                                    -Scope Global
    Set-Variable -Name "gEbinFolder"                -Value (Resolve-Path -Path "$gBaseDir/ebin")                                                                        -Scope Global

    # Set up the logger as quickly as possible
    Set-Content -Path $gLogFullPath -Value "" -Encoding utf8
    Write-LogPreamble -logFullPath $gLogFullPath
    Add-Content -Value (Get-TitleHeader -repeatedCharacter "#" -totalLength (Get-TitleHeaderLength) -titleValue " Script Results: Start ") -Path $gLogFullPath
   
    Set-Variable -Name "gErrMsg"                    -Value $null                                                                                                        -Scope Global
    Set-Variable -Name "gScriptStackTrace"          -Value $null                                                                                                        -Scope Global
    Set-Variable -Name "gStopWatch"                 -Value ([System.Diagnostics.Stopwatch]::StartNew())                                                                 -Scope Global
    Set-Variable -Name "gSilentMode"                -Value $null                                                                                                        -Scope Global
    Set-Variable -Name "gRebootRequired"            -Value $false                                                                                                       -scope Global
    

    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "package.json read successfully from path: $(Resolve-Path -Path $gkPackageJson)." -LogMessageType Info    
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Initialize() complete." -LogMessageType Info    
}
function Get-FormattedDate(){
    <#
        .SYNOPSIS
            Date formating for Log trace
        .OUTPUTS
            Date in a specific Format, such as  2020_09_28_08_36_59_123
    #>
    return Get-Date -Format yyyy_MM_dd_HH_mm_ss_fff -ErrorAction Stop
}
function Get-Timestamp {
    <#
    .SYNOPSIS
    Return a date formatted
    .EXAMPLE
    2023-07-03T17.37.29.6735970+02.00
    #>
    return Get-Date -Format o | ForEach-Object { $_ -replace ":", "." }
}
function Write-TraceWithTimestamp {
    <#
    .SYNOPSIS
    Writes to a log file with a message preceeded by a timestamp. 
    #
        
    .PARAMETER logFullPath
    The path to the log
    
    .PARAMETER message
    The message to write
    
    .PARAMETER LogMessageType
    The type of message to write

    .PARAMETER ScribeRawData
    If this switch is used, line sizes are ignored and the raw data is written witout any type of reformatting

    .PARAMETER writeToHost
    Switch parameter that will also send the message to the stdout

    .NOTES
    The message will receive a simple split (in the middle of the word, as opposed to with word boundraies)  based on the length of the line. 
    #>
    param(
        [Parameter(Mandatory = $true)][string]$logFullPath,
        [Parameter(Mandatory = $true)][string]$message,
        [Parameter(Mandatory = $true)][LogMessageType]$logMessageType,
        [switch]$ScribeRawData,
        [switch]$writeToHost
    )
    
    $timestamp = Get-Timestamp
    $maxLineLengthWithTimeStamp = ((Get-TitleHeaderLength) - ($timestamp.ToString().length))
    $maxLineLength = (Get-TitleHeaderLength)
    $fullStampPrefix = "$($timestamp) - $($logMessageType.ToString())"

    if ($writeToHost){
        [string]$msgToWrite = "$($fullStampPrefix): $message"
        Write-Host $msgToWrite
    }

    if ($ScribeRawData){
        Add-Content -Path $gLogFullPath -Value $fullStampPrefix
        Add-Content -Path $gLogFullPath -Value $message
    }else{
        # Split the message every $maxLineLength characters
        $linesSize1 = $message -split "(.{$maxLineLengthWithTimeStamp})" | Where-Object { $_ }
        $linesSize2 = $message -split "(.{$maxLineLength})" | Where-Object { $_ }
        
        if ($linesSize1.Count -ge 2) {
            # Split lines based on max length
            Add-Content -Path $gLogFullPath -Value "$($fullStampPrefix):"
            $linesSize2 = $message -split "(.{$maxLineLength})" | Where-Object { $_ } | ForEach-Object {
                Add-Content -Path $gLogFullPath -Value $_ -ErrorAction Stop
            }      
        
        }
        elseif ($linesSize1.Count -eq 1) {
            # The message and timestamp go on the same line
            Add-Content -Path $gLogFullPath -Value "$($fullStampPrefix): $($linesSize1)" -ErrorAction Stop
        }
    }

}
function Write-LogPreamble {
    <#
    .SYNOPSIS
    Set up the log with a preamble
       
    .PARAMETER logFullPath
    Path to the log file
    #>
    param(
        [Parameter(Mandatory = $true)][string]$logFullPath
    )
    $tz = Get-TimeZone
    [string]$preamble = @"
$(Get-TitleHeader -RepeatedCharacter "#" -TotalLength (Get-TitleHeaderLength) -TitleValue " Script Preamble: Start ")
Author:                             Sage Software
Script Description:                 The Sage Automatic Prerequisite Framework is designed to read the package.json 
                                    within the dependencies folder, locate each dependency, along with a set of 
                                    associated rules, and then install that dependency based on the dependency type.
                                    
Script Path:                        $gScriptFullPath

Machine:                            $($env:COMPUTERNAME)
FQDN:                               $(Get-FQDN)
Timezone:                           $($tz.DisplayName)
Script Execution OS User:           $gUser
Log file:                           $gLogFullPath
Log Date:                           $((Get-Date))
Installation Type:                  $(if ($preinstallation){"Preinstallation"}else{"Standard Installation Type"})
$(Get-TitleHeader -repeatedCharacter "#" -totalLength (Get-TitleHeaderLength) -titleValue " Script Preamble: End ")

"@
    Add-Content -Value $preamble -Path $gLogFullPath

}
function Get-TitleHeader {
    param (
        [char]$repeatedCharacter,
        [int]$totalLength = (Get-TitleHeaderLength),
        [string]$titleValue
    )

    # Calculate the length of the repeated characters on both sides
    $repeatedLength = [Math]::Floor(($totalLength - $titleValue.Length) / 2)

    # Create the left and right parts of the header
    $leftPart = $repeatedCharacter.ToString() * $repeatedLength
    $rightPart = $repeatedCharacter.ToString() * $repeatedLength

    # Adjust the right part if total length is not a multiple of 2
    if (($totalLength - $titleValue.Length) % 2 -ne 0) {
        $rightPart += $repeatedCharacter
    }

    # Create the final title header string
    $header = $leftPart + $titleValue + $rightPart

        return $header
}
function Get-TitleHeaderLength {
    return 160
}
function Get-FQDN() {
    return $([System.Net.Dns]::GetHostByName($env:computerName)).HostName
}
function Build-LogSuffix() {

    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Script complete" -LogMessageType Info

    # Error Messaging
    $myErrorMessage = @"
********** ERROR START **********
Error Message: '$gErrMsg'

Stack Trace: 
$gScriptStackTrace
********** ERROR END   **********
"@


    # Script Status
    $scriptStats = @"
Start Time:                 $gStartDatetime
End Time:                   $(Get-FormattedDate)
Duration Total Seconds:     $($gStopWatch.Elapsed.TotalSeconds)

"@

    $titleSuffixStart = (Get-TitleHeader -repeatedCharacter "-" -totalLength (Get-TitleHeaderLength) -titleValue " Script Execution Statistics: ")
    $titleSuffix = (Get-TitleHeader -repeatedCharacter "#" -totalLength (Get-TitleHeaderLength) -titleValue " Script Results: End ")
    
    $retVal = ""
    if (-not ([string]::IsNullOrWhiteSpace($gErrMsg))) {
        $retVal = @"
$titleSuffixStart
$myErrorMessage

$scriptStats
$titleSuffix
"@
        

    }
    else {        
        $retVal = @"
$titleSuffixStart
$scriptStats
$titleSuffix        
"@        
    }


    return $retVal
}
function Get-CRLF {
    <#
        .SYNOPSIS 
        Detects the operating system and sends back the proper carriage return, or carriage return line feed
		https://docs.microsoft.com/en-us/dotnet/api/system.environment.newline?view=net-6.0
    #>
    return [Environment]::NewLine
}
function Set-LogTag {
    param(
        [Parameter(Mandatory = $true)][string]$tag,
        [Parameter(Mandatory = $true)][string]$value,
        [Parameter(Mandatory = $true)][string]$logFullPath
    )

    $content = Get-Content $logFullPath
    $content = $content -replace $tag, $value
    $content | Set-Content $logFullPath
}
function Write-ContentFromLogToGlobalLog{
    param(
        [Parameter(Mandatory = $true)][object][ValidateNotNullOrEmpty()]$dependency,
        [Parameter(Mandatory = $true)][string][ValidateNotNullOrEmpty()]$destination_log
    )    

    if ($($dependency.Value.generic.$os.logFile)){
        $dependency_log = $(Resolve-JsonStringSubstitutions -original_string $($dependency.Value.generic.$os.logFile))

        if (Test-Path -Path $dependency_log){
            [string]$log_content = Get-Content -Path $dependency_log -Raw
            Write-TraceWithTimestamp -logFullPath $destination_log -message $log_content -LogMessageType ([LogMessageType]::Info) -ScribeRawData
        }else{
            Write-TraceWithTimestamp -logFullPath $destination_log -message "No log at '$dependency_log' from dependency to include into global log." -LogMessageType ([LogMessageType]::Info)
        }
            
    }else{
        Write-TraceWithTimestamp -logFullPath $destination_log -message "This dependency does not expect a log file and as such, will not be included into the global log." -LogMessageType ([LogMessageType]::Info)
    }
}
#endregion

#region Functions
function Get-OsType{
 
    if (([System.Environment]::OSVersion.Platform) -eq "Unix"){
        $retVal = "linux"
    }else{
        $retval = "windows"
    }
    
    return $retval   
}
function Get-IsWindows{
    # PowerShell 5 doesn't have $IsWidows or $IsLinux
    if ((Get-OsType) -eq "windows"){
        return $true
    }else{
        return $false
    }
}
function Get-IsLinux{
    # PowerShell 5 doesn't have $IsWidows or $IsLinux
    if ((Get-OsType) -eq "linux"){
        return $true
    }else{
        return $false
    }    
}
function Resolve-JsonStringSubstitutions {
    param(
        [Parameter(mandatory=$true)][string]$original_string
    )

    foreach ($key in $PackageJsonStringSubstitutions.Keys) {
        if ($original_string -like "*$key*") {
            $scriptBlock = $PackageJsonStringSubstitutions[$key]
            $value = & $scriptBlock
            $original_string = $original_string -replace [regex]::Escape($key), $value
        }
    }

    return $original_string
}
function Get-PackageDestinationPath{
    param(
        [Parameter(Mandatory=$true)][object]$setup_dependency
    )

    [string]$os = Get-OsType
    [string[]]$package_directory = $setup_dependency.Value.generic.$os.destinationFolders | ForEach-Object {$_.path}
    return $package_directory
}

function Get-PackageWithoutVersion{
    param(
        [Parameter(Mandatory=$true)][object]$setup_dependency
    )

    [string]$os = Get-OsType
    return $setup_dependency.Value.generic.$os.packageWithoutVersion
}
function Invoke-ExpandArchive{
    param(
        [Parameter(Mandatory=$true)][object]$setup_dependency
    )
    
    $retVal = New-Object psobject -Property @{
        ExitCode    = 0
        stdout      = ""
        stderr      = ""
    }        

    $os         = Get-OsType
    $package    = Get-PackageWithoutVersion -setup_dependency $setup_dependency
    $archive_log_file   = $($setup_dependency.Value.generic.$os.logfile)
    if (($null -ne $archive_log_file) -and (-not [string]::IsNullOrWhiteSpace($archive_log_file))){
        $archive_log_file = (Resolve-JsonStringSubstitutions -original_string $($setup_dependency.Value.generic.$os.logfile))
    }else{
        $archive_log_file = (Resolve-JsonStringSubstitutions -original_string "./{##APFPrefix##}.{##TIMESTAMP##}.archive.install.log")
    }

    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Expand Archive Log File: $archive_log_file" -LogMessageType ([LogMessageType]::Info)
    [string]$zip_path = (Resolve-Path -Path (Join-Path -Path $gDependencyFolder -ChildPath $package))

    $tmp = Get-PackageDestinationPath -setup_dependency $setup_dependency | ForEach-Object{
        $force = $setup_dependency.Value.AutomaticPrerequisiteFramework.allowForceInstall

        if ($force -eq "true" -or $force -eq $true){
            Expand-Archive -Path $zip_path -DestinationPath (Resolve-JsonStringSubstitutions -original_string $_) -Force -Verbose -ErrorAction SilentlyContinue -ErrorVariable myErr -WarningVariable myWarn -InformationVariable myInfo *>&1 | Out-File $archive_log_file -Append
        }else{
            # https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.archive/expand-archive?view=powershell-7.4
            # https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.archive/expand-archive?view=powershell-5.1
            # By default, without the force parameter "Expand-Archive doesn't overwrite"
            Expand-Archive -Path $zip_path -DestinationPath (Resolve-JsonStringSubstitutions -original_string $_) -Verbose -ErrorAction SilentlyContinue -ErrorVariable myErr -WarningVariable myWarn -InformationVariable myInfo *>&1 | Out-File $archive_log_file -Append
        }
        
        [string]$expandArchiveLogOutput = Get-Content -Path $archive_log_file -Raw

        
        if (-not ($myErr.Count -gt 0 -or $myWarn.Count -gt 0 -or $myInfo.Count -gt 0)){
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $expandArchiveLogOutput -LogMessageType ([LogMessageType]::Info) -ScribeRawData
        }elseif ($myErr){
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $myErr -LogMessageType ([LogMessageType]::Error) -ScribeRawData
            Add-Content -Path $archive_log_file -Value $myErr -Force       
            $retVal.ExitCode = 1
            $retVal.stderr = $myErr
            break
        }elseif ($myWarn){            
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $myWarn -LogMessageType ([LogMessageType]::Warning) -ScribeRawData
            Add-Content -Path $archive_log_file -Value $myWarn -Force
            $retVal.stdout += $myWarn + (Get-CRLF)
        }elseif ($myInfo){
            $expandArchiveLogOutput = Get-Content -Path $archive_log_file 
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $myInfo -LogMessageType ([LogMessageType]::Info) -ScribeRawData
            Add-Content -Path $archive_log_file -Value $myInfo -Force
            $retVal.stdout += $myInfo + (Get-CRLF)
        }
    }

    return $retVal
}
function Invoke-MyCommand{
    <#
        .SYNOPSIS
            Execute a system command. Return a structure which includes stderr and stdout.
		.Description

        .PARAMETER commandTitle
            commandTitle: title of the command line invoked
		.PARAMETER commandPath
            commandPath: full path with command to be executed
		.PARAMETER commandArguments
            commandArguments: arguments of the command invoked.
        .PARAMETER workingDirectory
            Optional parameter that if passed sets the working directory
        .PARAMETER outputEncoding
            The encoding used for the standard output and error streams. Defaults to UTF-8.      
        .PARAMETER waitOptions
            When present this function will look for two properties on the hash table, MaxWaitSeconds and LoopInterval. 
            These properties tell this function how to wait for the exit of the process. When this option is not present the
            function will issue a WaitForExit(). Be careful with this option, you may end up waiting indefinitely if the 
            out of process server is not sending back an exit code.
        .OUTPUTS
            None
		.EXAMPLE
			#Parameter inputs
			$cmd = """${mysqlcmd_path}"" -U $sqluser -P $(ConvertFrom-SecureString -SecureString $sys_password -AsPlainText) -S ${server_name} -d ${database_name} -i ""${sql_script_path}"" -o ""$($env:ADXDIR)\${scriptName}_${folder_name}.log"""
			$cmdTitle = "Sage X3 Application Creation Execution"

			#Execution
			Add-To-Log -line "Command line executed: $cmd" -file $logName -errorLevel 0
			$cmdRetVal = Invoke-MyCommand -commandTitle $cmdTitle -commandPath $env:EXE_OSQL -commandArguments "/c $cmd"		
    #>
    param(
        [Parameter(Mandatory = $true, Position = 1)] [string] $commandTitle,
        [Parameter(Mandatory = $true, Position = 2)] [string] $commandPath,
        [Parameter(Mandatory = $true, Position = 3)] [string] $commandArguments,
        [Parameter(Mandatory = $false, Position = 4)] [string] $workingDirectory,
        [Parameter(Mandatory = $false, Position = 5)] [System.Text.Encoding] $outputEncoding = [System.Text.Encoding]::UTF8,
        [Parameter(Mandatory = $false, Position = 6)] [hashtable] $waitOptions
    )
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Invoke-MyCommand starting" -LogMessageType ([LogMessageType]::Info)
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "commandPath: $commandPath, commandArguments: $commandArguments, workingDirectory: $workingDirectory" -LogMessageType ([LogMessageType]::Info) -ScribeRawData
    $pinfo                          = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName                 = $commandPath
    $pinfo.RedirectStandardError    = $true
    $pinfo.RedirectStandardOutput   = $true
    $pinfo.StandardOutputEncoding   = $outputEncoding
    $pinfo.StandardErrorEncoding    = $outputEncoding
    
    if ($workingDirectory){
        $pinfo.WorkingDirectory = $workingDirectory
    }
    
    $pinfo.UseShellExecute  = $false
    $pinfo.Arguments        = $commandArguments
    $p                      = New-Object System.Diagnostics.Process
    $p.StartInfo            = $pinfo
    $p.Start() | Out-Null

    $stdout = ""
    $stderr = ""
    $exitCode = 0    

    if (-not $waitOptions){
        $p.WaitForExit()
        $stdout     = $p.StandardOutput.ReadToEnd()
        $stderr     = $p.StandardError.ReadToEnd()
        $exitCode   = $p.ExitCode                
    }else{
        # Initialize a counter for the loop designed to wait n minutes max. Some installers don't report an exit code to pwsh and waitForExit() will hang.
        $maxWaitSeconds     = 300  # Default max wait time (5 minutes)
        $loopInterval       = 2      # Default loop interval (2 seconds)

        # Check and assign values from $waitOptions
        if ($waitOptions) {
            if ($null -ne $waitOptions["MaxWaitSeconds"]) {
                $maxWaitSeconds = $waitOptions["MaxWaitSeconds"] -as [int]
            } else {
                throw "waitOptions missing required key: MaxWaitSeconds"
            }
            if ($null -ne $waitOptions["LoopInterval"]) {
                $loopInterval = $waitOptions["LoopInterval"] -as [int]
            } else {
                throw "waitOptions missing required key: LoopInterval"
            }
        }

        $maxAttempts        = [Math]::Round($maxWaitSeconds / $loopInterval)

        while (!$p.HasExited -and $counter -lt $maxAttempts) {
            Start-Sleep -Seconds $loopInterval
            $counter++ 
        }        

        if ($p.HasExited -eq $false){
            $stderr = "Installation failed to report back with an exit code. Please check the individual log for the installation status."
            $exitCode = 1
        }
    }
    
    
    [pscustomobject]@{
        commandTitle = $commandTitle
        stdout       = $stdout
        stderr       = $stderr
        ExitCode     = $exitCode
    }
    

    if ($p.ExitCode -ne 0){
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Command ended with ExitCode: $($p.ExitCode)" -LogMessageType ([LogMessageType]::Error)
    }else{
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Command ended with ExitCode: $($p.ExitCode)" -LogMessageType ([LogMessageType]::Info)
    }
}
function Write-ReturnValueOutput{
    param(
        [Parameter(Mandatory = $true)] [object][ValidateNotNullOrEmpty()] $retVal,
        [Parameter(Mandatory = $true)] [object][ValidateNotNullOrEmpty()] $setup_dependency
    )

    [string]$msg = @"

Stdout:     $($retVal.stdout)
Stderr:     $($retVal.stderr)
Exit Code:  $($retVal.ExitCode)

"@

    if ($retVal.ExitCode -eq 0){
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $msg -LogMessageType ([LogMessageType]::info) -ScribeRawData

        # Write to Izpack panel
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Dependency: $dependency_name, Installation Status: Success" -LogMessageType ([LogMessageType]::Info) -writeToHost
    }else{
        # We write to izpack panel when error is non-zero
        $errorMapping = $setup_dependency.Value.generic.$os.ErrorMapping.where({$null -ne $_.$($retVal.ExitCode)})
        if ($errorMapping){
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $msg -LogMessageType ([LogMessageType]::Warning) -ScribeRawData

            # Write to Izpack panel
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Dependency: $dependency_name, Installation Status: Success with warnings. Please check the log." -LogMessageType ([LogMessageType]::Info) -writeToHost
            
        }else{
            # Write to Izpack panel
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Dependency: $dependency_name, Installation Status: Failed" -LogMessageType ([LogMessageType]::Info) -writeToHost
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $msg -LogMessageType ([LogMessageType]::Error) -writeToHost -ScribeRawData

            # Error management is handled in test-cancontinue
        }
    }
}
function Invoke-MyCommandWrapper{
    param(
        [Parameter(Mandatory = $true, Position = 1)] [object][ValidateNotNullOrEmpty()] $dependency,
        [Parameter(Mandatory=$true)][string][ValidateSet('adxadmin', 'runtime')]$componentRequirement
    )

    $package_message = $(Get-InstallPackageMessage -setup_dependency $setup_dependency)
    $section_header = (Get-TitleHeader -repeatedCharacter "-" -totalLength (Get-TitleHeaderLength) -titleValue $package_message)

    # Add a space before each section header
    Add-Content -Path $gLogFullPath -Value (Get-CRLF)
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $section_header -LogMessageType ([LogMessageType]::Info)

    $os = Get-OsType
    $package_type = Get-PackageFileType -setup_dependency $dependency
    $orig_location = Get-Location
    $dependency_name = $dependency.Name
    
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Operating System: $os, Package Type: $package_type, Dependency Name: $dependency_name" -LogMessageType ([LogMessageType]::Info)

    $retVal = New-Object psobject -Property @{
        ExitCode = 0
        stdout = ""
        stderr = ""
    }        

    $dependency_component_requirement = $dependency.Value.AutomaticPrerequisiteFramework.componentRequirement
    if ($componentRequirement -notin $dependency_component_requirement){ 
        [string]$skipped_msg = @"
Component: '$($dependency.name)' skipped due to package.json installation requirements. Requested installation type not certified for this component.
Requested Install Type: '$componentRequirement'
Dependency Component Certified Type: '$dependency_component_requirement'
"@
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $skipped_msg -LogMessageType ([LogMessageType]::Info) -ScribeRawData
        $retVal.ExitCode = 0
        $retVal.stdout = $msg
        return $retVal        
    }else{
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Requested Installation Type: '$componentRequirement' is certified for this dependency." -LogMessageType ([LogMessageType]::Info)
    }    

    # Do we have a dependency to install for this operating system?
    if ($null -eq $dependency.Value.generic.$os){
        [string]$msg = "A generic installation package does not exist for this operating system. Dependency Name: $dependency_name"
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $msg -LogMessageType ([LogMessageType]::Info)
        $retVal.ExitCode = 0
        $retVal.stdout = $msg
        return $retVal
    }

    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Dependency '$dependency_name' verified for this operating system: '$os'" -LogMessageType ([LogMessageType]::Info)

    Set-Location -Path $PSScriptRoot
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Location set to: $($PSScriptRoot)" -LogMessageType ([LogMessageType]::Info)

    # Run Controls First, potentially terminating the script with failure or ShouldNotInstall
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Retrieving controls" -LogMessageType ([LogMessageType]::Info)
    $controls = Get-Controls -setup_dependency $dependency
    if ($controls){
        $control_retVal = Invoke-ScriptBlock -script_blocks $controls
        # For Debugging
        # if ($package_type -eq ([FileTypes]::msi)){ $control_retVal = "ShouldInstall"}
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Control code for dependency: $dependency_name, returned: $control_retVal" -LogMessageType ([LogMessageType]::Info)

        switch ($control_retVal) {
            ([ControlReturnCodes]::ShouldInstall) {  
                break
            }
            ([ControlReturnCodes]::ShouldNotInstall) {  
                $retVal.ExitCode = 0
                $retVal.stdout = $msg
                Set-Location $orig_location
                return $retVal
            }
            Default {
                [string]$msg = "Unknown control response code for dependency: $dependency_name, terminating script."
                Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $msg -LogMessageType ([LogMessageType]::Error)
                $retVal.ExitCode = 1
                $retVal.stderr = $msg
                Set-Location $orig_location
                return $retVal
            }
        }
    }else{
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "No controls on dependency: $dependency_name. Installation proceeding." -LogMessageType ([LogMessageType]::Info)
    }

    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Verifying asset '$($dependency.Value.generic.$os.packageWithoutVersion)' exists on disk" -LogMessageType ([LogMessageType]::Info)
    $dependency_asset_on_disk = Get-Item (Join-Path -Path $gDependencyFolder -ChildPath $($dependency.Value.generic.$os.packageWithoutVersion))
    if (-not $dependency_asset_on_disk){
        $msg = "Expected file '$($dependency_asset_on_disk.FullName)' isn't available for install. Installation failed"
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $msg -LogMessageType ([LogMessageType]::Error)
        $retVal.ExitCode = 1
        $retVal.stderr = $msg
        Set-Location $orig_location
        return $retVal
    }else{
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Asset confirmed on disk: $($dependency_asset_on_disk.FullName). Installation proceeding." -LogMessageType ([LogMessageType]::Info)
    }

    # We are cleared to install
    $my_wait_options = @{ MaxWaitSeconds = 300; LoopInterval = 2 }
    switch ($package_type) {
        ([FileTypes]::exe) {  
            # Exe spawns a cmd window which needs to know the working directory
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Invoke-MyCommand with exe type." -LogMessageType ([LogMessageType]::Info)
            [object]$command_payload = Get-CommandPayload -dependency $dependency 
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Command Payload: $command_payload. Refer to command payload for log file name." -LogMessageType ([LogMessageType]::Info) -ScribeRawData
            $retVal = Invoke-MyCommand -commandTitle $package_message -commandPath $command_payload.commandPath -commandArguments $command_payload.ArgumentString  -workingDirectory $gDependencyFolder 

            # Log management: Framework will handle
            Write-ContentFromLogToGlobalLog -dependency $dependency -destination_log $gLogFullPath

            break
        }
        ([FileTypes]::nupkg) {  
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Invoke-MyCommand with nupkg type." -LogMessageType ([LogMessageType]::Info)
            $retVal = Install-PsModuleAccordingToRules -setup_dependency $dependency 

            # Log management: Retrieved from stdout/stderr and managed using Write-ReturnValueOutput just below
            break
        }
        ([FileTypes]::zip){
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Invoke-MyCommand with zip type." -LogMessageType ([LogMessageType]::Info)
            $retVal = Invoke-ExpandArchive -setup_dependency $dependency

            # Log management: Retrieved from stdout/stderr and managed using Write-ReturnValueOutput just below
            break
        }
        ([FileTypes]::msi){
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Invoke-MyCommand with msi type." -LogMessageType ([LogMessageType]::Info)
            [object]$command_payload = Get-CommandPayload -dependency $dependency 
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Command Payload: $command_payload. Refer to command payload for log file name." -LogMessageType ([LogMessageType]::Info) -ScribeRawData

            # Thanks to the Microsoft gods who need to do everything differently, we must encode msi only as unicode
            [System.Text.Encoding]$myEncoding = [System.Text.Encoding]::Unicode
            $retVal = Invoke-MyCommand -commandTitle $package_message -commandPath $command_payload.commandPath -commandArguments $command_payload.ArgumentString -outputEncoding $myEncoding -workingDirectory $gDependencyFolder 

            # Log management: Framework will handle
            Write-ContentFromLogToGlobalLog -dependency $dependency -destination_log $gLogFullPath
            break            
        }
        ([FileTypes]::ps1){
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Invoke-MyCommand with ps1 type." -LogMessageType ([LogMessageType]::Info)
            [object]$command_payload = Get-CommandPayload -dependency $dependency 
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Command Payload: $command_payload. Refer to command payload or powershell script for log file name." -LogMessageType ([LogMessageType]::Info) -ScribeRawData
            $retVal = Invoke-MyCommand -commandTitle $package_message -commandPath $command_payload.commandPath -commandArguments $command_payload.ArgumentString -workingDirectory $gDependencyFolder

            # Log management: Retrieved from stdout/stderr and managed using Write-ReturnValueOutput just below
            break
        }
        Default {
            [string]$msg = "The file type: $package_type is not supported in this function"
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $msg -LogMessageType ([LogMessageType]::Error)
            throw $msg
        }
    }
        
        
    Set-Location -Path $orig_location
    Write-ReturnValueOutput -retVal $retVal -setup_dependency $dependency

    return $retVal
}
function Get-PackageFileType {
    param(
        [Parameter(Mandatory = $true)][object][ValidateNotNullOrEmpty()]$setup_dependency
    )

    # Map the file extension to the enum, if possible
    try {
        $file_type = [FileTypes]::Parse([FileTypes], $setup_dependency.Value.AutomaticPrerequisiteFramework.installType, $true)
    } catch {
        throw "File extension '$($setup_dependency.Value.AutomaticPrerequisiteFramework.installType)' is not a valid FileType"
    }

    return $file_type
    
}
function Get-PackageFullName{
    param(
        [Parameter(Mandatory = $true)][object][ValidateNotNullOrEmpty()]$setup_dependency
    )    

    if (Get-IsWindows){
        if ($null -ne $setup_dependency.Value.generic.windows.packageFullName){
            $file_type = $setup_dependency.Value.generic.windows.packageFullName | Split-Path -Leaf
        }
    }elseif(Get-IsLinux){
        if ($null -ne $setup_dependency.Value.generic.linux.packageFullName){
            $file_type = $setup_dependency.Value.generic.linux.packageFullName | Split-Path -Leaf
        }
    }else{
        throw "Unknown operating system when determing PackageFullName"
    }


    return $retVal
}
function Get-InstallPackageMessage{
    param(
        [Parameter(mandatory=$true)][object][ValidateNotNullOrEmpty()]$setup_dependency
    )

    $os = Get-OsType
    [string]$msg = "Installing Package Name: $($setup_dependency.Name) | Package File: $($setup_dependency.Value.generic.$os.packageWithoutVersion), Package Version: $($setup_dependency.Value.generic.$os.packageVer)"
    return $msg
}
function Register-MyPsRepository{
    param(
        [Parameter(Mandatory=$true)][object]$setup_dependency
    )
    
    [string]$repo_name = $setup_dependency.Value.AutomaticPrerequisiteFramework.repository_name
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Checking PsRepository Exists: $repo_name" -LogMessageType ([LogMessageType]::Info)

    # Check if the repository exists
    $existingRepo = Get-PSRepository -Name $repo_name -ErrorAction SilentlyContinue -WarningAction SilentlyContinue -InformationAction SilentlyContinue

    if ($null -ne $existingRepo){
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Repository $repo_name exists" -LogMessageType ([LogMessageType]::Info)

        # Check if the SourceLocation matches the desired path
        if ($existingRepo.SourceLocation -ne $gDependencyFolder.Path) {
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Updating repository source location to $gDependencyFolder" -LogMessageType ([LogMessageType]::Info)
            Set-PSRepository -Name $repo_name -SourceLocation $gDependencyFolder.Path
        }
    } else {
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Creating local repository" -LogMessageType ([LogMessageType]::Info)
        Register-PSRepository -Name $repo_name -SourceLocation $gDependencyFolder.Path -InstallationPolicy Trusted 
    }

    return Get-PSRepository -Name $repo_name -WarningAction SilentlyContinue -InformationAction SilentlyContinue
}
function Get-PsPackageInstallBasePath{
    <#
        .SYNOPSIS
        When installing a powershell package provider using the zip deployment method you need to know the base path. 
        We take the all users scope here only.
    #>
    [string]$ps_path_by_userscope = ""
    
    if (Get-IsWindows){
            $ps_path_by_userscope = Join-Path -Path $env:ProgramFiles -ChildPath "/PackageManagement/ProviderAssemblies"
    }elseif (Get-IsLinux){
        # https://github.com/PowerShell/PowerShell/blob/dd598b3d527a03f4874d28b1e8aac19566d7aeb2/src/System.Management.Automation/engine/Modules/ModuleIntrinsics.cs#L1015-L1029
        [string]$ps_path_by_userscope = [System.Management.Automation.Platform]::SelectProductNameForDirectory('SHARED_MODULES')

    }else{
        throw "Operating system unsupported"
    }

    return $ps_path_by_userscope
}
function Get-PsModuleInstallBasePath{
    <#
        .SYNOPSIS
        When installing a powershell module you need to know the base path for where to install a module. This should not include the version of the module number 
        as the unzip process will handle that.
    #>
    param(
        [Parameter(Mandatory=$true)][string][ValidateNotNullOrEmpty()]$module_name,
        [Parameter(Mandatory=$true)][ValidateSet('AllUsers', 'CurrentUser')]$scope
    )

    [string]$ps_path_by_userscope = ""

    if (Get-IsWindows){
        if ($scope -eq "AllUsers") {
            $ps_path_by_userscope = Join-Path -Path $env:ProgramFiles -ChildPath "/PowerShell/Modules"
        } else {
            $ps_path_by_userscope = Join-Path -Path $HOME -ChildPath "/Documents/PowerShell/Modules"
        }
    }elseif (Get-IsLinux){
        if ($scope -eq "AllUsers") {
            # https://github.com/PowerShell/PowerShell/blob/dd598b3d527a03f4874d28b1e8aac19566d7aeb2/src/System.Management.Automation/engine/Modules/ModuleIntrinsics.cs#L1015-L1029
            [string]$ps_path_by_userscope = [System.Management.Automation.Platform]::SelectProductNameForDirectory('SHARED_MODULES')
        } else {
            # https://github.com/PowerShell/PowerShell/blob/dd598b3d527a03f4874d28b1e8aac19566d7aeb2/src/System.Management.Automation/engine/Modules/ModuleIntrinsics.cs#L965-L973
            [string]$ps_path_by_userscope = [System.Management.Automation.Platform]::SelectProductNameForDirectory('USER_MODULES')            
        }
    }else{
        throw "Operating system unsupported"
    }

    return $ps_path_by_userscope
}
function Get-ModulePathByScopeByVersion{
    param(
        [Parameter(Mandatory=$true)][string][ValidateNotNullOrEmpty()]$module_name,
        [Parameter(Mandatory=$true)][string][ValidateNotNullOrEmpty()]$required_version,
        [Parameter(Mandatory=$true)][ValidateSet('AllUsers', 'CurrentUser')]$scope
    )

    $module_path = ""
    
    [string]$base_path      = Get-PsModuleInstallBasePath -scope $scope -module_name $module_name
    [string]$module_path    = Join-Path -Path $base_path -ChildPath $module_name -AdditionalChildPath $required_version
    return $module_path
}
function Get-ModuleInstallationStatus {
    param(
        [Parameter(Mandatory=$true)][string][ValidateNotNullOrEmpty()]$module_name,
        [Parameter(Mandatory=$true)][string][ValidateNotNullOrEmpty()]$required_version
    )

    # Check if the module with the specific version is installed 
    $retVal = $false
    pwsh -Command {
        param($module_name, $required_version)
        if (Get-InstalledModule -Name $module_name -RequiredVersion $required_version -ErrorAction SilentlyContinue -WarningAction SilentlyContinue -InformationAction SilentlyContinue){
            exit 0
        }else {
            exit 1
        }
    } -args @($module_name, $required_version) 

    if ($LASTEXITCODE -eq 0){
        $retVal = $true
    }


    return $retVal
}
function Get-ScriptArgString{
    param(
        [Parameter(mandatory=$true)][object]$dependency
    )

    [string]$paramsArg = [string]::empty

    if ($null -ne $dependency.value.generic.$os.myArgs.parameters){
        foreach ($param in $dependency.value.generic.$os.myargs.parameters){
            $param = (Resolve-JsonStringSubstitutions -original_string $param)
            $paramsArg += "$param "
        }        
    }else{
        return $null
    }

    return $paramsArg
}
function Convert-JsonToPs1Args{
    param(
        [Parameter(mandatory=$true)][object]$dependency
    )

    $os = Get-OsType
    $execution_policy   = "-executionPolicy $($dependency.value.generic.$os.myargs.executionPolicy)"
    $file               = "-file $(Resolve-Path -Path (Resolve-JsonStringSubstitutions -original_string $dependency.value.generic.$os.myargs.scriptPath))"
    $paramsArg          = (Get-ScriptArgString -dependency $dependency)

    return "$execution_policy $file $paramsArg"
}
function Convert-JsonToExeArgs{
    param(
        [Parameter(mandatory=$true)][object]$dependency
    )

    # https://learn.microsoft.com/en-us/cpp/windows/redistributing-visual-cpp-files?view=msvc-170#command-line-options-for-the-redistributable-packages

    $package            = "$($dependency.value.generic.$os.packageWithoutVersion)"
    $paramsArg          = (Get-ScriptArgString -dependency $dependency)
    
    return "/C $package $paramsArg"
}
function Convert-JsonToMsiArgs{
    param(
        [Parameter(mandatory=$true)][object]$dependency
    )

    $os = Get-OsType 
    $package            = "/package $($dependency.value.generic.$os.packageWithoutVersion)"
    $paramsArg          = (Get-ScriptArgString -dependency $dependency)

    return "$package $paramsArg"
}
function Get-CommandPayload{
    param(
        [Parameter(mandatory=$true)][object][ValidateNotNullOrEmpty()]$dependency
    )    

    [object]$retVal = New-Object PSObject -Property @{
        ArgumentString  = $null
        CommandPath     = $null
    }
    [string]$argType = $dependency.Value.AutomaticPrerequisiteFramework.installType

    switch ($ArgType) {
        'EXE' {  
            $retVal.ArgumentString = Convert-JsonToExeArgs -dependency $dependency
            if ($null -eq $retVal.ArgumentString){
                $retVal.ArgumentString = " "
            }
            $retVal.commandPath = "cmd"
            break
        }
        'PS1' { 
            $retVal.ArgumentString = Convert-JsonToPs1Args -dependency $dependency
            if ($null -eq $retVal.ArgumentString){
                $retVal.ArgumentString = " "
            }
            $retVal.commandPath = "pwsh"
            break
         }
        'MSI' { 
            $retVal.ArgumentString = Convert-JsonToMsiArgs -dependency $dependency
            if ($null -eq $retVal.ArgumentString){
                $retVal.ArgumentString = " "
            }
            $retVal.commandPath = "msiexec"
            break 
        }
        'Nupkg'{
            # Not used
            $retVal.ArgumentString = ""
            $retVal.commandPath = ""
            break
        }
        'Zip'{
            $retVal.ArgumentString = ""
            $retVal.commandPath = ""
            break
        }
        Default {throw 'Unknown argument type'}
    }

    return $retVal
}
function Get-ScriptBlocks{
    param(
        [Parameter(mandatory=$true)][object][ValidateNotNullOrEmpty()]$dependency
    )

    $controls = Get-Controls -setup_dependency $dependency
    [object[]]$script_block = $null
    foreach($script in $controls){
        $script_block += $script
    }

    return $script_block
}
function Invoke-ScriptBlock {
    param(
        [Parameter(mandatory=$true)][object][ValidateNotNullOrEmpty()]$script_blocks
    )    
    
    $retVal = $false

    foreach ($script_block in $script_blocks) {    
        try {
            foreach($control in $script_block){
                $property_name = $script_block.PsObject.Properties.name
                [scriptblock]$block = [scriptblock]::Create($script_block.$property_name)
                $retVal = Invoke-Command -ScriptBlock $block

                # We can proceed as long as any control doesn't return false
                if ($retVal -eq ([ControlReturnCodes]::ShouldNotInstall)){
                    return $retVal
                }
            }
        }
        catch {
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Error executing script block: $($_)" -LogMessageType ([LogMessageType]::Info)
            $retVal = $_.Exception.HResult            
            break
        }
    }

    return $retVal
}
function Install-MyPsModule{
    param(
        [Parameter(Mandatory=$true)][string][ValidateNotNullOrEmpty()]$repo_name,
        [Parameter(Mandatory=$true)][string][ValidateNotNullOrEmpty()]$module_name,
        [Parameter(Mandatory=$true)][string][ValidateNotNullOrEmpty()]$required_version,
        [Parameter(Mandatory=$true)][ValidateSet('AllUsers', 'CurrentUser')]$scope,
        [Parameter(Mandatory=$true)][System.Boolean][ValidateNotNullOrEmpty()]$force,
        [Parameter(Mandatory=$true)][string][ValidateNotNullOrEmpty()]$output_log_file
    )

    [string]$msg = "Installing PowerShell module '$module_name', version '$required_version' into PowerShell Core (pwsh) using PowerShell Version '$($PSVersionTable.PSVersion.ToString())'"
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "PowerShell: $msg" -ScribeRawData -LogMessageType ([LogMessageType]::Info)

    # Installing modules must have the perspective of pwsh, not powershell
    $result = pwsh -Command {
        param($repo_name, $module_name, $required_version, $scope, $force)
        try {
            if ($force) {
                Install-Module -Repository $repo_name -Name $module_name -RequiredVersion $required_version -Scope $scope -Force -AllowClobber -ErrorAction Stop #-Verbose *>&1 | Out-File -FilePath $output_log_file
            } else {
                Install-Module -Repository $repo_name -Name $module_name -RequiredVersion $required_version -Scope $scope -ErrorAction Stop #-Verbose *>&1 | Out-File -FilePath $output_log_file
            }
            exit 0
            
        } catch {
            # ExitCode will be set to 1 
            Write-Host $_
            throw $_
        }
    } -args @($repo_name, $module_name, $required_version, $scope, $force) *>&1 | Out-File $output_log_file 

    $myErr = $LASTEXITCODE
    $retVal = New-Object psobject -Property @{
        ExitCode = 0
        stdout = ""
        stderr = ""
    }        

    if ($myErr -ne 0){
        $retVal.ExitCode    = 1
        [string]$msg        = "Module '$module_name' installation failed, please check the log: $output_log_file"
        $retVal.stderr      = $msg
        Set-Content -Path $output_log_file -Value $msg -Force 
    }else{
        $retVal.ExitCode    = 0
        [string]$msg        = "Module '$module_name' installation succeeded, log available: $output_log_file"
        $retVal.stderr      = $msg
        Set-Content -Path $output_log_file -Value $msg -Force         
    }
    return $retVal
}
function Install-PsModuleAccordingToRules{
    param(
        [Parameter(mandatory=$true)][object][ValidateNotNullOrEmpty()]$setup_dependency
    )

    $retVal = New-Object psobject -Property @{
        ExitCode = 0
        stdout = ""
        stderr = ""
    }        


    $repo = Register-MyPsRepository -setup_dependency $setup_dependency -WarningAction SilentlyContinue -InformationAction SilentlyContinue
    if ($null -eq $repo){
        Throw "Nupkg repository: '$($setup_dependency.Value.AutomaticPrerequisiteFramework.repository_name)' not registered"
    }    

    $os                     = Get-OsType
    $apf                    = $setup_dependency.Value.AutomaticPrerequisiteFramework
    $version                = $setup_dependency.Value.generic.$os.packageVer
    $psmodule_log_file      = $($setup_dependency.Value.generic.$os.logfile)
    $source_directory       = (Get-PSRepository -Name $setup_dependency.Value.AutomaticPrerequisiteFramework.repository_name -WarningAction SilentlyContinue -InformationAction SilentlyContinue).SourceLocation
    $package_path           = (Join-Path -Path $source_directory -ChildPath $($setup_dependency.Value.generic.$os.packageWithoutVersion))
    $repo_name              = $setup_dependency.Value.AutomaticPrerequisiteFramework.repository_name

    if (($null -ne $psmodule_log_file) -and (-not [string]::IsNullOrWhiteSpace($psmodule_log_file))){
        $psmodule_log_file = (Resolve-JsonStringSubstitutions -original_string $setup_dependency.Value.generic.$os.logfile)
    }else{
        $psmodule_log_file = (Resolve-JsonStringSubstitutions -original_string (Join-Path -Path $PSScriptRoot -ChildPath "{##LOCAL_DIRECTORY##}/{##APFPrefix##}.{##TIMESTAMP##}.psmodule.install.log"))
    }        

    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Installing PowerShell Module: $($apf.module_name)" -LogMessageType ([LogMessageType]::Info)
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "PowerShell Module Log File: $psmodule_log_file" -LogMessageType ([LogMessageType]::Info)

    # Do we need to install?
    if (Get-ModuleInstallationStatus -module_name $($apf.module_name) -required_version $version){
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "PowerShell Module '$($apf.module_name)' at version '$version' already installed. Skipping installation." -LogMessageType ([LogMessageType]::Info)
        $retVal.ExitCode = 0
        return $retVal
    }
    
    # Install from this repository
    if ($apf.allowForceInstall -eq "true"){
        if ($null -ne $apf.scope){
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Force install: true, Scope: $($apf.scope)" -LogMessageType ([LogMessageType]::Info)
            $tmp = Install-MyPsModule -repo_name $repo_name -module_name $($apf.module_name) -required_version $version -scope $($apf.scope) -force ([System.Boolean]$($apf.allowForceInstall)) -output_log_file $psmodule_log_file 
        }else{
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Force install: true, Scope: N/A" -LogMessageType ([LogMessageType]::Info)
            $tmp = Install-MyPsModule -repo_name $repo_name -module_name $($apf.module_name) -required_version $version -scope CurrentUser -force ([System.Boolean]$($apf.allowForceInstall)) -output_log_file $psmodule_log_file 
        }
    }else{
        if ($null -ne $apf.scope){
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Force install: false, Scope: $($apf.scope)" -LogMessageType ([LogMessageType]::Info)
            $tmp = Install-MyPsModule -repo_name $repo_name -module_name $($apf.module_name) -required_version $version -scope $($apf.scope) -force ([System.Boolean]$($apf.allowForceInstall)) -output_log_file $psmodule_log_file 
        }else{
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Force install: false, Scope: N/A" -LogMessageType ([LogMessageType]::Info)
            $tmp = Install-MyPsModule -repo_name $repo_name -module_name $($apf.module_name) -required_version $version -scope CurrentUser -force ([System.Boolean]$($apf.allowForceInstall)) -output_log_file $psmodule_log_file 
        }        
    }    

    # Verify the installation. 
    [System.Boolean]$module_status = Get-ModuleInstallationStatus -module_name $($apf.module_name) -required_version $version 

    if ($module_status -eq $true){
        [string]$psmodule_log_output = Get-Content -Path $psmodule_log_file -Raw
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $psmodule_log_output -LogMessageType ([LogMessageType]::Info) -ScribeRawData
        $retVal.ExitCode = 0
    }else{
        [string]$psmodule_error_msg = "PowerShell module: $($apf.module_name) failed installation. See log at $psmodule_log_file for more details."
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "PowerShell module: $($apf.module_name) failed installation" -LogMessageType ([LogMessageType]::Error) -ScribeRawData

        [string]$psmodule_log_output = Get-Content -Path $psmodule_log_file -Raw -ErrorAction SilentlyContinue
        if ($psmodule_log_output.Count -gt 0){
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $psmodule_log_output -LogMessageType ([LogMessageType]::Error) -ScribeRawData
        }

        $retVal.stderr      = $psmodule_error_msg
        $retVal.ExitCode    = 1
    }    

    return $retVal
}
function Install-NupkgPackage{
    param(
        [Parameter(Mandatory=$true)][object]$setup_dependency,
        [Parameter(Mandatory=$true)][string][ValidateSet('adxadmin', 'runtime')]$componentRequirement
    )
    return Invoke-MyCommandWrapper -dependency $setup_dependency -componentRequirement $componentRequirement
}
function Install-ExePackage {
    param(
        [Parameter(mandatory=$true)][object][ValidateNotNullOrEmpty()]$setup_dependency,
        [Parameter(Mandatory=$true)][string][ValidateSet('adxadmin', 'runtime')]$componentRequirement
    )
    return Invoke-MyCommandWrapper -dependency $setup_dependency -componentRequirement $componentRequirement
}
function Install-MsiPackage{
    param(
        [Parameter(mandatory=$true)][object][ValidateNotNullOrEmpty()]$setup_dependency,
        [Parameter(Mandatory=$true)][string][ValidateSet('adxadmin', 'runtime')]$componentRequirement
    )
    return Invoke-MyCommandWrapper -dependency $setup_dependency -componentRequirement $componentRequirement
}
function Install-Ps1Package{
    param(
        [Parameter(Mandatory=$true)][object]$setup_dependency,
        [Parameter(Mandatory=$true)][string][ValidateSet('adxadmin', 'runtime')]$componentRequirement
    )
    return Invoke-MyCommandWrapper -dependency $setup_dependency -componentRequirement $componentRequirement
}
function Install-ZipPackage {
    param(
        [Parameter(Mandatory=$true)][object]$setup_dependency,
        [Parameter(Mandatory=$true)][string][ValidateSet('adxadmin', 'runtime')]$componentRequirement
    )
    return Invoke-MyCommandWrapper -dependency $setup_dependency -componentRequirement $componentRequirement
}
function Install-PsScriptBlock{
    param(
        [Parameter(Mandatory=$true)][object]$setup_dependency,
        [Parameter(Mandatory=$true)][string][ValidateSet('adxadmin', 'runtime')]$componentRequirement
    )
    $package_message = $(Get-InstallPackageMessage -setup_dependency $setup_dependency)
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $package_message -LogMessageType ([LogMessageType]::Info)
    
    throw "Not implemented yet"
}
function Has-Property {
    param (
        [object]$Object,
        [string]$PropertyName
    )
    return $Object.PSObject.Properties.Name -contains $PropertyName
}
function Invoke-ExitCodeAction{
    param(
        [Parameter(Mandatory = $true)][object][ValidateNotNullOrEmpty()]$myRetVal,
        [Parameter(Mandatory = $true)][object][ValidateNotNullOrEmpty()]$setup_dependency
    )    

    [string]$os     = Get-OsType
    $exitCodeAction = $setup_dependency.Value.generic.$os.ErrorMapping.where({$_.$($myRetVal.ExitCode) -ne $null})
    if ($exitCodeAction){
        if ($exitCodeAction.scriptblock){
            $sb = [scriptblock]::Create($exitCodeAction.scriptblock)
            Invoke-Command -ScriptBlock $sb
        }
    }
}
function Test-ExitCode{
    param(
        [Parameter(Mandatory = $true)][object][ValidateNotNullOrEmpty()]$myRetVal,
        [Parameter(Mandatory = $true)][object][ValidateNotNullOrEmpty()]$setup_dependency
    )    

    [string]$os                     = Get-OsType
    [System.Boolean]$can_continue   = $true

    # Does an error mapping exist?
    $errorMapping = $setup_dependency.Value.generic.$os.ErrorMapping.where({$_.$($myRetVal.ExitCode) -ne $null})
    if ($errorMapping){
        # Error mapping exists for this return code

        # Does the error mapper say we can continue for this error code?
        if ($errorMapping.AllowContinue -eq "true"){
            $can_continue = $true
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $($errorMapping.$($myRetVal.ExitCode)) -LogMessageType ([LogMessageType]::Warning) -ScribeRawData
        }elseif($errorMapping.AllowContinue -eq "false"){
            $can_continue = $false
            Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $($errorMapping.$($myRetVal.ExitCode)) -LogMessageType ([LogMessageType]::Error) -ScribeRawData
        }else{
            throw "Error Mappings for this dependency is missing the AllowContinue attribute"
        }
    }else{
        # No Error Mapping exists for this setup_dependency. Let's proceed with normal exit code management.

        # Check if $retVal is a number and equals 0
        if ($retVal -is [int] -or $retVal -is [double] -or $retVal -is [float]) {
            if ($retVal -ne 0) {
                Write-TraceWithTimestamp -logFullPath $gLogFullPath 
                throw "Installation Package: $($setup_dependency.name) failed installation. Please check the logs."
            }
        }
        # Check if $retVal is an object and has an ExitCode property equal to 0
        elseif ($retVal -is [object] -and (Has-Property -Object $retVal -PropertyName "ExitCode")) {
            if ($retVal.ExitCode -ne 0) {
                throw "Installation Package: $($setup_dependency.name) failed installation. Please check the logs."
            } 
        } else {
            throw  "retVal is an unexpected data type with neither a number nor an object with an ExitCode property"
        }            
    }

    return $can_continue
}
function Test-CanContinue{
    param(
        [Parameter(Mandatory = $true)][object][ValidateNotNullOrEmpty()]$retVal,
        [Parameter(Mandatory = $true)][object][ValidateNotNullOrEmpty()]$setup_dependency
    )

    [System.Boolean]$can_continue   = $true

    # Build a retVal object if one doesn't already exist
    $myRetVal = New-Object psobject -Property @{
        ExitCode    = 0
        stdout      = ""
        stderr      = ""
    }   
    
    # Assign the error code to the temporary myRetVal object
    if ($retVal -is [object] -and (Has-Property -Object $retVal -PropertyName "ExitCode")){
        $myRetVal.ExitCode = $retVal.ExitCode
    }elseif ($retVal -is [int] -or $retVal -is [double] -or $retVal -is [float]){
        $myRetVal.ExitCode = $retVal
    }

    # Check if Error Mappings exist on this setup dependency. If there is a match on the $retVal ExitCode and the ErrorMappings
    Invoke-ExitCodeAction -myRetVal $myRetVal -setup_dependency $setup_dependency 

    # Check if we can continue
    $can_continue = Test-ExitCode -myRetVal $myRetVal -setup_dependency $setup_dependency 

    return $can_continue
}

function Loop-Dependencies {
    param(
        [Parameter(Mandatory=$true)][object][ValidateNotNullOrEmpty()]$config,
        [Parameter(Mandatory=$true)][string][ValidateSet('adxadmin', 'runtime')]$componentRequirement,
        [Parameter(Mandatory=$false)][switch]$preinstallation
    )

    [string]$install_begin_msg = @"
Beginning Automatic Prerequisite Framework Installation
Caution: Some dependency installations may take some time to complete
Log Directory: $gLogFileDir $(Get-CRLF)
"@
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $install_begin_msg -LogMessageType ([LogMessageType]::Info) -writeToHost   

    foreach ($dependency in $config.PSObject.Properties) {      

        $dependencyPreinstallation = $dependency.Value.AutomaticPrerequisiteFramework.preinstallation
        if ($preinstallation) {
            # If the preinstallation switch is provided, process only dependencies with preinstallation = 'true'
            if ($dependencyPreinstallation -ne 'true') {
                continue
            }
        } else {
            # If the preinstallation switch is not provided, skip dependencies with preinstallation = 'true'
            if ($dependencyPreinstallation -eq 'true') {
                continue
            }
        }

        $package_type = Get-PackageFileType -setup_dependency $dependency 

        switch ($package_type){
            ([FileTypes]::exe){
                $retVal = Install-ExePackage -setup_dependency $dependency -componentRequirement $componentRequirement
                $tmp = Test-CanContinue -retVal $retVal -setup_dependency $dependency
                break
            }
            ([FileTypes]::msi){
                $retVal = Install-MsiPackage -setup_dependency $dependency -componentRequirement $componentRequirement
                $tmp = Test-CanContinue -retVal $retVal -setup_dependency $dependency
                break
            }
            ([FileTypes]::zip){
                $retVal = Install-ZipPackage -setup_dependency $dependency -componentRequirement $componentRequirement
                $tmp = Test-CanContinue -retVal $retVal -setup_dependency $dependency
                break
            }
            ([FileTypes]::nupkg){
                $retVal = Install-NupkgPackage -setup_dependency $dependency -componentRequirement $componentRequirement
                $tmp = Test-CanContinue -retVal $retVal -setup_dependency $dependency
                break
            }
            ([FileTypes]::ps1){
                $retVal = Install-Ps1Package -setup_dependency $dependency -componentRequirement $componentRequirement
                $tmp = Test-CanContinue -retVal $retVal -setup_dependency $dependency
                break
            }
            ([FileTypes]::PsScriptBlock){
                $retVal = Install-PsScriptBlock -setup_dependency $dependency -componentRequirement $componentRequirement
                $tmp = Test-CanContinue -retVal $retVal -setup_dependency $dependency
                break
            }                        
            default{throw "Unsupported package type: $package_type"}
        }
    }

    # componentRequirement must exist in the package.json thanks to the validateset requirement and current values in package.json. 
    # In the future, if such a requirement changes, we need a retVal to report exit code and std* streams.
    if ($null -eq $retVal){
        $retVal = New-Object psobject -Property @{
            ExitCode    = 0
            stdout      = ""
            stderr      = ""
        }             
    }

    $section_header = (Get-TitleHeader -repeatedCharacter "-" -totalLength (Get-TitleHeaderLength) -titleValue "Dependency loop complete")
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $section_header -LogMessageType ([LogMessageType]::Info) 

    if ($gRebootRequired -eq $true){
        Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "A reboot is required. Please reboot your computer at the end of this installation." -LogMessageType ([LogMessageType]::Warning) -writeToHost    
    }    

    $LASTEXITCODE = $retVal.ExitCode
    return $retVal
}
function Get-Config{
    param(
        [Parameter(Mandatory=$true)][string]$file_path
    )

    return Get-Content -Path $file_path | ConvertFrom-Json
}
function Get-Controls{
    param(
        [Parameter(Mandatory=$true)][object]$setup_dependency
    )
    
    $retVal = $null
    $package_type = Get-PackageFileType -setup_dependency $setup_dependency
    switch ($package_type) {
        ([FileTypes]::zip) {  
            # [object]$retVal = $setup_dependency.Value.generic.$os.destinationFolders | ForEach-Object {$_.controls} | ConvertTo-Json
            [object[]]$retVal = $setup_dependency.Value.generic.$os.destinationFolders | ForEach-Object {$_.controls} | Where-Object { $_ -ne $null -and $_ -ne '' }

            break
        }
        Default {
            $retVal = $setup_dependency.Value.generic.$os.controls
            break
        }
    }

    return $retVal
}
function Get-VcRedistInstallInfo{
    # Microsoft advises to check the registry: https://learn.microsoft.com/en-us/cpp/windows/redistributing-visual-cpp-files?view=msvc-170
    $redist = Get-ItemProperty 'HKLM:\SOFTWARE\Microsoft\VisualStudio\14.0\VC\Runtimes\X64' 
    return $redist
}
#endregion


try {
    Initialize
    $config = Get-Config -file_path $gkPackageJson
    if ($preinstallation){
        Loop-Dependencies -config $config.setupDependencies -componentRequirement $componentRequirement -preinstallation
    }else{
        Loop-Dependencies -config $config.setupDependencies -componentRequirement $componentRequirement
    }

    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message "Prerequisite installations completed successfully" -LogMessageType ([LogMessageType]::Info) -writeToHost

    $gStopWatch.Stop()
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message (Build-LogSuffix) -LogMessageType ([LogMessageType]::Info) -ScribeRawData
    Exit 0
}
catch {
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $_ -LogMessageType ([LogMessageType]::Error) -ScribeRawData -writeToHost
    Write-Host "Refer to log for more details."
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $($_.Exception) -LogMessageType ([LogMessageType]::Info) -ScribeRawData
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message $($_.ScriptStackTrace) -LogMessageType ([LogMessageType]::Info) -ScribeRawData
    Write-TraceWithTimestamp -logFullPath $gLogFullPath -message (Build-LogSuffix) -LogMessageType ([LogMessageType]::Info) -ScribeRawData
    Exit 1    
}
finally {
    Write-Host "$(Get-CRLF)Procedure complete, log: $gLogFullPath"
}