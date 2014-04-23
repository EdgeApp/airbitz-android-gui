
package com.airbitz.shared.network;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import retrofit.RetrofitError;


public class ApiResponse<T> {

    private int statusCode;
    private String errorMessage;
    private String errorTitle;

    private T object;

    private boolean isSuccess = false;


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setRetrofitError(RetrofitError error) {
        setSuccess(false);
        try {
            setError(error.getResponse().getBody().in());
            statusCode = error.getResponse().getStatus();
        } catch (Exception e) {
            e.printStackTrace();
            setErrorTitle("Error");
            setErrorMessage(e.getMessage());
        }
    }

    public void setError(InputStream is) {

        try {

            String s = IOUtils.toString(is);
            this.setErrorMessage(s);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
