package com.kadullin.distributionofrequests.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kadullin.distributionofrequests.R;
import com.kadullin.distributionofrequests.activities.MyTasksActivity;
import com.kadullin.distributionofrequests.activities.TaskListActivity;
import com.kadullin.distributionofrequests.adapters.TaskAdapter;
import com.kadullin.distributionofrequests.models.Task;
import com.kadullin.distributionofrequests.utilities.Constants;
import com.kadullin.distributionofrequests.utilities.PreferenceManager;

import java.util.Objects;

public class TaskFragment extends Fragment {
    private PreferenceManager preferenceManager;
    private ProgressBar progressBar;
    private DocumentReference taskRef;
    private TextView taskTitle, taskDesc, taskCreateTime, taskStatus;
    private String title, desc, createTime, status;
    private LinearLayout linearLayout;
    private Button taskBtn;

    TaskFragment(){

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getActivity(), TaskListActivity.class);
                startActivity(intent);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    public static TaskFragment newInstance(String taskId) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }




    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        preferenceManager = new PreferenceManager(requireActivity());
        progressBar = view.findViewById(R.id.progress_bar);
        linearLayout = view.findViewById(R.id.linearLayout);
        taskBtn = view.findViewById(R.id.task_btn);
        progressBar.setVisibility(View.VISIBLE);

        taskTitle = view.findViewById(R.id.task_title);
        taskDesc = view.findViewById(R.id.task_desc);
        taskCreateTime = view.findViewById(R.id.task_create_time);
        taskStatus = view.findViewById(R.id.task_status);
        getTask();
        view.setOnTouchListener((v, event) -> true);

        return view;
    }

    private void getTask(){
        String currentUser = preferenceManager.getString(Constants.KEY_USER_ID);
        Bundle args = getArguments();
        if (args != null) {
            String taskId = args.getString(Constants.KEY_TASK_ID);
            preferenceManager.putString(Constants.KEY_TASK_ID, taskId);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            taskRef = db.collection(Constants.KEY_COLLECTION_TASK).document(taskId);
            taskRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    title = documentSnapshot.getString(Constants.KEY_TASK);
                    desc = documentSnapshot.getString(Constants.KEY_DESCRIPTION);
                    createTime = documentSnapshot.getString(Constants.KEY_TASK_TIME);
                    status = documentSnapshot.getString(Constants.KEY_TASK_STATUS);
                    taskTitle.setText(title);
                    taskDesc.setText(desc);
                    taskCreateTime.setText(createTime);
                    taskStatus.setText(status);
                    linearLayout.setVisibility(View.VISIBLE);
                    if (documentSnapshot.contains(Constants.KEY_USER_ID) &&
                            Objects.equals(documentSnapshot.getString(Constants.KEY_USER_ID), currentUser) &&
                            Objects.equals(documentSnapshot.getString(Constants.KEY_TASK_STATUS), "ВЫПОЛНЯЕТСЯ...")) {
                            taskBtn.setText("Завершить задачу");

                            }
                    taskBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                    taskBtn.setOnClickListener(v -> updateTask());
                } else {
                    showMessage("Задача не найдена");
                }
            }).addOnFailureListener(e -> {
            });
        }
    }
    private void updateTask(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String currentUser = preferenceManager.getString(Constants.KEY_USER_ID);
        String name = preferenceManager.getString(Constants.KEY_NAME);
        String taskId = preferenceManager.getString(Constants.KEY_TASK_ID);
        taskRef.get().addOnSuccessListener(document -> {
            if (document.exists() && !document.contains(Constants.KEY_USER_ID)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Взять задачу?")
                        .setPositiveButton("Да", (dialog, which) -> taskRef.update(Constants.KEY_TASK_STATUS, "ВЫПОЛНЯЕТСЯ...", Constants.KEY_NAME, name,
                                        Constants.KEY_USER_ID, currentUser)
                                .addOnSuccessListener(aVoid -> {
                                    DocumentReference userRef = database.collection(Constants.KEY_COLLECTION_USERS)
                                            .document(currentUser);
                                    userRef.update(Constants.KEY_TASK_USER, title, Constants.KEY_TASK_ID, taskId);
                                    preferenceManager.putString(Constants.KEY_TASK, title);
                                    showMessage("Информация обновлена");
                                    Activity currentActivity = requireActivity();
                                    if (currentActivity instanceof TaskListActivity) {
                                        TaskListActivity activity = (TaskListActivity) currentActivity;
                                        TaskAdapter taskAdapter = activity.getTaskAdapter();
                                        Task updatedTask = new Task(title, createTime, "ВЫПОЛНЯЕТСЯ...", name);
                                        updatedTask.setId(taskId);
                                        taskAdapter.updateTask(updatedTask);
                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    }
                                    else{
                                        MyTasksActivity activity = (MyTasksActivity) currentActivity;
                                        TaskAdapter taskAdapter = activity.getTaskAdapter();
                                        Task updatedTask = new Task(title, createTime, "ВЫПОЛНЯЕТСЯ...", name);
                                        updatedTask.setId(taskId);
                                        taskAdapter.updateTask(updatedTask);
                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    showMessage("Ошибка");
                                }))
                        .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                        .show();
            } else if (document.exists() && document.contains(Constants.KEY_USER_ID) &&
                    Objects.equals(document.getString(Constants.KEY_USER_ID), currentUser) &&
                    Objects.equals(document.getString(Constants.KEY_TASK_STATUS), "ВЫПОЛНЯЕТСЯ...")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Завершить задачу?")
                        .setPositiveButton("Да", (dialog, which) -> taskRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    DocumentReference userRef = database.collection(Constants.KEY_COLLECTION_USERS)
                                            .document(currentUser);
                                    userRef.update(Constants.KEY_TASK_USER, FieldValue.delete(), Constants.KEY_TASK_ID, FieldValue.delete());
                                    showMessage("Задача завершена");

                                    Activity currentActivity = requireActivity();
                                    if (currentActivity instanceof TaskListActivity) {
                                        TaskListActivity activity = (TaskListActivity) currentActivity;
                                        TaskAdapter taskAdapter = activity.getTaskAdapter();
                                        taskAdapter.removeTask(taskId);
                                    }
                                    else{
                                        MyTasksActivity activity = (MyTasksActivity) currentActivity;
                                        TaskAdapter taskAdapter = activity.getTaskAdapter();
                                        taskAdapter.removeTask(taskId);
                                    }
                                    requireActivity().getSupportFragmentManager().popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    showMessage("Ошибка");
                                }))
                        .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                        .show();
            } else if (document.exists() && document.contains(Constants.KEY_USER_ID) &&
                    !Objects.equals(document.getString(Constants.KEY_USER_ID), currentUser)) {
                showMessage("Данная задача уже выполняется");
            }
        });
    }
    private void showMessage(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}