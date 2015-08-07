/**   
 * TODO
 * @Title       : PlaybackGridView.java 
 * @Package     : com.dji.download.demo 
 * @author 	    : DJI Software
 * @date        : 2015年7月24日 下午3:51:04 
 * @version     : 2.0.0
 */


package com.dji.download.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/** 
 * @author      : DJI Software
 * @date        : 2015年7月24日 下午3:51:04 
 * @version     : V1.0
 */

public class PlaybackGridView extends GridView
{
    	
    public PlaybackGridView(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public PlaybackGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlaybackGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
             
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    // TODO Auto-generated method stub
        if(ev.getAction() == MotionEvent.ACTION_MOVE) {
           return true;
        }
        return super.dispatchTouchEvent(ev);
    }
    
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }
}
