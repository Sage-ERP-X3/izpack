package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.util.OsVersion;

/**
 * Class Helper about AdxAdmin, Registry, and xml file adxInstall.xml
 *
 * @author Franck DEPOORTERE
 */
public class RegistryHandlerX3 {
	private static final Logger logger = Logger.getLogger(RegistryHandlerX3.class.getName());

	private RegistryHandler registryHandler;
	private InstallData installData;
	public static String ADX_NODE_TYPE = "component.node.type";
	public static String ADX_NODE_FAMILY = "component.node.family";
	private static final String SPEC_FILE_NAME = "productsSpec.txt";
	private static final String LogPrefix = "RegistryHandlerX3 - ";

	public static final String AdxAdmFileWindows = "c:\\sage\\adxadm";
	public static final String AdxAdmFileLinux = "/sage/adxadm";

	public RegistryHandlerX3(RegistryHandler registryHandler, InstallData installData) {

		this.registryHandler = registryHandler;
		this.installData = installData;
	}

	public RegistryHandler getRegistryHandler() {
		return this.registryHandler;
	}

	public InstallData getInstallData() {
		return this.installData;
	}

	public boolean isAdminSetup() {

		String isAdxAdmin = this.installData != null ? this.installData.getVariable("is-adxadmin") : null;
		if (isAdxAdmin != null && isAdxAdmin.equalsIgnoreCase("true")) {

			return true;
		}
		return false;
	}

	public boolean needAdxAdmin() {

		String needAdxAdmin = this.installData != null ? this.installData.getVariable("need-adxadmin") : null;
		if (needAdxAdmin != null && needAdxAdmin.equalsIgnoreCase("true")) {

			return true;
		}
		return false;
	}

	/**
	 * Check if AdxAdmin is installed Check the Registry if Windows
	 *
	 * @return
	 * @throws NativeLibException
	 */
	public boolean adxadminProductRegistered() throws NativeLibException {

		String adxAdminPath = getAdxAdminDirPath();
		logger.log(Level.FINE, LogPrefix + "adxadminProductRegistered. adxAdminPath: " + adxAdminPath + "  result: "
				+ (adxAdminPath != null));
		return (adxAdminPath != null);
	}

	/**
	 *
	 * @return
	 * @throws NativeLibException
	 */
	public String getAdxAdminDirPath() throws NativeLibException {
		if (OsVersion.IS_UNIX) {
			return getAdxAdminPathUnix();
		}
		return getAdxAdminPathWin();
	}

	/**
	 * String keyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN"; String
	 * keyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
	 *
	 * @return C:\Sage\SafeX3\ADXADMIN
	 * @throws NativeLibException
	 */
	private String getAdxAdminPathWin() throws NativeLibException {

		String adxAdminPath = null;

		java.io.File adxadmFile = getWinPath();
		if (adxadmFile != null)
			adxAdminPath = readAdxAdmFile(adxadmFile);

		if (adxAdminPath != null) {
			logger.log(Level.FINE, LogPrefix + "getAdxAdminPathWin. adxAdminPath from " + adxadmFile.getAbsolutePath()
					+ ": '" + adxAdminPath + "'");
			return adxAdminPath;
		}

		int oldVal = this.registryHandler.getRoot();
		this.registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
		boolean exists64bits = this.registryHandler.keyExist(AdxCompHelper.ADXADMIN_REG_KeyName64Bits);
		// boolean exists32bits =
		// this.registryHandler.keyExist(AdxCompHelper.ADXADMIN_REG_KeyName32Bits);

		if (exists64bits) {
			adxAdminPath = this.registryHandler.getValue(AdxCompHelper.ADXADMIN_REG_KeyName64Bits, "ADXDIR")
					.getStringData();
		}
		// 32 bits is deprecated.
		// } else if (exists32bits){
		// adxAdminPath = this.registryHandler
		// .getValue(AdxCompHelper.ADXADMIN_REG_KeyName32Bits,
		// "ADXDIR").getStringData();
		// }
		this.registryHandler.setRoot(oldVal);

		logger.log(Level.FINE, LogPrefix + "getAdxAdminPathWin. adxAdminPath from registry: '" + adxAdminPath + "'");
		return adxAdminPath;
	}

	private String getAdxAdminPathUnix() {
		java.io.File adxadmFile = getUnixPath();
		return readAdxAdmFile(adxadmFile);
	}

	private java.io.File getWinPath() {
		String path = RegistryHandlerX3.AdxAdmFileWindows; // "c:\\sage\\adxadm";
		return getFile(path);
	}

