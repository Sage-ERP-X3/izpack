/**
 * 
 */
package com.sage.izpack;

import com.izforge.izpack.api.installer.DataValidator.Status;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.izforge.izpack.panels.userinput.validator.Validator;

public class NodeIdentifierValidator implements Validator {
	private static final String APP_NAME_PARAM = "APP_NAME";
	private NodeIdentifierDataValidator validator = new NodeIdentifierDataValidator();

	@Override
	public boolean validate(ProcessingClient client) {
		// find app_name
		String appname = client.getConfigurationOptionValue(APP_NAME_PARAM, null);

		// then validate
		String nodename = client.getText();

		return (validator.validate(nodename, appname) == Status.OK);
	}

}
