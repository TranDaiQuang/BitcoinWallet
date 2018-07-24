package wallet.bitcoin.bitcoinwallet.rest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wallet.bitcoin.bitcoinwallet.rest.service.ApiService;
import wallet.bitcoin.bitcoinwallet.rest.service.AuthService;

public class RestClient {

    private static final String BASE_URL = A.getApi();

    private AuthService authService;
    private ApiService apiService;

    private Retrofit retrofit;

    public RestClient() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
                                      @Override
                                      public Response intercept(Interceptor.Chain chain) throws IOException {
                                          Request original = chain.request();

                                          Request request = original.newBuilder()
                                                  .header("Authorization", A.getAuthString())
                                                  .header("Content-Type", "application/json")
                                                  .method(original.method(), original.body())
                                                  .build();

                                          return chain.proceed(request);
                                      }
                                  });

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);
        apiService = retrofit.create(ApiService.class);

    }

    public AuthService getAuthService() {
        return authService;
    }

    public ApiService getApiService() {
        return apiService;
    }

}
