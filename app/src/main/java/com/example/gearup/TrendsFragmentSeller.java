package com.example.gearup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TrendsFragmentSeller extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerViewPopular;
    private PopularAdapter popularAdapter;
    private List<String> addressList; // List of addresses
    private List<String> filteredAddressList; // List for filtered search results

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trends_seller, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and adapter
        recyclerViewPopular = view.findViewById(R.id.recyclerViewPopular);
        recyclerViewPopular.setLayoutManager(new LinearLayoutManager(getContext()));
        addressList = new ArrayList<>();
        filteredAddressList = new ArrayList<>();
        popularAdapter = new PopularAdapter(filteredAddressList, getContext());
        recyclerViewPopular.setAdapter(popularAdapter);

        // Fetch data from Firestore
        fetchPopularItems();

        // Search functionality
        EditText searchEditText = view.findViewById(R.id.et_search);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No need to implement this
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Filter list based on the query
                filterList(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed in this case
            }
        });

        return view;
    }

    private void fetchPopularItems() {
        db.collectionGroup("trends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            addressList.clear();

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String address = document.getString("address");

                                if (address != null && !address.isEmpty()) {
                                    addressList.add(address);
                                }
                            }

                            // Initially display all items
                            filteredAddressList.addAll(addressList);
                            popularAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Handle error
                    }
                });
    }

    private void filterList(String query) {
        filteredAddressList.clear();

        if (query.isEmpty()) {
            // If the search bar is empty, show all items
            filteredAddressList.addAll(addressList);
        } else {
            // Filter the list based on the query
            for (String address : addressList) {
                if (address.toLowerCase().contains(query.toLowerCase())) {
                    filteredAddressList.add(address);
                }
            }
        }

        // Update the adapter with the filtered list
        popularAdapter.setFilteredData(filteredAddressList);
    }
}
