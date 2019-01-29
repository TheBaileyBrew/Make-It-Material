package com.example.xyzreader.adapters;

import com.google.android.material.appbar.AppBarLayout;

public class CollapsingToolbarListener implements AppBarLayout.OnOffsetChangedListener {


    public enum State { EXPANDED, COLLAPSED, IDLE}

    protected State mCurrentState = State.IDLE;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {

        if (i == 0) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED);
            }
            mCurrentState = State.EXPANDED;
        } else if (Math.abs(i) == appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED);
            }
            mCurrentState = State.COLLAPSED;
        } else {
            if (mCurrentState != State.IDLE) {
                onStateChanged(appBarLayout, State.IDLE);
            }
            mCurrentState = State.IDLE;
        }
    }

    protected void onStateChanged(AppBarLayout appBarLayout, State state) {

    }
}
