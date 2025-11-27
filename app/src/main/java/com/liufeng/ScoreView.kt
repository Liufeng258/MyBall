package com.liufeng.ballfight

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.text.NumberFormat

class ScoreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    
    private val gradientPaint = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, 0f,
            Color.parseColor("#88FFB6C1"),
            Color.parseColor("#7CFC00"),
            Shader.TileMode.CLAMP
        )
    }

    fun updateScore(score: Long) {
        text = when {
            score >= 1_000_000_000_000L -> {
                paint.shader = gradientPaint.shader
                "${resources.getString(R.string.title_hungry_cat)} ${formatNumber(score)}"
            }
            else -> formatNumber(score)
        }
    }

    private fun formatNumber(number: Long): String {
        return NumberFormat.getNumberInstance().format(number)
    }
}