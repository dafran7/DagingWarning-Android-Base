package com.ilkom.network;

public class API_PATH {
    private static String API_UPLOAD = "";
    private static String API_PREDICT = "";

    public API_PATH(String upload, String predict){
        API_UPLOAD = upload;
        API_PREDICT = predict;
    }

    public String getApiUpload(){
        return API_UPLOAD;
    }

    public String getApiPredict(){
        return API_PREDICT;
    }
}
