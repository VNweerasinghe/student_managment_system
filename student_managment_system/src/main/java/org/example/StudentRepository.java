package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {
	private final String jdbcUrl;

	public StudentRepository() throws SQLException, IOException {
		this(Paths.get(System.getProperty("user.home"), ".student-management"));
	}

	StudentRepository(Path dataDirectory) throws SQLException, IOException {
		Files.createDirectories(dataDirectory);
		Path databasePath = dataDirectory.resolve("students.db");
		jdbcUrl = "jdbc:sqlite:" + databasePath;
		initializeDatabase();
	}

	private Connection connect() throws SQLException {
		return DriverManager.getConnection(jdbcUrl);
	}

	private void initializeDatabase() throws SQLException {
		boolean tableExists;
		String tableLookup = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'students'";
		try (Connection connection = connect();
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery(tableLookup)) {
			tableExists = result.next();
		}

		String createTable = "CREATE TABLE IF NOT EXISTS students ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "student_number TEXT NOT NULL UNIQUE, "
				+ "full_name TEXT NOT NULL, "
				+ "email TEXT NOT NULL, "
				+ "phone TEXT NOT NULL, "
				+ "course TEXT NOT NULL, "
				+ "status TEXT NOT NULL)";
		try (Connection connection = connect();
				Statement statement = connection.createStatement()) {
			statement.execute(createTable);
			if (!tableExists) {
				seedExamples(connection);
			}
		}
	}

	private void seedExamples(Connection connection) throws SQLException {
		String sql = "INSERT INTO students "
				+ "(student_number, full_name, email, phone, course, status) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			insertExample(statement, "STU-1001", "Ava Patel", "ava.patel@example.com",
					"555-0101", "Computer Science", "Enrolled");
			insertExample(statement, "STU-1002", "Noah Kim", "noah.kim@example.com",
					"555-0102", "Business", "Enrolled");
			insertExample(statement, "STU-1003", "Mia Garcia", "mia.garcia@example.com",
					"555-0103", "Data Science", "Graduated");
		}
	}

	private void insertExample(PreparedStatement statement, String number, String name, String email,
			String phone, String course, String status) throws SQLException {
		statement.setString(1, number);
		statement.setString(2, name);
		statement.setString(3, email);
		statement.setString(4, phone);
		statement.setString(5, course);
		statement.setString(6, status);
		statement.executeUpdate();
	}

	public List<Student> findAll() throws SQLException {
		List<Student> students = new ArrayList<>();
		String sql = "SELECT id, student_number, full_name, email, phone, course, status "
				+ "FROM students ORDER BY full_name COLLATE NOCASE";
		try (Connection connection = connect();
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery(sql)) {
			while (result.next()) {
				students.add(mapStudent(result));
			}
		}
		return students;
	}

	public void insert(Student student) throws SQLException {
		String sql = "INSERT INTO students "
				+ "(student_number, full_name, email, phone, course, status) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection connection = connect();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			bindStudent(statement, student);
			statement.executeUpdate();
		}
	}

	public void update(Student student) throws SQLException {
		String sql = "UPDATE students SET student_number = ?, full_name = ?, email = ?, "
				+ "phone = ?, course = ?, status = ? WHERE id = ?";
		try (Connection connection = connect();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			bindStudent(statement, student);
			statement.setInt(7, student.getId());
			statement.executeUpdate();
		}
	}

	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM students WHERE id = ?";
		try (Connection connection = connect();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, id);
			statement.executeUpdate();
		}
	}

	private void bindStudent(PreparedStatement statement, Student student) throws SQLException {
		statement.setString(1, student.getStudentNumber());
		statement.setString(2, student.getFullName());
		statement.setString(3, student.getEmail());
		statement.setString(4, student.getPhone());
		statement.setString(5, student.getCourse());
		statement.setString(6, student.getStatus());
	}

	private Student mapStudent(ResultSet result) throws SQLException {
		return new Student(
				result.getInt("id"),
				result.getString("student_number"),
				result.getString("full_name"),
				result.getString("email"),
				result.getString("phone"),
				result.getString("course"),
				result.getString("status"));
	}
}