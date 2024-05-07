package sample.project.page

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import io.feeba.Feeba
import io.least.demo.databinding.LayoutWithTimerBinding

class PageTriggerActivity : AppCompatActivity() {
    private var _binding: LayoutWithTimerBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var secondsElapsed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = LayoutWithTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        runnable = Runnable {
            secondsElapsed++
            binding.textViewTimer.text = "Time passed: $secondsElapsed seconds"
            handler.postDelayed(runnable, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(runnable)
        intent.getStringExtra("page_name")?.let { Feeba.pageOpened(it) }
    }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
        intent.getStringExtra("page_name")?.let { Feeba.pageClosed(it) }
    }
}