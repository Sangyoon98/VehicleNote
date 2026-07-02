package com.sangyoon.ocr

import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
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
import kotlinx.coroutines.isActive
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * CameraX PreviewView + ML Kit 번호판 인식 Composable.
 * 번호판이 인식되면 해당 위치에 바운딩 박스를 오버레이로 표시하고 2초 후 자동으로 사라진다.
 *
 * 인식률 개선을 위해:
 * - 분석 해상도 1280x720 (원거리·움직이는 번호판의 글자 픽셀 확보)
 * - 리티클 영역에 주기적 AF/AE 측광 (저조도·역광에서 번호판 초점·노출 우선)
 * - [analysisEnabled]가 false면 프레임 분석을 중단 (확인 다이얼로그 표시 중
 *   다음 차량 인식 결과가 버려지는 문제 방지 + 배터리 절약)
 *
 * @param plateRecognizer 번호판 인식기 (PlateRecognizer 구현체)
 * @param onPlateDetected 번호판 인식 시 콜백 (debounce 충족 후에만 호출)
 * @param modifier Modifier
 * @param analysisEnabled false면 카메라 프리뷰는 유지하되 OCR 분석을 일시 중지
 */
@Composable
fun CameraPreviewWithRecognition(
    plateRecognizer: PlateRecognizer,
    onPlateDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
    analysisEnabled: Boolean = true,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    // 콜백/플래그를 rememberUpdatedState로 감싸 recomposition 시 재설정 방지
    val currentOnPlateDetected by rememberUpdatedState(onPlateDetected)
    val currentAnalysisEnabled = rememberUpdatedState(analysisEnabled)

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

    // 바인딩된 카메라 — 주기적 측광에 사용
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // 인식 후 2초 표시, fadeOut 후 null 처리
    LaunchedEffect(showBounds) {
        if (showBounds) {
            delay(2000L)
            showBounds = false
            delay(500L) // fadeOut 애니메이션 완료 대기
            lastDetectedBounds = null
        }
    }

    // 리티클 영역(중앙 약간 위)에 주기적 AF/AE 측광 — 번호판에 초점·노출을 맞춰
    // 저조도/역광 환경에서 번호판 영역이 적정 노출로 잡히게 한다.
    LaunchedEffect(boundCamera) {
        val camera = boundCamera ?: return@LaunchedEffect
        while (isActive) {
            val previewView = previewViewRef
            if (previewView != null && previewView.width > 0 && previewView.height > 0) {
                val point = previewView.meteringPointFactory.createPoint(
                    previewView.width * 0.5f,
                    previewView.height * RETICLE_CENTER_Y_RATIO,
                )
                val action = FocusMeteringAction.Builder(
                    point,
                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE,
                )
                    .setAutoCancelDuration(METERING_INTERVAL_MS, TimeUnit.MILLISECONDS)
                    .build()
                runCatching { camera.cameraControl.startFocusAndMetering(action) }
            }
            delay(METERING_INTERVAL_MS)
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
                previewViewRef = previewView

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setResolutionSelector(
                            ResolutionSelector.Builder()
                                .setResolutionStrategy(
                                    ResolutionStrategy(
                                        Size(1280, 720),
                                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                                    )
                                )
                                .build()
                        )
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageRotationEnabled(true)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                if (!currentAnalysisEnabled.value) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }
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
                        boundCamera = cameraProvider.bindToLifecycle(
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

/** 리티클 중심의 세로 위치 비율 — EntryExitScreen의 리티클이 중앙보다 약간 위에 있다 */
private const val RETICLE_CENTER_Y_RATIO = 0.45f

/** AF/AE 측광 반복 주기 */
private const val METERING_INTERVAL_MS = 3000L
