import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Welcome to TextBuddy !
 * 
 * This class allows the user to store text, delete text, display text and clear text on a ".txt" file.
 * It supports the following command-line operations:
 *            Functionalities                                             Command format 
 * 1. add: add a specific line to the file                            add <specific text>
 * 2. delete: delete a specific line from the file                    delete <specific line number>
 * 3. display: display all texts in the file line by line             display
 * 4. clear: clear all texts in the file                              clear
 * 5. sort: case-insensitively sort all texts alphabetically          sort
 * 6. search: case-insensitively search for a keyword in the file     search <keyword>
 * 7. exit: exit the program                                          exit
 * 
 * Note: 
 * 1. There is no specific "save" command in TextBuddy as automatic saving is done 
 * after every operation to prevent undesired loss of unsaved data due to unforeseen 
 * physical failure;
 * 2. The lines of text in the text file are reordered after every deletion operation.
 * 
 * Additional Features: 
 * 1. TextBuddy re-prompts command from user instead of exiting the program immediately 
 * if user enters invalid command, and only exits on operation failures with 
 * error-feedback displayed to user;
 * 2. TextBuddy auto-detects available past file for restoration before creating a 
 * new text file, so user can decide whether to restore the old file or 
 * create a new text file which will overwrite the old one.
 * 
 * @author Ouyang Danwen
 *
 */

public class TextBuddy {

	//List of operation-command-format restrictions that user have to abide by
	private static final int ARGUMENT_SIZE_FOR_INITIALIZATION = 1;
	private static final int PARAMETER_SIZE_FOR_DISPLAY_OPERATION = 1;
	private static final int PARAMETER_SIZE_FOR_EXIT_OPERATION = 1;
	private static final int PARAMETER_SIZE_FOR_SORT_OPERATION = 1;
	private static final int PARAMETER_SIZE_FOR_CLEAR_OPERATION = 1;
	private static final int PARAMETER_SIZE_FOR_DELETE_OPERATION = 2;
	private static final int MINIMUM_PARAMETER_SIZE_FOR_SORT_OPERATION = 2;
	private static final int MINIMUM_PARAMETER_SIZE_FOR_ADD_OPERATION = 2;

	//List of common variables for easy access across the program
	private static String fileName;
	private static File file;
	// this list is used solely for the ease of deletion
	private static ArrayList<String> backupListForEasyDeletion;
	private static int COUNTER_FOR_WRITING_TO_FILE;
	private static Scanner scanner = new Scanner(System.in);

	//List of common messages used in TextBuddy
	private static final String MESSAGE_FOR_RESTORATION_OPTIONS = 
			"Press <y> to restore from this file or any other key "
					+ "to create a new file";
	private static final String MESSAGE_FOR_SUCCESSFUL_CLEARING = 
			"All content deleted from %s";
	private static final String MESSAGE_FOR_SORTING_TEXT = "%s sorted alphabetically";
	private static final String MESSAGE_FOR_ADDING_TO_TEXT = "Added to %s: \"%s\"";
	private static final String MESSAGE_FOR_SUCCESSFUL_DELETION = 
			"Deleted from %s: \"%s\"";
	private static final String MESSAGE_FOR_WELCOMING_USER = 
			"Welcome to TextBuddy! %s is ready for use";
	private static final String MESSAGE_FOR_EXACT_MATCH = 
			"Exact matches from %s:";
	private static final String MESSAGE_FOR_PARTIAL_MATCH = 
			"Partial matches from %s:";
	private static final String MESSAGE_FOR_EMPTY_RESULT = "Nil";
	private static final String ERROR_MESSAFGE_FOR_EMPTY_FILE = "%s is empty";
	private static final String ERROR_MESSAGE_FOR_INCORRECT_USAGE_OF_PROGRAM = 
			"Correct Usage: java TextBuddy <your choice of fileName>";
	private static final String ERROR_MESSAGE_FOR_EMPTY_KEYWORD =
			"Search keyword cannot be empty";
	private static final String ERROR_MESSAGE_FOR_ADD_NOTHING = "Must add a non-empty word";
	private static final String ERROR_MESSAGE_FOR_INVALID_COMMAND = 
			"Invalid command, please try again";
	private static final String ERROR_MESSAGE_FOR_EMPTY_COMMAND = 
			"Please enter a non-empty command";
	private static final String ERROR_MESSAGE_FOR_INVALID_LINE_NUMBER_FOR_DELETION = 
			"Invalid line number for deletion, please try again";
	private static final String ERROR_MESSAGE_FOR_INVALID_NUMBER_OF_AGRUMENTS = 
			"Invalid number of arguments";
	private static final String ERROR_MESSAGE_FOR_FILE_CREATION_FAILURE = 
			"Failed to create file";
	private static final String ERROR_MESSAGE_FOR_WRITING_TO_FILE_FAILURE = 
			"Failed to write to file";
	private static final String ERROR_MESSAGE_FOR_READING_FROM_FILE_FAILURE = 
			"Failed to read from file";
	private static final String ERROR_MESSAGE_FOR_CLEARING_FILE_FAILURE = 
			"Failed to clear file";
	private static final String ERROR_MESSAGE_FOR_UPDATING_FILE_FROM_DELETION_LIST = 
			"Failed to update file from deletion list";
	private static final String ERROR_MESSAGE_FOR_CHECKING_RESTORATION_FAILURE = 
			"Failed to check for past files for restoration";
	private static final String ERROR_MESSAGE_FOR_DELETION_ON_EMPTY_FILE = 
			"Cannot delete from empty file, please try again";

