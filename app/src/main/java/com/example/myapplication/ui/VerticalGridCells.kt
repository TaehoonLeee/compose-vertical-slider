package com.example.myapplication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> VerticalGridCells(
	modifier: Modifier = Modifier,
	items: List<T>,
	cells: Int,
	content: @Composable (T) -> Unit
) {
	val chunkedList = items.chunked(cells)
	LazyColumn(modifier) {
		itemsIndexed(chunkedList) { totalListIdx, row ->
			Row {
				for (i in row.indices) {
					Box(modifier = Modifier.weight(1f, fill = true)) {
						content(row[i])
					}
				}

				if (totalListIdx == chunkedList.lastIndex) {
					val spareCellCnt = chunkedList.size * cells - items.size
					for (i in 0 until spareCellCnt) {
						Spacer(modifier = Modifier.weight(1f, fill = true))
					}
				}
			}
		}
	}
}