package org.nexus_lab.relf.utils;

import java.util.Random;

/**
 * Random utilities (primarily for testing)
 *
 * @author Ruipeng Zhang
 */
public class RandomUtils {
    /**
     * Randomly choose one element from the input elements
     *
     * @param elements elements to choose from
     * @param <T>      type of the elements
     * @return the chosen one
     */
    public static <T> T oneOf(T... elements) {
        Random random = new Random();
        int i = random.nextInt(elements.length);
        return elements[i];
    }
}
