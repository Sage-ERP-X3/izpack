package com.sage.izpack;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.izforge.izpack.api.event.AbstractUninstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.helper.SpecHelper;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;

/*
 * @author Franck DEPOORTERE
 */
public class AdxCompUninstallerListener extends AbstractUninstallerListener {

	private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";

	private com.izforge.izpack.api.data.InstallData installData;
	private RegistryHandler registryHandler;
	private Prompt prompt;
	private Resources resources;
	private Messages messages;
	private SpecHelper specHelper = null;

	public AdxCompUninstallerListener(RegistryDefaultHandler handler, Resources resources, Messages messages,
			Prompt prompt) {

		// Doesn't seem to be possible to get InstallData installData, UninstallData
		// uninstallData in this type of class

		super();

		this.registryHandler = handler.getInstance();
		// this.installData = installData;
		this.prompt = prompt;
		this.resources = resources;
		this.messages = messages;
	}

	@Override
	public void initialise() {
		System.out.println("AdxCompUninstallerListener  initialise");

	}

	private AbstractUIHandler GetPromptUIHandler() {

		AbstractUIHandler handler = new PromptUIHandler(this.prompt);
		return handler;
	}

	@Override
	public void beforeDelete(List<File> arg0) {

		System.out.println("AdxCompUninstallerListener.beforeDelete(List<File>arg0: " + arg0 + ")");

		beforeDeletion();

	}

	@Override
	public void beforeDelete(List<File> arg0, ProgressListener arg1) {

		System.out.println("AdxCompUninstallerListener.beforeDelete(List<File> arg0, ProgressListener arg1)");

		beforeDeletion();

	}

	@Override
	public void beforeDelete(File arg0) {

		System.out.println("AdxCompUninstallerListener.beforeDelete(File arg0:" + arg0 + ")");

	}

