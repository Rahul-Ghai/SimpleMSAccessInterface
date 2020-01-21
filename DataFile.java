import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import net.ucanaccess.jdbc.UcanaccessSQLException;

public class DataFile {
	static Scanner option = new Scanner(System.in);
	static Connection connection;

	public static void main(String[] args) throws SQLException {
		connection = DriverManager.getConnection("jdbc:ucanaccess://C:/file/path/here/file.accdb");//file path connection to Microsoft Access File
		while (true)
			userInterface();
	}

	public static void userInterface() throws SQLException {
		System.out.println(
				"A. Create a Table\nB. Include an Entry\nC. Delete an entry/entries\nD. List a table\n1. Include a new"
						+ " book in the collection\n2. Include a new user\n3. List library users with books checked out\n4. Check out a book"
						+ "\n5. Return a book\n6. Exit");
		System.out.println("Select ");
		String menuOption = option.nextLine();
		switch (menuOption) {
		case "A":
			createTable();
			break;
		case "B":
			includeEntry();
			break;
		case "C":
			deleteEntry();
			break;
		case "D":
			ResultSet data = listTable();
			ResultSetMetaData table = data.getMetaData();
			int colNum = table.getColumnCount();
			for (int z = 1; z <= colNum; z++)
				System.out.print(String.format("%-22s", table.getColumnName(z)));
			System.out.println("");
			while (data.next()) {
				for (int i = 1; i <= colNum; i++) {
					String columnValue = data.getString(i);
					System.out.print(String.format("[%-20s]", columnValue));
				}
				System.out.println("");
			}
			break;
		case "1":
			includeNew();
			break;
		case "2":
			newUser();
			break;
		case "3":
			ResultSet data2 = listChecked();
			ArrayList<String> table2 = new ArrayList<String>();
			table2.add("Name");
			table2.add("Title");
			table2.add("Author");
			colNum = 3;
			for (int z = 1; z <= colNum; z++)
				System.out.print(String.format("%-22s", table2.get(z - 1)));
			System.out.println("");
			while (data2.next()) {
				for (int i = 1; i <= colNum; i++) {
					String columnValue = data2.getString(i);
					System.out.print(String.format("[%-20s]", columnValue));
				}
				System.out.println("");
			}
			break;
		case "4":
			checkBook();
			break;
		case "5":
			returnBook();
			break;
		case "6":
			exit();
			break;
		}
	}

	private static void exit() {// terminates the code
		System.exit(0);
	}

	private static void returnBook() throws SQLException {
		String queryBuilder = "DELETE FROM checkedOutBooks WHERE (";		
		System.out.println("SSN is?");
		String sS = option.nextLine();
		queryBuilder += "SSN = '"+sS+"' AND ";
		System.out.println("callNo is?");
		String cN = option.nextLine();
		queryBuilder += "callNo = '"+cN+"')";
		System.out.println(queryBuilder);
		
		String backgroundQuery = "SELECT * from checkedOutBooks WHERE SSN ='"+sS+"' AND callNo ='"+cN+"'";
		ResultSet temp = connection.createStatement().executeQuery(backgroundQuery);
		boolean check = temp.next();
		if(check)
			connection.createStatement().executeUpdate(queryBuilder);
		else
			System.err.println("This SSN/callNo combo is invalid");
	}

	private static void checkBook() throws SQLException {
		String cont;
		try {
		System.out.println("Enter the Social Security Number");
		String sS = option.nextLine();
		
		do {
			String queryBuilder = "INSERT INTO checkedOutBooks VALUES (";
			System.out.println("Enter the call number");
			String cN = option.nextLine();
			queryBuilder += "'" + sS + "', '" + cN + "');";
			connection.createStatement().executeUpdate(queryBuilder);
			System.out.println("Check out another book another book? y/n");
			cont = option.nextLine();
		} while (cont.equals("y"));
		}
		catch(UcanaccessSQLException e)
		{
			System.err.println("Error! Invalid SSN/callNo");
		}
	}

	private static ResultSet listChecked() throws SQLException {
		ResultSet tND = connection.createStatement().executeQuery("SELECT people.name, books.title, books.author from books inner join checkedOutBooks "
				+ "ON books.callNo = checkedOutBooks.callNo" + 
				" inner join  people ON checkedOutBooks.SSN = people.SSN; ");
		return tND;
	}

