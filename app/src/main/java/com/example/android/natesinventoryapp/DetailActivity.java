package com.example.android.natesinventoryapp;


import android.app.Activity;
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
import android.os.Build;
import android.os.Bundle;
import android.app.LoaderManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
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
    private static final String STATE_URI = "STATE_URI";
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int SEND_MAIL_REQUEST = 1;

    private Uri mCurrentItemUri;

    private TextView nameTextView, supplierTextView, quantityTextView, priceTextView, mTextView;
    private String mSupplierEmail = "test@gmail.com";
    private String mName = "Widget";
    private Button orderButton, deleteButton;
    private ImageButton incrementButton, decrementButton;
    private ImageView mImageView;
    private FloatingActionButton mFab;

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
        mImageView = (ImageView) findViewById(R.id.image);

        mFab = (FloatingActionButton) findViewById(R.id.fabDetail);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

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
            if (image != null) {
                Log.i(LOG_TAG, "Uri: " + mCurrentItemUri.toString());

                mTextView.setText(mCurrentItemUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mCurrentItemUri, mContext, mImageView));
            }
            //mImageView.setImageBitmap(getBitmapFromUri(Uri.parse(image), mContext, mImageView));

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

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mCurrentItemUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mCurrentItemUri.toString());

                mTextView.setText(mCurrentItemUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mCurrentItemUri, mContext, mImageView));
            }
        } else if (requestCode == SEND_MAIL_REQUEST && resultCode == Activity.RESULT_OK) {

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
                try {if(input != null){input.close();}
                } catch (IOException ioe) {

                }
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
                    mImageView.setImageBitmap(getBitmapFromUri(mCurrentItemUri, mContext, mImageView));
                }
            });
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

    private void sendEmail() {
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

            if (Build.VERSION.SDK_INT < 21) {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            } else {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }


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
