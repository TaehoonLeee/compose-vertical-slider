package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LazyListViewModel : ViewModel() {

	private val _animations: MutableStateFlow<List<ListAnimationTest>> = MutableStateFlow(testList)
	val animations: StateFlow<List<ListAnimationTest>> = _animations

	fun moveList(from: Int, to: Int) {
		_animations.update {
			it.toMutableList().apply {
				add(to, removeAt(from))
			}
		}
	}

	fun removeElement(idx: Int) {
		_animations.update {
			it.toMutableList().apply {
				removeAt(idx)
			}
		}
	}
}