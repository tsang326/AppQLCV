package com.example.appqlcv;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView searchResultsRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseReference taskReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_view);

        // Ánh xạ các phần tử trong layout
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);

        // Thiết lập LinearLayoutManager cho RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        searchResultsRecyclerView.setLayoutManager(layoutManager);

        // Khởi tạo và thiết lập adapter cho RecyclerView
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        searchResultsRecyclerView.setAdapter(taskAdapter);

        // Xử lý sự kiện nhấn nút tìm kiếm
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = searchEditText.getText().toString();
                performSearch(searchText);
            }
        });
    }

    private void performSearch(String keyword) {
        // Xóa danh sách công việc hiện tại
        taskList.clear();
        // Lấy ID người dùng hiện tại
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        taskReference = FirebaseDatabase.getInstance().getReference().child("Tasks");
        // Lắng nghe sự thay đổi dữ liệu của công việc
        taskReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Lặp qua các nút con trong dataSnapshot để lấy thông tin công việc
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot userIdsSnapshot = taskSnapshot.child("userIds");
                    if (userIdsSnapshot.hasChild(currentUserId) && userIdsSnapshot.child(currentUserId).getValue(Boolean.class)) {
                        // Lấy giá trị của các thuộc tính công việc
                        String taskId = taskSnapshot.child("id").getValue(String.class);
                        String taskTitle = taskSnapshot.child("title").getValue(String.class);
                        String taskDescription = taskSnapshot.child("description").getValue(String.class);
                        String taskDeadline = taskSnapshot.child("deadline").getValue(String.class);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date deadlineDate = null;
                        try {
                            deadlineDate = dateFormat.parse(taskDeadline);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        Calendar deadlineCalendar = Calendar.getInstance();
                        deadlineCalendar.setTime(deadlineDate);

                        // Tính toán số ngày còn lại từ ngày hiện tại đến ngày hết hạn
                        Calendar currentDate = Calendar.getInstance();
                        long millisecondsDifference = deadlineCalendar.getTimeInMillis() - currentDate.getTimeInMillis();
                        long daysDifference = millisecondsDifference / (24 * 60 * 60 * 1000) + 1;
                        String deadline = daysDifference + " days";

                        // Kiểm tra nếu tiêu đề hoặc mô tả công việc chứa keyword được tìm kiếm
                        if (taskTitle.toLowerCase().contains(keyword.toLowerCase()) ||
                                taskDescription.toLowerCase().contains(keyword.toLowerCase())) {
                            // Tạo đối tượng công việc và thêm vào danh sách
                            Task task = new Task(taskId, taskTitle, taskDescription, deadline);
                            taskList.add(task);
                        }
                    }
                }

                // Kiểm tra nếu không có công việc phù hợp với từ khóa tìm kiếm
                if (taskList.isEmpty()) {
                    Toast.makeText(SearchActivity.this, "No matching tasks found.", Toast.LENGTH_SHORT).show();
                }

                // Cập nhật adapter để hiển thị lại danh sách công việc
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình đọc dữ liệu từ Firebase
                Toast.makeText(SearchActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}