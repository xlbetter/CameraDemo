package com.example.administrator.camerademo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity {

    //static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 2;
    static final int CODE_RESULT_REQUEST = 3;
    static final int REQUEST_PICK_IMAGE = 4;


    ImageView mImageView;
    Button button;
    String mCurrentPhotoPath;
    TextView textView;
    String path;
    File temp;

    Button select;
    //String image;
    //File temp1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imageView);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

        select = findViewById(R.id.select);

        requestPermission();
    }

    public void camera(View view) {
        // requestPermission();
        dispatchTakePictureIntent();
    }

    public void setSelect(View view) {
        final String items[] = {"拍照", "相册"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                dispatchTakePictureIntent();
                                break;
                            case 1:
                                pickPicture();
                                break;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                temp = photoFile;
            } catch (IOException ex) {
                Log.e("Create File", "Directory not created");
            }
            if (photoFile != null) {
                //textView.setText(photoFile.toString());
                path = photoFile.toString();
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                //grantUriPermission("com.example.administrator.camerademo", photoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //takePictureIntent.setData(photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //getApplicationContext().checkCallingPermission(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    protected void pickPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO:
                    galleryAddPic();
                    //textView.setText(path);
                    Uri tempUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", temp);
                    //File temp=new File(Environment.getExternalStoragePublicDirectory(),);
                    //File temp=new File(Environment.getExternalStorageDirectory(),image);
                    crop(tempUri);
                    break;

                case REQUEST_PICK_IMAGE:
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mCurrentPhotoPath = cursor.getString(columnIndex);
                    cursor.close();
                    //mCurrentPhotoPath = selectedImage.getPath();
                    //Uri newImage = FileProvider.getUriForFile(this, "com.example.android.fileprovider", newFile);
                    crop(selectedImage);
                    break;

                case CODE_RESULT_REQUEST:
                    //Bundle extras = data.getExtras();
                    //Bitmap imageBitmap = (Bitmap) extras.getParcelable("data");
                    //mImageView.setImageBitmap(imageBitmap);
                    textView.setText(mCurrentPhotoPath);
                    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                    mImageView.setImageBitmap(bitmap);
                    break;
            }


            /*if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                //Bundle extras = data.getExtras();
                //Bitmap imageBitmap = (Bitmap) extras.get("data");
                //mImageView.setImageBitmap(imageBitmap);

                galleryAddPic();
                textView.setText(path);
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", temp);
                //File temp=new File(Environment.getExternalStoragePublicDirectory(),);
                //File temp=new File(Environment.getExternalStorageDirectory(),image);
                crop(photoURI);
            } else if (requestCode == CODE_RESULT_REQUEST && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.getParcelable("data");
                mImageView.setImageBitmap(imageBitmap);

            }*/
        } else {
            return;
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        //image = imageFileName;
        /*File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;*/
        File image = null;

        boolean isAvailable = isExternalStorageWritable();
        if (isAvailable == false) {
            Log.e("External unavailable", "External unavailable");
        } else {
            //requestPermission();

            //file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageFileName);
            //File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraDemo");
            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.d("CameraDemo", "failed to create directory");
                    return null;
                }
            }
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            //temp1 = File.createTempFile(imageFileName, "crop.jpg", storageDir);
            /*if (!file.mkdirs()) {
                Log.e("Picture File", "Directory not created");
            }*/
            mCurrentPhotoPath = image.getAbsolutePath();
        }
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(mCurrentPhotoPath);
        //File crop=new File();
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //camera(button);

                return;
            }
        }
    }

    public void crop(Uri uri) {
        //requestPermission();
        Intent intent = new Intent("com.android.camera.action.CROP");
        //Intent intent=new Intent(Intent.ACTION_GET_CONTENT,null);
        intent.setDataAndType(uri, "image/*");

        intent.putExtra("crop", "true");


        intent.putExtra("scale", true);
        //intent.putExtra("circleCrop","false");
        intent.putExtra("return-data", false);
        //intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaveDetection", false);
        intent.putExtra("output", uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


        /*File photoFile = null;
        try {
            photoFile = createImageFile();
            //temp=photoFile;
        } catch (IOException ex) {
            Log.e("Create File", "Directory not created");
        }*/
        //Uri tempUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);

        //grantUriPermission(getPackageName(),tempUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        //intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        //intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        //intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, CODE_RESULT_REQUEST);
    }


}
