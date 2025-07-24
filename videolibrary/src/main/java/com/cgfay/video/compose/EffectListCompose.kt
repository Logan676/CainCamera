package com.cgfay.video.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.video.bean.EffectMimeType
import com.cgfay.video.bean.EffectType

@Composable
fun EffectCategoryBar(
    selected: EffectMimeType?,
    onSelected: (EffectMimeType) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = EffectMimeType.values().toList()
    LazyRow(modifier = modifier) {
        items(categories) { type ->
            Text(
                text = type.displayName,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onSelected(type) },
                color = if (selected == type) MaterialTheme.colors.primary else Color.White
            )
        }
    }
}

@Composable
fun VideoEffectList(
    effects: List<EffectType>,
    selectedIndex: Int,
    onSelected: (Int, EffectType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier) {
        itemsIndexed(effects) { index, effect ->
            EffectItem(
                effect = effect,
                selected = index == selectedIndex,
                onClick = { onSelected(index, effect) }
            )
        }
    }
}

@Composable
fun EffectItem(effect: EffectType, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(80.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = effect.getThumb(),
            contentDescription = effect.getName(),
            modifier = Modifier
                .size(60.dp)
                .background(if (selected) MaterialTheme.colors.primary else Color.Transparent),
            contentScale = ContentScale.Crop
        )
        Text(text = effect.getName(), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun VideoFilterList(
    data: List<ResourceData>,
    selectedIndex: Int,
    onSelected: (Int, ResourceData) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier) {
        itemsIndexed(data) { index, filter ->
            FilterItem(
                filter = filter,
                selected = index == selectedIndex,
                onClick = { onSelected(index, filter) }
            )
        }
    }
}

@Composable
fun FilterItem(filter: ResourceData, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(80.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = filter.thumbPath,
            contentDescription = filter.name,
            modifier = Modifier
                .size(60.dp)
                .background(if (selected) MaterialTheme.colors.primary else Color.Transparent),
            contentScale = ContentScale.Crop
        )
        Text(text = filter.name, modifier = Modifier.padding(top = 4.dp))
    }
}
