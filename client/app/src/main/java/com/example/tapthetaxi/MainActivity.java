package com.example.tapthetaxi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity
{
    private BottomNavigationView bottomNavigationView; // 바텀 네비게이션 뷰
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Fragment_T frag1;
    private Fragment_A frag2;
    private Fragment_P frag3;

    Intent ittLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ittLogin = new Intent(MainActivity.this, LoginActivity.class);

        String getSession = sessionCheck();
        if(getSession.length() == 0){
            startActivity(ittLogin);
        }

        bottomNavigationView = findViewById(R.id.am_bnMainBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.bar_tap:
                        setFrag(0);
                        break;
                    case R.id.bar_account:
                        setFrag(1);
                        break;
                    case R.id.bar_profile:
                        setFrag(2);
                        break;
                }
                return true;
            }
        });

        frag1=new Fragment_T();
        frag2=new Fragment_A();
        frag3=new Fragment_P();
        setFrag(0); // 첫 프래그먼트 화면 지정
    }

    // 프레그먼트 교체
    private void setFrag(int n)
    {
        fm = getSupportFragmentManager();
        ft= fm.beginTransaction();
        switch (n)
        {
            case 0:
                ft.replace(R.id.am_flList,frag1);
                ft.commit();
                break;

            case 1:
                ft.replace(R.id.am_flList,frag2);
                ft.commit();
                break;

            case 2:
                ft.replace(R.id.am_flList,frag3);
                ft.commit();
                break;
        }
    }

    private String sessionCheck(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String s = pref.getString("session", "");
        return s;
    }
}
