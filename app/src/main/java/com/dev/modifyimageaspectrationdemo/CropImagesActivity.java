package com.dev.modifyimageaspectrationdemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class CropImagesActivity extends AppCompatActivity {


        private static int RESULT_LOAD_IMAGE = 1;
        private ImageView ivImageView,ivScalledImage;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

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
                /* Sending bitmap to be cropped */
                createCropImageByHeight(BitmapFactory.decodeFile(picturePath));


            }


        }
        /* Based on width , it's cropped */
    private void createCropImageByWidth(Bitmap oldBitmap) {
        int nh = (int) ( oldBitmap.getHeight() * (512  / oldBitmap.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(oldBitmap, 512, nh, true);
        ivScalledImage.setImageBitmap(scaled);
        getDimensionsOfBitmap(scaled);
    }
    /* Based on height  it's cropped */
    private void createCropImageByHeight(Bitmap oldBitmap) {
        int nh = (int) ( oldBitmap.getWidth() * (1024  / oldBitmap.getHeight()) );
        Bitmap scaled = Bitmap.createScaledBitmap(oldBitmap, nh, 1024, true);
        ivScalledImage.setImageBitmap(scaled);
        getDimensionsOfBitmap(scaled);
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
}