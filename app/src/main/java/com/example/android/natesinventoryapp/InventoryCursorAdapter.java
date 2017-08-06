package com.example.android.natesinventoryapp;

/**
 * Created by Hardkornate on 7/30/17.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.natesinventoryapp.data.Utils;

import java.text.DecimalFormat;

import static com.example.android.natesinventoryapp.R.id.item_image;
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

    private final Context mContext;

    InventoryCursorAdapter(Context context) {
        super(context, null, 0);
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
        Button mSellButton = (Button) view.findViewById(item_sell_button);
        ImageView itemImageView = (ImageView) view.findViewById(item_image);


        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_IMAGE);
        final Uri uri = ContentUris.withAppendedId(CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow(_ID)));


        // Read the item attributes from the Cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        Double itemPrice = cursor.getDouble(priceColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);
        String image = cursor.getString(imageColumnIndex);
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
        if (image != null) {
            itemImageView.setImageBitmap(Utils.getBitmapFromUri(Uri.parse(image), mContext, itemImageView));
        }
    }
}
