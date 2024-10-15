package com.sage.izpack;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.izforge.izpack.panels.userinput.validator.Validator;

public class HostnameValidator implements Validator {

	@Override
	public boolean validate(ProcessingClient client) {
		String host = "";
		boolean retValue = true;

		try {
			host = client.getFieldContents(0);
		} catch (Exception e) {
			return false;
		}

		try {
			InetAddress.getByName(host);
		} catch (UnknownHostException ex) {
			retValue = false;
		}
		return retValue;
	}

}
