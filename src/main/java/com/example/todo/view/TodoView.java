package com.example.todo.view;

import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.time.Duration;

@Route("")
public class TodoView extends VerticalLayout {
    private final TodoRepository repository;
    private final Grid<Todo> grid = new Grid<>(Todo.class, false);
    private final TextField title = new TextField("Title");
    private final TextField description = new TextField("Description");
    private final DateTimePicker dueDate = new DateTimePicker("Due Date");
    private final Button add = new Button("Add task");
    private final Button cancel = new Button("Cancel");
    private final Tabs tabs = new Tabs();
    private final Tab todayTab = new Tab("Today");
    private final Tab futureTab = new Tab("Future");
    private final Tab archiveTab = new Tab("Archive");
    private final Binder<Todo> addBinder = new Binder<>(Todo.class);
    private Todo editingTodo = null;
    private Todo lastAddedTodo = null;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private LocalDateTime editingOriginalDueDate = null;

    @Autowired
    public TodoView(TodoRepository repository) {
        this.repository = repository;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        add(new H2("Vaadin Todo App"));
        configureTabs();
        configureGrid();
        configureAddForm();
        add(tabs, grid, getAddFormLayout());
        updateList();
    }

    private void configureTabs() {
        tabs.add(todayTab, futureTab, archiveTab);
        tabs.setSelectedTab(todayTab);
        tabs.addSelectedChangeListener(event -> {
            updateList();
        });
    }

