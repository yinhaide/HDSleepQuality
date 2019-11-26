package com.yhd.sleepquality.app.frag;

import android.graphics.Color;
import android.view.View;
import android.widget.SeekBar;

import com.de.rocket.ue.frag.RoFragment;
import com.de.rocket.ue.injector.BindView;
import com.de.rocket.ue.injector.Event;
import com.yhd.sleepquality.SleepQualityView;
import com.yhd.sleepquality.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 双波浪曲线
 * Created by haide.yin(haide.yin@tcl.com) on 2019/6/6 16:12.
 */
public class Frag_sleepquality extends RoFragment {

    @BindView(R.id.slv)
    private SleepQualityView sleepView;
    @BindView(R.id.seekbar)
    private SeekBar seekBar;

    @Override
    public int onInflateLayout() {
        return R.layout.frag_sleepquality;
    }

    @Override
    public void initViewFinish(View inflateView) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sleepView.setWidthRatio((float) progress / seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onNexts(Object object) {

    }

    @Event(R.id.bt_update)
    private void update(View view) {

        /*
         * 睡眠数据列表,规则如下
         * float[0]:类型,1f：醒来 2f：浅睡 3f：深睡
         * float[1]:距离开始时间的百分比(开始时间/横坐标总时长)
         * float[2]:持续时间长的百分比(持续时间/横坐标总时长)
         */
        List<float[]> timeArray = new ArrayList<>();
        timeArray.add(new float[]{2f,0.18928571f,0.03968254f});
        timeArray.add(new float[]{3f,0.22896825f,0.06825397f});
        timeArray.add(new float[]{2f,0.29722223f,0.05579365f});
        timeArray.add(new float[]{3f,0.35301587f,0.024074074f});
        timeArray.add(new float[]{2f,0.37708995f,0.059391536f});
        timeArray.add(new float[]{3f,0.43648148f,0.025661375f});
        timeArray.add(new float[]{2f,0.46214285f,0.012857143f});
        timeArray.add(new float[]{3f,0.475f,0.1395238f});
        timeArray.add(new float[]{2f,0.6145238f,0.05931217f});
        timeArray.add(new float[]{3f,0.673836f,0.092989415f});
        timeArray.add(new float[]{2f,0.7668254f,0.1042328f});
        timeArray.add(new float[]{3f,0.8710582f,0.019285714f});
        timeArray.add(new float[]{2f,0.8903439f,0.022486772f});

        //x坐标轴的文字描述列表
        List<String> xAxisArray = new ArrayList<>();
        xAxisArray.add("23");
        xAxisArray.add("24");
        xAxisArray.add("1");
        xAxisArray.add("2");
        xAxisArray.add("3");
        xAxisArray.add("4");
        xAxisArray.add("5");
        xAxisArray.add("6");
        xAxisArray.add("7");

        int deepColor = Color.parseColor("#10e191");//深度睡眠的颜色
        int shallowColor = Color.parseColor("#398eff");//浅度睡眠的颜色
        int wakeColor = Color.parseColor("#ffa239");//清醒的颜色
        sleepView.setLineColor(wakeColor,shallowColor,deepColor);
        sleepView.setYAxisString("Wake","Shallow","Deep");
        sleepView.setDataSource(timeArray,xAxisArray);
    }
}
