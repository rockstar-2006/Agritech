package com.example.agri_tech;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class AddPhoneNumberDialogFragment extends DialogFragment {

    private OnPhoneNumberAddedListener listener;

    // Define an interface for the callback
    public interface OnPhoneNumberAddedListener {
        void onPhoneNumberAdded(String name, String phoneNumber);
    }

    // Pass listener as a constructor parameter
    public AddPhoneNumberDialogFragment(OnPhoneNumberAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_phone_number, null);

        final EditText nameInput = view.findViewById(R.id.nameInput);
        final EditText phoneNumberInput = view.findViewById(R.id.phoneNumberInput);
        Button addButton = view.findViewById(R.id.addButton);

        addButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String number = phoneNumberInput.getText().toString().trim();

            // Trigger the callback when the button is clicked
            if (!name.isEmpty() && !number.isEmpty()) {
                listener.onPhoneNumberAdded(name, number);
                dismiss();
            }
        });

        // Set up and return the AlertDialog
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Add Phone Number")
                .setNegativeButton("Cancel", (dialog, which) -> dismiss())
                .create();
    }
}
