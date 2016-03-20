package ru.shmakinv.android.widget.material.searchview;

import android.animation.Animator;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;

import com.devspark.robototextview.util.RobotoTypefaceManager;

import java.util.List;

/**
 * BaseRestoreInstanceFragment
 *
 * @author Vyacheslav Shmakin
 * @version 01.01.2016
 */
abstract class BaseRestoreInstanceFragment extends DialogFragment {

    private static final String REQUEST_CLOSE_INSTANCE = "ru.shmakinv.android.widget.material.searchview.BaseRestoreInstanceFragment.flag.closeRequested";
    private static final String VISIBILITY_INSTANCE = "ru.shmakinv.android.widget.material.searchview.BaseRestoreInstanceFragment.flag.visible";
    private static final String QUERY_INSTANCE = "ru.shmakinv.android.widget.material.searchview.BaseRestoreInstanceFragment.text.query";
    private static final String HINT_INSTANCE = "ru.shmakinv.android.widget.material.searchview.BaseRestoreInstanceFragment.text.hint";
    private static final String CURSOR_POSITION_INSTANCE = "ru.shmakinv.android.widget.material.searchview.BaseRestoreInstanceFragment.position.cursor";
    private static final String TYPEFACE_INSTANCE = "ru.shmakinv.android.widget.material.searchview.BaseRestoreInstanceFragment.typeface.value";
    private static final long ANIMATOR_DURATION_SHOW = 300L;
    protected static final String DIALOG_TAG = "ru.shmakinv.android.widget.material.searchview.BaseRestoreInstanceFragment.tag";
    protected static final long ANIMATOR_MIN_SUGGESTION_DURATION = 50L;
    protected static final long ANIMATOR_MAX_SUGGESTION_DURATION = 200L;

    protected Integer mMenuItemId = null;
    private float mAnimProportionX = -1;

    protected boolean mVisible = false;
    protected boolean mCloseRequested = false;
    protected boolean mCloseAnimationRunning = false;

