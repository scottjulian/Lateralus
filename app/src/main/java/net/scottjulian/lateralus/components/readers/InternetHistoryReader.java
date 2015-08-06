package net.scottjulian.lateralus.components.readers;


import android.content.Context;
import android.database.Cursor;
import android.provider.Browser;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;


public class InternetHistoryReader extends DataReader{
    private static final String TAG = "InternetHistoryReader";
    private static final String ROOT_KEY = "internet_history";

    public static final String KEY_URL   = "url";
    public static final String KEY_TITLE = "title";

    public InternetHistoryReader(Context ctx) {
        super(ctx);
    }

    @Override
    public JSONObject getData() {
        try{
            return new JSONObject().put(ROOT_KEY, getInternetHistory());
        }
        catch(Exception e){
            Log.e(TAG, "Error getting internet history data!");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getRootKey() {
        return ROOT_KEY;
    }

    public JSONArray getInternetHistory(){
        String[] proj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL };
        String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
        Cursor cursor = _ctx.getContentResolver().query(Browser.BOOKMARKS_URI, proj, sel, null, null);
        JSONArray rootArray = new JSONArray();
        if(cursor.moveToFirst()) {
            do{
                JSONObject page = new JSONObject();
                String title = cursor.getString(cursor.getColumnIndex(Browser.BookmarkColumns.TITLE));
                String url = cursor.getString(cursor.getColumnIndex(Browser.BookmarkColumns.URL));
                try{
                    page.put(KEY_TITLE, title);
                    page.put(KEY_URL, url);
                    rootArray.put(page);
                }
                catch(Exception e){
                    Log.e(TAG, "Error creating internet history data!");
                    e.printStackTrace();
                }
            }
            while(cursor.moveToNext());
            cursor.close();
            return rootArray;
        }
        return null;
    }
}
