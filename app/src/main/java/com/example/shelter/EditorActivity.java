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
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;

 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.app.NavUtils;

 import com.example.shelter.data.PetContract;
 import com.example.shelter.data.PetContract.PetEntry;

 /**
  * Allows user to create a new pet or edit an existing one.
  */
 public class EditorActivity extends AppCompatActivity {

     /** EditText field to enter the pet's name */
     private EditText mNameEditText;

     /** EditText field to enter the pet's breed */
     private EditText mBreedEditText;

     /** EditText field to enter the pet's weight */
     private EditText mWeightEditText;

     /** EditText field to enter the pet's gender */
     private Spinner mGenderSpinner;

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
         // Find all relevant views that we will need to read user input from
         mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
         mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
         mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
         mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

         setupSpinner();
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
    private  void insertPet()
    {

        String nameString = mNameEditText.getText().toString().trim();
        if(nameString.length()==0)
        {
            Toast.makeText(this, "Name Field cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        int weight;
        try {
            weight = Integer.parseInt(weightString);
        }
        catch (NumberFormatException e)
        {
            //return;
            weight = 0;
        }
        if(weight<0)
        {
            Toast.makeText(this, "Weight can't be negative", Toast.LENGTH_SHORT).show();
            return;
        }
       // int weight = Integer.parseInt(weightString);

        // Create database helper


        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        // Insert a new row for pet in the database, returning the ID of that new row.
        long newRowId= ContentUris.parseId(getContentResolver().insert(PetContract.PetEntry.CONTENT_URI,values));
        //long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, R.string.pet_saved_error, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, R.string.pet_saved_success , Toast.LENGTH_SHORT).show();
        }
    }
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu options from the res/menu/menu_editor.xml file.
         // This adds menu items to the app bar.
         getMenuInflater().inflate(R.menu.menu_editor, menu);
         return true;
     }
     public void deleteentry()
     {
         mNameEditText.setText("");
         mBreedEditText.setText("");
         mWeightEditText.setText("");
         setupSpinner();
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // User clicked on a menu option in the app bar overflow menu
         switch (item.getItemId()) {
             // Respond to a click on the "Save" menu option
             case R.id.action_save:
                 // Do nothing for now
                 insertPet();
                 finish();
                 return true;
             // Respond to a click on the "Delete" menu option
             case R.id.action_delete:
                 // Do nothing for now
                 deleteentry();
                 return true;
             // Respond to a click on the "Up" arrow button in the app bar
             case android.R.id.home:
                 // Navigate back to parent activity (CatalogActivity)
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 }