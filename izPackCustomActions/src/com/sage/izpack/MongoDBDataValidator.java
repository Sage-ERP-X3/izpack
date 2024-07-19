package com.sage.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bson.Document;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoIterable;

public class MongoDBDataValidator implements DataValidator {

	private static Logger logger = Logger.getLogger(MongoDBDataValidator.class.getName());

	/*
	 * Syracuse supports MongoDb 3, 4 and 7 from Sage X3 2024 R2
	 * 
	 * @param version: ex = "7.0.11"
	 */
	private static boolean isSupportedVersion(String version) {

		Integer majorVersion = 0;
		try {
			String[] verStr1 = version.split("\\.");
			if (verStr1[0] == null || verStr1[0].trim().length() == 0) {
				verStr1[0] = "0";
			}
			majorVersion = Integer.valueOf(verStr1[0]);
		} catch (NumberFormatException e) {
			majorVersion = 0;
			
		}
		return majorVersion >= 3;
		// return version.startsWith("3.") || version.startsWith("4.") || version.startsWith("7.");
	}

	@Override
	public Status validateData(InstallData adata) {
		Status bReturn = Status.OK;
		try {

			Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(InstallData.MODIFY_INSTALLATION));

			// String userName = adata.getVariable("mongodb.url.username");
			// String passWord = adata.getVariable("mongodb.url.password");

			String hostName = new String(adata.getVariable("mongodb.service.hostname"));
			String hostPort = adata.getVariable("mongodb.service.port");
			boolean sslEnabled = "true".equalsIgnoreCase(adata.getVariable("mongodb.ssl.enable"));
			String certFile = adata.getVariable("mongodb.ssl.client.certfile");
			String pemkeyFile = adata.getVariable("mongodb.ssl.client.pemkeyfile");
			String pemcaFile = adata.getVariable("mongodb.ssl.pemcafile");

			if (!sslEnabled) {

				MongoClient mongoClient = new MongoClient(hostName, Integer.parseInt(hostPort));
				String version = mongoClient.getDatabase("test").runCommand(new Document("buildInfo", 1)).getString("version");

				if (!isSupportedVersion(version)) {
					bReturn = Status.ERROR;
				} else {
					// test if syracuse db already exists
					MongoIterable<String> lstDb = mongoClient.listDatabaseNames();

					for (String dbb : lstDb) {
						if (dbb.equals("syracuse")) {
							if (modifyinstallation)
								bReturn = Status.OK;
							else
								bReturn = Status.WARNING;
							break;
						}
					}
				}

				mongoClient.close();
			} else {

				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

				CertificateFactory factory = CertificateFactory.getInstance("X.509");
				ArrayList<X509Certificate> chainArray = new ArrayList<X509Certificate>();

				if (pemcaFile != null && !"".equals(pemcaFile)) {
					// create truststore
					KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
					trustStore.load(null, null);
					InputStream inPemCaFile = new FileInputStream(pemcaFile);
					X509Certificate cacert = (X509Certificate) factory.generateCertificate(inPemCaFile);
					trustStore.setCertificateEntry("root", cacert);
					File trustStoreFile = File.createTempFile("tru", null);
					FileOutputStream trustStoreFileOutputStream = new FileOutputStream(trustStoreFile);
					trustStore.store(trustStoreFileOutputStream, "truststore".toCharArray());
					trustStoreFileOutputStream.close();

					chainArray.add(cacert);

					System.setProperty("javax.net.ssl.trustStore", trustStoreFile.getAbsolutePath());
					System.setProperty("javax.net.ssl.trustStorePassword", "truststore");

				}

				// create keystore
				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load(null, null);

				InputStream inPemCertFile = new FileInputStream(certFile);
				X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);

				InputStream inPemKeyFile = new FileInputStream(pemkeyFile);
				PEMParser pemParser = new PEMParser(new InputStreamReader(inPemKeyFile));
				Object object = pemParser.readObject();
				JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
				PEMKeyPair ukp = (PEMKeyPair) object;
				KeyPair kp = converter.getKeyPair(ukp);

				keyStore.setCertificateEntry(cert.getSubjectX500Principal().toString(), cert);

				chainArray.add(cert);

				X509Certificate[] chain = new X509Certificate[chainArray.size()];
				chain[0] = cert;
				if (chainArray.size() > 1)
					chain[1] = (X509Certificate) chainArray.get(0);

				keyStore.setKeyEntry("importkey", kp.getPrivate(), "keystore".toCharArray(), chain);

				File keyStoreFile = File.createTempFile("key", null);
				FileOutputStream keyStoreFileOutputStream = new FileOutputStream(keyStoreFile);
				keyStore.store(keyStoreFileOutputStream, "keystore".toCharArray());
				keyStoreFileOutputStream.close();

				System.setProperty("javax.net.ssl.keyStore", keyStoreFile.getAbsolutePath());
				System.setProperty("javax.net.ssl.keyStorePassword", "keystore");
				// System.setProperty("javax.net.debug", "ssl");

				System.setProperty("jdk.tls.trustNameService", "true");

				MongoClientOptions.Builder opts = MongoClientOptions.builder();
				opts.sslEnabled(true);
				opts.serverSelectionTimeout(60000);

				// final MongoClient mongoClient = new MongoClient( new ServerAddress(hostName,
				// Integer.parseInt(hostPort)) , new
				// MongoClientOptions.Builder().sslEnabled(true).build());
				MongoClient mongoClient = new MongoClient(new ServerAddress(hostName, Integer.parseInt(hostPort)),
						opts.build());

				// MongoClientURI cliUri = new MongoClientURI
				// ("mongodb://"+hostName+":"+hostPort+"/syracuse?ssl=true", new
				// MongoClientOptions.Builder().sslEnabled(true).build());
				// MongoClient mongoClient = new MongoClient(cliUri);
				// String version =
				// mongoClient.getDB("test").command("buildInfo").getString("version");

				String version = mongoClient.getDatabase("test").runCommand(new Document("buildInfo", 1)).getString("version");

				if (!isSupportedVersion(version)) {
					bReturn = Status.ERROR;
				} else {
					// test if syracuse db already exists
					MongoIterable<String> lstDb = mongoClient.listDatabaseNames();

					for (String dbb : lstDb) {
						if (dbb.equals("syracuse")) {
							if (modifyinstallation)
								bReturn = Status.OK;
							else
								bReturn = Status.WARNING;
							break;
						}
					}
				}

				mongoClient.close();
			}

		} catch (Exception ex) {
			logger.log(Level.FINE, ex.getMessage());
			bReturn = Status.ERROR;
		}
		return bReturn;
	}

	@Override
	public String getErrorMessageId() {
		return "mongodbtesterror";
	}

	@Override
	public String getWarningMessageId() {
		return "mongodbtestwarn";
	}

	@Override
	public boolean getDefaultAnswer() {
		// warning means that DB is already configured, can be ignored
		return Boolean.TRUE;
	}

}
