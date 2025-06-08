package toDoApp;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ToDo extends JFrame {
    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;
    private JTextField taskInput;
    private JComboBox<String> priorityCombo;
    private JTextField dueDateInput; // format: yyyy-MM-dd
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ToDo() {
        setTitle("To-Do List App");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top: Input panel with task, priority, due date, add button
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));

        taskInput = new JTextField();
        inputPanel.add(taskInput, BorderLayout.CENTER);

        // Priority dropdown
        String[] priorities = {"Low", "Medium", "High"};
        priorityCombo = new JComboBox<>(priorities);
        inputPanel.add(priorityCombo, BorderLayout.WEST);

        // Due date input (yyyy-MM-dd)
        dueDateInput = new JTextField("Due yyyy-MM-dd");
        dueDateInput.setForeground(Color.GRAY);
        inputPanel.add(dueDateInput, BorderLayout.EAST);

        // Clear placeholder on focus
        dueDateInput.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (dueDateInput.getText().equals("Due yyyy-MM-dd")) {
                    dueDateInput.setText("");
                    dueDateInput.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (dueDateInput.getText().isEmpty()) {
                    dueDateInput.setForeground(Color.GRAY);
                    dueDateInput.setText("Due yyyy-MM-dd");
                }
            }
        });

        // Add button
        JButton addButton = new JButton("Add");
        inputPanel.add(addButton, BorderLayout.SOUTH);

        // Center: Task list with custom renderer
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new TaskRenderer());
        JScrollPane scrollPane = new JScrollPane(taskList);

        // Bottom: Buttons panel
        JPanel actionPanel = new JPanel();

        JButton doneButton = new JButton("Mark as Done");
        JButton deleteButton = new JButton("Delete");
        JButton clearDoneButton = new JButton("Clear Completed");

        actionPanel.add(doneButton);
        actionPanel.add(deleteButton);
        actionPanel.add(clearDoneButton);

        // Add to frame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        // Add task button action
        addButton.addActionListener(e -> {
            String taskText = taskInput.getText().trim();
            String dueDateText = dueDateInput.getText().trim();
            String priority = (String) priorityCombo.getSelectedItem();

            if (taskText.isEmpty()) return;

            LocalDate dueDate = null;
            if (!dueDateText.isEmpty() && !dueDateText.equals("Due yyyy-MM-dd")) {
                try {
                    dueDate = LocalDate.parse(dueDateText, dateFormatter);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.");
                    return;
                }
            }

            Task newTask = new Task(taskText, priority, dueDate, false);
            taskListModel.addElement(newTask);

            taskInput.setText("");
            dueDateInput.setText("Due yyyy-MM-dd");
            dueDateInput.setForeground(Color.GRAY);
            priorityCombo.setSelectedIndex(0);
        });

        // Mark as done action
        doneButton.addActionListener(e -> {
            int index = taskList.getSelectedIndex();
            if (index != -1) {
                Task t = taskListModel.getElementAt(index);
                if (!t.isDone()) {
                    t.setDone(true);
                    taskList.repaint();
                }
            }
        });

        // Delete selected task
        deleteButton.addActionListener(e -> {
            int index = taskList.getSelectedIndex();
            if (index != -1) {
                taskListModel.remove(index);
            }
        });

        // Clear all completed tasks
        clearDoneButton.addActionListener(e -> {
            for (int i = taskListModel.size() - 1; i >= 0; i--) {
                if (taskListModel.getElementAt(i).isDone()) {
                    taskListModel.remove(i);
                }
            }
        });

        // Double-click to edit task
        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = taskList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        Task t = taskListModel.getElementAt(index);
                        editTaskDialog(t, index);
                    }
                }
            }
        });
    }

    // Dialog to edit task details
    private void editTaskDialog(Task task, int index) {
        JTextField taskField = new JTextField(task.getText());
        JComboBox<String> priorityField = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        priorityField.setSelectedItem(task.getPriority());
        JTextField dueDateField = new JTextField(task.getDueDate() != null ? task.getDueDate().format(dateFormatter) : "");
        
        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.add(new JLabel("Task:"));
        panel.add(taskField);
        panel.add(new JLabel("Priority:"));
        panel.add(priorityField);
        panel.add(new JLabel("Due Date (yyyy-MM-dd):"));
        panel.add(dueDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String newText = taskField.getText().trim();
            if (newText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Task cannot be empty!");
                return;
            }
            String newPriority = (String) priorityField.getSelectedItem();
            LocalDate newDueDate = null;
            String dueDateStr = dueDateField.getText().trim();
            if (!dueDateStr.isEmpty()) {
                try {
                    newDueDate = LocalDate.parse(dueDateStr, dateFormatter);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.");
                    return;
                }
            }
            task.setText(newText);
            task.setPriority(newPriority);
            task.setDueDate(newDueDate);
            taskList.repaint();
        }
    }

    // Custom class to hold task data
    private static class Task {
        private String text;
        private String priority;
        private LocalDate dueDate;
        private boolean done;

        public Task(String text, String priority, LocalDate dueDate, boolean done) {
            this.text = text;
            this.priority = priority;
            this.dueDate = dueDate;
            this.done = done;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

        public boolean isDone() { return done; }
        public void setDone(boolean done) { this.done = done; }

        @Override
        public String toString() {
            return text;
        }
    }

    // Renderer to show task with priority color, done status, and due date
    private static class TaskRenderer extends JLabel implements ListCellRenderer<Task> {
        private static final Color COLOR_DONE = new Color(150, 150, 150);
        private static final Color COLOR_HIGH = new Color(255, 80, 80);
        private static final Color COLOR_MEDIUM = new Color(255, 165, 0);
        private static final Color COLOR_LOW = new Color(100, 180, 100);

        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task task, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            String text = task.getText();

            if (task.isDone()) {
                text = "<html><strike>" + text + "</strike></html>";
                setForeground(COLOR_DONE);
            } else {
                setForeground(Color.BLACK);
            }

            // Priority color
            switch (task.getPriority()) {
                case "High": setForeground(COLOR_HIGH); break;
                case "Medium": setForeground(COLOR_MEDIUM); break;
                case "Low": setForeground(COLOR_LOW); break;
            }

            // Append due date if present
            if (task.getDueDate() != null) {
                text += " (Due: " + task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd")) + ")";
            }

            setText(text);

            if (isSelected) {
                setBackground(new Color(200, 200, 255));
                setOpaque(true);
            } else {
                setOpaque(false);
            }

            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ToDo app = new ToDo();
            app.setVisible(true);
        });
    }
}
