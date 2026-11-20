import java.util.Scanner;
import java.util.ArrayList;
import javax.swing.*;
private static ArrayList<String> list = new ArrayList<>();
private static boolean needsToBeSaved = false;
private static boolean lastWasPrint = false;
private static String listName = "";

void main() {
    Scanner in = new Scanner(System.in);
    do {
        IO.println();
        if (!lastWasPrint) PrintList(false);
        else lastWasPrint = false; // reset

        String option = SafeInput.getRegExString(in,
                "A - add\t\tD - delete\tI - insert\tM - move\tO - open\nS - save\tC - clear\tV - view\tQ - quit ? ",
                "^(?i)a|d|i|m|o|s|c|v|q$",
                "A/D/I/M/O/S/C/V/Q");

        try {
            switch (option.toLowerCase()) {
                case "a" -> AddList(in);
                case "d" -> DeleteList(in);
                case "i" -> InsertList(in);
                case "m" -> MoveList(in);
                case "o" -> OpenList(in);
                case "s" -> SaveList(in);
                case "c" -> ClearList(in);
                case "v" -> PrintList(true);
                case "q" -> QuitList(in);
            }
        } catch (FileNotFoundException e) {
            IO.println("File not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } while (true);
}

private static void AddList(Scanner in) {
    String addStr = SafeInput.getNonZeroLenString(in, "What would you like to add? ");
    list.add(addStr);
    needsToBeSaved = true;
    IO.println("Added \"" + addStr + "\"");
}

private static void DeleteList(Scanner in) {
    if(list.isEmpty()) {
        IO.println("List is empty");
        return;
    }
    int location = SafeInput.getRangedInt(in, "What list item # would you like to delete? ",
            0, list.size()-1);
    if (list.get(location) == null || list.get(location).isBlank()) {
        IO.println("Index [" + location + "] is already empty");
    } else {
        list.remove(location);
        needsToBeSaved = true;
        IO.println("Deleted [" + location + "]");
    }
}

private static void InsertList(Scanner in) {
    String str = SafeInput.getNonZeroLenString(in, "WHAT would you like to insert? ");
    int location = SafeInput.getRangedInt(in,
            "WHERE # would you like to insert it? ",
            0, list.size());

    list.add(location, str);
    needsToBeSaved = true;
    System.out.printf("Inserted \"%s\" at [%d]\n", str, location);
}

private static void MoveList(Scanner in) {
    int locationDel = SafeInput.getRangedInt(in, "WHAT list item # would you like to move? ",
            0, list.size()-1);
    int moveLocation = SafeInput.getRangedInt(in, "WHERE # would you like to move it? ",
            0, list.size()-1);
    if (list.get(locationDel) == null || list.get(locationDel).isBlank())
        IO.println("Index [" + locationDel + "] is empty");
    else {
        list.add(moveLocation, list.remove(locationDel));
        needsToBeSaved = true;
        System.out.printf("Moved [%d] -> [%d]\n",  locationDel, moveLocation);
    }
}

private static void OpenList(Scanner in) throws IOException {
    if (needsToBeSaved)
        if(!SafeInput.getYNConfirm(in, "Your list is unsaved. Do you wish to continue? [y/n] ")) return;

    JFileChooser chooser = new JFileChooser();
    File workingDirectory = new File(System.getProperty("user.dir") + "\\src");
    chooser.setCurrentDirectory(workingDirectory);

    if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
        IO.println("No file selected");
        return;
    }

    File selectedFile = chooser.getSelectedFile();

    list.clear();
    listName = selectedFile.getName();
    BufferedReader br = new BufferedReader(new FileReader(selectedFile));
    String line;
    while ((line = br.readLine()) != null)
        list.add(line);

    needsToBeSaved = false; // list is already in storage
}

private static void SaveList(Scanner in) throws IOException {
    Path newFile = null;
    if (!listName.isEmpty()) {
        newFile = Path.of(System.getProperty("user.dir") + "\\src\\" + listName);
    } else {
        do {
            String fileName = SafeInput.getRegExString(in, "New list name? ",
                    "^[^.]*$", "not including file extension"); // adds .csv automatically, user does not need to add

            Path fileLocation = Path.of(System.getProperty("user.dir") + "\\src\\" + fileName + ".txt");

            if (Files.exists(fileLocation)) {
                boolean continueYN = SafeInput.getYNConfirm(in, "List already exists (" + fileLocation + ")\nDo you wish to overwrite? [y/n] ");
                if (!continueYN) return;
            }

            newFile = fileLocation;
        } while (newFile == null);
    }

    Files.write(newFile, list);
    listName = newFile.getFileName().toString();
    needsToBeSaved = false;
    IO.println("File written to: " + newFile.getFileName());
}

private static void ClearList(Scanner in) {
    if (needsToBeSaved)
        if(!SafeInput.getYNConfirm(in, "Your list is unsaved. Do you wish to continue? [y/n] ")) return;

    list.clear();
    needsToBeSaved = false;
    listName = "";
    IO.println("List cleared");
}

private static void PrintList(boolean called) {
    // called refers to if the function was called from a menu option (true) or from the loop (false)
    lastWasPrint = called;

    IO.println("---------------------------");
    if (!listName.isEmpty()) IO.println("> Name: " + listName);

    for (int i = 0; i < list.size(); i++) {
        if (i < 10) System.out.printf("[%d]\t\t%s\n", i, list.get(i)); // fix spacing
        else System.out.printf("[%d]\t%s\n", i, list.get(i));

    }
    if (list.isEmpty()) IO.println("(empty)");
    if (needsToBeSaved) IO.println("--------------------------- (unsaved)");
    else IO.println("--------------------------");
}

private static void QuitList(Scanner in) {
    if (needsToBeSaved)
        if (!SafeInput.getYNConfirm(in, "Your list is unsaved. Do you wish to continue? [y/n] ")) return;

    System.exit(0);
}