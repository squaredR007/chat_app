package service;
import org.jasypt.util.text.BasicTextEncryptor;

public class EncryptionService {

    private static final String DEFAULT_SECURITY_CODE = "06Zein&Hane07";// fallback password
    private final BasicTextEncryptor encryptor;

    //constructor
    public EncryptionService(){
        String securityCode = System.getenv("CHAT_ENCRYPTION_KEY");
        if (securityCode == null || securityCode.trim().isEmpty()) {
            securityCode = DEFAULT_SECURITY_CODE;
        }
        this.encryptor= new BasicTextEncryptor();
        this.encryptor.setPassword(securityCode); //to enhance file security
    }

    //cryptography
    public String encrypt(String plainText){
        if (plainText==null)
            return null;
        return this.encryptor.encrypt(plainText);
    }

    //decoding
    public String decrypt(String cipherText){
        if (cipherText==null)
            return null;
        return this.encryptor.decrypt(cipherText);
    }
}