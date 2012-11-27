package com.hkb48.keepdo;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TasksActivity extends MainActivity {
    private static final int SUB_ACTIVITY = 1001;
    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //リストビューを作成
        ListView listView1 = (ListView)findViewById(R.id.listView1);
  
        ArrayList<String> items = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, R.layout.main_list, R.id.list_textview1, items);
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

                // TODO
                int taskId = 10;

                // Show calendar view
                Intent intent = new Intent(TasksActivity.this, CalendarActivity.class);
                intent.putExtra("TASK-ID", taskId);
                startActivity(intent);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add_task:
            Intent intent = new Intent(TasksActivity.this, TaskSettingActivity.class);
            intent.setAction("com.hkb48.keepdo.NEW_TASK");
            startActivityForResult(intent, SUB_ACTIVITY);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SUB_ACTIVITY) {
            if(resultCode == RESULT_OK) {
                Task task = (Task) data.getSerializableExtra("NEW-TASK");
                addTask(task.getName(), task.getRecurrence());
                List<Task> taskList = getTaskList();
                for(int i=0; i<taskList.size(); i++) {
                    adapter.add(task.getName());
                }
            }
        }
    }
}
