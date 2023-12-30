package io.feeba.ui

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import io.feeba.data.SurveyPresentation
import io.feeba.data.state.Defaults

//Create a custom view SurveyView that extends FrameLayout
class SurveyView : FrameLayout {

    //Create a constructor that takes in a context
    constructor(context: Context) : super(context) {
        //Call the init function
        init()
    }

    //Create a constructor that takes in a context and an attribute set
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        //Call the init function
        init()
    }

    //Create a constructor that takes in a context, an attribute set and a style
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        //Call the init function
        init()
    }

    //Create a constructor that takes in a context, an attribute set, a style and a default style
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        //Call the init function
        init()
    }

    //Create a function called init
    private fun init() {
        val appHistoryState = Defaults.appHistoryState
        val surveyUrl: String = "https://dev-dashboard.feeba.io/s/feeba/65716a0c87713b82c209e522"
        addView(createWebViewInstanceUrl(context as Activity, surveyUrl, appHistoryState))
        isFocusableInTouchMode = true
        requestFocus()
    }
}