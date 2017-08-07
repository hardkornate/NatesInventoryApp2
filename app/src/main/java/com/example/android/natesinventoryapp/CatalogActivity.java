package com.example.android.natesinventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry;
/**
 * Displays list of inventory items that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;
    private InventoryCursorAdapter mCursorAdapter;
    private View mEmptyStateView;
    private ListView mListview;
    private View mEmptyTextView, mEmptySubTextView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        // Setup FAB to open EditorActivity
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        mEmptyStateView = findViewById(R.id.empty);
        mEmptyTextView = findViewById(R.id.empty_title_text);
        mEmptySubTextView = findViewById(R.id.empty_sub_title_text);
        mListview = (ListView) findViewById(R.id.list);
        ((ViewGroup) mEmptyStateView.getParent()).removeView(mEmptyStateView);
        ViewGroup parentGroup = (ViewGroup) mListview.getParent();
        parentGroup.addView(mEmptyStateView);
        mListview.setEmptyView(mEmptyStateView);
        // Setup an Adapter to create a list item for each row of inventory data in the Cursor.
        // There is no inventory data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventoryCursorAdapter(this);
        mListview.setAdapter(mCursorAdapter);
        mListview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if (mCursorAdapter != null) {
            mListview.setVisibility((mCursorAdapter.isEmpty()) ? View.GONE : View.VISIBLE);
            mEmptyTextView.setVisibility((mCursorAdapter.isEmpty()) ? View.VISIBLE : View.GONE);
            mEmptySubTextView.setVisibility((mCursorAdapter.isEmpty()) ? View.VISIBLE : View.GONE);
        }
        // Setup the item click listener
        AdapterView.OnItemClickListener mDetailOnItemClickListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                // Create new intent to go to {@link DetailActivity}
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific inventory that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link InventoryEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.inventory/inventory/2"
                // if the inventory with ID 2 was clicked on.
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                // Set the URI on the data field of the intent
                intent.setData(currentInventoryUri);

                // Launch the {@link EditorActivity} to display the data for the current inventory.
                startActivity(intent);
            }
        };

        mListview.setOnItemClickListener(mDetailOnItemClickListener);
        // Kick off the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        if (mCursorAdapter != null) {
            mListview.setVisibility(mCursorAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }
    /**
     * Helper method to insert hardcoded item data into the database. For debugging purposes only.
     */
    private void insertItem() {
        // Create a ContentValues object where column names are the keys,
        // and item attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, "test");
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, "noone@gmail.com");
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, 1.00);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, 0);
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE, R.drawable.ic_insert_photo_white_48dp);

        // Insert a new row for an item into the provider using the ContentResolver.
        // Use the {@link InventoryEntry#CONTENT_URI} to indicate that we want to insert
        // into the inventory database table.
        // Receive the new content URI that will allow us to access the item's data in the future.
        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_SUPPLIER,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_IMAGE
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                InventoryEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link InventoryCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
