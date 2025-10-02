package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val NUM_PAGES = 3

class OnboardingAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return NUM_PAGES
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> OnboardingPage1Fragment()
            1 -> OnboardingPage2Fragment()
            2 -> OnboardingPage3Fragment()
            else -> OnboardingPage1Fragment()
        } as Fragment
    }
}