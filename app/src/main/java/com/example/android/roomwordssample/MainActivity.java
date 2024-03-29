package com.example.android.roomwordssample;

/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.commonsware.cwac.saferoom.SQLCipherUtils;
import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;
import com.facebook.soloader.SoLoader;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String ROOT_DOWNLOAD_DIR =
            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "BackupApp" + File.separator;
    public static final String ROOT_DOWNLOAD_DIR_DOCUMENT = ROOT_DOWNLOAD_DIR;

    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;

    private WordViewModel mWordViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        SoLoader.init(this, false);
        // Get a new or existing ViewModel from the ViewModelProvider.
        mWordViewModel = ViewModelProviders.of(this).get(WordViewModel.class);
//        mWordViewModel.closeRoom();
//        else if (SQLCipherUtils.getDatabaseState(dbOri) == SQLCipherUtils.State.ENCRYPTED){
//            Log.d("TAG", "onCreate: sudah di enkripsi"+ SQLCipherUtils.getDatabaseState(dbOri));
//        } else {
//            Log.d("TAG", "onCreate: db belum ada");
//        }
//        mWordViewModel.openRoom();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final WordListAdapter adapter = new WordListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mWordViewModel.getAllWords().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(@Nullable final List<Word> words) {
                // Update the cached copy of the words in the adapter.
                adapter.setWords(words);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewWordActivity.class);
                startActivityForResult(intent, NEW_WORD_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_WORD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Word word = new Word(data.getStringExtra(NewWordActivity.EXTRA_REPLY), 1);
            mWordViewModel.insert(word);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.empty_not_saved,
                    Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_backup:
                Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
//                                WordRoomDatabase appDatabase = WordRoomDatabase.getDatabase(getApplicationContext());
//                                appDatabase.close();

                                Log.d("TAG", "onOptionsItemSelected: " + MainActivity.this.getDatabasePath("word_database").getAbsoluteFile());
                                File dbOri = getDatabasePath("word_database");
                                File dbShmOri = getDatabasePath("word_database-shm");
                                File dbWal = getDatabasePath("word_database-wal");
                                File file = new File(ROOT_DOWNLOAD_DIR_DOCUMENT);
                                if (!file.exists()) {
                                    file.mkdirs();
                                }

                                File db2 = new File(ROOT_DOWNLOAD_DIR_DOCUMENT, "word_database");
                                try {
                                    backupFile(dbOri, db2);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (db2.exists()) {
                                    Log.d("tAG", "onPermissionsChecked: data ada");
                                    db2.setWritable(true);
                                }
                                File dbShm2 = new File(db2.getParent(), "word_database-shm");
                                if (dbShm2.exists()) {
                                    dbShm2.setWritable(true);
                                }
                                File dbWal2 = new File(db2.getParent(), "word_database-wal");
                                if (dbWal2.exists()) {
                                    dbWal2.setWritable(true);
                                }

                                try {
                                    backupFile(dbShmOri, dbShm2);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    backupFile(dbWal, dbWal2);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

//                                try {
//                                    copyFileUsingJava7Files(dbOri, db2);
////                                    copyFileUsingJava7Files(dbShmOri, dbShm2);
////                                    copyFileUsingJava7Files(dbWal, dbWal2);
//                                    Log.d("TAG", "onPermissionsChecked: "+db2.getAbsolutePath());
//                                    Toast.makeText(MainActivity.this, "Backup sukses", Toast.LENGTH_SHORT).show();
//                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                                    startActivity(intent);
//                                    System.exit(0);
//
//
//                                } catch (Exception e) {
//                                    Log.e("TAG", e.toString());
//                                    Toast.makeText(MainActivity.this, "eror", Toast.LENGTH_SHORT).show();
//                                }

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                            }
                        }).check();
                break;
            case R.id.action_restore:
                Dexter.withActivity(this)
                        .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                Toast.makeText(MainActivity.this, "Restore", Toast.LENGTH_SHORT).show();
//                                WordRoomDatabase appDatabase = WordRoomDatabase.getDatabase(getApplicationContext());
//                                appDatabase.close();

                                Log.d("TAG", "onPermissionsChecked: proses restore");

                                File db = new File(ROOT_DOWNLOAD_DIR_DOCUMENT, "word_database");
                                File dbShm = new File(db.getParent(), "word_database-shm");
                                File dbWal = new File(db.getParent(), "word_database-wal");

                                File db2 = getDatabasePath("word_database");
                                File dbShm2 = new File(db2.getParent(), "word_database-shm");
                                File dbWal2 = new File(db2.getParent(), "word_database-wal");

                                restoreFile(db, db2);
                                restoreFile(dbShm, dbShm2);
                                restoreFile(dbWal, dbWal2);
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(intent);
                                System.exit(0);
