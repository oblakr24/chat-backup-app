package com.rokoblak.chatbackup.faq

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.R
import com.rokoblak.chatbackup.ui.navigation.RouteNavigator
import com.rokoblak.chatbackup.data.util.ResourceResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FAQViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    resolver: ResourceResolver,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    private val items = flowOf(resolver.resolveStringArray(R.array.faq_items))

    private val expandedIndices = MutableStateFlow(emptyMap<Int, Boolean>())

    val uiState: StateFlow<FAQScreenUIState> by lazy {
        scope.launchMolecule(clock = RecompositionClock.ContextClock) {
            FAQPresenter(items, expandedIndices)
        }
    }

    fun handleAction(act: FAQAction) {
        when (act) {
            is FAQAction.ItemExpandedCollapsed -> expandCollapseItem(act.idx)
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun FAQPresenter(
        itemsFlow: Flow<Array<String>>,
        expandedIndicesFlow: StateFlow<Map<Int, Boolean>>,
    ): FAQScreenUIState {
        val items = itemsFlow.collectAsState(initial = emptyArray()).value
        val expandedIndices = expandedIndicesFlow.collectAsState().value

        val mappedItems = items.toList().chunked(2).withIndex().flatMap { (idx, titleAndSub) ->
            val expanded = expandedIndices[idx] == true
            val title = FAQScreenUIState.Item.Title(
                id = idx.toString(),
                idx = idx,
                titleAndSub.first(),
                expanded = expanded
            )
            if (expanded) {
                listOf(
                    title,
                    FAQScreenUIState.Item.Subtitle("sub_$idx", subtitle = titleAndSub.last())
                )
            } else {
                listOf(title)
            }
        }

        return FAQScreenUIState(
            mappedItems.toImmutableList()
        )
    }

    private fun expandCollapseItem(idx: Int) {
        expandedIndices.update {
            it.toMutableMap().apply {
                val current = it[idx] ?: false
                put(idx, !current)
            }.toMap()
        }
    }
}