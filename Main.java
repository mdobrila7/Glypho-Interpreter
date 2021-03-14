import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Deque;
import java.util.Scanner;
import java.util.Collections;
import java.math.BigInteger;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class Main {
    private static int radix;
    private static HashMap<Integer, Integer> lBraceIndexes = new HashMap<>();
    private static HashMap<Integer, Integer> rBraceIndexes = new HashMap<>();
    private static List<BigInteger> dequeue  = new LinkedList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) 
    {
        // deschid fisierul gly
        FileReader fr = null;
        BufferedReader bfr = null;
        try {
            fr = new FileReader(args[0]);
            bfr = new BufferedReader(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // verific daca primesc baza ca parametru
        if (args.length > 1) {
            radix = Integer.parseInt(args[1]);
        } else {
            radix = 10;
        }

        // citesc textul
        String text = null;
        try {
            text = bfr.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // filtrez textul citit ca sa contina doar caracterele permise
        StringBuilder filteredLine = new StringBuilder();
        for (char letter : text.toCharArray()) {
            if (letter >= 33 && letter <= 126) {
                filteredLine.append(letter);
            }
        }

        // sparg textul in instructiuni
        String code = new String(filteredLine);
        List<List<String>> oldCommands = new ArrayList<>();
        List<String> command = new ArrayList<>();
        for (char letter : code.toCharArray()) {
            String word = new String();
            word += letter;
            command.add(word);
            if (command.size() == 4) {
                oldCommands.add(command);
                command = new ArrayList<>();
            }
        }

        // daca numarul de simboluri nu e multiplu de 4 returnez eroare
        if (command.size() != 0) {
            exitProgram("Error", oldCommands.size(), -1);
        }

        // decodez fiecare instructiune
        List<List<String>> copy = new ArrayList<>(oldCommands);
        List<String> commands = new ArrayList<>();
        for (List<String> wordCopy : copy) {
            commands.add(transform(wordCopy));
        }

        // verific daca parantezele se inchid si se deschid cum trebuie
        Stack<Integer> stack = new Stack<>();
        int count = 0;
        int i;
        for (i = 0; i < commands.size(); i++) {
            String current = commands.get(i);
            if (current.equals("0110")) {
                stack.push(i);
                count++;
            } else if (current.equals("0123")) {
                if (count == 0) {
                    System.err.println("Error:" + i);
                    System.exit(-1);
                }

                // retin legaturile dintre paranteze in ambele sensuri cu 2 map-uri diferite
                lBraceIndexes.put(stack.peek(), i);
                rBraceIndexes.put(i, stack.peek());
                stack.pop();
                count--;
            }
        }
        if (count > 0) {
            exitProgram("Error", i, -1);
        }
        // System.out.println(commands);
        
        // execut comenzile
        i = 0;
        while (i < commands.size()) {
            i = execute(i, commands.get(i));
        }
        System.exit(0);
    }

    private static String transform(List<String> input) {
        StringBuilder res = new StringBuilder();
        int count = 0;
        HashMap<String, Integer> map = new HashMap<>();
        for (String word : input) {

            // numar de cate ori apare fiecare caracter, pastrand ordinea lor
            if (!map.containsKey(word)) {
                res.append(String.valueOf(count));
                map.put(word, count);
                count++;
            } else {
                res.append(map.get(word));
            }
        }
        return res.toString();
    }

    private static void exitProgram(String type, int index, int exitCode) {
        System.err.println(type + ":" + index);
        System.exit(exitCode);
    }

    private static int execute(int index, String command) {
        switch (command) {
            // NOP
            case "0000":
                break;
            
            // Input
            case "0001":
                String line = scanner.nextLine();
                String converted = null;
                try {
                    converted = (new BigInteger(line, radix)).toString(10); 
                } catch (NumberFormatException e) {
                    exitProgram("Exception", index, -2);
                }
                BigInteger number = new BigInteger(converted);
                dequeue.add(number);
                break;

            // Rot
            case "0010":
                if (dequeue.size() == 0) {
                    exitProgram("Exception", index, -2);
                }
                Collections.rotate(dequeue, 1);
                break;
            
            // Swap
            case "0011":
                if (dequeue.size() < 2) {
                    exitProgram("Exception", index, -2);
                }
                BigInteger temp = dequeue.get(dequeue.size() - 1);
                dequeue.set(dequeue.size() - 1, dequeue.get(dequeue.size() - 2));
                dequeue.set(dequeue.size() - 2, temp);
                break;

            // Push
            case "0012":
                dequeue.add(new BigInteger("1"));
                break;
            
            // RRot
            case "0100":
                if (dequeue.size() == 0) {
                    exitProgram("Exception", index, -2);
                }
                Collections.rotate(dequeue, -1);
                break;

            // Dup
            case "0101":
                if (dequeue.size() == 0) {
                    exitProgram("Exception", index, -2);
                }
                dequeue.add(dequeue.get(dequeue.size() - 1));
                break;

            // Add
            case "0102":
                if (dequeue.size() < 2) {
                    exitProgram("Exception", index, -2);
                }
                BigInteger sum = dequeue.get(dequeue.size() - 1);
                sum = sum.add(dequeue.get(dequeue.size() - 2));
                dequeue.set(dequeue.size() - 2, sum);
                dequeue.remove(dequeue.size() - 1);
                break;

            // L-brace
            case "0110":
                if (dequeue.size() == 0) {
                    exitProgram("Exception", index, -2);
                }
                if (dequeue.get(dequeue.size() - 1).compareTo(BigInteger.ZERO) == 0) {
                    index =  lBraceIndexes.get(index);
                }
                break;
            
            // Output
            case "0111":
                if (dequeue.size() == 0) {
                    exitProgram("Exception", index, -2);
                }
                System.out.println(dequeue.get(dequeue.size() - 1).toString(radix).toUpperCase());
                dequeue.remove(dequeue.size() - 1);
                break;

            // Multiply
            case "0112":
                if (dequeue.size() < 2) {
                    exitProgram("Exception", index, -2);
                }
                BigInteger mul = dequeue.get(dequeue.size() - 1);
                mul = mul.multiply(dequeue.get(dequeue.size() - 2));
                dequeue.set(dequeue.size() - 2, mul);
                dequeue.remove(dequeue.size() - 1);
                break;

            // Execute
            case "0120":
                if (dequeue.size() < 4) {
                    exitProgram("Exception", index, -2);
                }
                List<String> newCommand = new ArrayList<>();
                for (int j = 0; j < 4; j++) {
                    String word = dequeue.get(dequeue.size() - 1).toString();
                    newCommand.add(word);
                    dequeue.remove(dequeue.size() - 1);
                }
                String newWordCommand = transform(newCommand);
                if (!newWordCommand.equals("0110") && !newWordCommand.equals("0123")) {
                    execute(index, newWordCommand);
                } else {
                    exitProgram("Exception", index, -2);
                }
                break;

            // Negate
            case "0121":
                if (dequeue.size() == 0) {
                    exitProgram("Exception", index, -2);
                }
                number = dequeue.get(dequeue.size() - 1);
                number = number.negate();
                dequeue.set(dequeue.size() - 1, number);
                break;

            // Pop
            case "0122":
                if (dequeue.size() == 0) {
                    exitProgram("Exception", index, -2);
                }
                dequeue.remove(dequeue.size() - 1);
                break;

            // R-brace
            case "0123":
                index = rBraceIndexes.get(index) - 1;
                break;
        }
        
        return index + 1;
    }
}