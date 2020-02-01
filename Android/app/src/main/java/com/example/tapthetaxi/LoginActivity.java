package com.example.tapthetaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tapthetaxi.Retrofit.INodeJS;
import com.example.tapthetaxi.Retrofit.RetrofitClient;

import org.json.JSONObject;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    INodeJS myAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    EditText edt_id, edt_password;
    Button btn_login, btn_register;
    Intent ittRegister;
    Intent ittMain;

    @Override
    protected void onStop(){
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Retrofit retrofit = RetrofitClient.getInstance();
        myAPI = retrofit.create(INodeJS.class);

        edt_id = (EditText)findViewById(R.id.al_etLoginID);
        edt_password = (EditText)findViewById(R.id.al_etLoginPW);
        btn_login = (Button)findViewById(R.id.al_btLogin);
        btn_register = (Button)findViewById(R.id.al_btRegister);
        ittRegister = new Intent(LoginActivity.this, RegisterActivity.class);
        ittMain = new Intent(LoginActivity.this, MainActivity.class);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(edt_id.getText().toString(), edt_password.getText().toString());
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ittRegister);
            }
        });
    }

    private void loginUser(String id, String password) {
        compositeDisposable.add(myAPI.loginUser(id, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if(s.contains("userPassword")){
                            //Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                            JSONObject jsonObject = new JSONObject(s);
                            String id = jsonObject.getString("userID");
                            String name = jsonObject.getString("userName");
                            saveSession(id);
                            Toast.makeText(LoginActivity.this, name+"님 환영합니다.", Toast.LENGTH_SHORT).show();
                            startActivity(ittMain);
                        }
                        else
                            Toast.makeText(LoginActivity.this, ""+s, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void saveSession(String s){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("session", s);
        editor.commit();
    }
}
