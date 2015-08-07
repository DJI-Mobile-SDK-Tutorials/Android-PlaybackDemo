/**   
 * TODO
 * @Title       : DroneSelectionActivity.java 
 * @Package     : com.dji.download.demo 
 * @author 	    : DJI Software
 * @date        : 2015年7月22日 下午4:20:45 
 * @version     : 2.0.0
 */


package com.dji.download.demo;

import java.util.Timer;

import com.dji.download.demo.PlaybackProtocolActivity.Task;
import com.dji.mediadownloaddemo.R;

import dji.midware.data.manager.P3.ServiceManager;
import dji.sdk.api.DJIDrone;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/** 
 * @author      : DJI Software
 * @date        : 16:20:45, 22 July 
 * @version     : V1.0
 */

public class DroneSelectionActivity extends Activity
{
    private static final String TAG = "DroneSelectionActivitiy";
    
    private final int SHOWDIALOG = 0;
    
    private static DroneTypeInfo[] DroneTypes = {
      new DroneTypeInfo(R.string.drone_selection_activity_title_ins, R.string.drone_selection_activity_desc_ins),
      new DroneTypeInfo(R.string.drone_selection_activity_title_p3p, R.string.drone_selection_activity_desc_p3p),
      new DroneTypeInfo(R.string.drone_selection_activity_title_M100, R.string.drone_selection_activity_desc_M100),
      new DroneTypeInfo(R.string.drone_selection_activity_title_auto_ins, R.string.drone_selection_activity_desc_auto_ins),
      new DroneTypeInfo(R.string.drone_selection_activity_title_auto_p3p, R.string.drone_selection_activity_desc_auto_p3p),
      new DroneTypeInfo(R.string.drone_selection_activity_title_auto_M100, R.string.drone_selection_activity_desc_auto_M100),
    };
    
    private Handler handler = new Handler(new Handler.Callback() {
        
        @Override
        public boolean handleMessage(Message msg)
        {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case SHOWDIALOG : {
                    
                    break;
                }
                
                default : {
                    break;
                }
            }
            return false;
        }
    });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_type_selection);
        
        ListView mListView = (ListView)findViewById(R.id.DroneSelectionItemListView);
        mListView.setAdapter(new SelectDroneTypeAdapter());
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
               onListItemClick(position);
            }
            
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        ServiceManager.getInstance().pauseService(false);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        ServiceManager.getInstance().pauseService(true);
    }
    
    private class SelectDroneTypeAdapter extends BaseAdapter {

        @Override
        public int getCount()
        {
            return DroneTypes.length;
        }
	
        @Override
        public Object getItem(int position)
        {
            // TODO Auto-generated method stub
            return DroneTypes[position];
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }
	
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            convertView = View.inflate(DroneSelectionActivity.this, R.layout.demo_info_item, null);
            TextView title = (TextView)convertView.findViewById(R.id.title);
            TextView desc = (TextView)convertView.findViewById(R.id.desc);
            
            title.setText(DroneTypes[position].title);
            desc.setText(DroneTypes[position].desc);
            return convertView;
        }
        
    }
    
    private static class DroneTypeInfo{
        private final int title;
        private final int desc;

        public DroneTypeInfo(int title , int desc) {
            this.title = title;
            this.desc  = desc;
        }
    }
    
    private void onListItemClick(int index) {
        Intent intent = null;
        
        switch (index) { 
            case 0 : {
                intent = new Intent(DroneSelectionActivity.this, PlaybackProtocolActivity.class);
                intent.putExtra("droneType", 0);
                break;
            }
            
            case 1 : {
                intent = new Intent(DroneSelectionActivity.this, PlaybackProtocolActivity.class);
                intent.putExtra("droneType", 1);
                break;
            }
            
            case 2 : {
                intent = new Intent(DroneSelectionActivity.this, PlaybackProtocolActivity.class);
                intent.putExtra("droneType", 3);
                break;
            }
            
//            case 3 : {
//                intent = new Intent(DroneSelectionActivity.this, PlaybackProtocolActivity.class);
//                intent.putExtra("droneType", 3);
//                break;
//            }
            
            case 3 : {
                intent = new Intent(DroneSelectionActivity.this, AutoDownloadActivity.class);
                intent.putExtra("droneType", 0);
                break;
            }
            
            case 4 : {
                intent = new Intent(DroneSelectionActivity.this, AutoDownloadActivity.class);
                intent.putExtra("droneType", 1);
                break;
            }
            
            case 5 : {
                intent = new Intent(DroneSelectionActivity.this, AutoDownloadActivity.class);
                intent.putExtra("droneType", 3);
                break;
            }
            
            default : {
                intent = new Intent(DroneSelectionActivity.this, PlaybackProtocolActivity.class);
                break;
            }
        }

        this.startActivity(intent);
    }
    
}
