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
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.helper.SpecHelper;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;

// This happens when not creating the module-info.java
// Solves moving the JRE System Library from the Modulepath to the Classpath your issue? 
// Everything must be on the classpath, the JAR and the JRE System Library

// public class AdxCompInstallerListener extends SimpleInstallerListener implements CleanupClient
public class AdxCompInstallerListener extends AbstractInstallerListener implements CleanupClient {

	private static final Logger logger = Logger.getLogger(AdxCompInstallerListener.class.getName());

	private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";
	private SpecHelper specHelper = null;
	private Resources resources = null;
	private VariableSubstitutor variableSubstitutor;

	private com.izforge.izpack.api.data.InstallData installData;
	// private RegistryDefaultHandler handler;
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
		// installation was not successful now rewind aedxinstalls.xml changes
	}

	public void beforePacks(List<Pack> packs) // , ProgressListener listener);

	// TODO: FRDEPO
	// public void beforePacks(AutomatedInstallData idata, Integer npacks,
	// AbstractUIProgressHandler handler) throws Exception
	{
		// TODO : FRDEPO
		// super.beforePacks(idata, npacks, handler);
		super.beforePacks(packs);

		// Variables variables = new DefaultVariables();
		// this.idata = new AutomatedInstallData(variables, OsVersion.IS_UNIX ?
		// Platforms.LINUX : Platforms.WINDOWS);

		// TODO : FRDEPO
		// SimpleInstallerListener.langpack = idata.langpack;

		// TODO : FRDEPO
		// getSpecHelper().readSpec(SPEC_FILE_NAME);
		this.specHelper = new SpecHelper(this.resources);
		try {
			this.specHelper.readSpec(SPEC_FILE_NAME);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// getSpecHelper().readSpec(SPEC_FILE_NAME);

		// TODO : FRDEPO
		// rh.verify(idata);

	}

	@Override
	public void afterPacks(List<Pack> packs, ProgressListener listener)
	// public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler
	// handler)
	// throws Exception
	{
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
			logger.info("Reading XML file fileAdxinstalls: " + fileAdxinstalls.getAbsolutePath());

			Document xdoc = getXml(fileAdxinstalls);

			// adxinstalls.xml read or created
			// il faut ajouter le module
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", 4);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			IXMLElement elemSpec = this.specHelper.getSpec();
			IXMLElement moduleSpec = elemSpec.getFirstChildNamed("module");
			// NodeList nodelist = xdoc.getElementsByTagName("module");
			 VariableSubstitutor substitutor =  new VariableSubstitutorImpl(this.installData.getVariables());
			 String moduleName = substitutor.substitute(moduleSpec.getAttribute("name"), SubstitutionType.TYPE_PLAIN);
			 String moduleFamily = substitutor.substitute(moduleSpec.getAttribute("family"), SubstitutionType.TYPE_PLAIN);
			 String moduleType = substitutor.substitute(moduleSpec.getAttribute("type"), SubstitutionType.TYPE_PLAIN);
				if (moduleType == null)
					moduleType = "";
			 String version = moduleSpec.getFirstChildNamed("component." + moduleFamily.toLowerCase() + ".version").getContent();
			 
			// TODO: FRDEPO
			// IXMLElement elemSpec = getSpecHelper().getSpec();
			// IXMLElement moduleSpec = null; // elemSpec.getFirstChildNamed("module");
			// NodeList nodelist= xdoc.getElementsByTagName("module");
			// TODO: FRDEPO
			// VariableSubstitutor substitutor = new
			// VariableSubstitutor(idata.getVariables());
			// VariableSubstitutor substitutor = new
			// VariableSubstitutorImpl(idata.getVariables());
			// String name = substitutor.substitute(moduleSpec.getAttribute("name"),
			// VariableSubstitutor.PLAIN);
			// String family = substitutor.substitute(moduleSpec.getAttribute("family"),
			// VariableSubstitutor.PLAIN);
			// String type = substitutor.substitute(moduleSpec.getAttribute("type"),
			// VariableSubstitutor.PLAIN);
			// String name = substitutor.substitute(moduleSpec.getAttribute("name"));
			// String family = substitutor.substitute(moduleSpec.getAttribute("family"));
			// String type = substitutor.substitute(moduleSpec.getAttribute("type"));
			// String version = substitutor.substitute(
			// moduleSpec.getFirstChildNamed("component." + family.toLowerCase() +
			// ".version").getContent(), VariableSubstitutor.PLAIN);
			// moduleSpec.getFirstChildNamed("component." + family.toLowerCase() +
			// ".version").getContent());

			// String moduleName = this.installData.getVariable("component.node.name");
			// String moduleFamily = this.installData.getVariable("component.node.family"); // REPORT
			// String moduleType = this.installData.getVariable("component.node.type");

			Element module = null;
			boolean modifyinstallation = Boolean.valueOf(installData.getVariable(InstallData.MODIFY_INSTALLATION));

			logger.info("moduleName: " + moduleName + " moduleFamily: " + moduleFamily + " modifyinstallation: "
					+ modifyinstallation);

			if (modifyinstallation) {

				module = modifyReportModule(xdoc, moduleName, moduleFamily, moduleType);

			} else {

				module = createReportModule(xdoc, moduleName, moduleFamily, moduleType);
			}

			logger.info("saving XML xdoc:" + xdoc.getDocumentElement().getNodeName());
			saveXml(fileAdxinstalls, xdoc, transformer, module);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// catch (Exception e) {
		// e.printStackTrace();
		// throw new Exception(e.getMessage());
		// }
	}

	private void saveXml(java.io.File fileAdxinstalls, Document xdoc, Transformer transformer, Element module)
			throws TransformerException, ParserConfigurationException {
		// It's ok normally, the module is added, recreate the XML

		// write the content into xml file
		DOMSource source = new DOMSource(xdoc);
		StreamResult result = new StreamResult(fileAdxinstalls);

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);
		transformer.transform(source, result);

		// create resource for uninstall
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document xdoc2 = builder.newDocument();

		// Properties DOM
		xdoc2.setXmlVersion("1.0");
		xdoc2.setXmlStandalone(true);

		// create arborescence du DOM
		Element racine2 = xdoc2.createElement("install");
		xdoc2.appendChild(racine2);
		xdoc2.getDocumentElement().appendChild(xdoc2.importNode(module, true));

		// TODO: FRDEPO
		// idata.uninstallOutJar.putNextEntry(new ZipEntry(SPEC_FILE_NAME));

		// DOMSource source2 = new DOMSource(xdoc2);
		// StreamResult result2 = new StreamResult(idata.uninstallOutJar);

		// transformer.transform(source2, result2);
		// idata.uninstallOutJar.closeEntry();
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
	private Element createReportModule(Document xdoc, String moduleName, String moduleFamily, String moduleType) {

		// create module
		Element module = xdoc.createElement("module");
		module.setAttribute("name", moduleName);
		module.setAttribute("family", moduleFamily);
		module.setAttribute("type", moduleType);

		Node status = module
				.appendChild(xdoc.createElement("component." + moduleFamily.toLowerCase() + ".installstatus"));
		status.setTextContent("idle");

		Node path = module.appendChild(xdoc.createElement("component." + moduleFamily.toLowerCase() + ".path"));
		path.setTextContent(this.installData.getVariable(InstallData.INSTALL_PATH));

		Node nodeVersion = module
				.appendChild(xdoc.createElement("component." + moduleFamily.toLowerCase() + ".version"));
		nodeVersion.setTextContent(this.installData.getVariable("component.version"));

		/*
		 * for (IXMLElement param : moduleSpec.getChildren()) { Element xmlParam =
		 * xdoc.createElement(param.getName());
		 * xmlParam.setTextContent(substitutor.substitute(param.getContent()));
		 * module.appendChild(xmlParam); }
		 * 
		 * xdoc.getDocumentElement().appendChild(module);
		 */
		return module;
	}

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
			logger.info("name: " + moduleName + " type: " + moduleType + " family: " + moduleFamily
					+ " not found in xmlDocument " + xdoc);
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
		// Document xmodule = null;

		if (!fileAdxinstalls.exists()) {

			logger.info("Creating file " + fileAdxinstalls.getAbsolutePath());

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

			logger.info("Parsing file " + fileAdxinstalls.getAbsolutePath());
			xdoc = dBuilder.parse(fileAdxinstalls);
			xdoc.getDocumentElement().normalize();
		}

		XMLHelper.cleanEmptyTextNodes((Node) xdoc);

		logger.info("Xml file: " + fileAdxinstalls.getPath() + ". Root element: "
				+ xdoc.getDocumentElement().getNodeName());
		return xdoc;
	}

	private java.io.File getAdxInstallFile(java.io.File dirAdxDir) {
		StringBuilder adxInstallBuilder = new StringBuilder();
		adxInstallBuilder.append(dirAdxDir.getAbsolutePath());
		adxInstallBuilder.append(File.separator);
		adxInstallBuilder.append("inst");
		adxInstallBuilder.append(File.separator);
		adxInstallBuilder.append("adxinstalls.xml");

		return new java.io.File(adxInstallBuilder.toString());
	}

	/*
	 * we need to find adxadmin path
	 */
	private String getAdxAdminPath() throws NativeLibException, Exception, FileNotFoundException, IOException {

		String strAdxAdminPath = "";

		logger.info("Init dregistry installData Locale: " + this.installData.getLocaleISO2());
		logger.info("Init dregistry getInstallPath: " + this.installData.getInstallPath());

		RegistryHandlerX3 rh = new RegistryHandlerX3(this.registryHandler);
		if (this.registryHandler != null && rh != null) {

			// TODO: FRDEPO
			// rh.verify(idata);
			boolean adxAdminRegistered = rh.adxadminProductRegistered();
			logger.info("Init RegistryHandlerX3. adxadminProductRegistered: " + adxAdminRegistered);

			// Test adxadmin is already installed. Read registry
			// "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN"
			if (adxAdminRegistered) {

				String keyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
				String keyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
				int oldVal = this.registryHandler.getRoot();
				// rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
				this.registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);

				String keyName = keyName64Bits;
				if (!this.registryHandler.valueExist(keyName, "ADXDIR"))
					keyName = keyName32Bits;
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

				logger.info("ADXDIR path: " + strAdxAdminPath + "  Key: " + keyName);

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
			logger.info("AdxCompInstallerListener - Could not get RegistryHandler !");
			// Debug.log("CheckedHelloPanel - Could not get RegistryHandler !");

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
