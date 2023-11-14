import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import io.feeba.R


class CircleVew(context: Context) : FrameLayout(context) {
    private val imageView: ImageView

    init {
        addView(ImageView(context).apply {
            imageView = this
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setImageResource(R.drawable.question_circle)
            setColorFilter(resources.getColor(android.R.color.holo_blue_dark), PorterDuff.Mode.SRC_IN)
        })
    }

    fun startVibrationAnimation() {
        val rotateLeft = ObjectAnimator.ofFloat(this, "rotation", 0f, -10f)
        val rotateRight = ObjectAnimator.ofFloat(this, "rotation", -10f, 10f)
        val rotateCenter = ObjectAnimator.ofFloat(this, "rotation", 10f, 0f)

        rotateLeft.duration = 100
        rotateRight.duration = 200
        rotateCenter.duration = 100

        rotateLeft.repeatCount = 3 // 0-based, so 3 means it repeats 4 times

        rotateRight.repeatCount = 3
        rotateCenter.repeatCount = 3

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(rotateLeft, rotateRight, rotateCenter)
        animatorSet.interpolator = AccelerateDecelerateInterpolator()

        animatorSet.start()
    }
}