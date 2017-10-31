/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.commoninf;

import com.commoninf.TCIException;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Properties;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCIProperties
extends Properties {
    static final long serialVersionUID = 1;
    private static final Logger logger = LoggerFactory.getLogger(TCIProperties.class);
    private static final String ENC_PROP = "encrypted";
    private static final String PWD_PROP = "password";
    private static final String SEP = ":::";
    private static final String ENC_IF_NOT = "encrypt_if_not_encrypted";
    private static String k = "Lm01UDhAnABpoEYo4YEJwg==";

    public TCIProperties() {
    }

    public TCIProperties(Properties defaults) {
        super(defaults);
    }

    @Override
    public String getProperty(String k) {
        String enc;
        String v = super.getProperty(k);
        if (v != null && k.contains("password") && (enc = this.getProperty("encrypted")) != null && enc.equals("true")) {
            v = this.decrypt(v);
        }
        return v;
    }

    public void writeEncrypted(String fn) {
        String enc = this.getProperty("encrypted");
        String enc_if_not = this.getProperty("encrypt_if_not_encrypted");
        if ((enc == null || enc.equals("false")) && (enc_if_not == null || enc_if_not.equals("true"))) {
            Object var7_11;
            ArrayList<String> contents;
            File f = new File(fn);
            if (!f.canWrite()) {
                throw new TCIException(String.valueOf(fn) + " cannot be encrypted because it is not writable");
            }
            contents = new ArrayList<String>();
            try {
                Throwable throwable = null;
                var7_11 = null;
                try {
                    BufferedReader br = new BufferedReader(new FileReader(fn));
                    try {
                        String line;
                        while ((line = br.readLine()) != null) {
                            contents.add(line);
                        }
                    }
                    finally {
                        if (br != null) {
                            br.close();
                        }
                    }
                }
                catch (Throwable throwable2) {
                    if (throwable == null) {
                        throwable = throwable2;
                    } else if (throwable != throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
            }
            catch (Exception e) {
                throw new TCIException(e.getMessage());
            }
            try {
                Throwable e = null;
                var7_11 = null;
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(fn));
                    try {
                        boolean sawEnc = false;
                        for (String line : contents) {
                            if (line.matches("^ *#.*")) {
                                bw.write(line);
                                bw.newLine();
                                continue;
                            }
                            String[] keyval = line.split("=", 2);
                            if (keyval.length == 1) {
                                bw.write(line);
                                bw.newLine();
                                continue;
                            }
                            keyval[0] = keyval[0].trim();
                            keyval[1] = keyval[1].trim();
                            if (keyval[0].equals("encrypted")) {
                                bw.write("encrypted = true");
                                bw.newLine();
                                sawEnc = true;
                                continue;
                            }
                            if (line.contains("password")) {
                                bw.write(String.valueOf(keyval[0]) + " = " + this.encrypt(new StringBuilder(String.valueOf(keyval[0])).append(":::").append(keyval[1]).toString()));
                                bw.newLine();
                                continue;
                            }
                            bw.write(line);
                            bw.newLine();
                        }
                        if (!sawEnc) {
                            bw.write("encrypted = true");
                        }
                    }
                    finally {
                        if (bw != null) {
                            bw.close();
                        }
                    }
                }
                catch (Throwable throwable) {
                    if (e == null) {
                        e = throwable;
                    } else if (e != throwable) {
                        e.addSuppressed(throwable);
                    }
                    throw e;
                }
            }
            catch (Exception e) {
                throw new TCIException(e.getMessage());
            }
        }
    }

    private String encrypt(String val) {
        String hidden = TCIProperties.encrypt(val, k);
        return hidden;
    }

    private String decrypt(String val) {
        String revealed = TCIProperties.decrypt(val, k);
        String[] pcs = revealed.split(":::");
        return pcs[1];
    }

    private static String encrypt(String plainText, String encryptionKeyAsBase64EncodedString) {
        byte[] plainTextAsByteArray = plainText.getBytes();
        byte[] encryptionKeyAsBase64EncodedByteArray = encryptionKeyAsBase64EncodedString.getBytes();
        byte[] encryptionKeyAsRawByteArray = null;
        try {
            encryptionKeyAsRawByteArray = Base64.decode(encryptionKeyAsBase64EncodedByteArray);
        }
        catch (Base64DecodingException e) {
            e.printStackTrace();
        }
        return new String(Base64.encode(TCIProperties.encrypt(plainTextAsByteArray, encryptionKeyAsRawByteArray)));
    }

    private static byte[] encrypt(byte[] plainTextAsByteArray, byte[] encryptionKeyAsRawByteArray) {
        SecretKeySpec key = new SecretKeySpec(encryptionKeyAsRawByteArray, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES", "SunJCE");
            cipher.init(1, key);
            return cipher.doFinal(plainTextAsByteArray);
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            String msg = "Error encrypting";
            logger.error(msg, (Throwable)e);
            return null;
        }
    }

    private static String decrypt(String encryptedTextAsBase64EncodedString, String encryptionKeyAsBase64EncodedString) {
        byte[] encryptedTextAsBase64EncodedByteArray = encryptedTextAsBase64EncodedString.getBytes();
        byte[] encryptedTextAsRawByteArray = null;
        try {
            encryptedTextAsRawByteArray = Base64.decode(encryptedTextAsBase64EncodedByteArray);
        }
        catch (Base64DecodingException e) {
            e.printStackTrace();
        }
        byte[] encryptionKeyAsBase64EncodedByteArray = encryptionKeyAsBase64EncodedString.getBytes();
        byte[] encryptionKeyAsRawByteArray = null;
        try {
            encryptionKeyAsRawByteArray = Base64.decode(encryptionKeyAsBase64EncodedByteArray);
        }
        catch (Base64DecodingException e) {
            e.printStackTrace();
        }
        return new String(TCIProperties.decrypt(encryptedTextAsRawByteArray, encryptionKeyAsRawByteArray));
    }

    private static byte[] decrypt(byte[] encryptedTextAsRawByteArray, byte[] encryptionKeyAsRawByteArray) {
        SecretKeySpec key = new SecretKeySpec(encryptionKeyAsRawByteArray, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES", "SunJCE");
            cipher.init(2, key);
            return cipher.doFinal(encryptedTextAsRawByteArray);
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            String msg = "Error decrypting";
            logger.error(msg, (Throwable)e);
            return null;
        }
    }
}

