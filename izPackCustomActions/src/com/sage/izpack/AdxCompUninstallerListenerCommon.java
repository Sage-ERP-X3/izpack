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
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.izforge.izpack.api.exception.WrappedNativeLibException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.helper.SpecHelper;

/*
 * Manage XML file 'inst\adxinstalls.xml' while uninstalling the product
 * C:\Sage\SafeX3\ADXADMIN\inst\adxinstalls.xml
 * 
 * @author Franck DEPOORTERE
 */
public abstract class AdxCompUninstallerListenerCommon extends AbstractUninstallerListener {

	public static final String PrivilegesFriendlyMessage = "It looks that you don't have enough rights. You need to launch the 'Uninstaller' program from 'Add or remove programs' to get all privileges. ";
	public static final String SPEC_FILE_NAME = "AdxCompSpec.xml";
	private static final String LogPrefix = "AdxCompUninstallerListenerCommon - ";
	private static final Logger logger = Logger.getLogger(AdxCompUninstallerListenerCommon.class.getName());

	protected final Resources resources;
	protected final Prompt prompt;
	protected final Messages messages;
	protected SpecHelper specHelper = null;

	protected static boolean processDone = false;

	public AdxCompUninstallerListenerCommon(Resources resources, Messages messages, Prompt prompt) {
		super();

		this.prompt = prompt;
		this.resources = resources;
		this.messages = messages;
		this.specHelper = new SpecHelper(this.resources);
	}

	protected void emitWarning(String title, String message) {
		AbstractUIHandler UIHandler = new PromptUIHandler(this.prompt);
		if (this.prompt != null && UIHandler != null)	
			UIHandler.emitWarning(title, message);
		else
			System.err.println(message);
	}

	protected void emitError(String message, Exception exceptionMesg) {
		AbstractUIHandler UIHandler = new PromptUIHandler(this.prompt);
		if (this.prompt != null && UIHandler != null)
			UIHandler.emitError("Error", message);
		else
			System.err.println(message);

		if (exceptionMesg != null)
			System.err.println(exceptionMesg.getMessage());
		
	}

	@Override
	public void initialise() {
		logger.log(Level.FINE, LogPrefix + "initialise");
	}

	@Override
	public void beforeDelete(List<File> arg0) {

		logger.log(Level.FINE, LogPrefix + ".beforeDelete(List<File>arg0: " + arg0 + ")");
		super.beforeDelete(arg0);
		this.beforeDeletion();
	}

	@Override
	public void beforeDelete(List<File> arg0, ProgressListener arg1) {

		logger.log(Level.FINE, LogPrefix + ".beforeDelete(List<File> arg0, ProgressListener arg1)");
		super.beforeDelete(arg0, arg1);
		this.beforeDeletion();
	}

