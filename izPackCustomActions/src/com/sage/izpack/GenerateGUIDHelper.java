package com.sage.izpack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.action.ButtonAction;
import com.izforge.izpack.util.Console;

/**
 * Generate GUID (X3 Services Setup)
 * 
 * @author Franck DEPOORTERE
 */
public class GenerateGUIDHelper extends ButtonAction {

	private static final Logger logger = Logger.getLogger(AdxCompHelper.class.getName());
	private InstallData installData;

	public GenerateGUIDHelper(InstallData installData) {
		super(installData);
		this.installData = installData;
	}

	@Override
	public boolean execute() {
		boolean reachable = false;
		Random random = new Random();
		long msb = random.nextLong();
		long lsb = random.nextLong();
		UUID uuid = new UUID(msb, lsb);
		System.out.println(uuid); // Output: a custom-generated UUID

		// var variable = this.installData.getVariable("syracuse.generateclientid");
		this.installData.setVariable("syracuse.generateclientid", uuid.toString());
		return reachable;
	}

	@Override
	public boolean execute(Console console) {
		if (!execute()) {
			console.println("ERROR");
			return false;
		}
		return true;
	}

	@Override
	public boolean execute(Prompt prompt) {
		if (!execute()) {
			prompt.warn("ERROR");
			return false;
		}
		return true;
	}
}
