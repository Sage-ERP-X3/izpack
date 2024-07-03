package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.OsVersion;

public class InstallTypeConsolePanel extends AbstractConsolePanel implements ConsolePanel {

	private RegistryHandler rh;

	private RegistryHandlerX3 x3Handler;

	private Resources resources;

	public InstallTypeConsolePanel(PanelView<ConsolePanel> panel, InstallData installData, Resources resources, RegistryDefaultHandler handler) {
		super(panel);
		this.rh = handler != null ? handler.getInstance() : null;
		this.x3Handler = new RegistryHandlerX3(this.rh, installData);
		this.resources = resources;
	}

	private static final String SPEC_FILE_NAME = "productsSpec.txt";

	public static String ADX_NODE_TYPE = "component.node.type";
	public static String ADX_NODE_FAMILY = "component.node.family";

	@Override
	public boolean generateOptions(InstallData installData, Options options) {
		options.add(InstallData.MODIFY_INSTALLATION, installData.getVariable(InstallData.MODIFY_INSTALLATION));
		options.add("installpath", installData.getVariable("installpath"));

		return true;
	}

	@Override
	public boolean run(InstallData installData, Properties p) {
		String strType = p.getProperty(InstallData.MODIFY_INSTALLATION).trim();
		if (strType == null || "".equals(strType)) {
			// assume a normal install
			installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
		} else {
			if (Boolean.parseBoolean(strType)) {
				// is a modify type install
				installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");

				String strInstallpath = p.getProperty("installpath").trim();
				installData.setInstallPath(strInstallpath);

			} else {
				installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
			}
		}

		return true;
	}

	@Override
	public boolean run(InstallData installData, Console console){
		console.println("");
		console.println(resources.getString("InstallationTypePanel.info"));

		String i = "0";
		String[] choices = {"1","2","3"};

		while (!Arrays.asList(choices).contains(i)) {
			i = console.prompt(resources.getString("InstallationTypePanel.asktype"), choices);
		}
		int n = Integer.parseInt(i);
		if (n == 1) {
			installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
		} else if (n == 2) {
			installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");

			return chooseComponent(installData, console);
		} else {
			// want to exit
			return false;
		}

		return true;
	}

	public boolean chooseComponent(InstallData installData, Console console) {

		String strQuestion = resources.getString("InstallationTypePanel.askUpdatePath");

		List<String[]> installedProducts = loadListInstalledProducts(installData,console);

		console.println();
		String[] choices = new String[installedProducts.size()];
		for (int i = 0; i < installedProducts.size(); i++) {
			String[] product = (String[]) installedProducts.get(i);

			console.println(i + " - " + product[0]);
			choices[i] = ""+i;
			// console.println(i + " [" + (input.iSelectedChoice == i ? "x" : " ") + "] "
			// + (choice.strText != null ? choice.strText : ""));
		}

		console.println();

		while (true) {
			String strIn = console.prompt(strQuestion, choices);
			int j = -1;
			try {
				j = Integer.valueOf(strIn).intValue();
			} catch (Exception ex) {
			}
			if (j > -1 && j < installedProducts.size()) {
				String[] product = (String[]) installedProducts.get(j);

				installData.setInstallPath((String) product[2]);
				return true;
			} else {
				console.println(resources.getString("UserInputPanel.search.wrongselection.caption"));
			}
		}
	}

	private List<String[]> loadListInstalledProducts(InstallData installData, Console console) {
		if (x3Handler.needAdxAdmin()) {
			// Component is registered in adxadmin service
			// we can read pathes from adxinstalls.xml

			return loadListFromAdxadmin(installData, console);
		} else {
			if (OsVersion.IS_WINDOWS) {
				// we can read from registry
				return loadListFromRegistry(installData, console);

			} else {
				// maybe we can find a service ??

				console.println(resources.getString("installer.error")+"\n"+
						resources.getString("InstallationTypePanel.errNoCompFound"));

			}
		}
		return null;
	}

