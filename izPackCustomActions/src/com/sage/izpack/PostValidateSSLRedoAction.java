package com.sage.izpack;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

public class PostValidateSSLRedoAction implements PanelAction {

	@Override
	public void executeAction(final InstallData adata, AbstractUIHandler handler) {
		String strRedoSSL = adata.getVariable("MONGODB.SSL.REDO");
		boolean update = ModifyInstallationUtil.get(adata);
		String strSSLAlreadyDone = adata.getVariable("mongodb.ssl.alreadydone");

		if (update && "true".equalsIgnoreCase(strRedoSSL)
				&& "true".equalsIgnoreCase(strSSLAlreadyDone)) {
			// we want to redo ssl configuration
			// set mongodb.ssl.alreadydone to false

			adata.setVariable("mongodb.ssl.alreadydone", "false");

		}
	}

	@Override
	public void initialize(PanelActionConfiguration configuration) {
		// nothing to initialize

	}

}
