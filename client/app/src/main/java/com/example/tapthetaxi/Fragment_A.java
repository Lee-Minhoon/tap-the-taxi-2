package com.example.tapthetaxi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class Fragment_A extends Fragment
{

    private View view;

    INodeJS myAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    TextView txv_num, txv_balance;
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
        view = inflater.inflate(R.layout.fragment_a,container,false);

        Retrofit retrofit = RetrofitClient.getInstance();
        myAPI = retrofit.create(INodeJS.class);

        txv_num = (TextView)view.findViewById(R.id.aa_tvNum);
        txv_balance = (TextView)view.findViewById(R.id.aa_tvBalance);
        itt_login = new Intent(getActivity(), LoginActivity.class);

        // 세션체크(로그인 중인지, 로그인 중이 아니면 login 화면으로 팅굼)
        String getSession = sessionCheck();
        if(getSession.length() == 0){
            startActivity(itt_login);
        }

        compositeDisposable.add(myAPI.account(getSession)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if(s.contains("userNo")){
                            JSONObject jsonObject = new JSONObject(s);
                            // 계좌 번호와 잔액 불러옴
                            String num = jsonObject.getString("accountNum");
                            String balance = jsonObject.getString("accountBalance");

                            // setText
                            txv_num.setText("계좌번호 : " + num);
                            txv_balance.setText("계좌잔액 : " + balance);
                        }
                        else{
                            // 조회할 수 없음
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
}