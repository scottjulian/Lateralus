package xyz.lateralus.components.readers;

import android.content.Context;

import org.json.JSONObject;


public abstract class DataReader {

    protected Context _ctx;

    public DataReader(Context ctx){
        _ctx = ctx;
    }

    public abstract JSONObject getData();

    public abstract String getRootKey();
}
