package io.least.ui

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import io.feeba.Feeba
import io.least.ui.app.RateAppFragment

internal fun RateAppFragment.showKeyboard(view: View) {
    val inputMethodManager =
        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.showSoftInput(view, InputMethodManager.SHOW_FORCED)
}

internal fun RateAppFragment.closeKeyboard() {
    val inputMethodManager =
        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.let { im ->
        activity?.currentFocus?.windowToken?.let { binder ->
            im.hideSoftInputFromWindow(binder, InputMethodManager.HIDE_IMPLICIT_ONLY, null)
        }
    }
}

fun dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        Feeba.appContext.resources.displayMetrics
    ).toInt()
}