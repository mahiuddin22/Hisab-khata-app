package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BengaliBodyFontFamily
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassBorderLight

/**
 * iOS-style frosted glass container with subtle border and gradient depth
 */
@Composable
fun CupertinoGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    borderColor: Color = if (MaterialTheme.colorScheme.background == Color(0xFF080C14)) GlassBorderDark else GlassBorderLight,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .border(width = 1.dp, color = borderColor, shape = shape),
        shape = shape,
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        content()
    }
}

/**
 * iOS-style sliding pill segmented control
 */
@Composable
fun CupertinoSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    selectedColor: Color = MaterialTheme.colorScheme.primary
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(3.dp)
            .height(42.dp)
    ) {
        val segmentWidth = maxWidth / items.size
        val animatedOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = tween(durationMillis = 240),
            label = "pill_offset"
        )

        // Floating thumb pill
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(11.dp))
                .clip(RoundedCornerShape(11.dp))
                .background(selectedColor)
        )

        // Labels row
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, text ->
                val isSelected = selectedIndex == index
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(180),
                    label = "text_color"
                )

                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onIndexSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = textColor,
                        fontFamily = BengaliBodyFontFamily,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Cupertino stat pill badge
 */
@Composable
fun CupertinoPillBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = BengaliBodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.5.sp
        )
    }
}
