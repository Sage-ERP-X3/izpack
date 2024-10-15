package com.sage.izpack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 *
 */
public class CReportWritter {

	private final String pFileNameSuffix;
	private final CReport pReport;
	private final String pUserDir;

	/**
	 * @param aReport
	 */
	public CReportWritter(final CReport aReport) {
		this(aReport, CReport.SUFFIX_EMPTY);
	}

	/**
	 * @param aReport
	 * @param aSuffix
	 */
	public CReportWritter(final CReport aReport, final String aFileNameSuffix) {
		super();
		pReport = aReport;
		pFileNameSuffix = aFileNameSuffix;
		pUserDir = System.getProperty("user.dir");
	}

	/**
	 * @return
	 */
	public String getFileNameSuffix() {
		return pFileNameSuffix;
	}

	/**
	 * @return
	 */
	public File getOutputFile() {
		return new File(pUserDir, pReport.getFileName(getFileNameSuffix()));
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public File write() throws IOException {

		return write(getOutputFile());
	}

	/**
	 * @param aOutputFile
	 * @return
	 * @throws IOException
	 */
	public File write(final File aOutputFile) throws IOException {
		if (aOutputFile == null) {
			throw new IllegalArgumentException("the given OutputFile must be not null");
		}

		// create the needed hierarchy
		if (!aOutputFile.getParentFile().exists()) {
			Files.createDirectories(aOutputFile.getParentFile().toPath());
		}

		String wPaylod = pReport.toString();

		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aOutputFile), "UTF-8"));
		try {
			out.write(wPaylod);
		} finally {
			out.close();
		}

		return aOutputFile;
	}

}