    private void configureGrid() {
        grid.addColumn(new ComponentRenderer<>(todo -> {
            Checkbox checkbox = new Checkbox(todo.isCompleted());
            checkbox.addValueChangeListener(e -> {
                todo.setCompleted(e.getValue());
                todo.setUpdatedAt(LocalDateTime.now());
                repository.save(todo);
                updateList();
                if (e.getValue()) {
                    Notification.show("Task finished");
                }
            });
            checkbox.setEnabled(!todo.isCompleted());
            return checkbox;
        })).setHeader("").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(todo -> {
            if (editingTodo != null && editingTodo.getId().equals(todo.getId())) {
                TextField editTitle = new TextField();
                editTitle.setValue(todo.getTitle() == null ? "" : todo.getTitle());
                editTitle.setWidthFull();
                editTitle.addValueChangeListener(e -> editingTodo.setTitle(e.getValue()));
                return editTitle;
            } else {
                return new TextField() {{ setValue(todo.getTitle() == null ? "" : todo.getTitle()); setReadOnly(true); setWidthFull(); }};
            }
        })).setHeader("Title").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(todo -> {
            if (editingTodo != null && editingTodo.getId().equals(todo.getId())) {
                TextField editDesc = new TextField();
                editDesc.setValue(todo.getDescription() == null ? "" : todo.getDescription());
                editDesc.setWidthFull();
                editDesc.addValueChangeListener(e -> editingTodo.setDescription(e.getValue()));
                return editDesc;
            } else {
                return new TextField() {{ setValue(todo.getDescription() == null ? "" : todo.getDescription()); setReadOnly(true); setWidthFull(); }};
            }
        })).setHeader("Description").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(todo -> {
            if (editingTodo != null && editingTodo.getId().equals(todo.getId())) {
                DateTimePicker editDue = new DateTimePicker();
                editDue.setLocale(new Locale("ru"));
                editDue.setMin(LocalDateTime.now());
                editDue.setStep(Duration.ofHours(1));
                editDue.setValue(todo.getDueDate());
                editDue.addValueChangeListener(e -> editingTodo.setDueDate(e.getValue()));
                return editDue;
            } else {
                TextField tf = new TextField();
                tf.setValue(todo.getDueDate() == null ? "" : todo.getDueDate().format(dateTimeFormatter));
                tf.setReadOnly(true);
                tf.setWidthFull();
                return tf;
            }
        })).setHeader("Due Date").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(todo -> {
            if (tabs.getSelectedTab() == archiveTab) {
                Button restore = new Button("Return", e -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime newDue = now.withHour(13).withMinute(0).withSecond(0).withNano(0);
                    if (newDue.isBefore(now)) {
                        newDue = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
                    }
                    todo.setDueDate(newDue);
                    todo.setCompleted(false);
                    todo.setUpdatedAt(LocalDateTime.now());
                    repository.save(todo);
                    // Переключить вкладку в зависимости от новой даты
                    LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
                    if (!newDue.isAfter(endOfDay)) {
                        tabs.setSelectedTab(todayTab);
                    } else {
                        tabs.setSelectedTab(futureTab);
                    }
                    updateList();
                    Notification.show("Task returned");
                });
                Button delete = new Button("Delete", e -> {
                    Dialog confirmDialog = new Dialog();
                    confirmDialog.add("Are you sure you want to delete this task?");
                    Button yes = new Button("Yes", ev -> {
                        repository.delete(todo);
                        updateList();
                        Notification.show("Task deleted");
                        confirmDialog.close();
                    });
                    Button no = new Button("No", ev -> confirmDialog.close());
                    confirmDialog.add(new HorizontalLayout(yes, no));
                    confirmDialog.open();
                });
                return new HorizontalLayout(restore, delete);
            } else if (editingTodo != null && editingTodo.getId().equals(todo.getId())) {
                Button save = new Button("Save", e -> {
                    LocalDateTime oldDue = editingOriginalDueDate;
                    LocalDateTime newDue = editingTodo.getDueDate();
                    editingTodo.setUpdatedAt(LocalDateTime.now());
                    repository.save(editingTodo);
                    editingTodo = null;
                    editingOriginalDueDate = null;
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
                    if (newDue != null) {
                        if (!newDue.isAfter(endOfDay)) {
                            tabs.setSelectedTab(todayTab);
                        } else {
                            tabs.setSelectedTab(futureTab);
                        }
                    }
                    updateList();
                    Notification.show("Task updated");
                });
                Button cancel = new Button("Cancel", e -> {
                    editingTodo = null;
                    editingOriginalDueDate = null;
                    updateList();
                });
                return new HorizontalLayout(save, cancel);
            } else {
                Button edit = new Button("Edit", e -> {
                    editingTodo = todo;
                    editingOriginalDueDate = todo.getDueDate();
                    updateList();
                });
                Button delete = new Button("Delete", e -> {
                    Dialog confirmDialog = new Dialog();
                    confirmDialog.add("Are you sure you want to delete this task?");
                    Button yes = new Button("Yes", ev -> {
                        repository.delete(todo);
                        updateList();
                        Notification.show("Task deleted");
                        confirmDialog.close();
                    });
                    Button no = new Button("No", ev -> confirmDialog.close());
                    confirmDialog.add(new HorizontalLayout(yes, no));
                    confirmDialog.open();
                });
                edit.setEnabled(!todo.isCompleted());
                delete.setEnabled(true);
                return new HorizontalLayout(edit, delete);
            }
        })).setHeader("Actions").setAutoWidth(true);
        grid.setHeight("350px");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setAllRowsVisible(true);
    }

    private VerticalLayout getAddFormLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.add(new H4("Add task"));
        HorizontalLayout form = new HorizontalLayout(title, description, dueDate, add, cancel);
        form.setDefaultVerticalComponentAlignment(Alignment.END);
        form.setWidthFull();
        layout.add(form);
        return layout;
    }

    private void configureAddForm() {
        addBinder.bind(title, Todo::getTitle, Todo::setTitle);
        addBinder.bind(description, Todo::getDescription, Todo::setDescription);
        addBinder.bind(dueDate, Todo::getDueDate, Todo::setDueDate);
        dueDate.setLocale(new Locale("ru"));
        dueDate.setMin(LocalDateTime.now());
        dueDate.setStep(Duration.ofHours(1));
        clearAddForm();
        add.addClickListener(event -> {
            if (addBinder.validate().isOk()) {
                Todo todo = new Todo();
                addBinder.writeBeanIfValid(todo);
                LocalDateTime now = LocalDateTime.now();
                if (todo.getDueDate() == null) {
                    todo.setDueDate(now.plusHours(1).withMinute(0).withSecond(0).withNano(0));
                } else if (todo.getDueDate().toLocalDate().isEqual(now.toLocalDate()) && todo.getDueDate().toLocalTime().equals(LocalTime.MIDNIGHT)) {
                    todo.setDueDate(now.plusHours(1).withMinute(0).withSecond(0).withNano(0));
                }
                todo.setCreatedAt(now);
                todo.setUpdatedAt(now);
                todo.setCompleted(false);
                repository.save(todo);
                lastAddedTodo = todo;
                LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
                if (!todo.getDueDate().isAfter(endOfDay)) {
                    tabs.setSelectedTab(todayTab);
                } else {
                    tabs.setSelectedTab(futureTab);
                }
                updateList();
                clearAddForm();
                Notification.show("Task added");
            }
        });
        cancel.addClickListener(event -> clearAddForm());
    }

    private void clearAddForm() {
        addBinder.readBean(new Todo());
        dueDate.clear();
        dueDate.setLocale(new Locale("ru"));
        dueDate.setMin(LocalDateTime.now());
        dueDate.setStep(Duration.ofHours(1));
    }

    private void updateList() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
        if (tabs.getSelectedTab() == todayTab) {
            List<Todo> todayTodos = repository.findByCompletedFalseAndDueDateBetween(now, endOfDay);
            grid.setItems(todayTodos);
        } else if (tabs.getSelectedTab() == futureTab) {
            List<Todo> futureTodos = repository.findByCompletedFalseAndDueDateAfter(endOfDay);
            grid.setItems(futureTodos);
        } else if (tabs.getSelectedTab() == archiveTab) {
            List<Todo> archivedTodos = repository.findByCompletedTrue();
            grid.setItems(archivedTodos);
        }
    }
} 