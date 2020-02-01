package com.example.tapthetaxi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tapthetaxi.Retrofit.INodeJS;
import com.example.tapthetaxi.Retrofit.RetrofitClient;

import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class Fragment_P extends Fragment
{

    private View view;

    INodeJS myAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    TextView txv_name, txv_id, txv_tel, txv_account;
    Button btn_logout;
    Intent itt_login;

    @Override
    public void onStop(){
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy(){
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_p,container,false);

        Retrofit retrofit = RetrofitClient.getInstance();
        myAPI = retrofit.create(INodeJS.class);

        txv_name = (TextView)view.findViewById(R.id.aa_tvName);
        txv_id = (TextView)view.findViewById(R.id.aa_tvID);
        txv_tel = (TextView)view.findViewById(R.id.aa_tvTel);
        txv_account = (TextView)view.findViewById(R.id.aa_tvAccount);
        btn_logout = (Button)view.findViewById(R.id.aa_btLogout);
        itt_login = new Intent(getActivity(), LoginActivity.class);

        String getSession = sessionCheck();
        if(getSession.length() == 0){
            startActivity(itt_login);
        }

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSession();
                startActivity(itt_login);
            }
        });

        compositeDisposable.add(myAPI.profile(getSession)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if(s.contains("userID")){
                            JSONObject jsonObject = new JSONObject(s);
                            String name = jsonObject.getString("userName");
                            String id = jsonObject.getString("userID");
                            String tel = jsonObject.getString("userTel");

                            txv_name.setText("이름 : " + name);
                            txv_id.setText("아이디 : " + id);
                            txv_tel.setText("전화번호 : " + tel);
                        }
                        else{
                            Toast.makeText(getActivity(), ""+s, Toast.LENGTH_SHORT).show();
                            startActivity(itt_login);
                        }
                    }
                })
        );

        compositeDisposable.add(myAPI.account(getSession)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if(s.contains("userNo")){
                            JSONObject jsonObject = new JSONObject(s);
                            String account = jsonObject.getString("accountNum");

                            txv_account.setText("계좌번호 : " + account);
                        }
                        else{
                            Toast.makeText(getActivity(), ""+s, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        );
        return view;
    }

    private String sessionCheck(){
        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        String s = pref.getString("session", "");
        return s;
    }

    private void removeSession(){
        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("session");
        editor.commit();
    }
}