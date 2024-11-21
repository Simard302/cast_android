package me.clarius.sdk.cast.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController

class ProcedureSelectionFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_procedure_selection, container, false)

        val spinner = view.findViewById<Spinner>(R.id.spinner_procedures)
        val buttonStart = view.findViewById<Button>(R.id.button_start)

        // Set up the spinner with procedure names
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.procedures_array, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        buttonStart.setOnClickListener { v: View? ->
            val selectedProcedure = spinner.selectedItem.toString()
            val action =
                ProcedureSelectionFragmentDirections.actionProcedureSelectionFragmentToSlidesFragment(
                    selectedProcedure
                )
            findNavController(view).navigate(action)
        }

        return view
    }
}