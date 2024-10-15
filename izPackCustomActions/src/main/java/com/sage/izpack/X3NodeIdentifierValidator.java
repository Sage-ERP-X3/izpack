package com.sage.izpack;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

public class X3NodeIdentifierValidator
		implements DataValidator, com.izforge.izpack.panels.userinput.validator.Validator {

	private static final Logger logger = Logger.getLogger(X3NodeIdentifierValidator.class.getName());

	private static final String X3FAMILY = "X3FAMILY";
	private static final String X3TYPE = "X3TYPE";
	private RegistryHandler registryHandler;
	private InstallData installData;
	private static String LogLabel = "X3NodeIdentifierValidator - ";

	public X3NodeIdentifierValidator(RegistryDefaultHandler handler, InstallData installData) {
		super();
		this.registryHandler = handler.getInstance();
		this.installData = installData;
	}

	@Override
	public boolean getDefaultAnswer() {

		// can we validate in automated mode ?
		// say yes for now
		return false;
	}

	@Override
	public String getErrorMessageId() {
		return "nodealreadyexisterror";
	}

	@Override
	public String getWarningMessageId() {
		return "nodealreadyexistwarn";
	}

	@Override
	public boolean validate(ProcessingClient client) {

		boolean result = false;
		String moduleName = client.getText();
		logger.log(Level.FINE, LogLabel + "validate  ModuleName: " + moduleName);
		String x3Family = client.getConfigurationOptionValue(X3FAMILY);
		String x3Type = client.getConfigurationOptionValue(X3TYPE);
		try {
			result = checkData(moduleName, x3Family, x3Type);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Status validateData(InstallData installData) {

		return DataValidator.Status.OK;
	}

	/**
	 * Check in registry if X3 AdxAdmin has been installed : OK if installed. NOOK
	 * otherwise.
	 * 
	 * @param nodeName
	 * @param x3Family
	 * @param x3Type
	 * @return
	 * @throws Exception
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private boolean checkData(String nodeName, String x3Family, String x3Type)
			throws Exception, ParserConfigurationException, SAXException, IOException {

		logger.log(Level.FINE, LogLabel + "validateData");

		boolean validateResult = false;

		RegistryHandlerX3 x3Handler = new RegistryHandlerX3(this.registryHandler, installData);
		String adxAdminPath = x3Handler.getAdxAdminDirPath();
		boolean adxAdminRegistered = (adxAdminPath != null);
		if (adxAdminRegistered) {
			try {

				if (adxAdminRegistered) {
					logger.log(Level.FINE, LogLabel + "check adxadminProductRegistered: " + adxAdminRegistered);
				} else {
					logger.log(Level.SEVERE, LogLabel + "X3 AdxAdmin not installed or not found in registry.");
				}

				validateResult = checkXmlInstallNode(adxAdminPath, nodeName, x3Family, x3Type);

			} catch (NativeLibException e) {
				logger.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		logger.log(Level.FINE, LogLabel + "validateData: " + validateResult);
		return validateResult;
	}

	/**
	 * Check file "adxinstalls.xml" If "adxinstalls.xml" doesn't exist yet : OK
	 * 
	 * 
	 * @param rh
	 * @param nodename
	 * @param x3Family
	 * @param x3Type
	 * @return
	 * @throws NativeLibException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private boolean checkXmlInstallNode(String adminAdminPath, String nodename, String x3Family, String x3Type)
			throws NativeLibException, ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {

		String adxDir = adminAdminPath;
		logger.log(Level.FINE, LogLabel + "checkXmlInstallNode  Check directory '" + adxDir + "' nodename: '" + nodename
				+ "' X3Family: '" + x3Family + "' X3Type: '" + x3Type + "'");

		java.io.File dirAdxDir = new java.io.File(adxDir);
		if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) {
			throw new NativeLibException(LogLabel + dirAdxDir + " is not found or not a directory");
		}

		StringBuilder fullpathBuilder = new StringBuilder();
		fullpathBuilder.append(dirAdxDir.getAbsolutePath());
		fullpathBuilder.append(File.separator);
		fullpathBuilder.append("inst");
		java.io.File fileAdxinstalls = new java.io.File(fullpathBuilder.toString());
		if (!fileAdxinstalls.exists() || !fileAdxinstalls.isDirectory()) {
			String friendlyMesg = fileAdxinstalls
					+ " is not found or not a directory. Please check you AdxAdmin installation.";
			logger.log(Level.WARNING, friendlyMesg);
			throw new NativeLibException(friendlyMesg);
		}
		fullpathBuilder.append(File.separator);
		fullpathBuilder.append(AdxCompHelper.ADX_INSTALL_FILENAME);

		fileAdxinstalls = new java.io.File(fullpathBuilder.toString());
		if (!fileAdxinstalls.exists()) {
			logger.log(Level.FINE, LogLabel + "checkXmlInstallNode  " + fileAdxinstalls + " doesn't exist yet. OK");
			return true;
		}

		logger.log(Level.FINE, LogLabel + "checkXmlInstallNode  Opening " + fileAdxinstalls + "   nodename: " + nodename
				+ " X3Family: " + x3Family + " X3Type: " + x3Type);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document xdoc = dBuilder.parse(fileAdxinstalls);
		// Element xmlinstall = xdoc.getDocumentElement();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String strPath = "/install/module[@name='" + nodename + "' and @family='" + x3Family + "'";
		if (x3Type != null && !"".equals(x3Type))
			strPath += " and @type='" + x3Type + "'";
		strPath += "]";

		Node module = (Node) xPath.compile(strPath).evaluate(xdoc, XPathConstants.NODE);

		if (module == null) {
			logger.log(Level.FINE, LogLabel + "checkXmlInstallNode  NodeName: " + nodename + " X3Family: " + x3Family
					+ " X3Type: " + x3Type + " NOT found in " + fileAdxinstalls + ": OK");
			return true;
		}

		logger.log(Level.FINE, LogLabel + "checkXmlInstallNode  NodeName: " + nodename + " X3Family: " + x3Family
				+ " X3Type: " + x3Type + " FOUND in " + fileAdxinstalls + ": OK");
		return false;
	}

}
