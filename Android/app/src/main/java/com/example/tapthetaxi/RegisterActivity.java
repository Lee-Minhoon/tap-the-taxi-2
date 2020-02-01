package com.example.tapthetaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tapthetaxi.Retrofit.INodeJS;
import com.example.tapthetaxi.Retrofit.RetrofitClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    INodeJS myAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    EditText edt_name, edt_id, edt_password, edt_tel, edt_account;
    Button btn_register;
    Intent ittRegister;

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
        setContentView(R.layout.activity_register);

        Retrofit retrofit = RetrofitClient.getInstance();
        myAPI = retrofit.create(INodeJS.class);

        edt_name = (EditText)findViewById(R.id.ar_etRegisterName);
        edt_id = (EditText)findViewById(R.id.ar_etRegisterID);
        edt_password = (EditText)findViewById(R.id.ar_etRegisterPW);
        edt_tel = (EditText)findViewById(R.id.ar_etRegisterTel);
        edt_account = (EditText)findViewById(R.id.ar_etRegisterAccount);
        btn_register = (Button)findViewById(R.id.ar_btRegister);
        ittRegister = new Intent(RegisterActivity.this, LoginActivity.class);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(edt_name.getText().toString(), edt_id.getText().toString(), edt_password.getText().toString(), edt_tel.getText().toString(), edt_account.getText().toString());
            }
        });
    }

    private void registerUser(String name, String id, String password, String tel, String account) {
        compositeDisposable.add(myAPI.registerUser(name, id, password, tel, account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(RegisterActivity.this, ""+s, Toast.LENGTH_SHORT).show();
                        startActivity(ittRegister);
                    }
                })
        );
    }
}
