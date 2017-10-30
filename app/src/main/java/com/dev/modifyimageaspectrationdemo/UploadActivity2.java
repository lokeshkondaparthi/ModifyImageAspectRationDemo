package com.dev.modifyimageaspectrationdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.MultipartTypedOutput;
import retrofit.mime.TypedFile;

public class UploadActivity2 extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST= 101;
    private Bitmap mBitmap;
    private List<Bitmap> bitmapArray;
    List<String> fileNames = new ArrayList<String>();
    List<File> files = new ArrayList<>();
    private File file;
    private static String Root_url="https://pcapi.pyar.com/api/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload2);
        mIntiViews();
    }

    private void mIntiViews() {
        bitmapArray = new ArrayList<>();
    }

    public void uploadFiles(View view) {
        Intent intent = new Intent();
        intent.setType("image/jpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void uploadFilesToServer(View view) {
        MultipartTypedOutput multipartTypedOutput = new MultipartTypedOutput();
        convertToFile(bitmapArray.get(0),0);
        try {
            multipartTypedOutput.addPart("img1", new TypedFile("image/jpeg", files.get(0)));
            multipartTypedOutput.addPart("img2", new TypedFile("image/jpeg", files.get(0)));
            Log.d("UploadActivity", "postNewsFeed() returned: " + "" + file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        doServereCall(multipartTypedOutput);

    }

    private void doServereCall(MultipartTypedOutput multipartTypedOutput) {
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(Root_url).build();
        DetailsAPI api = adapter.create(DetailsAPI.class);
        api.uploadToserver(multipartTypedOutput, new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                l("UploadActivity",o.toString());
                Toast.makeText(UploadActivity2.this, "success"+o.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                l("UploadActivity",error.getMessage());
                Toast.makeText(UploadActivity2.this, "retro error! ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    bitmapArray.add(mBitmap);
                    fileNames.add(System.currentTimeMillis() + ".jpeg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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


}
