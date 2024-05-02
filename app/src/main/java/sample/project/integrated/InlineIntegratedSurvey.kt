package sample.project.integrated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.least.demo.databinding.DialogIntegratedSurveyBinding


class InlineIntegratedSurvey() : Fragment() {

    private var _binding: DialogIntegratedSurveyBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogIntegratedSurveyBinding.inflate(inflater, container, false)
        binding.surveyViewBlitz.loadSurvey("https://dev-sv.feeba.io/s/feeba/65a381db081d06ce889dfd09")
        binding.surveyViewBlitz2.loadSurvey("https://dev-sv.feeba.io/s/feeba/65a381db081d06ce889dfd09")
        binding.surveyViewBlitzNesteScroll.loadSurvey("https://dev-sv.feeba.io/s/feeba/65a381db081d06ce889dfd09")
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}