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

 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.CursorLoader;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;

 import androidx.appcompat.app.AlertDialog;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.app.NavUtils;

 import com.example.shelter.data.PetContract;
 import com.example.shelter.data.PetContract.PetEntry;

 /**
  * Allows user to create a new pet or edit an existing one.
  */
 public class EditorActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>{

     private static final int PET_LOADER =0 ;
     /** EditText field to enter the pet's name */
     private EditText mNameEditText;

     /** EditText field to enter the pet's breed */
     private EditText mBreedEditText;

     /** EditText field to enter the pet's weight */
     private EditText mWeightEditText;

     /** EditText field to enter the pet's gender */
     private Spinner mGenderSpinner;
     private  Uri CurrentPetUri;
     private boolean mPetHasChanged = false;
    /* /**
      * Gender of the pet. The possible valid values are in the PetContract.java file:
      * {@link PetEntry#GENDER_UNKNOWN}, {@link PetEntry#GENDER_MALE}, or
      * {@link PetEntry#GENDER_FEMALE}.
      */
     private int mGender = 0;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_editor);
         Intent intent=getIntent();
         CurrentPetUri=intent.getData();

         // Find all relevant views that we will need to read user input from
         mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
         mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
         mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
         mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
         mNameEditText.setOnTouchListener(mTouchListener);
         mBreedEditText.setOnTouchListener(mTouchListener);
         mWeightEditText.setOnTouchListener(mTouchListener);
         mGenderSpinner.setOnTouchListener(mTouchListener);
         setupSpinner();
         if(CurrentPetUri==null) {
             setTitle("Add a Pet");
             invalidateOptionsMenu();
         }
         else {
             setTitle("Edit a Pet");
             android.app.LoaderManager loaderManager=getLoaderManager();
             loaderManager.initLoader(PET_LOADER,null,this);
         }
         // Solving the bug due to rotation of activity
         if (savedInstanceState != null) {
             mPetHasChanged = savedInstanceState.getBoolean("mPetHasChanged");
         }


     }
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putBoolean("mPetHasChanged", mPetHasChanged);
     }
     /**
      * Setup the dropdown spinner that allows the user to select the gender of the pet.
      */
     private void setupSpinner() {
         // Create adapter for spinner. The list options are from the String array it will use
         // the spinner will use the default layout
         ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                 R.array.array_gender_options, android.R.layout.simple_spinner_item);

         // Specify dropdown layout style - simple list view with 1 item per line
         genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

         // Apply the adapter to the spinner
         mGenderSpinner.setAdapter(genderSpinnerAdapter);

         // Set the integer mSelected to the constant values
         mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 String selection = (String) parent.getItemAtPosition(position);
                 if (!TextUtils.isEmpty(selection)) {
                     if (selection.equals(getString(R.string.gender_male))) {
                         mGender = PetEntry.GENDER_MALE; // Male
                     } else if (selection.equals(getString(R.string.gender_female))) {
                         mGender = PetEntry.GENDER_FEMALE; // Female
                     } else {
                         mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                     }
                 }
             }

             // Because AdapterView is an abstract class, onNothingSelected must be defined
             @Override
             public void onNothingSelected(AdapterView<?> parent) {
                 mGender = PetEntry.GENDER_UNKNOWN; // Unknown
             }
         });
     }
    private  void savepet()
    {
            String nameString = mNameEditText.getText().toString().trim();
            if (nameString.length() == 0) {
                Toast.makeText(this, "Name Field cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            String breedString = mBreedEditText.getText().toString().trim();
            String weightString = mWeightEditText.getText().toString().trim();
            int weight;
            try {
                weight = Integer.parseInt(weightString);
            } catch (NumberFormatException e) {
                //return;
                weight = 0;
            }
            if (weight < 0) {
                Toast.makeText(this, "Weight can't be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put(PetEntry.COLUMN_PET_NAME, nameString);
            values.put(PetEntry.COLUMN_PET_BREED, breedString);
            values.put(PetEntry.COLUMN_PET_GENDER, mGender);
            values.put(PetEntry.COLUMN_PET_WEIGHT, weight);
            if(CurrentPetUri==null) {

                // Insert a new row for pet in the database, returning the ID of that new row.
                long newRowId = ContentUris.parseId(getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values));
                //long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);

                // Show a toast message depending on whether or not the insertion was successful
                if (newRowId == -1) {
                    // If the row ID is -1, then there was an error with insertion.
                    Toast.makeText(this, R.string.pet_saved_error, Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast with the row ID.
                    Toast.makeText(this, R.string.pet_saved_success, Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                long newRowId=(getContentResolver().update(CurrentPetUri,values,null,null));
                if (newRowId == 0) {
                    // If the row ID is -1, then there was an error with insertion.
                    Toast.makeText(this,  "Error in Updating", Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast with the row ID.
                    Toast.makeText(this, "Pet Updated", Toast.LENGTH_SHORT).show();
                }
            }

    }
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu options from the res/menu/menu_editor.xml file.
         // This adds menu items to the app bar.
         getMenuInflater().inflate(R.menu.menu_editor, menu);
         return true;
     }
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
         // If this is a new pet, hide the "Delete" menu item.
         if (CurrentPetUri == null) {
             MenuItem menuItem = menu.findItem(R.id.action_delete);
             menuItem.setVisible(false);
         }
         return true;
     }
     public void deleteEntry()
     {
         mNameEditText.setText("");
         mBreedEditText.setText("");
         mWeightEditText.setText("");
         setupSpinner();
         long rowAffected=getContentResolver().delete(CurrentPetUri,null,null);
         if(rowAffected!=0)
         {
             Toast.makeText(this,"Pet Deleted",Toast.LENGTH_SHORT).show();
         }
     }
     private void showDeleteConfirmationDialog() {
         // Create an AlertDialog.Builder and set the message, and click listeners
         // for the postivie and negative buttons on the dialog.
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.delete_dialog_msg);
         builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // User clicked the "Delete" button, so delete the pet.
                 deleteEntry();
                 finish();
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

     /**
      * Perform the deletion of the pet in the database.
      */

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // User clicked on a menu option in the app bar overflow menu
         switch (item.getItemId()) {
             // Respond to a click on the "Save" menu option
             case R.id.action_save:
                 // Do nothing for now
                 savepet();
                 finish();
                 return true;
             // Respond to a click on the "Delete" menu option
             case R.id.action_delete:
                 // Do nothing for now
                 showDeleteConfirmationDialog();
                 return true;
             // Respond to a click on the "Up" arrow button in the app bar
             case android.R.id.home:
                 if (!mPetHasChanged) {
                 NavUtils.navigateUpFromSameTask(EditorActivity.this);
                 return true;
             }

             // Otherwise if there are unsaved changes, setup a dialog to warn the user.
             // Create a click listener to handle the user confirming that
             // changes should be discarded.
             DialogInterface.OnClickListener discardButtonClickListener =
                     new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             // User clicked "Discard" button, navigate to parent activity.
                             NavUtils.navigateUpFromSameTask(EditorActivity.this);
                         }
                     };

             // Show a dialog that notifies the user they have unsaved changes
             showUnsavedChangesDialog(discardButtonClickListener);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
     private void showUnsavedChangesDialog(
             DialogInterface.OnClickListener discardButtonClickListener) {
         // Create an AlertDialog.Builder and set the message, and click listeners
         // for the positive and negative buttons on the dialog.
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.unsaved_changes_dialog_msg);
         builder.setPositiveButton(R.string.discard, discardButtonClickListener);
         builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // User clicked the "Keep editing" button, so dismiss the dialog
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
     public void onBackPressed() {
         // If the pet hasn't changed, continue with handling back button press
         if (!mPetHasChanged) {
             super.onBackPressed();
             return;
         }

         // Otherwise if there are unsaved changes, setup a dialog to warn the user.
         // Create a click listener to handle the user confirming that changes should be discarded.
         DialogInterface.OnClickListener discardButtonClickListener =
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         // User clicked "Discard" button, close the current activity.
                         finish();
                     }
                 };

         // Show dialog that there are unsaved changes
         showUnsavedChangesDialog(discardButtonClickListener);
     }
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         String []projection={PetEntry._ID,PetEntry.COLUMN_PET_NAME
         ,PetEntry.COLUMN_PET_BREED,PetEntry.COLUMN_PET_WEIGHT,PetEntry.COLUMN_PET_GENDER};

         return new CursorLoader(this,CurrentPetUri,projection,null,null,null);
     }

     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
         if(cursor.moveToFirst()){
         String name = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_NAME));
         String breed = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_BREED));
         int gender = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER));
         int  weight = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT));
         fillvalues(name,breed,gender,weight);}
     }
     private void fillvalues(String name,String breed,int gender,int weight)
     {
         mNameEditText.setText(name);
         mWeightEditText.setText(String.valueOf(weight));
         mBreedEditText.setText(breed);
         mGenderSpinner.setSelection(gender);

     }
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
    loader.reset();
     }
     private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
         @Override
         public boolean onTouch(View view, MotionEvent motionEvent) {
             mPetHasChanged = true;
             return false;
         }
     };

 }