package sample.integrated

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import io.feeba.data.state.Defaults
import io.feeba.ui.createWebViewInstance
import io.least.demo.databinding.DialogIntegratedSurveyBinding


class InViewIntegratedSurvey() : Fragment() {

    private var _binding: DialogIntegratedSurveyBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogIntegratedSurveyBinding.inflate(inflater, container, false)
        binding.surveyViewBlitz.loadSurvey("https://dev-sv.feeba.io/s/feeba/65a381db081d06ce889dfd09")

//        binding.container.addView(createWebViewInstance(context as Activity, Defaults.appHistoryState, onPageLoaded = { webView, loadType ->
//            Log.d("InViewIntegratedSurvey", "onPageLoaded")
//        }, onError = {
//            // In case of error, remove the view
//            binding.container.removeAllViews()
//        }).apply {
//            Log.d("InViewIntegratedSurvey", "FeebaWebView is created")
//            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply { gravity = Gravity.BOTTOM }
//            this.loadUrl("https://sherzodg1.g1-iad.qualtrics.com/jfe/form/SV_00tzVlOPjSBB3mu")
//        })

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}