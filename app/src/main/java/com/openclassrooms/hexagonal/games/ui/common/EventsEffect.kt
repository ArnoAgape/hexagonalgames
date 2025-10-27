package com.openclassrooms.hexagonal.games.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * A reusable composable utility that collects events from a [Flow] and executes
 * a provided action whenever a new event is emitted.
 *
 * It automatically handles lifecycle awareness using [repeatOnLifecycle],
 * ensuring that event collection only occurs while the [androidx.lifecycle.LifecycleOwner]
 * is in the [Lifecycle.State.STARTED] state.
 *
 * Commonly used for one-time UI events such as showing toast messages or triggering
 * navigation actions from a ViewModel.
 *
 * Example usage:
 * ```
 * EventsEffect(viewModel.eventsFlow) { event ->
 *     when (event) {
 *         is Event.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
 *     }
 * }
 * ```
 *
 * @param flow The [Flow] emitting events from the ViewModel.
 * @param onEvent A lambda executed for each new event emitted.
 */
@Composable
fun <T> EventsEffect(flow: Flow<T>, onEvent: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(flow, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(currentOnEvent)
            }
        }
    }
}