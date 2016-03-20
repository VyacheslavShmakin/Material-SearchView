package ru.shmakinv.android.widget.material.searchview;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.devspark.robototextview.util.RobotoTypefaceManager;
import com.devspark.robototextview.util.RobotoTypefaceUtils;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import java.util.List;

import ru.shmakinv.android.widget.material.searchview.transition.SizeTransition;

/**
 * SearchView
 *
 * @author Vyacheslav Shmakin
 * @version 20.03.2016
 */
public class SearchView extends BaseRestoreInstanceFragment implements
        DialogInterface.OnShowListener,
        SearchEditText.OnBackKeyPressListener,
        View.OnKeyListener {

    public static final int RECOGNIZER_CODE = 100500;
    private static final long SPEECH_RECOGNITION_DELAY = 300L;

    private RelativeLayout mRoot;
    private RelativeLayout mSearchRegion;
    private LinearLayout mSuggestionsRegion;
    private CardView mSearchOverlay;
    private ImageButton mNavBackBtn;
    private ImageButton mCloseVoiceBtn;
    private SearchEditText mSearchEditText;
    private RecyclerView mSuggestionsView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.ItemDecoration mDecoration;

    private OnQueryTextListener mOnQueryTextListener;
    private OnVisibilityChangeListener mOnVisibilityChangeListener;

    private boolean mShowSearchAnimationFinished = false;
    private boolean mSpeechRecognized = false;

    @NonNull
    public static SearchView getInstance(@NonNull Activity activity) {
        SearchView searchView = (SearchView) activity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        return searchView != null ? searchView : new SearchView();
    }

    public SearchView() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View dialogLayout = inflater.inflate(R.layout.search_view_layout, container, true);
        initViews(dialogLayout);
        initWindowParams();
        return dialogLayout;
    }

    private void initViews(@NonNull final View layout) {
        mRoot = (RelativeLayout) layout.findViewById(R.id.root);
        mSearchOverlay = (CardView) layout.findViewById(R.id.search_overlay);
        mNavBackBtn = (ImageButton) layout.findViewById(R.id.ibtn_navigation_back);
        mCloseVoiceBtn = (ImageButton) layout.findViewById(R.id.ibtn_voice_close);
        mSearchEditText = (SearchEditText) layout.findViewById(R.id.et_search_text);
        mSearchRegion = (RelativeLayout) layout.findViewById(R.id.search_region);
        mSuggestionsRegion = (LinearLayout) layout.findViewById(R.id.suggestions_region);
        mSuggestionsView = (RecyclerView) layout.findViewById(R.id.suggestion_list);
        if (mAdapter != null) {
            mSuggestionsView.setAdapter(mAdapter);
        }
        mSuggestionsView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity()));

        if (mDecoration != null) {
            mSuggestionsView.removeItemDecoration(mDecoration);
            mSuggestionsView.addItemDecoration(mDecoration);
        }

        Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                getActivity().getApplicationContext(),
                this.mTypefaceValue);
        RobotoTypefaceUtils.setUp(mSearchEditText, typeface);

        mSearchEditText.setText(mQuery);
        mSearchEditText.setHint(mHint);
        if (mSelection == -1) {
            if (mQuery != null) {
                mSearchEditText.setSelection(mQuery.length());
            }
        } else {
            mSearchEditText.setSelection(mSelection);
        }
    }

    private void initWindowParams() {
        Dialog dialog = getDialog();
        Window window = null;
        if (dialog != null) {
            window = dialog.getWindow();
        }

        if (dialog == null || window == null) {
            return;
        }

        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(new ColorDrawable());
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        params.x = 0;
        params.y = 0;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.windowAnimations = R.style.NoAnimationWindow;

        window.setAttributes(params);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(this);
    }

    @Override
    public void onShow(DialogInterface dialog) {
        if (!mVisible) {
            mVisible = true;
            if (mOnVisibilityChangeListener != null) {
                mOnVisibilityChangeListener.onShow();
            }
        }
        animateShow(mSearchOverlay, mSearchRegion, mMenuItemId);
    }

    @Override
    public void dismiss() {
        onClose();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        this.mSearchEditText.getText().clear();
        this.mAdapter = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchEditText.addTextChangedListener(mSearchTextWatcher);
        mSearchEditText.setOnBackKeyListener(this);
        mSearchEditText.setOnKeyListener(this);
        mCloseVoiceBtn.setOnClickListener(mCloseVoiceClickListener);
        mNavBackBtn.setOnClickListener(mNavigationBackClickListener);
        mSearchEditText.setCustomSelectionActionModeCallback(mNotAllowedToEditCallback);
        setUpDialogTouchListener(mOnOutsideTouchListener);

        updateCloseVoiceState(mQuery);
        if (mSpeechRecognized) {
            mSpeechRecognized = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setQuery(mQuery, true);
                }
            }, SPEECH_RECOGNITION_DELAY);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSearchEditText.setOnKeyListener(null);
        mSearchEditText.removeTextChangedListener(mSearchTextWatcher);
        mSearchEditText.setOnBackKeyListener(null);
        mCloseVoiceBtn.setOnClickListener(null);
        mNavBackBtn.setOnClickListener(null);
        mSearchEditText.setCustomSelectionActionModeCallback(null);
        setUpDialogTouchListener(null);
    }

    @Nullable
    private View getDecorView() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                return window.getDecorView();
            }
        }
        return null;
    }

    private void setUpDialogTouchListener(View.OnTouchListener listener) {
        View view = getDecorView();
        if (view != null) {
            view.setOnTouchListener(listener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mSelection = mSearchEditText.getSelectionStart();
        super.onSaveInstanceState(outState);
    }

    private void onClose() {
        if (mOnVisibilityChangeListener != null) {
            mOnVisibilityChangeListener.onDismiss();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && mAdapter != null && mAdapter.getItemCount() > 0) {

            TransitionSet transition = new SizeTransition()
                    .setDuration(ANIMATOR_MIN_SUGGESTION_DURATION)
                    .setInterpolator(new LinearInterpolator())
                    .addListener(mSuggestionsDismissListener);

            TransitionManager.beginDelayedTransition(mSearchOverlay, transition);

            mSuggestionsRegion.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    0));
        } else {
            animateDismiss(mSearchOverlay, mSearchRegion, mMenuItemId);
        }
    }

    @Override
    public boolean OnBackKeyEvent(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public void show(@NonNull FragmentManager manager) {
        if (isShown()) {
            dismissAllowingStateLoss();
        }
        super.show(manager);
    }

    public void setTypeface(int typefaceValue) {
        setupTypeface(typefaceValue);

        Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                getActivity().getApplicationContext(),
                typefaceValue);

        if (mSearchEditText != null) {
            RobotoTypefaceUtils.setUp(mSearchEditText, typeface);
        }
    }

    @SuppressWarnings("SameParameterValue")
    public void setSuggestionAdapter(@NonNull RecyclerView.Adapter adapter) {
        this.mAdapter = adapter;
        if (mSuggestionsView != null) {
            mSuggestionsView.setAdapter(this.mAdapter);
            if (isShown() && mShowSearchAnimationFinished) {
                onShowSearchAnimationEnd();
            }
        }
    }

    public void addItemDecoration(@NonNull RecyclerView.ItemDecoration decoration) {
        if (mSuggestionsView != null) {
            if (this.mDecoration != null) {
                mSuggestionsView.removeItemDecoration(this.mDecoration);
            }
            mSuggestionsView.addItemDecoration(decoration);
        }
        this.mDecoration = decoration;
    }

    @Nullable
    public RecyclerView.ItemDecoration getItemDecoration() {
        return this.mDecoration;
    }

    @Nullable
    public RecyclerView.Adapter getSuggestionAdapter() {
        return this.mAdapter;
    }

    public void setQuery(int id) {
        setQuery(getString(id));
    }

    public void setQuery(@NonNull String query) {
        setQuery(query, false);
    }

    public void setQuery(@NonNull String query, boolean submit) {
        this.mQuery = query;
        this.mSelection = query.length();
        if (mSearchEditText == null) {
            return;
        }
        mSearchEditText.setText(query);
        mSearchEditText.setSelection(mSelection);

        if (mOnQueryTextListener != null) {
            mOnQueryTextListener.onQueryTextChanged(query);
            submit = submit && mOnQueryTextListener.onQueryTextSubmit(query);
        }
        if (submit) {
            submitQuery();
        }
    }

    @Nullable
    public String getQuery() {
        return mQuery;
    }

    @Nullable
    public String getHint() {
        if (mSearchEditText == null) {
            return mHint;
        }
        return mSearchEditText.getHint().toString();
    }

    public void setHint(int id) {
        setHint(getString(id));
    }

    public void setHint(@NonNull String hint) {
        this.mHint = hint;
        if (mSearchEditText != null) {
            mSearchEditText.setHint(hint);
        }
    }

    @Override
    protected void animateShow(@NonNull View view, @NonNull View metricsView, int itemId) {
        mShowSearchAnimationFinished = false;
        super.animateShow(view, metricsView, itemId);
    }

    @Override
    protected void onShowSearchAnimationEnd() {
        mShowSearchAnimationFinished = true;

        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            mSuggestionsView.setAdapter(mAdapter);

            int itemHeight = ((WrapContentLinearLayoutManager) mSuggestionsView.getLayoutManager()).getChildHeight();
            int itemCount = mAdapter.getItemCount();

            long duration = Math.max(
                    ANIMATOR_MIN_SUGGESTION_DURATION,
                    Math.min(ANIMATOR_MAX_SUGGESTION_DURATION, itemCount * itemHeight));

            TransitionSet transition = new SizeTransition()
                    .setDuration(duration)
                    .setInterpolator(new LinearInterpolator());

            TransitionManager.beginDelayedTransition(mRoot, transition);

            mSuggestionsRegion.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    protected void onSuggestionsDismissed() {
        animateDismiss(mSearchOverlay, mSearchRegion, mMenuItemId);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER
                && event.getAction() == KeyEvent.ACTION_UP
                && mOnQueryTextListener != null) {

            String query = mSearchEditText.getText().toString();
            setQuery(query, true);
        }
        return false;
    }

    @Override
    protected void onQueryTextChanged(@NonNull Editable s) {
        mQuery = s.toString();
        updateCloseVoiceState(mQuery);

        if (mOnQueryTextListener != null) {
            mOnQueryTextListener.onQueryTextChanged(mQuery);
        }
    }

    private void updateCloseVoiceState(@Nullable String searchText) {
        if (searchText != null && searchText.length() != 0) {
            mCloseVoiceBtn.setVisibility(View.VISIBLE);
            mCloseVoiceBtn.setImageResource(R.drawable.searchview_button_close);
        } else if (!isSpeechRecognitionToolAvailable()) {
            mCloseVoiceBtn.setVisibility(View.INVISIBLE);
        } else {
            mCloseVoiceBtn.setImageResource(R.drawable.searchview_button_voice);
        }
    }

    @Override
    protected void onCloseVoiceClicked() {
        if (mSearchEditText != null && mSearchEditText.getText() != null && mSearchEditText.getText().toString().length() != 0) {
            mSearchEditText.getText().clear();
            mQuery = "";
        } else if (isSpeechRecognitionToolAvailable()) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, RECOGNIZER_CODE);
        }
    }

    @Override
    protected void onNavigationBackClicked() {
        onClose();
    }

    @Override
    protected void onOutsideTouch(@NonNull View v, @NonNull MotionEvent event) {
        if (!mCloseRequested) {
            Rect rect = new Rect();
            mSearchOverlay.getHitRect(rect);

            if (!rect.contains((int) event.getX(), (int) event.getY())) {
                mCloseRequested = true;
                onClose();
            }
        }
    }

    public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
        this.mOnVisibilityChangeListener = listener;
    }

    public interface OnVisibilityChangeListener {
        void onShow();
        void onDismiss();
    }

    @SuppressWarnings("SameReturnValue")
    public boolean onOptionsItemSelected(FragmentManager manager, MenuItem item) {
        this.mMenuItemId = item.getItemId();
        show(manager);
        return true;
    }

    public interface OnQueryTextListener {
        boolean onQueryTextSubmit(@NonNull String query);
        void onQueryTextChanged(@NonNull String newText);
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        this.mOnQueryTextListener = listener;
    }

    private void submitQuery() {
        setSuggestionAdapter(null);
        mSearchEditText.clearFocus();
        hideKeyboard();
        dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOGNIZER_CODE && resultCode == Activity.RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && results.size() > 0) {
                mQuery = results.get(0);
                mSpeechRecognized = true;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}