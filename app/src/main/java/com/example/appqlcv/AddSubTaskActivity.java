package com.example.appqlcv;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Random;

public class AddSubTaskActivity extends AppCompatActivity {

    private EditText subtaskEditText;
    private ImageButton addFileOrImageButton;
    private Button saveButton;

    private FirebaseDatabase database;
    private Uri fileUri; // Đường dẫn tệp được chọn

    private ActivityResultLauncher<String> fileChooserLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_sub_task);

        // Ánh xạ các view từ layout
        subtaskEditText = findViewById(R.id.subtaskEditText);
        addFileOrImageButton = findViewById(R.id.addFileOrImageButton);
        saveButton = findViewById(R.id.saveButton);
        database = FirebaseDatabase.getInstance();

        // Thiết lập sự kiện click cho nút "Lưu"
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSubTaskToFirebase();
            }
        });

        // Thiết lập sự kiện click cho nút "Thêm file hoặc hình ảnh"
        addFileOrImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Khởi tạo ActivityResultLauncher
        fileChooserLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        fileUri = result;
                        // TODO: Xử lý file được chọn
                    }
                });
    }

    public void addSubTaskToFirebase() {
        String subtaskContent = subtaskEditText.getText().toString().trim();

        // Kiểm tra xem nội dung công việc phụ có giá trị hợp lệ không
        if (TextUtils.isEmpty(subtaskContent)) {
            Toast.makeText(this, "Vui lòng nhập nội dung công việc phụ", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskId = getIntent().getStringExtra("taskId");
        String randomString = generateRandomString(3);

        DatabaseReference subTaskRef = FirebaseDatabase.getInstance().getReference("SubTasks")
                .child(taskId)
                .child(randomString);

        // Kiểm tra xem có tệp tin hoặc hình ảnh được chọn hay không
        if (fileUri != null) {
            // Tạo một tên tệp duy nhất cho tệp tin được tải lên
            String fileName = getOriginalFileName(fileUri);

            // Tạo reference đến Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("attachments").child(fileName);

            // Tải tệp tin lên Firebase Storage
            storageRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Lấy URL của tệp tin đã được tải lên
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String fileUrl = uri.toString();

                            // Tạo một đối tượng SubtaskItem với URL của tệp tin
                            SubtaskItem subTask = new SubtaskItem(taskId, randomString, subtaskContent, fileUrl);

                            // Lưu thông tin công việc phụ vào Firebase Realtime Database
                            subTaskRef.setValue(subTask);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý khi tải tệp tin lên gặp lỗi
                        Toast.makeText(AddSubTaskActivity.this, "Lỗi khi tải tệp tin lên", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Nếu không có tệp tin hoặc hình ảnh được chọn, chỉ lưu nội dung công việc phụ
            SubtaskItem subTask = new SubtaskItem(taskId, randomString, subtaskContent, "");
            subTaskRef.setValue(subTask);
        }

        // Quay trở lại Activity trước đó sau khi thêm công việc phụ thành công
        finish();
    }

    private String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return stringBuilder.toString();
    }
    private void openFileChooser() {
        fileChooserLauncher.launch("*/*");
    }

    private String getOriginalFileName(Uri uri) {
        String originalFileName = null;
        if (uri.getScheme().equals("content")) {
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                    originalFileName = cursor.getString(nameIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (originalFileName == null) {
            originalFileName = uri.getLastPathSegment();
        }
        return originalFileName;
    }
}