	/**
	 * This is a high-level method which shows the work-flow of the whole program.
	 * It deals with user commands by calling the four main methods inside.
	 */
	public static void main(String[] args) {

		exitIfIncorrectArguments(args);
		setUpEnvironment(args[0]);
		sendWelcomeMessageToUser(fileName);
		handleUserCommand();

	}

	private static void exitIfIncorrectArguments(String[] arguments) {
		if (arguments.length != ARGUMENT_SIZE_FOR_INITIALIZATION) {
			System.out.println(
					ERROR_MESSAGE_FOR_INVALID_NUMBER_OF_AGRUMENTS);
			System.out.println(ERROR_MESSAGE_FOR_INCORRECT_USAGE_OF_PROGRAM);
			System.exit(-1);
		}

	}

	private static void sendWelcomeMessageToUser(String fileName) {
		System.out.println
		(String.format(MESSAGE_FOR_WELCOMING_USER, fileName));
	}

	/**
	 * This method initializes the file environment for user operations.
	 * It also detects any past file with the same name in the current 
	 * directory and allows user to choose from restoring data from the 
	 * old text file or creating a new text file which overwrites the
	 * old one.
	 */
	private static void setUpEnvironment(String name) {
		fileName = name;
		file = new File(fileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			System.out.println(
					ERROR_MESSAGE_FOR_FILE_CREATION_FAILURE);
			System.exit(1);
		}

		backupListForEasyDeletion = new ArrayList<String>();
		boolean canRestore = isPossibleForRestoration();
		//user can choose whether to restore
		if (canRestore) {
			askUserForRestorationDecisionAndExecute();
		}
		resetCounter();

	}

	/**
	 * This methods handles all inputs from user by 
	 * repeatedly prompting and accepting command 
	 * from user.
	 */
	private static void handleUserCommand() {
		while (true) {
			String userCommand = scanner.nextLine();
			String commandType = getCommandType(
					userCommand);
			executeUserCommand(commandType,userCommand);
		}
	}

	private static boolean isEmptyCommand(String userCommand) {
		return userCommand.isEmpty();
	}

	/**
	 * This method extracts and returns the command type, i.e. 
	 * the first word from each user command
	 */
	private static String getCommandType(String userCommand) {
		String[] parametersFromUserCommand = userCommand.split(" ");
		String commandType = parametersFromUserCommand[0];
		return commandType;
	}

