package com.sage.izpack;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.helper.SpecHelper;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;

/*
 * Manage XML file adxinstalls.xml
 * 
 * @author Franck DEPOORTERE
 */
public class AdxCompInstallerListener extends AbstractInstallerListener implements CleanupClient {

	private static final Logger logger = Logger.getLogger(AdxCompInstallerListener.class.getName());
	private static String LogPrefix = "AdxCompInstallerListener - ";

	private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";

	private SpecHelper specHelper = null;
	private Resources resources = null;
	private VariableSubstitutor variableSubstitutor;

	private final com.izforge.izpack.api.data.InstallData installData;
	private final com.izforge.izpack.installer.data.UninstallData uninstallData;
	private final RegistryHandler registryHandler;

	public AdxCompInstallerListener(com.izforge.izpack.api.data.InstallData installData, UninstallData uninstallData,
			VariableSubstitutor variableSubstitutor, Resources resources, RegistryDefaultHandler handler) {

		super();
		this.installData = installData;
		this.uninstallData = uninstallData;
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
			e.printStackTrace();
		}

		if (this.installData.getInfo().isReadInstallationInformation()) {

			if (!InstallationInformationHelper.hasAlreadyReadInformation(this.installData))
				InstallationInformationHelper.readInformation(this.installData, this.resources);
			else
				logger.log(Level.FINE,
						LogPrefix + "beforePacks  ReadInstallationInformation: "
								+ this.installData.getInfo().isReadInstallationInformation() + " AlreadyRead: "
								+ InstallationInformationHelper.hasAlreadyReadInformation(this.installData));

		} else {
			logger.log(Level.FINE, LogPrefix + "beforePacks  ReadInstallationInformation: "
					+ this.installData.getInfo().isReadInstallationInformation());
		}
	}

	@Override
	public void afterPacks(List<Pack> packs, ProgressListener listener) {
		// here we need to update adxinstalls.xml

		try {
			AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, this.installData);
			String adxAdminPath = adxCompHelper.getAdxAdminPath();
			// check strAdxAdminPath
			if (adxAdminPath == null || "".equals(adxAdminPath))
				throw new Exception(ResourcesHelper.getCustomPropString("adxadminParseError"));
			// ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));

			java.io.File dirAdxDir = new java.io.File(adxAdminPath);
			if (!dirAdxDir.exists() || !dirAdxDir.isDirectory())
				// throw new Exception(langpack.getString("adxadminParseError"));
				throw new Exception(ResourcesHelper.getCustomPropString("adxadminParseError"));
			// ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));

			java.io.File fileAdxinstalls = adxCompHelper.getAdxInstallFile(dirAdxDir);
			logger.log(Level.FINE,
					LogPrefix + "afterPacks  Reading XML file fileAdxinstalls: " + fileAdxinstalls.getAbsolutePath());

			Document adxInstallXmlDoc = getXml(fileAdxinstalls);

			// adxinstalls.xml read or created
			IXMLElement elemSpec = this.specHelper.getSpec(); // AdxCompSpec.xml
			IXMLElement moduleSpec = elemSpec.getFirstChildNamed("module");
			VariableSubstitutor substitutor = new VariableSubstitutorImpl(this.installData.getVariables());
			String moduleName = substitutor.substitute(moduleSpec.getAttribute("name"), SubstitutionType.TYPE_PLAIN);
			String moduleFamily = substitutor.substitute(moduleSpec.getAttribute("family"),
					SubstitutionType.TYPE_PLAIN);
			String moduleType = substitutor.substitute(moduleSpec.getAttribute("type"), SubstitutionType.TYPE_PLAIN);
			if (moduleType == null)
				moduleType = "";
			String version = substitutor.substitute(
					moduleSpec.getFirstChildNamed("component." + moduleFamily.toLowerCase() + ".version").getContent(),
					SubstitutionType.TYPE_PLAIN);
			// String version = moduleSpec.getFirstChildNamed("component." +
			// moduleFamily.toLowerCase() + ".version").getContent();

			boolean modifyinstallation = ModifyInstallationUtil.get(installData);

			logger.log(Level.FINE, LogPrefix + "afterPacks  moduleName: " + moduleName + " moduleFamily: "
					+ moduleFamily + " modifyinstallation: " + modifyinstallation);

			Element module = null;
			if (modifyinstallation) {

				module = modifyXmlModule(adxInstallXmlDoc, moduleName, moduleFamily, moduleType, version);

			} else {

				module = createModule(adxInstallXmlDoc, moduleSpec, substitutor, moduleName, moduleFamily, moduleType);
			}
			Element moduleToAdd = null;
			if (module != null) {
				moduleToAdd = (Element) module.cloneNode(true);
			}
			logger.log(Level.FINE,
					LogPrefix + "afterPacks  saving XML xdoc:" + adxInstallXmlDoc.getDocumentElement().getNodeName());

			Transformer transformer = AdxCompHelper.getTransformer("UTF-8");
			AdxCompHelper.saveXml(fileAdxinstalls, adxInstallXmlDoc, transformer);
			logger.log(Level.FINE, LogPrefix + "afterPacks  XML doc saved");

			logger.log(Level.FINE, LogPrefix + "afterPacks  Add data to uninstaller " + moduleToAdd);
			if (moduleToAdd == null) {
				logger.log(Level.FINE,
						LogPrefix + "afterPacks  Error - module is NULL - Cannot add data to uninstaller");
			} else {
				addDataToUninstaller(transformer, adxCompHelper, moduleToAdd);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// throw new Exception(e.getMessage());
	}

	private void addDataToUninstaller(Transformer transformer, AdxCompHelper adxCompHelper, Element module)
			throws FileNotFoundException, NativeLibException, IOException, Exception {
		// Add "AdxCompSpec.xml" within uninstaller, to fetch module name while
		// uninstalling
		// idata.uninstallOutJar.putNextEntry(new ZipEntry(SPEC_FILE_NAME));
		// DOMSource source2 = new DOMSource(xdoc2);
		// StreamResult result2 = new StreamResult (idata.uninstallOutJar);
		// transformer.transform(source2, result2);
		// idata.uninstallOutJar.closeEntry();

		try {

			logger.log(Level.FINE, LogPrefix + "Add data " + SPEC_FILE_NAME + ": " + module);

			// specHelper.setSpec((IXMLElement) module);

			this.uninstallData.addAdditionalData("1" + SPEC_FILE_NAME, AdxCompHelper.asByteArray(module, "utf-8"));
			// this.uninstallData.addAdditionalData("3" + SPEC_FILE_NAME, module);

			// DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			// DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			// Document xdoc = dBuilder.newDocument();
			// Properties DOM
			// xdoc.setXmlVersion("1.0");
			// xdoc.setXmlStandalone(true);
			// Node newnode = xdoc.importNode(module, true);
			// xdoc.appendChild(newnode);
			// this.uninstallData.addAdditionalData("4" + SPEC_FILE_NAME, xdoc);

			// this.uninstallData.addAdditionalData("5" + SPEC_FILE_NAME, encode(xdoc));

			this.uninstallData.addAdditionalData("" + SPEC_FILE_NAME, AdxCompHelper.asString(module, "utf-8"));

			// String fileName = adxCompHelper.getAdxAdminPath() + File.separator + "tmp" +
			// File.separator
			// + SPEC_FILE_NAME;
			// java.io.File file = new java.io.File(fileName);
			// saveElementToFile(module, file, transformer);

			// logger.log(Level.FINE, "AdxCompInstallerListener Add file " + fileName);
			// this.uninstallData.addFile(fileName, false);

			// this.uninstallData.addUninstallScript(fileName);

			// logger.log(Level.FINE, "AdxCompInstallerListener Add file
			// addBuildResourceToUninstallerData " + fileName);
			// addBuildResourceToUninstallerData("7"+SPEC_FILE_NAME, file);

			// file.deleteOnExit();

		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	/*
	 * private byte[] encode(Document obj) { byte[] bytes = null; try { // Document
	 * vsNew = new Document(obj) ByteArrayOutputStream baos = new
	 * ByteArrayOutputStream(); GZIPOutputStream out = new GZIPOutputStream(baos);
	 * XMLEncoder encoder = new XMLEncoder(out); encoder.writeObject(obj);
	 * encoder.close(); bytes = baos.toByteArray(); } catch (Exception e) {
	 * logger.log(Level.FINE, "Exception caught while encoding/zipping ", e); }
	 * return bytes; }
	 */

	private void addBuildResourceToUninstallerData(String dataName, File buildFile) throws IOException {
		byte[] content;
		ByteArrayOutputStream bos = new ByteArrayOutputStream((int) buildFile.length());
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(buildFile));
		int aByte;
		while (-1 != (aByte = bis.read())) {
			bos.write(aByte);
		}
		content = bos.toByteArray();
		bis.close();
		uninstallData.addAdditionalData(dataName, content);
	}

	private void saveElementToFile(Element module, java.io.File file, Transformer transformer)
			throws ParserConfigurationException, IOException, SAXException, TransformerException {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document xdoc = dBuilder.newDocument();
		// Properties DOM
		xdoc.setXmlVersion("1.0");
		xdoc.setXmlStandalone(true);

		Node newnode = xdoc.importNode(module, true);
		xdoc.appendChild(newnode);

		if (!file.exists()) {
			file.createNewFile();
		}
		DOMSource source = new DOMSource(module);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
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
	private Element createModule(Document adxInstallXmlDoc, IXMLElement moduleSpec, VariableSubstitutor substitutor,
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

		return moduleToAddOrUpdate;
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
	private Element modifyXmlModule(Document xdoc, String moduleName, String moduleFamily, String moduleType,
			String version) throws Exception {

		XPath xPath = XPathFactory.newInstance().newXPath();
		String filter = "/install/module[@name='" + moduleName + "' and @type='" + moduleType + "' and @family='"
				+ moduleFamily + "']";
		Element module = (Element) xPath.compile(filter).evaluate(xdoc, XPathConstants.NODE);

		if (module == null) {
			logger.log(Level.FINE, LogPrefix + "modifyReportModule  name: " + moduleName + " type: " + moduleType
					+ " family: " + moduleFamily + " not found in xmlDocument " + xdoc);
			// throw new Exception(ResourcesHelper.getCustomPropString("sectionNotFound",
			// moduleName));
			ResourcesHelper resourceHelper = new ResourcesHelper(this.installData, this.resources);
			throw new Exception(resourceHelper.getCustomString("sectionNotFound", moduleName));
		}

		String statusComponent = "component." + moduleFamily.toLowerCase() + ".installstatus";
		Node status = module.getElementsByTagName(statusComponent).item(0);
		if (status == null) {
			status = module.appendChild(xdoc.createElement(statusComponent));
		}
		status.setTextContent("update");

		/*
		 * String version = this.installData.getVariable("component.version"); if
		 * (version == null || version == "") { version =
		 * this.installData.getVariable("COMPONENT.VERSION"); } if (version == null ||
		 * version == "") { version = this.installData.getVariable("APP_VER"); //
		 * app-version }
		 */
		String versionComponent = "component." + moduleFamily.toLowerCase() + ".version";
		Node nodeVersion = module.getElementsByTagName(versionComponent).item(0);
		if (nodeVersion == null) {
			nodeVersion = module.appendChild(xdoc.createElement(versionComponent));
		}
		nodeVersion.setTextContent(version);

		logger.log(Level.FINE, LogPrefix + "modifyXmlModule  saving XML '" + versionComponent + "': " + version);

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

			logger.log(Level.FINE, LogPrefix + "getXml  Creating file " + fileAdxinstalls.getAbsolutePath());

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

			logger.log(Level.FINE, LogPrefix + "getXml  Parsing file " + fileAdxinstalls.getAbsolutePath());
			xdoc = dBuilder.parse(fileAdxinstalls);
			xdoc.getDocumentElement().normalize();
		}

		XMLHelper.cleanEmptyTextNodes((Node) xdoc);

		logger.log(Level.FINE, LogPrefix + "getXml  Xml file: " + fileAdxinstalls.getPath() + ". Root element: "
				+ xdoc.getDocumentElement().getNodeName());
		return xdoc;
	}

}
