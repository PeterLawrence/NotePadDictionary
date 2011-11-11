/*
 * Copyright (C) 2007 The Android Open Source Project
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
 * 
 *  *  This code has been extensively modified by P.J.Lawrence (August 2011) to incorporate  
 *  1) Spellchecking functionality (Currently using francois.pessaux.droidic.android dictionary)
 * 
 */

package pj.lawrence.android.notepad;


import java.io.IOException;
import java.io.InputStream;

import pj.lawrence.android.notepad.NotePad.Notes;

import pj.lawrence.android.notepad.R;

import francois.pessaux.droidic.android.CLikeStringUtils;
import francois.pessaux.droidic.android.DicManagement;
import francois.pessaux.droidic.android.Global;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.content.SharedPreferences ;


/**
 * Displays a list of notes. Will display notes from the {@link Uri}
 * provided in the intent if there is one, otherwise defaults to displaying the
 * contents of the {@link NotePadProvider}
 */
public class NotesList extends ListActivity {
    private static final String TAG = "NotesList";

    // Menu item ids
    public static final int MENU_ITEM_DELETE = Menu.FIRST;
    public static final int MENU_ITEM_INSERT = Menu.FIRST + 1;
    public static final int EXITAPP_ID = Menu.FIRST + 2;
    public static final int ABOUTAPP_ID = Menu.FIRST + 3;
    public static final int MENU_DICT_ID = Menu.FIRST + 4;

    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Notes._ID, // 0
            Notes.TITLE, // 1
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    
    // dictionary data
    int mCurrentDicIndex = 0;
    private final CharSequence[] availableDicts = {
    	    "English", "Francais", "Italiano", "Norsk", "Portugues", "Espanol",
    	    "Svenska"
    	  } ;
    
    private final static int DIALOG_ID_SELECT_DIC = 0 ;
    private final static int DIALOG_ID_ABOUT = 1 ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Notes.CONTENT_URI);
        }

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                Notes.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.noteslist_item, cursor,
                new String[] { Notes.TITLE }, new int[] { android.R.id.text1 });
        setListAdapter(adapter);
        
        // Dictionary
        loadPrefs();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_insert)
                .setShortcut('3', 'a')
                .setIcon(android.R.drawable.ic_menu_add);
        
        menu.add(0, MENU_DICT_ID, 0, R.string.menu_dict)  
        .setShortcut('0', 'd')
        .setIcon(android.R.drawable.ic_menu_manage);
        
        menu.add(0, ABOUTAPP_ID, 0, R.string.menu_about)  
        .setShortcut('0', 'a')
        .setIcon(android.R.drawable.ic_menu_info_details);
        
        menu.add(0, EXITAPP_ID, 0, R.string.menu_exit)  
    	        .setShortcut('0', 'e')
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
       
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final boolean haveItems = getListAdapter().getCount() > 0;

        // If there are any notes in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection.  This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        if (haveItems) {
            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Build menu...  always starts with the EDIT action...
            Intent[] specifics = new Intent[1];
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
            MenuItem[] items = new MenuItem[1];

            // ... is followed by whatever other actions are available...
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null, specifics, intent, 0,
                    items);

            // Give a shortcut to the edit action.
            if (items[0] != null) {
                items[0].setShortcut('1', 'e');
            }
        } else {
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_INSERT:
            // Launch activity to insert a new item
            startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            return true;
        case EXITAPP_ID:
            finish ();
            return (true);
        case ABOUTAPP_ID:
            showDialog (DIALOG_ID_ABOUT) ;
            return (true) ;
        case MENU_DICT_ID:
            showDialog (DIALOG_ID_SELECT_DIC) ;
            return (true) ;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete);
    }
        
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            case MENU_ITEM_DELETE: {
                // Delete the note that the context menu is for
                Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
                getContentResolver().delete(noteUri, null, null);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a note selected by
            // the user.  The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
    
    // saved settings
    private void savePrefs ()
    {
      /* We need an Editor object to make preference changes. */
      SharedPreferences settings = getPreferences (MODE_PRIVATE) ;
      SharedPreferences.Editor editor = settings.edit () ;
      editor.putInt ("dicIndex", mCurrentDicIndex) ;
      
      /* Commit the edits. */
      editor.commit () ;
    }
    
    private void loadPrefs ()
    {
    	/* Restore last preferences. */
        SharedPreferences settings = getPreferences (MODE_PRIVATE) ;
        mCurrentDicIndex = settings.getInt ("dicIndex", 0) ;
        int dicResId = dicResIdFromDicIndex (mCurrentDicIndex) ;
        if (initDictionaryResource (dicResId))
        {
        	Global.case_sensitive=false;
        }
        else
        {
        	// Dictionary not found
        	AlertDialog.Builder builder;
        	builder = new AlertDialog.Builder (this) ;
            builder.setMessage (R.string.error_dictionary_load)
              .setCancelable (false)
              .setPositiveButton (R.string.button_quit, new DialogInterface.OnClickListener () {
                public void onClick (DialogInterface dialog, int id)
                {
                  System.exit (0) ;
                }}) ;
            builder.create();
            builder.show();
        }
    }
    
    
    // Dictionary
    private int dicResIdFromDicIndex (int index)
    {
      int id = -1 ;
      
      switch (index) {
      case 0: id = R.raw.english ; break ;
      case 1: id = R.raw.french ; break ;
      case 2: id = R.raw.italian ; break ;
      case 3: id = R.raw.norwegian ; break ;
      case 4: id = R.raw.portuguese ; break ;
      case 5: id = R.raw.spanish ; break ;
      case 6: id = R.raw.swedish ; break ;
      }
      return (id) ;
    }
    
    private boolean initDictionaryResource (int dicId)
    {
      try {
        Resources resources = getResources () ;
        InputStream dictInputStream = resources.openRawResource (dicId) ;
        int fileSize ;
        fileSize = dictInputStream.available () ;
        byte rawData[] = new byte[fileSize] ;
        dictInputStream.read (rawData, 0, fileSize) ;
        dictInputStream.close () ;
        int rawInts[] = CLikeStringUtils.intArrayFromByteArray (rawData) ;
        return (DicManagement.loadDictionaryFromRawData (rawInts)) ;
      }
      catch (IOException e) {
        return (false) ;
        }
    }
    
    @Override
    public void finish ()
    {
    	savePrefs();
    	DicManagement.unloadDictionary () ;
    	System.exit (0) ;
    }
    
    @Override
    protected Dialog onCreateDialog (int id)
    {
      AlertDialog.Builder builder ;
      AlertDialog alert ;
      
      switch (id) {
              
      case DIALOG_ID_SELECT_DIC:
        builder = new AlertDialog.Builder (this) ;
       // builder.setTitle (R.string.titleSelectDict) ;
        builder.setItems (availableDicts, new DialogInterface.OnClickListener () {
            public void onClick (DialogInterface dialog, int item)
            {
              int dicId = dicResIdFromDicIndex (item) ;
              if (dicId != -1) {
            	  mCurrentDicIndex = item ;
            	  DicManagement.unloadDictionary () ;
            	  initDictionaryResource (dicId) ;
              }
            }}) ;
        alert = builder.create () ;
        return (alert);
        
      case DIALOG_ID_ABOUT:
        builder = new AlertDialog.Builder (this) ;
        builder.setMessage ("NotePad Dictionary Version")
          .setCancelable (true)
          .setPositiveButton (R.string.button_ok, new DialogInterface.OnClickListener () {
            public void onClick (DialogInterface dialog, int id) { }}) ;
        alert = builder.create () ;
        //alert.show () ;
        return (alert);
        }
      return (null);
    }  
}
