package com.example.tapthetaxi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tapthetaxi.Retrofit.INodeJS;
import com.example.tapthetaxi.Retrofit.RetrofitClient;

import org.json.JSONObject;

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

        // 아이디 객체
        edt_id = (EditText)findViewById(R.id.al_etLoginID);
        // 패스워드 객체
        edt_password = (EditText)findViewById(R.id.al_etLoginPW);
        // 로그인 버튼 객체
        btn_login = (Button)findViewById(R.id.al_btLogin);
        // 회원가입 버튼 객체
        btn_register = (Button)findViewById(R.id.al_btRegister);
        // 인텐트 (로그인 액티비티 → 회원가입 액티비티)
        ittRegister = new Intent(LoginActivity.this, RegisterActivity.class);
        // 인텐트 (로그인 액티비티 → 메인 액티비티)
        ittMain = new Intent(LoginActivity.this, MainActivity.class);

        // 로그인 버튼 리스너
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // loginUser 함수 실행
                loginUser(edt_id.getText().toString(), edt_password.getText().toString());
            }
        });

        // 회원가입 버튼 리스너
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ittRegister);
            }
        });
    }

    // 로그인 함수
    private void loginUser(String id, String password) {
        compositeDisposable.add(myAPI.loginUser(id, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        // 로그인 성공
                        if(s.contains("userPassword")){
                            //Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                            // json 객체 받아옴
                            JSONObject jsonObject = new JSONObject(s);
                            // 아이디, 이름
                            String id = jsonObject.getString("userID");
                            String name = jsonObject.getString("userName");
                            // 세션 저장
                            saveSession(id);
                            // 로그인 성공 문구 표시
                            Toast.makeText(LoginActivity.this, name+"님 환영합니다.", Toast.LENGTH_SHORT).show();
                            // 메인 화면으로 이동
                            startActivity(ittMain);
                        }
                        else
                            // 실패 이유 문구 표시(비밀번호 오류 or 아이디 오류)
                            Toast.makeText(LoginActivity.this, ""+s, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    // 세션 저장
    private void saveSession(String s){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("session", s);
        editor.commit();
    }
}
