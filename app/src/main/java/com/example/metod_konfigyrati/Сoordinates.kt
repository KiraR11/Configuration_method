package com.example.metod_konfigyrati

import java.lang.reflect.Field

typealias OnFiledChangedListener = (field: CoordinatesList) -> Unit

data class Coordinates(var coords: Array<Float>,var valueFun: Float?, var legend: String? = null):java.io.Serializable

class CoordinatesList(var coordsCount: Int):java.io.Serializable{
    private  var points : ArrayList<Coordinates> = arrayListOf()
    private var minValue : ArrayList<Float> = arrayListOf()
    private var maxValue : ArrayList<Float> = arrayListOf()
    private var count : Int = 0
    val listeners = mutableSetOf<OnFiledChangedListener>()

    fun add(point : Coordinates){
        points.add(point)
        count++
        listeners.forEach {it.invoke(this)}//???
        if(minValue.isEmpty() && maxValue.isEmpty()) {
            for(i in point.coords.indices){
            minValue += point.coords[i]
            maxValue += point.coords[i]
            }
        }
        else{
            for(i in point.coords.indices){
                if(minValue[i] > point.coords[i])
                    minValue[i] = point.coords[i]
                if(maxValue[i] < point.coords[i])
                    maxValue[i] = point.coords[i]
            }
        }
    }
    fun getMinValue(): ArrayList<Float>{
        return minValue
    }
    fun getMaxValue(): ArrayList<Float>{
        return maxValue
    }
    fun getPoint(): ArrayList<Coordinates>{
        return points
    }
    fun getCount(): Int{
        return count
    }
    fun getDimen(): Int{
        return coordsCount
    }
}
