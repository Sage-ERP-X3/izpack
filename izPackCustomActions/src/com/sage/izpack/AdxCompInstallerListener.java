package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.AbstractInstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.panels.packs.PacksModel;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.helper.SpecHelper;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.sage.izpack.XMLHelper;

/*
 * Manage XML file adxinstalls.xml
 * 
 * @author Franck DEPOORTERE
 */
public class AdxCompInstallerListener extends AbstractInstallerListener implements CleanupClient {

	private static final Logger logger = Logger.getLogger(AdxCompInstallerListener.class.getName());

	private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";
	private static final String ADX_INSTALL_FILENAME = "adxinstalls.xml";
	/*
	 * SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN
	 */
	public static final String ADXADMIN_REG_KeyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
	/*
	 * SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN
	 */
	public static final String ADXADMIN_REG_KeyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";

	private SpecHelper specHelper = null;
	private Resources resources = null;
	private VariableSubstitutor variableSubstitutor;

	private com.izforge.izpack.api.data.InstallData installData;
	private RegistryHandler registryHandler;

	public AdxCompInstallerListener(com.izforge.izpack.api.data.InstallData installData,
			VariableSubstitutor variableSubstitutor, Resources resources, RegistryDefaultHandler handler) {

		super();
		this.installData = installData;
		this.variableSubstitutor = variableSubstitutor;
		this.resources = resources;
		this.registryHandler = handler.getInstance();
	}

	/**
	 * Remove all registry entries on failed installation
	 */
	public void cleanUp() {
		// installation was not successful now rewind adxinstalls.xml changes
	}

