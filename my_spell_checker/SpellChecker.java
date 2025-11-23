package my_spell_checker;

import java.io.*;
import java.util.*;

public class SpellChecker {
    private HashSet<String> dictionary;
    private HashSet<String> shakespeareWords;
    private HashSet<String> misspelledWords;
    
    public SpellChecker() {
        dictionary = new HashSet<>();
        shakespeareWords = new HashSet<>();
        misspelledWords = new HashSet<>();
    }
    
    /**
     * Load dictionary words into HashSet
     */
    public void loadDictionary() {
        String absolutePath = "G:/Computational Thinking and Big Data/Eclipse/eclipse_workspace/my_spell_checker/src/my_spell_checker/words_alpha.txt";
        File file = new File(absolutePath);
        
        System.out.println("Loading dictionary from: " + file.getAbsolutePath());
        System.out.println("Dictionary file exists: " + file.exists());
        
        try {
            Scanner scanner = new Scanner(file);
            int wordCount = 0;
            
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim().toLowerCase();
                
                // Only add words longer than 1 character
                if (word.length() > 1) {
                    dictionary.add(word);
                    wordCount++;
                }
            }
            
            scanner.close();
            System.out.println("✅ Dictionary loaded: " + wordCount + " words");
            
        } catch (FileNotFoundException e) {
            System.out.println("❌ Dictionary file not found: " + e.getMessage());
        }
    }
    
    /**
     * Extract words from Shakespeare's works
     */
    public void extractShakespeareWords() {
        String absolutePath = "G:/Computational Thinking and Big Data/Eclipse/eclipse_workspace/my_spell_checker/src/my_spell_checker/pg100.txt";
        File file = new File(absolutePath);
        
        System.out.println("Loading Shakespeare from: " + file.getAbsolutePath());
        System.out.println("Shakespeare file exists: " + file.exists());
        
        try {
            Scanner scanner = new Scanner(file);
            int lineCount = 0;
            int wordCount = 0;
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineCount++;
                
                // Skip header lines (common in Project Gutenberg files)
                if (line.contains("*** START OF") || line.contains("Project Gutenberg")) {
                    continue;
                }
                // Stop at end of text
                if (line.contains("*** END OF")) {
                    break;
                }
                
                // Split by non-word characters as per requirements
                String[] words = line.split("\\W");
                
                for (String word : words) {
                    String cleanWord = word.trim().toLowerCase();
                    
                    // Only consider words longer than 1 character
                    if (cleanWord.length() > 1) {
                        shakespeareWords.add(cleanWord);
                        wordCount++;
                    }
                }
                
                // Progress indicator for large files
                if (lineCount % 5000 == 0) {
                    System.out.println("Processed " + lineCount + " lines...");
                }
            }
            
            scanner.close();
            System.out.println("✅ Shakespeare processed: " + lineCount + " lines, " + wordCount + " unique words");
            System.out.println("📊 Unique Shakespeare words in HashSet: " + shakespeareWords.size());
            
        } catch (FileNotFoundException e) {
            System.out.println("❌ Shakespeare file not found: " + e.getMessage());
        }
    }
    
    /**
     * Find misspelled words (words in Shakespeare but not in dictionary)
     */
    public void findMisspelledWords() {
        System.out.println("\n=== FINDING MISSPELLED WORDS ===");
        
        int checkedWords = 0;
        int totalWords = shakespeareWords.size();
        
        for (String word : shakespeareWords) {
            checkedWords++;
            if (!dictionary.contains(word)) {
                misspelledWords.add(word);
            }
            
            // Progress indicator
            if (checkedWords % 1000 == 0) {
                System.out.println("Checked " + checkedWords + "/" + totalWords + " words...");
            }
        }
        
        System.out.println("✅ Misspelled words found: " + misspelledWords.size());
        System.out.println("📈 Percentage misspelled: " + 
            String.format("%.2f", (double)misspelledWords.size() / shakespeareWords.size() * 100) + "%");
    }
    
    /**
     * Print statistics and answer questions
     */
    public void printStatistics() {
        System.out.println("\n=== STATISTICS ===");
        System.out.println("📚 Dictionary words: " + dictionary.size());
        System.out.println("🎭 Shakespeare unique words: " + shakespeareWords.size());
        System.out.println("❌ Misspelled words: " + misspelledWords.size());
        
        // Calculate percentages
        double misspelledPercent = (double)misspelledWords.size() / shakespeareWords.size() * 100;
        double correctPercent = 100 - misspelledPercent;
        
        System.out.println("✅ Correctly spelled: " + String.format("%.2f", correctPercent) + "%");
        System.out.println("❌ Misspelled: " + String.format("%.2f", misspelledPercent) + "%");
    }
    
    /**
     * Show examples of misspelled words
     */
    public void showMisspelledExamples() {
        System.out.println("\n=== EXAMPLES OF MISSPELLED WORDS ===");
        
        List<String> examples = new ArrayList<>(misspelledWords);
        Collections.sort(examples);
        
        System.out.println("First 30 misspelled words (alphabetically):");
        int count = 0;
        for (String word : examples) {
            if (count < 30) {
                System.out.print(word + " ");
                count++;
                if (count % 10 == 0) System.out.println(); // New line every 10 words
            } else {
                break;
            }
        }
        System.out.println();
    }
    
    /**
     * Analyze types of misspellings
     */
    public void analyzeMisspellings() {
        System.out.println("\n=== MISSPELLING ANALYSIS ===");
        
        Map<Integer, Integer> lengthDistribution = new HashMap<>();
        for (String word : misspelledWords) {
            int length = word.length();
            lengthDistribution.put(length, lengthDistribution.getOrDefault(length, 0) + 1);
        }
        
        System.out.println("Misspelled word length distribution:");
        for (int length = 2; length <= 20; length++) {
            int count = lengthDistribution.getOrDefault(length, 0);
            if (count > 0) {
                double percentage = (double)count / misspelledWords.size() * 100;
                System.out.println("  Length " + length + ": " + count + " words (" + String.format("%.1f", percentage) + "%)");
            }
        }
        
        // Find longest misspelled words
        List<String> longestWords = new ArrayList<>();
        int maxLength = 0;
        
        for (String word : misspelledWords) {
            if (word.length() > maxLength) {
                maxLength = word.length();
                longestWords.clear();
                longestWords.add(word);
            } else if (word.length() == maxLength) {
                longestWords.add(word);
            }
        }
        
        System.out.println("Longest misspelled words (" + maxLength + " letters): " + longestWords);
    }
    
    /**
     * Answer the specific questions from the activity
     */
    public void answerQuestions() {
        System.out.println("\n=== ANSWERS TO ACTIVITY QUESTIONS ===");
        
        // Question 1: How many unique words in Shakespeare?
        System.out.println("1. How many unique words are in Shakespeare's works?");
        System.out.println("   Answer: " + shakespeareWords.size() + " unique words");
        
        // Question 2: How many misspelled words?
        System.out.println("2. How many words are misspelled?");
        System.out.println("   Answer: " + misspelledWords.size() + " misspelled words");
        
        // Question 3: Percentage misspelled?
        double percentage = (double)misspelledWords.size() / shakespeareWords.size() * 100;
        System.out.println("3. What percentage of words are misspelled?");
        System.out.println("   Answer: " + String.format("%.2f", percentage) + "%");
        
        // Comparison with expected value
        System.out.println("\n📝 Note from instructions:");
        System.out.println("   Expected HashSet size: ~23,975 words");
        System.out.println("   Your HashSet size: " + shakespeareWords.size() + " words");
        System.out.println("   Difference: " + Math.abs(shakespeareWords.size() - 23975) + " words");
    }
    
    /**
     * Check if specific words are in dictionary (for testing)
     */
    public void testSpecificWords() {
        System.out.println("\n=== TESTING SPECIFIC WORDS ===");
        String[] testWords = {"thou", "thee", "hath", "doth", "art", "thy"};
        
        for (String word : testWords) {
            boolean inDictionary = dictionary.contains(word.toLowerCase());
            boolean inShakespeare = shakespeareWords.contains(word.toLowerCase());
            System.out.println("Word '" + word + "': Dictionary=" + inDictionary + ", Shakespeare=" + inShakespeare);
        }
    }
    
    /**
     * Main method - run the complete spell checker
     */
    public static void main(String[] args) {
        SpellChecker spellChecker = new SpellChecker();
        
        System.out.println("🎯 SHAKESPEARE SPELL CHECKER");
        System.out.println("============================\n");
        
        // Part 1: Load files
        System.out.println("📁 LOADING FILES...");
        spellChecker.loadDictionary();
        spellChecker.extractShakespeareWords();
        
        // Part 2: Find misspelled words
        System.out.println("\n🔍 CHECKING SPELLING...");
        spellChecker.findMisspelledWords();
        
        // Part 3: Answer questions
        System.out.println("\n📊 GENERATING RESULTS...");
        spellChecker.printStatistics();
        spellChecker.answerQuestions();
        
        // Additional analysis
        spellChecker.showMisspelledExamples();
        spellChecker.analyzeMisspellings();
        spellChecker.testSpecificWords();
        
        System.out.println("\n🎉 SPELL CHECK COMPLETE!");
    }
}
