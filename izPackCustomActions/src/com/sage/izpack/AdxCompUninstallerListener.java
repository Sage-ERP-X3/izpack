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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.event.AbstractUninstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
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
	// implements CleanupClient {

	// private static final Logger logger =
	// Logger.getLogger(AdxCompUninstallerListener.class.getName());

	private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";

	private com.izforge.izpack.api.data.InstallData installData;
	private RegistryHandler registryHandler;
	private Prompt prompt;
	private Resources resources;
	private Messages messages;
	private SpecHelper specHelper = null;

	public AdxCompUninstallerListener(RegistryDefaultHandler handler, Resources resources, Messages messages, Prompt prompt) {
		// InstallData installData,

		super();

		// this.installData = installData;
		this.registryHandler = handler.getInstance();
		this.prompt = prompt;
		this.resources = resources;
		this.messages = messages;
	}

	@Override
	public void initialise() {
		// logger.log(Level.FINE, "AdxCompUninstallerListener.initialise");

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

			this.specHelper = new SpecHelper(this.resources);
			/*
			 * try { this.specHelper.readSpec(SPEC_FILE_NAME); } catch (Exception e) {
			 * e.printStackTrace(); }
			 */

			System.out.println("AdxCompUninstallerListener.beforeDeletion().");

			InputStream in = resources.getInputStream(SPEC_FILE_NAME);
			ObjectInputStream objIn = new ObjectInputStream(in);

			String obj = (String) objIn.readObject();
			System.out.println("AdxCompUninstallerListener.beforeDeletion(). obj: " + obj + " type: "
					+ obj.getClass().getTypeName());

			Element elemSpecDoc = AdxCompHelper.asXml(obj);
			objIn.close();
			in.close();

			// Load the defined adx module to be deleted.
			// InputStream in = this.specHelper.getResource(SPEC_FILE_NAME);
			// InputStream in = getClass().getResourceAsStream("/" + SPEC_FILE_NAME);
			// IXMLElement elemSpec = this.specHelper.getSpec(); // AdxCompSpec.xml

			// if (in == null) { // No actions, nothing todo.
			if (elemSpecDoc == null) {
				System.out.println("AdxCompUninstallerListener.beforeDeletion(). " + SPEC_FILE_NAME + " not found.");
				return;
			}

			// System.out.println("AdxCompUninstallerListener.beforeDeletion().
			// this.specHelper.getSpec: " +
			// AdxCompHelper.asByteString((Element)this.specHelper.getSpec().getElement(),
			// null));
			System.out.println("AdxCompUninstallerListener.beforeDeletion(). " + SPEC_FILE_NAME + " : "
					+ AdxCompHelper.asString(elemSpecDoc, null));

			// get file adxinstalls

			// here we need to update adxinstalls.xml

			// we need to find adxadmin path

			// vérification strAdxAdminPath
			AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, this.installData);
			String adxAdminPath = adxCompHelper.getAdxAdminPath();
			if (adxAdminPath == null || "".equals(adxAdminPath)) {
				System.out.println("AdxCompUninstallerListener.beforeDeletion OK => AdxAdmin not found.");
				return;
			}

			java.io.File dirAdxDir = new java.io.File(adxAdminPath);
			if (!dirAdxDir.exists() || !dirAdxDir.isDirectory())
				// throw new Exception(langpack.getString("adxadminParseError"));
				throw new Exception(ResourcesHelper.getCustomPropString("adxadminParseError"));
			// ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));

			java.io.File fileAdxinstalls = adxCompHelper.getAdxInstallFile(dirAdxDir);
			System.out.println("AdxCompUninstallerListener.beforeDeletion Reading XML file fileAdxinstalls: "
					+ fileAdxinstalls.getAbsolutePath());

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document adxInstallXmlDoc = null;

			if (!fileAdxinstalls.exists()) {
				System.out.println("AdxCompUninstallerListener.beforeDeletion " + fileAdxinstalls.getAbsolutePath()
						+ " doesn't exist.");
				return;
			} else {
				adxInstallXmlDoc = dBuilder.parse(fileAdxinstalls);
				System.out.println("AdxCompUninstallerListener.beforeDeletion FileAdxinstalls: "
						+ fileAdxinstalls.getAbsolutePath() + " read.");
			}

			String moduleName = elemSpecDoc.getAttribute("name");
			String moduleFamily = elemSpecDoc.getAttribute("family");

			System.out.println("AdxCompUninstallerListener.beforeDeletion   moduleSpec 0: + "
					+ AdxCompHelper.asString(elemSpecDoc, "utf-8") + "  moduleName found: " + moduleName
					+ "   moduleFamily found: " + moduleFamily);

			Element reportModule = getReportModule(adxInstallXmlDoc, elemSpecDoc, moduleName, moduleFamily);

			// module non trouvé :(
			if (reportModule == null) {
				System.out.println("AdxCompUninstallerListener.beforeDeletion.  module " + moduleName + "/"
						+ moduleFamily + " not in " + fileAdxinstalls.getAbsolutePath() + ". Check finished.");
				return;
			}

			System.out.println("AdxCompUninstallerListener.beforeDeletion module " + moduleName + "/"
					+ moduleFamily + " found in " + fileAdxinstalls.getAbsolutePath()+ " Remove XML and document." );

			cleanAndSave(fileAdxinstalls, adxInstallXmlDoc, moduleName, moduleFamily, reportModule);

			return;
		} catch (IzPackException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new IzPackException(exception);
		}
	}

	private void cleanAndSave(java.io.File fileAdxinstalls, Document adxInstallXmlDoc, String moduleName,
			String moduleFamily, Element reportModuleToRemove) throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException, ParserConfigurationException {

		NodeList lstChilds = reportModuleToRemove.getElementsByTagName("*");
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

		reportModuleToRemove.getParentNode().removeChild(reportModuleToRemove);

		AdxCompHelper.saveXml(fileAdxinstalls, adxInstallXmlDoc, AdxCompHelper.getTransformer(null));
	}

	private Element getReportModule(Document adxInstallXmlDoc, Element moduleSpec, String moduleName,
			String moduleFamily) {
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
