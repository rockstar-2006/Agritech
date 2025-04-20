package com.example.agri_tech;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    private OtpListener otpListener;

    public interface OtpListener {
        void onOtpReceived(String otp);
    }

    public void setOtpListener(OtpListener listener) {
        this.otpListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            if (status.getStatusCode() == CommonStatusCodes.SUCCESS) {
                String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                Pattern pattern = Pattern.compile("\\d{6}");
                Matcher matcher = pattern.matcher(message);
                if (matcher.find() && otpListener != null) {
                    otpListener.onOtpReceived(matcher.group(0));
                }
            }
        }
    }
}
