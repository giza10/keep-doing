package com.hkb48.keepdo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

public class CalendarActivity extends MainActivity {
	private GridLayout mGridLayout;
    private int mPosition = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.calendar_activity);

        mGridLayout = (GridLayout) findViewById(R.id.gridLayout);
        
        buildCalendar();
    }

    /***
     * <蟷ｴ4譯�/<譛茨ｼ呈｡�縺ｮ繝輔か繝ｼ繝槭ャ繝医〒譁�ｭ怜�繧剃ｽ懈�縺玲怦陦ｨ遉ｺ逕ｨ縺ｮTextView縺ｫ險ｭ螳�
     * 
     * @param calendar
     */
    private void setTitle(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");
        TextView textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setText(sdf.format(calendar.getTime()));
    }
    
    /***
     * 繧ｫ繝ｬ繝ｳ繝��繝��繧ｿ繧定ｨｭ螳�
     */
    private void buildCalendar() {
        // 陦ｨ遉ｺ縺吶ｋ譛医�繝昴ず繧ｷ繝ｧ繝ｳ繧堤畑縺�※繧ｫ繝ｬ繝ｳ繝��繧堤函謌�
        Calendar current = Calendar.getInstance();
        current.add(Calendar.MONTH, mPosition);
        current.set(Calendar.DAY_OF_MONTH, 1);

        // 繧ｿ繧､繝医Ν繧定ｨｭ螳�
        setTitle(current);

        // 繧ｫ繝ｬ繝ｳ繝��繝��繧ｿ繧偵Μ繧ｻ繝�ヨ
        mGridLayout.removeAllViews();

        // 譖懈律繧定ｨｭ螳�
        addDayOfWeek();

        // 譌･豈弱�繝��繧ｿ繧定ｨｭ螳�
        addDayOfMonth(current);
    }

    /***
     * 譖懈律繧定ｨｭ螳�
     */
    private void addDayOfWeek() {    	
        // 譌･�槫悄縺ｮ譖懈律縺ｮ譁�ｭ怜�繧貞叙蠕�
        String[] weeks = getResources().getStringArray(R.array.week_names);
        for (int i = 0; i < weeks.length; i++) {
            // 譖懈律縺ｮ繝ｬ繧､繧｢繧ｦ繝医ｒ逕滓�
            View child = getLayoutInflater().inflate(R.layout.calendar_week, null);
            // 譖懈律繧定ｨｭ螳壹☆繧亀extView縺ｮ繧､繝ｳ繧ｹ繧ｿ繝ｳ繧ｹ繧貞叙蠕�
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            // 譖懈律繧探extView縺ｫ險ｭ螳�
            textView1.setText(weeks[i]);
            // 譖懈律縺梧律譖懈律縺ｪ繧芽ｵ､縲√◎縺�〒縺ｪ縺代ｌ縺ｰ鮟偵�繝�く繧ｹ繝医き繝ｩ繝ｼ繧定ｨｭ螳�
            textView1.setTextColor(i == 0 ? Color.RED : Color.BLACK);

            // 菴懈�縺輔ｌ縺滓屆譌･縺ｮ繝ｬ繧､繧｢繧ｦ繝医ｒGridLayout縺ｫ霑ｽ蜉�
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setGravity(Gravity.FILL_HORIZONTAL);
            mGridLayout.addView(child);
        }
    }

    /***
     * 譌･豈弱�繝��繧ｿ繧定ｨｭ螳�
     * 
     * @param calendar
     */
    private void addDayOfMonth(Calendar calendar) {
        // 陦ｨ遉ｺ譛医�譛�､ｧ譌･謨ｰ繧貞叙蠕�
        int maxdate = calendar.getMaximum(Calendar.DAY_OF_MONTH);

        // 譛�､ｧ譌･謨ｰ蛻�ｹｰ繧願ｿ斐＠蜃ｦ逅�ｒ陦後≧
        for (int i = 0; i < maxdate; i++) {
            // 譌･豈弱�繝ｬ繧､繧｢繧ｦ繝医ｒ逕滓�
            View child = getLayoutInflater().inflate(R.layout.calendar_date, null);
            // 譌･豈弱�繝ｬ繧､繧｢繧ｦ繝医°繧牙推View縺ｮ繧､繝ｳ繧ｹ繧ｿ繝ｳ繧ｹ繧貞叙蠕�
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            TextView textView2 = (TextView) child.findViewById(R.id.textView2);
            TextView textView3 = (TextView) child.findViewById(R.id.textView3);
            // 菴懈�縺吶ｋ譌･縺ｮ譖懈律繧貞叙蠕�
            int week = calendar.get(Calendar.DAY_OF_WEEK);

            // 菴懈�縺吶ｋ譌･縺ｮ譌･莉倥ｒTextView縺ｫ險ｭ螳�
            textView1.setText(Integer.toString(i + 1));
            // 菴懈�縺吶ｋ譌･縺梧律譖懈律縺ｪ繧芽ｵ､縲√◎縺�〒縺ｪ縺代ｌ縺ｰ鮟偵�繝�く繧ｹ繝医き繝ｩ繝ｼ繧定ｨｭ螳�
            textView1.setTextColor(week == Calendar.SUNDAY ? Color.RED : Color.BLACK);

            // 菴懈�縺吶ｋ譌･縺ｮ繧､繝吶Φ繝医ｒ蜈ｨ縺ｦ蜿門ｾ�
//            List<CalendarEvent> events = queryEvent(calendar);
//            if (events != null && events.size() > 0) {
//                // 繧､繝吶Φ繝医′縺ゅｌ縺ｰ荳�▽逶ｮ縺ｮ繧､繝吶Φ繝医�繧ｿ繧､繝医Ν繧貞叙蠕励＠TextView縺ｫ險ｭ螳�
//                textView2.setText(events.get(0).getTitle());
//                // 隍�焚縺ｮ繧､繝吶Φ繝医′縺ゅｌ縺ｰ縲後�繝ｻ繝ｻ縲阪ｒ陦ｨ遉ｺ
//                if (events.size() > 1) {
//                    textView3.setVisibility(View.VISIBLE);
//                }
//                child.setTag(events);
//            }
//            else {
                // 繧､繝吶Φ繝医′縺ｪ縺��蜷医�繝悶Λ繝ｳ繧ｯ繧定ｨｭ螳�
                textView2.setText("");
                textView3.setVisibility(View.GONE);
//            }

            // 菴懈�縺輔ｌ縺滓律縺ｮ繝ｬ繧､繧｢繧ｦ繝医ｒGridLayout縺ｫ霑ｽ蜉�
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            if (i == 0) {
                params.rowSpec = GridLayout.spec(1);
                params.columnSpec = GridLayout.spec(week - Calendar.SUNDAY);
            }
            params.setGravity(Gravity.FILL_HORIZONTAL);
            mGridLayout.addView(child, params);

            // 譌･縺ｫ縺｡繧抵ｼ第律騾ｲ繧√ｋ
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
}