	@Override
	public void beforeDelete(File arg0) {

		logger.log(Level.FINE, LogPrefix + ".beforeDelete(File arg0:" + arg0 + ")");
		super.beforeDelete(arg0);
		this.beforeDeletion();
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

	protected Document getAdxInstallDocument()
			throws FileNotFoundException, NativeLibException, IOException, Exception {
		AdxCompHelper adxCompHelper = new AdxCompHelper(null, null);
		Document adxInstallXmlDoc = adxCompHelper.getAdxInstallDocument();
		return adxInstallXmlDoc;
	}

	/*
	 * @return: Ex: "C:\Sage\SafeX3\ADXADMIN"
	 */
	protected String getAdxAdminPath() throws FileNotFoundException, NativeLibException, IOException, Exception {
		AdxCompHelper adxCompHelper = new AdxCompHelper(null, null);
		return adxCompHelper.getAdxAdminPath();
	}

	/**
	 * @param adxAdminDir
	 * @return Ex: C:\Sage\SafeX3\ADXADMIN\inst\adxinstalls.xml
	 */
	protected File getAdxInstallFile(File adxAdminDir) {
		AdxCompHelper adxCompHelper = new AdxCompHelper(null, null);
		return adxCompHelper.getAdxInstallFile(adxAdminDir);
	}

	/**
	 * Delete /sage/adxadm or c:\\sage\\adxadm file
	 * 
	 * @return
	 */
	private boolean deleteAdxAdmFile() {

		boolean result = false;
		try {
			String path = RegistryHandlerX3.AdxAdmFileWindows; // "c:\\sage\\adxadm";
			if (OsVersion.IS_UNIX) {
				path = RegistryHandlerX3.AdxAdmFileLinux; // "/sage/adxadm";
			}
			java.io.File adxadmFile = new java.io.File(path);
			if (!adxadmFile.exists()) {
				return false;
			}
			adxadmFile.delete();
			result = true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			// GetPromptUIHandler().emitError("Error", ex.getMessage());
			emitError("Error", ex);
		}
		return result;
	}

	/*
	 * Remove module from Xml file [ADXADMIN_DIR]\inst\adxinstalls.xml
	 */
	private void cleanAdxInstallXml(String logPrefix, Element elemSpecDoc) throws TransformerFactoryConfigurationError,
			FileNotFoundException, NativeLibException, IOException, Exception {
		String moduleName = elemSpecDoc.getAttribute("name");
		String moduleFamily = elemSpecDoc.getAttribute("family");

		Document adxInstallXmlDoc = this.getAdxInstallDocument();
		java.io.File adxAdminDir = new java.io.File(this.getAdxAdminPath());
		java.io.File adxInstallFile = getAdxInstallFile(adxAdminDir);
		if (adxInstallXmlDoc == null && !adxInstallFile.exists()) {
			System.out.println(logPrefix + "Xml file " + adxInstallFile.getAbsolutePath() + " doesn't exist.");
			return;
		}

		logger.log(Level.FINE, logPrefix + "moduleSpec 0: + " + AdxCompHelper.asString(elemSpecDoc, "utf-8")
				+ "  moduleName found: " + moduleName + "   moduleFamily found: " + moduleFamily);

		// Get current module: Report, Runtime ...
		Element adxXmlModule = getModule(adxInstallXmlDoc, elemSpecDoc, moduleName, moduleFamily);

		// Xml module not found :(
		if (adxXmlModule == null) {
			System.out.println(logPrefix + "module " + moduleName + "/" + moduleFamily + " not in "
					+ adxAdminDir.getAbsolutePath() + ". Check finished.");
			return;
		}

		logger.log(Level.FINE, logPrefix + "module " + moduleName + "/" + moduleFamily + " found in "
				+ adxAdminDir.getAbsolutePath() + " Remove XML and document.");

		cleanAndSave(this.getAdxInstallFile(adxAdminDir), adxInstallXmlDoc, moduleName, moduleFamily, adxXmlModule);
	}

	protected void beforeDeletion() {

		String logPrefix = "AdxCompUninstallerListener.beforeDeletion - ";
		logger.log(Level.FINE, logPrefix + "");

		if (processDone) {
			logger.log(Level.FINE, logPrefix + " skipped. processDone");
			return;
		}

		try {

			Element elemSpecDoc = readAdxIzInstaller();
			// If there is no XML component linked to AdxAdmin, there is nothing to do.
			if (elemSpecDoc == null) {
				logger.log(Level.FINE, logPrefix + SPEC_FILE_NAME + " not found. Nothing to do.");
			} else {
				// we need to update adxinstalls.xml to remove the XML module: Runtime,
				// PrintServer, DbOra, DbSql...
				cleanAdxInstallXml(logPrefix, elemSpecDoc);
			}

			// The current uninstaller setup is AdxAdmin ?
			boolean isAdxAdmin = isAdxAdmin();
			if (isAdxAdmin) {

				Document adxInstallXmlDoc = this.getAdxInstallDocument();
				if (adxInstallXmlDoc == null) {
					logger.log(Level.FINE, this.getAdxAdminPath() + " doesn't exist or cannot be opened.");
				} else {
					NodeList listAdxInstallsNodes = adxInstallXmlDoc.getDocumentElement()
							.getElementsByTagName("module");
					int nodes = 0;
					if (listAdxInstallsNodes != null)
						nodes = listAdxInstallsNodes.getLength();
					if (nodes > 0) {
						String remaining = this.getString("uninstaller.adxadmin.remainingmodules",
								"remaining modules children: cancel installation !");
						System.out.println(remaining);
						emitError(remaining, null);
						System.exit(1);
					}
				}
				deleteAdxAdmFile();
			}

			this.processDone = true;
		} catch (WrappedNativeLibException exception) {
			emitError(this.getString("privilegesIssue", PrivilegesFriendlyMessage), exception);

			throw exception;
		} catch (Exception exception) {
			emitError(this.getString("privilegesIssue", PrivilegesFriendlyMessage), exception);
			throw new IzPackException(exception);
		}
	}

	protected boolean isAdxAdmin() throws Exception {
		boolean isAdxAdminB = isAdxAdminFromPath();
		if (!isAdxAdminB) {
			isAdxAdminB = isAdxAdminFromVariables();
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
				logger.log(Level.FINE, LogPrefix + "isAdxAdminFromVariables returns " + isAdxAdminB);
			}
		} catch (Exception e) {
			return false;
			// throw new InstallerException(e);
		}
		return isAdxAdminB;
	}

