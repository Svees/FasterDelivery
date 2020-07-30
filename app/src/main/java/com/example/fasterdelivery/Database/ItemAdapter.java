package com.example.fasterdelivery.Database;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fasterdelivery.R;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private SQLiteDatabase mDatabase;

    public ItemAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView numberText;
        ImageView imageView;
        TextView count;

        public ItemViewHolder(View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.tv_address);
            numberText = itemView.findViewById(R.id.tv_number);
            imageView = itemView.findViewById(R.id.imageButtonGo);
            count = itemView.findViewById(R.id.textViewCount);

        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_location, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        final String name = mCursor.getString(mCursor.getColumnIndex(ItemContract.AddressEntry.COLUMN_NAME));
        String number = mCursor.getString(mCursor.getColumnIndex(ItemContract.AddressEntry.COLUMN_NUMBER));
        long id = mCursor.getLong(mCursor.getColumnIndex(ItemContract.AddressEntry._ID));

        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        mDatabase = dbHelper.getWritableDatabase();


        holder.nameText.setText(name);
        holder.numberText.setText(String.valueOf(number));
        holder.itemView.setTag(id);
        holder.count.setText("");

        holder.imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //Toast.makeText(mContext,name,Toast.LENGTH_SHORT).show();

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+ name);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                activity.startActivity(mapIntent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }
}