package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.AbstractUninstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.helper.SpecHelper;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.util.Platform;

/*
 * @author Franck DEPOORTERE
 */
public class AdxCompUninstallerListener extends AbstractUninstallerListener {

	private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";

	private RegistryHandler registryHandler;
	private Prompt prompt;
	private Resources resources;
	private Messages messages;
	private SpecHelper specHelper = null;
	private static String LogPrefix = "AdxCompUninstallerListener - ";

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
		System.out.println(LogPrefix + "initialise");

	}

	private AbstractUIHandler GetPromptUIHandler() {

		AbstractUIHandler handler = new PromptUIHandler(this.prompt);
		return handler;
	}

	@Override
	public void beforeDelete(List<File> arg0) {

		System.out.println(LogPrefix + ".beforeDelete(List<File>arg0: " + arg0 + ")");

		beforeDeletion();

	}

	@Override
	public void beforeDelete(List<File> arg0, ProgressListener arg1) {

		System.out.println(LogPrefix + ".beforeDelete(List<File> arg0, ProgressListener arg1)");

		beforeDeletion();

	}

	@Override
	public void beforeDelete(File arg0) {

		System.out.println(LogPrefix + ".beforeDelete(File arg0:" + arg0 + ")");

	}

	private void beforeDeletion() {
		try {
			String logPrefix = "AdxCompUninstallerListener.beforeDeletion - ";
			System.out.println(logPrefix + "");
			this.specHelper = new SpecHelper(this.resources);

			Element elemSpecDoc = readAdxIzInstaller();
			// If there is no XML component linked to AdxAdmin, there is nothing to do.
			if (elemSpecDoc == null) {
				System.out.println(logPrefix + SPEC_FILE_NAME + " not found. Nothing to do.");
				return;
			}
			// this.prompt.error(logPrefix + SPEC_FILE_NAME + " : " +
			// AdxCompHelper.asString(elemSpecDoc, null));
			// we need to update adxinstalls.xml to remove the XML module: Runtime,
			// PrintServer, ...
			cleanAdxInstallXml(logPrefix, elemSpecDoc);

			boolean isAdxAdmin = isAdxAdmin();
			if (isAdxAdmin) {
				AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, null);
				Document adxInstallXmlDoc = adxCompHelper.getAdxInstallDoc();
				if (adxInstallXmlDoc == null) {
					System.out.println(adxCompHelper.getAdxAdminPath() + " doesn't exist or cannot be opened.");
					return;
				}
				NodeList listAdxInstallsNodes = adxInstallXmlDoc.getDocumentElement().getElementsByTagName("module");
				int nodes = 0;
				if (listAdxInstallsNodes != null)
					nodes = listAdxInstallsNodes.getLength();
				if (nodes > 0) {
					// remaining modules children: cancel installation !
					String remaining = "remaining modules children: cancel installation !";
					if (this.resources != null)
						remaining = this.resources.getString("uninstaller.adxadmin.remainingmodules");
					System.out.println(remaining);
					GetPromptUIHandler().emitError("Error", remaining);
					System.exit(1);
					//return;
				}
			}

		} catch (IzPackException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new IzPackException(exception);
		}
	}

	/*
	 * AdxAdmin XML component
	 */
	private Element readAdxIzInstaller() {
		Element elemSpecDoc = null;
		try {
			InputStream in = resources.getInputStream(SPEC_FILE_NAME);
			ObjectInputStream objIn = new ObjectInputStream(in);
			String obj = (String) objIn.readObject();
			// this.prompt.error(logPrefix + "obj: " + obj + " type: " +
			// obj.getClass().getTypeName());
			elemSpecDoc = AdxCompHelper.asXml(obj);
			objIn.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return elemSpecDoc;
	}

	private void cleanAdxInstallXml(String logPrefix, Element elemSpecDoc) throws TransformerFactoryConfigurationError,
			FileNotFoundException, NativeLibException, IOException, Exception {
		String moduleName = elemSpecDoc.getAttribute("name");
		String moduleFamily = elemSpecDoc.getAttribute("family");

		AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, null);
		Document adxInstallXmlDoc = adxCompHelper.getAdxInstallDoc();
		java.io.File adxAdminDir = new java.io.File(adxCompHelper.getAdxAdminPath());
		System.out.println(logPrefix + "moduleSpec 0: + " + AdxCompHelper.asString(elemSpecDoc, "utf-8")
				+ "  moduleName found: " + moduleName + "   moduleFamily found: " + moduleFamily);

		// Get current module: Report, Runtime ...
		Element adxXmlModule = getModule(adxInstallXmlDoc, elemSpecDoc, moduleName, moduleFamily);

		// module not found :(
		if (adxXmlModule == null) {
			System.out.println(logPrefix + "module " + moduleName + "/" + moduleFamily + " not in "
					+ adxAdminDir.getAbsolutePath() + ". Check finished.");
			return;
		}

		System.out.println(logPrefix + "module " + moduleName + "/" + moduleFamily + " found in "
				+ adxAdminDir.getAbsolutePath() + " Remove XML and document.");

		cleanAndSave(adxCompHelper.getAdxInstallFile(adxAdminDir), adxInstallXmlDoc, moduleName, moduleFamily,
				adxXmlModule);
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

					ResourcesHelper helper = new ResourcesHelper(null, resources);
					helper.mergeCustomMessages(messages);
					String errorMsg = "Error";
					String notidleMsg = "notidle";
					String friendlyMsg = helper.getCustomString(notidleMsg);
					if (friendlyMsg == null)
						friendlyMsg = errorMsg + ": module not idle (Status: " + modstatus + ") " + notidleMsg;
					GetPromptUIHandler().emitWarning(errorMsg, friendlyMsg);
					System.exit(1);
				}
			}
		}

		moduleToRemove.getParentNode().removeChild(moduleToRemove);

		AdxCompHelper.saveXml(fileAdxinstalls, adxInstallXmlDoc, AdxCompHelper.getTransformer("UTF-8"));
	}

	private Element getModule(Document adxInstallXmlDoc, Element moduleSpec, String moduleName, String moduleFamily) {
		Element result = null;
		NodeList listAdxInstallsNodes = adxInstallXmlDoc.getDocumentElement().getElementsByTagName("module");
		for (int i = 0; i < listAdxInstallsNodes.getLength(); i++) {
			Element aNode = (Element) listAdxInstallsNodes.item(i);

			if (aNode.getAttribute("name").equals(moduleName)
					&& aNode.getAttribute("type").equals(moduleSpec.getAttribute("type"))
					&& aNode.getAttribute("family").equals(moduleFamily)) {
				result = aNode;
				break;
			}
		}
		return result;
	}

	private boolean isAdxAdmin() throws Exception {
		boolean isAdxAdminB = isAdxAdminFromVariables();
		if (!isAdxAdminB) {
			InstallData installData = isAdxAdminFromInformations();
			String isAdxAdmin = installData.getVariable("is-adxadmin");
			isAdxAdminB = ((isAdxAdmin != null) ? isAdxAdmin.compareToIgnoreCase("true") >= 0 : false);
			if (!isAdxAdminB) {
				isAdxAdminB = isAdxAdminFromPath();
			}
		}
		return isAdxAdminB;
	}

	private boolean isAdxAdminFromVariables() {
		boolean isAdxAdminB = false;
		try {
			Object varObject = resources.getObject("variables");
			if (varObject != null && (varObject instanceof com.izforge.izpack.api.data.Variables)) {
				com.izforge.izpack.api.data.Variables variables = (com.izforge.izpack.api.data.Variables) varObject;
				String isAdxAdmin = (variables != null) ? variables.get("is-adxadmin") : null;
				isAdxAdminB = isAdxAdmin != null && isAdxAdmin.equalsIgnoreCase("true");
			}
		} catch (Exception e) {
			return false;
			// throw new InstallerException(e);
		}
		return isAdxAdminB;
	}

	private String getInstallPath() {
		String result = null;
		try {
			InputStream in = getClass().getResourceAsStream("/install.log");
			InputStreamReader inReader = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(inReader);
			result = reader.readLine();
			reader.close();
		} catch (IOException exception) {
			System.err
					.println("AdxCompUninstallerListener: unable to determine install path: " + exception.getMessage());
		}
		return result;
	}

	private InstallData isAdxAdminFromInformations() throws Exception {

		AutomatedInstallData installData = new AutomatedInstallData(new DefaultVariables(),
				new Platform(Platform.Name.WINDOWS));
		Map<String, Pack> result = InstallationInformationHelper.loadInstallationInformation(getInstallPath(),
				installData, resources);
		return installData;
	}

	private boolean isAdxAdminFromPath() {

		String installPath = getInstallPath();
		AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, null);
		String adxAdminPath = null;
		try {
			adxAdminPath = adxCompHelper.getAdxAdminPath();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (installPath != null && adxAdminPath != null && installPath.equalsIgnoreCase(adxAdminPath)) {
			return true;
		}
		return false;
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
