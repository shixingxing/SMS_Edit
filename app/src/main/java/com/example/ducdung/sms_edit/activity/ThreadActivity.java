package com.example.ducdung.sms_edit.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ducdung.sms_edit.GlobalState;
import com.example.ducdung.sms_edit.R;
import com.example.ducdung.sms_edit.adapter.ThreadItemAdapter;
import com.example.ducdung.sms_edit.constant.CommonConstant;
import com.example.ducdung.sms_edit.constant.UriConstant;
import com.example.ducdung.sms_edit.model.SmsThread;
import com.example.ducdung.sms_edit.util.ActivityHelper;
import com.example.ducdung.sms_edit.util.CursorInspector;
import com.example.ducdung.sms_edit.util.DialogBuilder;
import com.example.ducdung.sms_edit.util.TelephoneUtil;

import java.util.ArrayList;
import java.util.List;

public class ThreadActivity extends AppCompatActivity {
    static final String[] THREAD_PROJ = new String[]{"thread_id, address, body, date"};
    static final String[] THREAD_PROJ_ = new String[]{"_id, recipient_ids, snippet, date"};
    static final String THREAD_SCREEN = "Thread1";
    static final String tag = ThreadActivity.class.getSimpleName();
    ContentResolver contentResolver;
    Editor editor;
    Handler handler ;
    ProgressBar pg;
    SharedPreferences pref;
    ThreadItemAdapter threadAdapter;
    ListView threadList;
    List<SmsThread> threads;
    String userDefaultSmsApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_thread);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        threadList = (ListView) findViewById(R.id.lvMsgThreads);
        threadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openThreadView(threads.get(position));
            }
        });


        contentResolver = getContentResolver();
        this.handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("WrongConstant")
            public void handleMessage(Message msg) {
                if (ThreadActivity.this.threadAdapter == null) {
                    ThreadActivity.this.threadAdapter = new ThreadItemAdapter(
                            ThreadActivity.this,
                            ThreadActivity.this.threads
                    );
                    ThreadActivity.this.threadList.setAdapter(
                            ThreadActivity.this.threadAdapter
                    );
                } else {
                    ThreadActivity.this.threadAdapter.setThreadList(
                            ThreadActivity.this.threads
                    );
                    ThreadActivity.this.threadAdapter.notifyDataSetChanged();
                }
                if (ThreadActivity.this.pg != null) {
                    ThreadActivity.this.pg.setVisibility(8);
                }
            }
        };
        prepare();
    }

