package com.example.android.roomwordssample;


import android.util.Base64;
import android.util.Log;

import androidx.test.runner.AndroidJUnit4;

import com.facebook.android.crypto.keychain.SecureRandomFix;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.SecureRandom;
@RunWith(AndroidJUnit4.class)
public class Testing {
    @Test
    public void testSatu() {
        SecureRandom secureRandomFix = SecureRandomFix.createLocalSecureRandom();

        byte[] key = new byte[32];
        secureRandomFix.nextBytes(key);
        Log.d("TAG", "key: " + Base64.encodeToString(key, Base64.DEFAULT));

        byte[] ivKey = new byte[12];
        secureRandomFix.nextBytes(ivKey);
        Log.d("TAG", "ivKey: " + Base64.encodeToString(ivKey, Base64.DEFAULT));

        byte[] macKey = new byte[64];
        secureRandomFix.nextBytes(macKey);
        Log.d("TAG", "macKey: " + Base64.encodeToString(macKey, Base64.DEFAULT));

//        for (int i = 0; i < 3; i++) {
//            byte[] key = new byte[256];
//            secureRandomFix.nextBytes(key);
//
//            Log.d("TAG", "key: " + Base64.encodeToString(key, Base64.DEFAULT));
//        }

//        StringBuilder sb = new StringBuilder();
//        for (byte b : key) {
//            sb.append(String.format("%02x",b));
//        }
//        Log.d("TAG", "testSatu: string: "+sb.toString());
    }
}
