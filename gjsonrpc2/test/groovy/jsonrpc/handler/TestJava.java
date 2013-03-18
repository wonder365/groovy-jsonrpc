package groovy.jsonrpc.handler;

import groovy.jsonrpc.util.Preconditions;

import java.util.Date;
import java.util.List;


public class TestJava {
    public static int add(int a, int b) {
        return a + b;
    }

    public static Date getDate() {
        return new Date();
    }

    public static int fun1arg(int a) {
        return a + 1;
    }

    public static int adds(List<Integer> args) {
        Preconditions.checkArgument(args.size() > 2, "args count must > 2");
        int sum = 0;
        for (int ai : args) {
    	sum += ai;
        }
        return sum;
    }

    public static int sum(List<Integer> args) {
        int sum = 0;
        for (int ai : args) {
    	sum += ai;
        }
        return sum;
    }

    public static String donotify() {
        System.out.println("notify invoked");
        return "this is from notify";
    }

    public static Object echo(Object args) {
        return args;
    }
}