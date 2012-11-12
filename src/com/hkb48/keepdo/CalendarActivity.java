package com.hkb48.keepdo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

public class CalendarActivity extends Activity {
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
     * <年4桁>/<月２桁>のフォーマットで文字列を作成し月表示用のTextViewに設定
     * 
     * @param calendar
     */
    private void setTitle(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");
        TextView textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setText(sdf.format(calendar.getTime()));
    }
    
    /***
     * カレンダーデータを設定
     */
    private void buildCalendar() {
        // 表示する月のポジションを用いてカレンダーを生成
        Calendar current = Calendar.getInstance();
        current.add(Calendar.MONTH, mPosition);
        current.set(Calendar.DAY_OF_MONTH, 1);

        // タイトルを設定
        setTitle(current);

        // カレンダーデータをリセット
        mGridLayout.removeAllViews();

        // 曜日を設定
        addDayOfWeek();

        // 日毎のデータを設定
        addDayOfMonth(current);
    }

    /***
     * 曜日を設定
     */
    private void addDayOfWeek() {    	
        // 日～土の曜日の文字列を取得
        String[] weeks = getResources().getStringArray(R.array.week_names);
        for (int i = 0; i < weeks.length; i++) {
            // 曜日のレイアウトを生成
            View child = getLayoutInflater().inflate(R.layout.calendar_week, null);
            // 曜日を設定するTextViewのインスタンスを取得
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            // 曜日をTextViewに設定
            textView1.setText(weeks[i]);
            // 曜日が日曜日なら赤、そうでなければ黒のテキストカラーを設定
            textView1.setTextColor(i == 0 ? Color.RED : Color.BLACK);

            // 作成された曜日のレイアウトをGridLayoutに追加
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setGravity(Gravity.FILL_HORIZONTAL);
            mGridLayout.addView(child);
        }
    }

    /***
     * 日毎のデータを設定
     * 
     * @param calendar
     */
    private void addDayOfMonth(Calendar calendar) {
        // 表示月の最大日数を取得
        int maxdate = calendar.getMaximum(Calendar.DAY_OF_MONTH);

        // 最大日数分繰り返し処理を行う
        for (int i = 0; i < maxdate; i++) {
            // 日毎のレイアウトを生成
            View child = getLayoutInflater().inflate(R.layout.calendar_date, null);
            // 日毎のレイアウトから各Viewのインスタンスを取得
            TextView textView1 = (TextView) child.findViewById(R.id.textView1);
            TextView textView2 = (TextView) child.findViewById(R.id.textView2);
            TextView textView3 = (TextView) child.findViewById(R.id.textView3);
            // 作成する日の曜日を取得
            int week = calendar.get(Calendar.DAY_OF_WEEK);

            // 作成する日の日付をTextViewに設定
            textView1.setText(Integer.toString(i + 1));
            // 作成する日が日曜日なら赤、そうでなければ黒のテキストカラーを設定
            textView1.setTextColor(week == Calendar.SUNDAY ? Color.RED : Color.BLACK);

            // 作成する日のイベントを全て取得
//            List<CalendarEvent> events = queryEvent(calendar);
//            if (events != null && events.size() > 0) {
//                // イベントがあれば一つ目のイベントのタイトルを取得しTextViewに設定
//                textView2.setText(events.get(0).getTitle());
//                // 複数のイベントがあれば「・・・」を表示
//                if (events.size() > 1) {
//                    textView3.setVisibility(View.VISIBLE);
//                }
//                child.setTag(events);
//            }
//            else {
                // イベントがない場合はブランクを設定
                textView2.setText("");
                textView3.setVisibility(View.GONE);
//            }

            // 作成された日のレイアウトをGridLayoutに追加
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            if (i == 0) {
                params.rowSpec = GridLayout.spec(1);
                params.columnSpec = GridLayout.spec(week - Calendar.SUNDAY);
            }
            params.setGravity(Gravity.FILL_HORIZONTAL);
            mGridLayout.addView(child, params);

            // 日にちを１日進める
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
}
