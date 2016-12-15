package ru.shmakinv.android.widget.material.searchview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * FloatingSearchView
 *
 * @author Vyacheslav Shmakin
 * @version 20.11.2016
 */

public class FloatingSearchView extends BaseFloatingSearchView {

    public static final int RECOGNIZER_CODE = 1024;

    private OnQueryTextListener mListener;

    public FloatingSearchView(Context context) {
        super(context);
    }

    public FloatingSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FloatingSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @NonNull
    public RelativeLayout getRoot() {
        return mRoot;
    }

    @NonNull
    public CardView getSearchOverlay() {
        return mSearchOverlay;
    }

    @NonNull
    public ImageButton getNavigationBackButton() {
        return mNavBackBtn;
    }

    @NonNull
    public ImageButton getCloseVoiceButton() {
        return mCloseVoiceBtn;
    }

    @NonNull
    public SearchEditText getSearchEditText() {
        return mSearchEditText;
    }

    public void setQuery(@NonNull String query, boolean submit) {
        mSearchEditText.setText(query);
        mSearchEditText.setSelection(query.length());

        if (mListener != null) {
            mListener.onQueryTextChanged(query);
            submit = submit && mListener.onQueryTextSubmit(query);
        }
        if (submit) {
            submitQuery();
        }
    }

    public void recognizeSpeech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mListener
                .onSpeechRecognitionIntentCalled()
                .startActivityForResult(intent, RECOGNIZER_CODE);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER
                && event.getAction() == KeyEvent.ACTION_UP
                && mListener != null) {

            String query = mSearchEditText.getText().toString();
            setQuery(query, true);
        }
        return false;
    }

    @Override
    protected void onCloseVoiceClicked() {
        if (mSearchEditText != null && !TextUtils.isEmpty(mSearchEditText.getText())) {
            mSearchEditText.getText().clear();
        } else if (isSpeechRecognitionToolAvailable() && mListener != null) {
            recognizeSpeech();
        }
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        this.mListener = listener;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOGNIZER_CODE && resultCode == Activity.RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && results.size() > 0) {
                mSearchEditText.setText(results.get(0));
                mSpeechRecognized = true;
            }
        }
    }

    public interface OnQueryTextListener {
        boolean onQueryTextSubmit(@NonNull String query);
        void onQueryTextChanged(@NonNull String newText);
        void onNavigationBack();
        Activity onSpeechRecognitionIntentCalled();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ibtn_voice_close) {
            onCloseVoiceClicked();
        } else if (id == R.id.ibtn_navigation_back && mListener != null) {
            mListener.onNavigationBack();
        }
    }

    @Override
    protected void onQueryChanged(@NonNull String query) {
        updateCloseVoiceState(query);
        if (mListener != null) {
            mListener.onQueryTextChanged(query);
        }
    }
}
