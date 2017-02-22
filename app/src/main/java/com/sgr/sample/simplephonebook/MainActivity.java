package com.sgr.sample.simplephonebook;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, SearchView.OnQueryTextListener{

    private String id, name, phone, image_uri;
    private byte[] contactImage = null;
    private Bitmap bitmap;
    private int queryLength;
    private List<ContactItem> contactItems;

    private AdapterContact adapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private ListView listViewContact;

    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Perform some action here", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        listViewContact = (ListView) findViewById(R.id.listViewContact);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        searchView = (SearchView) findViewById(R.id.searchView);

        searchView.setOnQueryTextFocusChangeListener(this);
        searchView.setOnQueryTextListener(this);
        searchView.requestFocus();

        checkPermissionAndLoadContacts();
    }

    private void readContacts() {
        contactItems = new ArrayList<>();
        ContentResolver cr =  getApplicationContext().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                ContactItem item = new ContactItem();
                id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                image_uri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id},
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
                    while (pCur.moveToNext()) {
                        phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phone = phone.replaceAll("\\s+", "");
                        phone = phone.replaceAll("[^0-9]", "");
                    }
                    pCur.close();
                }
                if (image_uri != null) {
                    try {
                        bitmap = MediaStore.Images.Media
                                .getBitmap(getApplicationContext().getContentResolver(),
                                        Uri.parse(image_uri));
                        contactImage = getImageBytes(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    contactImage = null;
                }
                item.setId(id);
                item.setName(name);
                item.setContactImage(contactImage);
                item.setPhone(phone);
                contactItems.add(item);
            }
        }
    }

    private byte[] getImageBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            searchView.setQuery("", false);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        queryLength = newText.length();
        adapter.getFilter().filter(newText);
        return false;
    }


    public class ContactInfo extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            listViewContact.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            readContacts();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            listViewContact.setVisibility(View.VISIBLE);
            setListAdapter();
        }
    }

    private void setListAdapter() {
        adapter = new AdapterContact(getApplicationContext(), contactItems);
        listViewContact.setAdapter(adapter);
    }

    private void checkPermissionAndLoadContacts(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,  Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(getApplicationContext(), "Contact Read permission denied! Please grant Contact Read permission from seting to access phone book", Toast.LENGTH_LONG).show();
                Log.d("PHONEBOOK_SAMPLE", "Contact Read permission denied! Please grant Contact Read permission from seting to access phone book");
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }else{
            new ContactInfo().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new ContactInfo().execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Contact Read permission not granted yet! Please grant Contact Read permission to access phone book", Toast.LENGTH_LONG).show();
                    Log.d("PHONEBOOK_SAMPLE", "Contact Read permission not granted yet! Please grant Contact Read permission to access phone book");
                }
                return;
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
