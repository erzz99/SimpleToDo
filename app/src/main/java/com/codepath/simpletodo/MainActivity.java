package com.codepath.simpletodo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.io.FileUtils.writeLines;
import static org.apache.commons.io.FileUtils.readLines;


public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;


    Button btnAdd;
    EditText etItem;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = findViewById(R.id.btnAdd);
        etItem = findViewById(R.id.etItem);
        rvItems = findViewById(R.id.rvItems);


        loadItems();

        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener() {

            @Override
            public void OnItemLongClicked(int position) {
                //delete item from the model
                items.remove(position);

                //notify adapter
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(),"Item was removed", Toast.LENGTH_SHORT).show();
                saveItems();

            }
        };

        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void OnItemClicked(int position) {
                Log.d("MainActivity", "Single click at position" + position);

                Intent i = new Intent(MainActivity.this, EditActivity.class);

                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);

                startActivityForResult(i, EDIT_TEXT_CODE );

            }
        };

        itemsAdapter = new ItemsAdapter(items, onLongClickListener,onClickListener );
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoItem = etItem.getText().toString();
                //add item
                items.add(todoItem);
                //notify adapter that item has been added
                itemsAdapter.notifyItemInserted(items.size() - 1);
                etItem.setText("");
                Toast.makeText(getApplicationContext(),"Item was added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

    //handle result of edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if((resultCode == RESULT_OK) && (requestCode == EDIT_TEXT_CODE)){
            //retrieve the updated text value
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);

            //extract original position
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);

            //update model at new position
            items.set(position, itemText);

            //notify adapter
            itemsAdapter.notifyItemChanged(position);

            //persist the changes
            saveItems();
            Toast.makeText(getApplicationContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "unknown call on Add ActivityResult");
        }

    }

    private File getDataFile() {
        return new File(getFilesDir(), "data.txt");
    }


    //this function will load every item by reading line of the data field
    private void loadItems() {
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        }catch (IOException e) {
            Log.e("MainActivity", "Error reading items", e );
            items = new ArrayList<>();
        }
    }

    //this function saves items by writing them into the data file
    private void saveItems() {
         try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e ("MainActivity", "Error writing items", e);
        }
    }
}
