/**
 * 
 */
package com.sage.izpack;

import java.io.File;

import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.izforge.izpack.panels.userinput.validator.Validator;

public class X3RuntimeValidator implements Validator {

	@Override
	public boolean validate(ProcessingClient client) {
		boolean bReturn = false;
		try {

			// String x3runPath =
			// adata.getVariable("syracuse.certificate.x3runtime").trim();
			String x3runPath = client.getFieldContents(0).trim();

			File X3runKeys = new File(x3runPath + "/keys");
			if (X3runKeys.exists() && X3runKeys.isDirectory()) {
				bReturn = true;
			}

		} catch (Exception ex) {
			// got exception
			ex.printStackTrace();
			bReturn = false;
		}

		return bReturn;
	}

}
