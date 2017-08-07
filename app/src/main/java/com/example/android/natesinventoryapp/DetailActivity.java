package com.example.android.natesinventoryapp;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry;

import java.text.DecimalFormat;

import static com.example.android.natesinventoryapp.EditorActivity.LOG_TAG;
import static com.example.android.natesinventoryapp.data.InventoryContract.DEC_FORMAT;
import static com.example.android.natesinventoryapp.data.Utils.getBitmapFromUri;


/**
 * Created by Hardkornate on 7/30/17.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_INVENTORY_LOADER = 0;
    private static final String STATE_URI = "STATE_URI";
    private static final int SEND_MAIL_REQUEST = 1;
    private final Context mContext = this;
    private Uri mCurrentItemUri;
    private Uri mImageUri;
    private TextView nameTextView, supplierTextView, quantityTextView, priceTextView, mTextView;
    private String mSupplierEmail = "hardkornate@gmail.com";
    private String mName = "Widget";
    private String mItemUriString = null;
    private String mImageUriString = null;
    private Button orderButton, deleteButton;
    private ImageButton incrementButton, decrementButton;
    private ImageView mImageView;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mCurrentItemUri = super.getIntent().getData();

        nameTextView = (TextView) findViewById(R.id.display_item_name);
        supplierTextView = (TextView) findViewById(R.id.display_supplier_email);
        priceTextView = (TextView) findViewById(R.id.display_item_price);
        quantityTextView = (TextView) findViewById(R.id.display_item_quantity);
        orderButton = (Button) findViewById(R.id.order_item_button);
        deleteButton = (Button) findViewById(R.id.delete_item_button);
        incrementButton = (ImageButton) findViewById(R.id.increment);
        decrementButton = (ImageButton) findViewById(R.id.decrement);
        mTextView = (TextView) findViewById(R.id.image_uri);
        mImageView = (ImageView) findViewById(R.id.detail_image);

        if (mCurrentItemUri != null) {
            getLoaderManager().initLoader(DETAIL_INVENTORY_LOADER, null, this);
        }

        mFab = (FloatingActionButton) findViewById(R.id.fabDetail);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemUriString = mCurrentItemUri.toString();
                mImageUriString = mImageUri.toString();
                Intent intent = new Intent(DetailActivity.this, EditorActivity.class);
                intent.putExtra("URI", mItemUriString);
                intent.putExtra("IMAGE URI", mImageUriString);
                startActivity(intent);
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_SUPPLIER,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_IMAGE
        };

        return new CursorLoader(
                this,                       // Parent activity context
                mCurrentItemUri,            // Table to query
                projection,                 // Projection
                null,                       // Selection clause
                null,                       // Selection arguments
                null                        // Default sort order
        );
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
            DatabaseUtils.dumpCursor(cursor);
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);

            // Extract out the value from the Cursor for the given column index
            mName = cursor.getString(nameColumnIndex);
            mSupplierEmail = cursor.getString(supplierColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);
            String mImage = cursor.getString(imageColumnIndex);

            DecimalFormat dec = new DecimalFormat(DEC_FORMAT);
            String mPrice = dec.format(price);
            String mQuantity = String.valueOf(quantity);
            // Update the views on the screen with the values from the database
            nameTextView.setText(mName);
            supplierTextView.setText(mSupplierEmail);
            priceTextView.setText(mPrice);
            quantityTextView.setText(mQuantity);
            if (mCurrentItemUri != null) {
                ContentValues mValues = new ContentValues();
                if (mName != null) {
                    mValues.put(InventoryEntry.COLUMN_ITEM_NAME, mName);
                }
                if (mSupplierEmail != null) {
                    mValues.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, mSupplierEmail);
                }
                if (price >= 0) {
                    mValues.put(InventoryEntry.COLUMN_ITEM_PRICE, price);
                }
                if (quantity >= 0) {
                    mValues.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
                }
                if (mImage != null) {
                    mValues.put(InventoryEntry.COLUMN_ITEM_IMAGE, mImage);
                }
                getContentResolver().update(mCurrentItemUri, mValues, null, null);
            }

            // Display image attached to the product
            if (mImage != null) {
                Log.i(LOG_TAG, "Uri: " + mImage);
                mImageUri = Uri.parse(mImage);
                mImageView.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mImageView));
            } else {
                Log.i(LOG_TAG, "Uri: null");
            }

            orderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    composeEmail(getString(R.string.subject) + " of " + mName, getString(R.string.emailBeginning) + mName + getString(R.string.emailEnding), mSupplierEmail);
                }

            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmDeleteItem();
                }

            });

            incrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adjustInventory(mCurrentItemUri, (quantity + 1));
                }

            });

            decrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adjustInventory(mCurrentItemUri, (quantity - 1));
                }

            });

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCurrentItemUri != null)
            outState.putString(STATE_URI, mCurrentItemUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mCurrentItemUri = Uri.parse(savedInstanceState.getString(STATE_URI));
            mTextView.setText(mCurrentItemUri.toString());

            ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageView.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mImageView));
                }
            });
        }
    }


    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void adjustInventory(Uri itemUri, int mQuantity) {
        if (mQuantity < 0) {
            mQuantity = 0;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, mQuantity);
        getContentResolver().update(itemUri, values, null, null);
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

    /**   private void sendEmail() {
        if (mCurrentItemUri != null) {
            String subject = "URI Example";
            String stream = "Hello! \n"
                    + "Uri example" + ".\n"
                    + "Uri: " + mCurrentItemUri.toString() + "\n";

            Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                    .setStream(mCurrentItemUri)
                    .setSubject(subject)
                    .setText(stream)
                    .getIntent();

            // Provide read access
            shareIntent.setData(mCurrentItemUri);
            shareIntent.setType("message/rfc822");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
     shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

            startActivityForResult(Intent.createChooser(shareIntent, "Share with"), SEND_MAIL_REQUEST);

        } else {
            Snackbar.make(mFab, "Image not selected", Snackbar.LENGTH_LONG)
                    .setAction("Select", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openImageSelector();
                        }
                    }).show();
        }
    }


     * Method to ask confirmation for deleting a product
     */
    private void confirmDeleteItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_delete));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
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
     * Method to delete the product
     */
    private void deleteItem() {

        // Only perform the delete if this is an existing product
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete
                Toast.makeText(this, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful
                Toast.makeText(this, getString(R.string.delete_successful), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}
