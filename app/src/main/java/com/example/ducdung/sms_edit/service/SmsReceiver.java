package com.example.ducdung.sms_edit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import com.example.ducdung.sms_edit.util.SmsUtil;

public class SmsReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.i("", "Rcve Sms, doing nothing");
        if (intent.getAction().equals("android.provider.Telephony.SMS_DELIVER") && Build.VERSION.SDK_INT >= 19) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages != null && messages.length > 0) {
                SmsMessage message = messages[0];
                new SmsUtil(context).createSms( message.getOriginatingAddress(),
                        message.getDisplayMessageBody(),
                        System.currentTimeMillis(),
                        message.getProtocolIdentifier(), 0, 1
                );
            }
        }
    }

}
