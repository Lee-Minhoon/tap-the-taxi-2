package com.example.tapthetaxi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tapthetaxi.Retrofit.INodeJS;
import com.example.tapthetaxi.Retrofit.RetrofitClient;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class ChatActivity extends AppCompatActivity {

    private ListView lv;
    private MyAdapter mAdapter;
    private ArrayList<listItem> list;
    INodeJS myAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    Button button;
    ImageView money;
    EditText editText;
    Intent ittLogin;
    String getSession;

    private Socket socket;
    {
        try {
            socket = IO.socket("http://10.0.2.2:3000/");
        }catch (URISyntaxException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        list = new ArrayList<listItem>();
        lv = (ListView)findViewById(R.id.lv);
        mAdapter = new MyAdapter(list);
        lv.setAdapter(mAdapter);

        Retrofit retrofit = RetrofitClient.getInstance();
        myAPI = retrofit.create(INodeJS.class);

        ittLogin = new Intent(ChatActivity.this, LoginActivity.class);

        getSession = sessionCheck();
        if(getSession.length() == 0){
            startActivity(ittLogin);
        }

        socket.connect();
        socket.emit("joinRoom", getSession);
        socket.on("message_from_server", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                JSONObject jsonObject = (JSONObject)args[0];
                String sname = null;
                try {
                    sname = jsonObject.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String schat = null;
                try {
                    schat = jsonObject.getString("chat");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                list.add(new listItem(sname, schat));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        socket.on("request_money", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                JSONObject jsonObject = (JSONObject)args[0];
                String request_money = null;
                try {
                    request_money = jsonObject.getString("request_money");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                acceptMoney(Integer.parseInt(request_money));
            }
        });

        editText = (EditText) findViewById(R.id.ac_etChat);
        button = (Button) findViewById(R.id.ac_btChat);
        money = (ImageView) findViewById(R.id.ac_imMoney);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString();
                Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                editText.setText(null);
                socket.emit("message_from_client", msg, getSession);
            }
        });

        money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMoney();
            }
        });
    }

    public void acceptMoney(final Integer m){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("송금 확인");
        alert.setMessage(m + "원의 송금 요청입니다.");

        alert.setPositiveButton("수락", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                compositeDisposable.add(myAPI.account(getSession)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                if(s.contains("userNo")){
                                    JSONObject jsonObject = new JSONObject(s);
                                    // 계좌 번호와 잔액 불러옴
                                    String balance = jsonObject.getString("accountBalance");
                                    if(Integer.parseInt(balance) >= m) {
                                        socket.emit("moneyOK", m, getSession);
                                    }
                                    else {
                                        Toast.makeText(ChatActivity.this, "잔액 부족", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else{
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                );
            }
        });

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!ChatActivity.this.isFinishing()) {
                    alert.show();
                }
            }
        });
    }

    public void requestMoney(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("송금 요청");
        alert.setMessage("택시 요금을 입력하세요");

        final EditText money = new EditText(this);
        alert.setView(money);

        alert.setPositiveButton("요청", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Integer total = Integer.parseInt(money.getText().toString());
                socket.emit("money", total);
            }
        });

        alert.show();
    }

    public class listItem{
        private String name;
        private String chat;

        public listItem(String name, String chat){
            this.name = name;
            this.chat = chat;
        }
    }

    public class MyAdapter extends BaseAdapter{
        private ArrayList<listItem> list;

        public MyAdapter(ArrayList<listItem> list){
            this.list = list;
        }

        @Override
        public int getCount(){
            return list.size();
        }

        @Override
        public listItem getItem(int position){
            return list.get(position);
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            final int pos = position;
            View v = convertView;
            ViewHolder holder;

            if(v == null){
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView)v.findViewById(R.id.name);
                holder.chat = (TextView)v.findViewById(R.id.chat);
                v.setTag(holder);
            }else{
                holder = (ViewHolder) v.getTag();
            }

            holder.name.setText(getItem(pos). name);
            holder.chat.setText(getItem(pos). chat);
            return v;
        }
    }

    static class ViewHolder{
        TextView name;
        TextView chat;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("room");
        editor.commit();

        compositeDisposable.add(myAPI.roomExit(getSession)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private String sessionCheck(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String s = pref.getString("session", "");
        return s;
    }
}


