package com.example.tapthetaxi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.tapthetaxi.Retrofit.INodeJS;
import com.example.tapthetaxi.Retrofit.RetrofitClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class Fragment_T extends Fragment
{

    private View view;
    private ImageView btn_tap;
    private Intent itt_login, itt_chat;
    private String getSession;
    private Boolean getRoom;

    INodeJS myAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

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
        view = inflater.inflate(R.layout.fragment_t,container,false);

        Retrofit retrofit = RetrofitClient.getInstance();
        myAPI = retrofit.create(INodeJS.class);

        // 객체 받아오는거
        btn_tap = (ImageView) view.findViewById(R.id.at_tap);
        itt_login = new Intent(getActivity(), LoginActivity.class);
        itt_chat = new Intent(getActivity(), ChatActivity.class);

        // 세션 체크
        getSession = sessionCheck();
        if(getSession.length() == 0){
            startActivity(itt_login);
        }
        // 지금 접속중인 방 (세션으로 유지중)
        getRoom = sessionRoom();
        if(getRoom == true){
            startActivity(itt_chat);
        }
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        btn_tap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 세션 체크
                getSession = sessionCheck();
                if(getSession.length() == 0){
                    startActivity(itt_login);
                }
                // GPStracker 객체 받아옴
                GPStracker g = new GPStracker(getActivity().getApplicationContext());
                // 위치정보 받아와서
                Location l = g.getLocation();
                // 위치정보 확인되면
                if(l != null){
                    // 위도 경도 받아옴
                    double lat = l.getLatitude();
                    double lon = l.getLongitude();
                    // 위도 경도 띄워줌 화면에
                    Toast.makeText(getActivity().getApplicationContext(),"위도 : " + lat + "\n 경도 : " + lon, Toast.LENGTH_LONG).show();
                    // 룸 함수 실행
                    room(getSession, lat, lon);
                    // 챗 화면으로 넘어감
                    startActivity(itt_chat);
                    // 방 세션 저장
                    saveSession(true);
                }
            }
        });

        return view;
    }

    private void room(String id, Double x, Double y) {
        compositeDisposable.add(myAPI.room(id, x, y)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(getActivity(), ""+s, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private String sessionCheck(){
        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        String s = pref.getString("session", "");
        return s;
    }

    private Boolean sessionRoom(){
        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        Boolean b = pref.getBoolean("room", false);
        return b;
    }

    private void saveSession(Boolean b){
        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("room", b);
        editor.commit();
    }
}