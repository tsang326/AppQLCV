package com.example.appqlcv;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference chatReference;
    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messageList;
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ActivityResultLauncher<String> fileChooserLauncher;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatReference = FirebaseDatabase.getInstance().getReference().child("chat");

        messageList = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(this, messageList);

        RecyclerView messageRecyclerView = findViewById(R.id.messageRecyclerView);

        // Tạo LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setAdapter(messageAdapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        messageRecyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));

        TextView chatTitleTextView = findViewById(R.id.chatTitleTextView);
        // Nhận title từ Intent
        String title = getIntent().getStringExtra("title");
        String taskId = getIntent().getStringExtra("taskId");

        // Thiết lập title trong TextView
        chatTitleTextView.setText("Chat for Task: " + title);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText messageEditText = findViewById(R.id.messageEditText);
                String content = messageEditText.getText().toString().trim();
                if (!content.isEmpty()) {
                    sendMessage(content);
                    messageEditText.setText("");
                }
            }
        });

        ImageView attachmentButton = findViewById(R.id.attachmentButton);
        attachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        fileChooserLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        fileUri = result;
                        uploadFile(fileUri);
                        // TODO: Xử lý file được chọn
                    }
                });

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String taskId1 = dataSnapshot.getKey(); // Lấy taskId của thông điệp
                    if (taskId1.equals(taskId)) { // Kiểm tra đúng taskId
                        for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                            String sender = chatSnapshot.child("sender").getValue(String.class);
                            String content = chatSnapshot.child("content").getValue(String.class);
                            String timestamp = chatSnapshot.child("timestamp").getValue(String.class);
                            ChatMessage chatMessage = new ChatMessage(sender, content, timestamp);
                            messageList.add(chatMessage);
                        }
                    }
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần thiết
            }
        });
    }

    private void openFileChooser() {
        fileChooserLauncher.launch("*/*");
    }

    private void uploadFile(Uri fileUri) {
        if (fileUri != null) {
            String fileName = getOriginalFileName(fileUri);
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("attachments").child(fileName);

            storageRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String fileUrl = uri.toString();
                            sendMessageWithFile(fileUrl);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(ChatActivity.this, "Lỗi khi lấy URL của tệp tin", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, "Lỗi khi tải tệp tin lên", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void sendMessage(String content) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("fullName").getValue(String.class);
                        if (username != null) {
                            long timestamp = System.currentTimeMillis();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                            String formattedTime = dateFormat.format(timestamp);
                            String taskId = getIntent().getStringExtra("taskId");
                            ChatMessage message = new ChatMessage(username, content, formattedTime);
                            chatReference.child(taskId).push().setValue(message);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi nếu cần thiết
                }
            });
        }
    }

    private void sendMessageWithFile(String fileUrl) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("fullName").getValue(String.class);
                        if (username != null) {
                            long timestamp = System.currentTimeMillis();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                            String formattedTime = dateFormat.format(timestamp);
                            String file = getFileNameFromUrl(fileUrl);
                            String taskId = getIntent().getStringExtra("taskId");
                            ChatMessage message = new ChatMessage(username, file, formattedTime);
                            chatReference.child(taskId).push().setValue(message);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi nếu cần thiết
                }
            });
        }
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

    private String getFileNameFromUrl(String url) {
        String decodedUrl = Uri.decode(url);
        String[] segments = decodedUrl.split("/");
        String lastSegment = segments[segments.length - 1];
        String fileName = lastSegment.split("\\?")[0];
        return fileName;
    }
}