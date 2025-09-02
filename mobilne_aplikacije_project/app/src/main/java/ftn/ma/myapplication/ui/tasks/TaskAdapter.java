package ftn.ma.myapplication.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.ExecutorService;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.model.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskDao taskDao;
    private ExecutorService executorService;
    private OnTaskListener onTaskListener;

    public interface OnTaskListener {
        void onTaskLongClick(Task task);
    }

    // Konstruktor sada prima i DAO i ExecutorService
    public TaskAdapter(List<Task> taskList, TaskDao taskDao, ExecutorService executorService, OnTaskListener onTaskListener) {
        this.taskList = taskList;
        this.taskDao = taskDao;
        this.executorService = executorService;
        this.onTaskListener = onTaskListener; // NOVO
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public View categoryColorView;
        public TextView taskNameTextView;
        public TextView taskCategoryTextView;
        public CheckBox taskDoneCheckBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryColorView = itemView.findViewById(R.id.viewTaskCategoryColor);
            taskNameTextView = itemView.findViewById(R.id.textViewTaskName);
            taskCategoryTextView = itemView.findViewById(R.id.textViewTaskCategory);
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
        holder.taskNameTextView.setText(currentTask.getName());

        holder.itemView.setOnLongClickListener(view -> {
            if (onTaskListener != null) {
                onTaskListener.onTaskLongClick(currentTask);
                return true; // Vraćamo true da kažemo sistemu da smo obradili događaj
            }
            return false;
        });

        if (currentTask.getCategory() != null) {
            holder.taskCategoryTextView.setText(currentTask.getCategory().getName());
            holder.categoryColorView.setBackgroundColor(currentTask.getCategory().getColor());
        } else {
            holder.taskCategoryTextView.setText("Bez kategorije");
        }

        // Resetujemo listener pre postavljanja vrednosti da sprečimo neželjeno aktiviranje
        holder.taskDoneCheckBox.setOnCheckedChangeListener(null);
        holder.taskDoneCheckBox.setChecked(currentTask.getStatus() == Task.Status.URADJEN);

        // Postavljamo listener koji će sačuvati promenu u bazi
        holder.taskDoneCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentTask.setStatus(isChecked ? Task.Status.URADJEN : Task.Status.AKTIVAN);
            executorService.execute(() -> {
                taskDao.update(currentTask);
            });
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
