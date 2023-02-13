package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.rokoblak.chatbackup.ui.theme.LocalTypography
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme

@Composable
fun DetailsHeader(
    text: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    iconVector: ImageVector? = null,
    rightButtonText: String? = null,
    background: Color = MaterialTheme.colors.primaryVariant,
    onIconPressed: (() -> Unit)? = null,
    rightButtonEnabled: Boolean = true,
    rightButtonColor: Color = MaterialTheme.colors.secondary,
) {
    ConstraintLayout(
        modifier = modifier.shadow(4.dp)
            .background(background)
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
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
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable {
                        onBackPressed()
                    }
                    .padding(8.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary)
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
            style = if (text.length > 28) LocalTypography.current.bodySemiBold else LocalTypography.current.titleSemiBold,
            color = MaterialTheme.colors.secondary,
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
                colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary)
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

                style = LocalTypography.current.subheadRegular,
                color = rightButtonColor,
                maxLines = 1,
            )
        }
    }
}

@Preview
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