	private void beforeDeletion() {
		try {

			String logPrefix = "AdxCompUninstallerListener.beforeDeletion - ";
			this.specHelper = new SpecHelper(this.resources);
			/*
			 * try { this.specHelper.readSpec(SPEC_FILE_NAME); } catch (Exception e) {
			 * e.printStackTrace(); }
			 */
			System.out.println(logPrefix + "");

			InputStream in = resources.getInputStream(SPEC_FILE_NAME);
			ObjectInputStream objIn = new ObjectInputStream(in);

			String obj = (String) objIn.readObject();
			System.out.println(" obj: " + obj + " type: " + obj.getClass().getTypeName());

			this.prompt.error(logPrefix + "obj: " + obj + " type: " + obj.getClass().getTypeName());

			Element elemSpecDoc = AdxCompHelper.asXml(obj);
			objIn.close();
			in.close();

			// Load the defined adx module to be deleted.

			// No actions, nothing to do.
			if (elemSpecDoc == null) {
				System.out.println(logPrefix + SPEC_FILE_NAME + " not found.");
				this.prompt.error(logPrefix + SPEC_FILE_NAME + " not found.");
				return;
			}

			// System.out.println("AdxCompUninstallerListener.beforeDeletion().
			// this.specHelper.getSpec: " +
			// AdxCompHelper.asByteString((Element)this.specHelper.getSpec().getElement(),
			// null));
			System.out.println(logPrefix + SPEC_FILE_NAME + " : " + AdxCompHelper.asString(elemSpecDoc, null));

			this.prompt.error(logPrefix + SPEC_FILE_NAME + " : " + AdxCompHelper.asString(elemSpecDoc, null));

			// get file adxinstalls

			// here we need to update adxinstalls.xml

			// we need to find adxadmin path

			// verify adxAdminPath

			AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, this.installData);
			String adxAdminPath = adxCompHelper.getAdxAdminPath();
			if (adxAdminPath == null || "".equals(adxAdminPath)) {

				System.out.println(logPrefix + "OK => AdxAdmin not found.");
				return;
			}

			java.io.File dirAdxDir = new java.io.File(adxAdminPath);
			if (!dirAdxDir.exists() || !dirAdxDir.isDirectory())
				// throw new Exception(langpack.getString("adxadminParseError"));
				throw new Exception(ResourcesHelper.getCustomPropString("adxadminParseError"));
			// ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));

			java.io.File fileAdxinstalls = adxCompHelper.getAdxInstallFile(dirAdxDir);
			System.out.println(logPrefix + "Reading XML file fileAdxinstalls: " + fileAdxinstalls.getAbsolutePath());

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document adxInstallXmlDoc = null;

			if (!fileAdxinstalls.exists()) {
				System.out.println(logPrefix + fileAdxinstalls.getAbsolutePath() + " doesn't exist.");
				return;
			} else {
				adxInstallXmlDoc = dBuilder.parse(fileAdxinstalls);
				System.out.println(logPrefix + "FileAdxinstalls: " + fileAdxinstalls.getAbsolutePath() + " read.");
			}

			boolean isAdxAdminB = false;
			try {
				Object varObject = resources.getObject("variables");
				if (varObject != null && (varObject instanceof com.izforge.izpack.api.data.Variables)) {
					com.izforge.izpack.api.data.Variables variables = (com.izforge.izpack.api.data.Variables) varObject;
					String isAdxAdmin = (variables != null) ? variables.get("is-adxadmin") : null;
					isAdxAdminB = isAdxAdmin != null && isAdxAdmin.equalsIgnoreCase("true");
				}
			} catch (Exception e) {
				throw new InstallerException(e);
			}
			// TODO: FRDEPO => how to debug ?
			// this.specHelper.getResource(adxAdminPath)
			// Object varObject = resources.getObject("variables");
			// if (varObject!= null && varObject instanceof
			// com.izforge.izpack.api.data.Variables)
			// variables = (com.izforge.izpack.api.data.Variables) varObject;
			// if (adxCompHelper.isAdminSetup()) {
			if (isAdxAdminB) {

				NodeList listAdxInstallsNodes = adxInstallXmlDoc.getDocumentElement().getElementsByTagName("module");
				if (listAdxInstallsNodes != null && listAdxInstallsNodes.getLength() > 0) {
					// remaining modules children // cancel installation !
					System.out.println(logPrefix + "uninstaller.adxadmin.remainingmodules ");
					throw new InstallerException(
							ResourcesHelper.getCustomPropString("uninstaller.adxadmin.remainingmodules"));
					// JOptionPane.showMessageDialog(thisJframe,
					// "uninstaller.adxadmin.remainingmodules", title, JOptionPane.ERROR_MESSAGE );
					// destroyButton.setEnabled(false);
				}

				// if (data.hasChildren() && data.getFirstChildNamed("module")!=null) { //
				// JOptionPane.showMessageDialog(thisJframe,
				// langpack.getString("uninstaller.adxadmin.remainingmodules"), title,
				// JOptionPane.ERROR_MESSAGE ); destroyButton.setEnabled(false); return; } }
				// catch (Exception ex) { JOptionPane.showMessageDialog(thisJframe,
				// langpack.getString("uninstaller.adxadmin.errparseadxinstall"), title,
				// JOptionPane.ERROR_MESSAGE ); destroyButton.setEnabled(false); return; } } }
				//

			} else {

				String moduleName = elemSpecDoc.getAttribute("name");
				String moduleFamily = elemSpecDoc.getAttribute("family");

				System.out.println(logPrefix + "moduleSpec 0: + " + AdxCompHelper.asString(elemSpecDoc, "utf-8")
						+ "  moduleName found: " + moduleName + "   moduleFamily found: " + moduleFamily);

				// Get current module: Report, Runtime ...
				Element adxXmlModule = getModule(adxInstallXmlDoc, elemSpecDoc, moduleName, moduleFamily);

				// module not found :(
				if (adxXmlModule == null) {
					System.out.println(logPrefix + "module " + moduleName + "/" + moduleFamily + " not in "
							+ fileAdxinstalls.getAbsolutePath() + ". Check finished.");
					return;
				}

				System.out.println(logPrefix + "module " + moduleName + "/" + moduleFamily + " found in "
						+ fileAdxinstalls.getAbsolutePath() + " Remove XML and document.");

				cleanAndSave(fileAdxinstalls, adxInstallXmlDoc, moduleName, moduleFamily, adxXmlModule);
			}
		} catch (IzPackException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new IzPackException(exception);
		}
	}

	private void cleanAndSave(java.io.File fileAdxinstalls, Document adxInstallXmlDoc, String moduleName,
			String moduleFamily, Element moduleToRemove) throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException, ParserConfigurationException {

		NodeList lstChilds = moduleToRemove.getElementsByTagName("*");
		for (int i = 0; i < lstChilds.getLength(); i++) {
			Element elem = (Element) lstChilds.item(i);

			if (elem.getTagName().endsWith(".installstatus")) {
				String modstatus = elem.getTextContent();

				if (!"idle".equalsIgnoreCase(modstatus)) {

					// ResourcesHelper helper = new ResourcesHelper(installData, resources);
					String errorMsg = "Error"; // ResourcesHelper.getCustomPropString("installer.error"); //
												// ResourceBundle.getBundle("com/sage/izpack/messages").getString("installer.error");
					// String notidleMsg = helper.getCustomString("notidle", false);
					String notidleMsg = "notidle";
					String friendlyMsg = errorMsg + ": module not idle (Status: " + modstatus + ") " + notidleMsg;
					GetPromptUIHandler().emitError(errorMsg, friendlyMsg);
					System.exit(1);
				}
			}
		}

		moduleToRemove.getParentNode().removeChild(moduleToRemove);

		AdxCompHelper.saveXml(fileAdxinstalls, adxInstallXmlDoc, AdxCompHelper.getTransformer(null));
	}

	private Element getModule(Document adxInstallXmlDoc, Element moduleSpec, String moduleName, String moduleFamily) {
		Element reportModule = null;
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
		return reportModule;
	}

	@Override
	public boolean isFileListener() {

		return true;
	}

	@Override
	public void afterDelete(File arg0) {

	}

	@Override
	public void afterDelete(List<File> arg0, ProgressListener arg1) {

	}

}
