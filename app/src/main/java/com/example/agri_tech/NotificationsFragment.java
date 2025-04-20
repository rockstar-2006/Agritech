package com.example.agri_tech;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class NotificationsFragment extends Fragment {

    private BluetoothSocket socket;
    private BufferedReader reader;
    private RadarView radarView;

    private final String DEVICE_ADDRESS = "00:22:09:01:92:EA"; // Replace with your HC-05 MAC
    private final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private volatile boolean isReading = true;  // Controls background thread safety

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        radarView = new RadarView(getContext());
        radarView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        requestBluetoothPermissions();

        new Thread(this::connectBluetooth).start();

        return radarView;
    }

    private void requestBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 1);
        }
    }

    private void connectBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            showToast("Bluetooth not supported");
            return;
        }

        if (!adapter.isEnabled()) {
            showToast("Please enable Bluetooth");
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            showToast("Bluetooth permission not granted");
            return;
        }

        try {
            BluetoothDevice device = adapter.getRemoteDevice(DEVICE_ADDRESS);
            adapter.cancelDiscovery();

            try {
                socket = device.createRfcommSocketToServiceRecord(BT_UUID);
                socket.connect();
                showToast("Bluetooth Connected to HC-05");
            } catch (IOException connectException) {
                showToast("Standard connect failed, trying fallback...");
                try {
                    socket = (BluetoothSocket) device.getClass()
                            .getMethod("createRfcommSocket", new Class[]{int.class})
                            .invoke(device, 1);
                    socket.connect();
                    showToast("Connected using fallback method");

                } catch (Exception fallbackEx) {
                    fallbackEx.printStackTrace();
                    showToast("Fallback failed: " + fallbackEx.getMessage());
                    try {
                        socket.close();
                    } catch (IOException closeEx) {
                        closeEx.printStackTrace();
                    }
                    return;
                }
            }

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            readData();

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Connection error: " + e.getMessage());
        }
    }

    private void readData() {
        try {
            String line;

            while (!Thread.currentThread().isInterrupted() && isReading) {
                line = reader.readLine();

                if (line == null) {
                    Log.e("BluetoothData", "Lost connection or no data available");
                    break;
                }

                Log.d("BluetoothData", "Received: " + line);

                if (line.contains("Angle")) {
                    String[] parts = line.split("\\|");

                    if (parts.length == 2) {
                        try {
                            int angle = Integer.parseInt(parts[0].replace("Angle:", "").trim());
                            int distance = Integer.parseInt(parts[1].replace("Distance:", "").replace("cm", "").trim());

                            Log.d("RadarData", "Angle: " + angle + "° Distance: " + distance + " cm");

                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> radarView.updateRadar(angle, distance));
                            }

                        } catch (NumberFormatException e) {
                            Log.e("BluetoothData", "Error parsing data: " + e.getMessage());
                        }
                    } else {
                        Log.e("BluetoothData", "Invalid data format received: " + line);
                    }
                }
            }
        } catch (IOException e) {
            Log.e("BluetoothData", "Error reading data from Bluetooth: " + e.getMessage());
            e.printStackTrace();
            showToast("Error reading data: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
        } else {
            Log.w("BluetoothData", "Fragment not attached. Skipping toast: " + message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isReading = false;

        try {
            if (reader != null) reader.close();
            if (socket != null && socket.isConnected()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
