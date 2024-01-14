package com.example.appqlcv;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class AddTaskActivity extends AppCompatActivity {
    private EditText editTextTitle;
    private EditText editTextDescription;
    private Button buttonStartDate;
    private Button buttonEndDate;
    private Button buttonAddTask;

    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private SimpleDateFormat dateFormat;
    private FirebaseDatabase database;
    private AtomicInteger taskIdCounter = new AtomicInteger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);

        // Ánh xạ các view từ layout
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEndDate = findViewById(R.id.buttonEndDate);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        database = FirebaseDatabase.getInstance();

        // Khởi tạo các biến và định dạng ngày tháng
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Thiết lập sự kiện click cho nút Start Date
        buttonStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartDatePickerDialog();
            }
        });

        // Thiết lập sự kiện click cho nút End Date
        buttonEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndDatePickerDialog();
            }
        });

        // Thiết lập sự kiện click cho nút Add Task
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTaskToFirebase();
            }
        });
    }

    public void showStartDatePickerDialog() {
        DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                startDateCalendar.set(Calendar.YEAR, year);
                startDateCalendar.set(Calendar.MONTH, monthOfYear);
                startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Hiển thị ngày bắt đầu đã chọn trên button Start Date
                buttonStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
            }
        };

        // Tạo DatePickerDialog cho ngày bắt đầu
        DatePickerDialog startDatePickerDialog = new DatePickerDialog(
                this,
                startDateListener,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // Hiển thị DatePickerDialog
        startDatePickerDialog.show();
    }

    public void showEndDatePickerDialog() {
        DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                endDateCalendar.set(Calendar.YEAR, year);
                endDateCalendar.set(Calendar.MONTH, monthOfYear);
                endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Hiển thị ngày kết thúc đã chọn trên button End Date
                buttonEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
            }
        };

        // Tạo DatePickerDialog cho ngày kết thúc
        DatePickerDialog endDatePickerDialog = new DatePickerDialog(
                this,
                endDateListener,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // Hiển thị DatePickerDialog
        endDatePickerDialog.show();
    }

    public void addTaskToFirebase() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Kiểm tra xem title, description, startDate và endDate có giá trị hợp lệ không
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDateCalendar.after(endDateCalendar)) {
            Toast.makeText(this, "Start date must be before end date", Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar currentDate = Calendar.getInstance();
        // Trong thời gian giữa ngày bắt đầu và ngày kết thúc
        //long timeDifference = endDateCalendar.getTimeInMillis() - startDateCalendar.getTimeInMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String deadline = dateFormat.format(endDateCalendar.getTime());
        String randomString = generateRandomString(5);
        Task task = new Task(randomString, title, description, deadline);

        // Lấy ID người dùng hiện tại
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUserId = currentUser.getUid();

        DatabaseReference ref = database.getReference("Tasks").child(randomString);
        ref.setValue(task);
        ref.child("userIds").child(currentUserId).setValue(true);

        // Quay trở lại HomeActivity sau khi thêm công việc thành công
        Intent intent = new Intent(AddTaskActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Đóng AddTaskActivity để không quay lại nó khi nhấn nút Back
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
}