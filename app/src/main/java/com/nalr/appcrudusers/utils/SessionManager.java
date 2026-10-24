package com.nalr.appcrudusers.utils;

import android.content.Context;

public final class SessionManager {
    private static final String PREFS = "session";
    private static final String TOKEN = "access_token";

    private SessionManager() {
    }

    public static void save(Context context, String token) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(TOKEN, token)
                .apply();
    }

    public static String token(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(TOKEN, null);
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(TOKEN)
                .apply();
    }
}
