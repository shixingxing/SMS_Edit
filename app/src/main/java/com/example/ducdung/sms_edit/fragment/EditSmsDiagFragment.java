package com.example.ducdung.sms_edit.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.UiThread;
import androidx.fragment.app.DialogFragment;
import com.example.ducdung.sms_edit.GlobalState;
import com.example.ducdung.sms_edit.R;
import com.example.ducdung.sms_edit.activity.ThreadDetailActivity;
import com.example.ducdung.sms_edit.constant.CommonConstant;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.Executors;

import static com.example.ducdung.sms_edit.constant.UriConstant.SMS_URI;


public class EditSmsDiagFragment extends DialogFragment {
    private static final String TAG = EditSmsDiagFragment.class.getSimpleName();
    ProgressBar pgBar;
    Uri uri = Uri.parse(SMS_URI);
    private EditText sender, body, date;
    private TextView type;
    private Button save;
    private int _id;
    private Bundle bundle;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle(R.string.edit_message_diag_title);
        View editSmsView = View.inflate(getActivity(), R.layout.edit_single_message, null);
        builder.setView(editSmsView);

        pgBar = (ProgressBar) editSmsView.findViewById(R.id.diagLoading);

        bundle = getArguments();

        sender = (EditText) editSmsView.findViewById(R.id.sender);
        date = (EditText) editSmsView.findViewById(R.id.send_date);
        body = (EditText) editSmsView.findViewById(R.id.body);

        final String orig_addr = bundle.getString(CommonConstant.SENDER);
        sender.setText(orig_addr);
        date.setText(CommonConstant.dateFormat.format(bundle.getLong(CommonConstant.DATE)));
        body.setText(bundle.getString(CommonConstant.BODY));
        _id = bundle.getInt(CommonConstant.MSG_ID);

        type = (TextView) editSmsView.findViewById(R.id.type);

        final int s_type = bundle.getInt(CommonConstant.TYPE);

        if (s_type != 1)
            type.setText(getResources().getString(R.string.to));

        save = (Button) editSmsView.findViewById(R.id.save);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                saveMessage("" + s_type, orig_addr);
            }

        });

        return builder.create();
    }

    protected void saveMessage(String type, final String orig_addr) {
        final ContentValues cv = new ContentValues();

        cv.put(CommonConstant.BODY, body.getText().toString());
        final String after_addr = sender.getText().toString();
        cv.put("address", after_addr);
        cv.put(CommonConstant.TYPE, type);
        // default all messages are read. to be updated. 0- unread
        cv.put("read", 1);

        try {
            Date calldate = CommonConstant.dateFormat.parse(date.getText().toString());
            cv.put(CommonConstant.DATE, calldate.getTime());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ((ThreadDetailActivity) getActivity()).showLoading();
        pgBar.setVisibility(View.VISIBLE);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                modify(cv, after_addr, orig_addr);
            }
        });

        GlobalState.MODIFY_FLAG = true;

        //setResult(getActivity().RESULT_OK);
    }

    private void modify(ContentValues cv, String after_addr, String orig_addr) {
        // delete then insert
        getActivity().getContentResolver().delete(uri, "_id = ?", new String[]{_id + ""});
        // let system take care of thread
        getActivity().getContentResolver().insert(uri, cv);

        if (!after_addr.equalsIgnoreCase(orig_addr)) {
            ContentValues cv_empty2 = new ContentValues();
            cv_empty2.put("address", orig_addr);
            cv_empty2.put("read", 1);
            getActivity().getContentResolver().delete(getActivity().getContentResolver().insert(uri, cv_empty2),
                    null, null);
        }

        getActivity().getContentResolver().delete(getActivity().getContentResolver().insert(uri, cv),
                null, null);
        ContentValues cv_empty = new ContentValues();
        cv_empty.put("address", sender.getText().toString());
        cv_empty.put("read", 1);
        getActivity().getContentResolver().delete(getActivity().getContentResolver().insert(uri, cv_empty),
                null, null);

        hideLoading();
    }

    @UiThread
    private void hideLoading() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                pgBar.setVisibility(View.GONE);
                ThreadDetailActivity parentActivity = (ThreadDetailActivity) getActivity();

                if (parentActivity != null && !parentActivity.isChangingConfigurations()) {
                    Toast.makeText(parentActivity, getResources().getString(R.string.save_msg), Toast.LENGTH_SHORT).show();

                    parentActivity.notifyDataUpdate();
                }

                dismiss();
            }
        });
    }
}
