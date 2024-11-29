package me.clarius.sdk.cast.example.procedure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.clarius.sdk.cast.example.R

class ProcedureSelectionFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var procedureAdapter: ProcedureAdapter
    private var originalProcedureList = ProcedureList.getProcedures()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_procedure_selection, container, false)

        recyclerView = view.findViewById(R.id.procedureRecyclerView);
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val searchView = view.findViewById<SearchView>(R.id.procedure_search)
        searchView.setQueryHint("Search procedures...")
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProcedures(newText)
                return true
            }
        })

        val procedureList = ProcedureList.getProcedures()

        procedureAdapter = ProcedureAdapter(procedureList)
        recyclerView.adapter = procedureAdapter

        return view
    }

    private fun filterProcedures(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            originalProcedureList
        } else {
            originalProcedureList.filter { procedure -> procedure.name.contains(query, ignoreCase=true) }
        }
        procedureAdapter.updateList(filteredList)
    }

}