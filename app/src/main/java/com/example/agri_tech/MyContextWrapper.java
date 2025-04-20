package com.example.agri_tech;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class MyContextWrapper {

    public static Context wrap(Context context, String language) {
        Locale newLocale = new Locale(language);
        Locale.setDefault(newLocale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(newLocale);
        config.setLayoutDirection(newLocale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        } else {
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            return context;
        }
    }

    public static void setLocale(Context context, String language) {
        wrap(context, language);
    }
}
