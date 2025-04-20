package com.example.agri_tech;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.Random;

public class login extends AppCompatActivity {

    private EditText etPhone;
    private Button btnSendOtp;
    private LinearLayout otpLayout;
    private EditText[] otpFields = new EditText[6];
    private ProgressBar progressBar;
    private CountryCodePicker ccp;

    private String generatedOtp;
    private DatabaseReference databaseRef;
    private String userPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        otpLayout = findViewById(R.id.otpLayout);
        ccp = findViewById(R.id.ccp);
        progressBar = findViewById(R.id.progressBar);

        ccp.registerCarrierNumberEditText(etPhone);

        // Setup animations
        TextView welcomeText = findViewById(R.id.login_welcomeText);
        TextView sloganText = findViewById(R.id.login_sloganText);
        Animation welcomeAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation sloganAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        welcomeText.startAnimation(welcomeAnim);
        sloganText.postDelayed(() -> {
            sloganText.setVisibility(View.VISIBLE);
            sloganText.startAnimation(sloganAnim);
        }, 500);

        // OTP input fields
        otpFields[0] = findViewById(R.id.otp1);
        otpFields[1] = findViewById(R.id.otp2);
        otpFields[2] = findViewById(R.id.otp3);
        otpFields[3] = findViewById(R.id.otp4);
        otpFields[4] = findViewById(R.id.otp5);
        otpFields[5] = findViewById(R.id.otp6);
        otpLayout.setVisibility(View.GONE); // initially hide

        databaseRef = FirebaseDatabase.getInstance().getReference("phone_otps");

        // Auto-focus logic
        for (int i = 0; i < 5; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new SimpleTextWatcher(() -> {
                if (!otpFields[index].getText().toString().isEmpty()) {
                    otpFields[index + 1].requestFocus();
                }
            }));
        }

        otpFields[5].addTextChangedListener(new SimpleTextWatcher(this::verifyOtp));

        // SMS Retriever API
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        SmsBroadcastReceiver smsReceiver = new SmsBroadcastReceiver();

        Task<Void> task = client.startSmsRetriever();
        task.addOnSuccessListener(aVoid -> {
            // Retriever started
        }).addOnFailureListener(e -> {
            // Failed to start
        });

        smsReceiver.setOtpListener(otp -> {
            for (int i = 0; i < otp.length(); i++) {
                otpFields[i].setText(String.valueOf(otp.charAt(i)));
            }
            verifyOtp();
        });
        registerReceiver(smsReceiver, new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION));

        // Send OTP button
        btnSendOtp.setOnClickListener(v -> {
            String rawPhone = etPhone.getText().toString().replaceAll("\\s+", "");
            if (TextUtils.isEmpty(rawPhone) || rawPhone.length() != 10) {
                etPhone.setError("Enter valid 10-digit phone number");
                return;
            }

            userPhoneNumber = ccp.getFullNumberWithPlus().replaceAll("\\s+", "");
            sendOtp(userPhoneNumber);
        });
    }

    private void sendOtp(String number) {
        progressBar.setVisibility(View.VISIBLE);

        generatedOtp = String.format("%06d", new Random().nextInt(999999));

        // Save OTP to Firebase for testing/demo
        databaseRef.child(number).setValue(generatedOtp);

        progressBar.setVisibility(View.GONE);
        showOtpLayout();

        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        for (EditText field : otpFields) {
            field.startAnimation(zoomIn);
        }

        // Show OTP as Toast (simulate SMS)
        Toast.makeText(this, "OTP sent: " + generatedOtp, Toast.LENGTH_LONG).show();
    }

    private void showOtpLayout() {
        otpLayout.setVisibility(View.VISIBLE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        otpLayout.startAnimation(slideUp);

        for (EditText field : otpFields) {
            field.setText(""); // clear previous
        }

        otpFields[0].requestFocus();
    }

    private void verifyOtp() {
        StringBuilder otpBuilder = new StringBuilder();
        for (EditText field : otpFields) {
            otpBuilder.append(field.getText().toString());
        }

        String enteredOtp = otpBuilder.toString();

        if (enteredOtp.length() == 6) {
            if (enteredOtp.equals(generatedOtp)) {
                Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                showOtpError();
            }
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showOtpError() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        for (EditText field : otpFields) {
            field.setBackgroundResource(R.drawable.otp_box_error);
            field.startAnimation(shake);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");
        super.attachBaseContext(MyContextWrapper.wrap(newBase, langCode));
    }
}
