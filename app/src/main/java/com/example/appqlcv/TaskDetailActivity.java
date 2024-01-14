package com.example.appqlcv;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView titleTextView;
    private ImageButton menuButton;
    private ImageButton addSubtaskButton;
    private RecyclerView subtaskRecyclerView;
    private SubtaskAdapter subtaskAdapter;
    private List<SubtaskItem> subtaskList;
    private DatabaseReference subtaskReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Ánh xạ các thành phần giao diện từ layout XML
        backButton = findViewById(R.id.backButton);
        titleTextView = findViewById(R.id.titleTextView);
        menuButton = findViewById(R.id.menuButton);
        addSubtaskButton = findViewById(R.id.addSubtaskButton);
        subtaskRecyclerView = findViewById(R.id.subtaskRecyclerView);

        // Nhận title từ intent
        String taskId = getIntent().getStringExtra("taskId");
        String title = getIntent().getStringExtra("taskTitle");

        // Thiết lập title vào titleTextView
        titleTextView.setText(title);

        // Khởi tạo danh sách công việc phụ và Adapter
        subtaskList = new ArrayList<>();
        subtaskAdapter = new SubtaskAdapter(subtaskList);

        // Thiết lập Adapter cho RecyclerView
        subtaskRecyclerView.setAdapter(subtaskAdapter);
        subtaskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Tạo một SpacingItemDecoration và thiết lập nó cho RecyclerView
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        subtaskRecyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));

        subtaskReference = FirebaseDatabase.getInstance().getReference().child("SubTasks");

        subtaskReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Xóa danh sách công việc hiện tại
                subtaskList.clear();
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị của các thuộc tính công việc
                    String staskId = taskSnapshot.getKey();
                    if (staskId.equals(taskId)) {
                        for (DataSnapshot subtaskSnapshot : taskSnapshot.getChildren()) {
                            String subtaskId = subtaskSnapshot.child("subTaskId").getValue(String.class);
                            String subtaskDescription = subtaskSnapshot.child("description").getValue(String.class);
                            String attachmentUrl = subtaskSnapshot.child("attachmentUrl").getValue(String.class);
                            // Tạo đối tượng công việc và thêm vào danh sách
                            SubtaskItem subtaskItem = new SubtaskItem(taskId, subtaskId, subtaskDescription, attachmentUrl);
                            subtaskList.add(subtaskItem);
                        }
                    }
                }

                // Cập nhật adapter để hiển thị lại danh sách công việc
                subtaskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình đọc dữ liệu từ Firebase
                Toast.makeText(TaskDetailActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Thiết lập sự kiện cho nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý logic khi người dùng nhấn nút quay lại
                finish();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Dialog
                Dialog dialog = new Dialog(TaskDetailActivity.this);
                dialog.setContentView(R.layout.dialog_menu);

                // Ánh xạ ListView từ layout Dialog
                ListView menuListView = dialog.findViewById(R.id.menuListView);

                // Tạo danh sách các tùy chọn
                List<String> menuOptions = new ArrayList<>();
                menuOptions.add("Thêm thành viên");
                menuOptions.add("Chat");
                // ...

                // Tạo và thiết lập Adapter cho ListView
                ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(TaskDetailActivity.this, android.R.layout.simple_list_item_1, menuOptions);
                menuListView.setAdapter(menuAdapter);

                // Xử lý sự kiện khi người dùng chọn một tùy chọn
                menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedOption = menuOptions.get(position);
                        // Xử lý logic khi người dùng chọn một tùy chọn
                        if (selectedOption.equals("Thêm thành viên")) {
                            showAddMemberDialog(taskId);
                        } else if (selectedOption.equals("Chat")) {
                            Intent chatIntent = new Intent(TaskDetailActivity.this, ChatActivity.class);
                            chatIntent.putExtra("title", title);
                            chatIntent.putExtra("taskId", taskId);
                            startActivity(chatIntent);
                        }
                        dialog.dismiss(); // Đóng Dialog sau khi xử lý
                    }
                });

                // Hiển thị Dialog
                dialog.show();
            }
        });

        // Thiết lập sự kiện cho nút thêm công việc phụ
        addSubtaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskDetailActivity.this, AddSubTaskActivity.class);
                intent.putExtra("taskId", taskId);
                startActivity(intent);
            }
        });
        try {
            // Your existing code for TaskDetailActivity
        } catch (Exception e) {
            Log.e("TaskDetailActivity", "Error in TaskDetailActivity", e);
        }

    }
    private void showAddMemberDialog(String taskId) {
        Dialog addMemberDialog = new Dialog(TaskDetailActivity.this);
        addMemberDialog.setContentView(R.layout.add_member);

        // Ánh xạ các thành phần giao diện từ layout Dialog
        EditText nameEditText = addMemberDialog.findViewById(R.id.nameEditText);
        EditText emailEditText = addMemberDialog.findViewById(R.id.emailEditText);
        Button addButton = addMemberDialog.findViewById(R.id.addButton);

        // Xử lý sự kiện khi người dùng nhấn nút "Thêm"
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy thông tin thành viên từ trường nhập liệu
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();

                // Kiểm tra xem người dùng có tồn tại trong cơ sở dữ liệu không
                DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
                Query query = null;

                if (!name.isEmpty()) {
                    query = usersReference.orderByChild("fullName").equalTo(name);
                } else if (!email.isEmpty()) {
                    query = usersReference.orderByChild("email").equalTo(email);
                } else {
                    // Không có thông tin để kiểm tra
                    Toast.makeText(TaskDetailActivity.this, "Vui lòng nhập tên hoặc email", Toast.LENGTH_SHORT).show();
                    return;
                }

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean userExists = false;
                        String userId = "";

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            userExists = true;
                            userId = snapshot.getKey();
                            break;
                        }

                        if (userExists) {
                            // Thêm userId vào userIds của công việc
                            DatabaseReference taskReference = FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId).child("userIds");
                            taskReference.child(userId).setValue(true);
                        } else {
                            // Người dùng không tồn tại
                            Toast.makeText(TaskDetailActivity.this, "Người dùng không tồn tại", Toast.LENGTH_SHORT).show();
                        }

                        // Đóng hộp thoại sau khi hoàn thành
                        addMemberDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Xử lý lỗi nếu cần thiết
                    }
                });
            }
        });

        // Hiển thị Dialog
        addMemberDialog.show();
    }
}