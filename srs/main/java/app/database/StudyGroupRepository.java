package database;

import model.Coordinates;
import model.Country;
import model.Person;
import model.Semester;
import model.StudyGroup;
import model.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public class StudyGroupRepository {

    private static final String INSERT_SQL =
        "INSERT INTO study_groups (name, coordinates_x, coordinates_y, creation_date, " +
        "students_count, expelled_students, transferred_students, semester, " +
        "admin_name, admin_birthday, admin_eye_color, admin_nationality, owner_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
    private static final String FIND_ALL_SQL =
        "SELECT id, name, coordinates_x, coordinates_y, creation_date, students_count, " +
        "expelled_students, transferred_students, semester, admin_name, admin_birthday, " +
        "admin_eye_color, admin_nationality, owner_id FROM study_groups ORDER BY students_count, id";
    private static final String FIND_BY_ID_SQL =
        "SELECT id, name, coordinates_x, coordinates_y, creation_date, students_count, " +
        "expelled_students, transferred_students, semester, admin_name, admin_birthday, " +
        "admin_eye_color, admin_nationality, owner_id FROM study_groups WHERE id = ?";
    private static final String FIND_BY_OWNER_SQL =
        "SELECT id, name, coordinates_x, coordinates_y, creation_date, students_count, " +
        "expelled_students, transferred_students, semester, admin_name, admin_birthday, " +
        "admin_eye_color, admin_nationality, owner_id FROM study_groups WHERE owner_id = ? " +
        "ORDER BY students_count, id";
    private static final String UPDATE_SQL =
        "UPDATE study_groups SET name = ?, coordinates_x = ?, coordinates_y = ?, " +
        "students_count = ?, expelled_students = ?, transferred_students = ?, semester = ?, " +
        "admin_name = ?, admin_birthday = ?, admin_eye_color = ?, admin_nationality = ? " +
        "WHERE id = ? AND owner_id = ?";
    private static final String DELETE_SQL =
        "DELETE FROM study_groups WHERE id = ? AND owner_id = ?";
    private static final String DELETE_FIRST_SQL =
        "DELETE FROM study_groups WHERE id = (SELECT id FROM study_groups ORDER BY students_count, id LIMIT 1) " +
        "AND owner_id = ? RETURNING id, name, coordinates_x, coordinates_y, creation_date, students_count, " +
        "expelled_students, transferred_students, semester, admin_name, admin_birthday, " +
        "admin_eye_color, admin_nationality, owner_id";
    private static final String DELETE_LOWER_SQL =
        "DELETE FROM study_groups WHERE owner_id = ? AND students_count < ? RETURNING id, name, " +
        "coordinates_x, coordinates_y, creation_date, students_count, expelled_students, " +
        "transferred_students, semester, admin_name, admin_birthday, admin_eye_color, admin_nationality, owner_id";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM study_groups";

    public PriorityQueue<StudyGroup> findAll() throws SQLException {
        List<StudyGroup> groups = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(FIND_ALL_SQL)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                groups.add(mapRowToStudyGroup(rs));
            }
        }
        return new PriorityQueue<>(groups);
    }

    public List<StudyGroup> findByOwner(Integer ownerId) throws SQLException {
        List<StudyGroup> groups = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_OWNER_SQL)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                groups.add(mapRowToStudyGroup(rs));
            }
        }
        return groups;
    }

    public Optional<StudyGroup> findById(Integer id) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToStudyGroup(rs));
            }
            return Optional.empty();
        }
    }

    public Integer create(StudyGroup group, Integer ownerId) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, group.getName());
            ps.setInt(2, group.getCoordinates().getX());
            ps.setDouble(3, group.getCoordinates().getY());
            ps.setDate(4, java.sql.Date.valueOf(group.getCreationDate()));
            ps.setInt(5, group.getStudentsCount());
            if (group.getExpelledStudents() != null) {
                ps.setLong(6, group.getExpelledStudents());
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }
            ps.setInt(7, group.getTransferredStudents());
            if (group.getSemesterEnum() != null) {
                ps.setString(8, group.getSemesterEnum().name());
            } else {
                ps.setNull(8, java.sql.Types.VARCHAR);
            }
            ps.setString(9, group.getGroupAdmin().getName());
            ps.setTimestamp(10, new Timestamp(group.getGroupAdmin().getBirthday().getTime()));
            if (group.getGroupAdmin().getEyeColor() != null) {
                ps.setString(11, group.getGroupAdmin().getEyeColor().name());
            } else {
                ps.setNull(11, java.sql.Types.VARCHAR);
            }
            if (group.getGroupAdmin().getNationality() != null) {
                ps.setString(12, group.getGroupAdmin().getNationality().name());
            } else {
                ps.setNull(12, java.sql.Types.VARCHAR);
            }
            ps.setInt(13, ownerId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DatabaseManager.getInstance().commit();
                return rs.getInt("id");
            }
            throw new SQLException("Failed to create study group");
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
    }

    public boolean update(StudyGroup group, Integer ownerId) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, group.getName());
            ps.setInt(2, group.getCoordinates().getX());
            ps.setDouble(3, group.getCoordinates().getY());
            ps.setInt(4, group.getStudentsCount());
            if (group.getExpelledStudents() != null) {
                ps.setLong(5, group.getExpelledStudents());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            ps.setInt(6, group.getTransferredStudents());
            if (group.getSemesterEnum() != null) {
                ps.setString(7, group.getSemesterEnum().name());
            } else {
                ps.setNull(7, java.sql.Types.VARCHAR);
            }
            ps.setString(8, group.getGroupAdmin().getName());
            ps.setTimestamp(9, new Timestamp(group.getGroupAdmin().getBirthday().getTime()));
            if (group.getGroupAdmin().getEyeColor() != null) {
                ps.setString(10, group.getGroupAdmin().getEyeColor().name());
            } else {
                ps.setNull(10, java.sql.Types.VARCHAR);
            }
            if (group.getGroupAdmin().getNationality() != null) {
                ps.setString(11, group.getGroupAdmin().getNationality().name());
            } else {
                ps.setNull(11, java.sql.Types.VARCHAR);
            }
            ps.setInt(12, group.getId());
            ps.setInt(13, ownerId);

            int updated = ps.executeUpdate();
            DatabaseManager.getInstance().commit();
            return updated > 0;
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
    }

    public boolean delete(Integer id, Integer ownerId) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            ps.setInt(2, ownerId);
            int deleted = ps.executeUpdate();
            DatabaseManager.getInstance().commit();
            return deleted > 0;
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
    }

    public Optional<StudyGroup> deleteFirst(Integer ownerId) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(DELETE_FIRST_SQL)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DatabaseManager.getInstance().commit();
                return Optional.of(mapRowToStudyGroup(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
    }

    public List<StudyGroup> deleteLower(Integer ownerId, int studentsCount) throws SQLException {
        List<StudyGroup> deleted = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(DELETE_LOWER_SQL)) {
            ps.setInt(1, ownerId);
            ps.setInt(2, studentsCount);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                deleted.add(mapRowToStudyGroup(rs));
            }
            DatabaseManager.getInstance().commit();
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
        return deleted;
    }

    public int count() throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(COUNT_SQL)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    private StudyGroup mapRowToStudyGroup(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String name = rs.getString("name");
        int x = rs.getInt("coordinates_x");
        Double y = rs.getDouble("coordinates_y");
        LocalDate creationDate = rs.getDate("creation_date").toLocalDate();
        int studentsCount = rs.getInt("students_count");
        Long expelledStudents = rs.getLong("expelled_students");
        if (rs.wasNull()) expelledStudents = null;
        int transferredStudents = rs.getInt("transferred_students");

        String semesterStr = rs.getString("semester");
        Semester semester = semesterStr != null ? Semester.valueOf(semesterStr) : null;

        String adminName = rs.getString("admin_name");
        Timestamp adminBirthday = rs.getTimestamp("admin_birthday");

        String eyeColorStr = rs.getString("admin_eye_color");
        Color eyeColor = eyeColorStr != null ? Color.valueOf(eyeColorStr) : null;

        String nationalityStr = rs.getString("admin_nationality");
        Country nationality = nationalityStr != null ? Country.valueOf(nationalityStr) : null;

        Integer ownerId = rs.getInt("owner_id");
        if (rs.wasNull()) ownerId = null;

        Person admin = new Person(adminName, adminBirthday, eyeColor, nationality);
        Coordinates coords = new Coordinates(x, y);

        StudyGroup group = new StudyGroup(id, name, coords, creationDate, studentsCount,
            expelledStudents, transferredStudents, semester, admin);
        group.setOwnerId(ownerId);
        return group;
    }        try {

}