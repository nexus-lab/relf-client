package org.nexus_lab.relf.testload.app;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Calculator", packageName = "com.google.android.calculator")
public class Calculator extends RunnableApp {
    private final static String TAG = Calculator.class.getSimpleName();
    private final static String[] BASIC_BUTTONS = new String[]{
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "=", "+", "-", "*", "/", "del",
            "clr"
    };
    private final static String[] INVERT_BUTTONS = new String[]{
            "arcsin", "arccos", "arctan", "exp", "10pow", "sqr"
    };

    private boolean isInvertEnabled;

    private boolean isPadAdvancedOpen() {
        UiObject2 pad = getDevice().findObject(By.res(getResourceId("pad_advanced")));
        return pad != null;
    }

    private void togglePadAdvanced() {
        if (isPadAdvancedOpen()) {
            UiObject2 pad = getDevice().findObject(By.res(getResourceId("pad_advanced")));
            pad.swipe(Direction.RIGHT, 1);
            getDevice().wait(Until.gone(By.res(getResourceId("pad_advanced"))), 1000);
        } else {
            UiObject2 pad = getDevice().findObject(By.res(getResourceId("pad_basic")));
            pad.getParent().swipe(Direction.LEFT, 1);
            getDevice().wait(Until.hasObject(By.res(getResourceId("pad_advanced"))), 1000);
        }
    }

    private void toggleInvert() {
        boolean padOpen = isPadAdvancedOpen();
        if (!padOpen) {
            togglePadAdvanced();
        }
        UiObject2 inv = getDevice().findObject(By.res(getResourceId("toggle_inv")));
        inv.click();
        isInvertEnabled = inv.isSelected();
        if (!padOpen) {
            togglePadAdvanced();
        }
    }

    private UiObject2 getButtonByName(String name) {
        boolean inPadBasic = false;
        boolean inPadInvert = false;
        for (String button : BASIC_BUTTONS) {
            if (button.equals(name)) {
                inPadBasic = true;
            }
        }
        for (String button : INVERT_BUTTONS) {
            if (button.equals(name)) {
                inPadInvert = true;
            }
        }
        if (inPadBasic == isPadAdvancedOpen()) {
            togglePadAdvanced();
        }
        if (inPadInvert != isInvertEnabled) {
            toggleInvert();
        }
        switch (name) {
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                return getDevice().findObject(By.res(getResourceId("digit_" + name)));
            case ".":
                return getDevice().findObject(By.res(getResourceId("dec_point")));
            case "=":
                return getDevice().findObject(By.res(getResourceId("eq")));
            case "+":
                return getDevice().findObject(By.res(getResourceId("op_add")));
            case "-":
                return getDevice().findObject(By.res(getResourceId("op_sub")));
            case "*":
                return getDevice().findObject(By.res(getResourceId("op_mul")));
            case "/":
                return getDevice().findObject(By.res(getResourceId("op_div")));
            case "(":
                return getDevice().findObject(By.res(getResourceId("lparen")));
            case ")":
                return getDevice().findObject(By.res(getResourceId("rparen")));
            case "^":
                return getDevice().findObject(By.res(getResourceId("op_pow")));
            case "e":
                return getDevice().findObject(By.res(getResourceId("const_e")));
            case "pi":
                return getDevice().findObject(By.res(getResourceId("const_pi")));
            case "%":
                return getDevice().findObject(By.res(getResourceId("op_pct")));
            case "!":
                return getDevice().findObject(By.res(getResourceId("op_fact")));
            case "del":
            case "clr":
                return getDevice().findObject(By.res(getResourceId(name)));
            case "sin":
            case "cos":
            case "tan":
            case "arcsin":
            case "arccos":
            case "arctan":
            case "ln":
            case "log":
            case "exp":
            case "10pow":
                return getDevice().findObject(By.res(getResourceId("fun_" + name)));
            case "sqr":
            case "sqrt":
                return getDevice().findObject(By.res(getResourceId("op_" + name)));
            default:
                return null;
        }
    }

    private void eval(String expression) {
        String[] segments = expression.split("\\s+");
        for (String segment : segments) {
            UiObject2 button = getButtonByName(segment);
            if (button == null) {
                Log.w(TAG, "Cannot find button " + segment);
                return;
            }
            button.click();
        }
    }

    private void clearHistory() {
        UiObject2 menu = getDevice().findObject(By.desc("More options"));
        menu.click();
        UiObject2 history = getDevice().wait(Until.findObject(By.text("History")), 1000);
        history.click();
        menu = getDevice().wait(Until.findObject(By.desc("More options")), 1000);
        menu.click();
        UiObject2 clear = getDevice().wait(Until.findObject(By.text("Clear")), 1000);
        clear.click();
        getDevice().waitForIdle();
        UiObject2 confirm = getDevice().wait(Until.findObject(By.res("android:id/button1")), 1000);
        confirm.click();
        getDevice().waitForIdle();
    }

    private void clearInput() {
        UiObject2 del = getButtonByName("del");
        if (del != null) {
            del.click(500);
        } else {
            UiObject2 clr = getButtonByName("clr");
            if (clr != null) {
                clr.click(500);
            }
        }
    }

    @Override
    public void run() {
        eval("1 0 0 % 3 =");
        eval("sqrt ( 1 0 0 ) + 1 =");
        eval("sin pi ) + cos pi ) =");
        clearInput();
        eval("arctan 1 ) =");
        eval("exp 1 0 ) =");
        eval("10pow 1 0 =");
        clearHistory();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("formula"), 5000) != null;
    }
}
