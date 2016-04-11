package br.com.fiap.beerscatalog;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.fiap.beerscatalog.models.Beer;

public class FormActivity extends AppCompatActivity {

    private static final int THUMBNAIL_SIZE = 160;
    public static final int REQUEST_CODE_SUBMIT = 2;
    private static final int REQUEST_CODE_PICTURE = 1;

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    public static final String BUNDLE_EXTRA_BEERID_KEY = "BUNDLE_EXTRA_KEY";
    private static final String BUNDLE_SAVEDINSTANCE_URI_KEY = "BUNDLE_SAVEDINSTANCE_URI_KEY";

    private TextView formAction;
    private ImageView imageView;

    private ImageView takePic;
    private EditText nameEdit;
    private EditText breweryEdit;
    private EditText styleEdit;
    private EditText abvEdit;

    private Button save;
    private Button remove;

    private String outputFileUri;
    private Beer beer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        formAction = (TextView) findViewById(R.id.formAction);
        imageView = (ImageView) findViewById(R.id.imageView);
        takePic = (ImageView) findViewById(R.id.takePic);

        nameEdit = (EditText) findViewById(R.id.nameEdit);
        breweryEdit = (EditText) findViewById(R.id.breweryEdit);
        styleEdit = (EditText) findViewById(R.id.styleEdit);
        abvEdit = (EditText) findViewById(R.id.abvEdit);

        save = (Button) findViewById(R.id.save);
        remove = (Button) findViewById(R.id.remove);

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(FormActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(FormActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(FormActivity.this);
                        builder.setTitle(R.string.title_write_permission);

                        builder.setMessage(R.string.message_write_permission);
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                requestWriteExternalStoragePermission();

                            }
                        });

                        android.app.AlertDialog dialog = builder.create();
                        dialog.show();

                    } else {

                        requestWriteExternalStoragePermission();

                    }
                }else{

                    chooseIntent();

                }
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras == null){
            remove.setVisibility(View.GONE);
            formAction.setText(R.string.title_add);
        }else{
            remove.setVisibility(View.VISIBLE);
            formAction.setText(R.string.title_edit);

            long beerId = extras.getLong(BUNDLE_EXTRA_BEERID_KEY);
            beer = Beer.get(beerId);

            nameEdit.setText(beer.name);
            breweryEdit.setText(beer.brewery);
            styleEdit.setText(beer.style);
            abvEdit.setText(String.valueOf(beer.abv));

            if (beer.imageURL != null) {
                Bitmap image = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(beer.imageURL), THUMBNAIL_SIZE, THUMBNAIL_SIZE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(image);
            }
        }
    }

    private void chooseIntent(){
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);

        File outputFile = getOutputMediaFile();
        outputFileUri = outputFile.getAbsolutePath();

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));

        Intent chooserIntent = Intent.createChooser(pickIntent, getString(R.string.label_intent_chooser));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { takePhotoIntent });

        startActivityForResult(chooserIntent, REQUEST_CODE_PICTURE);
    }

    private void requestWriteExternalStoragePermission(){

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    chooseIntent();

                } else {

                    Toast.makeText(this, R.string.message_write_permission_denied,
                            Toast.LENGTH_LONG).show();
                    finish();

                }
                return;
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICTURE &&
                resultCode == RESULT_OK) {

            Bitmap bitmap = null;

            if (data != null) {
                try {
                    outputFileUri = getPath(this, data.getData());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            try{
                bitmap = BitmapFactory.decodeFile(outputFileUri);
            }catch(Exception e){
                e.printStackTrace();
            }

            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);
        }else{
            outputFileUri = null;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_SAVEDINSTANCE_URI_KEY, outputFileUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        outputFileUri = savedInstanceState.getString(BUNDLE_SAVEDINSTANCE_URI_KEY);
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    public static String getPath(final Context context, final Uri uri)
    {
        final boolean isKitKatOrAbove = Build.VERSION.SDK_INT >=  Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKatOrAbove && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public void submit(View v){
        if(beer == null){
            // create
            beer = new Beer();
            beer.date = System.currentTimeMillis() / 1000;
        }

        beer.name = nameEdit.getText().toString();
        beer.brewery = breweryEdit.getText().toString();
        beer.style = styleEdit.getText().toString();
        beer.abv = Double.valueOf(abvEdit.getText().toString());

        if(outputFileUri != null)
            beer.imageURL = new File(outputFileUri).getAbsolutePath();

        beer.save();

        setResult(RESULT_OK);
        finish();
    }

    public void remove(View v){

        new AlertDialog.Builder(FormActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.form_remove_title))
                .setMessage(getString(R.string.form_remove_message))
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        beer.delete();
                        setResult(RESULT_OK);
                        finish();

                    }

                })
                .setNegativeButton(getString(android.R.string.no), null)
                .show();

    }
}
