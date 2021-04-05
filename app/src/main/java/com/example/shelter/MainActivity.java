 /*
  * Copyright (C) 2016 The Android Open Source Project
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
 package com.example.shelter;

 import android.content.ContentValues;
 import android.content.CursorLoader;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;

 import androidx.appcompat.app.AlertDialog;
 import androidx.appcompat.app.AppCompatActivity;

 import com.example.shelter.data.PetContract;
 import com.example.shelter.data.PetContract.PetEntry;
 import com.google.android.material.floatingactionbutton.FloatingActionButton;
 /**
  * Displays list of pets that were entered and stored in the app.
  */
 public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {
private  static  final int PET_LOADER=0;
PetCursorAdapter mCursorAdapter;

     /** Database helper that will provide us access to the database */


     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_catalog);
         // Setup FAB to open EditorActivity
         FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
         fab.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                 startActivity(intent);
             }
         });
         ListView lvItems = (ListView) findViewById(R.id.list);
         View emptive=(View)findViewById(R.id.empty_view);
         lvItems.setEmptyView(emptive);

          mCursorAdapter= new PetCursorAdapter(this, null);
          lvItems.setAdapter(mCursorAdapter);

           //Kickof the loader
         //getLoaderManager().initLoader(PET_LOADER,null,this);
         android.app.LoaderManager loaderManager=getLoaderManager();
         loaderManager.initLoader(PET_LOADER,null,this);
         lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Uri uri=PetEntry.CONTENT_URI;
                 uri=Uri.withAppendedPath(uri,String.valueOf(id));
                 Intent intent=new Intent(MainActivity.this,EditorActivity.class);
                 intent.setData(uri);


                 startActivity(intent);
             }
         });
     }
    /* private void displayDatabaseInfo() {

          String []projection={PetEntry._ID,
                  PetEntry.COLUMN_PET_NAME,
                  PetEntry.COLUMN_PET_BREED,
                  PetEntry.COLUMN_PET_GENDER,
                  PetEntry.COLUMN_PET_WEIGHT};
       Cursor cursor=getContentResolver().query(PetEntry.CONTENT_URI,projection,null,null,null);
         ListView lvItems = (ListView) findViewById(R.id.list);
         View emptive=(View)findViewById(R.id.empty_view);
         lvItems.setEmptyView(emptive);

         PetCursorAdapter petCursorAdapter= new PetCursorAdapter(this, cursor);

         lvItems.setAdapter(petCursorAdapter);

     }
*/

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu options from the res/menu/menu_catalog.xml file.
         // This adds menu items to the app bar.
         getMenuInflater().inflate(R.menu.menu_catalog, menu);
         return true;
     }
     private  void insertPet() {
         // TODO: Insert a single pet into the database

         ContentValues values = new ContentValues();
         values.put(PetEntry.COLUMN_PET_NAME, "Garfield");
         values.put(PetEntry.COLUMN_PET_BREED, "Tabby");
         values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
         values.put(PetEntry.COLUMN_PET_WEIGHT, 7);
         Uri newUri= getContentResolver().insert(PetContract.PetEntry.CONTENT_URI,values);

        // displayDatabaseInfo();
     }
     void deleteEntry()
     {
         getContentResolver().delete(PetEntry.CONTENT_URI,null,null);
     }
     private void deletable()
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Are you sure to delete all the pets?");
         builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // User clicked the "Delete" button, so delete the pet.
                 deleteEntry();

             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // User clicked the "Cancel" button, so dismiss the dialog
                 // and continue editing the pet.
                 if (dialog != null) {
                     dialog.dismiss();
                 }
             }
         });

         // Create and show the AlertDialog
         AlertDialog alertDialog = builder.create();
         alertDialog.show();


     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // User clicked on a menu option in the app bar overflow menu
         switch (item.getItemId()) {
             // Respond to a click on the "Insert dummy data" menu option
             case R.id.action_insert_dummy_data:
                 // Do nothing for now
                 insertPet();
                 return true;
             // Respond to a click on the "Delete all entries" menu option
             case R.id.action_delete_all_entries:
                 deletable();
                 // Do nothing for now
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }

     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         String[] projection = {
                 PetEntry._ID,
                 PetEntry.COLUMN_PET_NAME,
                 PetEntry.COLUMN_PET_BREED
         };
         return new CursorLoader(this,PetEntry.CONTENT_URI,projection,null,null
         ,null);
     }

     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mCursorAdapter.swapCursor(data);
     }

     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
    mCursorAdapter.swapCursor(null);
     }


 }