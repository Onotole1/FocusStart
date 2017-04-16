package com.spitchenko.focusstart.base.controller;

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
    private static final String SEPARATOR = "\\.";
    private static final String DOT = ".";
    private static final int BEGIN_NAME_POSITION = 2;
    protected abstract void saveLocalThemeIdToPrefs(final int themeId);

    protected int readThemeIdOrNullFromPrefs(@NonNull final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        final String themeIdString = preferences.getString(
                context.getString(R.string.settings_fragment_theme_list), null);
        if (null != themeIdString) {
            final String[] themeIdStringDotArray = themeIdString.split(SEPARATOR);
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
        for (int i = BEGIN_NAME_POSITION; i < input.length; i++) {
            if (i != BEGIN_NAME_POSITION) {
                builder.append(DOT);
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
