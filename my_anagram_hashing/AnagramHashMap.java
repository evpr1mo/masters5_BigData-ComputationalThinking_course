package my_anagram_hashing;

import java.io.*;
import java.util.*;

public class AnagramHashMap {
    private HashMap<String, List<String>> anagramMap;
    
    public AnagramHashMap() {
        anagramMap = new HashMap<>();
    }
    
    /**
     * Sorts characters of a string (provided in the instructions)
     */
    public static String sortCharacters(String s) {
        char[] chArray = s.toCharArray();
        Arrays.sort(chArray);
        return new String(chArray);
    }
    
    /**
     * Reads dictionary file and builds the anagram map
     */
    public void buildAnagramMap(String filename) {
        // Абсолютний шлях до файлу
        String absolutePath = "G:/Computational Thinking and Big Data/Eclipse/eclipse_workspace/my_anagram_hashing/src/my_anagram_hashing/words_alpha.txt";
        File file = new File(absolutePath);
        
        System.out.println("Looking for file at: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        
        if (!file.exists()) {
            System.out.println("File not found! Using relative path as fallback...");
            file = new File(filename);
        }
        
        try {
            Scanner scanner = new Scanner(file);
            int wordCount = 0;
            int lineCount = 0;
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineCount++;
                
                // Split by non-word characters as per instructions
                String[] words = line.split("\\W");
                
                for (String word : words) {
                    // Skip empty strings
                    if (word.isEmpty()) {
                        continue;
                    }
                    
                    // Convert to lowercase for case-insensitive comparison
                    String lowercaseWord = word.toLowerCase();
                    String sortedKey = sortCharacters(lowercaseWord);
                    
                    // Add to HashMap
                    if (!anagramMap.containsKey(sortedKey)) {
                        anagramMap.put(sortedKey, new ArrayList<>());
                    }
                    
                    // Only add if not already in the list (avoid duplicates)
                    List<String> anagramList = anagramMap.get(sortedKey);
                    if (!anagramList.contains(lowercaseWord)) {
                        anagramList.add(lowercaseWord);
                        wordCount++;
                    }
                }
            }
            
            scanner.close();
            System.out.println("Processed " + lineCount + " lines and " + wordCount + " words");
            System.out.println("Created " + anagramMap.size() + " anagram groups");
            
        } catch (FileNotFoundException e) {
            System.out.println("Dictionary file not found: " + file.getAbsolutePath());
        }
    }
    
    /**
     * Gets all anagrams for a given word
     */
    public List<String> getAnagrams(String word) {
        String sortedKey = sortCharacters(word.toLowerCase());
        List<String> anagrams = anagramMap.getOrDefault(sortedKey, new ArrayList<>());
        
        // Remove the word itself from the list if present
        List<String> result = new ArrayList<>(anagrams);
        result.remove(word.toLowerCase());
        
        return result;
    }
    
    /**
     * Finds the largest anagram group (most anagrams for one key)
     */
    public void findLargestAnagramGroup() {
        String largestKey = null;
        int maxSize = 0;
        List<String> largestGroup = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : anagramMap.entrySet()) {
            if (entry.getValue().size() > maxSize) {
                maxSize = entry.getValue().size();
                largestKey = entry.getKey();
                largestGroup = new ArrayList<>(entry.getValue());
            }
        }
        
        if (largestKey != null) {
            System.out.println("\n=== LARGEST ANAGRAM GROUP ===");
            System.out.println("Sorted key: " + largestKey);
            System.out.println("Number of anagrams: " + maxSize);
            System.out.println("Anagrams: " + largestGroup);
            
            // Show first 10 anagrams if the list is long
            if (maxSize > 10) {
                System.out.println("First 10 anagrams: " + largestGroup.subList(0, 10));
            }
        }
    }
    
    /**
     * Prints statistics about the anagram map
     */
    public void printStatistics() {
        System.out.println("\n=== ANAGRAM STATISTICS ===");
        System.out.println("Total anagram groups: " + anagramMap.size());
        
        // Count groups by size
        Map<Integer, Integer> sizeDistribution = new HashMap<>();
        int totalWords = 0;
        
        for (List<String> group : anagramMap.values()) {
            int size = group.size();
            sizeDistribution.put(size, sizeDistribution.getOrDefault(size, 0) + 1);
            totalWords += size;
        }
        
        System.out.println("Total words processed: " + totalWords);
        System.out.println("Average group size: " + String.format("%.2f", (double)totalWords / anagramMap.size()));
        
        System.out.println("\nGroup size distribution:");
        for (int size = 1; size <= 10; size++) {
            int count = sizeDistribution.getOrDefault(size, 0);
            System.out.println("  Size " + size + ": " + count + " groups");
        }
        
        // Find groups larger than 10
        int largeGroups = 0;
        for (int size : sizeDistribution.keySet()) {
            if (size > 10) {
                largeGroups += sizeDistribution.get(size);
            }
        }
        System.out.println("Groups larger than 10: " + largeGroups);
    }
    
    /**
     * Part 4: Answer questions by testing specific words
     */
    public void answerQuestions() {
        System.out.println("\n=== ANAGRAM EXAMPLES ===");
        
        // Test some common words
        String[] testWords = {"listen", "silent", "elvis", "lives", "earth", "heart", "fried", "fired"};
        
        for (String word : testWords) {
            List<String> anagrams = getAnagrams(word);
            String sortedKey = sortCharacters(word.toLowerCase());
            System.out.println("Word: '" + word + "' -> Key: '" + sortedKey + "'");
            System.out.println("  Anagrams: " + anagrams);
        }
    }
    
    /**
     * Find words with no anagrams (groups of size 1)
     */
    public void findWordsWithNoAnagrams() {
        int noAnagramCount = 0;
        System.out.println("\n=== WORDS WITH NO ANAGRAMS ===");
        
        for (Map.Entry<String, List<String>> entry : anagramMap.entrySet()) {
            if (entry.getValue().size() == 1) {
                noAnagramCount++;
            }
        }
        
        System.out.println("Words with no anagrams: " + noAnagramCount);
        
        // Show some examples
        System.out.println("Examples of words with no anagrams:");
        int examplesShown = 0;
        for (Map.Entry<String, List<String>> entry : anagramMap.entrySet()) {
            if (entry.getValue().size() == 1 && examplesShown < 10) {
                System.out.println("  " + entry.getValue().get(0));
                examplesShown++;
            }
        }
    }
    
    /**
     * Main method to run the complete analysis
     */
    public static void main(String[] args) {
        AnagramHashMap anagramFinder = new AnagramHashMap();
        
        // Build the anagram map
        anagramFinder.buildAnagramMap("words_alpha.txt");
        
        // Print statistics
        anagramFinder.printStatistics();
        
        // Answer specific questions
        anagramFinder.answerQuestions();
        
        // Find largest anagram group
        anagramFinder.findLargestAnagramGroup();
        
        // Find words with no anagrams
        anagramFinder.findWordsWithNoAnagrams();
    }
}