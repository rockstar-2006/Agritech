package com.example.agri_tech;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private View webcamView, sprinklerView, soundView, flashView;
    private SwitchCompat webcamSwitch, sprinklerSwitch, soundSwitch, flashSwitch;
    private TextView webcamLabel, sprinklerLabel, soundLabel, flashLabel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Find control views
        webcamView = view.findViewById(R.id.webcamControl);
        sprinklerView = view.findViewById(R.id.sprinklerControl);
        soundView = view.findViewById(R.id.soundControl);
        flashView = view.findViewById(R.id.flashControl);

        // Find text labels
        webcamLabel = webcamView.findViewById(R.id.controlLabel);
        sprinklerLabel = sprinklerView.findViewById(R.id.controlLabel);
        soundLabel = soundView.findViewById(R.id.controlLabel);
        flashLabel = flashView.findViewById(R.id.controlLabel);

        // Set labels
        webcamLabel.setText("Webcam");
        sprinklerLabel.setText("Water Sprinkler");
        soundLabel.setText("Sound");
        flashLabel.setText("Flash");

        // Find switches
        webcamSwitch = webcamView.findViewById(R.id.controlSwitch);
        sprinklerSwitch = sprinklerView.findViewById(R.id.controlSwitch);
        soundSwitch = soundView.findViewById(R.id.controlSwitch);
        flashSwitch = flashView.findViewById(R.id.controlSwitch);

        // Setup toggle listeners
        setupToggle(webcamSwitch, webcamView, "Webcam");
        setupToggle(sprinklerSwitch, sprinklerView, "Water Sprinkler");
        setupToggle(soundSwitch, soundView, "Sound");
        setupToggle(flashSwitch, flashView, "Flash");

        return view;
    }

    private void setupToggle(SwitchCompat toggleSwitch, View controlView, String label) {
        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int bgColor = isChecked ? Color.parseColor("#E8F5E9") : Color.WHITE;
            controlView.setBackgroundColor(bgColor);

            String status = isChecked ? "turned ON" : "turned OFF";
            Toast.makeText(getContext(), label + " " + status, Toast.LENGTH_SHORT).show();
        });

        // Optionally: dynamically assign icons if needed
        ImageView icon = controlView.findViewById(R.id.controlIcon);
        switch (label) {
            case "Webcam":
                icon.setImageResource(R.drawable.ic_webcam); break;
            case "Water Sprinkler":
                icon.setImageResource(R.drawable.ic_sprinkel); break;
            case "Sound":
                icon.setImageResource(R.drawable.ic_sound); break;
            case "Flash":
                icon.setImageResource(R.drawable.ic_flash); break;
        }
    }

}

