package com.example.caloriecounter.activities.Views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.caloriecounter.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Chart extends View {

    private List<Integer> values;
    private List<String> dates;

    private Paint graph = new Paint();
    private int color;

    private int max = 0;
    private int min = 999999;

    private boolean showText;
    private int angle;

    public Chart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Chart, 0, 0);
        try{
            showText = a.getBoolean(R.styleable.Chart_show_text,true);
            angle = a.getInteger(R.styleable.Chart_label_style,0);
        } finally {
            a.recycle();
        }
    }

    private void drawAxes(@NotNull Canvas canvas) {
        graph.setColor(getResources().getColor(R.color.white));
        graph.setStrokeWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,2,getResources().getDisplayMetrics()));
        graph.setTextAlign(Paint.Align.CENTER);
        graph.setTextSize(35.0f);
        canvas.drawLine(0, canvas.getHeight()-75, canvas.getWidth(), canvas.getHeight()-75, graph);
        for(int i = 0; i < this.values.size(); i++) {
            graph.setColor(this.color);
            int val = this.values.get(i);
            int left = ((canvas.getWidth()*i)/7) + (canvas.getWidth()/14) - 25;
            int right = left + 50;
            int bottom = canvas.getHeight() - 100;
            int top = canvas.getHeight() - getBarTop(val) - 50;
            Log.d("LOL",top+" = TOP");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(left,top,right,bottom,25.0f,25.0f,graph);
            } else {
                canvas.drawRect(left,top,right,bottom,graph);
            }

            if(this.showText) {
                canvas.save();
                canvas.rotate((float)-this.angle,left+25,top-25);
                canvas.drawText(String.valueOf(val), left + 25, top - 25, graph);
                canvas.restore();
            }

            String date = this.dates.get(i);
            canvas.drawText(date,left+25, (float) (canvas.getHeight()-30),graph);

        }
    }

    public void setData(List<Integer> values, List<String> dates, int color){
        this.color = color;
        this.dates = dates;
        this.values = values;
        for(int x: values){
            if(x < this.min){
                this.min = x;
            }
            if(x > this.max){
                this.max = x;
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawAxes(canvas);
        super.onDraw(canvas);
    }

    private int getBarTop(int val){
        int minHeight = 100;
        int maxHeight = getHeight()-200;
        if(getHeight()==0){
            maxHeight = 800;
        }
        int diff = (maxHeight - minHeight);
        double fraction = ((double)val-(double)this.min)/((double)this.max-(double)this.min);
        Log.d("LOL","diff = " + diff + " fraction = " + fraction);
        return (int)(diff * fraction) + minHeight;
    }
}
