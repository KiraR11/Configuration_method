package com.example.metod_konfigyrati

import android.annotation.SuppressLint
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.metod_konfigyrati.databinding.IterItemBinding

class IterViewHolder(itemView: IterItemBinding)
    : RecyclerView.ViewHolder(itemView.root) {
    @SuppressLint("SetTextI18n")
    var tv_name_point = itemView.tvNamePoint
    var tv_values_point = itemView.tvValuesPoint
    var tv_values_fun = itemView.tvValueFun
    //var imageView = itemView.imageView
    fun bind(point: Coordinates){

        with(itemView){point.run {
            var valueFun = "f("
            var valuePoint = ""
            for(i in 1 ..point.coords.size) {
                valueFun += "x$i,"
                valuePoint += "x$i=${point.coords[i-1]}; "
            }
            tv_values_fun.text = valueFun.removeSuffix(",") + ") = ${point.valueFun}"
            tv_values_point.text= valuePoint
            tv_name_point.text = point.legend

           /*
            itemView.setOnClickListener{
                Log.d("MyLog","Удалить карту с именем ${cards.name}")
                listener.onClick(cards)
            }
            */
        }
        }
    }

}