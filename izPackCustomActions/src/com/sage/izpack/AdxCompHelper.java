package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.util.OsVersion;

/**
 *
 * @author Franck DEPOORTERE
 */
public class AdxCompHelper {

	private static final Logger logger = Logger.getLogger(AdxCompHelper.class.getName());

	/**
	 * adxinstalls.xml
	 */
	public static final String ADX_INSTALL_FILENAME = "adxinstalls.xml";
	/*
	 * SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN
	 */
	public static final String ADXADMIN_REG_KeyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
	/*
	 * SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN
	 */
	public static final String ADXADMIN_REG_KeyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";

	private com.izforge.izpack.api.data.InstallData installData;
	private RegistryHandler registryHandler;

	public AdxCompHelper(RegistryHandler registryHandler, com.izforge.izpack.api.data.InstallData installData) {
		this.registryHandler = registryHandler;
		this.installData = installData;
	}

	/**
	 * 
	 * @param dirAdxDir
	 * @return Ex: C:\Sage\SafeX3\ADXADMIN\inst\adxinstalls.xml
	 */
	public java.io.File getAdxInstallFile(java.io.File dirAdxDir) {

		StringBuilder adxInstallBuilder = new StringBuilder();
		adxInstallBuilder.append(dirAdxDir.getAbsolutePath());
		adxInstallBuilder.append(File.separator);
		adxInstallBuilder.append("inst");
		adxInstallBuilder.append(File.separator);
		adxInstallBuilder.append(AdxCompHelper.ADX_INSTALL_FILENAME);

		return new java.io.File(adxInstallBuilder.toString());
	}

	/*
	 * we need to find adxadmin path
	 */
	public String getAdxAdminPath() throws NativeLibException, Exception, FileNotFoundException, IOException {

		String strAdxAdminPath = "";

		if (this.installData != null) {
			logger.log(Level.FINE,
					"AdxCompHelper  Init registry installData Locale: " + this.installData.getLocaleISO2());
			logger.log(Level.FINE, "AdxCompHelper  Init registry getInstallPath: " + this.installData.getInstallPath());
		}

		RegistryHandlerX3 rh = new RegistryHandlerX3(this.registryHandler, this.installData);
		if (this.registryHandler != null && rh != null) {

			boolean adxAdminRegistered = rh.adxadminProductRegistered();
			logger.log(Level.FINE,
					"AdxCompHelper  Init RegistryHandlerX3. adxadminProductRegistered: " + adxAdminRegistered);

			// Test adxadmin is already installed. Read registry
			// String keyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
			// String keyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
			if (adxAdminRegistered) {

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

				logger.log(Level.FINE, "AdxCompHelper  ADXDIR path: " + strAdxAdminPath + "  Key: " + keyName);

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
				throw new Exception(ResourcesHelper.getCustomPropString("adxadminNotRegistered"));
				// throw new Exception("You must install an adxadmin administration runtime
				// first. Exiting now.");
			}

		} else {
			logger.log(Level.FINE, "AdxCompHelper - Could not get RegistryHandler !");

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
						throw new Exception(ResourcesHelper.getCustomPropString("adxadminNotRegistered"));
						// ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages")
						// .getString("adxadminNotRegistered"));
					}
				}

				FileReader readerAdxAdmFile = new FileReader(adxadmFile);
				BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
				strAdxAdminPath = buffread.readLine();
			}

		}
		return strAdxAdminPath;
	}

	public static String asString(Element elementdoc, String encoding) throws TransformerException {

		Transformer transformer =  getTransformer(encoding);		
		StringWriter writer = new StringWriter();
		Result result = new StreamResult(writer);
		DOMSource source = new DOMSource(elementdoc);
		transformer.transform(source, result);
		return writer.getBuffer().toString();
	}

	
	public static byte[] asByteArray(Element elementdoc, String encoding) throws TransformerException {
				
		return asString(elementdoc, encoding).getBytes();
	}

	
	public static Element asXml(String xmlString) throws TransformerException, SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder =  factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
		return doc.getDocumentElement();
	}
	
	
	public static Element getElementByTag(Document elemSpecDoc, String moduleName) throws XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();
		Element moduleSpec = (Element) xPath.compile("/module").evaluate(elemSpecDoc, XPathConstants.NODE);
		
		return moduleSpec;
	}

	
	/*
	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	transformerFactory.setAttribute("indent-number", 4);
	Transformer transformer = transformerFactory.newTransformer();
	transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
*/

	// Transformer transformer = TransformerFactory.newInstance().newTransformer();
	// transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	// transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
	// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

	public static Transformer getTransformer(String encoding)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException {
		if (encoding == null)
			encoding = "UTF-8";
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number", 2);
		Transformer transformer = transformerFactory.newTransformer();
		// transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		
		return transformer;
	}


	public static void saveXml(java.io.File fileAdxinstalls, Document adxInstallXmlDoc, Transformer transformer)
			throws TransformerException, ParserConfigurationException {
		// It's ok normally, the module is added, recreate the XML

		// write the content into xml filed
		DOMSource source = new DOMSource(adxInstallXmlDoc);
		StreamResult result = new StreamResult(fileAdxinstalls);
		transformer.transform(source, result);
	}

	/*
	private void saveXml(java.io.File fileAdxinstalls, Document adxInstallXmlDoc)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		// write the content into xml file
		Transformer transformer = AdxCompHelper.getTransformer(null);
		DOMSource source = new DOMSource(adxInstallXmlDoc);
		StreamResult result = new StreamResult(fileAdxinstalls);

		// Output to console for testing
		transformer.transform(source, result);
	}
	*/


}
