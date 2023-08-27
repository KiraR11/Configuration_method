package com.example.metod_konfigyrati

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.metod_konfigyrati.databinding.ActivityHistoryBinding
import kotlin.collections.ArrayList

lateinit var bindingHistory: ActivityHistoryBinding
private lateinit var iterAdapter: IterAdapter
class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingHistory = ActivityHistoryBinding.inflate(LayoutInflater.from(this))
        setContentView(bindingHistory.root)
        val coordinates = intent.getSerializableExtra("char") as CoordinatesList
        bindingHistory.chart.data = coordinates
        bindingHistory.rvIter.layoutManager = LinearLayoutManager(this)
        bindingHistory.rvIter.setHasFixedSize(true)
        iterAdapter = IterAdapter(coordinates.getPoint())
        bindingHistory.rvIter.adapter = iterAdapter

    }
    fun onClickBack(view: View) {
        val start = Intent(this,MainActivity::class.java)
        startActivity(start)
    }
}