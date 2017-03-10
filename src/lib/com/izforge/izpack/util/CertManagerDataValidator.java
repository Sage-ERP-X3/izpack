package com.izforge.izpack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

import sun.security.x509.X500Name;


public class CertManagerDataValidator implements DataValidator
{

	private String strMessage = "";
	public static final String strMessageId = "messageid";
	public static final String strMessageValue = "message.oldvalue"; // not to be stored

	private KeyPair pairCA = null;
	private X509Certificate cacert = null;


	@Override
	public String getErrorMessageId()
	{
		return strMessageId;
	}

	@Override
	public String getWarningMessageId()
	{
		return strMessageId;
	}

	@Override
	public boolean getDefaultAnswer()
	{
		return false;
	}



	@Override
	public Status validateData(AutomatedInstallData adata)
	{
		final Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(AutomatedInstallData.MODIFY_INSTALLATION));
		final Boolean mongoSSL = Boolean.valueOf(adata.getVariable("mongodb.ssl.enable"));

		try
		{

			///////////////////////////////////////////////////////////////////////////////////////////
			// first Syracuse part
			// setup only when not update mode
			if (!modifyinstallation)
			{
				final String localHOST_NAME = adata.getVariable("HOST_NAME").toLowerCase();
				final String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+localHOST_NAME;
				final String certCreate = adata.getVariable("syracuse.certificate.install");

				if (certCreate.equals("cert1"))
					// MB 4-3-2017 Simplified certificate installation
				{
					certCreate (adata);
					adata.getVariable("syracuse.certificate.hostname").toLowerCase();
				}
				else if (certCreate.equals("cert2"))
					// MB 4-3-2017 Use existing certificate
				{

					adata.setVariable("syracuse.certificate.certtool",adata.getVariable("INSTALL_PATH") + File.separator + "syracuse" + File.separator + "certs_tools");
					readCerts (adata);

					final CertificateFactory factory = CertificateFactory.getInstance("X.509");
					final InputStream inPemCertFile = new FileInputStream(adata.getVariable("syracuse.ssl.certfile"));
					final X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);

					final X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName());
					x500Name.getCommonName().toLowerCase();
					adata.setVariable("syracuse.certificate.serverpassphrase",adata.getVariable("syracuse.ssl.pemkeypassword"));
				}
				else if (certCreate.equals("cert3"))
					// MB 4-3-2017 Use existing cert tools folder
				{
					certCreate (adata);
					adata.getVariable("syracuse.certificate.hostname").toLowerCase();
				}
				// set for mongodb install
				adata.setVariable("mongodb.ssl.server.serverpassphrase",adata.getVariable("syracuse.certificate.serverpassphrase"));
				adata.setVariable("mongodb.ssl.server.certfile",strCertPath + File.separator + localHOST_NAME+".crt");
				adata.setVariable("mongodb.ssl.server.pemkeyfile",strCertPath + File.separator + localHOST_NAME+".key");
				adata.setVariable("mongodb.ssl.server.pemcafile",strCertPath + File.separator + "ca.cacrt");
			}

			if (mongoSSL)
			{
				///////////////////////////////////////////////////////////////////////////////////////////
				// Second MongoDB part
				if (!modifyinstallation)
				{
					final Boolean mongodbInstall = Boolean.valueOf(adata.getVariable("mongodb.service.install"));
					final String certCreate = adata.getVariable("syracuse.certificate.install");
					if (certCreate.equals("cert1"))
						// MB 4-3-2017 Simplified certificate installation
					{
						if (mongodbInstall)
						{
							clientCertCreate (adata);
							adata.setVariable("mongodb.service.hostname",adata.getVariable("syracuse.certificate.hostname").toLowerCase());
						}
						else
						{
							clientPutInPlace (adata, adata.getVariable("mongodb.ssl.pemcafile"));
						}
					}
					else if (certCreate.equals("cert2"))
						// MB 4-3-2017 Use existing certificate
					{
						if (mongodbInstall)
						{
							clientPutInPlace (adata, adata.getVariable("syracuse.ssl.pemcafile"));
							final CertificateFactory factory = CertificateFactory.getInstance("X.509");
							final InputStream inPemCertFile = new FileInputStream(adata.getVariable("syracuse.ssl.certfile"));
							final X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);

							final X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName());

							adata.setVariable("mongodb.service.hostname",x500Name.getCommonName().toLowerCase());
						}
						else
						{
							clientPutInPlace (adata, adata.getVariable("mongodb.ssl.pemcafile"));
						}
					}
					else if (certCreate.equals("cert3"))
						// MB 4-3-2017 Use existing cert tools folder
					{
						if (mongodbInstall)
						{
							clientCertCreate (adata);
							adata.setVariable("mongodb.service.hostname",adata.getVariable("syracuse.certificate.hostname").toLowerCase());
						}
						else
						{
							clientPutInPlace (adata, adata.getVariable("mongodb.ssl.pemcafile"));
						}
					}

				}
				else
				{
					clientPutInPlace (adata, adata.getVariable("mongodb.ssl.pemcafile"));
				}
			}


			return Status.OK;

		}
		catch (final Exception ex)
		{
			strMessage = ex.getMessage();
			adata.setVariable(strMessageValue, strMessage);
		}

		return Status.ERROR;
	}

	public void  clientPutInPlace (AutomatedInstallData adata, String strFieldCa) throws Exception
	{
		// client cert is provided with CA
		// only need to copy if not already in place

		//mongodb.ssl.client.certfile
		//mongodb.ssl.client.pemkeyfile
		//mongodb.ssl.pemcafile

		final String fieldPemCertFile = adata.getVariable("mongodb.ssl.client.certfile");
		final String fieldPemKeyFile = adata.getVariable("mongodb.ssl.client.pemkeyfile");
		final String fieldPemCaFile = strFieldCa;


		final String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+"mongodb";
		final File dirCerts =  new File (strCertPath);
		if (!dirCerts.exists()) {
			dirCerts.mkdirs();
		}

		final File certsCaCRT = new File (strCertPath + File.separator + "ca.cacrt");
		final File certsServerCRT = new File (strCertPath + File.separator + "client.crt");
		final File certsServerKey = new File (strCertPath + File.separator + "client.key");
		final File certsServerPem = new File (strCertPath + File.separator + "client.pem");

		// copy CA in output directory
		if (fieldPemCaFile!= null && !"".equals(fieldPemCaFile) && !fieldPemCaFile.equals(strCertPath + File.separator + "ca.cacrt"))
		{
			final File sourceCaCRT = new File (fieldPemCaFile);
			Files.copy(sourceCaCRT.toPath(), certsCaCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		if (!fieldPemCertFile.equals(strCertPath + File.separator + "client.crt"))
		{
			final File sourceServerCRT = new File (fieldPemCertFile);
			Files.copy(sourceServerCRT.toPath(), certsServerCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		if (!fieldPemKeyFile.equals(strCertPath + File.separator + "client.key"))
		{
			final File sourceServerKey = new File (fieldPemKeyFile);
			Files.copy(sourceServerKey.toPath(), certsServerKey.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		KeyPairGeneratorDataValidator.mergeFiles(new File[]{certsServerCRT,certsServerKey}, certsServerPem);

		// set variables for future use
		adata.setVariable("mongodb.ssl.client.certfile",strCertPath + File.separator + "client.crt");
		adata.setVariable("mongodb.ssl.client.pemkeyfile",strCertPath + File.separator + "client.key");
		adata.setVariable("mongodb.ssl.pemcafile",strCertPath + File.separator + "ca.cacrt");    }


	public void  clientCertCreate (AutomatedInstallData adata) throws Exception
	{
		final String countryCode = adata.getVariable("syracuse.certificate.countrycode");
		final String state = adata.getVariable("syracuse.certificate.state");
		final String city = adata.getVariable("syracuse.certificate.city");
		final String organization = adata.getVariable("syracuse.certificate.organization");
		final String organizationalUnit = adata.getVariable("syracuse.certificate.organisationalunit");
		final String name = adata.getVariable("syracuse.certificate.name");
		final String email = adata.getVariable("syracuse.certificate.email");
		final int validity = Integer.parseInt(adata.getVariable("syracuse.certificate.validity"));
		adata.getVariable("HOST_NAME").toLowerCase();

		// need to create client cert
		// then  create a client cert
		final KeyPair pairClient = CreateCertsValidator.generateRSAKeyPair(2048);
		final X509Certificate clientcert = CreateCertsValidator.generateClientV3Certificate(pairClient, countryCode, organization, organizationalUnit,
				state, city, name, email, validity, cacert , pairCA);

		final String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+"mongodb";
		final File dirCerts =  new File (strCertPath);
		if (!dirCerts.exists()) {
			dirCerts.mkdirs();
		}

		final File certsCaCRT = new File (strCertPath + File.separator + "ca.cacrt");
		final File certsServerCRT = new File (strCertPath + File.separator + "client.crt");
		final File certsServerKey = new File (strCertPath + File.separator + "client.key");
		final File certsServerPem = new File (strCertPath + File.separator + "client.pem");

		final FileWriter cacertfile = new FileWriter(certsCaCRT);
		PEMWriter pem = new PEMWriter(cacertfile);
		pem.writeObject(cacert);
		pem.close();

		final FileWriter clientcertfile = new FileWriter(certsServerCRT);
		pem = new PEMWriter(clientcertfile);
		pem.writeObject(clientcert);
		pem.close();

		KeyPairGeneratorDataValidator.writePrivateKey(strCertPath + File.separator + "client.key", pairClient, null);

		KeyPairGeneratorDataValidator.mergeFiles(new File[]{certsServerCRT,certsServerKey}, certsServerPem);

		// set variables for future use
		adata.setVariable("mongodb.ssl.client.certfile",strCertPath + File.separator + "client.crt");
		adata.setVariable("mongodb.ssl.client.pemkeyfile",strCertPath + File.separator + "client.key");
		adata.setVariable("mongodb.ssl.pemcafile",strCertPath + File.separator + "ca.cacrt");

	}

	public void  readCerts (AutomatedInstallData adata) throws Exception
	{
		final String fieldPemCertFile = adata.getVariable("syracuse.ssl.certfile");
		final String fieldPemKeyFile = adata.getVariable("syracuse.ssl.pemkeyfile");
		adata.getVariable("syracuse.ssl.pemkeypassword");
		final String fieldPemCaFile = adata.getVariable("syracuse.ssl.pemcafile");
		final String localHOST_NAME = adata.getVariable("HOST_NAME").toLowerCase();

		// prepare directory tree for syracuse
		final String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+localHOST_NAME;
		final File dirCerts =  new File (strCertPath);
		if (!dirCerts.exists()) {
			dirCerts.mkdirs();
		}
		final String strCertToolPath = adata.getVariable("syracuse.certificate.certtool");
		final File dirCertToolPath =  new File (strCertToolPath);
		if (!dirCertToolPath.exists()) {
			dirCertToolPath.mkdirs();
		}
		final File dirCertToolOutputPath =  new File (strCertToolPath+File.separator+"output");
		if (!dirCertToolOutputPath.exists()) {
			dirCertToolOutputPath.mkdirs();
		}
		final File dirCertToolPrivatePath =  new File (strCertToolPath+File.separator+"private");
		if (!dirCertToolPrivatePath.exists()) {
			dirCertToolPrivatePath.mkdirs();
		}

		final CertificateFactory factory = CertificateFactory.getInstance("X.509");

		// load CA for later use
		final InputStream inPemCaFile = new FileInputStream(fieldPemCaFile);
		cacert = (X509Certificate) factory.generateCertificate(inPemCaFile);

		// copy CA in output directory
		final File sourceCaCRT = new File (fieldPemCaFile);
		final File certToolOutputCaCRT = new File (strCertToolPath+File.separator+"output" + File.separator + "ca.cacrt");
		final File certsCaCRT = new File (strCertPath + File.separator + "ca.cacrt");
		Files.copy(sourceCaCRT.toPath(), certToolOutputCaCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
		Files.copy(sourceCaCRT.toPath(), certsCaCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);


		final InputStream inPemCertFile = new FileInputStream(fieldPemCertFile);
		final X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);

		final X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName());
		x500Name.getCommonName().toLowerCase();

		// copy Cert in output directory
		final File sourceserverCRT = new File (fieldPemCertFile);
		final File certToolOutputServerCRT = new File (strCertToolPath+File.separator+"output" + File.separator + localHOST_NAME +".crt");
		final File certsServerCRT = new File (strCertPath + File.separator + localHOST_NAME + ".crt");
		Files.copy(sourceserverCRT.toPath(), certToolOutputServerCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
		Files.copy(sourceserverCRT.toPath(), certsServerCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);

		// copy key in output directory
		final File sourceserverKey = new File (fieldPemKeyFile);
		final File certToolOutputServerkey = new File (strCertToolPath+File.separator+"output" + File.separator + localHOST_NAME +".key");
		final File certsServerkey = new File (strCertPath + File.separator + localHOST_NAME + ".key");
		Files.copy(sourceserverKey.toPath(), certToolOutputServerkey.toPath(), StandardCopyOption.REPLACE_EXISTING);
		Files.copy(sourceserverKey.toPath(), certsServerkey.toPath(), StandardCopyOption.REPLACE_EXISTING);

		// public key in output directoru
		final PublicKey pubKey = cert.getPublicKey();
		final File certToolOutputServerPub = new File (strCertToolPath+File.separator+"output" + File.separator + localHOST_NAME+".pem");
		final FileWriter serverpubfile = new FileWriter(certToolOutputServerPub);
		final PEMWriter pem = new PEMWriter(serverpubfile);
		pem.writeObject(pubKey);
		pem.close();

		// public key in x3runtime
		final Boolean setx3runtime = Boolean.valueOf(adata.getVariable("syracuse.certificate.setx3runtime"));
		if (setx3runtime)
		{
			final String strX3RuntimePath = adata.getVariable("syracuse.certificate.x3runtime");
			final String pemName = localHOST_NAME.replace('@', '_').replace('$', '_').replace('.', '_');
			final File x3ServerPub = new File (strX3RuntimePath+File.separator+"keys" + File.separator + pemName+".pem");
			Files.copy(certToolOutputServerPub.toPath(), x3ServerPub.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		// public key in x3webserver
		final Boolean setx3webserver = Boolean.valueOf(adata.getVariable("syracuse.certificate.setx3webserver"));
		if (setx3webserver)
		{
			final String strX3WebserverPath = adata.getVariable("syracuse.certificate.x3webserverdata");
			final String pemName = localHOST_NAME.replace('@', '_').replace('$', '_').replace('.', '_');
			final File x3ServerPub = new File (strX3WebserverPath + File.separator + pemName+".pem");
			Files.copy(certToolOutputServerPub.toPath(), x3ServerPub.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}


		// do I really need to load private key ?
		//         InputStream inPemKeyFile = new FileInputStream(fieldPemKeyFile);
		//        // Then check the private key
		//        PEMParser pemParser = new PEMParser(new InputStreamReader(inPemKeyFile));
		//        Object object = pemParser.readObject();
		//        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		//        KeyPair kp;
		//        if (object instanceof PEMEncryptedKeyPair)
		//        {
		//            // Encrypted key - we will use provided password
		//            PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) object;
		//            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(fieldPemKeyPassword.toCharArray());
		//            kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
		//        }
		//        else
		//        {
		//            // Unencrypted key - no password needed
		//            PEMKeyPair ukp = (PEMKeyPair) object;
		//            kp = converter.getKeyPair(ukp);
		//        }

	}


	public void  certCreate (AutomatedInstallData adata) throws Exception
	{
		final String certCreate = adata.getVariable("syracuse.certificate.install");

		final String countryCode = adata.getVariable("syracuse.certificate.countrycode");
		final String state = adata.getVariable("syracuse.certificate.state");
		final String city = adata.getVariable("syracuse.certificate.city");
		final String organization = adata.getVariable("syracuse.certificate.organization");
		final String organizationalUnit = adata.getVariable("syracuse.certificate.organisationalunit");
		final String name = adata.getVariable("syracuse.certificate.name");
		final String email = adata.getVariable("syracuse.certificate.email");
		final int validity = Integer.parseInt(adata.getVariable("syracuse.certificate.validity"));
		final String localHOST_NAME = adata.getVariable("HOST_NAME").toLowerCase();

		// prepare directory tree for syracuse
		final String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+localHOST_NAME;
		final File dirCerts =  new File (strCertPath);
		if (!dirCerts.exists()) {
			dirCerts.mkdirs();
		}
		final String strCertToolPath = adata.getVariable("syracuse.certificate.certtool");
		final File dirCertToolPath =  new File (strCertToolPath);
		if (!dirCertToolPath.exists()) {
			dirCertToolPath.mkdirs();
		}
		final File dirCertToolOutputPath =  new File (strCertToolPath+File.separator+"output");
		if (!dirCertToolOutputPath.exists()) {
			dirCertToolOutputPath.mkdirs();
		}
		final File dirCertToolPrivatePath =  new File (strCertToolPath+File.separator+"private");
		if (!dirCertToolPrivatePath.exists()) {
			dirCertToolPrivatePath.mkdirs();
		}
		if (certCreate.equals("cert1")){
			// first create CA
			pairCA = CreateCertsValidator.generateRSAKeyPair(4096);

			cacert = CreateCertsValidator.generateCAV3Certificate(pairCA, countryCode, organization, organizationalUnit,
					state, city, name, email, validity);
		}
		// copy in certs directory
		final File certsCaCRT = new File (strCertPath + File.separator + "ca.cacrt");
		if (certCreate.equals("cert1")){
			final FileWriter cacertfile = new FileWriter(certsCaCRT);
			final PEMWriter pem1 = new PEMWriter(cacertfile);
			pem1.writeObject(cacert);
			pem1.close();
		}

		// copy in output directory
		final File certToolOutputCaCRT = new File (strCertToolPath+File.separator+"output" + File.separator + "ca.cacrt");
		final String capassphrase = adata.getVariable("syracuse.certificate.capassphrase");
		if (certCreate.equals("cert1")){
			Files.copy(certsCaCRT.toPath(), certToolOutputCaCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// ca private key in private directory
			KeyPairGeneratorDataValidator.writePrivateKey(strCertToolPath+File.separator+"private" + File.separator + "ca.cakey", pairCA, capassphrase.toCharArray());

		}
		// MB 7-3-2017
		if (certCreate.equals("cert3")){
			Files.copy( certToolOutputCaCRT.toPath(),certsCaCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
			final FileReader cacertToolfile = new FileReader(certToolOutputCaCRT);
			PEMParser pem3 = new PEMParser(cacertToolfile);
			Object pemObject = pem3.readObject();
			if (pemObject instanceof X509CertificateHolder) {
				final X509CertificateHolder ckp = (X509CertificateHolder)pemObject;
				final JcaX509CertificateConverter decProv =  new JcaX509CertificateConverter().setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
				cacert = decProv.getCertificate(ckp );
			}
			pem3.close();

			// I really need to load private key ca.ca?
			final File inPemKeyFile = new File(strCertToolPath+File.separator+"private" + File.separator + "ca.cakey");
			//        // Then check the private key
			pem3 = new PEMParser(new FileReader(inPemKeyFile));
			pemObject = pem3.readObject();
			final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			if (pemObject instanceof PEMEncryptedKeyPair){
				//            // Encrypted key - we will use provided password
				final PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) pemObject;
				final PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(capassphrase.toCharArray());
				pairCA = converter.getKeyPair(ckp.decryptKeyPair(decProv));
			}
			else
			{
				//            // Unencrypted key - no password needed
				final PEMKeyPair ukp = (PEMKeyPair) pemObject;
				pairCA = converter.getKeyPair(ukp);
			}

		}
		// then create server cert
		final KeyPair pairServer = CreateCertsValidator.generateRSAKeyPair(2048);
		final String hostname = adata.getVariable("syracuse.certificate.hostname").toLowerCase();

		final X509Certificate servercert = CreateCertsValidator.generateServerV3Certificate(pairServer, countryCode, organization, organizationalUnit,
				state, city, hostname, null, validity, cacert , pairCA);

		// copy in certs directory
		final File certsServerCRT = new File (strCertPath + File.separator + localHOST_NAME+".crt");
		final FileWriter servercertfile = new FileWriter(certsServerCRT);
		PEMWriter pem = new PEMWriter(servercertfile);
		pem.writeObject(servercert);
		pem.close();

		// copy in output directory
		final File certToolOutputServerCRT = new File (strCertToolPath+File.separator+"output" + File.separator + localHOST_NAME+".crt");
		Files.copy(certsServerCRT.toPath(), certToolOutputServerCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);


		// private key in certs directory and output
		final String serverpassphrase = adata.getVariable("syracuse.certificate.serverpassphrase");
		KeyPairGeneratorDataValidator.writePrivateKey(strCertPath + File.separator + localHOST_NAME + ".key", pairServer, serverpassphrase.toCharArray());
		final File certToolOutputServerkey = new File (strCertToolPath+File.separator+"output" + File.separator + localHOST_NAME+".key");
		final File certsServerkey = new File (strCertPath + File.separator + localHOST_NAME + ".key");
		Files.copy(certsServerkey.toPath(), certToolOutputServerkey.toPath(), StandardCopyOption.REPLACE_EXISTING);

		// public key in output
		final File certToolOutputServerPub = new File (strCertToolPath+File.separator+"output" + File.separator + localHOST_NAME+".pem");
		final FileWriter serverpubfile = new FileWriter(certToolOutputServerPub);
		pem = new PEMWriter(serverpubfile);
		pem.writeObject(pairServer.getPublic());
		pem.close();

		// public key in x3runtime
		final Boolean setx3runtime = Boolean.valueOf(adata.getVariable("syracuse.certificate.setx3runtime"));
		if (setx3runtime)
		{
			final String strX3RuntimePath = adata.getVariable("syracuse.certificate.x3runtime");
			final String pemName = localHOST_NAME.replace('@', '_').replace('$', '_').replace('.', '_');
			final File x3ServerPub = new File (strX3RuntimePath+File.separator+"keys" + File.separator + pemName+".pem");
			Files.copy(certToolOutputServerPub.toPath(), x3ServerPub.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		// public key in x3webserver
		final Boolean setx3webserver = Boolean.valueOf(adata.getVariable("syracuse.certificate.setx3webserver"));
		if (setx3webserver)
		{
			final String strX3WebserverPath = adata.getVariable("syracuse.certificate.x3webserverdata");
			final String pemName = localHOST_NAME.replace('@', '_').replace('$', '_').replace('.', '_');
			final File x3ServerPub = new File (strX3WebserverPath + File.separator + pemName+".pem");
			Files.copy(certToolOutputServerPub.toPath(), x3ServerPub.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

	}


}
