package com.sage.izpack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

public class UnixGroupsValidator implements DataValidator, com.izforge.izpack.panels.userinput.validator.Validator {

	private static final Logger logger = Logger.getLogger(UnixGroupsValidator.class.getName());
	private static final String LogLabel = "UnixGroupsValidator - ";
	private ArrayList<String> groups = null;

	@Override
	public boolean validate(ProcessingClient client) {
		boolean result = false;
		String groupName = client.getText();
		result = isValidGroup(groupName);
		logger.log(Level.FINE, LogLabel + "validate groupName: " + groupName + " isValid:" + result);
		return result;
	}

	public boolean isValidGroup(String groupName) {

		if (this.groups == null) {
			initGroups();
		}

		for (String group : this.groups) {
			logger.log(Level.FINE, LogLabel + "Compare entry group '" + groupName + "'=='" + group + "'");

			if (groupName.equals(group))
				return true;
		}
		return false;
	}

	private void initGroups() {
		this.groups = new ArrayList<String>();

		String retValue = "";
		String filepath = "/etc/group";
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(filepath));
			while ((line = reader.readLine()) != null) {
				retValue = line.substring(0, line.indexOf(":"));
				this.groups.add(retValue);
			}
		} catch (Exception ex) {
			retValue = "";
		}
	}

	@Override
	public boolean getDefaultAnswer() {
		return false;
	}

	@Override
	public String getErrorMessageId() {
		return "groupNameNotFound";
	}

	@Override
	public String getWarningMessageId() {
		return "groupNameNotFound";
	}

	@Override
	public Status validateData(InstallData arg0) {
		return DataValidator.Status.OK;
	}

}
