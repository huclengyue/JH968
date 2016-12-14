package com.sunteam.recorder.recorder;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.sunteam.recorder.R;

public class ErrorCode {
    public final static int E_NOSDCARD = 1001;
    public final static int SUCCESS = 1000;
    public final static int E_STATE_RECODING = 1002;
    public final static int E_UNKOWN = 1003;
     
     
    public static int getErrorInfo(Context vContext, int vType) throws NotFoundException
    {
    	int retval = 1;
        switch(vType)
        {
        case SUCCESS:
        	retval =  1;
            break;
        case E_NOSDCARD:
        	retval = R.string.error_lack_space;
        	break;
        case E_STATE_RECODING:
        	retval =  R.string.error_state_record;  
        	break;
        case E_UNKOWN:
        default:
        	retval =  R.string.error_unknown;  
        	break;
             
        }
        return retval;
    }
 
}