	/**
	 * This method determines the type and checks the validity of every user command,
	 * and handles it accordingly.
	 * There are only five valid commands: add, delete, display, clear, exit.
	 * The commands are not case-sensitive;
	 */
	private static void executeUserCommand(String commandType, String userCommand) {

		if (commandType.equalsIgnoreCase("add") &&
				getNumberOfParameters(userCommand)
				>= MINIMUM_PARAMETER_SIZE_FOR_ADD_OPERATION) {
			addText(userCommand);
		}
		else if (commandType.equalsIgnoreCase("delete") && 
				getNumberOfParameters(userCommand)== 
				PARAMETER_SIZE_FOR_DELETE_OPERATION) {
			deleteText(userCommand);
		}
		else if (commandType.equalsIgnoreCase("display") && 
				getNumberOfParameters(userCommand)
				== PARAMETER_SIZE_FOR_DISPLAY_OPERATION) {
			displayText();
		}
		else if (commandType.equalsIgnoreCase("clear") && 
				getNumberOfParameters(userCommand)
				== PARAMETER_SIZE_FOR_CLEAR_OPERATION) {
			clearText();
		}
		else if (commandType.equalsIgnoreCase("exit") && 
				getNumberOfParameters(userCommand)
				== PARAMETER_SIZE_FOR_EXIT_OPERATION) {	
			exit();
		}
		else if (commandType.equalsIgnoreCase("sort") &&
				getNumberOfParameters(userCommand)
				== PARAMETER_SIZE_FOR_SORT_OPERATION) {
			sort();

		}
		else if (commandType.equalsIgnoreCase("search") &&
				getNumberOfParameters(userCommand)
				>= MINIMUM_PARAMETER_SIZE_FOR_SORT_OPERATION) {
			search(userCommand);
		}
		else {
			handleInvalidCommand(userCommand,commandType);
		}
	}

	/**
	 * This method determines and returns the number of parameters 
	 * in the user command for further checking
	 */
	private static int getNumberOfParameters(String userCommand) {
		String[] segments = userCommand.split(" ");
		int numberOfParameters = segments.length;
		return numberOfParameters;
	}

	/**
	 * This method restores the data from a past file ,if a 
	 * file with the same name is found.
	 * The decision whether to restore or create a new file
	 * is at sole discretion of the user.
	 */
	private static boolean isPossibleForRestoration() {

		String line;
		try {
			BufferedReader bufferedFileReader = new BufferedReader(
					new FileReader(file));
			while ((line = bufferedFileReader.readLine()) != null) {
				//counter is not added to the list for consistency
				backupListForEasyDeletion.add(getOriginalText(line));
			}
			bufferedFileReader.close();
		} catch (IOException e) {
			System.out.println(ERROR_MESSAGE_FOR_CHECKING_RESTORATION_FAILURE);
			exit();
		}

		return backupListForEasyDeletion.size() != 0; //only possible when non-empty
	}

	private static String getOriginalText(String line) {
		int index = line.indexOf(".");
		return line.substring(index + 2);
	}

	private static void askUserForRestorationDecisionAndExecute() {

		System.out.println("A past file named " + fileName + " is found");
		System.out.println(MESSAGE_FOR_RESTORATION_OPTIONS);
		String userChoice = scanner.nextLine();
		if (!userChoice.equalsIgnoreCase("y")) {
			overwritesPastFile();//overwrite the past file with a new empty file
		}
	}

	private static void addText(String userCommand) {

		String originalText = retrieveTextFromCommand(userCommand);
		String textToAdd = COUNTER_FOR_WRITING_TO_FILE + ". " 
				+ originalText;
		//text is also added to the backup list
		backupListForEasyDeletion.add(originalText);
		writeToFileLineByLine(textToAdd);

		COUNTER_FOR_WRITING_TO_FILE++;
		System.out.println(String.format(MESSAGE_FOR_ADDING_TO_TEXT,
				fileName,originalText));
	}

	private static void writeToFileLineByLine(String text) {
		try {
			BufferedWriter bufferedFileWriter = new BufferedWriter(new 
					FileWriter(file,true));
			bufferedFileWriter.write(text);
			bufferedFileWriter.newLine();
			bufferedFileWriter.flush();
			bufferedFileWriter.close();
		} catch (IOException e) {
			System.out.println(ERROR_MESSAGE_FOR_WRITING_TO_FILE_FAILURE);
			exit();
		}
	}