    protected String mQuery = null;
    protected String mHint = null;
    protected int mSelection = -1;
    protected int mTypefaceValue = RobotoTypefaceManager.Typeface.ROBOTO_REGULAR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            this.mVisible = savedInstanceState.getBoolean(VISIBILITY_INSTANCE);
            this.mCloseRequested = savedInstanceState.getBoolean(REQUEST_CLOSE_INSTANCE);
            this.mQuery = savedInstanceState.getString(QUERY_INSTANCE, null);
            this.mHint = savedInstanceState.getString(HINT_INSTANCE, null);
            this.mSelection = savedInstanceState.getInt(CURSOR_POSITION_INSTANCE, mSelection);
            this.mTypefaceValue = savedInstanceState.getInt(TYPEFACE_INSTANCE, mTypefaceValue);
        }
    }

    public void show(@NonNull final FragmentManager manager) {
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment prev = manager.findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            transaction.remove(prev);
        }

        transaction.add(this, DIALOG_TAG);
        transaction.commitAllowingStateLoss();
        manager.executePendingTransactions();
    }

    protected void setupTypeface(int typefaceValue) {
        this.mTypefaceValue = typefaceValue;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(VISIBILITY_INSTANCE, this.mVisible);
        outState.putBoolean(REQUEST_CLOSE_INSTANCE, this.mCloseRequested);
        outState.putString(QUERY_INSTANCE, this.mQuery);
        outState.putString(HINT_INSTANCE, this.mHint);
        outState.putInt(CURSOR_POSITION_INSTANCE, this.mSelection);
        outState.putInt(TYPEFACE_INSTANCE, this.mTypefaceValue);
    }

    @Override
    public void onDestroyView() {
        // removes the dismiss intent to avoid shown dialogs being dismissed.
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        this.mVisible = false;
        this.mCloseRequested = false;
        this.mQuery = null;
        this.mHint = null;
        this.mSelection = -1;
        this.mTypefaceValue = RobotoTypefaceManager.Typeface.ROBOTO_REGULAR;
    }

    public boolean isShown() {
        return mVisible;
    }

    protected void animateShow(@NonNull final View view, @NonNull final View metricsView, final int itemId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int centerX = calculateCenterX(itemId);
            int centerY = (metricsView.getHeight() / 2) - lp.topMargin;
            int endRadius = metricsView.getWidth();

            final Animator animatorShow = ViewAnimationUtils.createCircularReveal(
                    view,
                    centerX,
                    centerY,
                    0.0F,
                    endRadius);

            animatorShow.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                    showKeyboard();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    onShowSearchAnimationEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

            animatorShow.setDuration(ANIMATOR_DURATION_SHOW);
            animatorShow.setInterpolator(new LinearInterpolator());
            animatorShow.start();
        } else {
            view.setVisibility(View.VISIBLE);
            showKeyboard();
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            onShowSearchAnimationEnd();
        }
    }

    protected void animateDismiss(final View view, final View metricsView, final int itemId) {
        if (!mCloseAnimationRunning) {
            mCloseAnimationRunning = true;
        } else {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int centerX = calculateCenterX(itemId);
            int centerY = (metricsView.getHeight() / 2) - lp.topMargin;
            int startRadius = metricsView.getWidth();

            final Animator animatorHide = ViewAnimationUtils.createCircularReveal(
                    view,
                    centerX,
                    centerY,
                    startRadius,
                    0.0F);

            animatorHide.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    finishClosing(view);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animatorHide.setInterpolator(new LinearInterpolator());
            animatorHide.setDuration(ANIMATOR_DURATION_SHOW);
            animatorHide.start();
        } else {
            finishClosing(view);
        }
    }

    protected void finishClosing(View view) {
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        hideKeyboard();
        view.setVisibility(View.INVISIBLE);
        mCloseRequested = false;
        mCloseAnimationRunning = false;
        dismissAllowingStateLoss();
    }

    protected void onShowSearchAnimationEnd() {
    }

    protected void onSuggestionsDismissed() {
    }

    protected void onQueryTextChanged(Editable s) {
    }

    protected boolean isSpeechRecognitionToolAvailable() {
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() != 0;
    }

    protected final NotAllowedToEditCallBack mNotAllowedToEditCallback = new NotAllowedToEditCallBack();

    protected final SuggestionDismissListener mSuggestionsDismissListener = new SuggestionDismissListener() {
        @Override
        public void onSuggestionDismissed() {
            onSuggestionsDismissed();
        }
    };

    protected final QueryTextWatcher mSearchTextWatcher = new QueryTextWatcher() {
        @Override
        public void onQueryTextChanged(Editable s) {
            BaseRestoreInstanceFragment.this.onQueryTextChanged(s);
        }
    };

    protected void onCloseVoiceClicked() {
    }

    protected final View.OnClickListener mCloseVoiceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onCloseVoiceClicked();
        }
    };

    protected void onNavigationBackClicked() {
    }

    protected final View.OnClickListener mNavigationBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onNavigationBackClicked();
        }
    };

    protected void onOutsideTouch(View v, MotionEvent event) {
    }

    protected final View.OnTouchListener mOnOutsideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            onOutsideTouch(v, event);
            return false;
        }
    };

    private float calculateProportionX(int xPosition) {
        return (float) xPosition / getDisplayWidth();
    }

    private int getDisplayWidth() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private int calculateCenterX(int itemId) {
        View view = getActivity().findViewById(itemId);
        //3. Calculate Animation Position:
        mAnimProportionX = calculateProportionX(getAnimationPositionX(view));
        return (int) (mAnimProportionX * getDisplayWidth());
    }

    private int getAnimationPositionX(View view) {
        // If view is null then will be used current screen width parameter
        if (view == null) {
            return getDisplayWidth();
        }

        int[] position = new int[2];
        view.getLocationInWindow(position);
        // x = View.x + View.width / 2
        // y = View.y + View.height / 2
        return position[0] + view.getWidth() / 2;
    }

    void showKeyboard() {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    void hideKeyboard() {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}
