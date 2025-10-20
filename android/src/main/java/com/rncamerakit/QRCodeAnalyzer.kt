package com.rncamerakit

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.math.max
import kotlin.math.min

class QRCodeAnalyzer(
    private val onQRCodesDetected: (qrCodes: List<Barcode>, imageSize: Size) -> Unit,
    private val scanThrottleDelay: Long = 0L,
    /**
     * 프리뷰 기준 정규화 프레임(0~1). 예: RectF(0.2f, 0.3f, 0.8f, 0.7f)
     * null이면 필터링 없이 전부 통과.
     */
    private var normalizedFrame: RectF? = null,
    /**
     * 경계 오차 허용 px. 0이면 진짜 완전 포함.
     * 1~2 정도 주면 라운딩/회전 오차 허용.
     */
    private var containmentEpsilonPx: Int = 0
) : ImageAnalysis.Analyzer {

    private var lastBarcodeDetectedTime: Long = 0L

    fun updateFrame(frame: RectF?) {
        normalizedFrame = frame
    }

    fun updateContainmentEpsilon(epsilonPx: Int) {
        containmentEpsilonPx = max(0, epsilonPx)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: run { image.close(); return }
        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        // CameraX가 Preview/Analysis에 적용한 실제 센터 크롭 영역
        val crop: Rect = image.cropRect

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val now = System.currentTimeMillis()
                if (scanThrottleDelay > 0 && (now - lastBarcodeDetectedTime) < scanThrottleDelay) {
                    return@addOnSuccessListener
                }

                val result: List<Barcode> = if (normalizedFrame == null) {
                    barcodes
                } else {
                    val framePx = normalizedToCropRect(normalizedFrame!!, crop).let { r ->
                        if (containmentEpsilonPx > 0) {
                            Rect(r).apply { inset(-containmentEpsilonPx, -containmentEpsilonPx) }
                        } else r
                    }
                    barcodes.filter { bc ->
                        val box = bc.boundingBox ?: return@filter false
                        framePx.containsRect(box)
                    }
                }

                if (result.isNotEmpty()) {
                    lastBarcodeDetectedTime = now
                    onQRCodesDetected(result, Size(inputImage.width, inputImage.height))
                }
            }
            .addOnCompleteListener { image.close() }
    }

    private fun normalizedToCropRect(n: RectF, crop: Rect): Rect {
        val l = crop.left + (n.left * crop.width())
        val t = crop.top + (n.top * crop.height())
        val r = crop.left + (n.right * crop.width())
        val b = crop.top + (n.bottom * crop.height())
        val left = min(l, r).toInt()
        val top = min(t, b).toInt()
        val right = max(l, r).toInt()
        val bottom = max(t, b).toInt()
        return Rect(left, top, right, bottom)
    }

    /** this.contains(other) : other(바코드 박스)가 this(프레임) 안에 완전 포함? */
    private fun Rect.containsRect(other: Rect): Boolean {
        return this.left <= other.left &&
               this.top <= other.top &&
               this.right >= other.right &&
               this.bottom >= other.bottom
    }
}
