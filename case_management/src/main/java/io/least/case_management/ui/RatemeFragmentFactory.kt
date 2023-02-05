package io.least.case_management.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.least.case_management.ui.cases.CaseListFragment
import io.least.case_management.viewmodel.CaseListConfig

class CaseListFragmentFactory(private val config: CaseListConfig) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            CaseListFragment::class.java.name -> CaseListFragment(config)
            else -> super.instantiate(classLoader, className)
        }
    }
}