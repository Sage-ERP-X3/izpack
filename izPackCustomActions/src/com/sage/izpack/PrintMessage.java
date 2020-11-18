package com.sage.izpack;

import java.util.ResourceBundle;

import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.panels.process.AbstractUIProcessHandler;
import com.izforge.izpack.util.Debug;

public class PrintMessage {

	// public static boolean ConditionBoolean;

	public void run(AbstractUIProcessHandler handler, String[] args) {
		// ConditionBoolean = false;

		String message = "-";
		try {

			if (args != null && args.length > 0) {

				for (int i = 0; i < args.length; i++) {
					String messageId = args[i];
					message = messageId;

					// TODO: FRDEPO => search in XML files
					// ResourceManager resources = new ResourceManager();
					// resources.setResourceBasePath("/com/izforge/izpack/");
					// resources.setLocales(locales);
					// String message = resources.getString(messageId);

					message = ResourceBundle.getBundle("messages").getString(messageId);
					// System.out.println(message);
				}
			} else if (Debug.isTRACE()) {

				handler.logOutput("No message in parameter (com.sage.izpack.EchoMessage(mesg0, mesg1..))", true);
				// System.out.println("No message (EchoMessage)");

			}

			// ConditionBoolean = true;
		} catch (Throwable e) {
			if (Debug.isTRACE()) {
				handler.logOutput(e.getMessage(), false);
				System.err.println(e.getMessage());
			}
		}

		handler.logOutput(message, true);
	}

}
