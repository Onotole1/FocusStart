package com.spitchenko.focusstart.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.spitchenko.focusstart.R;

import lombok.NonNull;

/**
 * Date: 08.04.17
 * Time: 11:10
 *
 * @author anatoliy
 */
public abstract class BaseActivityController {
    protected abstract void saveLocalThemeIdToPrefs(final int themeId);

    protected int readThemeIdOrNullFromPrefs(@NonNull final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        final String themeIdString = preferences.getString(
                context.getString(R.string.settings_fragment_theme_list), null);
        if (null != themeIdString) {
            final String[] themeIdStringDotArray = themeIdString.split("\\.");
            if (themeIdStringDotArray.length > 1) {
                final String resourceName = themeIdStringDotArray[1];
                final String themeName = getThemeName(themeIdStringDotArray);

                return context.getResources().getIdentifier(themeName, resourceName
                        , context.getPackageName());
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private String getThemeName(@NonNull final String[] input) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 2; i < input.length; i++) {
            if (i != 2) {
                builder.append(".");
            }
            builder.append(input[i]);
        }
        return builder.toString();
    }

    protected abstract int readLocalThemeIdFromPrefs();

    protected void setThemeFromPrefs(@NonNull final Context context) {
        final int themeId = readThemeIdOrNullFromPrefs(context);
        if (0 != themeId) {
            context.setTheme(themeId);
            saveLocalThemeIdToPrefs(themeId);
        }
    }
}