//

    private void openThreadView(SmsThread smsThread) {
        Bundle bundle = new Bundle();
        bundle.putInt(CommonConstant.THREAD_ID, smsThread.theadId);
        bundle.putString(CommonConstant.OTHER_PHOTO, smsThread.avatar);
        bundle.putString(CommonConstant.OWN_PHOTO, TelephoneUtil.getOwnPhoto(this));
        bundle.putString(CommonConstant.NAME, TextUtils.isEmpty(smsThread.displayName) ? smsThread.from : smsThread.displayName);
        ActivityHelper.startWithBundle(this, ThreadDetailActivity.class, bundle, false);
        GlobalState.MODIFY_FLAG = false;
    }

    private void prepare() {
        Cursor cursor;
        try {
            cursor = this.contentResolver.query( Uri.parse(UriConstant.THREAD_URI),
                    THREAD_PROJ, null, null, "date desc");
            CursorInspector.printColumns(cursor);
        } catch (Exception e) {
            e.printStackTrace();
            cursor = this.contentResolver.query(Uri.parse(UriConstant.THREAD_URI_),
                    THREAD_PROJ_, null, null, "date desc");
            GlobalState.IS_FAULTY = true;
        }
        this.threads = new ArrayList();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int cols = cursor.getColumnCount();
            this.threads.clear();
            do {
                String address = null;
                if (GlobalState.IS_FAULTY) {
                    String recipientId = cursor.getString(cursor.getColumnIndex("recipient_ids"));
                    Cursor c = this.contentResolver.query(Uri.parse(UriConstant.CANONICAL_ADDR),
                            null, "_id = ?", new String[]{recipientId}, null);
                    CursorInspector.printColumns(c);
                    if (c != null) {
                        address = CursorInspector.getContentByColumnIndex(c, c.getColumnIndex(CommonConstant.ADDR));
                    }
                }
                SmsThread thread = new SmsThread( cursor.getInt(0),
                        cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1)) ),
                        cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2)) ),
                        cursor.getLong(cursor.getColumnIndex(cursor.getColumnName(3))) );
                if (!TextUtils.isEmpty(address)) {
                    thread.from = address;
                }
                Cursor contactCursor = TelephoneUtil.getContactProfile(this, thread.from);
                if (contactCursor != null && contactCursor.getCount() > 0) {
                    contactCursor.moveToFirst();
                    thread.avatar = contactCursor.getString(4);
                    thread.displayName = contactCursor.getString(1);
                    contactCursor.close();
                }
                this.threads.add(thread);
            } while (cursor.moveToNext());
            cursor.close();
        }
        this.handler.sendEmptyMessage(0);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onResume() {
        super.onResume();
        Log.i("", "resume, " + Build.VERSION.SDK_INT);

        if (pref.getBoolean("exit", false)) {
            return;
        }

        if (GlobalState.MODIFY_FLAG) {
            // reload content
            //prepare();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();//"com.rayy.android.editorad";

            userDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);

            if (userDefaultSmsApp != null && !userDefaultSmsApp.equals(myPackageName)) {
                // App is not default.
                // Show the "not currently set as the default SMS app" interface

                editor = pref.edit();

                editor.putString("default", userDefaultSmsApp);
                editor.commit();

                boolean change_diag_show = pref.getBoolean("change_diag_show", true);

                if (change_diag_show) {
                    // show alert builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    LayoutInflater inflater = this.getLayoutInflater();
                    final View diag_view = inflater.inflate(R.layout.diag_sms_hint, null);

                    //final View diag_view = findViewById(R.layout.diag_sms_hint);
                    TextView tv = (TextView) diag_view.findViewById(R.id.tv_sms_msg);
                    tv.setText(R.string.msg_set_default_sms);
                    //builder.setTitle(R.string.diag_set_default_sms);
                    builder.setView(diag_view);
                    //builder.setMessage(R.string.msg_set_default_sms);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("", "Setting default Sms");

                            CheckBox cb = (CheckBox) diag_view.findViewById(R.id.cb_no_show);

                            if (cb.isChecked()) {
                                editor.putBoolean("change_diag_show", false).apply();
                            }

                            startChangeSmsDiagActivity(myPackageName);
                        }
                    });

                    builder.create().show();
                } else {
                    startChangeSmsDiagActivity(myPackageName);
                }

            } else {
                Log.i(tag, "Current app is set the default Sms app");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startChangeSmsDiagActivity(String packageToChange) {
        Log.i(tag, "change to " + packageToChange);

        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageToChange);

        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onStop() {
        super.onStop();

        //pref.edit().clear().apply();
        editor = pref.edit();
        editor.remove("exit").apply();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onBackPressed() {
        pref.edit().putBoolean("exit", true).commit();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String newDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);

            userDefaultSmsApp = pref.getString("default", "");

            Log.i("", newDefaultSmsApp + ", " + userDefaultSmsApp);

            if (newDefaultSmsApp != null && !newDefaultSmsApp.equals(userDefaultSmsApp)) {

                boolean revert_diag_show = pref.getBoolean("revert_diag_show", true);

                if (revert_diag_show) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    final View diag_view = inflater.inflate(R.layout.diag_sms_hint, null);

                    builder.setTitle(R.string.diag_set_default_sms);
                    TextView tv = (TextView) diag_view.findViewById(R.id.tv_sms_msg);
                    tv.setText(R.string.msg_revert_default_sms);
                    builder.setView(diag_view);
                    //builder.setMessage(R.string.msg_revert_default_sms);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("", "Revert default Sms");

                            //System.gc();

                            CheckBox cb = (CheckBox) diag_view.findViewById(R.id.cb_no_show);

                            if (cb.isChecked()) {
                                editor = pref.edit();
                                editor.putBoolean("revert_diag_show", false).apply();
                            }

                            startChangeSmsDiagActivity(userDefaultSmsApp);
                        }
                    });

                    builder.create().show();
                } else {
                    startChangeSmsDiagActivity(userDefaultSmsApp);
                }
            } else {
                Log.i(tag, "Current app is not set the default Sms app");

                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.menu, m);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        String help = getResources().getString(R.string.help);
        String help_msg = getResources().getString(R.string.help_msg_);
        String abt = getResources().getString(R.string.about);
        String abt_msg = getResources().getString(R.string.about_msg);

        if (id == R.id.help) {
            DialogBuilder.showAlert(this, help, help_msg + "<br>" + abt_msg, true);
        }

        return true;
    }
}
