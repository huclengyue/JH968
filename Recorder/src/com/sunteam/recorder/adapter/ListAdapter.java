package com.sunteam.recorder.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sunteam.common.tts.TtsUtils;
import com.sunteam.recorder.Global;
import com.sunteam.recorder.R;

/**
 * ���б���
 * 
 * @author wzp
 *
 */
public class ListAdapter extends BaseAdapter implements OnClickListener
{
	private Context mContext = null;
	private ArrayList<String> gListData = null;
	private int selectItem = 0;	//��ǰѡ�е��Ĭ���ǵ�һ��
	private OnEnterListener mOnEnterListener = null;
	private Handler mHandler = null;
	@SuppressWarnings("unused")
	private int speakWay; //0:��ϣ�1��׷��
	private boolean isScanning = true;
	private boolean isStop = false;
	
	public interface OnEnterListener 	{
		public void onEnterCompleted( int selectItem, String menu );
	}

	//����ȷ��������
	public void setOnEnterListener( OnEnterListener listener ){
		mOnEnterListener = listener;
	}
	
	public ListAdapter( Context context, OnEnterListener listener, ArrayList<String> list ,Handler handler){
		this.mContext = context;
		this.gListData = list;
		this.selectItem = 0;
		this.mOnEnterListener = listener;
		this.mHandler = handler;
	}

	public void setSelectItem( int selectItem, int way){
		this.selectItem = selectItem;
		
		readSelectItemContent(way);	//�˴���Ҫ����tts�ʶ�selectItem����
		
		this.notifyDataSetChanged();
	}
	
	public void setSelectItemNoSpeak( int selectItem){
		this.selectItem = selectItem;
		this.notifyDataSetChanged();
	}
	
	public int getSelectItem(){
		return	this.selectItem;
	}
	
	public String getSelectItemContent(){
		if( gListData != null && gListData.size() > 0)	{
			if(gListData.get(selectItem).contains("REC")){
				return gListData.get(selectItem).replace("REC", "record");
			}else{
				return	gListData.get(selectItem);
			}
		}
		
		return	"";
	}
	
	//�����ϼ�
	public void up(){
		if( this.selectItem > 0 ){
			this.selectItem--;
			readSelectItemContent(0);		
			this.notifyDataSetChanged();
		}else{
			this.selectItem = gListData.size() - 1;
			mHandler.sendEmptyMessage(1);
			//Global.showToast(mContext, R.string.turn2end,mHandler,1);
		}			
	}
	
	//�����¼�
	public void down(){
		if( this.selectItem < gListData.size() - 1 ){
			this.selectItem++;
			readSelectItemContent(0);	
			this.notifyDataSetChanged();
		}else{
			this.selectItem = 0;
			mHandler.sendEmptyMessage(0);
			//Global.showToast(mContext, R.string.turn2start,mHandler,0);
		}
	}
	
	//���ε��
	public void enter(){
		if( mOnEnterListener != null ){
			mOnEnterListener.onEnterCompleted( this.selectItem, this.gListData.get(selectItem) );
		}
	}
	
	//tts�ʶ�selectItem����
	public void readSelectItemContent(int speakWay){
		String content;
		if(gListData.get(selectItem).contains("REC")){
			content = gListData.get(selectItem).replace("REC", "record");
		}else{
			content = gListData.get(selectItem);
		}
		if(speakWay == 0){
			TtsUtils.getInstance().speak(content);
		}else{
			TtsUtils.getInstance().speak(content, TtsUtils.TTS_QUEUE_ADD);
		}
		
	}	
	
	public boolean isScanning() {
		return isScanning;
	}

	public void setScanning(boolean isScanning) {
		this.isScanning = isScanning;
	}

	public boolean isStop() {
		return isStop;
	}

	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}

	public ArrayList<String> getListData()	{
		return	gListData;
	}
	
	public void setListData( ArrayList<String> list ){
		gListData = list;
	}
	
	@Override
	public int getCount(){
		if( null == gListData ){
			return	0;
		}
        return gListData.size();
    }
    
    @Override
    public Object getItem(int position) {
    	if( null == gListData ){
			return	null;
		}
       return gListData.get(position);
    }
    
    @Override
    public long getItemId(int position)  {
    	if( null == gListData ){
			return	0;
		}
        return	position;
    }
    
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;

        if( null == convertView ) {
        	vh = new ViewHolder();
        	convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
        	
        	vh.item = (TextView)convertView.findViewById(R.id.text1);    		
    		vh.item.setOnClickListener(this);

        	convertView.setTag(vh);
        }else{
        	vh = (ViewHolder) convertView.getTag();
        }
        
        vh.item.setTag(String.valueOf(position));
        
        if( selectItem == position ){	//ѡ��	
        	convertView.setBackgroundColor(Global.getForegroundColor());
        	//convertView.setBackgroundResource(R.color.yellow);
		}else{
			//convertView.setBackgroundResource(R.color.transparent);
			convertView.setBackgroundColor(Global.getBackgroundColor());
		}
		
    	if( !TextUtils.isEmpty( gListData.get(position) ) ){
    		vh.item.setText( gListData.get(position) );
    	}else	{
    		vh.item.setText( "" );
    	}
    	vh.item.setTextColor(Global.getApp_text_color());
                
        return convertView;
	}

	@Override
	public void onClick(View v) 	{
		int id = v.getId();
		int position = 0;
		
		setScanning(false);
		
		String tag = (String)v.getTag();
		position = Integer.parseInt(tag);
		
		/*switch( id ){
			case R.id.text1:
				if( this.selectItem != position ){
					Log.e("click", "one");
					setSelectItem(position, 0);
				}else{
					Log.e("click", "two");
					enter();	//������һ������
				}
				break;
			default:
				break;
		}*/
		if (R.id.text1 == id) {
			if (this.selectItem != position) {
				Log.e("click", "one");
				setSelectItem(position, 0);
			} else {
				Log.e("click", "two");
				enter(); // ������һ������
			}
		}
	}
	
	
	private class ViewHolder	{
		TextView item = null;				//�˵�����
	}
}
