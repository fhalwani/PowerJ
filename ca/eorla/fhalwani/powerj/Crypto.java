package ca.eorla.fhalwani.powerj;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

class Crypto {
    // Iteration count
    private final static int count = 1000;
    private final static String strTab = "\t";
	 // Salt
    private final static byte[] salt = {
        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99 };
	private boolean success = true;
    private String fileName = "";
    private PBEKeySpec pbeKeySpec = null;
    private PBEParameterSpec pbeParamSpec = null;
    private SecretKeyFactory keyFac = null;
    private SecretKey pbeKey = null;
    private Cipher pbeCipher = null;
    
    Crypto(String path) {
		fileName = path + "bin" + Constants.FILE_SEPARATOR + "powerj.bin";
    }
    
    String[] getFile() {
    	init();
		String[] data = null;
        byte[] ciphertext = read();
        if (ciphertext != null) {
            byte[] cleartext = decrypt(ciphertext);
            String strText = new String(cleartext);
    		data = strText.split(strTab);
        }
		return data;
    }
    
    boolean setFile(String[] data) {
		String text = data[0] + strTab + data[1] + strTab +
				data[2] + strTab + data[3] + strTab +
				data[4] + strTab + data[5];
    	init();
	    byte[] cleartext = text.getBytes();
        byte[] ciphertext = encrypt(cleartext);
        save(ciphertext);
		return success;
    }
    
    private byte[] encrypt(byte[] cleartext) {
        // Initialize PBE Cipher with key and parameters
    	byte[] ciphertext = null;
        try {
			pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
		    // Encrypt the cleartext
		    ciphertext = pbeCipher.doFinal(cleartext);
		} catch (InvalidKeyException e) {
			success = false;
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			success = false;
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			success = false;
			e.printStackTrace();
		} catch (BadPaddingException e) {
			success = false;
			e.printStackTrace();
		}
        return ciphertext;
    }

    private byte[] decrypt(byte[] ciphertext) {
    	byte[] cleartext = null;
        try {
		    // Initialize the same cipher for decryption
		    pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
		    // Decrypt the ciphertext
		    cleartext = pbeCipher.doFinal(ciphertext);
		} catch (InvalidKeyException e) {
			success = false;
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			success = false;
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			success = false;
			e.printStackTrace();
		} catch (BadPaddingException e) {
			success = false;
			e.printStackTrace();
		}
        return cleartext;
    }


    private void init() {
    	final String key = "PBEWithMD5AndDES";
		try {
            // Create PBE parameter set
            pbeParamSpec = new PBEParameterSpec(salt, count);
            keyFac = SecretKeyFactory.getInstance(key);
            // Create PBE Cipher
            pbeCipher = Cipher.getInstance(key);
            char[] chr = key.toCharArray();		
            pbeKeySpec = new PBEKeySpec(chr);
            pbeKey = keyFac.generateSecret(pbeKeySpec);
		} catch (NoSuchAlgorithmException e) {
			success = false;
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			success = false;
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			success = false;
			e.printStackTrace();
		}
    }
    
	private byte[] read() {
		File file = null;
		FileInputStream inputStream = null;
		byte[] ciphertext = null;
		try {
			file = new File(fileName);
			if (file.exists()) {
				inputStream = new FileInputStream(fileName);
				ciphertext = new byte[inputStream.available()];
				inputStream.read(ciphertext);
				inputStream.close();
			}
		} catch (FileNotFoundException e) {
			success = false;
			e.printStackTrace();
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}
		return ciphertext;
	}
	
	private void save(byte[] ciphertext) {
		File file = null;
		FileOutputStream fos = null;
		try {
			file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				fos = new FileOutputStream(file);
				fos.write(ciphertext);
			}
			fos.close();
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}
	}
}
