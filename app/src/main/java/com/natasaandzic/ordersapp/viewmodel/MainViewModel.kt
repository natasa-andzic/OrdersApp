package com.natasaandzic.ordersapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.natasaandzic.ordersapp.model.LoadingState
import com.natasaandzic.ordersapp.model.Order
import com.natasaandzic.ordersapp.model.OrderDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val ordersLiveData = MediatorLiveData<List<Order>>()
    private val _queryLiveData = MutableLiveData<String>()
    private val _allOrdersLiveData = MutableLiveData<List<Order>>()
    private val _searchOrdersLiveData = MediatorLiveData<List<Order>>()
    val loadingStateLiveData = MutableLiveData<LoadingState>()
    private var searchJob: Job? = null
    private val debouncePeriod = 500L

    init {
        ordersLiveData.addSource(_allOrdersLiveData) {
            ordersLiveData.value = it
        }
        ordersLiveData.addSource(_searchOrdersLiveData) {
            ordersLiveData.value = it
        }

        _searchOrdersLiveData.addSource(_queryLiveData) { query ->
            _searchOrdersLiveData.value = emptyList()
            val newSource = fetchOrdersByQuery(query)
            _searchOrdersLiveData.addSource(newSource) { result ->
                _searchOrdersLiveData.value = result
            }
        }
    }

    fun onViewReady() {
        if (_allOrdersLiveData.value.isNullOrEmpty())
            fetchAllOrders()
    }

    fun onSearchQuery(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(debouncePeriod)
            if (query.isEmpty()) {
                fetchAllOrders()
            } else {
                _queryLiveData.postValue(query)
            }
        }

    }

    private fun fetchAllOrders() {
        loadingStateLiveData.value = LoadingState.LOADING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val orders = OrderDataGenerator.getAllOrders()
                _allOrdersLiveData.postValue(orders)
                loadingStateLiveData.postValue(LoadingState.LOADED)
            } catch (e: Exception) {
                loadingStateLiveData.postValue(LoadingState.ERROR)
            }

        }
    }

    private fun fetchOrdersByQuery(query: String): LiveData<List<Order>> {
        val liveData = MutableLiveData<List<Order>>()
        loadingStateLiveData.value = LoadingState.LOADING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val orders = OrderDataGenerator.searchOrders(query)
                liveData.postValue(orders)
                loadingStateLiveData.postValue(LoadingState.LOADED)
            } catch (e: Exception) {
                loadingStateLiveData.postValue(LoadingState.ERROR)
            }
        }

        return liveData
    }
}