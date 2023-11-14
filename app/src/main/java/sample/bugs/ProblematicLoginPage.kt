package sample.bugs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.feeba.Feeba
import io.least.demo.databinding.DialogProblematicLogicBinding
import io.least.viewmodel.RateAppConfig


class ProblematicLoginPage() : Fragment() {

    private var _binding: DialogProblematicLogicBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var secondsElapsed: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogProblematicLogicBinding.inflate(inflater, container, false)
        runnable = Runnable {
            secondsElapsed++
            binding.textViewTimer.text = "Time passed: $secondsElapsed seconds"
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // FEEBA integration. Let Feeba know that the page is opened.
        Feeba.pageOpened("login_page")
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)

        // FEEBA integration. Let Feeba know that the page is closed.
        Feeba.pageClosed("login_page")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}