package edu.uwm.cs361.fantastic_five.training_tracker.tests.use_cases;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.junit.Before;
import org.junit.Test;

import edu.uwm.cs361.fantastic_five.training_tracker.app.entities.Program;
import edu.uwm.cs361.fantastic_five.training_tracker.app.entities.Student;
import edu.uwm.cs361.fantastic_five.training_tracker.app.use_cases.ProgramCreator;
import edu.uwm.cs361.fantastic_five.training_tracker.app.use_cases.StudentCreator;
import edu.uwm.cs361.fantastic_five.training_tracker.app.use_cases.StudentEnroller;
import edu.uwm.cs361.fantastic_five.training_tracker.app.use_cases.requests.CreateProgramRequest;
import edu.uwm.cs361.fantastic_five.training_tracker.app.use_cases.requests.CreateStudentRequest;
import edu.uwm.cs361.fantastic_five.training_tracker.app.use_cases.requests.EnrollStudentRequest;
import edu.uwm.cs361.fantastic_five.training_tracker.app.use_cases.requests.ListUnenrolledStudentsRequest;
import edu.uwm.cs361.fantastic_five.training_tracker.app.use_cases.responses.ListUnenrolledStudentsResponse;
import edu.uwm.cs361.fantastic_five.training_tracker.tests.AppEngineTest;

public class StudentEnrollerListUnenrolledStudentsTest extends AppEngineTest {
	private StudentEnroller studentEnroller;
	private ListUnenrolledStudentsRequest req;
	private ListUnenrolledStudentsResponse resp;

	@Before
	public void setUp() {
		studentEnroller = new StudentEnroller();
		req = new ListUnenrolledStudentsRequest();

		createProgram("Example Program", "Andrew Meyer", "2.60");
		createStudent("Andrew", "Meyer", "andrew@example.com");
		createStudent("Charlie", "Liberski", "charlie@example.com");
	}

	private void doRequest() {
		resp = studentEnroller.listUnenrolledStudents(req);
	}

	private void createProgram(String name, String instructor, String price) {
		CreateProgramRequest req = new CreateProgramRequest();
		req.name = name;
		req.instructor = instructor;
		req.price = price;

		new ProgramCreator().createProgram(req);
	}

	private void createStudent(String firstName, String lastName, String email) {
		CreateStudentRequest req = new CreateStudentRequest();
		req.firstName = firstName;
		req.lastName = lastName;
		req.email = email;

		new StudentCreator().createStudent(req);
	}

	private void enrollStudent(Program program, Student student) {
		String programId = Long.toString(program.getKey().getId());
		String studentId = Long.toString(student.getKey().getId());

		EnrollStudentRequest req = new EnrollStudentRequest();
		req.programId = programId;
		req.studentId = studentId;

		new StudentEnroller().enrollStudent(req);
	}

	@SuppressWarnings("unchecked")
	private List<Program> getAllPrograms() {
		PersistenceManager pm = getPersistenceManager();
		return (List<Program>) pm.newQuery(Program.class).execute();
	}
	private Program getFirstProgram() {
		List<Program> programs = getAllPrograms();
		return programs.iterator().next();
	}

	@SuppressWarnings("unchecked")
	private List<Student> getAllStudents() {
		PersistenceManager pm = getPersistenceManager();
		return (List<Student>) pm.newQuery(Student.class).execute();
	}
	private Student getFirstStudent() {
		List<Student> students = getAllStudents();
		return students.iterator().next();
	}

	private void generateValidRequest() {
		req.programId = Long.toString(getFirstProgram().getKey().getId());
	}

	@Test
	public void testResponseProgram() {
		Program program = getFirstProgram();
		req.programId = Long.toString(program.getKey().getId());
		doRequest();

		assertEquals(program.getKey().getId(), resp.program.getKey().getId());
	}

	@Test
	public void testNoEnrolledStudents() {
		generateValidRequest();
		doRequest();

		List<Student> unenrolledStudents = getAllStudents();
		for (Student unenrolledStudent : unenrolledStudents) {
			assertThat(resp.unenrolledStudents, hasItem(unenrolledStudent));
		}
	}

	@Test
	public void testContainsUnenrolledStudents() {
		Program program = getFirstProgram();
		Student student = getFirstStudent();
		enrollStudent(program, student);

		generateValidRequest();
		doRequest();

		List<Student> unenrolledStudents = new ArrayList<Student>(getAllStudents());
		unenrolledStudents.remove(student);

		for (Student unenrolledStudent : unenrolledStudents) {
			assertThat(resp.unenrolledStudents, hasItem(unenrolledStudent));
		}
	}

	@Test
	public void testDoesNotContainEnrolledStudents() {
		Program program = getFirstProgram();
		Student student = getFirstStudent();
		enrollStudent(program, student);

		generateValidRequest();
		doRequest();

		Set<Student> enrolledStudents = program.listStudents();
		for (Student enrolledStudent : enrolledStudents) {
			assertThat(resp.unenrolledStudents, not(hasItem(enrolledStudent)));
		}
	}
}
