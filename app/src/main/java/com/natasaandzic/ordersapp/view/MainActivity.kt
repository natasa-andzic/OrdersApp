package com.natasaandzic.ordersapp.view

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.natasaandzic.ordersapp.databinding.ActivityMainBinding
import com.natasaandzic.ordersapp.model.LoadingState
import com.natasaandzic.ordersapp.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val adapter = OrderAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    override fun onResume() {
        super.onResume()

        initializeUI()
        initializeObservers()
        viewModel.onViewReady()
    }

    private fun initializeUI() {
        binding.ordersRv.adapter = adapter
        binding.ordersRv.layoutManager = LinearLayoutManager(this)
        binding.searchEt.doOnTextChanged { text, start, before, count ->
            viewModel.onSearchQuery(text.toString())
        }
    }

    private fun initializeObservers() {
        viewModel.loadingStateLiveData.observe(this) {
            onLoadingStateChanged(it)
        }
        viewModel.ordersLiveData.observe(this) {
            adapter.updateOrders(it)
        }
    }

    private fun onLoadingStateChanged(state: LoadingState) {
        binding.searchEt.visibility =
            if (state == LoadingState.LOADED) View.VISIBLE else View.GONE
        binding.ordersRv.visibility = if (state == LoadingState.LOADED) View.VISIBLE else View.GONE
        binding.errorTv.visibility = if (state == LoadingState.ERROR) View.VISIBLE else View.GONE
        binding.loadingPb.visibility = if (state == LoadingState.LOADING) View.VISIBLE else View.GONE

    }
}