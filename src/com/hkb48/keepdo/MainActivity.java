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

        //���X�g�r���[���쐬
        listView1 = (ListView)findViewById(R.id.listView1);
  
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_list, R.id.list_textview1, mStrings);
        listView1.setAdapter(adapter);
   
        //�N���b�N�C�x���g�����o
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                //listView���w��
                ListView listView = (ListView) parent;
                //�N���b�N���ꂽ���̂��擾
                String item = (String) listView.getItemAtPosition(position);
                //Log�o��
                Log.v("tag", String.format("onItemClick: %s", item));
            }
        });
          
        //�Z���N�g���ꂽ�Ƃ��Ɏ��s�����
        listView1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                //listView���w��
                ListView listView = (ListView) parent;
                //�N���b�N���ꂽ���̂��擾
                String item = (String) listView.getSelectedItem();
                Log.v("tag", String.format("onItemSelected: %s", item));

                Intent intent = new Intent();
                intent.setClassName("com.hkb48.keepdo.MainActivity","com.hkb48.keepdo.CalendarActivity");
                //intent.putExtra("org.jpn.techbooster.demo.intent.testString", "!TEST STRING!");
                startActivity(intent);
            }
            //�����I�����ĂȂ��Ƃ��Ɏ��s
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
