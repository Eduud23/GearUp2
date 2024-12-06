package com.example.gearup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FutureSalesActivity extends AppCompatActivity {

    private FirebaseFirestore db; // Firestore instance
    private RecyclerView rvAddresses; // RecyclerView to display data
    private AddressAdapter addressAdapter; // Adapter for the RecyclerView
    private List<String> addresses = new ArrayList<>();
    private List<String> zipCodes = new ArrayList<>();
    private ProgressBar progressBar; // Loading spinner
    private EditText etSearch; // Search EditText
    private ImageView btnBack; // Back button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future_sales);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        rvAddresses = findViewById(R.id.rvAddresses);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));

        // Set up ProgressBar (loading indicator)
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);  // Show progress bar while data is being fetched

        // Set up the Search EditText and Back Button
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);

        // Fetch the addresses and zip codes from Firestore
        fetchAddressesAndZipCodes();

        // Back button click listener
        btnBack.setOnClickListener(v -> finish()); // Close the activity when back button is clicked

        // Search functionality - listening for text changes
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString().toLowerCase();
                filterAddresses(query);
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });
    }

    private void fetchAddressesAndZipCodes() {
        // Query Firestore for the "trends" collection (collectionGroup searches all subcollections)
        db.collectionGroup("trends")
                .get()
                .addOnCompleteListener(task -> {
                    // Hide the progress bar when the task is complete
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear previous data
                            addresses.clear();
                            zipCodes.clear();

                            // Iterate through the documents in Firestore and fetch 'address' and 'zipCode'
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String address = document.getString("address");
                                String zipCode = document.getString("zipCode");

                                // Add only non-null values to the lists
                                if (address != null && zipCode != null) {
                                    addresses.add(address);
                                    zipCodes.add(zipCode);
                                }
                            }

                            // Log fetched data
                            Log.d("FutureSalesActivity", "Fetched Addresses: " + addresses);
                            Log.d("FutureSalesActivity", "Fetched Zip Codes: " + zipCodes);

                            // Check if data is added to the list
                            if (!addresses.isEmpty() && !zipCodes.isEmpty()) {
                                // Set the adapter to the RecyclerView
                                addressAdapter = new AddressAdapter(this, addresses, zipCodes);
                                rvAddresses.setAdapter(addressAdapter);
                            } else {
                                // If no addresses or zip codes were fetched, show a message
                                Toast.makeText(this, "No addresses or zip codes found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle case when there are no documents in the collection
                            Log.d("FutureSalesActivity", "No documents found in 'trends' collection.");
                            Toast.makeText(this, "No data found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle error in Firestore query
                        Log.w("FutureSalesActivity", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterAddresses(String query) {
        List<String> filteredAddresses = new ArrayList<>();
        List<String> filteredZipCodes = new ArrayList<>();

        // Check if the query is empty or null
        if (TextUtils.isEmpty(query)) {
            // If the query is empty, just display the full list again
            filteredAddresses.addAll(addresses);
            filteredZipCodes.addAll(zipCodes);
        } else {
            // Otherwise, filter the lists based on the query
            for (int i = 0; i < addresses.size(); i++) {
                String address = addresses.get(i).toLowerCase();
                if (address.contains(query)) {
                    filteredAddresses.add(addresses.get(i));
                    filteredZipCodes.add(zipCodes.get(i));
                }
            }
        }

        // Update the adapter with the filtered lists
        addressAdapter = new AddressAdapter(this, filteredAddresses, filteredZipCodes);
        rvAddresses.setAdapter(addressAdapter);
    }
}
