package com.dev.modifyimageaspectrationdemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UploadImagesActivity extends AppCompatActivity {


        private static int RESULT_LOAD_IMAGE = 1;
        private ImageView ivImageView,ivScalledImage;
    private File file;
    List<File> files = new ArrayList<>();
    private static String Root_url="https://pcapi.pyar.com/api/";

    private String mCurrentPhotoPath;
    private int REQUEST_TAKE_PHOTO=105;
    private Uri photoURI;

    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.upload_content_main);

            ivImageView = (ImageView) findViewById(R.id.content_main_image_iv);
            ivScalledImage = (ImageView) findViewById(R.id.content_main_scalled_image_iv);

            Button buttonLoadImage = (Button) findViewById(R.id.content_main_change_sz_bt);
            buttonLoadImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
            });
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                try {
                    cursor.moveToFirst();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                 String picturePath = cursor.getString(columnIndex);
                cursor.close();

                ivImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

                getDimentionsOfImage(picturePath);
                createScalledBitMap(BitmapFactory.decodeFile(picturePath));


            }

           /* if (requestCode == REQUEST_TAKE_PHOTO) {
                if (data == null) {
                    Toast.makeText(this, "data is not null", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "data is null", Toast.LENGTH_SHORT).show();
                }

                Retrofit retrofit = getStringAdapter(this,"");
                DetailsAPI detailsAPI = retrofit.create(DetailsAPI.class);

                File  file = new File(mCurrentPhotoPath);
                RequestBody file1 = RequestBody.create(MediaType.parse("image/jpeg"), file);
                RequestBody file2 = RequestBody.create(MediaType.parse("image/jpeg"), file);
                Call<String> stringCall = detailsAPI.uploadImages2(file1,file2);
                stringCall.enqueue(new retrofit2.Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                        Toast.makeText(UploadImagesActivity.this, "Success"+response.body(), Toast.LENGTH_SHORT).show();
                        Log.d("UploadImagesActivity", "success: "+response.body());
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(UploadImagesActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                        Log.d("UploadImagesActivity", "failure: "+t.getMessage());

                    }
                });

            }*/


        }

    private void createScalledBitMap(Bitmap oldBitmap) {
        Bitmap resizedImage = Bitmap.createScaledBitmap(oldBitmap,500,500, true);
        ivScalledImage.setImageBitmap(resizedImage);
        getDimensionsOfBitmap(resizedImage);
    }

    private void getDimentionsOfImage(String picturePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(picturePath, options);
        int width = options.outWidth;
        int height = options.outHeight;
        Log.d("ImageGalleryDemo", "getDimentionsOfImage: width::"+width+" height"+height);
        //If you want, the MIME type will also be decoded (if possible)
        String type = options.outMimeType;
    }
    private void getDimensionsOfBitmap(Bitmap bitmap) {
        int imageHeight = bitmap.getHeight();
        int imageWidth = bitmap.getWidth();
        Log.d("ImageGalleryDemo","scalled image dimensions:: width"+imageWidth+"  height::"+imageHeight);
    }

    public void uploadToServer(View view) {
        files.clear();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        /*convertToFile(((BitmapDrawable)ivImageView.getDrawable()).getBitmap(),0);
        convertToFile(((BitmapDrawable)ivScalledImage.getDrawable()).getBitmap(),1);*/

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile  = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {

            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                 photoURI = FileProvider.getUriForFile(this,
                        "com.dev.modifyimageaspectrationdemo",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }


/*
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(Root_url).build();

        DetailsAPI api = adapter.create(DetailsAPI.class);*/


/*
        MultipartTypedOutput multipartTypedOutput = new MultipartTypedOutput();
        multipartTypedOutput.addPart("img1",new TypedFile("image/jpeg",files.get(0)));
        multipartTypedOutput.addPart("img2",new TypedFile("image/jpeg",files.get(1)));*/



    }

    private void convertToFile(Bitmap bitmap, int i) {
        file = new File(this.getCacheDir(), "file" + i);
        try {
            boolean isCreated  = file.createNewFile();
            if(isCreated)
            Toast.makeText(this,"File created successfully!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "File is not created successfully!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
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
/*
    public static Retrofit getStringAdapter(Context ctx, String serverUrl) {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Root_url+serverUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }*/

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



}