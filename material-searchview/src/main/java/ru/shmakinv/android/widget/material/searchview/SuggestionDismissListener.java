package ru.shmakinv.android.widget.material.searchview;

import com.transitionseverywhere.Transition;

/**
 * SuggestionDismissListener
 *
 * @author Vyacheslav Shmakin
 * @version 27.12.2015
 */
abstract class SuggestionDismissListener implements Transition.TransitionListener {

    @Override
    public void onTransitionStart(Transition transition) {

    }

    @Override
    public void onTransitionEnd(Transition transition) {
        onSuggestionDismissed();
    }

    @Override
    public void onTransitionCancel(Transition transition) {

    }

    @Override
    public void onTransitionPause(Transition transition) {

    }

    @Override
    public void onTransitionResume(Transition transition) {

    }

    public abstract void onSuggestionDismissed();
}
