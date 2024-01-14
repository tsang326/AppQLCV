package com.example.appqlcv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private Handler handler;
    private static final int NOTIFICATION_CHECK_INTERVAL = 24 * 60 * 60 * 1000; // 24 giờ

    private RecyclerView taskRecyclerView;
    private List<Task> taskList;
    private TaskAdapter taskAdapter;

    private ImageButton menuButton;
    private ImageButton searchButton;
    private ImageButton notificationButton;
    private ImageButton addTaskButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Khai báo biến Firebase
    private DatabaseReference userReference;
    private DatabaseReference taskReference;
    private static final int MENU_HOME = R.id.menu_home;
    private static final int MENU_SETTINGS = R.id.menu_settings;
    private static final int MENU_HELP = R.id.menu_help;
    private static final int MENU_ABOUT = R.id.menu_about;
    private static final int MENU_LOGOUT = R.id.menu_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        // Ánh xạ các thành phần giao diện
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        menuButton = findViewById(R.id.menuButton);
        searchButton = findViewById(R.id.searchButton);
        notificationButton = findViewById(R.id.notificationButton);
        addTaskButton = findViewById(R.id.addTaskButton);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        ImageView profileImage = headerView.findViewById(R.id.profileImage);
        TextView profileName = headerView.findViewById(R.id.profileName);
        TextView profileEmail = headerView.findViewById(R.id.profileEmail);
        handler = new Handler(Looper.getMainLooper());

        // Khởi tạo danh sách công việc và adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);

        // Thiết lập RecyclerView
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(taskAdapter);
        // Lấy ID người dùng hiện tại
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        // Khởi tạo biến Firebase
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        taskReference = FirebaseDatabase.getInstance().getReference().child("Tasks");

        // Lắng nghe sự thay đổi dữ liệu người dùng
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Lặp qua các nút con trong dataSnapshot để lấy thông tin người dùng
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    UserData userData = userSnapshot.getValue(UserData.class);
                    if (userData != null) {
                        String userId = userSnapshot.getKey();
                        if (userId.equals(currentUserId)) {
                            // Cập nhật dữ liệu trong navigation_header.xml
                            profileName.setText(userData.getFullName());
                            profileEmail.setText(userData.getEmail());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình đọc dữ liệu từ Firebase
                Toast.makeText(HomeActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        taskReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Xóa danh sách công việc hiện tại
                taskList.clear();

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
                        // Tạo đối tượng công việc và thêm vào danh sách

                        Task task = new Task(taskId, taskTitle, taskDescription, deadline);
                        taskList.add(task);

                        // Kiểm tra nếu deadline là 1 ngày, gửi thông báo
                        if (daysDifference <= 5) {
                            sendNotificationDeadline(task.getTitle(), task.getDescription());
                        }
                    }
                }

                // Cập nhật adapter để hiển thị lại danh sách công việc
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình đọc dữ liệu từ Firebase
                Toast.makeText(HomeActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        taskReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                handleTaskChanged(snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // Xử lý sự kiện khi nhấn nút Menu
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Xử lý sự kiện khi nhấn nút Search
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện khi nhấn nút Notification
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện khi nhấn nút Add Task
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
                startActivity(intent);

            }
        });

// Xử lý sự kiện khi chọn mục trong Navigation Drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Đóng Navigation Drawer sau khi chọn mục
                drawerLayout.closeDrawer(GravityCompat.START);

                // Xử lý sự kiện tương ứng với từng mục đã chọn
                int selectedItemId = menuItem.getItemId();
                if (selectedItemId == MENU_HOME) {
                    // TODO: Xử lý khi chọn mục Home
                } else if (selectedItemId == MENU_SETTINGS) {
                    // TODO: Xử lý khi chọn mục Settings
                } else if (selectedItemId == MENU_HELP) {
                    // TODO: Xử lý khi chọn mục Help
                } else if (selectedItemId == MENU_ABOUT) {
                    // TODO: Xử lý khi chọn mục About
                } else if (selectedItemId == MENU_LOGOUT) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("Đăng xuất")
                            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                            .setCancelable(true)
                            .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(HomeActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                } else {
                    // Nếu không có mục nào phù hợp, không làm gì
                }

                return true;
            }
        });
    }

    private void handleTaskChanged(DataSnapshot snapshot) {
        // Extract task details from the snapshot
        String taskId = snapshot.child("id").getValue(String.class);
        String taskTitle = snapshot.child("title").getValue(String.class);
        String taskDescription = snapshot.child("description").getValue(String.class);

        // Check if the task details are not null before sending a notification
        if (taskId != null && taskTitle != null && taskDescription != null) {
            sendNotificationForDatabaseChange(taskTitle, taskDescription);
        }
    }


    public void sendNotificationDeadline(String taskTitle, String taskDescription) {
        // Tùy chỉnh nội dung thông báo dựa trên thay đổi trong Realtime Database
        String notificationTitle = "Công việc sắp tới hạn";
        String notificationBody = "Công Việc: " + taskTitle + " \tMô Tả: " + taskDescription;

        int notificationId = generateUniqueNotificationId();

        NotificationUtils.sendNotification(this, notificationTitle, notificationBody, notificationId);
    }
    public void sendNotificationForDatabaseChange(String taskTitle, String taskDescription) {
        // Tùy chỉnh nội dung thông báo dựa trên thay đổi trong Realtime Database
        String notificationTitle = "Công việc mới";
        String notificationBody = "Công Việc: " + taskTitle + " \tMô Tả: " + taskDescription;

        int notificationId = generateUniqueNotificationId();

        NotificationUtils.sendNotification(this, notificationTitle, notificationBody, notificationId);
    }

    private int generateUniqueNotificationId() {
        // Sử dụng thời gian hiện tại tính bằng mili giây làm định danh duy nhất cho thông báo
        return (int) System.currentTimeMillis();
    }

}