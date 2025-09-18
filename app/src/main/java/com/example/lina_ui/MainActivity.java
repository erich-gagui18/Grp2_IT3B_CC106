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
    }
}