package com.example.spectaclepl;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @GET("spectacles")
    Call<List<Spectacle>> getSpectacles();
    @POST("reserver")
    @Headers("Content-Type: application/json")
    Call<Void> enregistrerReservation(@Body Reservation reservation);
    @PUT("spectacles/{matricule}/update-places")
    @Headers("Content-Type: application/json")
    Call<Void> updatePlaces(@Path("matricule") String matricule, @Body Map<String, Integer> body);

    // Authentification
    @POST("auth/register")
    @Headers("Content-Type: application/json")
    Call<AuthResponse> registerUser(@Body User user);

    @POST("auth/login")
    @Headers("Content-Type: application/json")
    Call<AuthResponse> loginUser(@Body LoginRequest loginRequest);

    // Route protégée exemple
    @GET("profile")
    Call<User> getProfile(@Header("Authorization") String token);

    @PUT("api/users/profile")
    Call<User> updateUserProfile(@Header("Authorization") String token, @Body UserUpdateRequest updateRequest);

    // Dans ApiService
    @POST("auth/forgot-password")
    @FormUrlEncoded
    Call<ApiResponse> sendPasswordResetCode(@Field("email") String email);

    @FormUrlEncoded
    @POST("get-current-password")
    Call<PasswordResponse> getCurrentPassword(
            @Field("email") String email
    );
    @POST("auth/verify-reset-code")
    Call<ApiResponse> verifyResetCode(@Body Map<String, String> codeData);

    @POST("auth/reset-password")
    Call<ApiResponse> resetPassword(@Body Map<String, String> passwordData);

}
 class ApiResponse {
     private String code; // Ajouté pour le développement

     // Getters et setters
     public String getCode() {
         return code;
     }

     public void setCode(String code) {
         this.code = code;
     }
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

     public boolean isSuccess() { return success; }
     public String getMessage() { return message; }
}

class PasswordResponse {
    private String password;

    public String getPassword() {
        return password;
    }}

class AuthResponse {
    private String token;

    public String getToken() {
        return token;
    }
}

class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}