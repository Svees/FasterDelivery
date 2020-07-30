package com.example.fasterdelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fasterdelivery.Database.DatabaseHelper;
import com.example.fasterdelivery.Database.ItemAdapter;
import com.example.fasterdelivery.Database.ItemContract;


public class HomeActivity extends AppCompatActivity {
    private SQLiteDatabase mDatabase;
    private ItemAdapter mAdapter;
    private EditText mEditTextName;
    private EditText mEditTextNumber;

    ImageView mPreviewIv;


    public static final String PREFS_NAME = "MySettingsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mPreviewIv = findViewById(R.id.imageIv);


        DatabaseHelper dbHelper = new DatabaseHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ItemAdapter(this, getAllItems());
        recyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                removeItem((long)viewHolder.itemView.getTag());
            }

        }).attachToRecyclerView(recyclerView);

        mEditTextName = findViewById(R.id.edittext_name);
        mEditTextNumber = findViewById(R.id.edittext_number);

        ImageButton buttonAdd = findViewById(R.id.button_add);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        hideNavigation();
    }

    private void addItem() {

        String name = mEditTextName.getText().toString();
        String number = mEditTextNumber.getText().toString();
        ContentValues cv = new ContentValues();
        cv.put(ItemContract.AddressEntry.COLUMN_NAME, name);
        cv.put(ItemContract.AddressEntry.COLUMN_NUMBER, number);

        if (mEditTextName.getText().toString().trim().length() != 0 && mEditTextNumber.getText().toString().trim().length() != 0){
            mDatabase.insert(ItemContract.AddressEntry.TABLE_NAME, null, cv);
            mAdapter.swapCursor(getAllItems());

            mEditTextName.getText().clear();
            mEditTextNumber.getText().clear();
            closeKeyboard();
        }

    }

    private void removeItem(long id) {
        mDatabase.delete(ItemContract.AddressEntry.TABLE_NAME, ItemContract.AddressEntry._ID + "=" + id,null);
        mAdapter.swapCursor(getAllItems());
    }
    private Cursor getAllItems() {
        return mDatabase.query(
                ItemContract.AddressEntry.TABLE_NAME,
                null,
                null,
                null,
                ItemContract.AddressEntry.COLUMN_NAME,
                null,
                ItemContract.AddressEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigation();
    }

    public void hideNavigation(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public void goMaps(View view) {


            Intent gomaps = new Intent(this,MapsActivity.class);
            startActivity(gomaps);

    }


    public void settings(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alertdialog_custom_view,null);

        // Specify alert dialog is not cancelable/not ignorable
        builder.setCancelable(false);

        // Set the custom layout as alert dialog view
        builder.setView(dialogView);
        Button btn_save = dialogView.findViewById(R.id.btb_save);
        final EditText et_name =  dialogView.findViewById(R.id.et_name);

        // Create the alert dialog
        final AlertDialog dialog = builder.create();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences weight_settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor smstext_editor = weight_settings.edit();

                String text = et_name.getText().toString();
                smstext_editor.putString("Text", text);
                smstext_editor.apply();

                dialog.cancel();

                Toast.makeText(getApplication(), "Saved", Toast.LENGTH_SHORT).show();
                hideNavigation();
            }
        });
        dialog.show();
    }
}
