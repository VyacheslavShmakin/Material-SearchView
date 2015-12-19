package ru.shmakinv.android.widget.material.searchview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.devspark.robototextview.widget.RobotoEditText;

/**
 * ExtendedEditText
 *
 * @author: Vyacheslav Shmakin
 * @version: 23.07.2015
 */
class SearchEditText extends RobotoEditText {

    private OnBackKeyPressListener mOnBackKeyPressListener;

    public SearchEditText(Context context) {
        super(context);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mOnBackKeyPressListener != null) {
            return mOnBackKeyPressListener.OnBackKeyEvent(keyCode, event);
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnBackKeyListener(OnBackKeyPressListener onBackKeyPressListener) {
        this.mOnBackKeyPressListener = onBackKeyPressListener;
    }

    public interface OnBackKeyPressListener {
        boolean OnBackKeyEvent(int keyCode, KeyEvent event);
    }
}
