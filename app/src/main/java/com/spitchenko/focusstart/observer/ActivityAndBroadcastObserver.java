package com.spitchenko.focusstart.observer;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 31.03.17
 * Time: 18:46
 *
 * @author anatoliy
 */
public interface ActivityAndBroadcastObserver {
    void updateOnCreate(@Nullable Bundle savedInstanceState);
    void updateOnResume();
    void updateOnPause();
    void updateOnReceiveItems(@Nullable final ArrayList<?> items, @Nullable final String action);
    void updateOnRestoreInstanceState(@NonNull final Bundle savedInstanceState);
    void updateOnSavedInstanceState(@NonNull final Bundle outState);
}
