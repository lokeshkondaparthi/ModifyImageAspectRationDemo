package com.dev.modifyimageaspectrationdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dev.modifyimageaspectrationdemo.blur_image.RationConstntBlurBuilder;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.FormBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static android.os.Build.VERSION.SDK_INT;

public class SendMultiImagesActivity2 extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST= 101;
    private Bitmap mBitmap;
    private List<Bitmap> bitmapArray,mResultBtmpProccess;
    List<String> fileNames = new ArrayList<String>();
    List<File> files = new ArrayList<>();
    private File file;
    private static String Root_url="https://pcapi.pyar.com/api/";
    private String TEMP_PHOTO_FILE_NAME="temp.jpg";
    private File mFileTemp;
    private String mCameraPhotoPath;
    private Uri imageUri;
    private int FILECHOOSER_RESULTCODE=1;
    private int scaleSize=1024;
    private ImageView mPreview;
    private List<String> mBase64Images  =null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload2);
        mIntiViews();
    }

    private void mIntiViews() {
        bitmapArray = new ArrayList<>();
        mResultBtmpProccess = new ArrayList<>();
        mPreview = (ImageView) findViewById(R.id.preview);
        mBase64Images = new ArrayList<>();
    }

    /* Show dialog to select image*/
    public void uploadFiles(View view) {
        mShowPicker();
    }

    public void  mShowPicker() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(SendMultiImagesActivity2.this.getPackageManager()) != null) {
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
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return File.createTempFile(imageFileName, ".png", storageDir);
    }

    public void uploadFilesToServer(View view) {


        doServereCall();

    }

    private void doServereCall() {
        Gson gson = new Gson();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Root_url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        ServiceOperations serviceOperations = retrofit.create(ServiceOperations.class);

        // Form body builder
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        bodyBuilder.add("originalImgBlob",mBase64Images.get(0));
        bodyBuilder.add("img430Blog",mBase64Images.get(1));
        bodyBuilder.add("img200Blog",mBase64Images.get(2));
        bodyBuilder.add("img100Blog",mBase64Images.get(3));
        bodyBuilder.add("blurResponseBlob",mBase64Images.get(4));   // Form body closed
        Call<String> call = serviceOperations.sendMultiImages("pIO9SqI5kFoiYwrh4yVkiQRRRR3RRRR3", "1",bodyBuilder.build());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body() != null)
                    l("UploadActivity",response.body());
                if(response.body() != null)
                    Toast.makeText(SendMultiImagesActivity2.this, "success"+response.body(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(SendMultiImagesActivity2.this, "null "+response.message()+ " "+response.isSuccessful(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(SendMultiImagesActivity2.this, "error "+t.getMessage()+" "+t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    l("UploadActivity","error "+t.getMessage());
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
            Toast.makeText(this, "path is ::"+results[0].getPath(), Toast.LENGTH_SHORT).show();
              Bitmap bitmpa  = getBitmapFromUri(results[0]);

            /*if(bitmpa.getHeight() >1900 || bitmpa.getWidth() >1900  )
               bitmpa =  mResizeBtmpAspectRatio(bitmpa);
            else if(bitmpa.getHeight() <400 || bitmpa.getWidth()<400)
            Toast.makeText(this, "Oh ho! image is small !", Toast.LENGTH_SHORT).show();*/



            if(bitmpa.getHeight() >1900 || bitmpa.getWidth() >1900  ) {
                mCheckAndCreate3Images(bitmpa);         // Create 3 files
                mConvetListToFiles();
                for (int i = 0; i < files.size(); i++) {
                    mEncodeFilesBase64(files.get(i).getPath(),i);
                }
            }
            else if(bitmpa.getHeight() <400 || bitmpa.getWidth()<400) // Selected image is very small Not selectable
            Toast.makeText(this, "Oh ho! image is small !", Toast.LENGTH_SHORT).show();

            Log.d("SendMultiImagesActivity", "onActivityResult: "+results[0]);

            /*mPreview.setImageBitmap(bitmpa);*/
            mPreview.setImageURI(results[0]);
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
        mResultBtmpProccess.add(0,bitmpa);
        mResizeBitmap(bitmpa,430,430,1);
        mResizeBitmap(bitmpa,200,200,2);
        mResizeBitmap(bitmpa,100,100,3);
        mBlurImage(bitmpa,4);
    }

    public void mDecodeBitmap() {

    }

    private void mConvertToFile(Bitmap bitmap,int mFilePosition) {
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME+mFilePosition);
            } else {
                mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME+mFilePosition);
            }

            FileOutputStream out = new FileOutputStream(mFileTemp);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            files.add(mFilePosition,mFileTemp);
                                    /*  if (Environment.MEDIA_MOUNTED.equals(state)) {
                                          mImageCaptureUri = FileProvider.getUriForFile(InstagLoginActivity.this,"com.pyarinc.pyar.provider",mFileTemp);
                                      } else {
                                    *//* The solution is taken from here: http://stackoverflow.com/questions/10042695/how-to-get-camera-result-as-a-uri-in-data-folder *//*
                                          mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
                                      }*/
        } catch(Exception e){
            // some action
        }
    }

    private void convertToFile(Bitmap bitmap, int i) {
        file = new File(this.getCacheDir(), "file" + i);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            files.add(i, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    public Bitmap mResizeBitmap(Bitmap bm, int newWidth, int newHeight,int listPosition) {
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, newWidth, newHeight);
        mResultBtmpProccess.add(listPosition,resizedBitmap);
        return resizedBitmap;
    }

        /* Without selection*/
        public Bitmap mResizeBtmpAspectRatio(Bitmap bitmap) {
            Bitmap resizedBitmap = null;
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();
            int newWidth = -1;
            int newHeight = -1;
            float multFactor = -1.0F;
            if(originalHeight > originalWidth) {
                newHeight = scaleSize ;
                multFactor = (float) originalWidth/(float) originalHeight;
                newWidth = (int) (newHeight*multFactor);
            } else if(originalWidth > originalHeight) {
                newWidth = scaleSize ;
                multFactor = (float) originalHeight/ (float)originalWidth;
                newHeight = (int) (newWidth*multFactor);
            } else if(originalHeight == originalWidth) {
                newHeight = scaleSize ;
                newWidth = scaleSize ;
            }
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            return resizedBitmap;
        }

        /* With selection Note: bitmpa Height width will be above 1900*/
        public Bitmap mResizeBtmpAspectRatioWithSelection(Bitmap bitmap) {
            Bitmap resizedBitmap = null;
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();
            int newWidth = -1;
            int newHeight = -1;
            float multFactor = -1.0F;
            int x=0,y=0,xTemp=0,yTemp=0;
            if(originalHeight > originalWidth) {
                newHeight = scaleSize ;
                multFactor = (float) originalWidth/(float) originalHeight;
                newWidth = (int) (newHeight*multFactor);
            } else if(originalWidth > originalHeight) {
                newWidth = scaleSize ;
                multFactor = (float) originalHeight/ (float)originalWidth;
                newHeight = (int) (newWidth*multFactor);
            } else if(originalHeight == originalWidth) {
                newHeight = scaleSize ;
                newWidth = scaleSize ;
            }




           /* if (originalHeight > 1900) {
                xTemp = originalHeight-1900;
                if(originalWidth > 1900)
                yTemp = originalWidth -1900;
                else
                    yTemp = 2;
            }
            if (originalWidth > 1900) {
                yTemp = originalWidth -1900;
                if(originalHeight > 1900)
                    xTemp = originalHeight -1900;
                else
                    xTemp = 2;
            }*/

            if (originalWidth > originalHeight) {
                if (originalWidth > 1900) {
                    xTemp = originalWidth - 1900;
                } else {
                    xTemp = 2;
                }
            } else {
                if (originalHeight > 1900) {
                    yTemp = originalHeight - 1900;
                } else {
                    yTemp = 2;
                }
            }
            if(xTemp != 2)
//                resizedBitmap = Bitmap.createBitmap(bitmap,xTemp/2,1,originalWidth-(xTemp/2),originalHeight-1);
            resizedBitmap = Bitmap.createBitmap(bitmap,1,1,originalWidth-900,originalHeight-900);

            if(yTemp != 2)
                resizedBitmap = Bitmap.createBitmap(bitmap,1,yTemp/2,newWidth,newHeight-(yTemp/2));

            /*resizedBitmap = Bitmap.createBitmap(bitmap,xTemp/2,yTemp/2,newWidth,newHeight);*/

            return resizedBitmap;
        }

        /* Blur Bitmap*/
    private Bitmap mBlurImage(Bitmap bitmap,int position) {
        Bitmap mBlurBitmap = RationConstntBlurBuilder.blur(this, bitmap);
        mResultBtmpProccess.add(position,mBlurBitmap);
        return mBlurBitmap;
    }

    private String mEncodeFilesBase64(String path,int positionFile)
    {
        File imagefile = new File(path);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(imagefile);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        //Base64.de
        mBase64Images.add(positionFile,encImage);
        return encImage;

    }

}
