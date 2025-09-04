package ftn.ma.myapplication.ui.tasks;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.model.Task;
import java.text.SimpleDateFormat;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskDao taskDao;
    private ExecutorService executorService;
    private OnTaskListener onTaskListener;
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());


    public interface OnTaskListener {
        void onTaskLongClick(Task task);
        void onTaskClick(Task task);
        void onTaskCheckedChanged(Task task, boolean isChecked);
    }

    public TaskAdapter(List<Task> taskList, TaskDao taskDao, ExecutorService executorService, OnTaskListener onTaskListener) {
        this.taskList = taskList;
        this.taskDao = taskDao;
        this.executorService = executorService;
        this.onTaskListener = onTaskListener;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public View categoryColorView;
        public TextView taskNameTextView;
        public TextView taskCategoryTextView;
        public TextView textViewTaskTime; // NOVO
        public CheckBox taskDoneCheckBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryColorView = itemView.findViewById(R.id.viewTaskCategoryColor);
            taskNameTextView = itemView.findViewById(R.id.textViewTaskName);
            taskCategoryTextView = itemView.findViewById(R.id.textViewTaskCategory);
            textViewTaskTime = itemView.findViewById(R.id.textViewTaskTime); // NOVO
            taskDoneCheckBox = itemView.findViewById(R.id.checkBoxTaskDone);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = taskList.get(position);

        // Resetujemo izgled pre postavljanja novih vrednosti (zbog recikliranja)
        holder.taskNameTextView.setTextColor(Color.BLACK);
        holder.taskNameTextView.setPaintFlags(holder.taskNameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        holder.taskDoneCheckBox.setEnabled(true);

        holder.taskNameTextView.setText(currentTask.getName());

        if (currentTask.getCategory() != null) {
            holder.taskCategoryTextView.setText(currentTask.getCategory().getName());
            holder.categoryColorView.setBackgroundColor(currentTask.getCategory().getColor());
        } else {
            holder.taskCategoryTextView.setText("Bez kategorije");
        }

        if (!currentTask.isRecurring() && currentTask.getExecutionTime() != null) {
            holder.textViewTaskTime.setVisibility(View.VISIBLE);
            holder.textViewTaskTime.setText("u " + timeFormatter.format(currentTask.getExecutionTime()));
        } else {
            holder.textViewTaskTime.setVisibility(View.GONE);
        }

        // Postavljamo izgled na osnovu statusa
        switch (currentTask.getStatus()) {
            case PAUZIRAN:
                holder.taskNameTextView.setTextColor(Color.GRAY);
                holder.taskDoneCheckBox.setEnabled(false); // Pauziran zadatak se ne može rešiti
                break;
            case OTKAZAN:
                holder.taskNameTextView.setTextColor(Color.GRAY);
                holder.taskNameTextView.setPaintFlags(holder.taskNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.taskDoneCheckBox.setEnabled(false); // Otkazan zadatak se ne može rešiti
                break;
        }

        // Postavljamo listenere
        holder.itemView.setOnLongClickListener(view -> {
            if (onTaskListener != null) {
                onTaskListener.onTaskLongClick(currentTask);
                return true;
            }
            return false;
        });

        holder.itemView.setOnClickListener(view -> {
            if (onTaskListener != null) {
                onTaskListener.onTaskClick(currentTask);
            }
        });

        holder.taskDoneCheckBox.setOnCheckedChangeListener(null);
        holder.taskDoneCheckBox.setChecked(currentTask.getStatus() == Task.Status.URADJEN);
        holder.taskDoneCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (onTaskListener != null) {
                onTaskListener.onTaskCheckedChanged(currentTask, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}