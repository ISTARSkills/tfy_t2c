package com.istarindia.apps.services;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AppEncryptionService {

	private static final String ALGORITHM = "AES";
    private static final byte[] PRIVATE_KEY = "OXY IS IN ISTAR ".getBytes();
    
    
    public String encrypt(String valueToEnc){      
        try{
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encValue = cipher.doFinal(valueToEnc.getBytes());
        byte[] encryptedBytes = Base64.getEncoder().encode(encValue);
        String encryptedValue = new String(encryptedBytes);
        return encryptedValue;
        }catch(Exception e){
        	e.printStackTrace();
        	return null;
        }
    }
    
    public String decrypt(String encryptedValue){
    	try{
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decodedBytes = Base64.getDecoder().decode(encryptedValue.getBytes());
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        String decryptedValue = new String(decryptedBytes);
        return decryptedValue;
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }

    
    private Key generateKey() throws Exception {
        Key key = new SecretKeySpec(PRIVATE_KEY, ALGORITHM);
        return key;
    }
    
}
