package sample.project.test

import ResizableFrameLayout
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import io.feeba.data.SurveyPresentation
import io.feeba.data.state.Defaults
import io.feeba.ui.IntegrationMode
import io.feeba.ui.PageResized
import io.feeba.ui.createWebViewInstance

class TestFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val survey = arguments?.getSerializable("survey") as SurveyPresentation
        return ScrollView(requireContext()).also { scrollView ->
            scrollView.isHorizontalScrollBarEnabled = true
            scrollView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

            scrollView.addView(LinearLayout(requireContext()).also { scrollViewChild ->
                scrollViewChild.id = View.generateViewId()
                scrollViewChild.orientation = LinearLayout.VERTICAL
                scrollViewChild.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                scrollViewChild.setBackgroundColor(Color.BLUE)

                scrollViewChild.addView(ResizableFrameLayout(requireContext()).also { resizableFrameLayout ->
                    resizableFrameLayout.setBackgroundColor(Color.GREEN)
                    resizableFrameLayout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

                    fun resizeTheContainer(w: Int, h: Int) {
                        resizableFrameLayout.layoutParams = resizableFrameLayout.layoutParams.apply {
                            width = w
                            height = h
                        }
                    }
                    resizableFrameLayout.addView(
                        createWebViewInstance(requireContext(), survey, Defaults.appHistoryState, IntegrationMode.Modal,
                            onPageLoaded = { webView, loadType ->
                                if (loadType is PageResized) {
                                    // Changing the container while webView size is statically set  is working
//                                    webView.setInitialScale(100)
                                    resizeTheContainer(loadType.w, loadType.h)
                                    // Changing the webview size is not working. It is causing scaled content
//                                    webView.layoutParams = webView.layoutParams.apply {
//                                        width = loadType.w
//                                        height = loadType.h
//                                    }
                                }
                            }, onError = {}, onOutsideTouch = {})
                            .apply {
                                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                            }
                    )
                })
            })
        }
    }
}