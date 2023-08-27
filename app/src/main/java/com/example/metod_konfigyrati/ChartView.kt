package com.example.metod_konfigyrati

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.lang.Float.min
import java.lang.Float.max
import java.lang.Integer.max
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class ChartView(context: Context, attributesSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
    View(context, attributesSet, defStyleAttr, defStyleRes) {

    var data: CoordinatesList? = null
    set(value){
        field?.listeners?.remove { listener }
        field = value
        field?.listeners?.add { listener }
        if(value != null && value.getDimen() == 2) {
            updateViewSize()
            requestLayout()
            invalidate()
        }
        if(value != null && value.getDimen() != 2){
            requestLayout()
            invalidate()
        }

    }

    private var gridColor by Delegates.notNull<Int>()
    private var pointColor by Delegates.notNull<Int>()
    private var lineColor by Delegates.notNull<Int>()
    private var AxisColor by Delegates.notNull<Int>()
    private var AxisLabelColor by Delegates.notNull<Int>()
    private var pointLegendColor by Delegates.notNull<Int>()

    private var pointPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var dotLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var AxisPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var gridPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var AxisLabelPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var pointLegendPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)


    private var valueYAxis : Array<Float?> = arrayOfNulls(COUNT_MARK_LINE)
    private var valueXAxis :  Array<Float?> = arrayOfNulls(COUNT_MARK_LINE)

    private lateinit var points : ArrayList<Coordinates>

    private var factorXAxis = 0f
    private var factorYAxis = 0f


    private var minXAxis = 0f
    private var maxXAxis = 0f
    private var minYAxis = 0f
    private var maxYAxis = 0f

    private var maxTextSize = 100f

    private var fieldRect = RectF(0f,0f,0f,0f)
    private  var chartRect = RectF(0f,0f,0f,0f)
    private var step = 0f


    constructor(context: Context,attributeSet: AttributeSet?, defStyleAttr: Int): this(context,attributeSet, defStyleAttr,R.attr.chartViewStyle)
    constructor(context: Context,attributeSet: AttributeSet?): this(context,attributeSet,R.attr.chartViewStyle)
    constructor(context: Context): this(context, null)

    init{
        if(attributesSet != null)
            initAttributes(attributesSet, defStyleAttr, defStyleRes)
        else
            initAttributesDefault()
        initPaints()

        if(isInEditMode){
            data = CoordinatesList(3)
            for(i in 0..9){
                var point = arrayOf<Float>()
                for(j in 0..2)
                    point += (i+1)*(j+1).toFloat()
                val x = (0..1).random()
                if(x == 0)
                data?.add(Coordinates(point,null,"x${data?.getCount()}"))
                else
                    data?.add(Coordinates(point,null,"y${data?.getCount()}"))
            }
        }
    }
    private fun initAttributes(attributesSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int){
        val typedArray = context.obtainStyledAttributes(attributesSet, R.styleable.ChartView, defStyleAttr, defStyleRes)
        gridColor = typedArray.getColor(R.styleable.ChartView_gridColor, Color.GRAY)
        pointColor = typedArray.getColor(R.styleable.ChartView_pointColor, Color.BLACK)
        lineColor = typedArray.getColor(R.styleable.ChartView_lineColor, Color.GREEN)
        AxisColor = typedArray.getColor(R.styleable.ChartView_AxisColor, Color.BLACK)
        AxisLabelColor = typedArray.getColor(R.styleable.ChartView_AxisLabelColor, Color.BLACK)
        pointLegendColor = typedArray.getColor(R.styleable.ChartView_pointLegendColor, Color.BLACK)
        typedArray.recycle()
    }
    private fun initAttributesDefault(){
        gridColor = Color.GRAY
        pointColor = Color.BLACK
        lineColor = Color.GREEN
        AxisColor = Color.BLACK
        AxisLabelColor = Color.BLACK
        pointLegendColor = Color.BLACK
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        data?.listeners?.add{listener}
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        data?.listeners?.remove{listener}
    }
    private var listener: OnFiledChangedListener = {
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSize()
    }
    //тут будут ошибки готовся
    fun updateViewSize(){
        val field = this.data ?:return
        if(width <= 0 || height <=0) return
        if(field.getDimen()!=2) return

        points = arrayListOf()

        val safeWith =  width - paddingLeft - paddingRight
        val safeHeight = height - paddingTop - paddingBottom

        minXAxis = field.getMinValue()[0]
        maxXAxis = field.getMaxValue()[0]
        minYAxis = field.getMinValue()[1]
        maxYAxis = field.getMaxValue()[1]

        val stepY = (maxYAxis-minYAxis) / (COUNT_MARK_LINE-1).toFloat()
        val stepX = (maxXAxis-minXAxis) / (COUNT_MARK_LINE-1).toFloat()

        for(i in 0 until COUNT_MARK_LINE){
            valueXAxis[i] = ((minXAxis + i * stepX)*10).roundToInt().toFloat()/10
            valueYAxis[i] = ((minYAxis + i * stepY)*10).roundToInt().toFloat()/10
        }


        val maxLengthTextX = max(minXAxis.toString().length,maxXAxis.toString().length)
        val maxLengthTextY = max(minYAxis.toString().length,maxYAxis.toString().length)

        var fieldWith = safeWith.toFloat()
        var fieldHeight = safeHeight.toFloat()
        var chartSize = 0f

       maxTextSize = 100f
        while(fieldWith >= safeWith || fieldHeight >= safeHeight) {
            maxTextSize-=1
            val chartWith = (maxLengthTextX+1) * maxTextSize * COUNT_MARK_LINE
            val chartHeight = maxTextSize * 2 * COUNT_MARK_LINE
            chartSize = max(chartWith, chartHeight)
            fieldWith = chartSize + (maxLengthTextY+maxLengthTextX) * maxTextSize
            fieldHeight = chartSize + maxTextSize * 2.5f
        }
        maxTextSize*=2f
        factorXAxis = chartSize/(maxXAxis-minXAxis)
        factorYAxis = chartSize/(maxYAxis-minYAxis)
        fieldRect.left = paddingLeft + safeWith - fieldWith
        fieldRect.right = paddingRight.toFloat()
        fieldRect.bottom = paddingBottom + safeHeight - fieldHeight
        fieldRect.top = paddingTop.toFloat()

        chartRect.right = width-(paddingRight + maxLengthTextX * maxTextSize/2)
        chartRect.left = chartRect.right - chartSize
        chartRect.top = paddingTop.toFloat() + 1f * maxTextSize
        chartRect.bottom = chartRect.top + chartSize


        var testPoints = data?.getPoint()
        for(i in 0 until data?.getCount()!!)
            points.add(Coordinates(floatArrayOf(chartRect.left+factorXAxis*(testPoints!![i].coords[0]-data!!.getMinValue()[0]),chartRect.bottom-factorYAxis*(testPoints[i].coords[1]-data!!.getMinValue()[1])).toTypedArray(),null,testPoints[i].legend))
        initPaint(AxisLabelPaint,pointColor,Style.STROKE,maxTextSize)//обновнение кисти лэйбла осей
        AxisLabelPaint.textSize = maxTextSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {//???
        // min size of our view
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        // calculating desired size of view
        val desiredCellSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,maxTextSize,
            resources.displayMetrics).toInt()

        val desiredWith = max(minWidth, (COUNT_MARK_LINE) * desiredCellSizeInPixels + paddingLeft + paddingRight)
        val desiredHeight = max(minHeight, (COUNT_MARK_LINE)  * desiredCellSizeInPixels + paddingTop + paddingBottom)

        setMeasuredDimension(
            resolveSize(desiredWith, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    private fun initPaint(paint: Paint, color: Int, style: Style, size : Float){
        paint.color = color
        paint.style = style
        paint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,size,resources.displayMetrics)

    }
    private  fun initPaints(){
        initPaint(linePaint,Color.BLACK,Style.STROKE,1f)
        initPaint(dotLinePaint,Color.BLACK,Style.STROKE,1f)
        dotLinePaint.pathEffect = DashPathEffect(floatArrayOf(30f, 10f), 0f)
        initPaint(gridPaint,gridColor,Style.STROKE,1f)
        initPaint(pointLegendPaint,pointColor,Style.STROKE,maxTextSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(data == null) return
        if(data?.getDimen() == 2) {
            drawGrid(canvas)
            drawLabels(canvas)
            drawPoints(canvas)
            drawLine(canvas)
        }
        else
            drawError(canvas)
    }
    private fun drawError(canvas: Canvas){
        val testPaint = Paint()
        testPaint.color = AxisLabelColor
        testPaint.textSize = 40f
        testPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Неудалось построить график",550f,300f,testPaint)
        canvas.drawText("Неудовлетворительное кол-во переменных",550f,400f,testPaint)
    }
    private fun drawGrid(canvas: Canvas){
        step = (chartRect.bottom-chartRect.top)/(COUNT_MARK_LINE-1)
        for(i in 0 until COUNT_MARK_LINE){
            canvas.drawLine(chartRect.left+i*step,chartRect.top,chartRect.left+i*step, chartRect.bottom,gridPaint)//верт. линии
            canvas.drawLine(chartRect.left,chartRect.top+i*step,chartRect.right,chartRect.top+i*step,gridPaint)// гориз. линии
        }
    }
    private fun drawLabels(canvas: Canvas){
        val testPaint = Paint()
        testPaint.color = AxisLabelColor
        testPaint.textSize = maxTextSize
        testPaint.textAlign = Paint.Align.RIGHT
        for(i in 0 until COUNT_MARK_LINE)
            canvas.drawText(valueYAxis[i].toString(),chartRect.left-maxTextSize,chartRect.bottom-i*step+maxTextSize/4,testPaint)
        testPaint.textAlign = Paint.Align.CENTER
        for(i in 0 until COUNT_MARK_LINE)
            canvas.drawText(valueXAxis[i].toString(),chartRect.left+i*step,chartRect.bottom+2*maxTextSize,testPaint)

    }
    private fun drawPoints(canvas: Canvas){
        val testPaint = Paint()
        testPaint.color = AxisLabelColor
        testPaint.textSize = 30f
        testPaint.textAlign = Paint.Align.RIGHT

        for(i in 0 until points.size){
            canvas.drawCircle(points[i].coords[0],points[i].coords[1],5f,pointPaint)
            points[i].legend?.let { canvas.drawText(it,points[i].coords[0]+30f*2,points[i].coords[1],testPaint) }
       }
    }
    private fun drawLine(canvas: Canvas){
        for(i in 0..points.size-2){
            if (points[i].legend!!.isEmpty()) break
            if((points[i].legend!![0] == 'y' && points[i+1].legend!![0]== 'x') || (points[i].legend!![0] == 'x' && points[i+1].legend!![0]== 'x'))
                canvas.drawLine(points[i].coords[0],points[i].coords[1],points[i+1].coords[0],points[i+1].coords[1],linePaint)
        }
        for(i in 1 until points.size){
            if(points[i].legend!![0] == 'y')
                for(j in 0 until i)
                    if (points[j].legend!! == "x${points[i].legend!!.removeRange(0, 1).toInt() - 1}") {
                    canvas.drawLine(
                        points[j].coords[0],
                        points[j].coords[1],
                        points[i].coords[0],
                        points[i].coords[1],
                        dotLinePaint
                    )
                    break
                }
        }
    }
    companion object{
        const val COUNT_MARK_LINE = 10
    }


}