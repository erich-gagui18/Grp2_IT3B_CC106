package com.example.lina_ui;


package com.example.barbershopapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barbershopapp.adapter.ProductAdapter
import com.example.barbershopapp.model.Product

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.productsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val productList = listOf(
                Product("SEA SALT SPRAY", R.drawable.ic_sea_salt_spray),
                Product("SEA SALT SPRAY", R.drawable.ic_sea_salt_spray),
                Product("HAIR POMADE", R.drawable.ic_hair_pomade),
                Product("TEXTURING POWDER", R.drawable.ic_texturing_powder)
        )

        val adapter = ProductAdapter(productList)
        recyclerView.adapter = adapter

import com.example.lina_ui.Adapter.ProductAdapter;
import com.example.lina_ui.model.Product;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView productsRecyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productList = new ArrayList<>();
        // Add each product with its unique image resource ID
        productList.add(new Product("SEA SALT SPRAY", 19.99, R.drawable.sea_salt_spray));
        productList.add(new Product("HAIR POMADE", 24.50, R.drawable.hair_pomade));
        productList.add(new Product("TEXTURING POWDER", 15.00, R.drawable.texturing_powder));
        productList.add(new Product("SEA SAlT SPRAY", 15.00, R.drawable.seasalt_spray));

        adapter = new ProductAdapter(this, productList);
        productsRecyclerView.setAdapter(adapter);

    }
}