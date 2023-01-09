package com.example.ducdung.sms_edit.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.example.ducdung.sms_edit.R;
import com.example.ducdung.sms_edit.fragment.EditSmsDiagFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LoadMessage extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = LoadMessage.class.getSimpleName();
    private final int PICK_CONTACT = 0, PICK_MESSAGE = 1, LOAD_MESSAGE = 2;
    ImageButton pick_c;
    private Context mContext;
    private View editSmsView;
    private ListView lv;
    private Button pick_m;
    private EditText number;
    private Cursor c;
    private String[] from = {"src", "body", "date"};
    private int[] to = {R.id.img, R.id.left, R.id.right};
    private int picked;
    private ArrayList<String> numList = new ArrayList<String>();
    private String _id, sender, date, body, type;
    private ArrayList<HashMap<String, Object>> msgs;
    private HashMap<String, Object> msg;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.getData().getInt("total");
            Log.v(TAG, "2. Handler message get.." + total);
            if (total == 1) {
                diag.dismiss();
                //Toast.makeText(LoadMessage.this, "Messages loaded successfully", Toast.LENGTH_SHORT).show();
                //Log.v(TAG, "2. Handler message finished..");
                loadMessage();
            }
        }
    };
    private ProgressDialog pdg, diag;
    private String help_msg, abt_msg, help, abt, loading, loading_empty;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String currentDefaultSmsApp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        pref = PreferenceManager.getDefaultSharedPreferences(mContext);

        setContentView(R.layout.edit_sms);


        number = (EditText) findViewById(R.id.from);

        lv = (ListView) findViewById(R.id.lv);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long position) {
                msg = msgs.get(id);
                _id = msg.get("_id").toString();
                sender = msg.get("address").toString();
                type = msg.get("type").toString();
                body = msg.get("body").toString();
                date = msg.get("date").toString();

                Bundle bundle = new Bundle();
                bundle.putString("_id", _id);
                bundle.putString("sender", sender);
                bundle.putString("type", type);
                bundle.putString("body", body);
                bundle.putString("date", date);

                //Intent intent = new Intent(EditSMS.this, EditMessage.class);
                //intent.putExtras(bundle);

                //startActivityForResult(intent, LOAD_MESSAGE);
                showEditSmsDialog(bundle);
            }

        });

        pick_c = (ImageButton) findViewById(R.id.pick_c);

        pick_c.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                pickContact();
            }

        });

        pick_m = (Button) findViewById(R.id.pick_m);

        pick_m.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //loadMessage();
                String num = number.getText().toString();
                if (!num.equalsIgnoreCase("") && num.charAt(0) == '+' && num.length() >= 4) {
                    //num = num.substring(3).replaceAll("\\s","");
                    num = num.substring(3);
                }

                char[] numChars = num.toCharArray();
                String numFormatted = "";
                StringBuilder sb = new StringBuilder();

                for (char c : numChars) {
                    sb.append(c).append("%");
                }

                loading = getResources().getString(R.string.loading);

                diag = new ProgressDialog(LoadMessage.this);
                diag.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                diag.setMessage(loading);
                diag.show();

                loadMessageCursor(sb.toString());
            }

        });

    }

    /*
    * display dialog edit sms
    * */
    protected void showEditSmsDialog(Bundle bundle) {
        //FragmentTransaction ft = getFragmentManager().beginTransaction();
        FragmentManager fm = getSupportFragmentManager();
        EditSmsDiagFragment editDiag = new EditSmsDiagFragment();
        editDiag.setArguments(bundle);
        editDiag.setRetainInstance(true);

        editDiag.show(fm, "e_dialog");
    }

    protected void showAnim() {

        loading = getResources().getString(R.string.loading);

        diag = new ProgressDialog(this);
        diag.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        diag.setMessage(loading);
        diag.show();

        Timer tm = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Message msg = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putInt("total", 1);
                msg.setData(b);
                handler.sendMessage(msg);
            }

        };

        tm.schedule(task, 2000);
    }

    protected boolean loadMessage() {

        ContentResolver cr = getContentResolver();
        Uri uri = Uri.parse("content://sms");

        msgs = new ArrayList<HashMap<String, Object>>();

        String num = number.getText().toString();
        /*if (!num.equalsIgnoreCase("") && num.charAt(0) == '+')
            num = num.substring(4);*/
        if (!num.equalsIgnoreCase("") && num.charAt(0) == '+' && num.length() >= 4) {
            //num = num.substring(3).replaceAll("\\s","");
            num = num.substring(3);
        }
        Log.d(TAG, num);
        c = cr.query(uri, null, "address like ?", new String[]{"%" + num}, null);

        Log.v(TAG, c.getCount() + " "); // Total messages ? get thread count??

        if (c.getCount() == 0) {
            loading_empty = getResources().getString(R.string.loading_empty);
            Toast.makeText(this, loading_empty, Toast.LENGTH_SHORT).show();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");

        // Parse date format.
        // c.moveToFirst();
        while (c.moveToNext()) {
            msg = new HashMap<String, Object>();

            for (int i = 0; i < c.getColumnCount(); i++) {
                if (c.getColumnName(i).equalsIgnoreCase("date")) {
                    Date d = new Date(Long.parseLong(c.getString(i)));

                    msg.put("date", sdf.format(d));

                    Log.v(TAG, sdf.format(d));
                } else if (c.getColumnName(i).equalsIgnoreCase("type")) {
                    int src = (Integer.parseInt(c.getString(i)) == 1) ? R.drawable.arrow_down : R.drawable.arrow_up;
                    msg.put("src", src);
                    msg.put("type", c.getString(i));
                } else {
                    msg.put(c.getColumnName(i), c.getString(i));
                }

                // Log.v(TAG, c.getColumnName(i) + " " + c.getString(i));
            }

            msgs.add(msg);
        }

        // SimpleCursorAdapter sa = new SimpleCursorAdapter(this,
        // R.layout.msg_list, c, from, to);
        SimpleAdapter sa = new SimpleAdapter(this, msgs, R.layout.edit_msg_list, from, to);

        lv.setAdapter(sa);

        return true;
    }

    /*
    * load sms by cursor
    * */
    public void loadMessageCursor(String num) {
        Bundle bundle = new Bundle();
        bundle.putString("numKey", num);

        Log.i(TAG, "address: " + num);

        getLoaderManager().restartLoader(12, bundle, this);
    }

    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.menu, m);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        help = getResources().getString(R.string.help);
        help_msg = getResources().getString(R.string.help_msg);
        abt = getResources().getString(R.string.about);
        abt_msg = getResources().getString(R.string.about_msg);

        if (id == R.id.help) {
            //String text = getString(R.string.help);
            showDialog(help, "<br>" + help_msg + "<br>" + abt_msg);
        }
        /*if (id == R.id.about) {
            String text = getString(R.string.about);
			showDialog(abt, abt_msg);
		}*/

        return true;
    }

    private void showDialog(String title, String text) {
        // TODO Auto-generated method stub

        TextView diag = new TextView(this);
        diag.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        diag.setPadding(20, 0, 20, 20);
        diag.setTextColor(Color.WHITE);
        diag.setTextSize(16f);
        diag.setText(Html.fromHtml(text));
        diag.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(diag);
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }

        });

        builder.create().show();
    }

    protected void pickContact() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    public void onActivityResult(int requstCode, int resultCode, Intent data) {

        if (requstCode == PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = managedQuery(contactData, null,
                        null, null, null);
                ContentResolver cr = getContentResolver();

                if (c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                    if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                + " = ?", new String[]{id}, null);

                        Log.v(TAG, "Size: " + pCur.getCount()); // Contact has
                        // this many
                        // numbers
                        numList.clear();

                        while (pCur.moveToNext()) {
                            for (int i = 0; i < pCur.getColumnCount(); i++) {
                                if (pCur.getColumnName(i).equalsIgnoreCase("data1")) {
                                    numList.add((pCur.getString(i).replace("-", ""))); // ???
                                    Log.v(TAG, "Added: " + pCur.getString(i));
                                }
                            }
                        }

                        if (numList.size() == 1) {
                            number.setText(numList.get(0));
                        } else {
                            numberChooser(numList);
                        }

                        pCur.close();
                    }
                }
            }
        }

        if (requstCode == LOAD_MESSAGE) {
            if (resultCode == RESULT_OK) {
                loadMessage();
                // c.requery();
            }
        }
    }

    protected void numberChooser(ArrayList<String> list) {

        final String[] temp = new String[list.size()];

        for (int i = 0; i < list.size(); i++)
            temp[i] = list.get(i);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Number");
        builder.setSingleChoiceItems(temp, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                picked = id;
                number.setText(temp[picked]);
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Log.v(TAG, String.valueOf(picked));
            }
        });

        builder.create().show();
    }

    public void onPause() {
        super.onPause();
        Log.i("", "pause");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onStop() {
        super.onStop();
        Log.i("", "stop");

        //pref.edit().clear().apply();
        editor = pref.edit();
        editor.remove("default").remove("exit").apply();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onBackPressed() {
        Log.i("", "onBack");

        pref.edit().putBoolean("exit", true).commit();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String newDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);

            currentDefaultSmsApp = pref.getString("default", "");

            Log.i("", newDefaultSmsApp + ", " + currentDefaultSmsApp);

            if (newDefaultSmsApp != null && !newDefaultSmsApp.equals(currentDefaultSmsApp)) {

                boolean revert_diag_show = pref.getBoolean("revert_diag_show", true);

                if (revert_diag_show) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoadMessage.this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    final View diag_view = inflater.inflate(R.layout.diag_sms_hint, null);
                    //final View diag_view = findViewById(R.layout.diag_sms_hint);

                    builder.setTitle(R.string.diag_set_default_sms);
                    TextView tv = (TextView) diag_view.findViewById(R.id.tv_sms_msg);
                    tv.setText(R.string.msg_revert_default_sms);
                    builder.setView(diag_view);
                    //builder.setMessage(R.string.msg_revert_default_sms);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("", "Revert default Sms");

                            //System.gc();

                            CheckBox cb = (CheckBox) diag_view.findViewById(R.id.cb_no_show);

                            if (cb.isChecked()) {
                                editor = pref.edit();
                                editor.putBoolean("revert_diag_show", false).apply();
                            }

                            startChangeSmsDiagActivity(currentDefaultSmsApp);
                        }
                    });

                    builder.create().show();
                } else {
                    startChangeSmsDiagActivity(currentDefaultSmsApp);
                }
            } else {
                Log.i(TAG, "Current app is not set the default Sms app");

                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onResume() {
        super.onResume();
        Log.i("", "resume, " + Build.VERSION.SDK_INT);

        if (pref.getBoolean("exit", false)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = mContext.getPackageName();//"com.rayy.android.editorad";

            currentDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);

            if (currentDefaultSmsApp != null && !currentDefaultSmsApp.equals(myPackageName)) {
                // App is not default.
                // Show the "not currently set as the default SMS app" interface

                editor = pref.edit();

                editor.putString("default", currentDefaultSmsApp);
                editor.commit();

                boolean change_diag_show = pref.getBoolean("change_diag_show", true);

                if (change_diag_show) {
                    // show alert builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoadMessage.this);

                    LayoutInflater inflater = this.getLayoutInflater();
                    final View diag_view = inflater.inflate(R.layout.diag_sms_hint, null);

                    //final View diag_view = findViewById(R.layout.diag_sms_hint);
                    TextView tv = (TextView) diag_view.findViewById(R.id.tv_sms_msg);
                    tv.setText(R.string.msg_set_default_sms);
                    builder.setTitle(R.string.diag_set_default_sms);
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
                Log.i(TAG, "Current app is set the default Sms app");
            }
        }
    }

    /*
    * premision android 4.4
    * */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startChangeSmsDiagActivity(String packageToChange) {
        Log.i(TAG, "change to " + packageToChange);
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageToChange);

        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }

        // Log.v(TAG, "Orientation change.");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        CursorLoader cl = new CursorLoader(mContext);

        cl.setUri(Uri.parse("content://sms"));
        cl.setProjection(null);
        cl.setSelection("address like ?");
        cl.setSelectionArgs(new String[]{"%" + bundle.getString("numKey")});
        cl.setSortOrder(null);

        //cl.loadInBackground();

        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.i(TAG, "cursorloader: " + cursor.getCount());

        if (cursor.getCount() == 0) {
            loading_empty = getResources().getString(R.string.loading_empty);
            Toast.makeText(this, loading_empty, Toast.LENGTH_SHORT).show();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");

        msgs = new ArrayList<HashMap<String, Object>>();

        // Parse date format.
        // c.moveToFirst();
        while (cursor.moveToNext()) {
            msg = new HashMap<String, Object>();

            for (int i = 0; i < cursor.getColumnCount(); i++) {
                if (cursor.getColumnName(i).equalsIgnoreCase("date")) {
                    Date d = new Date(Long.parseLong(cursor.getString(i)));
                    msg.put("date", sdf.format(d));
                }
                else if (cursor.getColumnName(i).equalsIgnoreCase("type")) {
                    int src = (Integer.parseInt(cursor.getString(i)) == 1) ? R.drawable.arrow_down : R.drawable.arrow_up;
                    msg.put("src", src);
                    msg.put("type", cursor.getString(i));
                }
                else {
                    msg.put(cursor.getColumnName(i), cursor.getString(i));
                }
            }

            msgs.add(msg);
        }

        // SimpleCursorAdapter sa = new SimpleCursorAdapter(this,
        // R.layout.msg_list, c, from, to);
        SimpleAdapter sa = new SimpleAdapter(this, msgs, R.layout.edit_msg_list, from, to);

        lv.setAdapter(sa);

        if (diag.isShowing())
            diag.dismiss();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader = null;
    }
}