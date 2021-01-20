package com.ilkom.network;

public class Config {

    public static final String BASE_URL = "http://meat-detect.apps.cs.ipb.ac.id:80/"; // Your local IP Address
    public static final String API_DIR = "daging/";

    public static final String API_UPLOAD = BASE_URL + API_DIR + "upload_gambar.php";
    public static final String API_PREDICT = BASE_URL + API_DIR + "api.php";

}