/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.shmakinv.android.widget.material.searchview.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.transitionseverywhere.TransitionValues;
import com.transitionseverywhere.Visibility;
import com.transitionseverywhere.utils.ViewUtils;

/**
 * EmptyTransition
 *
 * @author Vyacheslav Shmakin
 * @version 20.12.2015
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class EmptyTransition extends Visibility {

    private static final String LOG_TAG = "EmptyTransition";

    /**
     * Constructs a Fade transition that will fade targets in and out.
     */
    public EmptyTransition() {
    }

    /**
     * Utility method to handle creating and running the Animator.
     */
    @SuppressWarnings("SameParameterValue")
    private Animator createAnimation(@NonNull View view, int endAlpha) {
        return ObjectAnimator.ofFloat(view, ViewUtils.getAlphaProperty(), endAlpha);
    }

    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view,
                             TransitionValues startValues,
                             TransitionValues endValues) {
        if (DBG) {
            View startView = (startValues != null) ? startValues.view : null;
            Log.d(LOG_TAG, "Fade.onAppear: startView, startVis, endView, endVis = " +
                    startView + ", " + view);
        }
        return createAnimation(view, 1);
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, final View view, TransitionValues startValues,
                                TransitionValues endValues) {
        return createAnimation(view, 1);
    }

}