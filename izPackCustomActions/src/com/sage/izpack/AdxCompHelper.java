package com.sage.izpack;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryHandler;

/**
 * Read/Save/Update Xml file adxinstalls.xml
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
	// public static final String ADXADMIN_REG_KeyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";

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

	public Document getAdxInstallDocument() throws FileNotFoundException, NativeLibException, IOException, Exception {
		String logPrefix = "AdxCompHelper - ";
		String adxAdminPath = this.getAdxAdminPath();
		if (adxAdminPath == null || "".equals(adxAdminPath)) {

			System.out.println(logPrefix + "OK => AdxAdmin not found.");
			return null;
		}

		java.io.File dirAdxDir = new java.io.File(adxAdminPath);
		if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) {
			// throw new Exception(langpack.getString("adxadminParseError"));
			System.out.println(logPrefix + ResourcesHelper.getCustomPropString("adxadminParseError"));
		// ResourceBundle.getBundle("com/sage/izpack/messages").getString("adxadminParseError"));
			return null;
		}
		java.io.File fileAdxinstalls = this.getAdxInstallFile(dirAdxDir);
		System.out.println(logPrefix + "Reading XML file fileAdxinstalls: " + fileAdxinstalls.getAbsolutePath());

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document adxInstallXmlDoc = null;

		if (!fileAdxinstalls.exists()) {
			System.out.println(logPrefix + fileAdxinstalls.getAbsolutePath() + " doesn't exist.");
			return null;
		} else {
			adxInstallXmlDoc = dBuilder.parse(fileAdxinstalls);
			System.out.println(logPrefix + "FileAdxinstalls: " + fileAdxinstalls.getAbsolutePath() + " read.");
		}
		
		return adxInstallXmlDoc;
	}
	

	
	
	public boolean isAdminSetup() {
		RegistryHandlerX3 rh = new RegistryHandlerX3(this.registryHandler, this.installData);
		return rh.isAdminSetup();		
	}
	
	/*
	 * We need to find adxadmin path directory: 
	 * Ex: directory "C:\Sage\SafeX3\ADXADMIN"
	 */
	public String getAdxAdminPath() throws NativeLibException, Exception, FileNotFoundException, IOException {

		if (this.installData != null) {
			logger.log(Level.FINE, "AdxCompHelper  Init registry installData Locale: "
					+ this.installData.getLocaleISO2() + " getInstallPath: " + this.installData.getInstallPath());
		}

		RegistryHandlerX3 rh = new RegistryHandlerX3(this.registryHandler, this.installData);
		String adxAdminPath = rh.getAdxAdminDirPath();
		logger.log(Level.FINE, "AdxCompHelper  Init RegistryHandlerX3. adxAdminPath: " + adxAdminPath);

		// Test AdxAdmin is already installed. Read registry
		if (adxAdminPath == null)  {
			// Exception(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("adxadminNotRegistered"));
			throw new Exception(ResourcesHelper.getCustomPropString("adxadminNotRegistered"));
			// throw new Exception("You must install an adxadmin administration runtime
			// first. Exiting now.");
		}

		return adxAdminPath;
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
