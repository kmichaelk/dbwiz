package ru.mkr.dbwiz.handlers;

import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.enums.ProjectExecutionState;

public interface ProjectExecutionHandler {

    String getProjectName();
    void interruptProjectExecution();
    void onProjectExecutionInterruptRequest(int exitCode);
    void onProjectEngineInitialized(ScriptEngine engine);
    void onProjectStateChange(ProjectExecutionState state);
    void onProjectStatusChange(String status);
    void onProjectProgressChange(int progress);
    void onProjectExecutionTimeCalculated(long time);
    void onProjectExecutionException(Exception ex);

    final class DummyProjectExecutionHandler implements ProjectExecutionHandler {

        private ScriptEngine engine = null;
        private boolean interrupted = false;
        private Thread thread = Thread.currentThread();

        public void setThread(Thread thread) {
            this.thread = thread;
        }

        @Override
        public String getProjectName() {
            return "Проект";
        }

        @Override
        public void interruptProjectExecution() {
            System.out.println("Прерывание выполения проекта...");
            interrupted = true;
            if(engine != null) {
                engine.kill();
                engine.shutdown();
                engine = null;
            }
            thread.interrupt();
            thread.stop();
            onProjectStateChange(ProjectExecutionState.INTERRUPTED);
            System.out.println("Выполение проекта прервано");
        }

        @Override
        public void onProjectExecutionInterruptRequest(int exitCode) {
            System.out.println("\n-----------------\nПроект запросил остановку работы" +
                    (exitCode == 0 ? "" : ", код выхода: " + exitCode));
            interruptProjectExecution();
        }

        @Override
        public void onProjectEngineInitialized(ScriptEngine engine) {
            this.engine = engine;
        }

        @Override
        public void onProjectStateChange(ProjectExecutionState state) {}

        @Override
        public void onProjectStatusChange(String status) {
            System.out.println("Проект установил статус: " + status);
        }

        @Override
        public void onProjectProgressChange(int progress) {
            System.out.println("Прогресс: " + progress + "%");
        }

        @Override
        public void onProjectExecutionTimeCalculated(long time) {}

        @Override
        public void onProjectExecutionException(Exception ex) {
            if(interrupted) return;
            System.out.println("При выполнении проекта произошла ошибка: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
