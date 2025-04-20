package com.example.agri_tech;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ListFragment extends Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        BarChart barChart = view.findViewById(R.id.barChart);
        RecyclerView recyclerView = view.findViewById(R.id.animalRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Animal> animalList = new ArrayList<>();
        AnimalAdapter adapter = new AnimalAdapter(animalList);
        recyclerView.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("detected_animals");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ((ArrayList<?>) animalList).clear();

                Map<String, Integer> labelCountMap = new HashMap<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Animal animal = child.getValue(Animal.class);
                    if (animal != null) {
                        animalList.add(animal);

                        // Count frequency for each animal label
                        String label = animal.getLabel();
                        labelCountMap.put(label, labelCountMap.getOrDefault(label, 0) + 1);
                    }
                }

                adapter.notifyDataSetChanged();

                // Build Bar Entries from frequency map
                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                int index = 0;
                for (Map.Entry<String, Integer> entry : labelCountMap.entrySet()) {
                    entries.add(new BarEntry(index, entry.getValue()));
                    labels.add(entry.getKey());
                    index++;
                }

                BarDataSet dataSet = new BarDataSet(entries, "Animal Frequency");
                dataSet.setColor(Color.parseColor("#4CAF50"));
                dataSet.setValueTextSize(14f);

                BarData barData = new BarData(dataSet);
                barChart.setData(barData);

                // Setup X-Axis labels
                XAxis xAxis = barChart.getXAxis();
                xAxis.setGranularity(1f);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setLabelRotationAngle(-45);

                barChart.getDescription().setEnabled(false);
                barChart.getAxisRight().setEnabled(false);
                barChart.animateY(1000);
                barChart.invalidate(); // refresh
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}