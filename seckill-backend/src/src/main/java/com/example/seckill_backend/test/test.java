package com.example.seckill_backend.test;

public class Test {
    public static void main(String[] args) {
        System.out.println("=== Java基础测试程序 ===");

        // 1. 变量定义和基本运算
        int a = 10;
        int b = 5;
        int sum = a + b;
        int product = a * b;

        System.out.println("a = " + a + ", b = " + b);
        System.out.println("a + b = " + sum);
        System.out.println("a * b = " + product);

        // 2. 条件判断
        System.out.println("\n=== 条件判断示例 ===");
        if (a > b) {
            System.out.println("a 大于 b");
        } else if (a < b) {
            System.out.println("a 小于 b");
        } else {
            System.out.println("a 等于 b");
        }

        // 3. 循环示例
        System.out.println("\n=== 循环示例 ===");
        System.out.print("1到5的数字：");
        for (int i = 1; i <= 5; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        // 4. 数组操作
        System.out.println("\n=== 数组操作示例 ===");
        int[] numbers = {3, 7, 2, 9, 1};
        System.out.print("数组元素：");
        for (int num : numbers) {
            System.out.print(num + " ");
        }
        System.out.println();

        // 查找最大值
        int max = numbers[0];
        for (int num : numbers) {
            if (num > max) {
                max = num;
            }
        }
        System.out.println("数组最大值：" + max);

        // 5. 方法调用
        System.out.println("\n=== 方法调用示例 ===");
        int factorialResult = factorial(5);
        System.out.println("5的阶乘：" + factorialResult);

        // 6. 字符串操作
        System.out.println("\n=== 字符串操作 ===");
        String message = "Hello, Java!";
        System.out.println("原始字符串：" + message);
        System.out.println("大写：" + message.toUpperCase());
        System.out.println("长度：" + message.length());

        // 7. 简单异常处理
        System.out.println("\n=== 异常处理示例 ===");
        try {
            int result = divide(10, 2);
            System.out.println("10 / 2 = " + result);

            result = divide(10, 0); // 会抛出异常
        } catch (ArithmeticException e) {
            System.out.println("捕获异常：" + e.getMessage());
        }

        System.out.println("\n=== 程序结束 ===");
    }

    /**
     * 计算阶乘的方法
     */
    public static int factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }

    /**
     * 除法运算，演示异常抛出
     */
    public static int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("除数不能为零");
        }
        return a / b;
    }
}