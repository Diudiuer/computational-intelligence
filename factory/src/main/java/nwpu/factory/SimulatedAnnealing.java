package nwpu.factory;

import java.util.Arrays;
import java.util.Random;

public class SimulatedAnnealing {

    private static final int MAX_ITER = 10000;
    private static final double INITIAL_TEMP = 100.0;
    private static final double FINAL_TEMP = 1e-3;
    private static final double ALPHA = 0.95;

    public String generateBestSolution() {
        String currentSolution = generateInitialSolution();
        String bestSolution = currentSolution;
        double currentEnergy = calculateEnergy(currentSolution);
        double bestEnergy = currentEnergy;

        double temp = INITIAL_TEMP;

        Random random = new Random();

        while (temp > FINAL_TEMP) {
            for (int i = 0; i < MAX_ITER; i++) {
                String newSolution = generateNeighbor(currentSolution, random);
                double newEnergy = calculateEnergy(newSolution);

                if (acceptanceProbability(currentEnergy, newEnergy, temp) > random.nextDouble()) {
                    currentSolution = newSolution;
                    currentEnergy = newEnergy;

                    if (currentEnergy < bestEnergy) {
                        bestSolution = currentSolution;
                        bestEnergy = currentEnergy;
                    }
                }
            }

            temp *= ALPHA;
        }
        System.out.println(bestSolution);
        return bestSolution;
    }

    private static String generateInitialSolution() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // 前两个周期（0-2, 3-5索引）数字和为3
        for (int i = 0; i < 2; i++) {
            int[] group = new int[3];
            int sum = 0;
            while (sum < 3) {
                int idx = random.nextInt(3);
                if (group[idx] < 2) {
                    group[idx]++;
                    sum++;
                }
            }
            for (int value : group) {
                sb.append(value);
            }
        }

        // 后四个周期（6-8, 9-11, 12-14, 15-17索引）数字和为3或0
        for (int i = 0; i < 4; i++) {
            int[] group = new int[3];
            int sum = random.nextBoolean() ? 3 : 0;
            while (sum > 0) {
                int idx = random.nextInt(3);
                if (group[idx] < 2) {
                    group[idx]++;
                    sum--;
                }
            }
            for (int value : group) {
                sb.append(value);
            }
        }
        return sb.toString();

    }

    private static double calculateEnergy(String solution) {

        double energy = 0.0;

        return energy;
    }

    private static String generateNeighbor(String current, Random random) {
        char[] chars = current.toCharArray();

        int groupIndex = random.nextInt(6); // 选择一个周期（0-5）
        int startIndex = groupIndex * 3;   // 计算周期的起始索引

        // 如果是前两个周期，确保总和为3
        if (groupIndex < 2) {
            adjustGroupSum3(chars, startIndex, random);
        } else {
            // 对于后四个周期，总和可以为3或0
            boolean isSumThree = (chars[startIndex] - '0' + chars[startIndex + 1] - '0' + chars[startIndex + 2] - '0') > 0;
            if (isSumThree) {
                adjustGroupSum3(chars, startIndex, random);
            } else {
                // 如果当前总和为0，随机设置一个数字为1或2，其余为0
                Arrays.fill(chars, startIndex, startIndex + 3, '0');
                chars[startIndex + random.nextInt(3)] = (char) ('1' + random.nextInt(2));
            }
        }

        return new String(chars);
    }

    private static void adjustGroupSum3(char[] chars, int startIndex, Random random) {
        // 随机选择一个数字增加，另一个数字减少，以保持总和为3
        int increaseIndex = random.nextInt(3);
        int decreaseIndex = random.nextInt(3);
        while (decreaseIndex == increaseIndex) {
            decreaseIndex = random.nextInt(3);
        }

        if (chars[startIndex + decreaseIndex] > '0') {
            chars[startIndex + decreaseIndex]--;
        }

        if (chars[startIndex + increaseIndex] < '2') {
            chars[startIndex + increaseIndex]++;
        }
    }
    private static double acceptanceProbability(double energy, double newEnergy, double temperature) {
        if (newEnergy < energy) {
            return 1.0;
        }
        return Math.exp((energy - newEnergy) / temperature);
    }
}