	private static void deleteText(String userCommand) {

		int lineNumberToDelete = Integer.parseInt(
				retrieveTextFromCommand(userCommand));

		if (isFileEmpty()) {//cannot delete from empty file
			System.out.println(
					ERROR_MESSAGE_FOR_DELETION_ON_EMPTY_FILE);
		}
		//the line number must be valid
		else if (isPossibleToDelete(lineNumberToDelete)) {
			//the index of the backup list starts from 0
			int listEntryToDelete = lineNumberToDelete - 1;
			String deletedText = updateBackupList(listEntryToDelete);
			COUNTER_FOR_WRITING_TO_FILE--;
			updateFile(backupListForEasyDeletion);
			System.out.println(String.format(
					MESSAGE_FOR_SUCCESSFUL_DELETION, fileName,deletedText) );
		}
		else {
			System.out.println(
					ERROR_MESSAGE_FOR_INVALID_LINE_NUMBER_FOR_DELETION);
		}

	}

	/**
	 * This method displays all texts in the file line by line, if the
	 * file is not empty.
	 * Otherwise, it informs the user that the file is empty.
	 */
	private static void displayText() {

		if (isFileEmpty()) {
			System.out.println(String.format(ERROR_MESSAFGE_FOR_EMPTY_FILE, fileName));
			return;
		}

		//the file is not empty
		String line;
		try {
			BufferedReader bufferedFileReader = new BufferedReader(new 
					FileReader(file));
			while ((line = bufferedFileReader.readLine()) != null) {
				System.out.println(line);
			}
			bufferedFileReader.close();
		} catch (IOException e) {
			System.out.println(ERROR_MESSAGE_FOR_READING_FROM_FILE_FAILURE);
			exit();
		}
	}

	private static void clearText() {
		clearFile();
		backupListForEasyDeletion.clear();
		System.out.println(String.format(MESSAGE_FOR_SUCCESSFUL_CLEARING,fileName));
	}

	/**
	 * This method sorts the text file alphabetically.
	 * Sorting is not case-insensitive.
	 */
	private static void sort() {
		sort(backupListForEasyDeletion);
		System.out.println(String.format(MESSAGE_FOR_SORTING_TEXT, fileName));
	}

	public static ArrayList<String> sort(ArrayList<String> list) {
		Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
		//updateFile(list);//sorted text must be written to file
		return list;
	}

	/**
	 * This method searches a user-specified keyword
	 * in the text file.
	 * Search is case-insensitive.
	 * Both exact matches and partial matches are included
	 * in the result.
	 */
	private static void search(String userCommand) {
		if (isFileEmpty()) {
			System.out.println(String.format(ERROR_MESSAFGE_FOR_EMPTY_FILE, fileName));
			return;
		}

		else {
			String keyword = retrieveTextFromCommand(userCommand);
			//two lists for storing search results
			ArrayList<String> exactMatchList = getExactMatch
					(backupListForEasyDeletion, keyword);
			ArrayList<String> partialMatchList = getPartialMatch
					(backupListForEasyDeletion, keyword);
			displaySearchResult(exactMatchList,partialMatchList);
			//clear the contents after displaying the result
			exactMatchList.clear();
			partialMatchList.clear();
		}

	}

	private static ArrayList<String> getExactMatch(ArrayList<String> data,
			String keyword) {
		ArrayList<String> exactMatchList = new ArrayList<String>();
		for (int i = 0; i < data.size(); i++) {
			String line = data.get(i);
			//search for exact match
			if (isLineContainingExactKeyword(line,keyword)) {
				exactMatchList.add(line);
			}
		}

		return exactMatchList;	
	}

	private static ArrayList<String> getPartialMatch(ArrayList<String> data,
			String keyword) {
		ArrayList<String> partialMatchList = new ArrayList<String>();
		for (int i = 0; i < data.size(); i++) {
			String line = data.get(i);
			//search for partial match
			if (isLineContainingPartialKeyword(line,keyword)) {
				partialMatchList.add(line);
			}
		}

		return partialMatchList;
	}

