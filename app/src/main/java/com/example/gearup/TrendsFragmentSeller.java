package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

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
    private List<PopularItem> popularItemList; // List of PopularItem (address, zipCode, productImage, productQuantity)
    private List<PopularItem> filteredItemList; // List for filtered search results
    private ImageButton searchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trends_seller, container, false);


        searchButton = view.findViewById(R.id.iv_search_icon);

        // Set the OnClickListener
        searchButton.setOnClickListener(v -> {
            // Navigate to FutureSalesActivity when clicked
            Intent intent = new Intent(getActivity(), FutureSalesActivity.class);
            startActivity(intent);
        });


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and adapter
        recyclerViewPopular = view.findViewById(R.id.recyclerViewPopular);
        recyclerViewPopular.setLayoutManager(new LinearLayoutManager(getContext()));
        popularItemList = new ArrayList<>();
        filteredItemList = new ArrayList<>();
        popularAdapter = new PopularAdapter(filteredItemList, getContext());
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
        // Fetch trends from Firestore
        db.collectionGroup("trends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear the popular items list to avoid old data when re-fetching
                            popularItemList.clear();

                            // Iterate through the documents in Firestore and create PopularItem objects
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String address = document.getString("address");
                                String zipCode = document.getString("zipCode");

                                if (address != null && !address.isEmpty() && zipCode != null && !zipCode.isEmpty()) {
                                    // Check if this item already exists in the list
                                    PopularItem existingItem = getItemByAddressAndZipCode(address, zipCode);
                                    if (existingItem == null) {
                                        // Create a new PopularItem and add to the list if it doesn't exist
                                        PopularItem popularItem = new PopularItem();
                                        popularItem.setAddress(address);
                                        popularItem.setZipCode(zipCode);
                                        popularItemList.add(popularItem);
                                        fetchSalesData(address, zipCode, popularItem);
                                    } else {
                                        // If item exists, just update it instead of adding a duplicate
                                        fetchSalesData(address, zipCode, existingItem);
                                    }
                                }
                            }

                            // Initially display all items
                            filteredItemList.addAll(popularItemList);
                            popularAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Handle error (e.g., show a Toast)
                    }
                });
    }

    private void fetchSalesData(String address, String zipCode, PopularItem popularItem) {
        // Fetch sales data for the given address from the "sales" collection
        db.collection("sales")
                .whereEqualTo("address", address)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            int highestQuantity = 0;
                            String productImage = null;

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Get the quantitySold field and check for null
                                Long quantitySoldLong = document.getLong("productQuantity");

                                // If quantitySold is null, set a default value of 0
                                int quantitySold = (quantitySoldLong != null) ? quantitySoldLong.intValue() : 0;

                                // Check if this is the highest quantity sold
                                if (quantitySold > highestQuantity) {
                                    highestQuantity = quantitySold;
                                    productImage = document.getString("productImage");
                                }
                            }

                            // Update the PopularItem object with the fetched data
                            popularItem.setProductImage(productImage);
                            popularItem.setProductQuantity(highestQuantity);

                            // Check if the item is already in the filtered list, update if necessary
                            int position = filteredItemList.indexOf(popularItem);
                            if (position >= 0) {
                                // If the item exists in the filtered list, update and notify
                                filteredItemList.set(position, popularItem);
                                popularAdapter.notifyItemChanged(position);
                            } else {
                                // If it's not in the filtered list, add it
                                filteredItemList.add(popularItem);
                                popularAdapter.notifyDataSetChanged();
                            }
                        } else {
                            // If no sales data exists for this address, set default values
                            popularItem.setProductImage(null); // Or set a default image
                            popularItem.setProductQuantity(0);

                            // Add the item to the filtered list if it's not already there
                            int position = filteredItemList.indexOf(popularItem);
                            if (position < 0) {
                                filteredItemList.add(popularItem);
                                popularAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        // Handle error (e.g., show a Toast)
                    }
                });
    }


    private void filterList(String query) {
        filteredItemList.clear();

        if (query.isEmpty()) {
            // If the search bar is empty, show all items
            filteredItemList.addAll(popularItemList);
        } else {
            // Filter the list based on the query (both address and zipCode)
            for (PopularItem popularItem : popularItemList) {
                if (popularItem.getAddress().toLowerCase().contains(query.toLowerCase()) ||
                        popularItem.getZipCode().toLowerCase().contains(query.toLowerCase())) {
                    filteredItemList.add(popularItem);
                }
            }
        }

        // Update the adapter with the filtered list
        popularAdapter.setFilteredData(filteredItemList);
    }

    private PopularItem getItemByAddressAndZipCode(String address, String zipCode) {
        // Check if a PopularItem with the same address and zipCode already exists
        for (PopularItem item : popularItemList) {
            if (item.getAddress().equals(address) && item.getZipCode().equals(zipCode)) {
                return item;
            }
        }
        return null; // No matching item found
    }
}
