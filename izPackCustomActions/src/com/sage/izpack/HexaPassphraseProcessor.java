package com.sage.izpack;

import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;


public class HexaPassphraseProcessor implements Processor {


	@Override
	public String process(ProcessingClient client) {
		String returnValue = client.getFieldContents(0);
		return StringUtil.asciiToHex(returnValue);
	}


}
