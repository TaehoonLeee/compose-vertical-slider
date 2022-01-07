package com.example.myapplication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.myapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Stack
import kotlin.math.min
import kotlin.math.roundToInt

data class ListAnimationTest(
	val name: String,
	val url: String,
	val key: String
)

@Composable
fun TestLayout(
	idx: Int,
	item: ListAnimationTest,
	dragDropState: DragDropState
) {
	val scrollScope = rememberCoroutineScope()

	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp)
	) {
		Image(
			painter = painterResource(id = R.drawable.ic_handle),
			contentDescription = null,
			modifier = Modifier
				.size(24.dp)
				.pointerInput(idx) {
					detectVerticalDragGestures(
						onVerticalDrag = { change, dragAmount ->
							change.consumeAllChanges()
							scrollScope.launch {
								dragDropState.onDrag(dragAmount)
							}
						},
						onDragStart = { dragDropState.onDragStart(idx) },
						onDragEnd = {
							scrollScope.launch {
								dragDropState.onDragInterrupted()
							}
						},
						onDragCancel = {
							scrollScope.launch {
								dragDropState.onDragInterrupted()
							}
						}
					)
				}
		)

		Column {
			Text(
				text = item.name,
				color = Color.White
			)
			Text(
				text = item.url,
				color = Color.White
			)
		}
	}
}

@Composable
fun <T> DragDropLazyColumn(
	list: List<T>,
	key: ((T) -> Any)? = null,
	onSwipeAction: (Int, T) -> Unit,
	onPlaced: (from: Int, to: Int) -> Unit,
	content: @Composable (Int, T, DragDropState) -> Unit
) {
	val dragDropListState by rememberDragDropListState(onPlaced = onPlaced)
	val listDiffState by rememberListDiffState(list = list)
	listDiffState.calculateDiff(list)

	LazyColumn(state = dragDropListState.lazyListState) {
		items(
			listDiffState.oldList.size,
			key = if (key != null) { keyIdx -> key(listDiffState.oldList[keyIdx].item) } else null
		) { idx ->
			val animatedItem = listDiffState.oldList[idx]
			val offsetOrNull = dragDropListState.elementOffset.roundToInt().takeIf {
				idx == dragDropListState.initiallyDraggedElement?.index
			}

			key(key?.invoke(animatedItem.item)) {
				AnimatedVisibility(visibleState = animatedItem.visibility) {
					AnimatedContent(
						offset = dragDropListState.getStandingElementOffset(idx) ?: offsetOrNull ?: 0,
						onSwipeAction = { onSwipeAction(idx, animatedItem.item) }
					) {
						content(idx, animatedItem.item, dragDropListState)
					}
				}
			}
		}
	}
}

@Composable
fun AnimatedContent(
	offset: Int,
	onSwipeAction: () -> Unit,
	content: @Composable () -> Unit
) {
	Column(Modifier
		.offset { IntOffset(0, offset) }
	) {
		SwipeableRow(onSwipeAction = onSwipeAction) {
			content()
		}
	}
}

@Composable
fun SwipeableRow(
	onSwipeAction: () -> Unit,
	content: @Composable RowScope.() -> Unit
) {
	val animationScope = rememberCoroutineScope()
	val offsetX = remember { Animatable(0f) }
	var onSwipedAlpha by remember { mutableStateOf(1f) }
	val onSwipeActionState = rememberUpdatedState(newValue = onSwipeAction)
	val backgroundAlpha = lerp(0f, 0.5f, (offsetX.value / -1000f).coerceIn(0f, 0.5f))

	Row(
		content = content,
		modifier = Modifier
			.fillMaxWidth()
			.alpha(onSwipedAlpha)
			.background(Color.Red.copy(alpha = min(backgroundAlpha, onSwipedAlpha)))
			.offset { IntOffset((offsetX.value).roundToInt(), 0) }
			.pointerInput(Unit) {
				detectHorizontalDragGestures(
					onHorizontalDrag = { change, dragAmount ->
						val newX = offsetX.value + dragAmount
						change.consumePositionChange()
						animationScope.launch {
							offsetX.snapTo(newX.coerceAtMost(0f))
						}
					},
					onDragEnd = {
						animationScope.launch {
							if (offsetX.value < -500f) {
								onSwipedAlpha = 0f
								onSwipeActionState.value.invoke()
							} else {
								offsetX.animateTo(0f, spring())
							}
						}
					}
				)
			}
	)
}

data class AnimatedItem<T>(
	val item: T,
	val visibility: MutableTransitionState<Boolean> = MutableTransitionState(true)
) {
	override fun hashCode(): Int = item.hashCode()

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		if (item != (other as AnimatedItem<*>).item) return false

		return true
	}
}

@Composable
fun <T> rememberListDiffState(
	list: List<T>,
	diffScope: CoroutineScope = rememberCoroutineScope()
) = remember { mutableStateOf(ListDiffState(
	diffScope,
	list.map { AnimatedItem(it) }.toMutableStateList())
) }

class ListDiffState<T>(
	val diffScope: CoroutineScope,
	val oldList: SnapshotStateList<AnimatedItem<T>>
) {
	fun calculateDiff(newList: List<T>) {
		val diffCallback =
			object : DiffUtil.Callback() {
				override fun getOldListSize(): Int = oldList.size

				override fun getNewListSize(): Int = newList.size

				override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
					oldList[oldItemPosition].item == newList[newItemPosition]

				override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
					oldList[oldItemPosition].item == newList[newItemPosition]
			}

		DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(object : ListUpdateCallback {
			override fun onInserted(position: Int, count: Int) {
				for (i in 0 until count) {
					val newPosition = position + i
					val newItem = AnimatedItem(newList[newPosition], MutableTransitionState(false))
					newItem.visibility.targetState = true
					oldList.add(newPosition, newItem)
				}
			}

			override fun onRemoved(position: Int, count: Int) {
				oldList[position].visibility.targetState = false
				diffScope.launch {
					Animatable(1f).animateTo(0f)
					oldList.removeAt(position)
				}
			}

			override fun onMoved(fromPosition: Int, toPosition: Int) {
				oldList.add(toPosition, oldList.removeAt(fromPosition))
			}

			override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
		})
	}
}

