package com.example.android.natesinventoryapp;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.LoaderManager;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import static com.example.android.natesinventoryapp.EditorActivity.LOG_TAG;
import static com.example.android.natesinventoryapp.data.InventoryContract.DEC_FORMAT;


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
    private ImageView mImageItem;

    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        nameTextView = (TextView) findViewById(R.id.display_item_name);
        supplierTextView = (TextView) findViewById(R.id.display_supplier_email);
        priceTextView = (TextView) findViewById(R.id.display_item_price);
        quantityTextView = (TextView) findViewById(R.id.display_item_quantity);
        orderButton = (Button) findViewById(R.id.order_item_button);
        deleteButton = (Button) findViewById(R.id.delete_item_button);
        incrementButton = (ImageButton) findViewById(R.id.increment);
        decrementButton = (ImageButton) findViewById(R.id.decrement);
        mImageItem = (ImageView) findViewById(R.id.item_image);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        if(mCurrentItemUri != null) {
            getLoaderManager().initLoader(DETAIL_INVENTORY_LOADER, null, this);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,                       // Parent activity context
                mCurrentItemUri,         // Table to query
                null,                       // Projection
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
            final String image = cursor.getString(imageColumnIndex);

            DecimalFormat dec = new DecimalFormat(DEC_FORMAT);
            String mPrice = dec.format(price);
            String mQuantity = String.valueOf(quantity);
            // Update the views on the screen with the values from the database
            nameTextView.setText(mName);
            supplierTextView.setText(mSupplierEmail);
            priceTextView.setText(mPrice);
            quantityTextView.setText(mQuantity);

            // Display image attached to the product
            ViewTreeObserver viewTreeObserver = mImageItem.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageItem.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageItem.setImageBitmap(getBitmapFromUri(Uri.parse(image), mContext, mImageItem));
                }
            });

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


        public static Bitmap getBitmapFromUri(Uri uri, Context mContext, ImageView imageView){

            if (uri == null || uri.toString().isEmpty())
                return null;

            // Get the dimensions of the View
            int targetW = imageView.getWidth();
            int targetH = imageView.getHeight();

            InputStream input = null;
            try {
                input = mContext.getContentResolver().openInputStream(uri);

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, bmOptions);
                input.close();

                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;

                input = mContext.getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
                input.close();
                return bitmap;

            } catch (FileNotFoundException fne) {
                Log.e(LOG_TAG, mContext.getString(R.string.exception_image_load_failed), fne);
                return null;
            } catch (Exception e) {
                Log.e(LOG_TAG, mContext.getString(R.string.exception_image_load_failed), e);
                return null;
            } finally {
                try {
                    input.close();
                } catch (IOException ioe) {

                }
            }
        }




    @Override
    public void onLoaderReset(Loader loader) {

    }

    private int adjustInventory(Uri itemUri, int mQuantity) {
        if (mQuantity < 0) {
            return 0;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, mQuantity);
        int rowsUpdated = getContentResolver().update(itemUri, values, null, null);
        return rowsUpdated;
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

    /**
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
