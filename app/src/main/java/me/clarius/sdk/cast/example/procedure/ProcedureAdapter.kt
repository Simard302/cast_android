package me.clarius.sdk.cast.example.procedure

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import me.clarius.sdk.cast.example.R

class ProcedureAdapter(private var procedures: List<Procedure>) :
    RecyclerView.Adapter<ProcedureAdapter.ProcedureViewHolder>() {

    class ProcedureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.procedureNameTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.procedureDescriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcedureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_procedure, parent, false)
        return ProcedureViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProcedureViewHolder, position: Int) {
        val procedure = procedures[position]
        holder.nameTextView.text = procedure.name
        holder.descriptionTextView.text = procedure.description

        holder.itemView.setOnClickListener {
            val action = ProcedureSelectionFragmentDirections.actionProcedureSelectionFragmentToOverlayFragment(procedure.name)
            holder.itemView.findNavController().navigate(action)
        }
    }

    fun updateList(newList: List<Procedure>) {
        procedures = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return procedures.size
    }
}