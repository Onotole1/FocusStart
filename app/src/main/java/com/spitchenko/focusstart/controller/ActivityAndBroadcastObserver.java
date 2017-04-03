package com.spitchenko.focusstart.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;

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
    void updateOnReceiveItems(final ArrayList<?> items, final String action);
    void updateOnRestoreInstanceState(final Bundle savedInstanceState);
    void updateOnSavedInstanceState(final Bundle outState);
}
