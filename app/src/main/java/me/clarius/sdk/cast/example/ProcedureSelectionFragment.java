package me.clarius.sdk.cast.example;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class ProcedureSelectionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_procedure_selection, container, false);

        Spinner spinner = view.findViewById(R.id.spinner_procedures);
        Button buttonStart = view.findViewById(R.id.button_start);

        // Set up the spinner with procedure names
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.procedures_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        buttonStart.setOnClickListener(v -> {
            String selectedProcedure = spinner.getSelectedItem().toString();
            ProcedureSelectionFragmentDirections.ActionProcedureSelectionFragmentToSlidesFragment action =
                    ProcedureSelectionFragmentDirections.actionProcedureSelectionFragmentToSlidesFragment(selectedProcedure);
            Navigation.findNavController(view).navigate(action);
        });

        return view;
    }
}