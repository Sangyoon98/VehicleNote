package com.sangyoon.ocr

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

/**
 * CameraX PreviewView + ML Kit 번호판 인식 Composable.
 * 번호판이 인식되면 해당 위치에 바운딩 박스를 오버레이로 표시하고 2초 후 자동으로 사라진다.
 *
 * @param plateRecognizer 번호판 인식기 (PlateRecognizer 구현체)
 * @param onPlateDetected 번호판 인식 시 콜백 (debounce 충족 후에만 호출)
 * @param modifier Modifier
 */
@Composable
fun CameraPreviewWithRecognition(
    plateRecognizer: PlateRecognizer,
    onPlateDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    // 콜백을 rememberUpdatedState로 감싸 recomposition 시 재설정 방지
    val currentOnPlateDetected by rememberUpdatedState(onPlateDetected)

    // 이미지 정보: 분석 스레드에서 쓰고 메인 스레드에서 읽음 (@Volatile로 가시성 보장)
    val imageInfo = remember {
        object {
            @Volatile var width = 0
            @Volatile var height = 0
            @Volatile var rotation = 0
        }
    }

    // 뷰 크기 — onSizeChanged로 업데이트 (BoxWithConstraints 대비 recomposition 절감)
    var viewWidthPx by remember { mutableIntStateOf(0) }
    var viewHeightPx by remember { mutableIntStateOf(0) }

    // 인식된 바운딩 박스 — ML Kit 콜백(메인 스레드)에서 업데이트
    var lastDetectedBounds by remember { mutableStateOf<android.graphics.Rect?>(null) }
    var showBounds by remember { mutableStateOf(false) }

    // 인식 후 2초 표시, fadeOut 후 null 처리
    LaunchedEffect(showBounds) {
        if (showBounds) {
            delay(2000L)
            showBounds = false
            delay(500L) // fadeOut 애니메이션 완료 대기
            lastDetectedBounds = null
        }
    }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { size ->
                viewWidthPx = size.width
                viewHeightPx = size.height
            }
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageRotationEnabled(true)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                imageInfo.width = imageProxy.width
                                imageInfo.height = imageProxy.height
                                imageInfo.rotation = imageProxy.imageInfo.rotationDegrees
                                plateRecognizer.recognize(imageProxy) { plate, boundingBox ->
                                    if (plate != null) {
                                        lastDetectedBounds = boundingBox
                                        showBounds = true
                                        currentOnPlateDetected(plate)
                                    }
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 번호판 인식 바운딩 박스 오버레이
        AnimatedVisibility(
            visible = showBounds,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(500)),
            modifier = Modifier.fillMaxSize()
        ) {
            val bounds = lastDetectedBounds
            val vw = viewWidthPx.toFloat()
            val vh = viewHeightPx.toFloat()
            if (bounds != null && vw > 0 && imageInfo.width > 0) {
                // ML Kit는 회전이 적용된 디스플레이 좌표계로 바운딩 박스를 반환
                val rotated = imageInfo.rotation == 90 || imageInfo.rotation == 270
                val imgDisplayW = if (rotated) imageInfo.height.toFloat() else imageInfo.width.toFloat()
                val imgDisplayH = if (rotated) imageInfo.width.toFloat() else imageInfo.height.toFloat()

                // FILL_CENTER(CENTER_CROP) 스케일 및 오프셋 계산
                val scale = maxOf(vw / imgDisplayW, vh / imgDisplayH)
                val offsetX = (vw - imgDisplayW * scale) / 2f
                val offsetY = (vh - imgDisplayH * scale) / 2f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = Color(0xFF00E676), // Green A400
                        topLeft = Offset(
                            x = bounds.left * scale + offsetX,
                            y = bounds.top * scale + offsetY,
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            width = (bounds.right - bounds.left) * scale,
                            height = (bounds.bottom - bounds.top) * scale,
                        ),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
    }
}
