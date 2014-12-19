package dennisdufback.app.labb3deno;

import android.content.Context;
import android.view.View;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class ListItem extends View {
    private int backgroundColor,textColor;
    private String listText;
    private Paint listPaint;
    public ListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        listPaint = new Paint();
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.listItem, 0, 0);

        try {
            listText = a.getString(R.styleable.listItem_listText);
            backgroundColor = a.getInteger(R.styleable.listItem_backgroundColor, 0);
            textColor = a.getInteger(R.styleable.listItem_textColor, 0);
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
        listPaint.setColor(backgroundColor);

        canvas.drawRect(0, 0, viewWidth, viewHeight, listPaint);
        listPaint.setColor(textColor);
        listPaint.setTextAlign(Paint.Align.LEFT);
        listPaint.setTextSize(viewHeight*0.7f);
        canvas.drawText(listText,0,canvas.getHeight()/2+listPaint.getTextSize()/3,listPaint);
    }
    public int getBackgroundColor(){
        return backgroundColor;
    }
    public int getTextColor(){
        return textColor;
    }
    public String getListText(){
        return listText;
    }
    public void setBackgroundColor(int newColor){
        backgroundColor = newColor;
        invalidate();
        requestLayout();
    }
    public void setTextColor(int newColor){
        textColor = newColor;
        invalidate();
        requestLayout();
    }
    public void setListText(String newLabel) {
        listText = newLabel;
        invalidate();
        requestLayout();
    }

}
