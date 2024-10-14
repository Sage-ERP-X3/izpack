package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.util.OsVersion;


public class WinServiceUserDataValidator implements DataValidator {

    private final static String errLogon = "errwinsrvaccount";
    private final static String errLogonService = "warnwinsrvaccount";
    private final static String warnLogonServiceChanged = "warnLogonServiceChanged";
    private String errorMsg = errLogon;
	private static Logger logger = Logger.getLogger(WinServiceUserDataValidator.class.getName());

	@Override
	public boolean getDefaultAnswer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getErrorMessageId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWarningMessageId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status validateData(InstallData adata) {

        if (!OsVersion.IS_WINDOWS) return Status.OK;
        
        Status bReturn = Status.ERROR;
        try
        {
        
            String userName = adata.getVariable("syracuse.winservice.username");
            String passWord = adata.getVariable("syracuse.winservice.password");
            String strDomain = ".";
            String bUseDomain = "true";
            
            // check domain
            if (userName.contains("\\"))
            {
                strDomain = userName.substring(0, userName.indexOf("\\"));
                userName = userName.substring(userName.indexOf("\\")+1);
            }
            else if (userName.contains("@"))
            {
                strDomain = null;
            }
            else
            {
                // local database
                bUseDomain = "false";
            }
            
             HANDLEByReference phToken = new  HANDLEByReference();

            if (! Advapi32.INSTANCE.LogonUser(userName, strDomain, passWord, WinBase.LOGON32_LOGON_NETWORK, WinBase.LOGON32_PROVIDER_DEFAULT, phToken))
            {
                throw new LastErrorException(Kernel32.INSTANCE.GetLastError());
            }

            Kernel32.INSTANCE.CloseHandle(phToken.getValue());
if (! Advapi32.INSTANCE.LogonUser(userName, strDomain, passWord, WinBase.LOGON32_LOGON_SERVICE, WinBase.LOGON32_PROVIDER_DEFAULT, phToken))
            {
                bReturn = Status.ERROR;
                errorMsg = errLogonService;
                return bReturn;
            }
            else
            {
                bReturn = Status.OK;
            }

            Kernel32.INSTANCE.CloseHandle(phToken.getValue());
            /*
            HANDLEByReference phToken = new  HANDLEByReference();
            if (! Advapi32.INSTANCE.LogonUser(userName, strDomain, passWord, WinBase.LOGON32_LOGON_NETWORK, WinBase.LOGON32_PROVIDER_DEFAULT, phToken))
            {
                throw new LastErrorException(Kernel32.INSTANCE.GetLastError());
            }

          
            Kernel32.INSTANCE.CloseHandle(phToken.getValue());            
            if (! Advapi32.INSTANCE.LogonUser(userName, strDomain, passWord, WinBase.LOGON32_LOGON_SERVICE, WinBase.LOGON32_PROVIDER_DEFAULT, phToken))
            {
                bReturn = Status.ERROR;
                errorMsg = errLogonService;
                return bReturn;
            }
            else
            {
                bReturn = Status.OK;
            }
            Kernel32.INSTANCE.CloseHandle(phToken.getValue());
*/
            //userName = strDomain + "\\" + userName;
            //adata.setVariable("syracuse.winservice.username", userName);
            adata.setVariable("syracuse.winservice.usedomain", bUseDomain);
            if (strDomain != null && ".".equals(strDomain))
            {
                // local database
                adata.setVariable("syracuse.winservice.username", userName);
            }

String passwordBase64 = adata.getVariable("syracuse.winservice.pwdbase64")
if ( passwordBase64 != null && passwordBase64.equals("true", IgnoreCase) ) {
    String encodedString = Base64.getEncoder().encodeToString( "base64:" passWord.getBytes());
  adata.setVariable("syracuse.winservice.password", encodedString);
}

            bReturn = Status.OK;
        }
        catch (Exception ex)
        {
        	logger.log(Level.WARNING, ex.getMessage());
            bReturn = Status.ERROR; 
        }

        return bReturn;
    }

}
