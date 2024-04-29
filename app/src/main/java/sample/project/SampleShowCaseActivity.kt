package sample.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.least.demo.R
import io.least.demo.databinding.ActivitySampleShowcaseBinding
import sample.project.project_list.ShowCaseFragment

class SampleShowCaseActivity : AppCompatActivity() {
    private var _binding: ActivitySampleShowcaseBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySampleShowcaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, NavHostFragment())
            .commit()
    }
}