@Composable
fun rememberDragDropListState(
	onPlaced: (from: Int, to: Int) -> Unit,
	coroutineScope: CoroutineScope = rememberCoroutineScope(),
	lazyListState: LazyListState = rememberLazyListState()
) = remember { mutableStateOf(DragDropState(lazyListState, coroutineScope, onPlaced)) }

class DragDropState(
	val lazyListState: LazyListState,
	private val actionScope: CoroutineScope,
	private val onPlaced: (from: Int, to: Int) -> Unit,
) {

	enum class ScrollDirection {
		UP,
		DOWN
	}

	private var draggedDistance by mutableStateOf(0f)
	private val elementDisplacement = Animatable(0f)

	private val initialOffsets: Pair<Int, Int>?
		get() = initiallyDraggedElement?.let {
			Pair(it.offset, it.offset + it.size)
		}

	private var currentIndexOfDraggedItem: Int? by mutableStateOf(0)
	private val currentElement: LazyListItemInfo?
		get() = currentIndexOfDraggedItem?.let {
			val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
			visibleItems.getOrNull(it - visibleItems.first().index)
		}

	private val standingElementsStack: Stack<LazyListItemInfo> = Stack()
	private val standingElements: MutableMap<LazyListItemInfo, Animatable<Float, AnimationVector1D>> = mutableMapOf()
	private var scrollDirection: ScrollDirection? = null

	var initiallyDraggedElement: LazyListItemInfo? by mutableStateOf(null)
		private set

	val elementOffset get() = elementDisplacement.value

	fun getStandingElementOffset(idx: Int): Int? {
		val hoveringElement = lazyListState.layoutInfo.visibleItemsInfo.getOrNull(idx)
		return standingElements[hoveringElement]?.value?.roundToInt()
	}

	fun onDragStart(idx: Int) {
		initiallyDraggedElement = lazyListState.layoutInfo.visibleItemsInfo[idx]
		currentIndexOfDraggedItem = initiallyDraggedElement?.index
	}

	suspend fun onDragInterrupted() {
		val initiallyElement = requireNotNull(initiallyDraggedElement)
		val currentElement = requireNotNull(currentElement)
		val movedDistance = (currentElement.index - initiallyElement.index) * initiallyElement.size

		elementDisplacement.animateTo(movedDistance.toFloat(), tween(500))
		onPlaced(initiallyElement.index, currentElement.index)
		initiallyDraggedElement = null
		currentIndexOfDraggedItem = null
		standingElements.clear()
		draggedDistance = 0f
		scrollDirection = null
	}

	suspend fun onDrag(dragAmount: Float) {
		draggedDistance += dragAmount
		elementDisplacement.snapTo(draggedDistance)

		initialOffsets?.let { (top, bottom) ->
			val startOffset = top + draggedDistance
			val endOffset = bottom + draggedDistance

			currentElement?.let { hovered ->
				lazyListState.layoutInfo.visibleItemsInfo.filterNot { item ->
					(item.offset + item.size) < startOffset || item.offset > endOffset || hovered.index == item.index
				}.firstOrNull { item ->
					val delta = startOffset - hovered.offset
					if (delta > 0) {
						endOffset > (item.offset + item.size)
					} else {
						startOffset < item.offset
					}
				}?.also { item ->
					val currentScrollDirection = getScrollDirection(hovered.index, item.index)
					scrollDirection?.let { mainScrollDirection ->
						if (mainScrollDirection == currentScrollDirection) {
							moveStandingElement(hovered, item)
						}
						else {
							if (standingElementsStack.empty()) {
								scrollDirection = currentScrollDirection
								moveStandingElement(hovered, item)
							} else {
								returnStandingElement(hovered)
							}
						}
					}?: run {
						scrollDirection = currentScrollDirection
						moveStandingElement(hovered, item)
					}
					currentIndexOfDraggedItem = item.index
				}
			}
		}
	}

	fun checkForOverScroll(): Float = initiallyDraggedElement?.let { itemInfo ->
		val startOffset = itemInfo.offset + draggedDistance
		val endOffset = itemInfo.offset + itemInfo.size + draggedDistance

		when {
			draggedDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset).takeIf { it > 0 }
			draggedDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset).takeIf { it < 0 }
			else -> null
		}
	}?: 0f

	private fun moveStandingElement(from: LazyListItemInfo, to: LazyListItemInfo) {
		standingElementsStack.push(to)
		val targetDistance = to.size * (from.index - to.index)

		standingElements[to]?.let {
			actionScope.launch { it.animateTo(targetDistance.toFloat(), tween(500)) }
		}?: run {
			val standingElementDisplacement = Animatable(0f)
			standingElements[to] = standingElementDisplacement
			actionScope.launch {
				standingElementDisplacement.animateTo(targetDistance.toFloat(), tween(500))
			}
		}
	}

	private fun returnStandingElement(hovered: LazyListItemInfo) {
		standingElements[hovered]?.let {
			standingElementsStack.pop()
			actionScope.launch {
				it.animateTo(0f, tween(500))
			}
		}
	}

	private fun getScrollDirection(from: Int, to: Int) =
		if (from < to) ScrollDirection.DOWN else ScrollDirection.UP
}