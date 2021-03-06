package com.example.newsgb._core.ui.store

import com.example.newsgb._core.ui.model.AppEffect
import com.example.newsgb._core.ui.model.AppEvent
import com.example.newsgb._core.ui.model.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Класс-хранилище текущего состояния приложения (вместе с данными)
 *
 * Создается в активити и, соответственно, имеет жизненный цикл активитии.
 * Экземпляр класса, созданный в активити, переается фрагментам,
 * таким образом фрагменты всегда знают текущее состояние приложения, даже после пересоздания,
 * и могут в любой момент подписаться на него
 *
 * */

class NewsStore : CoroutineScope by MainScope() {

    /** Flow текущих состояний стора */
    private val _storeState = MutableStateFlow<AppState>(AppState.Empty)
    val storeState: StateFlow<AppState> = _storeState.asStateFlow()

    /** Flow команд от стора */
    private val _storeEffect: MutableSharedFlow<AppEffect> = MutableSharedFlow()
    val storeEffect: SharedFlow<AppEffect> = _storeEffect.asSharedFlow()

    /**
     * Метод обработки событий
     * При событии Refresh: передаем команду на загрузку данных (AppEffect.LoadData) и переключаемся в состояние загрузки (AppState.Loading)
     * При событии ErrorReceived: если предыдущим было состояние пустой загрузки, то переключаемся в состояние ошибки (AppState.Error)
     *                            если предыдущим было состояние дозагрузки, то передаем команду на вывод сообщениея об ошибке
     * При событии DataReceived: если предыдущим было состояние пустой загрузки, то при пустых нанных переключаемся в состояние AppState.Empty
     *                                                                           при непустых данных переключаемся в состояние AppState.Data и передаем туда данные
     *                           если предыдущим было состояние дозагрузки, то переключаемся в состояние AppState.Data прибавляя к старым данным вновь полученные данные
     * При событии LoadMore: передаем команду на загрузку данных (AppEffect.LoadData)
     * и переключаемся в состояние дозагрузки (AppState.MoreLoading) сохраняя в параметрах ранее загруженные данные
     * */
    fun dispatch(event: AppEvent) {
        val currentState = storeState.value // сохраняем в переменную текущее состояние
        val newState = when (event) {
            is AppEvent.Refresh -> {
                when (currentState) {
                    is AppState.Empty, is AppState.Error -> {
                        launch { _storeEffect.emit(AppEffect.LoadData) }
                        AppState.Loading
                    }
                    else -> currentState
                }
            }
            is AppEvent.ErrorReceived -> {
                when (currentState) {
                    AppState.Loading -> AppState.Error(message = event.message)
                    is AppState.MoreLoading -> {
                        launch { _storeEffect.emit(AppEffect.Error(message = event.message)) }
                        AppState.Data(data = currentState.data)
                    }
                    else -> currentState
                }
            }
            is AppEvent.DataReceived -> {
                when (currentState) {
                    AppState.Loading -> {
                        if (event.data.isEmpty()) AppState.Empty
                        else AppState.Data(data = event.data)
                    }
                    is AppState.MoreLoading -> {
                        AppState.Data(data = currentState.data + event.data)
                    }
                    else -> currentState
                }
            }
            is AppEvent.LoadMore -> {
                when (currentState) {
                    is AppState.Data -> {
                        launch { _storeEffect.emit(AppEffect.LoadData) }
                        AppState.MoreLoading(data = currentState.data)
                    }
                    else -> currentState
                }
            }
        }
        //если новое состояне отличается от текущего, то устанавливаем новое состояние
        if (newState != currentState) {
            _storeState.value = newState
        }
    }
}