package org.example;

import java.util.ArrayList;

/**
 * группа строк из файла
 */
public class Group extends ArrayList<Long[]>{
    //добавит новую строку с условием отсутствия в группе похожих строк
    public boolean addUnique(Long[] line) {
        for (Long[] existingLine : this) {
            if (isLinesEqual(existingLine, line)) {
                System.err.println("Найдена неуникальная строка");
                return false;
            }
        }
        return super.add(line);
    }

    //Сравнивает две строки на равенство
    private boolean isLinesEqual(Long[] line1, Long[] line2) {
        if (line1.length != line2.length) return false;

        for (int i = 0; i < line1.length; i++) {
            if (line1[i] == null) {
                if (line2[i] != null) {
                    return false;
                }
            } else {
                if (!line1[i].equals(line2[i])) {
                    return false;
                }
            }
        }
        return true;
    }
}
