package com.dev.modifyimageaspectrationdemo;


/*import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;*/


import com.google.gson.JsonObject;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.mime.MultipartTypedOutput;

public interface DetailsAPI {
         /*@Headers("Accept:application/json")
         @POST("mbrphotos/testimg")
         Call<String> uploadImages(@Body MultipartTypedOutput multipartTypedOutput);*/

/*    @Headers("Accept:application/json")
    @Multipart
    @POST("mbrphotos/testimg")
    Call<String> uploadImages2(@Part("img1") RequestBody file1,@Part("img2") RequestBody file2);*/

        @Headers("Accept:application/json")
        @POST("/mbrphotos/testimg")
        public void uploadToserver(@Body MultipartTypedOutput multipartTypedOutput, Callback<Object> callback);



        @Headers("Accept:application/json")
        @POST("/mbrphotos/prfImgIU/{memberId}/{actionType}")
        public void sendMultiImages(@Path("memberId") String memberId,
                                    @Path("actionType") String actionType,
                                    @Body MultipartTypedOutput multipartTypedOutput, Callback<JsonObject> callback);

/*
        @Headers("Accept:application/json")
        @POST("/mbrphotos/prfImgIU/{memberId}/{actionType}")
        public void sendMultiImages(@Path("memberId") String memberId,
                                    @Path("actionType") String actionType,
                                    @Body MultipartTypedOutput multipartTypedOutput, Callback<JsonObject> callback);*/


}