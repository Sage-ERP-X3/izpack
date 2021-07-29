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
import org.w3c.dom.Element;
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

	public X3NodeIdentifierValidator(RegistryDefaultHandler handler) {
		super();
		this.registryHandler = handler.getInstance();
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
		logger.log(Level.FINE, "X3NodeIdentifierValidator.validate  moduleName: " + moduleName);
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

		return DataValidator.Status.OK; // checkData();
	}

	private boolean checkData(String nodeName, String x3Family, String x3Type)
			throws Exception, ParserConfigurationException, SAXException, IOException {

		logger.log(Level.FINE, "X3NodeIdentifierValidator.validateData");

		boolean validateResult = false; // DataValidator.Status.ERROR; // super.validateData(idata);

		RegistryHandlerX3 rh = new RegistryHandlerX3(this.registryHandler);
		if (this.registryHandler != null && rh != null) {

			boolean adxAdminRegistered;
			try {
				adxAdminRegistered = rh.adxadminProductRegistered();

				if (adxAdminRegistered) {
					// validateResult = true; // DataValidator.Status.OK;
					logger.log(Level.FINE,
							"X3NodeIdentifierValidator  check adxadminProductRegistered: " + adxAdminRegistered);
				} else {
					logger.log(Level.SEVERE,
							"X3NodeIdentifierValidator : X3 AdxAdmin not installed or not found in registry.");
				}

				validateResult = CheckXmlInstallNode(rh, nodeName, x3Family, x3Type);

			} catch (NativeLibException e) {
				e.printStackTrace();
			}
		}
		logger.log(Level.FINE, "X3NodeIdentifierValidator.validateData - validateData: " + validateResult);
		return validateResult;
	}

	
	
	private boolean CheckXmlInstallNode(RegistryHandlerX3 rh, String nodename, String strX3Family, String strX3Type)
			throws NativeLibException, ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {

		String dir = rh.getAdxAdminDirPath();
		logger.log(Level.FINE, "X3NodeIdentifierValidator  CheckXmlInstallNode: Check directory " + dir + " nodename: "
				+ nodename + " strX3Family: " + strX3Family + " strX3Type: " + strX3Type);

		java.io.File dirAdxDir = new java.io.File(dir);

		if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) {
			throw new NativeLibException(dirAdxDir + " is not found or not a directory");
		}

		StringBuilder fullpathBuilder = new StringBuilder();
		fullpathBuilder.append(dirAdxDir.getAbsolutePath());
		fullpathBuilder.append(File.separator);
		fullpathBuilder.append("inst");
		java.io.File fileAdxinstalls = new java.io.File(fullpathBuilder.toString());
		if (!fileAdxinstalls.exists() || !fileAdxinstalls.isDirectory()) {
			throw new NativeLibException(fileAdxinstalls + " is not found or not a directory");
		}
		fullpathBuilder.append(File.separator);
		fullpathBuilder.append(AdxCompInstallerListener.ADX_INSTALL_FILENAME);

		fileAdxinstalls = new java.io.File(fullpathBuilder.toString());
		if (!fileAdxinstalls.exists()) {
			logger.log(Level.FINE,
					"X3NodeIdentifierValidator  CheckXmlInstallNode: " + fileAdxinstalls + " doesn't exist yet. OK");
			return true;
		}

		logger.log(Level.FINE, "X3NodeIdentifierValidator.CheckXmlInstallNode   Opening " + fileAdxinstalls
				+ "   nodename: " + nodename + " X3Family: " + strX3Family + " X3Type: " + strX3Type);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document xdoc = dBuilder.parse(fileAdxinstalls);
		// Element xmlinstall = xdoc.getDocumentElement();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String strPath = "/install/module[@name='" + nodename + "' and @family='" + strX3Family + "'";
		if (strX3Type != null && !"".equals(strX3Type))
			strPath += " and @type='" + strX3Type + "'";
		strPath += "]";

		Node module = (Node) xPath.compile(strPath).evaluate(xdoc, XPathConstants.NODE);

		if (module == null) {
			logger.log(Level.FINE,
					"X3NodeIdentifierValidator.CheckXmlInstallNode  nodename: " + nodename + " X3Family: " + strX3Family
							+ " X3Type: " + strX3Type + " NOT found in " + fileAdxinstalls + ": OK");
			return true;
		}

		logger.log(Level.FINE, "X3NodeIdentifierValidator.CheckXmlInstallNode  nodename: " + nodename + " X3Family: "
				+ strX3Family + " X3Type: " + strX3Type + " FOUND in " + fileAdxinstalls + ": OK");
		return false;
	}

}
