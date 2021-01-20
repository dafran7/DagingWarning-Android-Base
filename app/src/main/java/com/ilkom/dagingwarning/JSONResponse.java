package com.ilkom.dagingwarning;

import org.json.JSONArray;
import org.json.JSONObject;

public interface JSONResponse {
    void onCallback(JSONObject jObj);

    void onFail(String err_message);
}