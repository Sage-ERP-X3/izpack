package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.util.OsVersion;

/**
 * @author apozzo
 *
 */
public class NodeIdentifierDataValidator implements DataValidator {

	private static final String SPEC_FILE_NAME = "productsSpec.txt";

	@Override
	public Status validateData(InstallData adata) {
		return validate(adata.getVariable("component.node.name"), adata.getVariable("APP_NAME"));
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
	public boolean getDefaultAnswer() {
		// can we validate in automated mode ?
		// say yes for now
		return true;
	}

	public Status validate(String pstrNodeName, String pstrAppName) {
		Status bReturn = Status.ERROR;
		try {

			String nodeName = pstrNodeName;
			String svcExt = ".service";

			ArrayList<String> uninstallKeyPrefixList = new ArrayList<>();
			uninstallKeyPrefixList.add(pstrAppName);

			// load additionnal prefix from resource

			try {
				InputStream input = new ResourceManager().getInputStream(SPEC_FILE_NAME);

				if (input != null) {

					BufferedReader reader = new BufferedReader(new InputStreamReader(input));
					StringBuilder out = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						uninstallKeyPrefixList.add(line.trim());
					}
					reader.close();
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// check node unicity by service name ?
			// is there a better way ?
			String serviceName = "";

			if (OsVersion.IS_UNIX) {
				// check file /etc/init/xxxxxxx-$SERVICE_NAME.conf
				// SERVICE_NAME = node name in lower
				// first line of xxxxxxx-$SERVICE_NAME.conf contains "# pstrAppName"

				File etcInitDir = new File("/etc/systemd/system");

				if (!(etcInitDir.exists() && etcInitDir.isDirectory())) {
					etcInitDir = new File("/etc/init");
					svcExt = ".conf";
				}

				bReturn = Status.OK;

				// System.out.println("Service path : "+etcInitDir);

				for (File fileEntry : etcInitDir.listFiles()) {
					// System.out.println(fileEntry.getAbsolutePath());
					if (fileEntry.getName().endsWith("-" + pstrNodeName.toLowerCase() + svcExt)
							|| fileEntry.getName().endsWith("_-_" + pstrNodeName.toLowerCase() + svcExt)) {
						BufferedReader reader = new BufferedReader(new FileReader(fileEntry));

						String firstLine = reader.readLine();
						reader.close();

						// System.out.println(firstLine);
						for (String prefix : uninstallKeyPrefixList) {
							if (firstLine.startsWith("# " + prefix))
								return Status.ERROR;
						}

					}
				}

			} else {
				// windows
				bReturn = Status.OK;
				// serviceName = pstrAppName+" - "+nodeName;
				String commandquery = "sc query state= all | findstr /R /C:\"" + nodeName + "\"";
				String[] command = { "CMD", "/C", commandquery };

				ProcessBuilder probuilder = new ProcessBuilder(command);

				Process process = probuilder.start();

				// Read out dir output
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;

				while ((line = br.readLine()) != null) {
					// to find : DISPLAY_NAME: serviceName
					for (String prefix : uninstallKeyPrefixList) {
						if (line.startsWith("DISPLAY_NAME: " + prefix)) {
							bReturn = Status.ERROR;
						}
					}
				}

				// Wait to get exit value
				try {
					int exitValue = process.waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		} catch (Exception ex) {
			// got exception
			ex.printStackTrace();
			bReturn = Status.ERROR;
		}

		return bReturn;

	}

}
