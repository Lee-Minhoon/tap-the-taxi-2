package com.example.tapthetaxi.Retrofit;


import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface INodeJS {
    @POST("register")
    @FormUrlEncoded
    Observable<String> registerUser(@Field("name") String name,
                                    @Field("id") String id,
                                    @Field("password") String password,
                                    @Field("tel") String tel,
                                    @Field("account") String account);

    @POST("login")
    @FormUrlEncoded
    Observable<String> loginUser(@Field("id") String id,
                                 @Field("password") String password);

    @POST("profile")
    @FormUrlEncoded
    Observable<String> profile(@Field("id") String id);

    @POST("account")
    @FormUrlEncoded
    Observable<String> account(@Field("id") String id);

    @POST("room")
    @FormUrlEncoded
    Observable<String> room(@Field("id") String id,
                            @Field("x") Double x,
                            @Field("y") Double y);

    @POST("roomExit")
    @FormUrlEncoded
    Observable<String> roomExit(@Field("id") String id);
}
