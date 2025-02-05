package backend.academy;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class HangmanGame {
    private static final int MAX_ATTEMPTS = 6; // Maximum number of attempts
    private static final int MAX_INPUT_ATTEMPTS = 10; // Maximum number of input attempts
    private static final int TOTAL_DIFFICULTY_LEVELS = 3; // The total number of difficulty levels
    private static final int TOTAL_CATEGORY_OPTIONS = 3; // Total number of categories
    private static final String HINT_LABEL = "Hint: "; // The constant for a hint

    private final WordBank wordBank; // The source of the words
    private final InputReader inputReader; // Reading user input
    private final OutputWriter outputWriter; // Displaying messages to the user
    private final HangmanDisplay display = new HangmanDisplay(); // Displaying the gallows
    private String hint; // The hint for the current word

    public HangmanGame(WordBank wordBank, InputReader inputReader, OutputWriter outputWriter) {
        this.wordBank = wordBank;
        this.inputReader = inputReader;
        this.outputWriter = outputWriter;
    }

    public void startGame() {
        try {
            // Выбор категории и уровня сложности
            Category category = selectCategory();
            Difficulty difficulty = selectDifficulty();

            // Выбор слова и подсказки
            Word selectedWord = wordBank.selectWord(category, difficulty);

            String word = selectedWord.getWord(); // The hidden word
            this.hint = selectedWord.getHint(); // Hint

            outputWriter.println(HINT_LABEL + hint); // Printing a hint
            playGame(word); // Начало игры
        } catch (IOException e) {
            outputWriter.println("An error occurred: " + e.getMessage()); // Error handling
        }
    }

    private Category selectCategory() throws IOException {
        outputWriter.println("Select a category or press Enter for random:");
        outputWriter.println("1. Animals");
        outputWriter.println("2. Fruits");
        outputWriter.println("3. Countries");

        String input = inputReader.readLine();
        if (input.isEmpty()) {
            return Category.getRandomCategory();
        }

        int choice = Integer.parseInt(input);
        return switch (choice) {
        case 1 -> Category.ANIMALS;
        case 2 -> Category.FRUITS;
        case TOTAL_CATEGORY_OPTIONS -> Category.COUNTRIES; // The constant TOTAL_CATEGORY_OPTIONS
        default -> Category.getRandomCategory();
        };
    }

    private Difficulty selectDifficulty() throws IOException {
        outputWriter.println("Select a difficulty level or press Enter for random:");
        outputWriter.println("1. Easy");
        outputWriter.println("2. Medium");
        outputWriter.println("3. Hard");

        String input = inputReader.readLine();
        if (input.isEmpty()) {
            return Difficulty.getRandomDifficulty();
        }

        int choice = Integer.parseInt(input);
        return switch (choice) {
        case 1 -> Difficulty.EASY;
        case 2 -> Difficulty.MEDIUM;
        case TOTAL_DIFFICULTY_LEVELS -> Difficulty.HARD; // The constant TOTAL_DIFFICULTY_LEVELS
        default -> Difficulty.getRandomDifficulty();
        };
    }

    private void playGame(String word) throws IOException {
        int attempts = 0;
        Set<Character> guessedLetters = new HashSet<>();

        outputWriter.println("The word has " + word.length() + " letters. You have " + MAX_ATTEMPTS + " attempts.");

        while (attempts < MAX_ATTEMPTS && !isWordGuessed(word, guessedLetters)) {
            display.updateDisplay(word, guessedLetters, outputWriter); // Updating the word display
            display.drawHangman(attempts, outputWriter); // Drawing the Gallows

            char guessedLetter = getValidLetter(guessedLetters); // getting a valid letter

            if (!word.contains(String.valueOf(guessedLetter))) {
                attempts++;
                outputWriter.println("Incorrect guess! Attempts left: " + (MAX_ATTEMPTS - attempts));
            } else {
                outputWriter.println("Correct guess!");
            }
            guessedLetters.add(guessedLetter);
        }

        display.updateDisplay(word, guessedLetters, outputWriter);
        display.drawHangman(attempts, outputWriter);

        if (isWordGuessed(word, guessedLetters)) {
            outputWriter.println("Congratulations! You guessed the word: " + word);
        } else {
            outputWriter.println("Sorry, you've been hanged. The word was: " + word);
        }
    }

    private char getValidLetter(Set<Character> guessedLetters) throws IOException {
        int inputAttempts = 0;
        while (inputAttempts < MAX_INPUT_ATTEMPTS) {
            outputWriter.print("Enter a letter (or type 'hint' for a hint): ");
            String input = inputReader.readLine().toLowerCase();

            if (input.equals("hint")) {
                outputWriter.println(HINT_LABEL + hint);
                continue;
            }

            if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
                outputWriter.println("Invalid input. Please enter a single letter.");
                continue;
            }

            char guessedLetter = input.charAt(0);
            if (guessedLetters.contains(guessedLetter)) {
                outputWriter.println("You've already guessed this letter. Try again.");
            } else {
                return guessedLetter;
            }
            inputAttempts++;
        }
        throw new IOException("Max input attempts reached");
    }

    private boolean isWordGuessed(String word, Set<Character> guessedLetters) {
        return word.chars().allMatch(c -> guessedLetters.contains((char) c));
    }
}
