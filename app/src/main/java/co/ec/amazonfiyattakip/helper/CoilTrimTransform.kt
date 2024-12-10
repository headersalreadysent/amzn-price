package co.ec.amazonfiyattakip.helper

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

class CoilTrimTransform : Transformation {
    override val cacheKey: String = "TrimEdgesTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val trimmedBitmap = trimEdges(input)
        return trimmedBitmap ?: input
    }

    private fun trimEdges(input: Bitmap): Bitmap? {
        val width = input.width
        val height = input.height

        var top = 0
        var left = 0
        var right = width
        var bottom = height

        val pixels = IntArray(width * height)
        input.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            if (!isTransparentOrWhiteRow(pixels, y, width)) {
                top = y
                break
            }
        }

        for (y in height - 1 downTo 0) {
            if (!isTransparentOrWhiteRow(pixels, y, width)) {
                bottom = y + 1
                break
            }
        }

        for (x in 0 until width) {
            if (!isTransparentOrWhiteColumn(pixels, x, width, height, top, bottom)) {
                left = x
                break
            }
        }

        for (x in width - 1 downTo 0) {
            if (!isTransparentOrWhiteColumn(pixels, x, width, height, top, bottom)) {
                right = x + 1
                break
            }
        }

        if (top >= bottom || left >= right) return null
        return Bitmap.createBitmap(input, left, top, right - left, bottom - top)
    }

    private fun isTransparentOrWhiteRow(pixels: IntArray, row: Int, width: Int): Boolean {
        val start = row * width
        val end = start + width
        for (i in start until end) {
            if (!isTransparentOrWhite(pixels[i])) return false
        }
        return true
    }

    private fun isTransparentOrWhiteColumn(
        pixels: IntArray,
        column: Int,
        width: Int,
        height: Int,
        top: Int,
        bottom: Int
    ): Boolean {
        for (y in top until bottom) {
            val i = y * width + column
            if (!isTransparentOrWhite(pixels[i])) return false
        }
        return true
    }

    private fun isTransparentOrWhite(pixel: Int): Boolean {
        val alpha = (pixel shr 24) and 0xFF
        val red = (pixel shr 16) and 0xFF
        val green = (pixel shr 8) and 0xFF
        val blue = pixel and 0xFF
        return alpha == 0 || (red > 240 && green > 240 && blue > 240)
    }
}