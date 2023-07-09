package com.rokoblak.chatbackup.ui.commonui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.rokoblak.chatbackup.ui.theme.AppThemePreviews
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme

@Composable
fun DetailsHeader(
    text: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    iconVector: ImageVector? = null,
    rightButtonText: String? = null,
    background: Color = MaterialTheme.colorScheme.primaryContainer,
    onIconPressed: (() -> Unit)? = null,
    rightButtonEnabled: Boolean = true,
    rightButtonColor: Color = MaterialTheme.colorScheme.secondary,
) {
    ConstraintLayout(
        modifier = modifier
            .shadow(4.dp)
            .background(background)
            .wrapContentHeight()
            .fillMaxWidth(),
    ) {
        val (backBtn, title, icon) = createRefs()
        Box(
            modifier = Modifier
                .height(56.dp)
                .constrainAs(backBtn) {
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable {
                        onBackPressed()
                    }
                    .padding(8.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
        Text(
            modifier = Modifier.constrainAs(title) {
                width = Dimension.wrapContent
                height = Dimension.wrapContent
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            text = text,
            textAlign = TextAlign.Center,
            style = if (text.length > 28) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
        )
        if (iconVector != null) {
            Image(
                painter = rememberVectorPainter(iconVector),
                contentDescription = null,
                modifier = Modifier
                    .height(54.dp)
                    .constrainAs(icon) {
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
                    .let {
                        if (rightButtonEnabled) {
                            it.clickable {
                                onIconPressed?.invoke()
                            }
                        } else {
                            it
                        }
                    }
                    .padding(16.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
            )
        } else if (rightButtonText != null) {
            Text(
                modifier = Modifier
                    .height(54.dp)
                    .constrainAs(icon) {
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                        baseline.linkTo(title.baseline)
                        start.linkTo(title.end)
                        end.linkTo(parent.end)
                    }
                    .let {
                        if (rightButtonEnabled) {
                            it.clickable {
                                onIconPressed?.invoke()
                            }
                        } else {
                            it
                        }
                    }
                    .padding(16.dp),
                text = rightButtonText,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,

                style = MaterialTheme.typography.labelSmall,
                color = rightButtonColor,
                maxLines = 1,
            )
        }
    }
}

@AppThemePreviews
@Composable
private fun DetailsHeaderPreview() {
    ChatBackupTheme {
        Column {
            DetailsHeader(
                text = "Details header1",
                onBackPressed = {}, iconVector = Icons.Filled.Add, onIconPressed = {}
            )
            DetailsHeader(
                text = "Details header2",
                onBackPressed = {}, rightButtonText = "EDIT", onIconPressed = {}
            )
            DetailsHeader(
                text = "Details header3",
                onBackPressed = {}, rightButtonText = "EDIT LONG LONG NAME", onIconPressed = {}
            )
            DetailsHeader(
                text = "Details header4",
                onBackPressed = {}, onIconPressed = {}
            )
        }
    }
}
