package com.example.android.roomwordssample;

import android.content.Context;
import android.util.Base64;

import com.facebook.android.crypto.keychain.SecureRandomFix;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;


public class CustomKeyChain implements KeyChain {
    private static final String CK = "XyPNfPrigjhwmbnudOXtQafk3Tlb9bSGbScfLRUj57s=";
    private static final String MC = "4XlXuXpKppwrkMz6";
    private static final String IV = "92duY9oGra9vjxjmURiqs2uMzVBLYpKMfhSY/BCc2Lb5Sp9TlrDk+/Z/8zy6Lu+lk8Z57XbgMIsU3qlDSn0lLA==";
//    private final CryptoConfig mCryptoConfig;

//    /** @deprecated */
//    @Deprecated
//    public CustomKeyChain(Context context) {
//        this(context, CryptoConfig.KEY_128);
//    }
//
//    public CustomKeyChain(Context context, CryptoConfig config) {
////        String prefName = prefNameForConfig(config);
////        this.mSharedPreferences = context.getSharedPreferences(prefName, 0);
////        this.mSecureRandom = SecureRandomFix.createLocalSecureRandom();
//        this.mCryptoConfig = config;
//    }

    @Override
    public synchronized byte[] getCipherKey() {

        return Base64.decode(CK, Base64.DEFAULT);
    }

    @Override
    public byte[] getMacKey() throws KeyChainException {
        return Base64.decode(MC, Base64.DEFAULT);
    }

    @Override
    public byte[] getNewIV() throws KeyChainException {
        return Base64.decode(IV, Base64.DEFAULT);
    }

    @Override
    public void destroyKeys() {

    }
}
