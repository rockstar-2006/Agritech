package com.example.agri_tech;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int SMS_PERMISSION_CODE = 1001;
    private String lastTimestamp = "";
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private List<Contact> contacts = new ArrayList<>();
    private LinearLayout phoneNumbersLayout;
    private ValueEventListener animalDetectionListener;
    private DatabaseReference dbRef;

    private void animateText(TextView textView, String fullText, long delayMillis) {
        final Handler localHandler = new Handler();
        final int[] index = {0};

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    textView.setText(fullText.substring(0, index[0]));
                    index[0]++;
                    localHandler.postDelayed(this, delayMillis);
                } else {
                    index[0] = 0;
                    localHandler.postDelayed(this, 1000);
                }
            }
        };

        localHandler.post(runnable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView welcomeText = view.findViewById(R.id.login_welcomeText);
        TextView sloganText = view.findViewById(R.id.login_sloganText);
        animateText(welcomeText, "Agri - tech", 150);
        animateText(sloganText, "Empowering Farmers", 100);

        TextView alertMessage = view.findViewById(R.id.alertMessage);
        LinearLayout alertContainer = view.findViewById(R.id.alertContainer);
        TextView alertTitle = view.findViewById(R.id.alertTitle);
        alertTitle.setSelected(true);
        TextView alertTime = view.findViewById(R.id.alertTime);
        ImageView alertImage = view.findViewById(R.id.alertImage);
        ImageView addPhoneIcon = view.findViewById(R.id.addPhoneIcon);
        phoneNumbersLayout = view.findViewById(R.id.phoneNumbersLayout);

        handler = new Handler();
        requestSMSPermission();

        dbRef = FirebaseDatabase.getInstance().getReference("detected_animals");

        welcomeText.setText(getString(R.string.app_name));
        sloganText.setText(getString(R.string.tagline));

        animalDetectionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String label = child.child("label").getValue(String.class);
                    String timestamp = child.child("timestamp").getValue(String.class);
                    String base64Image = child.child("image_base64").getValue(String.class);

                    if (label != null && timestamp != null) {
                        alertContainer.setVisibility(View.VISIBLE);

                        try {
                            Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
                            alertContainer.startAnimation(slideIn);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        alertTitle.setText("⚠️ Alert");
                        alertMessage.setText(label.substring(0, 1).toUpperCase() + label.substring(1) + " detected!");
                        alertTime.setText(timestamp);

                        if (base64Image != null && base64Image.startsWith("data:image")) {
                            String pureBase64 = base64Image.split(",")[1];
                            byte[] decodedString = Base64.decode(pureBase64, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            alertImage.setImageBitmap(decodedByte);
                        }

                        if (!timestamp.equals(lastTimestamp)) {
                            lastTimestamp = timestamp;
                            playOneTimeAlertSound();
                            sendEmergencySMSToAll();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle Firebase error
            }
        };

        dbRef.limitToLast(1).addValueEventListener(animalDetectionListener);

        addPhoneIcon.setOnClickListener(v -> showAddPhoneNumberDialog());

        return view;
    }

    private void playOneTimeAlertSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        Uri alertUri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.alert_sound);
        mediaPlayer = MediaPlayer.create(getContext(), alertUri);
        mediaPlayer.start();

        handler.postDelayed(() -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }, 15000);
    }

    private void sendEmergencySMSToAll() {
        for (Contact contact : contacts) {
            sendEmergencySMS(contact.getNumber());
        }
    }

    private void sendEmergencySMS(String phoneNumber) {
        String message = "It's an emergency in your field. An animal has entered.";

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(getContext(), "SMS sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.SEND_SMS}, 101);
        }
    }

    private void showAddPhoneNumberDialog() {
        AddPhoneNumberDialogFragment dialogFragment = new AddPhoneNumberDialogFragment((name, phoneNumber) -> {
            if (!phoneNumber.isEmpty()) {
                Contact newContact = new Contact(name, phoneNumber);
                contacts.add(newContact);
                displayPhoneNumbers();
                Toast.makeText(getContext(), "Phone number added!", Toast.LENGTH_SHORT).show();
                sendEmergencySMS(phoneNumber);
            } else {
                Toast.makeText(getContext(), "Invalid phone number!", Toast.LENGTH_SHORT).show();
            }
        });
        dialogFragment.show(getParentFragmentManager(), "AddPhoneNumberDialog");
    }

    private void displayPhoneNumbers() {
        phoneNumbersLayout.removeAllViews();

        for (Contact contact : contacts) {
            LinearLayout contactLayout = new LinearLayout(getContext());
            contactLayout.setOrientation(LinearLayout.VERTICAL);
            contactLayout.setPadding(24, 24, 24, 24);
            contactLayout.setBackgroundResource(R.drawable.card_background);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 12, 0, 12);
            contactLayout.setLayoutParams(layoutParams);

            // Name
            TextView nameText = new TextView(getContext());
            nameText.setText("👤 " + contact.getName());
            nameText.setTextSize(16);
            nameText.setTextColor(Color.parseColor("#333333"));
            nameText.setTypeface(Typeface.DEFAULT_BOLD);
            nameText.setPadding(0, 0, 0, 8);

            // Phone Number
            TextView phoneText = new TextView(getContext());
            phoneText.setText("📞 " + contact.getNumber());
            phoneText.setTextSize(14);
            phoneText.setTextColor(Color.parseColor("#555555"));

            contactLayout.addView(nameText);
            contactLayout.addView(phoneText);
            phoneNumbersLayout.addView(contactLayout);
        }
    }

    private void requestSMSPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        handler.removeCallbacksAndMessages(null);

        if (dbRef != null && animalDetectionListener != null) {
            dbRef.removeEventListener(animalDetectionListener);
        }
    }
}
