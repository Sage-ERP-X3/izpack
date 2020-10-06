package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.event.AbstractInstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.event.AbstractProgressInstallerListener;
import com.izforge.izpack.api.data.AutomatedInstallData;
// import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.config.SingleConfigurableTask.Entry.LookupType;
import com.izforge.izpack.util.xml.XMLHelper;

import jline.internal.Log;

// public class AdxCompInstallerListener extends SimpleInstallerListener implements CleanupClient
public class AdxCompInstallerListener extends AbstractInstallerListener implements CleanupClient {

	private static final Logger logger = Logger.getLogger(AdxCompInstallerListener.class.getName());

	private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";

	private AutomatedInstallData idata = null;
	
	public AdxCompInstallerListener() {
		// super(true);
		super();
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

		Variables variables = new DefaultVariables();
		this.idata = new AutomatedInstallData(variables,
				OsVersion.IS_UNIX ? Platforms.LINUX : Platforms.WINDOWS);

		// TODO : FRDEPO
		// SimpleInstallerListener.langpack = idata.langpack;

		// TODO : FRDEPO
		// getSpecHelper().readSpec(SPEC_FILE_NAME);

		// RegistryHandler rh = RegistryDefaultHandler.getInstance();
		RegistryHandlerX3 rh = new RegistryHandlerX3();
		if (rh == null) {
			return;
		}
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

		// we need to find adxadmin path
		String strAdxAdminPath = "";

		// RegistryHandler rh = RegistryDefaultHandler.getInstance();
		RegistryHandlerX3 rh = new RegistryHandlerX3();
		if (rh != null) {

			// TODO: FRDEPO
			// rh.verify(idata);

			// test adxadmin déjà installé avec registry
			if (rh.adxadminProductRegistered()) {

				String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
				int oldVal = rh.getRoot();
				// rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
				rh.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
				if (!rh.valueExist(keyName, "ADXDIR"))
					keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
				if (!rh.valueExist(keyName, "ADXDIR"))
					// throw new Exception(langpack.getString("adxadminNoAdxDirReg"));
					throw new Exception(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages")
							.getString("adxadminNoAdxDirReg"));

				// récup path
				strAdxAdminPath = rh.getValue(keyName, "ADXDIR").getStringData();

				// free RegistryHandler
				rh.setRoot(oldVal);
			} else {
				// else throw new Exception(langpack.getString("adxadminNotRegistered"));
				throw new Exception(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages")
						.getString("adxadminNotRegistered"));
			}

		} else {
			Log.debug("CheckedHelloPanel - Could not get RegistryHandler !");
			// Debug.log("CheckedHelloPanel - Could not get RegistryHandler !");

			// else we are on a os which has no registry or the
			// needed dll was not bound to this installation. In
			// both cases we forget the "already exist" check.

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

		// check strAdxAdminPath

		// if (strAdxAdminPath == null || "".equals(strAdxAdminPath)) throw new
		// Exception(langpack.getString("adxadminParseError"));
		if (strAdxAdminPath == null || "".equals(strAdxAdminPath))
			throw new Exception(ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));

		java.io.File dirAdxDir = new java.io.File(strAdxAdminPath);

		if (!dirAdxDir.exists() || !dirAdxDir.isDirectory())
			// throw new Exception(langpack.getString("adxadminParseError"));
			throw new Exception(ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(dirAdxDir.getAbsolutePath());
		strBuilder.append(File.separator);
		strBuilder.append("inst");
		strBuilder.append(File.separator);
		strBuilder.append("adxinstalls.xml");

		java.io.File fileAdxinstalls = new java.io.File(strBuilder.toString());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document xdoc = null;
		Document xmodule = null;

		if (!fileAdxinstalls.exists()) {
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

			// Propriétés du DOM
			xdoc.setXmlVersion("1.0");
			xdoc.setXmlStandalone(true);

			// create arborescence du DOM
			Element racine = xdoc.createElement("install");
			xdoc.appendChild(racine);
		} else {
			xdoc = dBuilder.parse(fileAdxinstalls);
		}

		src.com.sage.izpack.XMLHelper.cleanEmptyTextNodes((Node) xdoc);

		// adxinstalls.xml lu ou crée
		// il faut ajouter le module
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number", 4);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// TODO: FRDEPO
		// IXMLElement elemSpec = getSpecHelper().getSpec();
		IXMLElement moduleSpec = elemSpec.getFirstChildNamed("module");
		// TODO: FRDEPO
		// VariableSubstitutor substitutor = new VariableSubstitutor(idata.getVariables());	
        VariableSubstitutor substitutor = new VariableSubstitutorImpl(idata.getVariables());
		// String name = substitutor.substitute(moduleSpec.getAttribute("name"), VariableSubstitutor.PLAIN);
		// String family = substitutor.substitute(moduleSpec.getAttribute("family"), VariableSubstitutor.PLAIN);
		// String type = substitutor.substitute(moduleSpec.getAttribute("type"), VariableSubstitutor.PLAIN);
		String name = substitutor.substitute(moduleSpec.getAttribute("name"));
		String family = substitutor.substitute(moduleSpec.getAttribute("family"));
		String type = substitutor.substitute(moduleSpec.getAttribute("type"));
		String version = substitutor.substitute(
				// moduleSpec.getFirstChildNamed("component." + family.toLowerCase() + ".version").getContent(),  VariableSubstitutor.PLAIN);
				moduleSpec.getFirstChildNamed("component." + family.toLowerCase() + ".version").getContent());

		Element module = null;

		boolean modifyinstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
		if (modifyinstallation) {
			Element xmlinstall = xdoc.getDocumentElement();
			XPath xPath = XPathFactory.newInstance().newXPath();

			module = (Element) xPath.compile(
					"/install/module[@name='" + name + "' and @type='" + type + "' and @family='" + family + "']")
					.evaluate(xdoc, XPathConstants.NODE);
			// if (module == null) throw new
			// Exception(String.format(langpack.getString("sectionNotFound"), name));
			if (module == null)
				throw new Exception(String.format(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages")
						.getString("sectionNotFound"), name));

			Node status = module.getElementsByTagName("component." + family.toLowerCase() + ".installstatus").item(0);
			if (status == null) {
				status = module.appendChild(xdoc.createElement("component." + family.toLowerCase() + ".installstatus"));
				status.setTextContent("update");
			}

			// module = (Element) status.getParentNode();
			if (status.getTextContent().equalsIgnoreCase("active")) {
				status.setTextContent("update");
			}

			Node nodeVersion = module.getElementsByTagName("component." + family.toLowerCase() + ".version").item(0);
			if (nodeVersion == null)
				nodeVersion = module.appendChild(xdoc.createElement("component." + family.toLowerCase() + ".version"));
			nodeVersion.setTextContent(version);

			if (family.equalsIgnoreCase("RUNTIME")) {
				// SAM (Syracuse) 99562 New Bug 'Performance issue with Oracle Instant Client'
				// do not use instant client by default but only when n-tiers

				// runtime.odbc.dbhome
				Node nodedbhome = module.getElementsByTagName("runtime.odbc.dbhome").item(0);
				if (nodedbhome == null)
					nodedbhome = module.appendChild(xdoc.createElement("runtime.odbc.dbhome"));
				// X3-134671 : In update mode, we have to let the console manage this node. We
				// let the value retreived
				// nodedbhome.setTextContent("");

				// runtime.odbc.forcedblink
				Node nodedblink = module.getElementsByTagName("runtime.odbc.forcedblink").item(0);
				if (nodedblink == null)
					nodedblink = module.appendChild(xdoc.createElement("runtime.odbc.forcedblink"));
				// X3-134671 : In update mode, we have to let the console manage this node. We
				// let the value retreived
				// nodedblink.setTextContent("False");

			}

		} else {

			// create module
			module = xdoc.createElement("module");
			module.setAttribute("name", name);
			module.setAttribute("family", family);
			module.setAttribute("type", type);

			for (IXMLElement param : moduleSpec.getChildren()) {
				Element xmlParam = xdoc.createElement(param.getName());
				// xmlParam.setTextContent(substitutor.substitute(param.getContent(), VariableSubstitutor.PLAIN));
				xmlParam.setTextContent(substitutor.substitute(param.getContent()));
				module.appendChild(xmlParam);
			}

			xdoc.getDocumentElement().appendChild(module);

		}

		// en principe c'est bon, le module est ajouté, réécriture du XML

		// write the content into xml file
		DOMSource source = new DOMSource(xdoc);
		StreamResult result = new StreamResult(fileAdxinstalls);

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);
		transformer.transform(source, result);

		// create resource for uninstall
		Document xdoc2 = dBuilder.newDocument();

		// Propriétés du DOM
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

}
