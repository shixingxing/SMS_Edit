
package com.example.ducdung.sms_edit.activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ducdung.sms_edit.R;
import com.example.ducdung.sms_edit.constant.CommonConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * dev : Dung Pham
 * actvity edit message
 * */
public class EditMessage extends AppCompatActivity {

    private EditText sender, body, date;
    private Button save;
    private String _id;
    private int key;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit);

        Bundle bundle = getIntent().getExtras();

        sender = (EditText) findViewById(R.id.sender);
        date = (EditText) findViewById(R.id.send_date);
        body = (EditText) findViewById(R.id.body);

        sender.setText(bundle.getString("sender"));
        date.setText(bundle.getString("date"));
        body.setText(bundle.getString("body"));
        _id = bundle.getString("_id");
        key = bundle.getInt("key");

        TextView type = (TextView) findViewById(R.id.type);
        String s_type = bundle.getString("type");
        if (!s_type.equalsIgnoreCase("1"))
            type.setText(getResources().getString(R.string.to));

        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                saveMessage();
            }

        });
    }


    /*
    * method save mesage
    * */
    @SuppressLint("WrongConstant")
    protected void saveMessage() {
        Uri uri = Uri.parse("content://sms");
        ContentValues cv = new ContentValues();
        cv.put(CommonConstant.BODY, this.body.getText().toString());
        cv.put(CommonConstant.ADDR, this.sender.getText().toString());
        try {
            Date calldate = new SimpleDateFormat("HH:mm dd-MM-yyyy").parse(this.date.getText().toString());
            Log.v("TAG", calldate.getTime() + "");
            cv.put(CommonConstant.DATE, Long.valueOf(calldate.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // update new record
        getContentResolver().update(uri, cv, "_id = ?", new String[]{this._id});
        Toast.makeText(this, "Message is updated", 0).show();
        setResult(this.key);
        finish();
    }

}
