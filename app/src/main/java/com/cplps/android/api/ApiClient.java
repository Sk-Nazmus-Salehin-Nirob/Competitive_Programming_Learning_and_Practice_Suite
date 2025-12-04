package com.cplps.android.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String CODEFORCES_BASE_URL = "https://codeforces.com/api/";
    private static Retrofit retrofit = null;
    private static CodeforcesAPI codeforcesAPI = null;

    public static CodeforcesAPI getCodeforcesAPI() {
        if (codeforcesAPI == null) {
            codeforcesAPI = getRetrofitInstance().create(CodeforcesAPI.java);
        }
        return codeforcesAPI;
    }

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Logging interceptor for debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(CODEFORCES_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
