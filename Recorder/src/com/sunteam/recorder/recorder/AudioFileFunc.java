package com.sunteam.recorder.recorder;

import java.io.File;

import com.sunteam.recorder.Global;

import android.media.MediaRecorder;
import android.os.Environment;

public class AudioFileFunc {
    //音频输入-麦克风
    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    public final static int AUDIO_INPUT2 = 9;//MediaRecorder.AudioSource.CAMCORDER;
     
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public final static int AUDIO_SAMPLE_RATE = 16000;
    //录音输出文件
    public static String AUDIO_WAV_FILENAME;
    public static File fileBasePath = new File(Global.storagePath);
     
    /**
     * 判断是否有外部存储设备sdcard
     * @return true | false
     */
    public static boolean isSdcardExit(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
        	if(!fileBasePath.exists())
        		fileBasePath.mkdirs();
        	return true;
        }else{
        	 return false;
        }       
    }
     
    /**
     * 获取编码后的WAV格式音频文件路径
     * @return
     */
    public static String getWavFilePath(){
        String mAudioWavPath = "";
        if(isSdcardExit()){
            mAudioWavPath = fileBasePath+"/"+AUDIO_WAV_FILENAME;
        }
        return mAudioWavPath;
    }  
     
     
    /**
     * 获取文件大小
     * @param path,文件的绝对路径
     * @return
     */
    public static long getFileSize(String path){
        File mFile = new File(path);
        if(!mFile.exists())
            return -1;
        return mFile.length();
    }
 
}
