package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {



    public static void main(String[] args) {

        Timer.start();

        Path path = getPathAndValidate(args);

        System.out.print("Подождите, идет выполнение программы...");


        //мапа содержит все значения long встречающиеся в файле
        //и подсчитает количество раз их повторения
        Map<Long,Integer> map = new HashMap<>();
        readTxtFile(
                path,
                nextLine -> {
                    Long[] longs = buildLineArray(nextLine);

                    for (Long l : longs) {
                        if (map.containsKey(l)) map.put(l, map.get(l) + 1);
                        else map.put(l, 1);
                    }
                    return null;
                });

        //set содержит все long встречающиеся в файле более 1 раза
        HashSet<Long> nonUniqueLongs = map.entrySet()
                .stream()
                .filter(entry->entry.getValue()>1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(HashSet::new));

        map.clear();

        //пройдемся по файлу еще раз, выберем только те строки,
        //которые в себе имеют неуникальные значения
        LinkedList<Long[]> lines = new LinkedList<>();
        readTxtFile(path,
                nextLine -> {
                    Long[] longsLine = buildLineArray(nextLine);
                    boolean a = false;
                    for (Long l : longsLine) {
                        if (l != null && nonUniqueLongs.contains(l)) {
                            a = true;
                            break;
                        }
                    }
                    if (a) lines.add(longsLine);
                    return null;
                });

        //соберем из списка строк список Group
        List<Group> groups = collectGroups(lines)
                .stream()
                .filter(it -> it.size() > 1)
                .sorted((a,b)->Integer.compare(b.size(), a.size()))
                .toList();

        Path outputFile = writeIntoFile(groups);

        System.out.print("\r                                                                    \r");
        System.out.println("Готово!");
        System.out.println("Количество групп: "+groups.size());
        System.out.println("Путь к файлу c выводом групп: " + outputFile);

        Timer.stopAndPrint();
    }

    private static Path writeIntoFile(List<Group> groups) {
        // Получаем текущую рабочую директорию
        String currentDir = System.getProperty("user.dir");
        // Создаем путь к файлу в текущей директории
        Path filePath = Paths.get(currentDir, "output.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("Количество групп: "+groups.size());
            writer.newLine();
            writer.newLine();

            for (int i = 0; i < groups.size(); i++) {
                writer.write("Группа "+(i+1));
                writer.newLine();
                for (Long[] line : groups.get(i)) {
                    writer.write(lineAsString(line));
                    writer.newLine();
                }
                writer.write("...");
                writer.newLine();
                writer.newLine();
            }
        } catch (IOException e){
            System.err.println("Ошибка вывода в файл");
            e.printStackTrace();
        }
        return filePath;
    }


    private static String lineAsString(Long[] line) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < line.length; i++) {
            builder.append("\"");
            if(line[i]!=null)builder.append(line[i].toString());
            builder.append("\"");
            if(i!=line.length-1) builder.append(";");
        }
        return builder.toString();
    }

    //соберет из списка строк список групп
    private static List<Group> collectGroups(LinkedList<Long[]> lines) {
        List<Group> groups = new ArrayList<>();

        while (!lines.isEmpty()){
            Group group = new Group();
            //добавим первую строку в группу
            group.add(lines.pollFirst());

            //пройдемся по остальным строкам совпадают ли они со строками в группе
            int indexCheckedLine = 0;
            while (indexCheckedLine< group.size()){
                Iterator<Long[]> it = lines.iterator();
                Long[] curGroupLine = group.get(indexCheckedLine);

                while (it.hasNext()){
                    Long[] next = it.next();
                    //сравним две строки по условию
                    if(isFits(curGroupLine,next)){
                        group.addUnique(next); //сразу добавит и проверит на совпадение с предыдущими строками в группе
                        it.remove();
                    }
                }
                indexCheckedLine++;
            }
            groups.add(group);
        }
        return groups;
    }

    //сравнит две строки по условию из задания
    private static boolean isFits(Long[] curGroupLine, Long[] next) {
        for (int i = 0; i < curGroupLine.length && i < next.length; i++) {
            if (curGroupLine[i] == null || next[i]==null) continue;
            if (curGroupLine[i].equals(next[i])) return true;
        }
        return false;
    }

    //переведет из строки в список long[]
    //так как в файле все элементы имеют формат long, с ними легче работать меньше весят быстрее работа процессора
    public static Long[] buildLineArray(String str){
        String[] parts = str.split(";", -1);
        Long[] numbers = new Long[parts.length];

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim().replace("\"", "");  // Удаляем кавычки
            if (part.isEmpty()) {
                numbers[i] = null;
            } else {
                numbers[i] = Long.parseLong(part);
            }
        }
        return numbers;
    }

    //реализует функционал построчного чтения файла .txt
    public static void readTxtFile(Path path, Function<String,Void> function){
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                function.apply(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //проверит чтобы в параметры не подложили файл отличный от .txt
    //так как в задании был файл .gz
    //и другие проверки
    private static Path getPathAndValidate(String[] args) {
        //проверка, что передан хотя бы один аргумент
        if (args.length == 0) {
            System.err.println("Ошибка: Не указан путь к файлу .txt");
            System.err.println("Использование: java -Xmx1G -jar <path_to_jar>.jar <path_to_file.txt>");
            System.exit(1);
        }

        Path filePath = Path.of(args[0]);

        //проверка существования файла
        if (!Files.exists(filePath)) {
            System.err.println("Ошибка: Файл не существует - " + filePath);
            System.exit(2);
        }

        // Проверка расширения файла (.txt или .TXT)
        String fileName = filePath.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".txt")) {
            System.err.println("Ошибка: Файл должен иметь расширение .txt");
            System.err.println("Получен файл: " + fileName);
            System.exit(3);
        }

        return filePath;
    }
}