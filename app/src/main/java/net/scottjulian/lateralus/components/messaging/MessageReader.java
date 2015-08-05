package net.scottjulian.lateralus.components.messaging;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import net.scottjulian.lateralus.components.contacts.ContactReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;


public class MessageReader {

    public static final String CONTENT_SMS = "content://sms/";
    public static final String CONTENT_MMS = "content://mms/";
    public static final String CONTENT_MMS_PART = "content://mms/part/";

    private static final String PLAIN_TEXT  = "text/plain";
    private static final String UTF8        = "UTF-8";
    private static final String M_TYPE_SENT = "128";

    private static final String COL_ID      = "_id";
    private static final String COL_ADDRESS = "address";
    private static final String COL_DATE    = "date";
    private static final String COL_BODY    = "body";
    private static final String COL_TYPE    = "type";
    private static final String COL_TEXT    = "text";
    private static final String COL_M_TYPE  = "m_type";
    private static final String COL_CT      = "ct";
    private static final String COL_DATA    = "_data";

    public static HashMap<String, ArrayList<TextMessage>> getConversations(Context ctx, int count){
        HashMap<String, ArrayList<TextMessage>> map = new HashMap<>();

        ArrayList<TextMessage> sms = getSmsMessages(ctx, count);
        ArrayList<TextMessage> mms = getMmsMessages(ctx, count);

        // sms
        for(TextMessage textMessage : sms){
            if(map.containsKey(textMessage.getNumber())){
                map.get(textMessage.getNumber()).add(textMessage);
            }
            else{
                ArrayList<TextMessage> list = new ArrayList<>();
                list.add(textMessage);
                map.put(textMessage.getNumber(), list);
            }
        }

        // mms
        for(TextMessage textMessage : mms){
            if(map.containsKey(textMessage.getNumber())){
                map.get(textMessage.getNumber()).add(textMessage);
            }
            else{
                ArrayList<TextMessage> list = new ArrayList<>();
                list.add(textMessage);
                map.put(textMessage.getNumber(), list);
            }
        }

        // sort
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            Collections.sort((ArrayList<TextMessage>) pair.getValue());
        }

        return map;
    }

    //////// SMS ////////

    public static ArrayList<TextMessage> getSmsMessages(Context ctx, int count){
        Cursor cursor = ctx.getContentResolver().query(Uri.parse(CONTENT_SMS), null, null, null, null);
        ArrayList<TextMessage> smsMessages = new ArrayList<>();
        if(cursor.moveToFirst()) {
            int x = 0;
            do {
                String address = cursor.getString(cursor.getColumnIndex(COL_ADDRESS));
                smsMessages.add(new TextMessage(
                        ContactReader.getContactName(ctx, address),
                        address,
                        cursor.getString(cursor.getColumnIndex(COL_BODY)),
                        TextMessage.SMS,
                        cursor.getLong(cursor.getColumnIndex(COL_DATE)),
                        cursor.getInt(cursor.getColumnIndex(COL_TYPE))
                ));
                x++;
            }
            while(cursor.moveToNext() && x < count);
            cursor.close();
        }
        return smsMessages;
    }

    //////// MMS ////////

    public static ArrayList<TextMessage> getMmsMessages(Context ctx, int count){
        Cursor cursor = ctx.getContentResolver().query(Uri.parse(CONTENT_MMS), null, null, null, null);
        ArrayList<TextMessage> mmsMessages = new ArrayList<>();
        if(cursor.moveToFirst()) {
            int x = 0;
            do {
                String pid = cursor.getString(cursor.getColumnIndex(COL_ID));
                long timestamp = cursor.getLong(cursor.getColumnIndex(COL_DATE)) * 1000; // wtf google
                int type = (cursor.getString(cursor.getColumnIndex(COL_M_TYPE)).equals(M_TYPE_SENT)) ? TextMessage.SENT : TextMessage.RECEIVED;
                TextMessage tm = extractMms(ctx, pid, timestamp, type);
                if(tm != null) {
                    mmsMessages.add(tm);
                }
                x++;
            }
            while(cursor.moveToNext() && x < count);
            cursor.close();
        }
        return mmsMessages;
    }

    private static TextMessage extractMms(Context ctx, String pid, long timestamp, int type){
        String selectionPart = "mid=" + pid;
        Cursor cursor = ctx.getContentResolver().query(Uri.parse(CONTENT_MMS_PART), null, selectionPart, null, null);

        String address = getMmsAddress(ctx, pid);
        String name = ContactReader.getContactName(ctx, address);
        String msgText = "";

        if (cursor.moveToFirst()) {
            do {
                String partId = cursor.getString(cursor.getColumnIndex(COL_ID));
                String ct_type = cursor.getString(cursor.getColumnIndex(COL_CT));
                if (PLAIN_TEXT.equals(ct_type)) {
                    if (cursor.getString(cursor.getColumnIndex(COL_DATA)) != null) {
                        msgText = getMmsText(ctx, partId);
                    }
                    else {
                        msgText = cursor.getString(cursor.getColumnIndex(COL_TEXT));
                    }
                }
            }
            while (cursor.moveToNext());
        }

        if(!address.isEmpty() && !msgText.isEmpty()) {
            return new TextMessage(name, address, msgText, TextMessage.MMS, timestamp, type);
        }
        return null;
    }

    private static String getMmsText(Context ctx, String partId){
        InputStream is = null;
        StringBuilder builder = new StringBuilder();
        try {
            is = ctx.getContentResolver().openInputStream(Uri.parse(CONTENT_MMS_PART + partId));
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, UTF8);
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    builder.append(temp);
                    temp = reader.readLine();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // do nothing
                }
            }
        }
        return builder.toString();
    }

    public static String getMmsAddress(Context context, String partId) {
        String addrSelection = "type=137 AND msg_id=" + partId;
        String[] columns = { COL_ADDRESS };
        Cursor cursor = context.getContentResolver().query(
                Uri.parse(MessageFormat.format(CONTENT_MMS + "{0}/addr", partId)),
                columns,
                addrSelection,
                null,
                null);
        String address = "";
        String val;
        if (cursor.moveToFirst()) {
            do {
                val = cursor.getString(cursor.getColumnIndex(COL_ADDRESS));
                if (val != null && !val.equals("insert-address-token")) {
                    address = val;
                    break;
                }
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return address.replaceAll("[^0-9]", "");
    }

}
