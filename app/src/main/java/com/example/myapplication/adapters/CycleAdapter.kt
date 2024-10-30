// CycleAdapter.kt
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.helperClasses.Cycle

class CycleAdapter(private val context: Context, private val cycles: List<Cycle>) : BaseAdapter() {

    override fun getCount(): Int = cycles.size

    override fun getItem(position: Int): Cycle = cycles[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_cycle, parent, false)

        val cycle = getItem(position)

        val dateTextView = view.findViewById<TextView>(R.id.cycleDateTextView)
        val lengthTextView = view.findViewById<TextView>(R.id.cycleLengthTextView)
        val symptomsTextView = view.findViewById<TextView>(R.id.cycleSymptomsTextView)

        dateTextView.text = "${cycle.startDate} - ${cycle.endDate}"
        lengthTextView.text = "Длина: ${cycle.length} дней"
        symptomsTextView.text = "Симптомы: ${cycle.symptoms}"

        return view
    }
}
