package com.izforge.izpack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.util.io.pem.PemHeader;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.ssl.CertificateVerifier;


public class CheckCertificateP12Validator implements com.izforge.izpack.installer.DataValidator
{
    
    private String strMessage = "";
    public static final String strMessageId = "messageid";
    public static final String strMessageValue = "message.oldvalue"; // not to be stored
    
    public Status validateData(AutomatedInstallData adata)
    {
     
        try {
            CheckCertificateP12Validator.writeP12File(adata.getVariable("mongodb.ssl.serverpassphrase"),adata);
            return Status.OK; 
        }  
        catch (Exception ex)
        {
            strMessage = ex.getMessage();
            adata.setVariable(strMessageValue, strMessage);
            return Status.ERROR;
        }
    }

    static void writeP12File(String passphrase, AutomatedInstallData adata) throws IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());        
        
        String strCertPath = adata.getVariable("mongodb.dir.certs");
        String hostname = adata.getVariable("HOST_NAME");
        String pemKeyFile = strCertPath + File.separator + hostname + ".pem";
        String certFile = strCertPath + File.separator + hostname + ".crt";
        String privKeyFile = strCertPath + File.separator + hostname + ".key";
        String p12File = strCertPath + File.separator + hostname + ".p12";
        String serverpassphrase = passphrase;
        

        if(! (new File(p12File)).exists()) {
            InputStream inPemKeyFile = new FileInputStream(privKeyFile);
            InputStream inPemCertFile = new FileInputStream(certFile);

            CertificateFactory factory = CertificateFactory.getInstance("X.509"); 
            X509Certificate servercert = (X509Certificate) factory.generateCertificate(inPemCertFile);

            PEMParser pemParser = new PEMParser(new InputStreamReader(inPemKeyFile));
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            KeyPair pairServer;
            if (object instanceof PEMEncryptedKeyPair)
            {
                // Encrypted key - we will use provided password
                PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) object;
                PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(serverpassphrase.toCharArray());
                pairServer = converter.getKeyPair(ckp.decryptKeyPair(decProv));
            }
            else
            {
                // Unencrypted key - no password needed
                PEMKeyPair ukp = (PEMKeyPair) object;
                pairServer = converter.getKeyPair(ukp);
            }

            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(null, null);
            keyStore.setKeyEntry("trust", pairServer.getPrivate(), null, new Certificate[] { servercert });
            FileOutputStream foStream = new FileOutputStream( strCertPath + File.separator + hostname + ".p12");
            keyStore.store(foStream, serverpassphrase.toCharArray());
            foStream.close();
        }  
        
    }

    public String getErrorMessageId()
    {
        return strMessageId;
    }

    public String getWarningMessageId()
    {
        return strMessageId;
    }

    public boolean getDefaultAnswer()
    {
        // By default do not continue if an error occurs
        return false;
    }

}
