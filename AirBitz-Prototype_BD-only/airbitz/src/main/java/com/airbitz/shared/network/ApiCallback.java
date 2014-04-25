package com.airbitz.shared.network;

/**
 * Created by dannyroa on 3/3/14.
 */

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by dannyroa on 2/4/14.
 */
public abstract class ApiCallback<T> implements Callback<T> {


    @Override public void success(T t, Response response) {

        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.setSuccess(true);

        apiResponse.setObject(t);

        response(apiResponse);

    }

    @Override public void failure(RetrofitError error) {

        ApiResponse<T> apiResponse = new ApiResponse<T>();
            apiResponse.setSuccess(false);
            apiResponse.setRetrofitError(error);
            response(apiResponse);

    }

    public abstract void response(ApiResponse<T> apiResponse);
}
