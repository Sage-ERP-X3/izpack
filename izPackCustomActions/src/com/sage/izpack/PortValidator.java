package com.sage.izpack;

import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

public class PortValidator implements DataValidator, com.izforge.izpack.panels.userinput.validator.Validator {

	private static final Logger logger = Logger.getLogger(X3NodeIdentifierValidator.class.getName());

	@Override
	public boolean validate(ProcessingClient arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getDefaultAnswer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getErrorMessageId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWarningMessageId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status validateData(InstallData arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
