package com.example.tvscreensaver.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        setupObservers()
    }

    abstract fun setupUI(view: View)
    open fun setupObservers() {}
}
