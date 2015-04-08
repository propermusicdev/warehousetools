package com.proper.data.helpers;

import java.io.Serializable;

/**
 * Created by Lebel on 25/02/2015.
 */
public class HttpResponseHelper implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean Success;
    private Object Response;
    private int HttpResponseCode;
    private Class<?> ExceptionClass;
    private String ResponseMessage;
    private long Duration;

    public HttpResponseHelper() {
    }

    public HttpResponseHelper(boolean success, Object response, int httpResponseCode, Class<?> exceptionClass, String responseMessage, long duration) {
        Success = success;
        Response = response;
        HttpResponseCode = httpResponseCode;
        ExceptionClass = exceptionClass;
        ResponseMessage = responseMessage;
        Duration = duration;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean isSuccess() {
        return Success;
    }

    public void setSuccess(boolean success) {
        Success = success;
    }

    public Object getResponse() {
        return Response;
    }

    public void setResponse(Object response) {
        Response = response;
    }

    public int getHttpResponseCode() {
        return HttpResponseCode;
    }

    public void setHttpResponseCode(int httpResponseCode) {
        HttpResponseCode = httpResponseCode;
    }

    public Class<?> getExceptionClass() {
        return ExceptionClass;
    }

    public void setExceptionClass(Class<?> exceptionClass) {
        ExceptionClass = exceptionClass;
    }

    public String getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        ResponseMessage = responseMessage;
    }

    public long getDuration() {
        return Duration;
    }

    public void setDuration(long duration) {
        Duration = duration;
    }
}
