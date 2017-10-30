package com.dev.modifyimageaspectrationdemo;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by user2 on 10/27/2017.
 */

public  interface ServiceOperations {

    @POST("mbrphotos/prfImgIU/{memberId}/{actionType}")
    Call<String> sendMultiImages(@Path("memberId") String memberId,
                                         @Path("actionType") String actionType,
                                         @Body RequestBody multipartTypedOutput);
}
