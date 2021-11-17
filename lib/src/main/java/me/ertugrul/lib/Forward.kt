package me.ertugrul.lib

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.lifecycle.Lifecycle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

typealias OnAnimationStartOrEndCallBack = (() -> Unit)?

class Forward @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Dynamic attributes
    private var angle = 0f
    private var currentOpacity = OPAQUE
    private var currentCircleOpacity = TRANSPARENT
    private var currentScalePercent = 0f
    private var shiftX = 0f
    private var showArcCenterText = true
    private var showShiftingText = false
    private var radiusScaleSize = 0f
    private var measureTextSize = 0f

    private val centerX get() = width / 2f

    // Core attributes
    private var _circleColor = Color.parseColor(CIRCLE_COLOR)

    private var _itemColor = Color.parseColor(COLOR)

    private var _itemStrokeWidth = STROKE_WIDTH

    private var _itemTextSize = TEXT_SIZE

    private var _sweepAngle = SWEEP_ANGLE

    private var circleColor: Int
        get() = _circleColor
        set(value) {
            paintCircle.color = value
            invalidate()
        }

    private var itemColor: Int
        get() = _itemColor
        set(value) {
            _itemColor = value
            paintArc.color = value
            paintText.color = value
            invalidate()
        }

    private var itemStrokeWidth: Float
        get() = _itemStrokeWidth
        set(value) {
            _itemStrokeWidth = value
            paintArc.strokeWidth = value
            paintText.strokeWidth = value
            invalidate()
        }

    private var itemTextSize: Float
        get() = _itemTextSize
        set(value) {
            _itemTextSize = value
            paintText.textSize = itemTextSize
            invalidate()
        }

    private var sweepAngle: Float
        get() = _sweepAngle
        set(value) {
            _sweepAngle = value
            invalidate()
        }

    private var arrowMargin = ARROW_MARGIN

    private var arcMargin = ARC_MARGIN

    private var textInput = TEXT_INPUT

    private var animationDuration = ANIMATION_DURATION

    private var arcRotationAngle = ARC_ROTATION_ANGLE

    private var endScale = END_SCALE

    private val pointStart = PointF()
    private val pointEnd = PointF()

    private val arcBorderRect = RectF()
    private val arcCenterPointF = PointF()

    // Listeners
    private var onAnimationStartListener: OnAnimationStartListener? = null
    private var onAnimationEndListener: OnAnimationEndListener? = null

    var onAnimationStart: OnAnimationStartOrEndCallBack = null
    var onAnimationEnd: OnAnimationStartOrEndCallBack = null

    private var alphaAnimator: ValueAnimator? = null
    private var rotateAnimator: ValueAnimator? = null
    private var scaleAnimator: ValueAnimator? = null
    private var translationAnimator: ValueAnimator? = null

    fun setSeekForwardInSeconds(seconds:Int){
        textInput = seconds
        invalidate()
    }

    // Paints
    private val paintArc = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = itemColor
        strokeWidth = itemStrokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val paintCircle = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = circleColor
        alpha = currentCircleOpacity
    }

    private val paintText = Paint().apply {
        alpha = currentOpacity
        isAntiAlias = true
        style = Paint.Style.FILL
        color = itemColor
        textSize = itemTextSize
        strokeWidth = itemStrokeWidth
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.Forward,
            defStyleAttr,
            0
        )
        try {
            itemTextSize = typedArray.getDimension(R.styleable.Forward_f_textSize, TEXT_SIZE)
            textInput = typedArray.getInteger(R.styleable.Forward_f_textInput, TEXT_INPUT)
            itemColor =
                typedArray.getColor(R.styleable.Forward_f_color, Color.parseColor(COLOR))
            circleColor =
                typedArray.getColor(R.styleable.Forward_f_circle_color, Color.parseColor(CIRCLE_COLOR))
            itemStrokeWidth =
                typedArray.getDimension(R.styleable.Forward_f_strokeWidth, STROKE_WIDTH)
            animationDuration = typedArray.getInteger(
                R.styleable.Forward_f_animationDuration,
                ANIMATION_DURATION
            )
            arcRotationAngle = typedArray.getFloat(
                R.styleable.Forward_f_arcRotationAngle,
                ARC_ROTATION_ANGLE
            )
            arcMargin = typedArray.getDimension(R.styleable.Forward_f_arrowSize, ARC_MARGIN)
            sweepAngle = typedArray.getFloat(R.styleable.Forward_f_sweepAngle, SWEEP_ANGLE)
            endScale = typedArray.getInteger(
                R.styleable.Forward_f_scalePercent,
                SCALE_PERCENT
            ).toFloat() / 100

            arrowMargin = arcMargin * 10 / 13
            animationDuration = animationDuration * 10 / 27
            measureTextSize = paintText.measureText("+$textInput")
        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        arcBorderRect.set(
            arrowMargin / 2,
            arcMargin,
            centerX - arcMargin,
            height.toFloat() - arrowMargin / 2
        )
        arcCenterPointF.x = (arcBorderRect.right - arcBorderRect.left) / 2 + arrowMargin / 2
        arcCenterPointF.y = (arcBorderRect.bottom - arcBorderRect.top) / 2 + arcMargin
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        radiusScaleSize = arcCenterPointF.x * currentScalePercent

        arcBorderRect.left = arrowMargin / 2 + radiusScaleSize
        arcBorderRect.top = arcMargin + radiusScaleSize
        arcBorderRect.right = centerX - arcMargin - radiusScaleSize
        arcBorderRect.bottom = height.toFloat() - arrowMargin / 2 - radiusScaleSize

        paintCircle.alpha = currentCircleOpacity
        paintText.alpha = currentOpacity

        //draw arc
        canvas?.drawArc(
            arcBorderRect,
            START_ANGLE + angle,
            sweepAngle,
            false,
            paintArc
        )
        // draw centered circle
        canvas?.drawCircle(
            arcCenterPointF.x,
            arcCenterPointF.y,
            (centerX - arcMargin - arrowMargin / 2) / 2 - radiusScaleSize,
            paintCircle
        )

        //first arrow head
        pointStart.x = arcCenterPointF.x
        pointStart.y = arcMargin + radiusScaleSize
        pointEnd.x = arcCenterPointF.x - arrowMargin
        pointEnd.y = arcMargin
        drawArrowHead(canvas, pointStart, pointEnd)

        //second arrow head
        pointStart.x = arcCenterPointF.x + arrowMargin
        pointStart.y = arcMargin + radiusScaleSize
        pointEnd.x = arcCenterPointF.x
        pointEnd.y = arcMargin
        drawArrowHead(canvas, pointStart, pointEnd)

        val textHeight = paintText.descent() + paintText.ascent()
        if (showArcCenterText) {
            canvas?.drawText(
                textInput.toString(),
                arcCenterPointF.x,
                arcCenterPointF.y - textHeight / 2,
                paintText
            )
        }
        if (showShiftingText) {
            canvas?.drawText(
                "+$textInput",
                arcBorderRect.right + measureTextSize / 2 + shiftX,
                arcCenterPointF.y - textHeight / 2,
                paintText
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && arcBorderRect.contains(event.x, event.y)) {
            invokeAnimation()
        }
        return true
    }

    private fun updateArcCenterTextVisibility(){
        showArcCenterText = !((alphaAnimator != null && alphaAnimator!!.isRunning)
                || (rotateAnimator != null && rotateAnimator!!.isRunning)
                || (scaleAnimator != null && scaleAnimator!!.isRunning)
                || (translationAnimator != null && translationAnimator!!.isRunning))
    }

    fun invokeAnimation() {
        onAnimationStartListener?.onAnimationStart()
        onAnimationStart?.invoke()

        // arc centered text fade animation
        alphaAnimation(
            OPAQUE,
            TRANSPARENT,
            DecelerateInterpolator(),
            animationDuration / 2L,
            false
        ).doOnEnd {
            currentOpacity = OPAQUE
            showArcCenterText = false
        }
        // arc scale down animation
        scaleAnimation(
            START_SCALE,
            endScale,
            LinearInterpolator(),
            animationDuration / 4L
        ).doOnEnd {
            // rotate arc animation
            rotateAnimation(
                0f,
                arcRotationAngle,
                DecelerateInterpolator(4f),
                animationDuration / 2L
            ).doOnEnd {
                // arc scale up animation
                scaleAnimation(
                    endScale,
                    START_SCALE,
                    DecelerateInterpolator(2f),
                    (animationDuration.toLong() * 17L) / 10L
                )
                // reverse rotate arc animation
                rotateAnimation(
                    arcRotationAngle,
                    0f,
                    DecelerateInterpolator(2f),
                    (animationDuration.toLong() * 17L) / 10L
                )
            }
            // circle appear animation
            alphaAnimation(
                TRANSPARENT,
                (OPAQUE * 6) / 10,
                DecelerateInterpolator(),
                animationDuration / 4L,
                true
            ).doOnEnd {
                currentCircleOpacity = TRANSPARENT
                showShiftingText = true
                // translate the shifting text
                translateXAnimation(
                    arrowMargin / 2,
                    centerX - measureTextSize + arcMargin,
                    DecelerateInterpolator(2f),
                    (animationDuration.toLong() * 17L) / 10L
                )
                // shifting text alpha animation
                alphaAnimation(
                    OPAQUE * 3 / 10,
                    OPAQUE,
                    DecelerateInterpolator(2f),
                    (animationDuration.toLong() * 17L) / 10L,
                    false
                ).doOnEnd {
                    // shifting text fade animation
                    alphaAnimation(
                        OPAQUE,
                        TRANSPARENT,
                        DecelerateInterpolator(),
                        animationDuration.toLong() / 4L,
                        false
                    ).doOnEnd {
                        updateArcCenterTextVisibility()
                        showShiftingText = false
                        shiftX = 0f
                        // arc centered text appear animation
                        alphaAnimation(
                            TRANSPARENT,
                            OPAQUE,
                            LinearInterpolator(),
                            animationDuration.toLong() / 4L,
                            false
                        ).doOnEnd {
                            onAnimationEndListener?.onAnimationEnd()
                            onAnimationEnd?.invoke()
                        }
                    }
                }
            }
        }
    }

    private fun alphaAnimation(
        start: Int,
        end: Int,
        timeInterpolator: TimeInterpolator,
        animationDuration: Long,
        isCircle: Boolean
    ) : ValueAnimator {
        alphaAnimator = ValueAnimator.ofInt().apply {
            setIntValues(start, end)
            addUpdateListener {
                if (isCircle) {
                    currentCircleOpacity = it.animatedValue as Int
                } else {
                    currentOpacity = it.animatedValue as Int
                }
                invalidate()
            }
            duration = animationDuration
            interpolator = timeInterpolator
            start()
        }
        return alphaAnimator!!
    }

    private fun rotateAnimation(
        start: Float,
        end: Float,
        timeInterpolator: TimeInterpolator,
        animationDuration: Long
    ) : ValueAnimator {
        rotateAnimator = ValueAnimator.ofFloat().apply {
            setFloatValues(start, end)
            addUpdateListener {
                angle = it.animatedValue as Float
                invalidate()
            }
            duration = animationDuration
            interpolator = timeInterpolator
            start()
        }
        return rotateAnimator!!
    }

    private fun scaleAnimation(
        start: Float,
        end: Float,
        timeInterpolator: TimeInterpolator,
        animationDuration: Long
    ) : ValueAnimator{
        scaleAnimator = ValueAnimator.ofFloat().apply {
            setFloatValues(start, end)
            addUpdateListener {
                currentScalePercent = animatedValue as Float
                invalidate()
            }
            interpolator = timeInterpolator
            duration = animationDuration
            start()
        }
        return scaleAnimator!!
    }

    private fun translateXAnimation(
        start: Float,
        end: Float,
        timeInterpolator: TimeInterpolator,
        animationDuration: Long
    ) : ValueAnimator {
        translationAnimator = ValueAnimator.ofFloat().apply {
            setFloatValues(start, end)
            addUpdateListener {
                shiftX = it.animatedValue as Float
                invalidate()
            }
            duration = animationDuration
            interpolator = timeInterpolator
            start()
        }
        return translationAnimator!!
    }

    private fun drawArrowHead(canvas: Canvas?, start: PointF, end: PointF) {
        end.y = arcMargin - arrowMargin + radiusScaleSize
        val scaleUpPart = calculateScale(start, end, true)
        // Draw up part
        canvas?.drawLine(
            rotateX(start.x, start.y),
            rotateY(start.x, start.y),
            rotateX(scaleUpPart.x, scaleUpPart.y),
            rotateY(scaleUpPart.x, scaleUpPart.y),
            paintArc
        )

        end.y = arcMargin + arrowMargin + radiusScaleSize
        val scaleDownPart = calculateScale(start, end, false)
        // Draw down part
        canvas?.drawLine(
            rotateX(start.x, start.y),
            rotateY(start.x, start.y),
            rotateX(scaleDownPart.x, scaleDownPart.y),
            rotateY(scaleDownPart.x, scaleDownPart.y),
            paintArc
        )
    }

    private fun calculateScale(p1: PointF, p2: PointF, isUp: Boolean): PointF {
        val x: Float
        val y: Float
        if (isUp) {
            x = p2.x + (p1.x - p2.x) * currentScalePercent
            y = p2.y + (p1.y - p2.y) * currentScalePercent
        } else {
            x = p2.x + (p1.x - p2.x) * currentScalePercent
            y = p2.y - (p2.y - p1.y) * currentScalePercent
        }

        return PointF(x, y)
    }

    private fun rotateX(x: Float, y: Float): Float =
        (((x - arcCenterPointF.x) * cos(angle.toRadian())) - ((y - arcCenterPointF.y) * sin(angle.toRadian()))) + arcCenterPointF.x

    private fun rotateY(x: Float, y: Float): Float =
        (((y - arcCenterPointF.y) * cos(angle.toRadian())) + ((x - arcCenterPointF.x) * sin(angle.toRadian()))) + arcCenterPointF.y

    private fun Float.toRadian(): Float = this * (PI / 180).toFloat()

    fun setOnAnimationStartListener(onAnimationStartListener: OnAnimationStartListener) {
        this.onAnimationStartListener = onAnimationStartListener
    }

    fun setOnAnimationEndListener(onAnimationEndListener: OnAnimationEndListener) {
        this.onAnimationEndListener = onAnimationEndListener
    }

    companion object {
        private const val ARC_MARGIN = 65f
        private const val ARC_ROTATION_ANGLE = 70f
        private const val STROKE_WIDTH = 24f
        private const val ARROW_MARGIN = 50f

        private const val COLOR = "#000000"

        private const val CIRCLE_COLOR = "#000000"

        private const val TEXT_SIZE = 200f
        private const val TEXT_INPUT = 10

        private const val ANIMATION_DURATION = 1000

        private const val SWEEP_ANGLE = 300f
        private const val START_ANGLE = 330f

        private const val START_SCALE = 0f
        private const val END_SCALE = 0.16f
        private const val SCALE_PERCENT = 16

        private const val OPAQUE = 255
        private const val TRANSPARENT = 0
    }
}
