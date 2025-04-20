package com.example.agri_tech;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Welcome extends AppCompatActivity {

    LinearLayout rippleContainer;
    LinearLayout logoContainer;
    TextView sloganText;
    TextView infoText;
    TextView messageTextView;

    String logoText = "Agri-Tech";
    String slogan = "Empowering Smart Agriculture";

    List<CardView> cardViews;
    Map<String, String> messages = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize views
        rippleContainer = findViewById(R.id.rippleContainer);
        logoContainer = findViewById(R.id.logoContainer);
        sloganText = findViewById(R.id.sloganText);
        TextView helloText = findViewById(R.id.helloText);
        messageTextView = findViewById(R.id.selectedLanguageMessage);
        TextView continueText = findViewById(R.id.continueText);
        ImageView arrowIcon = findViewById(R.id.arrowIcon);

        // Load animations
        Animation arrowAnim = AnimationUtils.loadAnimation(this, R.anim.arrow_wiggle);
        Animation helloAnim = AnimationUtils.loadAnimation(this, R.anim.hello_fade_slide);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.ripple_pulses);
        Animation blinkAnim = AnimationUtils.loadAnimation(this, R.anim.blink);

        arrowIcon.startAnimation(arrowAnim);
        helloText.setVisibility(View.VISIBLE);
        helloText.startAnimation(helloAnim);
        continueText.startAnimation(blinkAnim);
        rippleContainer.startAnimation(pulse);

        new Handler().postDelayed(() -> {
            continueText.setVisibility(View.VISIBLE);
            continueText.startAnimation(fadeIn);
        }, 500);

        // Start logo and slogan animation loops
        startLogoAnimationLoop();
        startSloganAnimationLoop();

        // Language card initialization
        cardViews = Arrays.asList(
                findViewById(R.id.card_english),
                findViewById(R.id.card_kannada),
                findViewById(R.id.card_tulu),
                findViewById(R.id.card_tamil),
                findViewById(R.id.card_telugu),
                findViewById(R.id.card_hindi)
        );

        // Language messages
        messages.put("English", "You chose English");
        messages.put("ಕನ್ನಡ", "ನೀವು ಕನ್ನಡವನ್ನು ಆಯ್ಕೆ ಮಾಡಿದ್ದಾರೆ");
        messages.put("ತುಳು", "ನೀನ್ ತುಳುನ ಆಯೆರ್");
        messages.put("தமிழ்", "நீங்கள் தமிழ் தேர்ந்தெடுத்துள்ளீர்கள்");
        messages.put("తెలుగు", "మీరు తెలుగు ఎంచుకున్నారు");
        messages.put("हिन्दी", "आपने हिन्दी चुनी है");

        // Set click listeners for cards
        for (CardView card : cardViews) {
            TextView textView = (TextView) card.getChildAt(0);
            card.setOnClickListener(view -> {
                String language = textView.getText().toString();
                resetCardColors();
                card.setCardBackgroundColor(Color.parseColor("#FFBB86FC"));
                messageTextView.setText(messages.get(language));
                messageTextView.setVisibility(View.VISIBLE);

                // Set language code
                String langCode = "en"; // default
                switch (language) {
                    case "ಕನ್ನಡ":
                        langCode = "kn";
                        break;
                    case "ತುಳು":
                        langCode = "tcy";
                        break;
                    case "தமிழ்":
                        langCode = "ta";
                        break;
                    case "తెలుగు":
                        langCode = "te";
                        break;
                    case "हिन्दी":
                        langCode = "hi";
                        break;
                    case "English":
                        langCode = "en";
                        break;
                }

                // Save language to SharedPreferences
                getSharedPreferences("settings", MODE_PRIVATE)
                        .edit()
                        .putString("language", langCode)
                        .apply();

                // Restart activity to apply language
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            });
        }

        // ✅ Continue to login
        View.OnClickListener redirectToLogin = v -> {
            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            String langCode = prefs.getString("language", "en"); // get selected lang

            // Save to SharedPreferences permanently
            getSharedPreferences("settings", MODE_PRIVATE)
                    .edit()
                    .putString("language", langCode)
                    .apply();

            // Wrap context and restart app flow
            MyContextWrapper.setLocale(Welcome.this, langCode);

            // Now go to login
            Intent intent = new Intent(Welcome.this, login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        };

        // ✅ Set the ripple click after it's defined
        rippleContainer.setOnClickListener(redirectToLogin);
    }

    private void resetCardColors() {
        for (CardView card : cardViews) {
            card.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");
        super.attachBaseContext(MyContextWrapper.wrap(newBase, langCode));
    }

    private void startLogoAnimationLoop() {
        Handler handler = new Handler();
        Runnable animateLogo = new Runnable() {
            @Override
            public void run() {
                logoContainer.removeAllViews();
                for (int i = 0; i < logoText.length(); i++) {
                    final int index = i;
                    handler.postDelayed(() -> {
                        TextView letterView = new TextView(Welcome.this);
                        letterView.setText(String.valueOf(logoText.charAt(index)));
                        letterView.setTextSize(36);
                        letterView.setTextColor(getResources().getColor(R.color.teal_700));
                        letterView.setTypeface(getResources().getFont(R.font.ll));
                        Animation slide = AnimationUtils.loadAnimation(Welcome.this, R.anim.slide_down);
                        letterView.startAnimation(slide);
                        logoContainer.addView(letterView);
                    }, 200 * index);
                }
                handler.postDelayed(this, logoText.length() * 200 + 2000);
            }
        };
        handler.post(animateLogo);
    }

    private void startSloganAnimationLoop() {
        Handler handler = new Handler();
        Runnable typeAndRepeat = new Runnable() {
            @Override
            public void run() {
                sloganText.setText("");
                sloganText.setVisibility(View.VISIBLE);
                for (int i = 0; i < slogan.length(); i++) {
                    final int index = i;
                    handler.postDelayed(() -> {
                        sloganText.append(String.valueOf(slogan.charAt(index)));
                    }, 100 * index);
                }
                handler.postDelayed(this, slogan.length() * 100 + 3000);
            }
        };
        handler.post(typeAndRepeat);
    }
}
