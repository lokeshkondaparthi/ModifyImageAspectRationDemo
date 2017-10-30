package com.dev.modifyimageaspectrationdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dev.modifyimageaspectrationdemo.blur_image.RationConstntBlurBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.MultipartTypedOutput;
import retrofit.mime.TypedFile;

import static android.os.Build.VERSION.SDK_INT;

public class SendMultiImagesActivity extends AppCompatActivity {

    public static final String RESIZE_BITMAP = "ResizeBitmap";
    private List<Bitmap> mResultBtmpProccess;
    List<File> files = new ArrayList<>();
    private static String Root_url="https://pcapi.pyar.com/api/";
    private String TEMP_PHOTO_FILE_NAME="temp.jpg";
    private File mFileTemp;
    private String mCameraPhotoPath;
    private Uri imageUri;
    private int FILECHOOSER_RESULTCODE=1;
    private ImageView mPreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload2);
        mIntiViews();
    }

    private void mIntiViews() {
        mResultBtmpProccess = new ArrayList<>();
        mPreview = (ImageView) findViewById(R.id.preview);
    }

    /* Show dialog to select image*/
    public void uploadFiles(View view) {
        mShowPicker();
    }

    public void  mShowPicker() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(SendMultiImagesActivity.this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                imageUri = Uri.fromFile(photoFile); // saving as private temp data
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
        }
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStorageDirectory();
        return File.createTempFile(imageFileName, ".png", storageDir);
    }

    public void uploadFilesToServer(View view) {
        MultipartTypedOutput multipartTypedOutput = new MultipartTypedOutput();
        try {
            multipartTypedOutput.addPart("originalImgBlob", new TypedFile("image/png", files.get(0)));
            multipartTypedOutput.addPart("img430Blog", new TypedFile("image/png;base64", files.get(0)));
            multipartTypedOutput.addPart("img200Blog", new TypedFile("image/png;base64", files.get(0)));
            multipartTypedOutput.addPart("img100Blog", new TypedFile("image/png;base64", files.get(0)));
            multipartTypedOutput.addPart("blurResponseBlob", new TypedFile("image/png;base64", files.get(0)));
            Log.d("UploadActivity", "postNewsFeed() returned: " + "" + files.get(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        doServereCall(multipartTypedOutput);

    }

    private void doServereCall(MultipartTypedOutput multipartTypedOutput) {
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(Root_url).build();
        DetailsAPI api = adapter.create(DetailsAPI.class);
        api.sendMultiImages("pIO9SqI5kFoiYwrh4yVkiQRRRR3RRRR3", "1", multipartTypedOutput, new Callback<JsonObject>() {
            @Override
            public void success(JsonObject o, Response response) {
                if(o != null)
                l("UploadActivity",o.toString());
                if(o != null)
                Toast.makeText(SendMultiImagesActivity.this, "success"+o.toString(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(SendMultiImagesActivity.this, "null "+response.getReason()+ " "+response.getStatus(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                l("UploadActivity",error.getMessage());
                Toast.makeText(SendMultiImagesActivity.this, "retro error! ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != FILECHOOSER_RESULTCODE) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null || data.getData() == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{imageUri};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
              Bitmap bitmpa  = getBitmapFromUri(results[0]);
            if(bitmpa.getHeight() >1900 || bitmpa.getWidth() >1900  ) {
                mCheckAndCreate3Images(bitmpa);         // Create 3 files
                mConvetListToFiles();
            }
            else if(bitmpa.getHeight() <400 || bitmpa.getWidth()<400) // Selected image is very small Not selectable
            Toast.makeText(this, "Oh ho! image is small !", Toast.LENGTH_SHORT).show();



            Toast.makeText(this, "bitmap null?"+(bitmpa == null)+ "h::"+bitmpa.getWidth()+" "+bitmpa.getHeight()
                    , Toast.LENGTH_SHORT).show();
            for (int i = 0; i < results.length; i++) {
                if(results[i] != null)
                l("SMIA","path is"+results[i].getPath());
            }
        } else if (SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE ) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {

                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? this.imageUri : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                            Toast.LENGTH_LONG).show();
                }
                l("SMIA","below version"+(new Uri[]{result}));
            }
        }
    }

    private void mConvetListToFiles() {
        for (int i = 0; i < mResultBtmpProccess.size(); i++) {
            mConvertToFile(mResultBtmpProccess.get(i),i);
        }
    }

    /* Create orginal ,and other 3 type images */
    private void mCheckAndCreate3Images(Bitmap bitmpa) {

        mResultBtmpProccess.add(0,getResizedBitmapWithWidth(bitmpa,1900));
        mResizeBitmap(bitmpa,430,430,1);
        mResizeBitmap(bitmpa,200,200,2);
        mResizeBitmap(bitmpa,100,100,3);
        mBlurImage(bitmpa,4);
    }



    private void mConvertToFile(Bitmap bitmap,int mFilePosition) {
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
            } else {
                mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
            }

            FileOutputStream out = new FileOutputStream(mFileTemp);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            files.add(mFilePosition,mFileTemp);

        } catch(Exception e){
            // some action
        }
    }



    public static void l(String key, String value) {
        Log.d(key,value);
    }

    Bitmap getBitmapFromUri(Uri uri)
    {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public Bitmap getResizedBitmapWithWidth(Bitmap unscaledBitmap,int wantedWitdth) {
        float aspectRatio = unscaledBitmap.getWidth() /
                (float) unscaledBitmap.getHeight();
        int width;
        int height;
        width = wantedWitdth;
        do {
            height = Math.round(width / aspectRatio);
            if(height >1900)
                width = width-10;
        } while (height>1900);
        Bitmap  scaledBitmap = Bitmap.createScaledBitmap(
                unscaledBitmap, width, height, false);
        Log.d(RESIZE_BITMAP, "width:: "+width+" , "+ scaledBitmap.getWidth()+
                "||||"+"height::"+unscaledBitmap.getHeight()+","+scaledBitmap.getHeight());
        mPreview.setImageBitmap(scaledBitmap);
        return scaledBitmap;
    }
    public Bitmap mResizeBitmap(Bitmap bm, int newWidth, int newHeight,int listPosition) {
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, newWidth, newHeight);
        Log.d(RESIZE_BITMAP, "mResizeBitmap: "+resizedBitmap.getHeight()+" width"+resizedBitmap.getWidth());
        mResultBtmpProccess.add(listPosition,resizedBitmap);
        return resizedBitmap;
    }



        /* Blur Bitmap*/
    private Bitmap mBlurImage(Bitmap bitmap,int position) {
        Bitmap mBlurBitmap = RationConstntBlurBuilder.blur(this, bitmap);
        mResultBtmpProccess.add(position,getResizedBitmapWithWidth(mBlurBitmap,1900));
        return mBlurBitmap;
    }



}
