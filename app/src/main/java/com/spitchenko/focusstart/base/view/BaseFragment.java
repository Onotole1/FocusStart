package com.spitchenko.focusstart.base.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.spitchenko.focusstart.observer.FragmentAndBroadcastObserver;

import java.util.ArrayList;
/**
 * Date: 29.04.17
 * Time: 13:36
 *
 * @author anatoliy
 */
public abstract class BaseFragment extends Fragment {
    private final static int NUMBER_OBSERVERS = 1;
    private final ArrayList<FragmentAndBroadcastObserver> observers = new ArrayList<>(NUMBER_OBSERVERS);

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notifyObserversOnCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        notifyObserversOnResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        notifyObserversOnPause();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        notifyObserversOnSaveInstanceState(outState);
    }

    private void notifyObserversOnSaveInstanceState(final Bundle state) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final FragmentAndBroadcastObserver observer = observers.get(i);
            observer.updateOnSavedInstanceState(state);
        }
    }

    @Override
    public void onViewStateRestored(final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        notifyObserversOnViewStateRestored(savedInstanceState);
    }

    protected void addObserver(final FragmentAndBroadcastObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    protected void removeObserver(final FragmentAndBroadcastObserver observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    protected void notifyObserversOnCreateView(final View view) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final FragmentAndBroadcastObserver observer = observers.get(i);
            observer.updateOnCreateView(view);
        }
    }

    private void notifyObserversOnCreate(final Bundle state) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final FragmentAndBroadcastObserver observer = observers.get(i);
            observer.updateOnCreate(state);
        }
    }

    private void notifyObserversOnResume() {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final FragmentAndBroadcastObserver observer = observers.get(i);
            observer.updateOnResume();
        }
    }

    private void notifyObserversOnPause() {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final FragmentAndBroadcastObserver observer = observers.get(i);
            observer.updateOnPause();
        }
    }

    /*private void notifyObserversOnSaveInstanceState(final Bundle outState) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final FragmentAndBroadcastObserver observer = observers.get(i);
            observer.updateOnSaveInstanceState(outState);
        }
    }*/

    private void notifyObserversOnViewStateRestored(final Bundle savedInstanceState) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final FragmentAndBroadcastObserver observer = observers.get(i);
            observer.updateOnRestoreInstanceState(savedInstanceState);
        }
    }
}
