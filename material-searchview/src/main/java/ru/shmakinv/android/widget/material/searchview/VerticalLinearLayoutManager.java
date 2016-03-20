package ru.shmakinv.android.widget.material.searchview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * WrapContentLinearLayoutManager
 *
 * @author Vyacheslav Shmakin
 * @version 01.01.2016
 */
class VerticalLinearLayoutManager extends LinearLayoutManager {

    private int mChildHeight;

    public VerticalLinearLayoutManager(Context context) {
        super(context, LinearLayoutManager.VERTICAL, false);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
        mChildHeight = View.MeasureSpec.getSize(heightSpec);
    }

    public int getChildHeight() {
        return mChildHeight;
    }
}
