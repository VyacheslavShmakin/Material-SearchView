package ru.shmakinv.android.widget.material.searchview.transition;

import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionValues;

/**
 * EmptyTransition
 *
 * @author Vyacheslav Shmakin
 * @version 19.04.2017
 */

public class EmptyTransition extends Transition {

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        // ignore
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        // ignore
    }

}
