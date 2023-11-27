package com.sage.izpack;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.izforge.izpack.util.unix.UnixUsers;

public class UnixUsersValidator implements DataValidator, com.izforge.izpack.panels.userinput.validator.Validator {

	private static final Logger logger = Logger.getLogger(UnixUsersValidator.class.getName());
	private static final String LogLabel = "UnixUsersValidator - ";

	@Override
	public boolean validate(ProcessingClient client) {
		boolean result = false;
		String userName = client.getText();
		result = isValid(userName);
		logger.log(Level.FINE, LogLabel + "validate userName: " + userName + " isValid:" + result);
		return result;
	}

	private boolean isValid(String userName) {
		ArrayList<String> users = UnixUsers.getEtcPasswdUsersAsArrayList();
		for (String userline : users) {
            String userLineName = (userline.substring(0, userline.indexOf(":")));
			logger.log(Level.FINE, LogLabel + "Compare entry user '" + userName + "'=='" + userLineName + "'");
			if (userName.equals(userLineName))
				return true;
		}
		return false;
	}

	@Override
	public boolean getDefaultAnswer() {
		return false;
	}

	@Override
	public String getErrorMessageId() {
		return "userNameNotFound";
	}

	@Override
	public String getWarningMessageId() {
		return "userNameNotFound";
	}

	@Override
	public Status validateData(InstallData arg0) {
		return DataValidator.Status.OK;
	}

}