	private java.io.File getUnixPath() {
		String path = RegistryHandlerX3.AdxAdmFileLinux; // "/sage/adxadm";
		return getFile(path);
	}

	private java.io.File getFile(String path) {
		java.io.File adxadmFile = new java.io.File(path);
		if (!adxadmFile.exists()) {
			return null;
		}
		return adxadmFile;
	}

	private String readAdxAdmFile(java.io.File adxadmFile) {
		String adxAdminPath = null;

		if (adxadmFile == null) {
			logger.log(Level.WARNING, LogPrefix + "readAdxAdmFile. Cannot open NULL file");
			return null;
		}

		FileReader readerAdxAdmFile;
		try {
			logger.log(Level.FINE, LogPrefix + "readAdxAdmFile. Reading file '" + adxadmFile.getAbsolutePath() + "'");
			readerAdxAdmFile = new FileReader(adxadmFile);
			BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
			adxAdminPath = buffread.readLine();
			if (adxAdminPath != null)
				adxAdminPath = adxAdminPath.trim();
			buffread.close();
			logger.log(Level.FINE, LogPrefix + "readAdxAdmFile. adxAdminPath: '" + adxAdminPath + "'");
		} catch (IOException e) {
			logger.log(Level.WARNING, LogPrefix + "readAdxAdmFile. Cannot open file '" + adxadmFile + "'");
			e.printStackTrace();
		}
		return adxAdminPath;
	}

	public HashMap<String, String[]> loadComponentsList() throws Exception {

		if (needAdxAdmin()) {
			return loadListFromAdxadmin();
		} else if (OsVersion.IS_WINDOWS) {
			return loadListFromRegistry();
		} else {
			// ResourcesHelper resourcesHelper = new ResourcesHelper(installData,
			// this.resources);
			// throw new Exception(resourcesHelper.getCustomString("installer.error"),
			// resourcesHelper.getCustomString("InstallationTypePanel.errNoCompFound"));
			// maybe we can find a service ??
			throw new Exception("installer.error" + "InstallationTypePanel.errNoCompFound");
		}
	}

