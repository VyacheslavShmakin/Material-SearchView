package ru.shmakinv.android.widget.material.searchview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * BaseFixedSearchView
 *
 * @author Vyacheslav Shmakin
 * @version 20.11.2016
 */

abstract class BaseFloatingSearchView extends FrameLayout implements
        View.OnKeyListener,
        View.OnClickListener {

    private final NotAllowedToEditCallBack mNotAllowedToEditCallback = new NotAllowedToEditCallBack();

    protected RelativeLayout mRoot;
    protected CardView mSearchOverlay;
    protected ImageButton mNavBackBtn;
    protected ImageButton mCloseVoiceBtn;
    protected SearchEditText mSearchEditText;

    protected boolean mSpeechRecognized = false;

    public BaseFloatingSearchView(Context context) {
        super(context);
        init(context, null);
    }

    public BaseFloatingSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BaseFloatingSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseFloatingSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.layout_view_search_view, this);
        initViews(this);
    }

    private void initViews(@NonNull View layout) {
        mRoot = (RelativeLayout) layout.findViewById(R.id.root);
        mSearchOverlay = (CardView) layout.findViewById(R.id.search_overlay);
        mNavBackBtn = (ImageButton) layout.findViewById(R.id.ibtn_navigation_back);
        mCloseVoiceBtn = (ImageButton) layout.findViewById(R.id.ibtn_voice_close);
        mSearchEditText = (SearchEditText) layout.findViewById(R.id.et_search_text);

        setUpListeners();
    }

    private void setUpListeners() {
        mSearchEditText.addTextChangedListener(mSearchTextWatcher);
        mSearchEditText.setOnKeyListener(this);
        mCloseVoiceBtn.setOnClickListener(this);
        mNavBackBtn.setOnClickListener(this);
        mSearchEditText.setCustomSelectionActionModeCallback(mNotAllowedToEditCallback);
    }

    protected void updateCloseVoiceState(@Nullable String searchText) {
        if (searchText != null && searchText.length() != 0) {
            mCloseVoiceBtn.setVisibility(View.VISIBLE);
            mCloseVoiceBtn.setImageResource(R.drawable.searchview_button_close);
        } else if (!isSpeechRecognitionToolAvailable()) {
            mCloseVoiceBtn.setVisibility(View.INVISIBLE);
        } else {
            mCloseVoiceBtn.setImageResource(R.drawable.searchview_button_voice);
        }
    }

    protected abstract void onCloseVoiceClicked();

    public abstract void setQuery(@NonNull String query, boolean submit);

    protected abstract void onQueryChanged(@NonNull String query);

    protected void submitQuery() {
        mSearchEditText.clearFocus();
        hideKeyboard();
    }

    protected boolean isSpeechRecognitionToolAvailable() {
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() != 0;
    }

    protected void hideKeyboard() {
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    protected final QueryTextWatcher mSearchTextWatcher = new QueryTextWatcher() {
        @Override
        public void onQueryTextChanged(Editable s) {
            onQueryChanged(s.toString());
        }
    };

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.value = mSearchEditText.getText().toString();
        ss.speechRecognized = this.mSpeechRecognized;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mSearchEditText.setText(ss.value);
        mSpeechRecognized = ss.speechRecognized;
    }

    static class SavedState extends BaseSavedState {
        String value;
        boolean speechRecognized;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.value = in.readString();
            this.speechRecognized = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(this.value);
            out.writeInt(this.speechRecognized ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
