package io.feeba.survey

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import io.feeba.FeebaFacade
import io.feeba.databinding.LayoutSurveyBinding
import io.least.core.createWithFactory

const val KEY_SURVEY_URL = "survey_url"

class SurveyFragment : DialogFragment() {

    private var _binding: LayoutSurveyBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: SurveyFragmentViewModel by viewModels {
        createWithFactory {
            SurveyFragmentViewModel(
                requireActivity().application,
                FeebaFacade.config.serviceConfig,
                "http://dev-dashboard.feeba.io/s/feeba/6504ee57ba0d101292e066a8"
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = LayoutSurveyBinding.inflate(inflater, container, false)
        val surveyUrl = arguments?.getString(KEY_SURVEY_URL) ?: throw IllegalArgumentException("Survey URL not provided")

        binding.webView.apply {
            loadUrl(surveyUrl)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                allowFileAccess = true

                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            }
            this.addJavascriptInterface(JsInterface(requireContext()) {
                when (it) {
                    CallToAction.CLOSE_SURVEY -> {
                        this@SurveyFragment.dismiss()
                    }
                }
            }, "Mobile")
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.webView.visibility = GONE
                binding.progressBarHolder.visibility = VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.webView.visibility = VISIBLE
                binding.progressBarHolder.visibility = GONE
            }


        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}

fun showSurveyFragment(
    supportFragmentManager: FragmentManager,
    surveyUrl: String
) {
    val fragment = SurveyFragment().apply {
        arguments = Bundle().apply {
            putSerializable(KEY_SURVEY_URL, surveyUrl)
        }
    }
//    supportFragmentManager.beginTransaction().replace().commit()
}