	private static boolean isLineContainingExactKeyword(String line, 
			String keyword) {
		String[] segments = line.split(" ");

		for (int i = 0; i < segments.length; i++) {
			if (segments[i].equalsIgnoreCase(keyword)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isLineContainingPartialKeyword(String line, 
			String keyword) {
		String[] segments = line.split(" ");

		for (int i = 0; i < segments.length; i++) {
			if (segments[i].toLowerCase().contains(keyword.toLowerCase()) &&
					segments[i].length() > keyword.length()) {
				return true;
			}
		}

		return false;
	}

	private static void displaySearchResult(ArrayList<String> exactMatchList, 
			ArrayList<String> partialMatchList) {
		System.out.println(String.format(MESSAGE_FOR_EXACT_MATCH, fileName));
		printSearchResult(exactMatchList);
		System.out.println();
		System.out.println(String.format(MESSAGE_FOR_PARTIAL_MATCH, fileName));
		printSearchResult(partialMatchList);
	}

	private static void printSearchResult(ArrayList<String> result) {
		if (result.isEmpty()) {
			System.out.println(MESSAGE_FOR_EMPTY_RESULT);
		}
		else {
			for (int i = 0; i < result.size(); i++) {
				System.out.println(result.get(i));
			}
		}
	}

	private static void handleInvalidCommand(String userCommand, String commandType) {

		if (isEmptyCommand(userCommand)) {//empty command
			System.out.println(ERROR_MESSAGE_FOR_EMPTY_COMMAND);
		}
		//search keyword is empty
		else if (commandType.equalsIgnoreCase("search")) {
			System.out.println(ERROR_MESSAGE_FOR_EMPTY_KEYWORD);
		}
		//add an empty word
		else if (commandType.equalsIgnoreCase("add")) {
			System.out.println(ERROR_MESSAGE_FOR_ADD_NOTHING);
		}
		else {//other invalid command
			System.out.println(ERROR_MESSAGE_FOR_INVALID_COMMAND);
		}
	}

	private static void exit() {
		scanner.close();
		System.exit(-1);
	}

	private static boolean isFileEmpty() {
		//the file is empty when the back up list empty
		return backupListForEasyDeletion.size() == 0;
	}

	/**
	 * This method extracts and returns the real text from the user command 
	 * if applicable.
	 * The operation name is discarded.
	 */
	private static String retrieveTextFromCommand(String userCommand) {

		String segments[] = userCommand.split(" ");
		int numberOfSegments = segments.length;

		String text = "";
		for (int i = 1; i < numberOfSegments; i++) {
			text += " " + segments[i];
		}

		return text.trim();
	}

	private static void clearFile() {

		file.delete(); 
		file = new File(fileName); 
		try {
			file.createNewFile();
		} catch (IOException e) {
			System.out.println(ERROR_MESSAGE_FOR_CLEARING_FILE_FAILURE);
			exit();
		}
		resetCounter();
	}

	private static boolean isPossibleToDelete (int lineNumberToDelete) {
		//deletion is only possible with positive integer line number 
		//which are within the size of the size of the back up list
		return lineNumberToDelete <= backupListForEasyDeletion.size() 
				&& lineNumberToDelete >= 1;
	}

	private static String updateBackupList(int listEntryToDelete) {
		//remove the user-specified entry from the list
		return backupListForEasyDeletion.remove(listEntryToDelete);
	}

	/**
	 * This method is called for every deletion or sort operation. It rewrites
	 * the text file with data from the backup list with the specific
	 * line of text removed or after sorting 
	 */
	private static void updateFile(ArrayList<String> list) {

		clearFile();//clear the file for rewriting after deletion or sorting

		//rewriting the file with data from the backup list
		try {
			BufferedWriter bufferedFileWriter = new BufferedWriter(
					new FileWriter(file,true));
			for (int i = 0; i < list.size(); i++) {
				String textToTransfer = "" + (i + 1) + ". " 
						+ list.get(i);
				bufferedFileWriter.write(textToTransfer);
				bufferedFileWriter.newLine();
				bufferedFileWriter.flush();
			}
			bufferedFileWriter.close();
		} catch (IOException e) {
			System.out.println(
					ERROR_MESSAGE_FOR_UPDATING_FILE_FROM_DELETION_LIST);
			exit();
		}
	}

	private static void overwritesPastFile() {
		//also clear the backup list while overwriting
		backupListForEasyDeletion.clear();
		clearFile();
		System.out.println("Overwriting is successful with a new " 
				+ fileName + " created");
	}

	private static void resetCounter() {
		COUNTER_FOR_WRITING_TO_FILE = 
				backupListForEasyDeletion.size() + 1;
	}
}
