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

import android.content.Context;
import android.util.AttributeSet;

import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.TransitionSet;

/**
 * Utility class for creating a default transition that automatically fades,
 * moves, and resizes views during a scene change.
 * <p/>
 * <p>An SizeTransition can be described in a resource file by using the
 * tag <code>autoTransition</code>, along with the other standard
 * attributes of {@link com.transitionseverywhere.R.styleable#Transition}.</p>
 */
public class SizeTransition extends TransitionSet {

    /**
     * Constructs an SizeTransition object, which is a TransitionSet which
     * first fades out disappearing targets, then moves and resizes existing
     * targets, and finally fades in appearing targets.
     */
    public SizeTransition() {
        init();
    }

    public SizeTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new EmptyTransition());
    }
}
