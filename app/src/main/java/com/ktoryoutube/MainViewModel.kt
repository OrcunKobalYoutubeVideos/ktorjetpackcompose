package com.ktoryoutube

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val apiClient: ApiClient
) : ViewModel() {

    var productList = MutableStateFlow<MainUIState<List<ProductItem>>>(value = MainUIState.Loading())
        private set

    init {
        viewModelScope.launch {

            apiClient.getProducts().onSuccess { list->
                productList.update { MainUIState.Success(response = list) }
            }.onError { error->
                productList.update {
                    MainUIState.Error(message = error)
                }
            }
        }
    }

}