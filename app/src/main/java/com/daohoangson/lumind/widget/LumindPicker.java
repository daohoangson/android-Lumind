package com.daohoangson.lumind.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.Observable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.NumberPicker;

/**
 * @author sondh
 */
abstract public class LumindPicker extends NumberPicker {
    public LumindPicker(Context context) {
        super(context);
        init();
    }

    public LumindPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LumindPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("WeakerAccess")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LumindPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    abstract void init();

    public interface DateObserver {
        Observable.OnPropertyChangedCallback getOnDateChangedCallback();
    }
}
