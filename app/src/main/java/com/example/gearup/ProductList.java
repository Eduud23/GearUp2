package com.example.gearup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductList extends AppCompatActivity {

    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private List<Product> products;

    private static final int PRODUCT_DETAILS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerViewProducts = findViewById(R.id.recyclerView_products);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));

        String category = getIntent().getStringExtra("CATEGORY");
        products = getIntent().getParcelableArrayListExtra("PRODUCTS"); // Retrieve products

        productAdapter = new ProductAdapter(products, this::onProductClick);
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void onProductClick(int position) {
        Intent intent = new Intent(this, ProductDetails.class);
        intent.putExtra("PRODUCT", products.get(position));
        intent.putExtra("POSITION", position);
        startActivityForResult(intent, PRODUCT_DETAILS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PRODUCT_DETAILS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            boolean deleteProduct = data.getBooleanExtra("DELETE_PRODUCT", false);
            if (deleteProduct) {
                int position = data.getIntExtra("POSITION", -1);
                if (position != -1) {
                    products.remove(position);
                    productAdapter.notifyDataSetChanged();
                }
            } else {
                Product updatedProduct = data.getParcelableExtra("UPDATED_PRODUCT");
                int position = data.getIntExtra("POSITION", -1);
                if (updatedProduct != null && position != -1) {
                    products.set(position, updatedProduct);
                    productAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
