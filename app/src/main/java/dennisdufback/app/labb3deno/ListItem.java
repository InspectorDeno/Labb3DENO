package dennisdufback.app.labb3deno;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.jar.Attributes;

public class ListItem extends View{
    private int listColor,labelColor;
    private String listText;
    private Paint listPaint;
    private Context context;
    private AttributeSet attrs;
    public ListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        listPaint = new Paint();
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.listItem, 0, 0);

        try {
            listText = a.getString(R.styleable.listItem_listText);
            listColor = a.getInteger(R.styleable.listItem_listColor, 0);
            labelColor = a.getInteger(R.styleable.listItem_labelColor, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float viewWidth = this.getWidth();
        float viewHeight = this.getHeight();
        listPaint.setStyle(Style.FILL);
        listPaint.setAntiAlias(true);
        listPaint.setColor(listColor);

        canvas.drawRect(0, 0, viewWidth, viewHeight, listPaint);
        listPaint.setColor(labelColor);
        listPaint.setTextAlign(Paint.Align.LEFT);
        listPaint.setTextSize(80);
        canvas.drawText(listText,0,canvas.getHeight()/2+listPaint.getTextSize()/3,listPaint);
    }
    public int getListColor(){
        return listColor;
    }
    public int getLabelColor(){
        return labelColor;
    }
    public String getListText(){
        return listText;
    }
    public void setListColor(int newColor){
        listColor = newColor;
        invalidate();
        requestLayout();
    }
    public void setLabelColor(int newColor){
        labelColor = newColor;
        invalidate();
        requestLayout();
    }
    public void setListText(String newLabel){
        listText = newLabel;
        invalidate();
        requestLayout();
    }
    public void addName(String newName){
        listText = listText + " " + newName;
        invalidate();
        requestLayout();
    }
}