	public void beforePacks(List<Pack> packs) {
		super.beforePacks(packs);

		this.specHelper = new SpecHelper(this.resources);
		try {
			this.specHelper.readSpec(SPEC_FILE_NAME);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (this.installData.getInfo().isReadInstallationInformation()) {

			InstallationInformationHelper.readInformation(this.installData);

		} else {
			logger.log(Level.FINE, "AdxCompInstallerListener.beforePacks  ReadInstallationInformation: "
					+ this.installData.getInfo().isReadInstallationInformation());
		}

	}

	@Override
	public void afterPacks(List<Pack> packs, ProgressListener listener) {
		// here we need to update adxinstalls.xml

		try {
			String strAdxAdminPath = getAdxAdminPath();
			// check strAdxAdminPath
			if (strAdxAdminPath == null || "".equals(strAdxAdminPath))
				throw new Exception(
						ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));

			java.io.File dirAdxDir = new java.io.File(strAdxAdminPath);
			if (!dirAdxDir.exists() || !dirAdxDir.isDirectory())
				// throw new Exception(langpack.getString("adxadminParseError"));
				throw new Exception(
						ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));

			java.io.File fileAdxinstalls = getAdxInstallFile(dirAdxDir);
			logger.log(Level.FINE, "AdxCompInstallerListener.afterPacks  Reading XML file fileAdxinstalls: "
					+ fileAdxinstalls.getAbsolutePath());

			Document adxInstallXmlDoc = getXml(fileAdxinstalls);

			// adxinstalls.xml read or created
			// il faut ajouter le module
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", 4);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			IXMLElement elemSpec = this.specHelper.getSpec(); // AdxCompSpec.xml
			IXMLElement moduleSpec = elemSpec.getFirstChildNamed("module");
			VariableSubstitutor substitutor = new VariableSubstitutorImpl(this.installData.getVariables());
			String moduleName = substitutor.substitute(moduleSpec.getAttribute("name"), SubstitutionType.TYPE_PLAIN);
			String moduleFamily = substitutor.substitute(moduleSpec.getAttribute("family"),
					SubstitutionType.TYPE_PLAIN);
			String moduleType = substitutor.substitute(moduleSpec.getAttribute("type"), SubstitutionType.TYPE_PLAIN);
			if (moduleType == null)
				moduleType = "";
			String version = moduleSpec.getFirstChildNamed("component." + moduleFamily.toLowerCase() + ".version")
					.getContent();

			// String moduleName = this.installData.getVariable("component.node.name");
			// String moduleFamily = this.installData.getVariable("component.node.family");
			// // REPORT
			// String moduleType = this.installData.getVariable("component.node.type");

			boolean modifyinstallation = Boolean.valueOf(installData.getVariable(InstallData.MODIFY_INSTALLATION));

			logger.log(Level.FINE, "AdxCompInstallerListener.afterPacks  moduleName: " + moduleName + " moduleFamily: "
					+ moduleFamily + " modifyinstallation: " + modifyinstallation);

			if (modifyinstallation) {

				modifyReportModule(adxInstallXmlDoc, moduleName, moduleFamily, moduleType);

			} else {

				createModule(adxInstallXmlDoc, moduleSpec, substitutor, moduleName, moduleFamily, moduleType);
			}

			logger.log(Level.FINE, "AdxCompInstallerListener.afterPacks  saving XML xdoc:"
					+ adxInstallXmlDoc.getDocumentElement().getNodeName());
			saveXml(fileAdxinstalls, adxInstallXmlDoc, transformer);
			logger.log(Level.FINE, "AdxCompInstallerListener.afterPacks  XML doc saved");

		} catch (Exception e) {
			e.printStackTrace();
		}
		// throw new Exception(e.getMessage());
	}

	// <module name="EDTSRV" family="REPORT" type="">
	// <component.report.installstatus>update</component.report.installstatus>
	// <component.report.path>c:\Sage\SafeX3\EDTV2\EDTSRVFRDEP</component.report.path>
	// <component.report.platform>WIN64</component.report.platform>
	// <component.report.servername>po027493.sagefr.adinternal.com</component.report.servername>
	// <component.report.version>R092.003</component.report.version>
	// <report.adxd.adxlan>FRA</report.adxd.adxlan>
	// <report.service.printport>1890</report.service.printport>
	// </module>
	private void createModule(Document adxInstallXmlDoc, IXMLElement moduleSpec, VariableSubstitutor substitutor,
			String moduleName, String moduleFamily, String moduleType) {

		Element moduleToAddOrUpdate = adxInstallXmlDoc.createElement("module");
		moduleToAddOrUpdate.setAttribute("name", moduleName);
		moduleToAddOrUpdate.setAttribute("family", moduleFamily);
		moduleToAddOrUpdate.setAttribute("type", moduleType);

		for (IXMLElement param : moduleSpec.getChildren()) {
			Element xmlParam = adxInstallXmlDoc.createElement(param.getName());
			xmlParam.setTextContent(substitutor.substitute(param.getContent(), SubstitutionType.TYPE_PLAIN));
			moduleToAddOrUpdate.appendChild(xmlParam);
		}
		adxInstallXmlDoc.getDocumentElement().appendChild(moduleToAddOrUpdate);
	}

	private void saveXml(java.io.File fileAdxinstalls, Document adxInstallXmlDoc, Transformer transformer)
			throws TransformerException, ParserConfigurationException {
		// It's ok normally, the module is added, recreate the XML

		// write the content into xml filed
		DOMSource source = new DOMSource(adxInstallXmlDoc);
		StreamResult result = new StreamResult(fileAdxinstalls);

		transformer.transform(source, result);

		// Output to console for testing
		// StreamResult resultOutput = new StreamResult(System.out);

		/*
		 * // create resource for uninstall DocumentBuilderFactory factory =
		 * DocumentBuilderFactory.newInstance(); DocumentBuilder builder =
		 * factory.newDocumentBuilder();
		 * 
		 * Document xdoc2 = builder.newDocument();
		 * 
		 * // Properties DOM xdoc2.setXmlVersion("1.0"); xdoc2.setXmlStandalone(true);
		 * 
		 * // create arborescence du DOM Element racine2 =
		 * xdoc2.createElement("install"); xdoc2.appendChild(racine2);
		 * xdoc2.getDocumentElement().appendChild(xdoc2.importNode(module, true));
		 */
		// TODO: FRDEPO
		// idata.uninstallOutJar.putNextEntry(new ZipEntry(SPEC_FILE_NAME));

		// DOMSource source2 = new DOMSource(xdoc2);
		// StreamResult result2 = new StreamResult(idata.uninstallOutJar);

		// transformer.transform(source2, result2);
		// idata.uninstallOutJar.closeEntry();
	}

	/***
	 * Create XML module
	 * 
	 * @param adxInstallXmlDoc
	 * @param moduleName
	 * @param moduleFamily
	 * @param moduleType
	 * @return
	 */
	/*
	 * private Element createReportModule(Document adxInstallXmlDoc, IXMLElement
	 * moduleSpec, String moduleName, String moduleFamily, String moduleType) {
	 * 
	 * Element module = adxInstallXmlDoc.createElement("module");
	 * module.setAttribute("name", moduleName); module.setAttribute("family",
	 * moduleFamily); module.setAttribute("type", moduleType);
	 * 
	 * 
	 * Node status = module .appendChild(adxInstallXmlDoc.createElement("component."
	 * + moduleFamily.toLowerCase() + ".installstatus"));
	 * status.setTextContent("idle");
	 * 
	 * Node path = module.appendChild(adxInstallXmlDoc.createElement("component." +
	 * moduleFamily.toLowerCase() + ".path"));
	 * path.setTextContent(this.installData.getVariable(InstallData.INSTALL_PATH));
	 * 
	 * Node nodeVersion = module
	 * .appendChild(adxInstallXmlDoc.createElement("component." +
	 * moduleFamily.toLowerCase() + ".version"));
	 * nodeVersion.setTextContent(this.installData.getVariable("component.version"))
	 * ;
	 * 
	 * 
	 * adxInstallXmlDoc.getDocumentElement().appendChild(module);
	 * 
	 * return module; }
	 */

	/*
	 * Update module from adxInstall.xml, component.[report}.version and
	 * component.[report].installstatus component.[report].installstatus: "update"
	 */
	private Element modifyReportModule(Document xdoc, String moduleName, String moduleFamily, String moduleType)
			throws Exception {

		XPath xPath = XPathFactory.newInstance().newXPath();
		String filter = "/install/module[@name='" + moduleName + "' and @type='" + moduleType + "' and @family='"
				+ moduleFamily + "']";
		Element module = (Element) xPath.compile(filter).evaluate(xdoc, XPathConstants.NODE);

		// if (module == null) throw new
		// Exception(String.format(langpack.getString("sectionNotFound"), moduleName));
		if (module == null) {
			logger.log(Level.FINE, "AdxCompInstallerListener.modifyReportModule  name: " + moduleName + " type: "
					+ moduleType + " family: " + moduleFamily + " not found in xmlDocument " + xdoc);
			throw new Exception(String.format(
					ResourceBundle.getBundle("com/sage/izpack/messages").getString("sectionNotFound"), moduleName));
		}

		Node status = module.getElementsByTagName("component." + moduleFamily.toLowerCase() + ".installstatus").item(0);
		if (status == null) {
			status = module
					.appendChild(xdoc.createElement("component." + moduleFamily.toLowerCase() + ".installstatus"));
		}
		status.setTextContent("update");

		String version = this.installData.getVariable("component.version");
		Node nodeVersion = module.getElementsByTagName("component." + moduleFamily.toLowerCase() + ".version").item(0);
		if (nodeVersion == null) {
			nodeVersion = module
					.appendChild(xdoc.createElement("component." + moduleFamily.toLowerCase() + ".version"));
		}
		nodeVersion.setTextContent(version);

		/*
		 * module = (Element) xPath.compile( "/install/module[@name='" + name +
		 * "' and @type='" + type + "' and @family='" + family + "']") .evaluate(xdoc,
		 * XPathConstants.NODE); if (module == null) throw new //
		 * Exception(String.format(langpack.getString("sectionNotFound"), name)); if
		 * (module == null) throw new Exception( String.format(ResourceBundle.getBundle(
		 * "com/izforge/izpack/ant/langpacks/messages") .getString("sectionNotFound"),
		 * name));
		 * 
		 * Node status = module.getElementsByTagName("component." + family.toLowerCase()
		 * + ".installstatus") .item(0); if (status == null) { status =
		 * module.appendChild(xdoc.createElement("component." + family.toLowerCase() +
		 * ".installstatus")); status.setTextContent("update"); }
		 * 
		 * // module = (Element) status.getParentNode(); if
		 * (status.getTextContent().equalsIgnoreCase("active")) {
		 * status.setTextContent("update"); }
		 * 
		 * Node nodeVersion = module.getElementsByTagName("component." +
		 * family.toLowerCase() + ".version") .item(0); if (nodeVersion == null)
		 * nodeVersion = module .appendChild(xdoc.createElement("component." +
		 * family.toLowerCase() + ".version")); nodeVersion.setTextContent(version);
		 * 
		 * if (family.equalsIgnoreCase("RUNTIME")) { // SAM (Syracuse) 99562 New Bug
		 * 'Performance issue with Oracle Instant Client' // do not use instant client
		 * by default but only when n-tiers
		 * 
		 * // runtime.odbc.dbhome Node nodedbhome =
		 * module.getElementsByTagName("runtime.odbc.dbhome").item(0); if (nodedbhome ==
		 * null) nodedbhome =
		 * module.appendChild(xdoc.createElement("runtime.odbc.dbhome")); // X3-134671 :
		 * In update mode, we have to let the console manage this node. We // let the
		 * value retreived // nodedbhome.setTextContent("");
		 * 
		 * // runtime.odbc.forcedblink Node nodedblink =
		 * module.getElementsByTagName("runtime.odbc.forcedblink").item(0); if
		 * (nodedblink == null) nodedblink =
		 * module.appendChild(xdoc.createElement("runtime.odbc.forcedblink")); //
		 * X3-134671 : In update mode, we have to let the console manage this node. We
		 * // let the value retreived // nodedblink.setTextContent("False");
		 * 
		 * }
		 */

		return module;
	}

	private Document getXml(java.io.File fileAdxinstalls)
			throws ParserConfigurationException, IOException, SAXException {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document xdoc = null;

		if (!fileAdxinstalls.exists()) {

			logger.log(Level.FINE,
					"AdxCompInstallerListener.getXml  Creating file " + fileAdxinstalls.getAbsolutePath());

			fileAdxinstalls.createNewFile();

			if (OsVersion.IS_UNIX) {
				Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
				// add owners permission
				perms.add(PosixFilePermission.OWNER_READ);
				perms.add(PosixFilePermission.OWNER_WRITE);
				// add group permissions
				perms.add(PosixFilePermission.GROUP_READ);
				perms.add(PosixFilePermission.GROUP_WRITE);
				// add others permissions
				perms.add(PosixFilePermission.OTHERS_READ);
				perms.add(PosixFilePermission.OTHERS_WRITE);

				Files.setPosixFilePermissions(fileAdxinstalls.toPath(), perms);
			}

			xdoc = dBuilder.newDocument();

			// Properties DOM
			xdoc.setXmlVersion("1.0");
			xdoc.setXmlStandalone(true);

			// create arborescence du DOM
			Element racine = xdoc.createElement("install");
			xdoc.appendChild(racine);

		} else {

			logger.log(Level.FINE,
					"AdxCompInstallerListener.getXml  Parsing file " + fileAdxinstalls.getAbsolutePath());
			xdoc = dBuilder.parse(fileAdxinstalls);
			xdoc.getDocumentElement().normalize();
		}

		XMLHelper.cleanEmptyTextNodes((Node) xdoc);

		logger.log(Level.FINE, "AdxCompInstallerListener.getXml  Xml file: " + fileAdxinstalls.getPath()
				+ ". Root element: " + xdoc.getDocumentElement().getNodeName());
		return xdoc;
	}

	/**
	 * 
	 * @param dirAdxDir
	 * @return Ex: C:\Sage\SafeX3\ADXADMIN\inst\adxinstalls.xml
	 */
	private java.io.File getAdxInstallFile(java.io.File dirAdxDir) {

		StringBuilder adxInstallBuilder = new StringBuilder();
		adxInstallBuilder.append(dirAdxDir.getAbsolutePath());
		adxInstallBuilder.append(File.separator);
		adxInstallBuilder.append("inst");
		adxInstallBuilder.append(File.separator);
		adxInstallBuilder.append(ADX_INSTALL_FILENAME);

		return new java.io.File(adxInstallBuilder.toString());
	}

	/*
	 * we need to find adxadmin path
	 */
	private String getAdxAdminPath() throws NativeLibException, Exception, FileNotFoundException, IOException {

		String strAdxAdminPath = "";

		logger.log(Level.FINE,
				"AdxCompInstallerListener  Init registry installData Locale: " + this.installData.getLocaleISO2());
		logger.log(Level.FINE,
				"AdxCompInstallerListener  Init registry getInstallPath: " + this.installData.getInstallPath());

		RegistryHandlerX3 rh = new RegistryHandlerX3(this.registryHandler);
		if (this.registryHandler != null && rh != null) {

			boolean adxAdminRegistered = rh.adxadminProductRegistered();
			logger.log(Level.FINE, "AdxCompInstallerListener  Init RegistryHandlerX3. adxadminProductRegistered: "
					+ adxAdminRegistered);

			// Test adxadmin is already installed. Read registry
			// "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN"
			if (adxAdminRegistered) {

				// String keyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
				// String keyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
				int oldVal = this.registryHandler.getRoot();
				this.registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);

				String keyName = ADXADMIN_REG_KeyName64Bits;
				if (!this.registryHandler.valueExist(keyName, "ADXDIR"))
					keyName = ADXADMIN_REG_KeyName32Bits;
				if (!this.registryHandler.valueExist(keyName, "ADXDIR"))
					// <str id="adxadminNoAdxDirReg" txt="A previous installation of the adxadmin
					// administration runtime was detected in the registry but the setup is unable
					// to find the installation path, some registry keys are missing !"/>
					// throw new
					// Exception(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("adxadminNoAdxDirReg"));
					throw new Exception(
							"A previous installation of the adxadmin administration runtime was detected in the registry but the setup is unable to find the installation path, some registry keys are missing.");

				// fetch ADXDIR path
				strAdxAdminPath = this.registryHandler.getValue(keyName, "ADXDIR").getStringData();

				logger.log(Level.FINE,
						"AdxCompInstallerListener  ADXDIR path: " + strAdxAdminPath + "  Key: " + keyName);

				// free RegistryHandler
				this.registryHandler.setRoot(oldVal);
			} else {
				// else throw new Exception(langpack.getString("adxadminNotRegistered"));
				// <str id="adxadminNotRegistered" txt="You must install an adxadmin
				// administration runtime first. Exiting now."/>
				// String warnMessage = String.format(ResourceBundle.getBundle("messages",
				// installData.getLocale()).getString("adxadminNotRegistered"), line);
				// throw new
				// Exception(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("adxadminNotRegistered"));
				throw new Exception("You must install an adxadmin administration runtime first. Exiting now.");
			}

		} else {
			logger.log(Level.FINE, "AdxCompInstallerListener - Could not get RegistryHandler !");

			// else we are on a os which has no registry or the needed dll was not bound to
			// this installation.
			// In both cases we forget the "already exist" check.

			// test adxadmin sous unix avec /adonix/adxadm ?
			if (OsVersion.IS_UNIX) {
				java.io.File adxadmFile = new java.io.File("/sage/adxadm");
				if (!adxadmFile.exists()) {
					adxadmFile = new java.io.File("/adonix/adxadm");
					if (!adxadmFile.exists()) {
						// throw new Exception(langpack.getString("adxadminNotRegistered"));
						throw new Exception(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages")
								.getString("adxadminNotRegistered"));
					}
				}

				FileReader readerAdxAdmFile = new FileReader(adxadmFile);
				BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
				strAdxAdminPath = buffread.readLine();
			}

		}
		return strAdxAdminPath;
	}

}
