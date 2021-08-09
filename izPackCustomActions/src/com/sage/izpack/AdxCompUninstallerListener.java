package com.sage.izpack;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.uninstaller.event.*;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;

/*
 * @author Franck DEPOORTERE
 */
public class AdxCompUninstallerListener extends UninstallerListeners
		implements com.izforge.izpack.api.event.UninstallerListener {

	private static final Logger logger = Logger.getLogger(AdxCompUninstallerListener.class.getName());

	private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";

	private com.izforge.izpack.api.data.InstallData installData;
	private RegistryDefaultHandler handler;
	private RegistryHandler registryHandler;
	private Prompt prompt;
	private Resources resources;

	public AdxCompUninstallerListener(com.izforge.izpack.api.data.InstallData installData,
			RegistryDefaultHandler handler, Prompt prompt, Resources resources) {
		super(null); // Prompt

		this.installData = installData;
		this.handler = handler;
		this.registryHandler = handler.getInstance();
		this.prompt = prompt;
		this.resources = resources;
	}

	private AbstractUIHandler GetPromptUIHandler() {

		AbstractUIHandler handler = new PromptUIHandler(this.prompt);
		return handler;
	}

	@Override
	public void beforeDelete(List<File> arg0) {

		beforeDeletion();

	}

	@Override
	public void beforeDelete(File arg0) {
		
		beforeDeletion();

	}

	@Override
	public void beforeDelete(List<File> arg0, ProgressListener arg1) {

		beforeDeletion();
	}

	private void beforeDeletion() {
		try {

			// Load the defined adx module to be deleted.
			InputStream in = getClass().getResourceAsStream("/" + SPEC_FILE_NAME);
			if (in == null) { // No actions, nothing todo.
				return;
			}

			// get file adxinstalls

			// here we need to update adxinstalls.xml

			// we need to find adxadmin path

			// vérification strAdxAdminPath
			AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, this.installData);
			String adxAdminPath = adxCompHelper.getAdxAdminPath();
			if (adxAdminPath == null || "".equals(adxAdminPath)) {
				logger.log(Level.FINE, "AdxCompUninstallerListener.beforeDeletion  OK => AdxAdmin not found.");
				return;
			}

			java.io.File dirAdxDir = new java.io.File(adxAdminPath);
			if (!dirAdxDir.exists() || !dirAdxDir.isDirectory())
				// throw new Exception(langpack.getString("adxadminParseError"));
				throw new Exception(ResourcesHelper.getCustomPropString("adxadminParseError"));
			// ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));

			java.io.File fileAdxinstalls = adxCompHelper.getAdxInstallFile(dirAdxDir);
			logger.log(Level.FINE, "AdxCompUninstallerListener.beforeDeletion  Reading XML file fileAdxinstalls: "
					+ fileAdxinstalls.getAbsolutePath());

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document adxInstallXmlDoc = null;
			Element reportModule = null;

			if (!fileAdxinstalls.exists()) {
				logger.log(Level.FINE, "AdxCompUninstallerListener.beforeDeletion  " + fileAdxinstalls.getAbsolutePath()
						+ " doesn't exist.");
				return;
			} else {
				adxInstallXmlDoc = dBuilder.parse(fileAdxinstalls);
			}

			// TODO : FRDEPO
			// XMLHelper.cleanEmptyTextNodes((Node)xdoc);

			Document elemSpecDoc = dBuilder.parse(in);
			Element moduleSpec = (Element) elemSpecDoc.getDocumentElement().getElementsByTagName("module").item(0);
			String moduleName = moduleSpec.getAttribute("name");
			String moduleFamily = moduleSpec.getAttribute("family");
			// IXMLElement moduleSpec = elemSpecDoc.getFirstChildNamed("module");

			NodeList listAdxInstallsNodes = adxInstallXmlDoc.getDocumentElement().getElementsByTagName("module");
			for (int i = 0; i < listAdxInstallsNodes.getLength(); i++) {
				Element aNode = (Element) listAdxInstallsNodes.item(i);

				if (aNode.getAttribute("name").equals(moduleName)
						&& aNode.getAttribute("type").equals(moduleSpec.getAttribute("type"))
						&& aNode.getAttribute("family").equals(moduleFamily)) {
					reportModule = aNode;
					break;
				}

			}

			// module non trouvé :(
			if (reportModule == null) {
				logger.log(Level.FINE, "AdxCompUninstallerListener.beforeDeletion  OK => module " + moduleName + "/"
						+ moduleFamily + " not in " + fileAdxinstalls.getAbsolutePath());
				return;
			}

			// Report module found
			logger.log(Level.FINE, "AdxCompUninstallerListener.beforeDeletion  module " + moduleName + "/"
					+ moduleFamily + " has been found in " + fileAdxinstalls.getAbsolutePath());

			NodeList lstChilds = reportModule.getElementsByTagName("*");
			for (int i = 0; i < lstChilds.getLength(); i++) {
				Element elem = (Element) lstChilds.item(i);

				if (elem.getTagName().endsWith(".installstatus")) {
					String modstatus = elem.getTextContent();

					if (!"idle".equalsIgnoreCase(modstatus)) {

						ResourcesHelper helper = new ResourcesHelper(installData, resources);
						String errorMsg = ResourcesHelper.getCustomPropString("installer.error"); // ResourceBundle.getBundle("com/sage/izpack/messages").getString("installer.error");
						String notidleMsg = helper.getCustomString("notidle", false);

						String friendlyMsg = errorMsg + ": module not idle (Status: " + modstatus + ") " + notidleMsg;
						logger.log(Level.SEVERE, "AdxCompUninstallerListener.beforeDeletion  " + friendlyMsg);

						GetPromptUIHandler().emitError(errorMsg, friendlyMsg);

						System.exit(1);
					}
				}
			}

			reportModule.getParentNode().removeChild(reportModule);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(adxInstallXmlDoc);
			StreamResult result = new StreamResult(fileAdxinstalls);

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			return;
		} catch (IzPackException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new IzPackException(exception);
		}

	}

	@Override
	public boolean isFileListener() {

		return false;
	}

	@Override
	public void afterDelete(File arg0) {


	}

	@Override
	public void afterDelete(List<File> arg0, ProgressListener arg1) {


	}

}
