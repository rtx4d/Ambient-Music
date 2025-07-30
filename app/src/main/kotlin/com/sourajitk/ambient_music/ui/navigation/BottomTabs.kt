package com.sourajitk.ambient_music.ui.navigation

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.expressa.m3.motion.MotionScheme
import com.kyant.expressa.m3.shape.CornerShape
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ui.LocalContentColor
import com.kyant.liquidglass.GlassStyle
import com.kyant.liquidglass.LiquidGlassProviderState
import com.kyant.liquidglass.liquidGlass
import com.kyant.liquidglass.liquidGlassProvider
import com.kyant.liquidglass.material.GlassMaterial
import com.kyant.liquidglass.refraction.InnerRefraction
import com.kyant.liquidglass.refraction.RefractionAmount
import com.kyant.liquidglass.refraction.RefractionHeight
import com.kyant.liquidglass.rememberLiquidGlassProviderState
import com.kyant.liquidglass.sampler.ContinuousLuminanceSampler
import com.kyant.liquidglass.sampler.ExperimentalLuminanceSamplerApi
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLuminanceSamplerApi::class)
@Composable
fun <T> BottomTabs(
  tabs: List<T>,
  selectedTabState: MutableState<T>,
  liquidGlassProviderState: LiquidGlassProviderState,
  background: Color,
  modifier: Modifier = Modifier,
  content: BottomTabsScope.(tab: T) -> BottomTabsScope.BottomTab,
  onTabSelected: (MainNavTab) -> Unit,
) {
  val bottomTabsLiquidGlassProviderState = rememberLiquidGlassProviderState(null)

  val animationScope = rememberCoroutineScope()
  val initialContentColor = onSurface
  val contentColor = remember { Animatable(initialContentColor) }
  val luminanceSampler = remember {
    ContinuousLuminanceSampler { animationSpec, luminance ->
      val isLight = luminance.pow(2f) > 0.5f
      animationScope.launch {
        contentColor.animateTo(
          if (isLight) Color.Black else Color.White,
          tween(300, 0, LinearEasing),
        )
      }
    }
  }

  val scope = remember { BottomTabsScope() }

  val density = LocalDensity.current
  var isDragging by remember { mutableStateOf(false) }
  val offset = remember { Animatable(0f) }

  val padding = 4.dp
  val paddingPx = with(density) { padding.roundToPx() }
  val itemBackground = primaryContainer

  BoxWithConstraints(
    modifier.height(64.dp).fillMaxWidth().pointerInput(Unit) { detectTapGestures {} }
  ) {
    val widthWithoutPaddings =
      (constraints.maxWidth.toFloat() - paddingPx * 2f).fastCoerceAtLeast(0f)
    val tabWidth = widthWithoutPaddings / tabs.size
    val maxWidth = (widthWithoutPaddings - tabWidth).fastCoerceAtLeast(0f)

    Row(
      Modifier.liquidGlassProvider(bottomTabsLiquidGlassProviderState)
        .liquidGlass(liquidGlassProviderState, luminanceSampler = luminanceSampler) {
          val luminance = luminanceSampler.luminance.pow(2f)

          GlassStyle(
            CornerShape.full,
            innerRefraction =
              InnerRefraction(height = RefractionHeight(12.dp), amount = RefractionAmount.Half),
            material =
              if (luminance > 0.5f) {
                GlassMaterial(
                  brush = SolidColor(Color.White),
                  alpha = (luminance - 0.5f) * 2f * 0.8f,
                )
              } else {
                GlassMaterial(
                  brush = SolidColor(Color.Black),
                  alpha = (0.5f - luminance) * 2f * 0.3f,
                )
              },
          )
        }
        .fillMaxSize()
        .padding(padding),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      CompositionLocalProvider(LocalContentColor providesComputed { contentColor.value }) {
        tabs.forEach { tab ->
          key(tab) {
            val itemBackgroundAlpha by
              animateFloatAsState(
                if (selectedTabState.value == tab && !isDragging) {
                  0.8f
                } else {
                  0f
                },
                MotionScheme.slowEffects(),
              )

            scope
              .content(tab)
              .Content(
                Modifier.clip(CornerShape.full)
                  .drawBehind { drawRect(itemBackground, alpha = itemBackgroundAlpha) }
                  .pointerInput(Unit) {
                    detectTapGestures {
                      if (selectedTabState.value != tab) {
                        selectedTabState.value = tab
                        animationScope.launch {
                          launch {
                            offset.animateTo(
                              (tabs.indexOf(tab) * tabWidth).fastCoerceIn(0f, maxWidth),
                              MotionScheme.slowSpatial(),
                            )
                          }
                          launch {
                            isDragging = true
                            delay(200)
                            isDragging = false
                          }
                        }
                      }
                    }
                  }
                  .weight(1f)
              )
          }
        }
      }
    }

    val scaleXFraction by animateFloatAsState(if (!isDragging) 0f else 1f, spring(0.5f, 300f))
    val scaleYFraction by animateFloatAsState(if (!isDragging) 0f else 1f, spring(0.5f, 600f))

    Spacer(
      Modifier.layout { measurable, constraints ->
          val width = tabWidth.fastRoundToInt()
          val height = 56.dp.roundToPx()
          val placeable =
            measurable.measure(
              Constraints.fixed(
                (width * lerp(1f, 1.5f, scaleXFraction)).fastRoundToInt(),
                (height * lerp(1f, 1.5f, scaleYFraction)).fastRoundToInt(),
              )
            )

          layout(width, height) {
            placeable.place(
              (width - placeable.width) / 2 + paddingPx,
              (height - placeable.height) / 2 + paddingPx,
            )
          }
        }
        .drawWithContent {
          translate(0f, lerp(0f, 4f, scaleYFraction).dp.toPx()) {
            this@drawWithContent.drawContent()
          }
        }
        .graphicsLayer {
          translationX = offset.value
          scaleX = lerp(1f, 0.9f, scaleXFraction)
          scaleY = lerp(1f, 0.9f, scaleYFraction)
          transformOrigin = TransformOrigin(0f, 0f)
        }
        .background(background, CornerShape.full)
        .liquidGlass(
          bottomTabsLiquidGlassProviderState,
          GlassStyle(
            CornerShape.full,
            innerRefraction =
              InnerRefraction(
                height =
                  RefractionHeight(animateFloatAsState(if (!isDragging) 0f else 10f).value.dp),
                amount = RefractionAmount.Half,
              ),
            material = GlassMaterial.None,
          ),
        )
        .draggable(
          rememberDraggableState { delta ->
            animationScope.launch {
              offset.snapTo((offset.value + delta).fastCoerceIn(0f, maxWidth))
            }
          },
          Orientation.Horizontal,
          startDragImmediately = true,
          onDragStarted = { isDragging = true },
          onDragStopped = { velocity ->
            isDragging = false
            val currentIndex = offset.value / tabWidth
            val targetIndex =
              when {
                velocity > 0f -> ceil(currentIndex).toInt()
                velocity < 0f -> floor(currentIndex).toInt()
                else -> currentIndex.fastRoundToInt()
              }.fastCoerceIn(0, tabs.lastIndex)
            onTabSelected(tabs[targetIndex] as MainNavTab)
            animationScope.launch {
              offset.animateTo(
                (targetIndex * tabWidth).fastCoerceIn(0f, maxWidth),
                MotionScheme.defaultSpatial(),
              )
            }
          },
        )
    )
  }
}
