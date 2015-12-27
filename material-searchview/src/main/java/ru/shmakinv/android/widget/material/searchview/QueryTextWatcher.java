package ru.shmakinv.android.widget.material.searchview;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * QueryTextWatcher
 *
 * @author: Vyacheslav Shmakin
 * @version: 27.12.2015
 */
abstract class QueryTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        onQueryTextChanged(s);
    }

    public abstract void onQueryTextChanged(Editable s);
}
