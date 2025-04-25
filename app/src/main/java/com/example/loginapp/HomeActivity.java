package com.example.loginapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.loginapp.api.ApiService;
import com.example.loginapp.models.ImageUploadResponse;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {
    private ImageView imagePreview;
    private MaterialButton selectImageButton;
    private MaterialButton uploadButton;
    private MaterialButton logoutButton;
    private View uploadProgressBar;
    private Uri selectedImageUri;
    private ApiService apiService;
    private static final String BASE_URL = "https://dev-otp.yacorpt.com/";
    private static final String TOKEN = "your-secret-token-here";

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    displaySelectedImage();
                    uploadButton.setEnabled(true);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupRetrofit();
        setupButtons();
    }

    private void initViews() {
        imagePreview = findViewById(R.id.imagePreview);
        selectImageButton = findViewById(R.id.selectImageButton);
        uploadButton = findViewById(R.id.uploadButton);
        logoutButton = findViewById(R.id.logoutButton);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void setupButtons() {
        selectImageButton.setOnClickListener(v -> openImagePicker());
        uploadButton.setOnClickListener(v -> uploadImage());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImage.launch(intent);
    }

    private void displaySelectedImage() {
        Glide.with(this)
                .load(selectedImageUri)
                .centerCrop()
                .into(imagePreview);
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            showMessage("Please select an image first");
            return;
        }

        setLoading(true);

        try {
            File imageFile = createTempFileFromUri(selectedImageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

            Call<ImageUploadResponse> call = apiService.uploadImage("otp-image", TOKEN, imagePart);
            call.enqueue(new Callback<ImageUploadResponse>() {
                @Override
                public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        handleUploadResponse(response.body());
                    } else {
                        showMessage("Upload failed. Please try again.");
                    }
                }

                @Override
                public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                    setLoading(false);
                    showMessage("Network error. Please check your connection.");
                }
            });
        } catch (Exception e) {
            setLoading(false);
            showMessage("Error preparing image for upload");
        }
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
        FileOutputStream fos = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }

        fos.close();
        inputStream.close();
        return tempFile;
    }

    private void handleUploadResponse(ImageUploadResponse response) {
        if (response.isSuccess()) {
            showMessage("Image uploaded successfully");
            // Reset UI
            selectedImageUri = null;
            imagePreview.setImageResource(android.R.color.transparent);
            uploadButton.setEnabled(false);
        } else {
            showMessage(response.getMessage());
        }
    }

    private void logout() {
        // Navigate back to login screen
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setLoading(boolean isLoading) {
        uploadButton.setEnabled(!isLoading);
        selectImageButton.setEnabled(!isLoading);
        uploadProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