	private HashMap<String, String[]> loadListFromAdxadmin() {

		HashMap<String, String[]> lstCompPropsParam = new HashMap<>();

		try {
			String adxAdminPath = this.getAdxAdminDirPath();
			if (adxAdminPath == null || "".equals(adxAdminPath)) {
				// nothing to do
				logger.log(Level.WARNING,
						LogPrefix + "loadListFromAdxadmin error while retrieve AdxAdminDirPath=" + adxAdminPath);
				return lstCompPropsParam;
			}

			java.io.File dirAdxDir = new java.io.File(adxAdminPath);
			if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) {
				// nothing to do
				logger.log(Level.WARNING, LogPrefix + "loadListFromAdxadmin error while reading AdxAdminDirPath="
						+ dirAdxDir.getAbsolutePath());
				return lstCompPropsParam;
			}

			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(dirAdxDir.getAbsolutePath());
			strBuilder.append(File.separator);
			strBuilder.append("inst");
			strBuilder.append(File.separator);
			strBuilder.append("adxinstalls.xml");

			java.io.File fileAdxinstalls = new java.io.File(strBuilder.toString());

			if (!fileAdxinstalls.exists()) {
				// nothing to do
				logger.log(Level.WARNING, LogPrefix + "loadListFromAdxadmin error - File "
						+ fileAdxinstalls.getAbsolutePath() + " doesn't exist.");
				return lstCompPropsParam;
			}

			// we need to know type and family
			String strComponentType = this.getInstallData().getVariable(ADX_NODE_TYPE);
			String strComponentFamily = this.getInstallData().getVariable(ADX_NODE_FAMILY);

			// do nothing if we don't know family
			if (strComponentFamily == null)
				return lstCompPropsParam;

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(fileAdxinstalls);

			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "//module[@family='" + strComponentFamily + "'";

			if (strComponentType != null && !strComponentType.isBlank())
				expression += " and @type='" + strComponentType + "'";

			expression += "]";

			NodeList nodeLst = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			// NodeList nodeLst = doc.getElementsByTagName("module");

			for (int i = 0; i < nodeLst.getLength(); i++) {

				Element moduleNode = (Element) nodeLst.item(i);
				String path = xPath.evaluate("./component." + strComponentFamily.toLowerCase() + ".path", moduleNode);
				String strversion = xPath.evaluate("./component." + strComponentFamily.toLowerCase() + ".version",
						moduleNode);
				String name = moduleNode.getAttribute("name");

				File installInformation = new File(path + File.separator + InstallData.INSTALLATION_INFORMATION);

				if (installInformation.exists()) {
					String key = name + " " + strversion + " (" + path + ")";
					// listItemsParam.addElement(key);
					lstCompPropsParam.put(key, new String[] { name, path, strversion });
					// listItems.addElement(new String[] {moduleNode.getAttribute("name")+" "+
					// strversion +" ("+path+")", path, strversion});

				} else if (path.endsWith(File.separator + "tool")) {
					path = path.substring(0, path.length() - 5);
					installInformation = new File(path + File.separator + InstallData.INSTALLATION_INFORMATION);

					if (installInformation.exists()) {
						String key = name + " " + strversion + " (" + path + ")";
						// listItemsParam.addElement(key);
						lstCompPropsParam.put(key, new String[] { name, path, strversion });
						// listItems.addElement(new String[] {moduleNode.getAttribute("name")+" "+
						// strversion +" ("+path+")", path, strversion});
					}
				}
			}
		} catch (Exception ex) {
			logger.log(Level.WARNING, LogPrefix + "loadListFromAdxadmin error : " + ex);
			ex.printStackTrace();
		}
		return lstCompPropsParam;

	}

	private HashMap<String, String[]> loadListFromRegistry() {

		HashMap<String, String[]> lstCompPropsParam = new HashMap<>();
		try {
			// need to process prefix

			String uninstallName = this.installData.getVariable("UNINSTALL_NAME");
			String uninstallKeySuffix = this.installData.getVariable("UninstallKeySuffix");
			String uninstallKeyPrefix = new String(uninstallName);
			ArrayList<String> uninstallKeyPrefixList = new ArrayList<>();

			if (uninstallKeySuffix != null && !"".equals(uninstallKeySuffix)) {
				uninstallKeyPrefix = uninstallKeyPrefix.substring(0,
						uninstallKeyPrefix.length() - uninstallKeySuffix.length());
			}

			uninstallKeyPrefixList.add(uninstallKeyPrefix);

			// load additionnal prefix from resource

			try {
				InputStream input = new ResourceManager().getInputStream(SPEC_FILE_NAME);

				if (input != null) {

					BufferedReader reader = new BufferedReader(new InputStreamReader(input));
					StringBuilder out = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						uninstallKeyPrefixList.add(line.trim());
					}
					reader.close();
				}

			} catch (Exception ex) {
				logger.log(Level.FINE, LogPrefix + "Error while loading " + SPEC_FILE_NAME + " : " + ex);
			}

			// load registry
			RegistryHandler rh = registryHandler; // RegistryDefaultHandler.getInstance();
			if (rh == null) {
				// nothing to do
				return lstCompPropsParam;
			}

			// rh.verify(idata);

			String UninstallKeyName = RegistryHandler.UNINSTALL_ROOT; // "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
			int oldVal = rh.getRoot();
			rh.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);

			List<String> lstSubKeys = Arrays.asList(rh.getSubkeys(UninstallKeyName));

			for (String uninstallKey : lstSubKeys) {

				for (String keyToSearchFor : uninstallKeyPrefixList) {
					if (uninstallKey.startsWith(keyToSearchFor)) {
						// read path from uninstall string :((
						String productPath = null;
						try {
							productPath = rh.getValue(UninstallKeyName + "\\" + uninstallKey, "UninstallString")
									.getStringData();
						} catch (Exception ex) {
							continue;
						}

						String productVersion = null;
						try {
							productVersion = rh.getValue(UninstallKeyName + "\\" + uninstallKey, "DisplayVersion")
									.getStringData();
						} catch (Exception ex) {
							continue;
						}

						productPath = productPath.substring(productPath.lastIndexOf("\"", productPath.length() - 2) + 1,
								productPath.length() - 29);
						String name = uninstallKey;
						if (name.indexOf(" - ") > 0) {
							name = name.substring(name.indexOf(" - ") + 3);
						}

						File installInformation = new File(
								productPath + File.separator + InstallData.INSTALLATION_INFORMATION);

						if (installInformation.exists()) {
							String key = name + " " + productVersion + " (" + productPath + ")";
							// listItemsParam.addElement(key);
							// listItems.addElement(new String[] {name+""+ productVersion +"
							// ("+productPath+")", productPath, productVersion});
							lstCompPropsParam.put(key, new String[] { name, productPath, productVersion });
						}

					}
				}
			}

			// free RegistryHandler
			rh.setRoot(oldVal);

		} catch (Exception ex) {
			logger.log(Level.ALL, LogPrefix + "loadListFromRegistry error : " + ex);
		}
		return lstCompPropsParam;

	}

}
