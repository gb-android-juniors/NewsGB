package com.example.newsgb.news.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsgb._core.ui.NewsDtoToUiMapper
import com.example.newsgb._core.ui.model.*
import com.example.newsgb._core.ui.store.NewsStore
import com.example.newsgb.news.domain.NewsRepository
import com.example.newsgb.utils.ui.Category
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewsViewModel(
    private val newsRepo: NewsRepository,
    private val mapper: NewsDtoToUiMapper,
    private val store: NewsStore,
    private val category: Category
) : ViewModel() {

    /** переменная состояния экрана со списком новостей */
    private val _viewState = MutableStateFlow<ListViewState>(ListViewState.Loading)
    val viewState: StateFlow<ListViewState> = _viewState.asStateFlow()

    init {
        /** При инициализации подписываемся на обновления состояний и команд от NewsStore */
        store.storeState.onEach { renderStoreState(it) }.launchIn(viewModelScope)
        store.storeEffect.onEach { renderAppEffect(it) }.launchIn(viewModelScope)
    }

    /**
     * метод обработки состояний NewsStore
     * конвертируем состояния приложения в состояния экрана
     * */
    private fun renderStoreState(storeState: AppState) {
        when(storeState) {
            AppState.Empty, AppState.Loading, is AppState.MoreLoading -> _viewState.value = ListViewState.Loading
            is AppState.Data -> setSuccessState(data = storeState.data)
            is AppState.Error -> _viewState.value = ListViewState.Error(message = storeState.message)
        }
    }

    /**
     * метод обработки команд от NewsStore
     *
     * AppEffect.LoadData - команда на загрузку данных
     * AppEffect.Error - команда отображение ошибки при дозагрузке данных
     * */
    private fun renderAppEffect(effect: AppEffect) {
        when (effect) {
            AppEffect.LoadData -> getNewsByCategory()
            is AppEffect.Error -> {}
        }
    }

    /**
     * отфильтровываем список данных из стора по категории и
     * передаем их в соответствующем состоянии для отображения на экране
     **/
    private fun setSuccessState(data: List<Article>) {
        val filteredData = data.filter { it.category == category }
        if (filteredData.isEmpty()) {
            _viewState.value = ListViewState.Empty
        } else {
            _viewState.value = ListViewState.Data(data = filteredData)
        }
    }

    /**
     * метод получения списка новостей по категории.
     * Проверяем, если данные уже загружены ранее и хранятся в стор, берем их от туда отфильтровав по категории.
     * Если данные еще не загружены, инициируем событие по дозагрузке данных
     **/
    fun getData() {
        val currentStoreState = store.storeState.value
        if (currentStoreState is AppState.Data) {
            val filteredData = currentStoreState.data.filter { it.category == category }
            if (filteredData.isEmpty()) {
                store.dispatch(event = AppEvent.LoadMore)
            } else {
                _viewState.value = ListViewState.Data(data = filteredData)
            }
        }
    }

    /**
     * метод запроса первой страницы новостей по категории.
     * В случае успеха конвертируем поулченные данные с помощью маппера и
     * передаем в качестве успешного события AppEvent.DataReceived в NewsStore
     * */
    private fun getNewsByCategory() {
        viewModelScope.launch {
            newsRepo.getNewsByCategory(page = INITIAL_PAGE, countryCode = "ru", category = category.apiCode)
                .onSuccess { response ->
                    store.dispatch(AppEvent.DataReceived(data = mapper(newsList = response.articles, category = category)))
                }
                .onFailure { ex ->
                    store.dispatch(AppEvent.ErrorReceived(message = ex.message))
                }
        }
    }

    companion object {
        private const val INITIAL_PAGE = 1
    }
}