//                                try {
////                                    String word = "password";
////                                    char[] pass = word.toCharArray();
////                                    SQLCipherUtils.decrypt(MainActivity.this,db, pass);
////                                    Log.d("TAG", "onCreate: status: " + SQLCipherUtils.getDatabaseState(db));
//                                    copyFileUsingJava7Files(db, db2);
////                                    copyFileUsingJava7Files(dbShm, dbShm2);
////                                    copyFileUsingJava7Files(dbWal, dbWal2);
//                                    Toast.makeText(MainActivity.this, "restore sukses", Toast.LENGTH_SHORT).show();
//                                    Log.d("TAG", "onPermissionsChecked: suskes restor");
////                                    mWordViewModel.closeRoom();
////                                    mWordViewModel.openRoom();
////                                    mWordViewModel.restore();
//                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                                    startActivity(intent);
//                                    System.exit(0);
//
//
//                                } catch (Exception e) {
//                                    Log.d("TAG", "eror restore" + e);
//                                    Toast.makeText(MainActivity.this, "restor eror", Toast.LENGTH_SHORT).show();
//                                }

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                            }
                        }).check();
                break;

            case R.id.action_enkripsi:
                File dbOri = getDatabasePath("word_database");
                String word = "password";
                char[] pass = word.toCharArray();
                try {
                    SQLCipherUtils.encrypt(this, dbOri, pass);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("TAG", "onCreate: proses enkripsi" + SQLCipherUtils.getDatabaseState(dbOri));
                break;

        }
        return true;
    }


    private static void copyFileUsingJava7Files(File source, File dest) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (dest.exists()) {
                Files.deleteIfExists(dest.toPath());
                Files.copy(source.toPath(), dest.toPath());
            } else {
                Files.copy(source.toPath(), dest.toPath());
            }
        }
    }


    private void backupFile(File fileOri, File fileDestiny) throws IOException {

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(fileOri);
            os = new FileOutputStream(fileDestiny);
            KeyChain keyChain = new CustomKeyChain();
            Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
            if (!crypto.isAvailable()) {
                return;
            }
            OutputStream outputStreamEnkrip = crypto.getCipherOutputStream(os,Entity.create("entity_id"));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                outputStreamEnkrip.write(buffer, 0, length);
            }
            outputStreamEnkrip.close();
        } catch (IOException | CryptoInitializationException | KeyChainException e) {
            e.printStackTrace();
        }
//        try {
//            // Creates a new Crypto object with default implementations of a key chain
////            KeyChain keyChain = new SharedPrefsBackedKeyChain(MainActivity.this, CryptoConfig.KEY_256);
//            KeyChain keyChain = new CustomKeyChain();
//            try {
//                Log.d("TAG", "backupFile: keychain key; "+ Arrays.toString(keyChain.getCipherKey()));
//            } catch (KeyChainException e) {
//                e.printStackTrace();
//            }
//            Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
//            if (!crypto.isAvailable()) {
//                return;
//            }
//
//            FileInputStream inputStream = new FileInputStream(fileOri);
//            OutputStream outStream = new FileOutputStream(fileDestiny);
//            OutputStream outputStreamEnkrip = null;
//            try {
//                outputStreamEnkrip = crypto.getCipherOutputStream(
//                        outStream,
//                        Entity.create("entity_id"));
//            } catch (CryptoInitializationException e) {
//                e.printStackTrace();
//            } catch (KeyChainException e) {
//                e.printStackTrace();
//            }
//
//            int read;
//            byte[] buffer = new byte[1024];
//            while ((read = inputStream.read(buffer)) != -1) {
//                outStream.write(buffer, 0, read);
//            }
//
//            outStream.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void restoreFile(File ori, File destiny) {
        try {
//            KeyChain keyChain = new SharedPrefsBackedKeyChain(MainActivity.this, CryptoConfig.KEY_256);
            KeyChain keyChain = new CustomKeyChain();
            Log.d("tAG", "restoreFile: key chiperkey: " + keyChain.getCipherKey());
            Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
            if (!crypto.isAvailable()) {
                return;
            }
            FileInputStream fileStreamChipper = new FileInputStream(ori);
            InputStream inputStream = null;

            inputStream = crypto.getCipherInputStream(
                    fileStreamChipper,
                    Entity.create("entity_id"));

            OutputStream outStream = new FileOutputStream(destiny);

            int read;
            byte[] buffer = new byte[1024];
            while ((read = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, read);
            }
            Log.d("TAG", "restoreFile: proses restore");
            outStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d("TAG", "restoreFile: io" + e);
        } catch (CryptoInitializationException e) {
            Log.d("tAG", "restoreFile: crypto init");
        } catch (KeyChainException e) {
            Log.d("TAG", "restoreFile: keychain");
        }


    }


}
