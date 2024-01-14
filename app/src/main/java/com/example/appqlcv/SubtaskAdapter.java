package com.example.appqlcv;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.SubtaskViewHolder> {

    private List<SubtaskItem> subtaskList;
    private DatabaseReference databaseReference;

    public SubtaskAdapter(List<SubtaskItem> subtaskList) {
        this.subtaskList = subtaskList;
        databaseReference = FirebaseDatabase.getInstance().getReference("SubTasks");
    }

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sub_task, parent, false);
        return new SubtaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
        SubtaskItem subtaskItem = subtaskList.get(position);
        holder.subtaskTextView.setText(subtaskItem.getDescription());

        String attachmentUrl = subtaskItem.getAttachmentUrl();
        if (!attachmentUrl.isEmpty()) {
            holder.attachmentImageView.setVisibility(View.VISIBLE);
            holder.attachmentTextView.setVisibility(View.VISIBLE);

            if (isImageFile(attachmentUrl)) {
                // Hiển thị hình ảnh từ Firebase Storage
                Glide.with(holder.itemView.getContext())
                        .load(attachmentUrl)
                        .into(holder.attachmentImageView);
            } else {
                holder.attachmentImageView.setVisibility(View.GONE);
                holder.attachmentTextView.setText(getFileNameFromUrl(attachmentUrl));
                holder.itemView.setOnClickListener(v -> {
                    // Xử lý khi người dùng nhấn vào tệp tin
                    downloadFile(holder.itemView.getContext(), attachmentUrl);
                });
            }
        } else {
            // Nếu không có tệp tin đính kèm, ẩn ImageView và TextView
            holder.attachmentImageView.setVisibility(View.GONE);
            holder.attachmentTextView.setVisibility(View.GONE);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Xóa công việc khi nhấn giữ
                removeSubTask(holder.getAdapterPosition());
                Toast.makeText(v.getContext(), "Xóa công việc phụ thành công", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    public void removeSubTask(int position) {
        SubtaskItem subtaskItem = subtaskList.get(position);
        String taskId = subtaskItem.getTaskId();
        String subtaskId = subtaskItem.getSubTaskId();

        // Xóa công việc khỏi danh sách
        subtaskList.remove(position);
        notifyItemRemoved(position);

        // Xóa dữ liệu tương ứng trên Firebase Realtime Database
        databaseReference.child(taskId)
                .child(subtaskId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Xóa thành công
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xóa thất bại
                    }
                });
    }

    @Override
    public int getItemCount() {
        return subtaskList.size();
    }

    static class SubtaskViewHolder extends RecyclerView.ViewHolder {
        TextView subtaskTextView;
        ImageView attachmentImageView;
        TextView attachmentTextView;

        SubtaskViewHolder(@NonNull View itemView) {
            super(itemView);
            subtaskTextView = itemView.findViewById(R.id.subtaskTitleTextView);
            attachmentImageView = itemView.findViewById(R.id.attachmentImageView);
            attachmentTextView = itemView.findViewById(R.id.attachmentTextView);
        }
    }

    private boolean isImageFile(String url) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        return mimeType != null && mimeType.startsWith("image");
    }

    private String getFileNameFromUrl(String url) {
        String decodedUrl = Uri.decode(url);
        String[] segments = decodedUrl.split("/");
        String lastSegment = segments[segments.length - 1];
        String fileName = lastSegment.split("\\?")[0];
        return fileName;
    }

    private void downloadFile(Context context, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Downloading File");
        request.setDescription("Please wait...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getFileNameFromUrl(url));

        downloadManager.enqueue(request);

        Toast.makeText(context, "Downloading File...", Toast.LENGTH_SHORT).show();
    }
}