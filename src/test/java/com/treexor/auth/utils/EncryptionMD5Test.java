package com.treexor.auth.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class EncryptionMD5Test {

    @Test
    public void given_The_Same_Pasword_And_Salt_Then_Return_The_Same_Result() {
        String pass = "FSAFJSAOFJSOFFMSAOFMSFOSAFI3J42349UI23904U398FUJJDSFIS;;;;;PAFIDSJAF%&f";
        long salt = 354023942094L;

        String encryptedPass = EncryptionMD5.encrypt(pass, salt);
        String encryptedPass2 = EncryptionMD5.encrypt(pass, salt);
        String encryptedPass3 = EncryptionMD5.encrypt(pass, salt);

        assertEquals("Failure - we were expecting the encrypted passwords to the same", encryptedPass, encryptedPass2);
        assertEquals("Failure - we were expecting the encrypted passwords to the same", encryptedPass, encryptedPass3);
    }

    @Test
    public void given_Differnt_Paswords_And_Salt_Combinations_Then_Return_Different_Results() {
        String pass = "FSAFJSAOFJSOFFMSAOFMSFOSAFI3J42349UI23904U398FUJJDSFIS;;;;;PAFIDSJAF%&f";
        long salt = 354023942094L;

        String pass2 = "FSAFJSAOFJSOFFMS;;;PAFIDSJAF%&f";
        long salt2 = 354023094L;

        String encryptedPass = EncryptionMD5.encrypt(pass, salt);
        String encryptedPass2 = EncryptionMD5.encrypt(pass, salt2);
        String encryptedPass3 = EncryptionMD5.encrypt(pass2, salt);
        String encryptedPass4 = EncryptionMD5.encrypt(pass2, salt2);

        assertNotEquals("Failure - we were expecting the encrypted passwords to the different", encryptedPass, encryptedPass2);
        assertNotEquals("Failure - we were expecting the encrypted passwords to the different", encryptedPass, encryptedPass3);
        assertNotEquals("Failure - we were expecting the encrypted passwords to the different", encryptedPass, encryptedPass4);
        assertNotEquals("Failure - we were expecting the encrypted passwords to the different", encryptedPass2, encryptedPass3);
        assertNotEquals("Failure - we were expecting the encrypted passwords to the different", encryptedPass2, encryptedPass4);
        assertNotEquals("Failure - we were expecting the encrypted passwords to the different", encryptedPass3, encryptedPass4);
    }
}
