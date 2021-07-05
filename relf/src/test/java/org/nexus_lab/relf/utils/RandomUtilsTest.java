package org.nexus_lab.relf.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Ruipeng Zhang
 */
public class RandomUtilsTest {

    @Test
    public void oneOf() {
        int i = 0;
        int prev = 0;
        boolean different = false;
        while (i < 5 || !different) {
            int result = RandomUtils.oneOf(1, 2, 3, 4, 5);
            if (prev > 0) {
                if (prev != result) {
                    different = true;
                }
            }
            assertTrue("The function does not choose from provided values", result >= 1);
            assertTrue("The function does not choose from provided values", result <= 5);
            assertTrue("The function does not return randomly", i < 30);
            i++;
            prev = result;
        }
    }
}