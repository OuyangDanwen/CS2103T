import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
 * 5. exit: exit the program                                          exit
 * 
 * Note: 
 * 1. There is no specific "save" command in TextBuddy as automatic saving is done 
 * after every operation to prevent undesired loss of unsaved data due to unforeseen 
 * physical failure;
 * 2. A backup list is used to solely facilitate deletion operation which requires a 
 * rewriting of the text file with data from this list;
 * 3. All other operations except deletion are done on the text file directly.
 * 4. The lines of text in the text file are reordered after every deletion operation.
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
	private static final int PARAMETER_SIZE_FOR_CLEAR_OPERATION = 1;
	private static final int PARAMETER_SIZE_FOR_DELETE_OPERATION = 2;

	//List of common variables for easy access across the program
	protected static String fileName;
	protected static File file;
	// this list is used solely for the ease of deletion
	protected static ArrayList<String> backupListForEasyDeletion;
	protected static int COUNTER_FOR_WRITING_TO_FILE;
	private static Scanner scanner = new Scanner(System.in);

	//List of common messages used in TextBuddy
	private static final String MESSAGE_FOR_RESTORATION_OPTIONS = 
			"Press <y> to restore from this file or any other key "
					+ "to create a new file";
	private static final String MESSAGE_HEAD_FOR_SUCCESSFUL_CLEARING = 
			"All content deleted from ";
	private static final String MESSAGE_HEAD_FOR_ADDING_TEXT = "Added to ";
	private static final String MESSAGE_HEAD_FOR_SUCCESSFUL_DELETION = 
			"Deleted from ";
	private static final String MESSAGE_TAIL_FOR_WELCOME_MESSAGE = 
			" is ready for use";
	private static final String MESSAGE_TAIL_FOR_EMPTY_FILE = " is empty";
	private static final String MESSAGE_FOR_CORRECT_USAGE_OF_PROGRAM = 
			"Correct Usage: java TextBuddy <your choice of fileName>";
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
			System.out.println(MESSAGE_FOR_CORRECT_USAGE_OF_PROGRAM);
			System.exit(-1);
		}

	}

	private static void sendWelcomeMessageToUser(String fileName) {
		System.out.println(fileName 
				+ MESSAGE_TAIL_FOR_WELCOME_MESSAGE);
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

		if (commandType.equalsIgnoreCase("add")) {
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
		else {
			handleInvalidCommand(userCommand);
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
				backupListForEasyDeletion.add(line);
			}
			bufferedFileReader.close();
		} catch (IOException e) {
			System.out.println(ERROR_MESSAGE_FOR_CHECKING_RESTORATION_FAILURE);
			exit();
		}

		return backupListForEasyDeletion.size() != 0; //only possible when non-empty
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

		try {
			BufferedWriter bufferedFileWriter = new BufferedWriter(new 
					FileWriter(file,true));
			bufferedFileWriter.write(textToAdd);
			bufferedFileWriter.newLine();
			bufferedFileWriter.flush();
			bufferedFileWriter.close();
		} catch (IOException e) {
			System.out.println(ERROR_MESSAGE_FOR_WRITING_TO_FILE_FAILURE);
			exit();
		}

		COUNTER_FOR_WRITING_TO_FILE++;
		System.out.println(MESSAGE_HEAD_FOR_ADDING_TEXT + 
				fileName + ": \"" + originalText + "\"");
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
			updateFile();
			System.out.println(MESSAGE_HEAD_FOR_SUCCESSFUL_DELETION + 
					fileName + ": \"" + deletedText + "\"");
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
			System.out.println(fileName + MESSAGE_TAIL_FOR_EMPTY_FILE);
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
		System.out.println(MESSAGE_HEAD_FOR_SUCCESSFUL_CLEARING + fileName);
	}

	private static void handleInvalidCommand(String userCommand) {

		if (isEmptyCommand(userCommand)) {//empty command
			System.out.println(ERROR_MESSAGE_FOR_EMPTY_COMMAND);
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
	 * This method is called for every deletion operation. It rewrites
	 * the text file with data from the backup list with the specific
	 * line of text removed
	 */
	private static void updateFile() {

		clearFile();//clear the file for rewriting after deletion

		//rewriting the file with data from the backup list
		try {
			BufferedWriter bufferedFileWriter = new BufferedWriter(
					new FileWriter(file,true));
			for (int i = 0; i < backupListForEasyDeletion.size(); i++) {
				String textToTransfer = "" + (i + 1) + ". " 
						+ backupListForEasyDeletion.get(i);
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
		backupListForEasyDeletion.clear();//also clear the backup list while overwriting
		clearFile();
		System.out.println("Overwriting is successful with a new " 
				+ fileName + " created");
	}

	private static void resetCounter() {
		COUNTER_FOR_WRITING_TO_FILE = 
				backupListForEasyDeletion.size() + 1;
	}
}