	private static void newUser() throws SQLException {
		String cont;
		do {
			String queryBuilder = "INSERT INTO people VALUES (";
			System.out.println("Enter the SSN");
			String sS = option.nextLine();
			System.out.println("Enter the Name");
			String uN = option.nextLine();
			queryBuilder += "'" + sS + "', '" + uN + "');";
			connection.createStatement().executeUpdate(queryBuilder);
			System.out.println("Enter another person? y/n");
			cont = option.nextLine();
		} while (cont.equals("y"));
	}

	private static void includeNew() throws SQLException {
		String cont;
		do {
			String queryBuilder = "INSERT INTO books VALUES (";
			System.out.println("Enter the Book Author");
			String bA = option.nextLine();
			System.out.println("Enter the Book Title");
			String bT = option.nextLine();
			System.out.println("Enter the Book callNo");
			String cN = option.nextLine();
			queryBuilder += "'" + bA + "', '" + bT + "', '" + cN + "');";
			connection.createStatement().executeUpdate(queryBuilder);
			System.out.println("Enter another book? y/n");
			cont = option.nextLine();
		} while (cont.equals("y"));
	}

	private static ResultSet listTable() throws SQLException {
		System.out.println("List which table?\n");
		String tN = option.nextLine();
		ResultSet tND = connection.createStatement().executeQuery("SELECT * FROM [" + tN + "]");
		return tND;
	}

	private static void deleteEntry() throws SQLException {
		String queryBuilder = "DELETE from ";
		System.out.println("Delete from table...?");
		String tN = option.nextLine();
		queryBuilder += tN + " where ";
		System.out.println("Column that the deletion key is in?");
		String cN = option.nextLine();
		queryBuilder += cN + " = ";
		System.out.println("Deleting which?");
		String dK = option.nextLine();
		queryBuilder += "'" + dK + "'";
		connection.createStatement().executeUpdate(queryBuilder);
	}

	private static void includeEntry() throws SQLException {
		String queryBuilder = "INSERT INTO ";
		System.out.println("Insert into table...?");
		String tN = option.nextLine();
		queryBuilder += tN + " VALUES (";
		ResultSet tempSet = connection.createStatement().executeQuery("SELECT * FROM [" + tN + "]");
		ResultSetMetaData tempMeta = tempSet.getMetaData();
		int columns = tempMeta.getColumnCount();
		String[] cN = new String[columns];
		for (int i = 1; i <= columns; i++) {
			cN[i - 1] = tempMeta.getColumnName(i);
		}
		String[] inputs = new String[columns];
		for (int i = 0; i < columns; i++) {
			System.out.println("Enter a value for " + cN[i] + ":\n");
			inputs[i] = option.nextLine();
		}
		for (int i = 0; i < columns; i++) {
			queryBuilder += "'" + inputs[i] + "'";
			if (i == (columns - 1)) {

			} else {
				queryBuilder += ", ";
			}
		}
		queryBuilder += ");";
		connection.createStatement().executeUpdate(queryBuilder);
	}

	private static void createTable() throws SQLException {
		String queryBuilder = "CREATE TABLE ";
		System.out.println("Enter table name");
		String check;
		queryBuilder += option.nextLine() + "(";
		do {
			System.out.println("Enter column name");
			String colName = option.nextLine();
			String type = "varchar(80)";// This assumes 80 characters is acceptable for all data columns
			queryBuilder += (colName + " " + type);
			System.out.println("Another column?\ny/n\n");
			check = option.nextLine();
		} while (check.equalsIgnoreCase("y") && (queryBuilder += ", ") != "");
		check = "";
		System.out.println("Is there a primary key for this table?\ny/n\n");
		check = option.nextLine();
		if (check.equals("y")) {
			System.out.println("It is the column...?\n");
			String pKey = option.nextLine();
			queryBuilder += ", PRIMARY KEY(" + pKey + ")";
		}

		System.out.println("Is there a foreign key for this table?\ny/n\n");
		check = option.nextLine();
		if (check.equals("y")) {
			do {
				System.out.println("The foreign key is used for column...?\n");
				String fKey = option.nextLine();
				queryBuilder += ", FOREIGN KEY(" + fKey + ")";
				System.out.println("Referenced Table?");
				String rT = option.nextLine();
				System.out.println("Referenced Column");
				String rC = option.nextLine();
				queryBuilder += " REFERENCES " + rT + " (" + rC + ")";
				System.out.println("Is there an additional foreign key for this table?\ny/n\n");
				check = option.nextLine();
			} while (check.equals("y"));
		}

		queryBuilder += ");";
		System.out.println(queryBuilder);
		connection.createStatement().execute(queryBuilder);// executed the constructed sql statement
	}
}