	protected boolean isAdxAdminFromPath() {

		String installPath = getInstallPath();
		String adxAdminPath = null;
		try {
			adxAdminPath = this.getAdxAdminPath();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (installPath != null && adxAdminPath != null && installPath.equalsIgnoreCase(adxAdminPath)) {
			return true;
		}
		return false;
	}

	protected String getInstallPath() {
		String result = null;
		try {
			InputStream in = getClass().getResourceAsStream("/install.log");
			InputStreamReader inReader = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(inReader);
			result = reader.readLine();
			if (result != null)
				result = result.trim();
			reader.close();
		} catch (Exception exception) {
			System.err.println(LogPrefix + "unable to determine install path: " + exception.getMessage());
		}
		return result;
	}

	/*
	 * AdxAdmin XML component: AdxCompSpec.xml
	 */
	protected Element readAdxIzInstaller() {
		Element elemSpecDoc = null;
		try {
			InputStream in = resources.getInputStream(SPEC_FILE_NAME);
			ObjectInputStream objIn = new ObjectInputStream(in);
			String obj = (String) objIn.readObject();
			elemSpecDoc = AdxCompHelper.asXml(obj);
			objIn.close();
			in.close();
		} catch (Exception exception) {
			System.err.println(LogPrefix + "Cannot read " + SPEC_FILE_NAME + " - " + exception.getMessage());
			// e.printStackTrace();
		}
		return elemSpecDoc;
	}

	protected String getString(String resourceId, String defaultTranslation) {
		ResourcesHelper helper = new ResourcesHelper(null, resources);
		if (messages != null)
			helper.mergeCustomMessages(messages);
		String result = helper.getCustomString(resourceId);
		if (result == null)
			result = defaultTranslation;
		return result;
	}

	protected void cleanAndSave(java.io.File fileAdxinstalls, Document adxInstallXmlDoc, String moduleName,
			String moduleFamily, Element moduleToRemove) throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException, ParserConfigurationException {

		NodeList lstChilds = moduleToRemove.getElementsByTagName("*");
		for (int i = 0; i < lstChilds.getLength(); i++) {
			Element elem = (Element) lstChilds.item(i);

			if (elem.getTagName().endsWith(".installstatus")) {
				String modstatus = elem.getTextContent();

				if (!"idle".equalsIgnoreCase(modstatus)) {
					String errorMsg = "Error";
					String notidleMsg = "notidle";
					String friendlyMsg = this.getString(notidleMsg,
							errorMsg + ": module not idle (Status: " + modstatus + ") " + notidleMsg);
					// GetPromptUIHandler().emitWarning(errorMsg, friendlyMsg);
					emitWarning(errorMsg, friendlyMsg);
					System.exit(1);
				}
			}
		}
		moduleToRemove.getParentNode().removeChild(moduleToRemove);
		AdxCompHelper.saveXml(fileAdxinstalls, adxInstallXmlDoc, AdxCompHelper.getTransformer("UTF-8"));
	}

	protected Element getModule(Document adxInstallXmlDoc, Element moduleSpec, String moduleName, String moduleFamily) {
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

}
