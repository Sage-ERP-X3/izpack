package com.sage.izpack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.util.OsVersion;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinBase.STARTUPINFO;
import com.sun.jna.platform.win32.WinDef.WORD;
import com.sun.jna.ptr.IntByReference;

public class UpdatePassphraseValidator implements DataValidator {

	private String strMessage = "";
	public static final String strMessageId = "messageid";
	public static final String strMessageValue = "message.oldvalue"; // not to be stored
	public static final int STILL_ACTIVE = 259;

	@Override
	public Status validateData(InstallData adata) {
		Status sreturn = Status.OK;

		boolean updateMode = ModifyInstallationUtil.get(adata);
		boolean createCertificate = true;

		if (adata.getVariable("syracuse.certificate.install") != null)
			createCertificate = adata.getVariable("syracuse.certificate.install").equalsIgnoreCase("true");

		if (OsVersion.IS_WINDOWS && !updateMode) {
			String userName = adata.getVariable("syracuse.winservice.username");
			String passWord = adata.getVariable("syracuse.winservice.password");
			String strDomain = ".";

			// check domain
			if (userName.contains("\\")) {
				strDomain = userName.substring(0, userName.indexOf("\\"));
				userName = userName.substring(userName.indexOf("\\") + 1);
			} else if (userName.contains("@")) {
				strDomain = null;
			}

			WString nullW = null;
			PROCESS_INFORMATION processInformation = new PROCESS_INFORMATION();
			STARTUPINFO startupInfo = new STARTUPINFO();
			startupInfo.dwFlags = 1;
			startupInfo.wShowWindow = new WORD(0);

			String strPassphrasePath = adata.getVariable("INSTALL_PATH") + "\\syracuse"; // ${INSTALL_PATH}${FILE_SEPARATOR}syracuse
			String strCertsDir = adata.getVariable("syracuse.dir.certs"); // syracuse.dir.certs

			Boolean certCreate = Boolean.valueOf(adata.getVariable("syracuse.certificate.install"));

			// String strHOST_NAME = adata.getVariable("syracuse.certificate.hostname");
			String strHOST_NAME = adata.getVariable("HOST_NAME").toLowerCase();
			String strServerPassphrase = adata.getVariable("syracuse.certificate.serverpassphrase"); // syracuse.certificate.serverpassphrase

			try {
				if (!certCreate) {
					// CertificateFactory factory = CertificateFactory.getInstance("X.509");
					// InputStream inPemCertFile = new
					// FileInputStream(adata.getVariable("syracuse.ssl.certfile"));
					// X509Certificate cert = (X509Certificate)
					// factory.generateCertificate(inPemCertFile);
					// Java 8
					// X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName());
					// strHOST_NAME=x500Name.getCommonName().toLowerCase();

					strServerPassphrase = adata.getVariable("syracuse.ssl.pemkeypassword");
				}

				String strPassPhraseFile = strCertsDir + "\\" + strHOST_NAME + "\\" + strHOST_NAME + ".pwd";

				// delete old passphrase ?
				File oldPassphrase = new File(strPassPhraseFile);
				if (oldPassphrase.exists() && !oldPassphrase.delete())
					throw new Exception(strPassPhraseFile);

				String hexstrServerPassphrase = asciiToHex(strServerPassphrase);

				File tempFile = new File(strPassphrasePath + "\\tmpcmd.cmd");
				tempFile.deleteOnExit();
				PrintWriter printWriter = new PrintWriter(new FileOutputStream(tempFile), true);
				printWriter.println("ping -n 5 127.0.0.1>NUL");

				printWriter.println("\"" + strPassphrasePath + "\\passphrasehex.cmd\" \"" + hexstrServerPassphrase
						+ "\" 1>out.log 2>err.log");
				printWriter.println("if errorlevel 1 exit /B 1");
				printWriter.close();
				String strcommand = "/C /E:ON \"" + tempFile.getCanonicalPath() + "\" \"" + strServerPassphrase + "\"";

				boolean result2 = MoreAdvApi32.INSTANCE.CreateProcessWithLogonW(new WString(userName), // user
						(strDomain == null) ? nullW : new WString(strDomain), // domain , null if local
						new WString(passWord), // password
						MoreAdvApi32.LOGON_WITH_PROFILE, // dwLogonFlags
						nullW, // lpApplicationName
						new WString(tempFile.getCanonicalPath()), // command line
						// new WString("c:\\UnxUtils\\usr\\local\\wbin\\sleep.exe 100"), // command line
						MoreAdvApi32.CREATE_UNICODE_ENVIRONMENT, // dwCreationFlags
						null, // lpEnvironment
						new WString(strPassphrasePath), // directory
						startupInfo, processInformation);

				if (!result2) {
					int error = Kernel32.INSTANCE.GetLastError();
					// System.out.println("OS error #" + error);
					// System.out.println(Kernel32Util.formatMessageFromLastErrorCode(error));

					strMessage = "OS error #" + error + " - " + Kernel32Util.formatMessageFromLastErrorCode(error);

					adata.setVariable(strMessageValue, strMessage);
					return Status.WARNING;

				}

				// join the process ?
				boolean bFinished = false;
				int loop = 0;

				while (!bFinished) {

					IntByReference lpExitCode = new IntByReference(9999);
					result2 = Kernel32.INSTANCE.GetExitCodeProcess(processInformation.hProcess, lpExitCode);

					if (!result2) {
						int error = Kernel32.INSTANCE.GetLastError();
						// System.out.println("OS error #" + error);
						// System.out.println(Kernel32Util.formatMessageFromLastErrorCode(error));

						strMessage = "OS error #" + error + " - " + Kernel32Util.formatMessageFromLastErrorCode(error);

						adata.setVariable(strMessageValue, strMessage);
						return Status.WARNING;

					}

					if (lpExitCode.getValue() != STILL_ACTIVE) {
						// process has finished
						// how to interpret exit code ?
						int nexitCode = lpExitCode.getValue();
						bFinished = true;
					}

					loop += 1;

					if (loop > 30) {
						// more than 30 s !
						strMessage = "Error # Could not update passphrase ! (" + strPassPhraseFile + ")";

						adata.setVariable(strMessageValue, strMessage);
						return Status.WARNING;

					}

					// process is not finished
					// wait a little
					Thread.sleep(1000);
				}

				// read err and out
				String strErr = new String(Files.readAllBytes(Paths.get(strPassphrasePath + "\\err.log")));
				File strOutFile = new File(strPassphrasePath + "\\out.log");
				File strErrFile = new File(strPassphrasePath + "\\err.log");

				strOutFile.delete();
				strErrFile.delete();

				// test for passphrase ?
				if (!oldPassphrase.exists()) {
					strMessage = "Error # Passphrase update failed ! (" + strPassPhraseFile + ")\r\n" + strErr;

					adata.setVariable(strMessageValue, strMessage);
					return Status.WARNING;
				}
			} catch (Exception ex) {
				ex.printStackTrace(System.err);

				strMessage = "OS error #" + ex.getMessage();

				adata.setVariable(strMessageValue, strMessage);
				sreturn = Status.WARNING;
			}
		}

		return sreturn;
	}

	@Override
	public String getErrorMessageId() {

		return strMessageId;
	}

	@Override
	public String getWarningMessageId() {

		return strMessageId;
	}

	@Override
	public boolean getDefaultAnswer() {
		// by default continue
		return true;
	}

	private static String asciiToHex(String asciiValue) {
		char[] chars = asciiValue.toCharArray();
		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString((int) chars[i]));
		}
		return hex.toString();
	}
}
