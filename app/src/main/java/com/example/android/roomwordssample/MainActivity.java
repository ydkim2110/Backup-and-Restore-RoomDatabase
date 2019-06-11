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
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;

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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static String DB_PATH = "/data/data/com.example.android.roomwordssample/databases/";

    public static final String ROOT_DOWNLOAD_DIR =
            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "BackupApp" + File.separator;
    public static final String ROOT_DOWNLOAD_DIR_DOCUMENT = ROOT_DOWNLOAD_DIR + "Documents"  + File.separator;

    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;

    private WordViewModel mWordViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final WordListAdapter adapter = new WordListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        mWordViewModel = ViewModelProviders.of(this).get(WordViewModel.class);

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
            Word word = new Word(data.getStringExtra(NewWordActivity.EXTRA_REPLY),1);
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
                                Log.d("TAG", "onOptionsItemSelected: test: " + MainActivity.this.getDatabasePath("word_database").getAbsoluteFile());
                                File dbOri = getDatabasePath("word_database");
                                File dbShmOri = getDatabasePath("word_database-shm");
                                File dbWal = getDatabasePath("word_database-wal");
                                File file = new File(ROOT_DOWNLOAD_DIR_DOCUMENT);
                                if (!file.exists()) {
                                    file.mkdirs();
                                }

                                File db2 = new File(ROOT_DOWNLOAD_DIR_DOCUMENT, "word_database");
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
                                    copyFileUsingJava7Files(dbOri, db2);
                                    copyFileUsingJava7Files(dbShmOri, dbShm2);
                                    copyFileUsingJava7Files(dbWal, dbWal2);
                                    Toast.makeText(MainActivity.this, "Backup", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.e("TAG", e.toString());
                                    Toast.makeText(MainActivity.this, "eror", Toast.LENGTH_SHORT).show();

                                }


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
                                Log.d("TAG", "onPermissionsChecked: resteror");

                                File db = new File(ROOT_DOWNLOAD_DIR_DOCUMENT, "word_database");
                                File dbShm = new File(db.getParent(), "word_database-shm");
                                File dbWal = new File(db.getParent(), "word_database-wal");

                                File db2 = getDatabasePath("word_database");
                                File dbShm2 = new File(db2.getParent(), "word_database-shm");
                                File dbWal2 = new File(db2.getParent(), "word_database-wal");

                                try {
                                    copyFileUsingJava7Files(db, db2);
                                    copyFileUsingJava7Files(dbShm, dbShm2);
                                    copyFileUsingJava7Files(dbWal, dbWal2);
//                                    mWordViewModel.restore();
                                } catch (Exception e) {
                                    Log.d("TAG", e.toString());
                                }

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                            }
                        }).check();
                break;
        }
        return true;
    }


    private static void copyFileUsingJava7Files(File source, File dest) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (dest.exists()) {
                Files.deleteIfExists(dest.toPath());
                Files.copy(source.toPath(), dest.toPath());
            } else  {
                Files.copy(source.toPath(), dest.toPath());
            }
        }
    }


}
