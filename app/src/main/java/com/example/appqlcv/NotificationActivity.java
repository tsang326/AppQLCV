package com.example.appqlcv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

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

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.rcv_notification_new);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Gọi getNotificationList() và truyền vào một đối tượng NotificationCallback
        getNotificationList(new NotificationCallback() {
            @Override
            public void onNotificationListReceived(List<Notification> notifications) {
                adapter = new NotificationAdapter(notifications);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private void getNotificationList(NotificationCallback callback) {
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference().child("Tasks");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Notification> notifications = new ArrayList<>();
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot userIdsSnapshot = taskSnapshot.child("userIds");
                    if (userIdsSnapshot.hasChild(currentUserId) && userIdsSnapshot.child(currentUserId).getValue(Boolean.class)) {
                        String title1 = taskSnapshot.child("title").getValue(String.class);
                        String description = taskSnapshot.child("description").getValue(String.class);
                        // Đọc các thông tin khác của công việc tương tự như trên

                        // Kiểm tra xem công việc có thuộc tính liên quan đến thông báo không
                        if (title1 != null) {
                            // Tạo đối tượng Notification từ dữ liệu công việc
                            String title = "Công việc " + title1 + " mới được tạo";
                            String content = "Description: " + description;
                            Notification notification = new Notification(title, content);
                            notifications.add(notification);

                            // Kiểm tra deadline của công việc
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
                            if (daysDifference < 5) {
                                // Tạo thông báo cho công việc sắp hết hạn
                                String deadline = "Còn " + daysDifference + " days";
                                String deadlineTitle = "Công việc " + title1 + " sắp hết hạn";
                                String deadlineContent = "Deadline: " + deadline;
                                Notification deadlineNotification = new Notification(deadlineTitle, deadlineContent);
                                notifications.add(deadlineNotification);
                            }
                        }
                    }
                }
                // Gọi phương thức onNotificationReceived() của NotificationCallback
                callback.onNotificationListReceived(notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu cần thiết
            }
        });
    }

    public interface NotificationCallback {
        void onNotificationListReceived(List<Notification> notifications);
    }
}