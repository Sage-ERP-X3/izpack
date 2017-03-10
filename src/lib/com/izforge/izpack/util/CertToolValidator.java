/**
 *
 */
package com.izforge.izpack.util;

import java.io.File;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;


/**
 * @author mboutafa
 *
 */
public class CertToolValidator implements Validator
{

	/* (non-Javadoc)
	 * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.izpack.installer.AutomatedInstallData)
	 */
	@Override
	public boolean validate(ProcessingClient client,AutomatedInstallData adata)
	{
		boolean bReturn = false;
		try
		{

			//String x3runPath = adata.getVariable("syracuse.certificate.x3runtime").trim();
			final String CertToolPath = client.getFieldContents(0).trim();

			final File CertToolFolder = new File (CertToolPath);
			if (CertToolFolder.exists() && CertToolFolder.isDirectory())
			{
				final File CertToolFolderOutput = new File (CertToolPath+"/Output/ca.cacrt");
				final File CertToolFolderPrivate = new File (CertToolPath+"/Private/ca.cakey");
				if (CertToolFolderOutput.exists() && CertToolFolderPrivate.exists()) {
					bReturn = true;
				}
			}

		}
		catch (final Exception ex)
		{
			// got exception
			Debug.trace(ex.getMessage());
			bReturn = false;
		}

		return bReturn;
	}
}
