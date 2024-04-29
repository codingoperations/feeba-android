package sample.project.project_list.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.feeba.Feeba
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.least.demo.R
import sample.data.Project

class ProjectListAdapter(
    private val dataSet: List<Project>,
    private val itemClicked: (project: Project) -> Unit
) :
    RecyclerView.Adapter<ViewHolder>() {

    init {
        Logger.log(LogLevel.DEBUG, "EventsAdapter::init: ${dataSet.size}")
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_project_list, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textViewEvent.text = dataSet[position].name
        viewHolder.textViewExtras.text = dataSet[position].created
        viewHolder.triggerButton.setOnClickListener {
            itemClicked(dataSet[position])
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        Logger.log(LogLevel.DEBUG, "EventsAdapter::getItemCount: ${dataSet.size}")
        return dataSet.size
    }
}

/**
 * Provide a reference to the type of views that you are using
 * (custom ViewHolder)
 */
class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textViewEvent: TextView
    val textViewExtras: TextView
    val triggerButton: Button

    init {
        // Define click listener for the ViewHolder's View
        textViewEvent = view.findViewById(R.id.textViewProjectName)
        textViewExtras = view.findViewById(R.id.textViewExtra)
        triggerButton = view.findViewById(R.id.buttonOpenProject)
    }
}

data class EventTriggerUiModel(
    val event: String,
    val description: String,
)