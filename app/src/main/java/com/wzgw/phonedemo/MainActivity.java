package com.wzgw.phonedemo;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ContentResolver resolver;

    //测试写入的号码,联系人名字 AAAAA
    private String[] number = {"02869514433",
            "02869514432",
            "02869514431",
            "02869514115",
            "02869514396",
            "02869514397",
            "02869514398",
            "02869514399",
            "02869514400",
            "02869514113",
            "02869514111",
            "02869514114",
            "02869514112"};

    //  联系人表的uri
    private Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;

    private Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    private Uri emailUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    private Cursor mContactsCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Cursor contactsCursor = resolver.query(contactsUri, null, null, null, null);
    }

    //获取所有
    public void getAll(View view) {
        mContactsCursor = getContentResolver().query(Uri.parse("content://com.android.contacts/contacts"), null, null, null, null);
        List<ContactBean> data = getData(mContactsCursor);
        for (int i = 0; i < data.size(); i++) {
            ContactBean bean = data.get(i);
            Log.d(TAG, "name: " + bean.name + "phone: " + bean.phone);
        }
    }

    //插入
    public void insert(View view) {
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts._ID}, ContactsContract.Contacts.DISPLAY_NAME + "=?",
                new String[]{"AAAAA"}, null);
        if (cursor != null && cursor.moveToNext()) {
            //通讯录已经有这个人
            //TODO 先删除这个人,再重新写入

        } else {
            //没有这个人,添加

            ContentValues values = new ContentValues();
            Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);

            // TODO 1. 向data表插入姓名数据
            values.clear();
            values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, "AAAAA");
            getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

            // TODO 2. 向data表插入电话数据
            for (String phone : number) {
                values.clear();
                values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }
        }
    }

    //往AAAAA插入一个号码
    public void insert_one(View view) {
        String name = "AAAAA";
        //根据姓名求id
        Uri uri2 = Uri.parse("content://com.android.contacts/raw_contacts");
        Cursor cursor1 = getContentResolver().query(uri2, new String[]{ContactsContract.Contacts.Data._ID}, "display_name=?", new String[]{name}, null);
        if (cursor1 != null && cursor1.moveToFirst()) {
            int rawContactsId = cursor1.getInt(0);

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactsId)  // 这里关键是传入正确的raw_contacts_id值
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, "1111111111")
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                    .build());
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            } finally {
                if (cursor1 != null) {
                    cursor1.close();
                }
            }
        }
    }


    //删除一个联系人所有号码
    public void delete(View view) {
        //根据姓名求id
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.Data._ID}, "display_name=?", new String[]{"AAAAA"}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            //根据id删除data中的相应数据
            resolver.delete(uri, "display_name=?", new String[]{"AAAAA"});
            uri = Uri.parse("content://com.android.contacts/data");
            resolver.delete(uri, "raw_contact_id=?", new String[]{id + ""});
        }
        if (cursor != null)
            cursor.close();
    }

    //删除某个联系人一个号码
    public void delete_one(View view) {
        Toast.makeText(this, "暂时还没法处理", Toast.LENGTH_SHORT).show();
    }

    //修改
    public void change(View view) {
        String phone = "999999";
        Uri uri = Uri.parse("content://com.android.contacts/data");//对data表的所有数据操作
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put("data1", phone);

        String name = "AAAAA";
        //根据姓名求id
        Uri uri2 = Uri.parse("content://com.android.contacts/raw_contacts");
        Cursor cursor1 = resolver.query(uri2, new String[]{ContactsContract.Contacts.Data._ID}, "display_name=?", new String[]{name}, null);
        if (cursor1.moveToFirst()) {
            int id = cursor1.getInt(0);
            //在raw_contacts表根据姓名(此处的姓名为name记录的data2的数据而不是data1的数据)查出id；
            resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/phone_v2", id + ""});
        }
    }

    private List<ContactBean> getData(Cursor cursor) {
        List<ContactBean> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            //找到对应的列
            int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int displayNameColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            do {
                // 获得联系人的ID
                String contactId = cursor.getString(idColumn);
                // 获得联系人姓名
                String displayName = cursor.getString(displayNameColumn);

                // 查看联系人有多少个号码，如果没有号码，返回0
                int phoneCount = cursor.getInt(cursor
                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (phoneCount > 0) {
                    // 获得联系人的电话号码列表
                    Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + "=" + contactId, null, null);
                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        do {
                            //遍历所有的联系人下面所有的电话号码
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String name = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            ContactBean bean = new ContactBean();
                            bean.name = name;
                            bean.phone = phoneNumber;
                            list.add(bean);
                        } while (phoneCursor.moveToNext());
                    }
                }

            } while (cursor.moveToNext());
        }

        return list;
    }

    //读取所有通话记录
    public void read_all(View view) {
        ContentResolver resolver = getContentResolver();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, // 查询通话记录的URI
                new String[]{CallLog.Calls.CACHED_NAME// 通话记录的联系人
                        , CallLog.Calls.NUMBER// 通话记录的电话号码
                        , CallLog.Calls.DATE// 通话记录的日期
                        , CallLog.Calls.DURATION// 通话时长
                        , CallLog.Calls.TYPE}// 通话类型
                , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        );

        List<CalllogBean> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
            String date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date(dateLong));
            int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
            String typeString = "";
            switch (type) {
                case CallLog.Calls.INCOMING_TYPE:
                    typeString = "打入";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    typeString = "打出";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    typeString = "未接";
                    break;
                default:
                    break;
            }
            CalllogBean bean = new CalllogBean();
            bean.name = (name == null) ? "未备注联系人" : name;
            bean.number = number;
            bean.date = date;
            bean.duration = (duration / 60) + "分钟";
            bean.type = typeString;

            list.add(bean);
        }

        for (int i = 0; i < list.size(); i++) {
            CalllogBean bean = list.get(i);
            Log.d(TAG, "name: " + bean.name + "phone: " + bean.number + "data: "
                    + bean.date + "duration: " + bean.duration + "type: " + bean.type);
        }

    }

    //删除某个人的所有通话记录
    public void delete_one_calllog(View view) {
        ContentResolver resolver = getContentResolver();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        int a = resolver.delete(CallLog.Calls.CONTENT_URI, "number = ?", new String[]{"888888888"});
    }

    //写入一条通话记录到某人
    public void write_one(View view) {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, "888888888");
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
        values.put(CallLog.Calls.DURATION, "2");
        values.put(CallLog.Calls.TYPE, "未接");//未接
        values.put(CallLog.Calls.NEW, 0);//0已看1未看

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
    }
}
