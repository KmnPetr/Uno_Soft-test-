package org.example;

/**
 * простой класс таймер засекает время выполнения кода
 */
public class Timer {
    private static long startTime;

    //вызывается перед выполнением кода время выполнения которого необходимо посчитать
    public static void start() {
        startTime = System.currentTimeMillis();
    }

    //вызывается после выполнения кода, делает подсчет времени
    public static void stopAndPrint() {
        long durationMs = System.currentTimeMillis() - startTime;
        double durationSec = durationMs / 1000.0;
        System.out.printf("Время выполнения: %.3f сек%n", durationSec);
    }
}
