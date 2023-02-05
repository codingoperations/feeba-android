package io.least.case_management.ui.cases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.least.case_management.R
import io.least.case_management.data.CaseItem

private const val FOOTER_VIEW = 1

class CaseListAdapter(private val onFooterClick: () -> Unit) :
    ListAdapter<CaseItem, RecyclerView.ViewHolder>(DiffCallback) {


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == FOOTER_VIEW) {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.case_footer, viewGroup, false)
            return FooterViewHolder(view, onFooterClick)
        }
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.case_item, viewGroup, false)
        return CaseItemViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is FooterViewHolder -> {
                // Footer
                viewHolder.textView.text = "Create a new Chat"
            }
            is CaseItemViewHolder -> {
                // Get element from your dataset at this position and replace the
                // contents of the view with that element
                viewHolder.textView.text = getItem(position).name
            }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == currentList.size) {
            // This is where we'll add footer.
            FOOTER_VIEW
        } else super.getItemViewType(position)
    }
}

/**
 * Provide a reference to the type of views that you are using
 * (custom ViewHolder).
 */
class CaseItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textView: TextView

    init {
        // Define click listener for the ViewHolder's View.
        textView = view.findViewById(R.id.textView)
    }
}

// Define a ViewHolder for Footer view
class FooterViewHolder(view: View, clickListener: () -> Unit) : RecyclerView.ViewHolder(view) {

    val textView: TextView

    init {
        // Define click listener for the ViewHolder's View.
        textView = view.findViewById(R.id.textView)
        view.setOnClickListener { clickListener.invoke() }
    }
}

private object DiffCallback : DiffUtil.ItemCallback<CaseItem>() {
    override fun areItemsTheSame(oldItem: CaseItem, newItem: CaseItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: CaseItem, newItem: CaseItem): Boolean {
        return oldItem.id == newItem.id
    }
}