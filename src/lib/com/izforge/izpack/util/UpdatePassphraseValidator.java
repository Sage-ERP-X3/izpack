/**
 *
 */
package com.izforge.izpack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.util.os.MoreAdvApi32;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinBase.STARTUPINFO;
import com.sun.jna.platform.win32.WinDef.WORD;
import com.sun.jna.ptr.IntByReference;

import sun.security.x509.X500Name;


/**
 * @author apozzo
 *
 */
public class UpdatePassphraseValidator implements DataValidator
{

	private String strMessage = "";
	public static final String strMessageId = "messageid";
	public static final String strMessageValue = "message.oldvalue"; // not to be stored
	public static final int STILL_ACTIVE = 259;

	/* (non-Javadoc)
	 * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.izpack.installer.AutomatedInstallData)
	 */
	@Override
	public Status validateData(AutomatedInstallData adata)
	{
		Status sreturn = Status.OK;

		boolean updateMode = false;
		if (adata.getVariable("MODIFY.IZPACK.INSTALL")!=null) {
			updateMode = adata.getVariable("MODIFY.IZPACK.INSTALL").equalsIgnoreCase("true");
		}
		if (adata.getVariable("syracuse.certificate.install")!=null) {
			adata.getVariable("syracuse.certificate.install").equalsIgnoreCase("true");
		}


		if (OsVersion.IS_WINDOWS && !updateMode )
		{

			String userName = adata.getVariable("syracuse.winservice.username");
			final String passWord = adata.getVariable("syracuse.winservice.password");
			String strDomain = ".";

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


			final WString nullW = null;
			final PROCESS_INFORMATION processInformation = new PROCESS_INFORMATION();
			final STARTUPINFO startupInfo = new STARTUPINFO();
			startupInfo.dwFlags = 1;
			startupInfo.wShowWindow = new WORD(0);


			final String strPassphrasePath = adata.getVariable("INSTALL_PATH")+"\\syracuse"; //${INSTALL_PATH}${FILE_SEPARATOR}syracuse
			final String strCertsDir = adata.getVariable("syracuse.dir.certs"); // syracuse.dir.certs

			final String certCreate = adata.getVariable("syracuse.certificate.install");

			//String strHOST_NAME = adata.getVariable("syracuse.certificate.hostname");
			final String strHOST_NAME = adata.getVariable("HOST_NAME").toLowerCase();
			String strServerPassphrase = adata.getVariable("syracuse.certificate.serverpassphrase"); //syracuse.certificate.serverpassphrase



			try
			{


				if (certCreate.equals("cert2"))
				{
					final CertificateFactory factory = CertificateFactory.getInstance("X.509");
					final InputStream inPemCertFile = new FileInputStream(adata.getVariable("syracuse.ssl.certfile"));
					final X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);

					new X500Name(cert.getSubjectX500Principal().getName());

					//strHOST_NAME=x500Name.getCommonName().toLowerCase();

					strServerPassphrase = adata.getVariable("syracuse.ssl.pemkeypassword");
				}

				final String strPassPhraseFile = strCertsDir+"\\"+strHOST_NAME+"\\"+strHOST_NAME+".pwd";

				//delete old passphrase ?
				final File oldPassphrase = new File (strPassPhraseFile);
				if (oldPassphrase.exists() && !oldPassphrase.delete()) {
					throw new Exception(strPassPhraseFile);
				}

				final String hexstrServerPassphrase = StringTool.asciiToHex(strServerPassphrase);

				final File tempFile = new File(strPassphrasePath+"\\tmpcmd.cmd");
				tempFile.deleteOnExit();
				final PrintWriter printWriter = new PrintWriter(new FileOutputStream(tempFile), true);
				printWriter.println ("ping -n 5 127.0.0.1>NUL");

				printWriter.println ("\""+strPassphrasePath+"\\passphrasehex.cmd\" \""+hexstrServerPassphrase+"\" 1>out.log 2>err.log");
				printWriter.println ("if errorlevel 1 exit /B 1");
				printWriter.close ();
				tempFile.getCanonicalPath();

				boolean result2 = MoreAdvApi32.INSTANCE.CreateProcessWithLogonW
						(new WString(userName),                         // user
								strDomain==null?nullW:new WString(strDomain),                                           // domain , null if local
										new WString(passWord),                         // password
										MoreAdvApi32.LOGON_WITH_PROFILE,                 // dwLogonFlags
										nullW,                                           // lpApplicationName
										new WString(tempFile.getCanonicalPath()),   // command line
										//new WString("c:\\UnxUtils\\usr\\local\\wbin\\sleep.exe 100"),   // command line
										MoreAdvApi32.CREATE_UNICODE_ENVIRONMENT,                 // dwCreationFlags
										null,                                            // lpEnvironment
										new WString(strPassphrasePath),                   // directory
										startupInfo,
										processInformation);

				if (!result2)
				{
					final int error = Kernel32.INSTANCE.GetLastError();
					//System.out.println("OS error #" + error);
					//System.out.println(Kernel32Util.formatMessageFromLastErrorCode(error));

					strMessage = "OS error #" + error + " - " + Kernel32Util.formatMessageFromLastErrorCode(error);

					adata.setVariable(strMessageValue, strMessage);
					return Status.WARNING;

				}

				// join the process ?
				boolean bFinished = false;
				int loop = 0;

				while (!bFinished)
				{

					final IntByReference lpExitCode = new IntByReference(9999);
					result2 = Kernel32.INSTANCE.GetExitCodeProcess(processInformation.hProcess, lpExitCode) ;

					if (!result2)
					{
						final int error = Kernel32.INSTANCE.GetLastError();
						//System.out.println("OS error #" + error);
						//System.out.println(Kernel32Util.formatMessageFromLastErrorCode(error));

						strMessage = "OS error #" + error + " - " + Kernel32Util.formatMessageFromLastErrorCode(error);

						adata.setVariable(strMessageValue, strMessage);
						return Status.WARNING;

					}

					if (lpExitCode.getValue() != STILL_ACTIVE)
					{
						lpExitCode.getValue();
						bFinished = true;
					}

					loop+=1;

					if (loop > 30)
					{
						// more than 30 s !
						strMessage = "Error # Could not update passphrase ! ("+strPassPhraseFile+")";

						adata.setVariable(strMessageValue, strMessage);
						return Status.WARNING;

					}

					// process is not finished
					// wait a little
					Thread.sleep(1000);
				}

				// read err and out
				final String strErr = new String(Files.readAllBytes(Paths.get(strPassphrasePath+"\\err.log")));
				final File strOutFile = new File (strPassphrasePath+"\\out.log");
				final File strErrFile = new File (strPassphrasePath+"\\err.log");

				strOutFile.delete ();
				strErrFile.delete ();

				// test for passphrase ?
				if (!oldPassphrase.exists())
				{
					strMessage = "Error # Passphrase update failed ! ("+strPassPhraseFile+")\r\n"+strErr;

					adata.setVariable(strMessageValue, strMessage);
					return Status.WARNING;
				}


			}
			catch (final Exception ex)
			{
				Debug.trace(ex);
				Debug.trace(ex.getMessage());

				strMessage = "OS error #" + ex.getMessage();

				adata.setVariable(strMessageValue, strMessage);
				sreturn = Status.WARNING;
			}
		}



		return sreturn;
	}

	/* (non-Javadoc)
	 * @see com.izforge.izpack.installer.DataValidator#getErrorMessageId()
	 */
	@Override
	public String getErrorMessageId()
	{

		return strMessageId;
	}

	/* (non-Javadoc)
	 * @see com.izforge.izpack.installer.DataValidator#getWarningMessageId()
	 */
	@Override
	public String getWarningMessageId()
	{

		return strMessageId;
	}

	/* (non-Javadoc)
	 * @see com.izforge.izpack.installer.DataValidator#getDefaultAnswer()
	 */
	@Override
	public boolean getDefaultAnswer()
	{
		// by default continue
		return true;
	}

}
