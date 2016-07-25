package com.treexor.auth.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class EncryptionMD5 {
    private static final Logger log = LoggerFactory.getLogger(EncryptionMD5.class);

    private static MessageDigest md;

    public static String encrypt(String pass, long salt) {
        try {
            if (!Strings.isNullOrEmpty(pass) && salt > 0) {
                md = MessageDigest.getInstance("MD5");
                byte[] passBytes = (pass + salt).getBytes();
                md.reset();

                byte[] digested = md.digest(passBytes);
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < digested.length; i++) {
                    sb.append(Integer.toHexString(0xff & digested[i]));
                }
                return sb.toString();
            }
        } catch (NoSuchAlgorithmException ex) {
            log.error(ex.getMessage());
        }
        return null;
    }
}
