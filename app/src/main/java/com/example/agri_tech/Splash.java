package com.example.agri_tech;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class Splash extends Activity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // This should be your splash layout

        // Media
        mediaPlayer = MediaPlayer.create(this, R.raw.agritech_tone);
        mediaPlayer.start();

        // UI References
        FrameLayout ripple1 = findViewById(R.id.ripple1);
        FrameLayout ripple2 = findViewById(R.id.ripple2);
        FrameLayout ripple3 = findViewById(R.id.ripple3);
        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.app_name);
        TypeWriter slogan = findViewById(R.id.slogan);
        TextView developedBy = findViewById(R.id.developed_by);

        // Start ripple animations
        startRipple(ripple1, 0);
        startRipple(ripple2, 600);
        startRipple(ripple3, 1000);

        // App name text animation
        new Handler().postDelayed(() -> animateText(appName, "AGRI-TECH", 350), 2000);

        // Show logo with zoom/fade
        new Handler().postDelayed(() -> {
            logo.setVisibility(View.VISIBLE);
            appName.setVisibility(View.VISIBLE);

            AnimationSet logoAnim = new AnimationSet(true);
            logoAnim.addAnimation(getZoomInAnimation());
            logoAnim.addAnimation(getFadeInAnimation());

            logo.startAnimation(logoAnim);
            appName.startAnimation(getFadeInAnimation());

            // Ripple around logo
            new Handler().postDelayed(() -> {
                AnimationSet rippleAnim = new AnimationSet(true);
                rippleAnim.addAnimation(getBounceAnimation());
                rippleAnim.addAnimation(getAlphaPulse());
                logo.startAnimation(rippleAnim);
            }, 1000); // after initial zoom-in finishes

        }, 3000);

        // Slogan typewriter effect
        new Handler().postDelayed(() -> {
            slogan.setVisibility(View.VISIBLE);
            slogan.setCharacterDelay(80);
            slogan.animateText("Empowering Smart Agriculture");
        }, 4200);

        // Developer credits animation
        new Handler().postDelayed(() -> {
            developedBy.setVisibility(View.VISIBLE);
            animateText(developedBy, "Developed by Fab Four", 120);
        }, 4800);

        // Navigate to Welcome screen
        // Navigate to Welcome screen
        new Handler().postDelayed(() -> {

            logo.animate()
                    .translationY(-300f)
                    .setDuration(800)
                    .setInterpolator(new AccelerateDecelerateInterpolator())

                    .withEndAction(() -> {
                        Intent intent = new Intent(Splash.this, Welcome.class);

                        // Shared elements for transition
                        Pair<View, String>[] pairs = new Pair[2];
                        pairs[0] = new Pair<>(logo, "logo_image");
                        pairs[1] = new Pair<>(appName, "logo_text");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                                    Splash.this,
                                    pairs
                            );
                            startActivity(intent, options.toBundle());
                        } else {
                            startActivity(intent);
                        }

                        finish();
                    })
                    .start();

        }, 8000);

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");
        super.attachBaseContext(MyContextWrapper.wrap(newBase, langCode));
    }

    private void startRipple(View view, long delay) {
        ScaleAnimation scale = new ScaleAnimation(
                0.1f, 15f, 0.1f, 15f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(1200);
        scale.setStartOffset(delay);
        scale.setFillAfter(true);

        AlphaAnimation fade = new AlphaAnimation(0.8f, 0.0f);
        fade.setDuration(1200);
        fade.setStartOffset(delay);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scale);
        set.addAnimation(fade);

        view.setVisibility(View.VISIBLE);
        view.startAnimation(set);
    }

    private Animation getZoomInAnimation() {
        ScaleAnimation zoomIn = new ScaleAnimation(
                0.7f, 1f, 0.7f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        zoomIn.setDuration(1000);
        zoomIn.setFillAfter(true);
        return zoomIn;
    }

    private Animation getFadeInAnimation() {
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        return fadeIn;
    }

    private Animation getBounceAnimation() {
        ScaleAnimation bounce = new ScaleAnimation(
                1f, 1.2f, 1f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        bounce.setDuration(1500);
        bounce.setRepeatMode(Animation.REVERSE);
        bounce.setRepeatCount(Animation.INFINITE);
        return bounce;
    }

    private Animation getAlphaPulse() {
        AlphaAnimation alpha = new AlphaAnimation(1f, 0.7f);
        alpha.setDuration(1500);
        alpha.setRepeatMode(Animation.REVERSE);
        alpha.setRepeatCount(Animation.INFINITE);
        return alpha;
    }

    private void animateText(final TextView textView, final String text, final long delay) {
        final Handler handler = new Handler();
        textView.setText("");
        textView.setVisibility(View.VISIBLE);

        for (int i = 0; i < text.length(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                textView.append(String.valueOf(text.charAt(index)));
                Animation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(300);
                textView.startAnimation(fadeIn);
            }, delay * i);
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
