package ua.pp.rozkladznu.app.processor.dependency;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vojko Vladimir
 */
public class ScheduleDependency {

    private Set<String> groups;
    private Set<String> subjects;
    private Set<String> academicHours;
    private Set<String> lecturers;
    private Set<String> audiences;

    public ScheduleDependency() {
        groups = new HashSet<>();
        subjects = new HashSet<>();
        academicHours = new HashSet<>();
        lecturers = new HashSet<>();
        audiences = new HashSet<>();
    }

    public boolean hasGroups() {
        return groups.size() != 0;
    }

    public boolean hasSubjects() {
        return subjects.size() != 0;
    }

    public boolean hasAcademicHours() {
        return academicHours.size() != 0;
    }

    public boolean hasLecturers() {
        return lecturers.size() != 0;
    }

    public boolean hasAudiences() {
        return audiences.size() != 0;
    }

    public String[] getGroups() {
            return groups.toArray(new String[groups.size()]);
    }

    public String[] getSubjects() {
        return subjects.toArray(new String[subjects.size()]);
    }

    public String[] getAcademicHours() {
        return academicHours.toArray(new String[academicHours.size()]);
    }

    public String[] getLecturers() {
        return lecturers.toArray(new String[lecturers.size()]);
    }

    public String[] getAudiences() {
        return audiences.toArray(new String[audiences.size()]);
    }

    public void addGroup(String group) {
        groups.add(group);
    }

    public void addSubject(String subject) {
        subjects.add(subject);
    }

    public void addAcademicHour(String academicHour) {
        academicHours.add(academicHour);
    }

    public void addLecturer(String lecturer) {
        lecturers.add(lecturer);
    }

    public void addAudience(String audience) {
        audiences.add(audience);
    }

    @Override
    public String toString() {
        return "ScheduleDependency:\n" +
                "[Groups: " + groups.size() + ",\n" +
                "Subjects: " + subjects.size() + ",\n" +
                "AcademicHours: " + academicHours.size() + ",\n" +
                "Lecturers: " + lecturers.size() + ",\n" +
                "Audiences: " + audiences.size() + "]\n";
    }
}
