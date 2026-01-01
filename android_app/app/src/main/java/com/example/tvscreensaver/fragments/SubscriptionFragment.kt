package com.example.tvscreensaver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.tvscreensaver.R
import com.example.tvscreensaver.billing.BillingManager
import com.example.tvscreensaver.billing.SubscriptionRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SubscriptionFragment : BaseFragment(R.layout.fragment_subscription) {

    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var billingManager: BillingManager
    
    private lateinit var subscriptionStatusText: TextView
    private lateinit var btnSubscribeMonthly: Button
    private lateinit var btnSubscribeYearly: Button
    private lateinit var subscriptionOptionsContainer: LinearLayout

    override fun setupUI(view: View) {
        subscriptionRepository = SubscriptionRepository(requireContext())
        billingManager = BillingManager(requireContext(), subscriptionRepository)
        
        subscriptionStatusText = view.findViewById(R.id.subscriptionStatusText)
        btnSubscribeMonthly = view.findViewById(R.id.btnSubscribeMonthly)
        btnSubscribeYearly = view.findViewById(R.id.btnSubscribeYearly)
        subscriptionOptionsContainer = view.findViewById(R.id.subscriptionOptionsContainer)
        
        setupClickListeners()
        updateSubscriptionStatus()
    }
    
    override fun setupObservers() {
        observeSubscriptionStatus()
        observeAvailableProducts()
    }

    private fun setupClickListeners() {
        btnSubscribeMonthly.setOnClickListener {
            purchaseSubscription(BillingManager.PRODUCT_ID_MONTHLY)
        }
        
        btnSubscribeYearly.setOnClickListener {
            purchaseSubscription(BillingManager.PRODUCT_ID_YEARLY)
        }
        
        updateSubscriptionStatus()
    }

    private fun observeSubscriptionStatus() {
        lifecycleScope.launch {
            billingManager.subscriptionStatus.collectLatest { isSubscribed ->
                updateSubscriptionStatus()
            }
        }
    }

    private fun observeAvailableProducts() {
        lifecycleScope.launch {
            billingManager.availableProducts.collectLatest { products ->
                if (products.isEmpty()) {
                    // Use default pricing text
                    return@collectLatest
                }
                
                // Update button text with actual pricing
                products.forEach { product ->
                    val price = product.subscriptionOfferDetails?.firstOrNull()
                        ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    
                    when (product.productId) {
                        BillingManager.PRODUCT_ID_MONTHLY -> {
                            btnSubscribeMonthly.text = "Subscribe Monthly - $price/month"
                        }
                        BillingManager.PRODUCT_ID_YEARLY -> {
                            btnSubscribeYearly.text = "Subscribe Yearly - $price/year"
                        }
                    }
                }
            }
        }
    }

    private fun updateSubscriptionStatus() {
        val isSubscribed = subscriptionRepository.isSubscriptionActive()
        if (isSubscribed) {
            subscriptionStatusText.text = "Current Status: Premium Active ✓"
            subscriptionStatusText.setTextColor(resources.getColor(R.color.groovy_lime, null))
            subscriptionOptionsContainer.visibility = View.GONE
        } else {
            subscriptionStatusText.text = "Current Status: Free"
            subscriptionStatusText.setTextColor(resources.getColor(R.color.white, null))
            subscriptionOptionsContainer.visibility = View.VISIBLE
        }
    }

    private fun purchaseSubscription(productId: String) {
        val products = billingManager.availableProducts.value
        val product = products.find { it.productId == productId }
        
        if (product != null) {
            billingManager.launchPurchaseFlow(requireActivity(), product)
        } else {
            Toast.makeText(
                requireContext(),
                "Subscription not available. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroy()
    }
}
