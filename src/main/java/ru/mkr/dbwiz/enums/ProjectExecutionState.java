package ru.mkr.dbwiz.enums;

public enum ProjectExecutionState {

    LOADING("Загрузка проекта"),
    ENGINE_INITIALIZING("Инициализация движка"),
    MODULES_LOADING("Загрузка модулей"),
    STARTING("Запуск проекта"),
    EXECUTING("Выполнение проекта"),
    FINISHED("Выполнение проекта завершено"),
    INTERRUPTED("Выполнение проекта прервано");

    private final String displayName;

    ProjectExecutionState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNumber() {
        return ordinal()+1;
    }

    public boolean isLast() {
        //return getNumber() == values().length-1;
        return this == FINISHED || this == INTERRUPTED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
