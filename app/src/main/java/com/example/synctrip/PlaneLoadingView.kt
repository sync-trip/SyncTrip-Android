package com.example.synctrip

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.content.res.AppCompatResources
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class PlaneLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dp = resources.displayMetrics.density

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#006492")
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * dp
        pathEffect = DashPathEffect(floatArrayOf(7f * dp, 7f * dp), 0f)
    }

    private val planeDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_plane_orbit)
    private val planeHalf = (11 * dp).toInt()

    private var angle = 0f

    private val animator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 2500
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { angle = it.animatedValue as Float; invalidate() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f - planeHalf * 1.5f

        canvas.drawCircle(cx, cy, radius, circlePaint)

        val rad = Math.toRadians((angle - 90.0))
        val px = cx + radius * cos(rad).toFloat()
        val py = cy + radius * sin(rad).toFloat()

        planeDrawable?.let { d ->
            canvas.save()
            canvas.translate(px, py)
            canvas.rotate(angle + 90f)  // 접선 방향 = angle + 90°
            d.setBounds(-planeHalf, -planeHalf, planeHalf, planeHalf)
            d.draw(canvas)
            canvas.restore()
        }
    }
}
