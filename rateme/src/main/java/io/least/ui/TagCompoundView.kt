package io.least.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import io.least.rate.R

class TagCompoundView : LinearLayout {
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private var onTagSelectedListener: OnTagSelectedListener? = null
    lateinit var imageViewTag: ImageView
    private lateinit var tagText: TextView

    private fun init(context: Context) {
        // Inflate the existing XML layout
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_selectable_tag, this, true)
        imageViewTag = view.findViewById(R.id.imageViewTag)
        tagText = view.findViewById(R.id.textViewTagText)
        isClickable = true
        orientation = VERTICAL
        gravity = Gravity.CENTER

        setBackgroundResource(R.drawable.selector_tag)
        dpToPx(6f, context).let {
            setPadding(it, it, it, it)
        }
    }

    fun setTagText(text: String) {
        tagText.text = text
        invalidate()
    }

    fun setTagImage(imageUrl: String) {
        imageViewTag.visibility = VISIBLE
        Glide.with(context)
            .load(imageUrl)
            .into(imageViewTag)
    }

    fun setOnTagSelectedListener(listener: OnTagSelectedListener) {
        this.onTagSelectedListener = listener
    }

    override fun performClick(): Boolean {
        super.performClick()
        this.isSelected = !this.isSelected
        onTagSelectedListener?.onTagSelected(isSelected)
        return false
    }
}

fun interface OnTagSelectedListener {
    fun onTagSelected(isSelected: Boolean)
}
