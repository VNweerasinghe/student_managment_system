package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentRepositoryTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void seedsAndSupportsStudentCreateUpdateAndDelete() throws Exception {
		StudentRepository repository = new StudentRepository(temporaryDirectory);
		assertEquals(3, repository.findAll().size());

		repository.insert(student("STU-2001", "Riley Chen"));
		Student inserted = findByNumber(repository.findAll(), "STU-2001");
		assertEquals("Riley Chen", inserted.getFullName());

		repository.update(new Student(inserted.getId(), inserted.getStudentNumber(), "Riley Morgan",
				inserted.getEmail(), inserted.getPhone(), inserted.getCourse(), "Graduated"));
		Student updated = findByNumber(repository.findAll(), "STU-2001");
		assertEquals("Riley Morgan", updated.getFullName());
		assertEquals("Graduated", updated.getStatus());

		repository.delete(updated.getId());
		assertFalse(repository.findAll().stream().anyMatch(student -> "STU-2001".equals(student.getStudentNumber())));
	}

	@Test
	void rejectsDuplicateStudentNumbers() throws Exception {
		StudentRepository repository = new StudentRepository(temporaryDirectory);
		repository.insert(student("STU-2002", "Taylor Lee"));

		assertThrows(SQLException.class, () -> repository.insert(student("STU-2002", "Another Student")));
	}

	@Test
	void doesNotReseedAfterAllStudentsAreDeleted() throws Exception {
		StudentRepository repository = new StudentRepository(temporaryDirectory);
		for (Student student : repository.findAll()) {
			repository.delete(student.getId());
		}

		StudentRepository reopenedRepository = new StudentRepository(temporaryDirectory);
		assertEquals(0, reopenedRepository.findAll().size());
	}

	private Student findByNumber(List<Student> students, String number) {
		return students.stream()
				.filter(student -> number.equals(student.getStudentNumber()))
				.findFirst()
				.orElseThrow(AssertionError::new);
	}

	private Student student(String number, String name) {
		return new Student(0, number, name, "student@example.com", "555-0100", "Computer Science", "Enrolled");
	}
}