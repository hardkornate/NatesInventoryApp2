package com.example.android.natesinventoryapp;


import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentUris.parseId;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry.COLUMN_ITEM_NAME;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry._ID;

import com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry;

import java.text.DecimalFormat;


/**
 * Created by Hardkornate on 7/30/17.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_INVENTORY_LOADER = 0;
    private Uri mCurrentItemUri;

    private TextView nameTextView, supplierTextView, quantityTextView, priceTextView;
    private String mSupplierEmail = "test@gmail.com";
    private String mName = "Widget";
    private Button orderButton, deleteButton;
    private ImageButton incrementButton, decrementButton;

    private Context context = this;
    private ContentValues values = new ContentValues();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        /* Content URI for the existing item (null if it's a new item) */
        mCurrentItemUri = intent.getData();
        getLoaderManager().getLoader(DETAIL_INVENTORY_LOADER);

        nameTextView = (TextView) findViewById(R.id.display_item_name);
        supplierTextView = (TextView) findViewById(R.id.display_supplier_email);
        priceTextView = (TextView) findViewById(R.id.display_item_price);
        quantityTextView = (TextView) findViewById(R.id.display_item_quantity);
        orderButton = (Button) findViewById(R.id.order_item_button);
        deleteButton = (Button) findViewById(R.id.delete_item_button);
        incrementButton = (ImageButton) findViewById(R.id.increment);
        decrementButton = (ImageButton) findViewById(R.id.decrement);

        orderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                composeEmail(getString(R.string.subject) + " of " + mName, getString(R.string.emailBeginning) + mName + getString(R.string.emailEnding), mSupplierEmail);
            }

        });

        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if ((context.getContentResolver().delete(mCurrentItemUri, null, null)) > 0) {
                    Toast.makeText(context," ITEM DELETED ",Toast.LENGTH_SHORT).show();
                }
                finish();
            }

        });

        incrementButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int quantity = values.getAsInteger(COLUMN_ITEM_QUANTITY);
                int newItemQuantity = quantity + 1;
                quantityTextView.setText(String.valueOf(newItemQuantity));
                values.put(COLUMN_ITEM_QUANTITY, newItemQuantity);
                context.getContentResolver().update(mCurrentItemUri, values, null, null);
            }

        });
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = values.getAsInteger(COLUMN_ITEM_QUANTITY);
                if (quantity > 0) {
                    int newItemQuantity = quantity - 1;
                    quantityTextView.setText(String.valueOf(newItemQuantity));
                    values.put(COLUMN_ITEM_QUANTITY, newItemQuantity);
                    context.getContentResolver().update(mCurrentItemUri, values, null, null);

                } else {
                    Toast.makeText(context, " no products available , all product sold ", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    @Override
    public CursorLoader onCreateLoader(int id, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the inventory table
        String[] projection = {
                _ID,
                COLUMN_ITEM_NAME,
                COLUMN_ITEM_SUPPLIER,
                COLUMN_ITEM_PRICE,
                COLUMN_ITEM_QUANTITY };
        String mId = String.valueOf(parseId(mCurrentItemUri));
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, // Parent activity context
                mCurrentItemUri, // Query the content URI for the current item
                projection, // Columns to include in the resulting Cursor
                mId, // No selection clause
                null, // No selection arguments
                null); // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            mName = cursor.getString(nameColumnIndex);
            mSupplierEmail = cursor.getString(supplierColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            DecimalFormat dec = new DecimalFormat("$####.00");
            String mPrice = dec.format(price);
            String mQuantity = String.valueOf(quantity);
            // Update the views on the screen with the values from the database
            nameTextView.setText(mName);
            supplierTextView.setText(mSupplierEmail);
            priceTextView.setText(mPrice);
            quantityTextView.setText(mQuantity);

            // Create a ContentValues object where column names are the keys,
            // and item attributes from the editor are the values.
            values.put(InventoryEntry.COLUMN_ITEM_NAME, mName);
            values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, mSupplierEmail);
            values.put(InventoryEntry.COLUMN_ITEM_PRICE, mPrice);
            values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, mQuantity);

            context.getContentResolver().update(mCurrentItemUri, values, null, null);

        }
    }



    @Override
    public void onLoaderReset(Loader loader) {
        nameTextView.setText(mName = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME));
        supplierTextView.setText(mSupplierEmail = values.getAsString(InventoryEntry.COLUMN_ITEM_SUPPLIER));
        priceTextView.setText(values.getAsString(InventoryEntry.COLUMN_ITEM_PRICE));
        quantityTextView.setText(values.getAsString(InventoryEntry.COLUMN_ITEM_QUANTITY));
    }


    private void composeEmail(String subject, String body, String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email)); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        finish();
    }

}
