package com.example.android.natesinventoryapp;

/**
 * Created by Hardkornate on 7/30/17.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import static com.example.android.natesinventoryapp.DetailActivity.getBitmapFromUri;
import static com.example.android.natesinventoryapp.R.id.item_sell_button;
import static com.example.android.natesinventoryapp.R.id.name;
import static com.example.android.natesinventoryapp.R.id.price;
import static com.example.android.natesinventoryapp.R.id.quantity;
import static com.example.android.natesinventoryapp.data.InventoryContract.DEC_FORMAT;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry.COLUMN_ITEM_IMAGE;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry.COLUMN_ITEM_NAME;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry.CONTENT_URI;
import static com.example.android.natesinventoryapp.data.InventoryContract.InventoryEntry._ID;

class InventoryCursorAdapter extends CursorAdapter {

    private static Context mContext;

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(name);
        TextView priceTextView = (TextView) view.findViewById(price);
        final TextView quantityTextView = (TextView) view.findViewById(quantity);
        ImageButton mSellButton = (ImageButton) view.findViewById(item_sell_button);


        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_IMAGE);
        final Uri uri = ContentUris.withAppendedId(CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow(_ID)));


        // Read the pet attributes from the Cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        Double itemPrice = cursor.getDouble(priceColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);
        final String image = cursor.getString(imageColumnIndex);
        Uri imageUri = Uri.withAppendedPath(uri, image);
        mSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemQuantity > 0) {
                    int newItemQuantity = itemQuantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_ITEM_QUANTITY, newItemQuantity);
                    context.getContentResolver().update(uri, values, null, null);

                } else{
                    Toast.makeText(context,R.string.no_product,Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        String mQuantity = String.valueOf(itemQuantity);
        DecimalFormat dec = new DecimalFormat(DEC_FORMAT);
        String mPrice = dec.format(itemPrice);
        priceTextView.setText(mPrice);
        quantityTextView.setText(mQuantity);
        mSellButton.setImageBitmap(getBitmapFromUri(imageUri, mContext, mSellButton));
    }
}
