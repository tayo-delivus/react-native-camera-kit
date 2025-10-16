// QRCodeAnalyzer.kt
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
     * 프리뷰 기준 정규화 프레임(0f~1f). 예) RectF(0.2f, 0.3f, 0.8f, 0.7f)
     * null 이면 필터링 없이 동작.
     */
    private var normalizedFrame: RectF? = null,
    /**
     * true: boundingBox 전체가 프레임 내부에 완전히 들어와야 인정
     * false: 교집합 비율(minIntersectRatio)로 판단
     */
    private var requireFullContainment: Boolean = true,
    /**
     * requireFullContainment=false 일 때만 사용. (0.0 ~ 1.0)
     * ex) 0.6f => 바코드 박스의 60% 이상이 프레임과 겹쳐야 인정
     */
    private var minIntersectRatio: Float = 0.6f
) : ImageAnalysis.Analyzer {

    // 마지막 콜백 시간 (ms)
    private var lastBarcodeDetectedTime: Long = 0L

    /** 런타임에 프레임 변경하고 싶을 때 호출 */
    fun updateFrame(frame: RectF?) {
        normalizedFrame = frame
    }

    /** 판정 옵션 런타임 업데이트 */
    fun updateContainmentPolicy(requireFull: Boolean, minIntersect: Float = 0.6f) {
        requireFullContainment = requireFull
        minIntersectRatio = minIntersect.coerceIn(0f, 1f)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: run {
            image.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                // 스로틀
                val now = System.currentTimeMillis()
                if (scanThrottleDelay > 0 && (now - lastBarcodeDetectedTime) < scanThrottleDelay) {
                    return@addOnSuccessListener
                }

                val filtered = if (normalizedFrame == null) {
                    barcodes
                } else {
                    val imgW = inputImage.width
                    val imgH = inputImage.height
                    val framePx = normalizedToImageRect(normalizedFrame!!, imgW, imgH)

                    barcodes.filter { bc ->
                        val box = bc.boundingBox ?: return@filter false
                        if (requireFullContainment) {
                            framePx.contains(box)
                        } else {
                            intersectionRatio(box, framePx) >= minIntersectRatio
                        }
                    }
                }

                if (filtered.isNotEmpty()) {
                    lastBarcodeDetectedTime = now
                    // image.width/height 는 회전 전 버퍼 기준일 수 있어 Size 는 InputImage 기준으로 맞추자
                    onQRCodesDetected(filtered, Size(inputImage.width, inputImage.height))
                }
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    private fun normalizedToImageRect(n: RectF, imgW: Int, imgH: Int): Rect {
        // 정규화(0~1) → 이미지 픽셀 좌표
        val l = (n.left  * imgW).coerceIn(0f, imgW.toFloat())
        val t = (n.top   * imgH).coerceIn(0f, imgH.toFloat())
        val r = (n.right * imgW).coerceIn(0f, imgW.toFloat())
        val b = (n.bottom* imgH).coerceIn(0f, imgH.toFloat())
        // 정렬 보정
        val left = min(l, r).toInt()
        val top = min(t, b).toInt()
        val right = max(l, r).toInt()
        val bottom = max(t, b).toInt()
        return Rect(left, top, right, bottom)
    }

    /** boxA 가 boxB 에 완전히 포함되는지 */
    private fun Rect.contains(other: Rect): Boolean {
        return this.left <= other.left &&
               this.top <= other.top &&
               this.right >= other.right &&
               this.bottom >= other.bottom
    }

    /**
     * 교집합 비율 = (A∩B 면적) / A 면적
     * (바코드 박스의 몇 %가 프레임 안에 들어왔는지)
     */
    private fun intersectionRatio(a: Rect, b: Rect): Float {
        val ix = max(0, min(a.right, b.right) - max(a.left, b.left))
        val iy = max(0, min(a.bottom, b.bottom) - max(a.top, b.top))
        val inter = ix * iy
        val aArea = max(0, a.width()) * max(0, a.height())
        if (aArea == 0) return 0f
        return inter.toFloat() / aArea.toFloat()
    }
}
