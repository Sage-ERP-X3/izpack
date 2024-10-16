package com.sage.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.crypto.Cipher;

import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;

public class CheckCertificateDataValidator implements DataValidator {

	private String strMessage = "";
	public static final String strMessageId = "messageid";
	public static final String strMessageValue = "message.oldvalue"; // not to be stored

	@Override
	public Status validateData(InstallData adata) {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		String strCertPath = adata.getVariable("mongodb.dir.certs");
		String hostname = adata.getVariable("HOST_NAME");

		// notcreatecert + notupdate
		// at least pemkeyfile and certfile must be provided
		String fieldPemCertFile = adata.getVariable("mongodb.ssl.certfile");
		String fieldPemKeyFile = adata.getVariable("mongodb.ssl.pemkeyfile");
		String fieldPemKeyPassword = adata.getVariable("mongodb.ssl.pemkeypassword");
		String fieldPemCaFile = adata.getVariable("mongodb.ssl.pemcafile");
		boolean useCaFile = false;

		// notcreatecert + update
		// pem has
		// String fieldPemCertFile = adata.getVariable("mongodb.ssl.certfile");
		// String fieldPemKeyFile = adata.getVariable("mongodb.ssl.pemkeyfile");
		// String fieldPemKeyPassword = adata.getVariable("mongodb.ssl.pemkeypassword");
		// String fieldPemCaFile = adata.getVariable("mongodb.ssl.pemcafile");

		// createcert
		// String str

		try {
			InputStream inPemKeyFile = new FileInputStream(fieldPemKeyFile);
			InputStream inPemCertFile = new FileInputStream(fieldPemCertFile);

			// first check the certificate
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);

			// if a CA was provided then we need to check the validity of our certificate
			if (fieldPemCaFile != null && !fieldPemCaFile.trim().equals("")) {
				InputStream inPemCaFile = new FileInputStream(fieldPemCaFile);
				Collection<X509Certificate> certCAChain = (Collection<X509Certificate>) factory
						.generateCertificates(inPemCaFile);

				// cert should be part of the path to be validated
				certCAChain.add(cert);

				PKIXCertPathBuilderResult verifiedCertChain = CertificateVerifier.verifyCertificate(cert,
						new HashSet<X509Certificate>(certCAChain));

				adata.setVariable("mongodb.ssl.usecafile", "true");
				useCaFile = true;
			}

			// Then check the private key
			PEMParser pemParser = new PEMParser(new InputStreamReader(inPemKeyFile));
			Object object = pemParser.readObject();
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			KeyPair kp;
			if (object instanceof PEMEncryptedKeyPair) {
				// Encrypted key - we will use provided password
				PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) object;
				PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
						.build(fieldPemKeyPassword.toCharArray());
				kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
			} else {
				// Unencrypted key - no password needed
				PEMKeyPair ukp = (PEMKeyPair) object;
				kp = converter.getKeyPair(ukp);
			}

			byte[] input = "1234567890ABCDEF".getBytes();

			// System.out.println("input: " + new String(input));
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
			byte[] cipherText = cipher.doFinal(input);

			// System.out.println("cipher: " + new String(cipherText));

			cipher.init(Cipher.DECRYPT_MODE, kp.getPrivate());
			byte[] decrypted = cipher.doFinal(cipherText);

			// System.out.println("plain : " + new String(decrypted));

			if (Arrays.equals(decrypted, input)) {
				File certpath = new File(strCertPath);
				if (!certpath.exists())
					certpath.mkdirs();

				// we need to copy things
				File pemKeyFile = new File(strCertPath + File.separator + hostname + ".pem");
				File certFile = new File(fieldPemCertFile);
				File privKeyFile = new File(fieldPemKeyFile);

				File destCertFile = new File(strCertPath + File.separator + hostname + ".crt");
				Files.copy(certFile.toPath(), destCertFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				File destKeyFile = new File(strCertPath + File.separator + hostname + ".key");
				Files.copy(privKeyFile.toPath(), destKeyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

				KeyPairGeneratorDataValidator.mergeFiles(new File[] { certFile, privKeyFile }, pemKeyFile);

				if (useCaFile) {
					File caPath = new File(fieldPemCaFile);
					File certCaPath = new File(strCertPath + File.separator + "ca.cacrt");
					Files.copy(caPath.toPath(), certCaPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}

				CheckCertificateP12Validator.writeP12File(fieldPemKeyPassword, adata);
				// we need to says that this step was done at least one time
				adata.setVariable("mongodb.ssl.alreadydone", "true");

				return Status.OK;
			} else {
				strMessage = "Unknow error";
				adata.setVariable(strMessageValue, strMessage);
				return Status.ERROR;
			}

			// RSA
			// KeyFactory keyFac = KeyFactory.getInstance("RSA");
			// RSAPrivateCrtKeySpec privateKey = keyFac.getKeySpec(kp.getPrivate(),
			// RSAPrivateCrtKeySpec.class);
			// RSAPublicKeySpec publicKey = keyFac.getKeySpec(cert.getPublicKey(),
			// RSAPublicKeySpec.class);

		} catch (Exception ex) {
			strMessage = ex.getMessage();
			adata.setVariable(strMessageValue, strMessage);
			return Status.ERROR;
		}

		// strMessage = "OS error #" + error + " - " ;
		// adata.setVariable(strMessageValue, strMessage);
		// return Status.WARNING;
	}

	@Override
	public String getErrorMessageId() {
		return strMessageId;
	}

	@Override
	public String getWarningMessageId() {
		return strMessageId;
	}

	@Override
	public boolean getDefaultAnswer() {
		// By default do not continue if an error occurs
		return false;
	}

}
