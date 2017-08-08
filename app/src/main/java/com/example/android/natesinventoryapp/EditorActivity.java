package com.example.android.natesinventoryapp;

/**
 * Created by Hardkornate on 7/30/17.
 */

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.natesinventoryapp.data.Utils;

import java.text.DecimalFormat;
import java.util.Objects;

import static com.example.android.natesinventoryapp.data.InventoryProvider.isValidEmail;
import static com.example.android.natesinventoryapp.data.Utils.getBitmapFromUri;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = InventoryEntry.class.getSimpleName();
    /**
     * Code for image request
     */
    private static final int IMAGE_REQUEST_CODE = 0;
    /**
     * Image state URI
     */
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";
    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;
    /**
     * Context for the activity
     */
    private final Context mContext = this;
    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private Uri mCurrentItemUri;
    /**
     * EditText field to enter the items name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the items supplier
     */
    private EditText mSupplierEditText;
    /**
     * EditText field to enter the items price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the item quantity
     */
    private EditText mQuantityEditText;
    /**
     * ImaveView for the Item Image
     */
    private ImageView mImageItem;
    /**
     * URI for the product image
     */
    private Uri mImageUri;
    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
    private boolean mItemHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };
    /**
     * Button for adding image
     */
    private Button mButtonAddImage;

    public EditorActivity() {
        mCurrentItemUri = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Intent intent = getIntent();
        //Bundle bundle = intent.getExtras();

        //if (bundle != null) {
        //    mCurrentItemUri = Uri.parse(bundle.getString("URI"));
        //    mImageUri = Uri.parse(bundle.getString("IMAGE URI"));
        //}

        getLoaderManager().getLoader(EXISTING_ITEM_LOADER);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mSupplierEditText = (EditText) findViewById(R.id.edit_item_supplier_email);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_initial_quantity);
        mButtonAddImage = (Button) findViewById(R.id.add_image_button);
        mImageItem = (ImageView) findViewById(R.id.item_image);

        if (mImageUri == null) {
            mButtonAddImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonImageClick();
                }
            });
        } else {
            mButtonAddImage.setVisibility(View.GONE);
            mImageItem.setVisibility(View.VISIBLE);
        }
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        // Setup FAB to save EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View view){
                saveItem();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString("").equals(STATE_IMAGE_URI)) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            ViewTreeObserver viewTreeObserver = mImageItem.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageItem.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageItem.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mImageItem));
                }
            });
        }
    }


    /**
     * Method to select a picture from device's media storage
     */
    private void buttonImageClick() {
        Intent intent;

        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.action_select_picture)), IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && (resultCode == RESULT_OK)) {
            try {
                mImageUri = data.getData();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                mImageItem.setImageBitmap(Utils.getBitmapFromUri(mImageUri, mContext, mImageItem));
                saveItem();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Get user input from editor and save pet into database.
     */
    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        //  String nameString = mNameEditText.getText().toString().trim();
        //  String supplierString = mSupplierEditText.getText().toString().trim();
        // String priceString = mPriceEditText.getText().toString().trim();
        // String quantityString = mQuantityEditText.getText().toString().trim();
        // String mImagePath = mImageUri.toString().trim();

        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        // if (mCurrentItemUri == null &&
        //       TextUtils.isEmpty(nameString) && TextUtils.isEmpty(supplierString) &&
        //      TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) && mImageItem.getDrawable() == null) {
            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
        //return;
        //} else {
        ContentValues values = new ContentValues();
        if (!mNameEditText.getText().toString().equals("")) {
            String nameString = mNameEditText.getText().toString().trim();
            values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        } else {
            Toast.makeText(this, getString(R.string.name_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        if (Objects.equals(mSupplierEditText.getText().toString(), "")) {
            Toast.makeText(this, getString(R.string.supplier_failed), Toast.LENGTH_SHORT).show();
            return;
        } else if (!isValidEmail(mSupplierEditText.getText().toString())) {
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return;
        } else {
            String supplierString = mSupplierEditText.getText().toString().trim();
            values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, supplierString);
        }
        if (Objects.equals(mPriceEditText.getText().toString(), "")) {
            Toast.makeText(this, getString(R.string.price_failed), Toast.LENGTH_SHORT).show();
            return;
        } else {
            String priceString = mPriceEditText.getText().toString().trim();
            values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceString);
        }
        if (Objects.equals(mQuantityEditText.getText().toString(), "")) {
            Toast.makeText(this, getString(R.string.quantity_failed), Toast.LENGTH_SHORT).show();
            return;
        } else {
            String quantityString = mQuantityEditText.getText().toString().trim();
            values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityString);
        }
        if (mImageUri != null) {
            String mImagePath = mImageUri.toString().trim();
            values.put(InventoryEntry.COLUMN_ITEM_IMAGE, mImagePath);
        } else {
            Toast.makeText(this, getString(R.string.image_failed), Toast.LENGTH_SHORT).show();
            return;
        }
            mCurrentItemUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        //}
            // Show a toast message depending on whether or not the insertion was successful.
            if (mCurrentItemUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null) {
            outState.putString(STATE_IMAGE_URI, mImageUri.toString());
        }
    }
    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
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
    public CursorLoader onCreateLoader(int id, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
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
                mCurrentItemUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
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
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            DecimalFormat dec = new DecimalFormat("$####.00");
            String mPrice = dec.format(price);
            String mQuantity = String.valueOf(quantity);
            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mSupplierEditText.setText(supplier);
            mPriceEditText.setText(mPrice);
            mQuantityEditText.setText(mQuantity);

            if (image != null) {
                mImageUri = Uri.parse(image);
                mImageItem.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mImageItem));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
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
}