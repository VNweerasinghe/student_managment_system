package org.example;

public class Student {
	private final int id;
	private final String studentNumber;
	private final String fullName;
	private final String email;
	private final String phone;
	private final String course;
	private final String status;

	public Student(int id, String studentNumber, String fullName, String email, String phone,
			String course, String status) {
		this.id = id;
		this.studentNumber = studentNumber;
		this.fullName = fullName;
		this.email = email;
		this.phone = phone;
		this.course = course;
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public String getStudentNumber() {
		return studentNumber;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getCourse() {
		return course;
	}

	public String getStatus() {
		return status;
	}
}