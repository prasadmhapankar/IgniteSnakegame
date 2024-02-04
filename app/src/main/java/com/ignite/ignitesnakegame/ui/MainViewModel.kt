package com.ignite.ignitesnakegame.ui

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ignite.ignitesnakegame.data.MoveRequest
import com.ignite.ignitesnakegame.domain.model.Cell
import com.ignite.ignitesnakegame.domain.util.Direction
import com.ignite.ignitesnakegame.common.Resource
import com.ignite.ignitesnakegame.domain.use_case.PostSnakeStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val postSnakeStateUseCase: PostSnakeStateUseCase,
) : ViewModel() {

    private val _state = mutableStateOf(SnakeState())
    val state: State<SnakeState> = _state

    init {
        Log.d(TAG, ": MainViewModel init")
        postSnakeState(Direction.LEFT)
    }

    fun onEvent(snakeStateEvent: SnakeEvent) {
        when (snakeStateEvent) {
            is SnakeEvent.OnMove -> postSnakeState(snakeStateEvent.direction)
        }
    }

    private fun postSnakeState(direction: Direction) {
        viewModelScope.launch(Dispatchers.IO) {
            postSnakeStateUseCase(
                postMove = MoveRequest(
                    playerId = "player1",
                    direction = direction.id
                )
            ).collectLatest { response: Resource<List<List<Cell>>> ->
                when (response) {
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        withContext(Dispatchers.Main) {
                            _state.value = state.value.copy(board = response.data)
                        }
                    }
                }
            }
        }
    }

    /*private fun initBoard() {
        //Get SnakeState from server
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSnakeState().collectLatest { response ->
                Log.d(TAG, "initBoard response: $response")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "initBoard Get response: $responseBody")

                    responseBody?.apply {
                        updateBoard(responseBody)
                    }
                }
            }
        }
    }*/

}