	private List<String[]> loadListFromRegistry(InstallData installData, Console console) {
		try {
			// need to process prefix

			String uninstallName = installData.getVariable("UNINSTALL_NAME");
			String uninstallKeySuffix = installData.getVariable("UninstallKeySuffix");
			String uninstallKeyPrefix = new String(uninstallName);
			ArrayList<String> uninstallKeyPrefixList = new ArrayList<String>();

			if (uninstallKeySuffix != null && !"".equals(uninstallKeySuffix)) {
				uninstallKeyPrefix = uninstallKeyPrefix.substring(0,
						uninstallKeyPrefix.length() - uninstallKeySuffix.length());
			}

			uninstallKeyPrefixList.add(uninstallKeyPrefix);

			// load additionnal prefix from resource

			try {
				InputStream input = resources.getInputStream(SPEC_FILE_NAME);

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
				ex.printStackTrace();
			}

			// load registry
			if (rh == null) {
				// nothing to do
				console.println(resources.getString("installer.error")+"\n"+
						resources.getString("InstallationTypePanel.errNoCompFound"));
			}

			String UninstallKeyName = RegistryHandler.UNINSTALL_ROOT; // "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
			int oldVal = rh.getRoot();
			rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);

			List<String> lstSubKeys = Arrays.asList(rh.getSubkeys(UninstallKeyName));

			ArrayList<String[]> tblComps = new ArrayList<String[]>();

			for (String uninstallKey : lstSubKeys) {
				for (String keyToSearchFor : uninstallKeyPrefixList) {
					if (uninstallKey.startsWith(keyToSearchFor)) {
						// read path from uninstall string :((
						String productPath = rh.getValue(UninstallKeyName + "\\" + uninstallKey, "UninstallString")
								.getStringData();
						productPath = productPath.substring(productPath.lastIndexOf("\"", productPath.length() - 2) + 1,
								productPath.length() - 29);

						String productVersion = rh.getValue(UninstallKeyName + "\\" + uninstallKey, "DisplayVersion")
								.getStringData();

						String name = uninstallKey;
						if (name.indexOf(" - ") > 0) {
							name = name.substring(name.indexOf(" - ") + 3);
						}

						// test for .installinformation existence

						File installInformation = new File(
								productPath + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);

						if (installInformation.exists()) {
							String[] elem = new String[] { name + " " + productVersion + " (" + productPath + ")", name,
									productPath, productVersion };
							tblComps.add(elem);
						}
					}
				}
			}

			// free RegistryHandler
			rh.setRoot(oldVal);
			return tblComps;

		} catch (Exception ex) {
			ex.printStackTrace();
			console.println(resources.getString("installer.error")+"\n"+
					resources.getString("InstallationTypePanel.errNoCompFound"));

		}
		return null;

	}

	private List<String[]> loadListFromAdxadmin(InstallData installData, Console console) {
		try {
			String strAdxAdminPath = "";

			if (OsVersion.IS_UNIX) {
				java.io.File adxadmFile = new java.io.File("/sage/adxadm");
				if (!adxadmFile.exists()) {
					adxadmFile = new java.io.File("/adonix/adxadm");
				}

				if (!adxadmFile.exists()) {
					// nothing to do
					console.println(resources.getString("installer.error")+"\n"+
							resources.getString("InstallationTypePanel.errNoCompFound"));
				}

				FileReader readerAdxAdmFile = new FileReader(adxadmFile);
				BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
				strAdxAdminPath = buffread.readLine();
			} else {
				if (rh == null) {
					// nothing to do
					console.println(resources.getString("installer.error")+"\n"+
							resources.getString("InstallationTypePanel.errNoCompFound"));
				}


				// test adxadmin déjà installé avec registry
				if (!x3Handler.adxadminProductRegistered()) {
					// nothing to do
					console.println(resources.getString("installer.error")+"\n"+
							resources.getString("InstallationTypePanel.errNoCompFound"));
				}

				String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
				int oldVal = rh.getRoot();
				rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
				if (!rh.valueExist(keyName, "ADXDIR"))
					keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
				if (!rh.valueExist(keyName, "ADXDIR")) {
					// nothing to do
					console.println(resources.getString("installer.error")+"\n"+
							resources.getString("InstallationTypePanel.errNoCompFound"));
				}

				// récup path
				strAdxAdminPath = rh.getValue(keyName, "ADXDIR").getStringData();

				// free RegistryHandler
				rh.setRoot(oldVal);
			}

			// check strAdxAdminPath
			if (strAdxAdminPath == null || "".equals(strAdxAdminPath)) {
				// nothing to do
				console.println(resources.getString("installer.error")+"\n"+
						resources.getString("InstallationTypePanel.errNoCompFound"));
			}

			java.io.File dirAdxDir = new java.io.File(strAdxAdminPath);

			if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) {
				// nothing to do
				console.println(resources.getString("installer.error")+"\n"+
						resources.getString("InstallationTypePanel.errNoCompFound"));
			}

			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(dirAdxDir.getAbsolutePath());
			strBuilder.append(dirAdxDir.separator);
			strBuilder.append("inst");
			strBuilder.append(dirAdxDir.separator);
			strBuilder.append("adxinstalls.xml");

			java.io.File fileAdxinstalls = new java.io.File(strBuilder.toString());

			if (!fileAdxinstalls.exists()) {
				// nothing to do
				console.println(resources.getString("installer.error")+"\n"+
						resources.getString("InstallationTypePanel.errNoCompFound"));
			}

			// we need to know type and family
			String strComponentType = installData.getVariable(ADX_NODE_TYPE);
			String strComponentFamily = installData.getVariable(ADX_NODE_FAMILY);

			// do nothing if we don't know family
			if (strComponentFamily == null) {
				console.println(resources.getString("installer.error")+"\n"+
						resources.getString("InstallationTypePanel.errNoCompFound"));
			}

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(fileAdxinstalls);

			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "//module[@family='" + strComponentFamily + "'";

			if (strComponentType != null)
				expression += " and @type='" + strComponentType + "'";

			expression += "]";

			NodeList nodeLst = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			ArrayList<String[]> tblComps = new ArrayList<String[]>();

			for (int i = 0; i < nodeLst.getLength(); i++) {
				Element moduleNode = (Element) nodeLst.item(i);
				String path = xPath.evaluate("./component." + strComponentFamily.toLowerCase() + ".path", moduleNode);
				String strversion = xPath.evaluate("./component." + strComponentFamily.toLowerCase() + ".version",
						moduleNode);
				String name = moduleNode.getAttribute("name");

				File installInformation = new File(
						path + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);

				if (installInformation.exists()) {
					String[] elem = new String[] { name + " " + strversion + " (" + path + ")", name, path,
							strversion };
					tblComps.add(elem);
				}

				else if (path.endsWith(File.separator + "tool")) {
					path = path.substring(0, path.length() - 5);
					installInformation = new File(
							path + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);

					if (installInformation.exists()) {
						String[] elem = new String[] { name + " " + strversion + " (" + path + ")", name, path,
								strversion };
						tblComps.add(elem);
					}
				}
			}

			return tblComps;

		} catch (Exception ex) {
			ex.printStackTrace();
			console.println(resources.getString("installer.error")+"\n"+
					resources.getString("InstallationTypePanel.errNoCompFound"));
		}

		return null;
	}
	

}
