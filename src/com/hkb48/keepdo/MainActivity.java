package com.hkb48.keepdo;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	private String[] mStrings ={ "Action1", "Action2", "Action3", "Action4" };
	ListView listView1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //リストビューを作成
        listView1 = (ListView)findViewById(R.id.listView1);
  
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_list, R.id.list_textview1, mStrings);
        listView1.setAdapter(adapter);
   
        //クリックイベントを検出
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                //listViewを指定
                ListView listView = (ListView) parent;
                //クリックされたものを取得
                String item = (String) listView.getItemAtPosition(position);
                //Log出力
                Log.v("tag", String.format("onItemClick: %s", item));
            }
        });
          
        //セレクトされたときに実行される
        listView1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                //listViewを指定
                ListView listView = (ListView) parent;
                //クリックされたものを取得
                String item = (String) listView.getSelectedItem();
                Log.v("tag", String.format("onItemSelected: %s", item));

                Intent intent = new Intent();
                intent.setClassName("com.hkb48.keepdo.MainActivity","com.hkb48.keepdo.CalendarActivity");
                //intent.putExtra("org.jpn.techbooster.demo.intent.testString", "!TEST STRING!");
                startActivity(intent);
            }
            //何も選択さてないときに実行
            public void onNothingSelected(AdapterView<?> parent) {
                Log.v("tag", "onNothingSelected");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
