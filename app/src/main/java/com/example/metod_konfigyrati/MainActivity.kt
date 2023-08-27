package com.example.metod_konfigyrati

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.convertTo
import com.example.metod_konfigyrati.databinding.ActivityMainBinding
import kotlin.math.roundToInt


private lateinit var bilding:ActivityMainBinding
var count_Pepem : Int = 0
var function : String = ""
var start_koor = arrayOf<Float>()
var start_step = arrayOf<Float>()
var uskor: Float = 2f
var accuracy:Float = 0.1f
var step: Float = 2f
lateinit var solution: CoordinatesList
var count_iter = 10

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bilding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(bilding.root)
        SaveUp()
        bilding.tvKolPerem.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // действия, когда вводится какой то текст
                // s - то, что вводится, для преобразования в строку - s.toString()
            }

            override fun afterTextChanged(editable: Editable) {
                // действия после того, как что то введено
                // editable - то, что введено. В строку - editable.toString()
                if(editable.toString().isNotEmpty()) {
                    CreatEditText(bilding.layStartKoorg, editable.toString().toInt(), 1)
                    CreatEditText(bilding.layStartStep, editable.toString().toInt(), 2)
                }
                else{
                    bilding.layStartKoorg.removeViews(0,bilding.layStartKoorg.childCount)
                    bilding.layStartStep.removeViews(0,bilding.layStartStep.childCount)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // действия перед тем, как что то введено
            }
        })
    }
    fun onClickGO(view: View){
        if(findError())
            utit()
        else
            bilding.tvSulyh.text = "ошибка введённых данных"
        }
    fun findError(): Boolean {
        try {
            count_Pepem = bilding.tvKolPerem.text.toString().toInt()
            function = bilding.tvFun.text.toString()
            uskor = bilding.tvUskorMnog.text.toString().toFloat()
            accuracy = bilding.tvAccuracy.text.toString().toFloat()
            step = bilding.tvStep.text.toString().toFloat()
            start_koor = arrayOf()
            start_step = arrayOf()
            solution = CoordinatesList(count_Pepem)
            for(i in 0 until count_Pepem){
                var tv = findViewById<EditText>(10+i)
                if(tv.length() > 0) start_koor += tv.text.toString().toFloat()
                else return false
                tv = findViewById(10*2+i)
                if(tv.length() > 0) start_step += tv.text.toString().toFloat()
                else return false
            }
            return true
        }
        catch (e: NumberFormatException) {return false}
    }

    //заполнение переменных из EditText
    fun utit(){
        solution.add(Coordinates(start_koor,FindValueOfFunction(start_koor),"x0"))
        bilding.btDelet.backgroundTintList = ContextCompat.getColorStateList(this,R.color.green)
        bilding.btHistory.backgroundTintList = ContextCompat.getColorStateList(this,R.color.green)

        var s_solution = "Ответ: "
        SearchSolution(start_koor, start_koor, start_step, 0)
        val otvet = solution.getPoint().last().coords
        for (j in otvet.indices) s_solution += "x${j + 1} = ${otvet[j]} ;"
        bilding.tvSulyh.text = s_solution
    }
    //заполнение переменных и EditTest из DataBase
    fun SaveUp(){
        //написать вписывание данных в TextView при возврате
    }
    fun onClickHistiry(view: View){
        //Написать переход на Layout c графиком и RecyclerView
        val start = Intent(this,HistoryActivity::class.java)
        start.putExtra("char",solution)
        startActivity(start)
    }
    fun onClickDelete(view: View){
        //Написать стирание данных в TextView и DataBase
        bilding.btDelet.backgroundTintList = ContextCompat.getColorStateList(this,R.color.grey)
        bilding.btHistory.backgroundTintList = ContextCompat.getColorStateList(this,R.color.grey)
        bilding.tvKolPerem.text.clear()
        bilding.tvFun.text.clear()
        bilding.tvUskorMnog.text.clear()
        bilding.tvAccuracy.text.clear()
        bilding.tvStep.text.clear()
        bilding.tvSulyh.text = ""
    }

    fun ExploratorySearch(Y1_Koord : Array<Float>, step : Array<Float>, i_koord: Int): Array<Float>?{
        var X2_Koord = arrayOf<Float>()
        for(i in Y1_Koord.indices)
            X2_Koord += Y1_Koord[i]
        var new_i_koord = i_koord
        X2_Koord[i_koord] = Y1_Koord[i_koord] + step [i_koord]
        val valueFunY1 = FindValueOfFunction(Y1_Koord)?: return null
        var valueFunX2 = FindValueOfFunction(X2_Koord)?: return null

        if(valueFunX2 >= valueFunY1){
            X2_Koord[i_koord] = Y1_Koord[i_koord] - step [i_koord]
            valueFunX2 = FindValueOfFunction(X2_Koord)?: return null
            if(valueFunX2 >= valueFunY1)
                X2_Koord[i_koord] = Y1_Koord[i_koord]
        }
        new_i_koord++
        return if (new_i_koord >= Y1_Koord.size) X2_Koord
        else
            ExploratorySearch(X2_Koord, step, new_i_koord)
    }
    fun SearchSolution(X1_Koord: Array<Float>, Y1_Koord: Array<Float>, old_step : Array<Float>, iter: Int):Array<Float>{
        var new_iter = iter+1
        var new_step = old_step
        var X2_Koord = ExploratorySearch(Y1_Koord,new_step,0) ?: return X1_Koord
        val valueFunX2 = FindValueOfFunction(X2_Koord)?: return X1_Koord
        val valueFunX1 = FindValueOfFunction(X1_Koord)?: return X1_Koord


        return if((new_iter >= count_iter)) X1_Koord
        else {
            if (valueFunX2 < valueFunX1) {//поиск по образцу
                var Y2_Koord = arrayOf<Float>()
                for (i in X1_Koord.indices)
                    Y2_Koord += ((X2_Koord[i] + uskor * (X2_Koord[i] - X1_Koord[i]))*100).roundToInt().toFloat()/100f
                val valueFunY2 = FindValueOfFunction(Y2_Koord)?: return X1_Koord
                if(valueFunY2 < valueFunX2){// удачный поиск по образцу
                    solution.add(Coordinates(X2_Koord,valueFunX2,"x${iter+1}"))
                    solution.add(Coordinates(Y2_Koord,valueFunY2,"y${iter+1}"))
                    SearchSolution(X2_Koord,Y2_Koord, old_step, new_iter)
                }
                else{// неудачный поиск по образцу
                    solution.add(Coordinates(X2_Koord,valueFunX2,"x${iter+1}"))
                    SearchSolution(X2_Koord,X2_Koord, old_step, new_iter)
                }
            } else {//уменьшение шага
                new_iter = 0
                var k = 0
                for (i in new_step.indices)
                    if (new_step[i] <= accuracy) k++
                    else new_step[i] /= step
                return if (k == new_step.size) X1_Koord
                else SearchSolution(X1_Koord,X1_Koord, new_step, new_iter)
            }
        }
    }
    fun CreatEditText(tableLayout: TableLayout,countPer:Int,id:Int){
        tableLayout.removeViews(0,tableLayout.childCount)
        var tr = TableRow(this)
        tr.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        tr.setPadding(0,0,10,0)
        for(i in 0 until countPer){
            var tv = EditText(this)
            tv.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            tv.keyListener = DigitsKeyListener.getInstance("0123456789.-")
            tv.height = 100
            tv.width = 100
            tv.textSize = 16f
            tv.gravity = Gravity.CENTER_HORIZONTAL
            tv.setBackgroundResource(R.drawable.rect_with_border)
            tv.id = 10*id+i
            tv.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(4))
            when(id){
                1-> { tv.hint = "x" + (i + 1)
                    tv.setText("1")}//изменить на 0
                2-> { tv.hint = "h" + (i + 1)
                    tv.setText("1")}
            }
            var p = TableRow.LayoutParams()
            p.rightMargin = 10
            tv.layoutParams = p
            tr.addView(tv)
        }
        tableLayout.addView(tr, TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT))
    }
    fun FindValueOfFunction(solution:Array<Float>): Float?{
        var new_function = function
        for(j in 1..count_Pepem)
            new_function = new_function.replace("x$j",solution[j-1].toString())

        while(true)
            {
                Log.d("Test",new_function )
                //поиск индекса нужной операции
                var signIndex = -1
                var signIndex1 = -1
                var signIndex2 = -1
                var signIndex3 = -1

                for(i in new_function.length-1 downTo 1){
                    when(new_function[i]){
                        '^'-> signIndex1 = i
                        '*'-> signIndex2 = i
                        '/'-> signIndex2 = i
                        '+'-> signIndex3 = i
                        '-'-> signIndex3 = i
                    }
                }
                if(signIndex3 != -1)
                    signIndex = signIndex3
                if(signIndex2 != -1)
                    signIndex = signIndex2
                if(signIndex1 != -1)
                    signIndex = signIndex1
                if(signIndex3 == -1 && signIndex2 == -1 && signIndex1 == -1){
                    return try {
                        new_function.toFloat()
                    } catch (e: NumberFormatException) {
                        null
                    }
                }

                //поиск данных об одной операции

                var lengthTraction = 0
                var indexStartTrack = 0
                var lengthLeftTraction = 0
                var lengthRightTraction = 0
                var stringLeft = ""
                var stringRight = ""
                var value = 0f
                var fixErrorOperation = ""

                //нахождение длины левой части операции
                for (i in signIndex - 1 downTo 0){
                    if (new_function[i].isDigit() || new_function[i] == '.') lengthLeftTraction++
                    else if (new_function[i] == '-'){
                        lengthLeftTraction++
                        break
                    }
                    else break
                }

                //нахождение длины правой части операции
                for (i in signIndex + 1 until new_function.length)
                    if (new_function[i].isDigit() || new_function[i] == '.' || (i == signIndex + 1 && new_function[i] == '-')) lengthRightTraction++ else break

                val indexEndTraction: Int = signIndex + lengthRightTraction
                lengthTraction = lengthRightTraction + lengthLeftTraction + 1
                indexStartTrack = signIndex- lengthLeftTraction
                for (i in indexStartTrack until signIndex) stringLeft += new_function[i]
                for (i in signIndex + 1..indexEndTraction) stringRight += new_function[i]

               if(stringLeft.isEmpty() || stringRight.isEmpty())
                   return  null

                if(stringLeft[0] == '-' && stringRight[0] == '-' && (new_function[signIndex] == '*' || new_function[signIndex] == '/'))//???
                    fixErrorOperation = "+"

                val valueLeft = stringLeft.toFloat()
                val valueRight = stringRight.toFloat()

                when(new_function[signIndex]){
                    '^' -> value = Math.pow(valueLeft.toDouble(),valueRight.toDouble()).toFloat()
                    '*' -> value = valueLeft*valueRight
                    '/' -> value = valueLeft/valueRight
                    '+' -> value = valueLeft+valueRight
                    '-' -> value = valueLeft-valueRight
                }
                if(value>10000f || value<-10000f)
                    return null
                new_function = new_function.replaceRange(indexStartTrack until indexStartTrack+lengthTraction,fixErrorOperation+value.toString())
        }
    }

    fun onClickInstick(view: View) {
        var color ="FFC4C5C3"
        if(bilding.ivInstract.visibility == View.INVISIBLE) {
            bilding.ivInstract.visibility = View.VISIBLE
            bilding.instractButton.setBackgroundColor(resources.getColor(R.color.grey))
        }
        else{
            bilding.ivInstract.visibility = View.INVISIBLE
            bilding.instractButton.setBackgroundColor(Color.WHITE)
        }
    }
    fun onClickExample(view: View) {
        if(bilding.ivExample.visibility == View.INVISIBLE) {
            bilding.ivExample.visibility = View.VISIBLE
            bilding.exampleButton.setBackgroundColor(resources.getColor(R.color.grey))
        }
        else {
            bilding.ivExample.visibility = View.INVISIBLE
            bilding.exampleButton.setBackgroundColor(Color.WHITE)
        }
    }
}
