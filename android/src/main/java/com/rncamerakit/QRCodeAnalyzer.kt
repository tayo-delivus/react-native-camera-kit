class QRCodeAnalyzer(
    private val onQRCodesDetected: (qrCodes: List<Barcode>, imageSize: Size) -> Unit,
    private val scanThrottleDelay: Long = 0L,
    private var normalizedFrame: RectF? = null,
    private var containmentEpsilonPx: Int = 0
) : ImageAnalysis.Analyzer {

    private var lastBarcodeDetectedTime: Long = 0L

    @SuppressLint("UnsafeExperimentalUsageError")
    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: run { image.close(); return }
        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

        val scanner = BarcodeScanning.getClient()
        val crop = image.cropRect ?: Rect(0, 0, inputImage.width, inputImage.height)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val now = System.currentTimeMillis()
                if (scanThrottleDelay > 0 && (now - lastBarcodeDetectedTime) < scanThrottleDelay) {
                    return@addOnSuccessListener
                }

                val filtered = if (normalizedFrame == null) {
                    barcodes
                } else {
                    val framePx = normalizedToCropRect(normalizedFrame!!, crop).let { r ->
                        if (containmentEpsilonPx > 0) Rect(r).apply { inset(-containmentEpsilonPx, -containmentEpsilonPx) } else r
                    }
                    barcodes.filter { bc ->
                        val box = bc.boundingBox ?: return@filter false
                        framePx.contains(box)
                    }
                }

                if (filtered.isNotEmpty()) {
                    lastBarcodeDetectedTime = now
                    // imageSize는 InputImage 기준으로 계속 전달
                    onQRCodesDetected(filtered, Size(inputImage.width, inputImage.height))
                }
            }
            .addOnCompleteListener { image.close() }
    }

    private fun normalizedToCropRect(n: RectF, crop: Rect): Rect {
        val l = crop.left   + (n.left   * crop.width()).coerceIn(0f, crop.width().toFloat())
        val t = crop.top    + (n.top    * crop.height()).coerceIn(0f, crop.height().toFloat())
        val r = crop.left   + (n.right  * crop.width()).coerceIn(0f, crop.width().toFloat())
        val b = crop.top    + (n.bottom * crop.height()).coerceIn(0f, crop.height().toFloat())
        return Rect(
            kotlin.math.min(l, r).toInt(),
            kotlin.math.min(t, b).toInt(),
            kotlin.math.max(l, r).toInt(),
            kotlin.math.max(t, b).toInt()
        )
    }

    private fun Rect.contains(other: Rect): Boolean =
        left <= other.left && top <= other.top && right >= other.right && bottom >= other.bottom
}
