package com.kadullin.distributionofrequests.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kadullin.distributionofrequests.R;
import com.kadullin.distributionofrequests.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Task> taskList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    public void updateTask(Task updatedTask) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(updatedTask.getId())) {
                taskList.set(i, updatedTask);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeTask(String taskId) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(taskId)) {
                taskList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }
    public TaskAdapter(List<Task> taskList, OnItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskTitle;
        private final TextView taskCreateTime;
        private final TextView taskStatus;
        private final TextView taskEmployee;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.task);
            taskCreateTime = itemView.findViewById(R.id.task_time);
            taskStatus = itemView.findViewById(R.id.task_status);
            taskEmployee = itemView.findViewById(R.id.name_employee);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                }
            });
        }

        public void bind(Task task) {
            taskTitle.setText(task.getTitle());
            taskCreateTime.setText(task.getCreateTime());
            taskStatus.setText(task.getStatus());

            ConstraintLayout cardView = itemView.findViewById(R.id.cardView);

            if (task.getStatus().equals("ВЫПОЛНЯЕТСЯ...")) {
                cardView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.yellow));
            } else {
                cardView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            }


            taskEmployee.setText(task.getEmployee());

        }


    }
}