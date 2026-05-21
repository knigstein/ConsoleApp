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

/**
 * Репозиторий для сохранения и загрузки сущностей {@link StudyGroup}.
 *
 * <p>Изменяющие операции выполняются в транзакции:
 * при успехе выполняется commit, при ошибке rollback.</p>
 */
public class StudyGroupRepository {

    private static final String OWNER_FILTER =
        "((? IS NULL AND o.owner_id IS NULL) OR (? IS NOT NULL AND o.owner_id = ?))";
    private static final String BASE_SELECT =
        "SELECT sg.id, n.name, c.x AS coordinates_x, c.y AS coordinates_y, cd.creation_date, " +
        "ss.students_count, ss.expelled_students, ss.transferred_students, sem.semester, " +
        "ga.name AS admin_name, ga.birthday AS admin_birthday, ga.eye_color AS admin_eye_color, " +
        "ga.nationality AS admin_nationality, o.owner_id " +
        "FROM study_groups sg " +
        "JOIN study_group_names n ON n.study_group_id = sg.id " +
        "JOIN study_group_coordinates c ON c.study_group_id = sg.id " +
        "JOIN study_group_creation_dates cd ON cd.study_group_id = sg.id " +
        "JOIN study_group_student_stats ss ON ss.study_group_id = sg.id " +
        "LEFT JOIN study_group_semesters sem ON sem.study_group_id = sg.id " +
        "JOIN study_group_admins ga ON ga.study_group_id = sg.id " +
        "LEFT JOIN study_group_owners o ON o.study_group_id = sg.id";
    private static final String INSERT_GROUP_SQL = "INSERT INTO study_groups DEFAULT VALUES RETURNING id";
    private static final String INSERT_OWNER_SQL =
        "INSERT INTO study_group_owners (study_group_id, owner_id) VALUES (?, ?)";
    private static final String INSERT_NAME_SQL =
        "INSERT INTO study_group_names (study_group_id, name) VALUES (?, ?)";
    private static final String INSERT_COORDINATES_SQL =
        "INSERT INTO study_group_coordinates (study_group_id, x, y) VALUES (?, ?, ?)";
    private static final String INSERT_CREATION_DATE_SQL =
        "INSERT INTO study_group_creation_dates (study_group_id, creation_date) VALUES (?, ?)";
    private static final String INSERT_STUDENT_STATS_SQL =
        "INSERT INTO study_group_student_stats (study_group_id, students_count, expelled_students, transferred_students) " +
        "VALUES (?, ?, ?, ?)";
    private static final String INSERT_SEMESTER_SQL =
        "INSERT INTO study_group_semesters (study_group_id, semester) VALUES (?, ?)";
    private static final String INSERT_ADMIN_SQL =
        "INSERT INTO study_group_admins (study_group_id, name, birthday, eye_color, nationality) " +
        "VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY ss.students_count, sg.id";
    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE sg.id = ?";
    private static final String FIND_BY_OWNER_SQL =
        BASE_SELECT + " WHERE " + OWNER_FILTER + " ORDER BY ss.students_count, sg.id";
    private static final String UPDATE_NAME_SQL =
        "UPDATE study_group_names SET name = ? WHERE study_group_id = ?";
    private static final String UPDATE_COORDINATES_SQL =
        "UPDATE study_group_coordinates SET x = ?, y = ? WHERE study_group_id = ?";
    private static final String UPDATE_CREATION_DATE_SQL =
        "UPDATE study_group_creation_dates SET creation_date = ? WHERE study_group_id = ?";
    private static final String UPDATE_STUDENT_STATS_SQL =
        "UPDATE study_group_student_stats SET students_count = ?, expelled_students = ?, transferred_students = ? " +
        "WHERE study_group_id = ?";
    private static final String UPDATE_SEMESTER_SQL =
        "UPDATE study_group_semesters SET semester = ? WHERE study_group_id = ?";
    private static final String UPDATE_ADMIN_SQL =
        "UPDATE study_group_admins SET name = ?, birthday = ?, eye_color = ?, nationality = ? WHERE study_group_id = ?";
    private static final String DELETE_SQL = "DELETE FROM study_groups WHERE id = ?";
    private static final String FIND_FIRST_BY_OWNER_SQL =
        BASE_SELECT + " WHERE " + OWNER_FILTER + " ORDER BY ss.students_count, sg.id LIMIT 1";
    private static final String FIND_LOWER_BY_OWNER_SQL =
        BASE_SELECT + " WHERE " + OWNER_FILTER + " AND ss.students_count < ? ORDER BY ss.students_count, sg.id";
    private static final String CHECK_OWNERSHIP_SQL =
        "SELECT 1 FROM study_group_owners WHERE study_group_id = ? " +
        "AND ((? IS NULL AND owner_id IS NULL) OR (? IS NOT NULL AND owner_id = ?))";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM study_groups";

    /**
     * Загружает все учебные группы из БД.
     *
     * @return очередь с приоритетом, содержащая все группы
     * @throws SQLException если запрос завершился ошибкой
     */
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

    /**
     * Загружает группы, принадлежащие конкретному пользователю.
     *
     * @param ownerId идентификатор владельца
     * @return список групп владельца
     * @throws SQLException если запрос завершился ошибкой
     */
    public List<StudyGroup> findByOwner(Integer ownerId) throws SQLException {
        List<StudyGroup> groups = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_OWNER_SQL)) {
            bindOwnerFilter(ps, 1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                groups.add(mapRowToStudyGroup(rs));
            }
        }
        return groups;
    }

    /**
     * Ищет группу по идентификатору.
     *
     * @param id идентификатор группы
     * @return {@link Optional} с найденной группой
     * @throws SQLException если запрос завершился ошибкой
     */
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

    /**
     * Вставляет новую группу и возвращает сгенерированный id.
     *
     * @param group данные группы
     * @param ownerId идентификатор владельца
     * @return сгенерированный id новой группы
     * @throws SQLException если вставка завершилась ошибкой
     */
    public Integer create(StudyGroup group, Integer ownerId) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement insertGroup = conn.prepareStatement(INSERT_GROUP_SQL)) {
            ResultSet rs = insertGroup.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt("id");
                insertOwner(conn, newId, ownerId);
                insertName(conn, newId, group);
                insertCoordinates(conn, newId, group);
                insertCreationDate(conn, newId, group);
                insertStudentStats(conn, newId, group);
                insertSemester(conn, newId, group);
                insertAdmin(conn, newId, group);
                DatabaseManager.getInstance().commit();
                return newId;
            }
            throw new SQLException("Failed to create study group");
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
    }

    /**
     * Обновляет существующую группу, если она принадлежит указанному владельцу.
     *
     * @param group обновленные данные группы
     * @param ownerId идентификатор владельца
     * @return {@code true}, если была обновлена хотя бы одна строка
     * @throws SQLException если обновление завершилось ошибкой
     */
    public boolean update(StudyGroup group, Integer ownerId) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try {
            if (!isOwnedBy(conn, group.getId(), ownerId)) {
                return false;
            }
            updateName(conn, group);
            updateCoordinates(conn, group);
            updateCreationDate(conn, group);
            updateStudentStats(conn, group);
            updateSemester(conn, group);
            updateAdmin(conn, group);
            DatabaseManager.getInstance().commit();
            return true;
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
    }

    /**
     * Удаляет группу по id, если она принадлежит владельцу.
     *
     * @param id идентификатор группы
     * @param ownerId идентификатор владельца
     * @return {@code true}, если группа была удалена
     * @throws SQLException если удаление завершилось ошибкой
     */
    public boolean delete(Integer id, Integer ownerId) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try {
            if (!isOwnedBy(conn, id, ownerId)) {
                return false;
            }
            int deleted = deleteById(conn, id);
            DatabaseManager.getInstance().commit();
            return deleted > 0;
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
    }

    /**
     * Удаляет первую группу в порядке сортировки для указанного владельца.
     *
     * @param ownerId идентификатор владельца
     * @return {@link Optional} со снимком удаленной группы
     * @throws SQLException если удаление завершилось ошибкой
     */
    public Optional<StudyGroup> deleteFirst(Integer ownerId) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(FIND_FIRST_BY_OWNER_SQL)) {
            bindOwnerFilter(ps, 1, ownerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                StudyGroup group = mapRowToStudyGroup(rs);
                deleteById(conn, group.getId());
                DatabaseManager.getInstance().commit();
                return Optional.of(group);
            }
            return Optional.empty();
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
    }

    /**
     * Удаляет группы владельца, у которых количество студентов меньше порога.
     *
     * @param ownerId идентификатор владельца
     * @param studentsCount пороговое значение studentsCount
     * @return список удаленных групп
     * @throws SQLException если удаление завершилось ошибкой
     */
    public List<StudyGroup> deleteLower(Integer ownerId, int studentsCount) throws SQLException {
        List<StudyGroup> deleted = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(FIND_LOWER_BY_OWNER_SQL)) {
            bindOwnerFilter(ps, 1, ownerId);
            ps.setInt(4, studentsCount);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StudyGroup group = mapRowToStudyGroup(rs);
                deleted.add(group);
            }
            for (StudyGroup group : deleted) {
                deleteById(conn, group.getId());
            }
            DatabaseManager.getInstance().commit();
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
        return deleted;
    }

    /**
     * Подсчитывает общее количество групп в хранилище.
     *
     * @return число строк в таблице study_groups
     * @throws SQLException если запрос завершился ошибкой
     */
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

    /**
     * Преобразует строку {@link ResultSet} в объект {@link StudyGroup}.
     *
     * @param rs текущая строка результата запроса
     * @return объект учебной группы
     * @throws SQLException если строку не удалось корректно прочитать
     */
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
    }

    private void insertOwner(Connection conn, Integer groupId, Integer ownerId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_OWNER_SQL)) {
            ps.setInt(1, groupId);
            setNullableOwner(ps, 2, ownerId);
            ps.executeUpdate();
        }
    }

    private void insertName(Connection conn, Integer groupId, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_NAME_SQL)) {
            ps.setInt(1, groupId);
            ps.setString(2, group.getName());
            ps.executeUpdate();
        }
    }

    private void insertCoordinates(Connection conn, Integer groupId, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_COORDINATES_SQL)) {
            ps.setInt(1, groupId);
            ps.setInt(2, group.getCoordinates().getX());
            ps.setDouble(3, group.getCoordinates().getY());
            ps.executeUpdate();
        }
    }

    private void insertAdmin(Connection conn, Integer groupId, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_ADMIN_SQL)) {
            ps.setInt(1, groupId);
            ps.setString(2, group.getGroupAdmin().getName());
            ps.setTimestamp(3, new Timestamp(group.getGroupAdmin().getBirthday().getTime()));
            if (group.getGroupAdmin().getEyeColor() != null) {
                ps.setString(4, group.getGroupAdmin().getEyeColor().name());
            } else {
                ps.setNull(4, java.sql.Types.VARCHAR);
            }
            if (group.getGroupAdmin().getNationality() != null) {
                ps.setString(5, group.getGroupAdmin().getNationality().name());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }
            ps.executeUpdate();
        }
    }

    private void insertCreationDate(Connection conn, Integer groupId, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_CREATION_DATE_SQL)) {
            ps.setInt(1, groupId);
            ps.setDate(2, java.sql.Date.valueOf(group.getCreationDate()));
            ps.executeUpdate();
        }
    }

    private void insertStudentStats(Connection conn, Integer groupId, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_STUDENT_STATS_SQL)) {
            ps.setInt(1, groupId);
            ps.setInt(2, group.getStudentsCount());
            if (group.getExpelledStudents() != null) {
                ps.setLong(3, group.getExpelledStudents());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setInt(4, group.getTransferredStudents());
            ps.executeUpdate();
        }
    }

    private void insertSemester(Connection conn, Integer groupId, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SEMESTER_SQL)) {
            ps.setInt(1, groupId);
            if (group.getSemesterEnum() != null) {
                ps.setString(2, group.getSemesterEnum().name());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }
            ps.executeUpdate();
        }
    }

    private void updateName(Connection conn, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_NAME_SQL)) {
            ps.setString(1, group.getName());
            ps.setInt(2, group.getId());
            ps.executeUpdate();
        }
    }

    private void updateCoordinates(Connection conn, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_COORDINATES_SQL)) {
            ps.setInt(1, group.getCoordinates().getX());
            ps.setDouble(2, group.getCoordinates().getY());
            ps.setInt(3, group.getId());
            ps.executeUpdate();
        }
    }

    private void updateCreationDate(Connection conn, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_CREATION_DATE_SQL)) {
            ps.setDate(1, java.sql.Date.valueOf(group.getCreationDate()));
            ps.setInt(2, group.getId());
            ps.executeUpdate();
        }
    }

    private void updateStudentStats(Connection conn, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_STUDENT_STATS_SQL)) {
            ps.setInt(1, group.getStudentsCount());
            if (group.getExpelledStudents() != null) {
                ps.setLong(2, group.getExpelledStudents());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setInt(3, group.getTransferredStudents());
            ps.setInt(4, group.getId());
            ps.executeUpdate();
        }
    }

    private void updateSemester(Connection conn, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SEMESTER_SQL)) {
            if (group.getSemesterEnum() != null) {
                ps.setString(1, group.getSemesterEnum().name());
            } else {
                ps.setNull(1, java.sql.Types.VARCHAR);
            }
            ps.setInt(2, group.getId());
            ps.executeUpdate();
        }
    }

    private void updateAdmin(Connection conn, StudyGroup group) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_ADMIN_SQL)) {
            ps.setString(1, group.getGroupAdmin().getName());
            ps.setTimestamp(2, new Timestamp(group.getGroupAdmin().getBirthday().getTime()));
            if (group.getGroupAdmin().getEyeColor() != null) {
                ps.setString(3, group.getGroupAdmin().getEyeColor().name());
            } else {
                ps.setNull(3, java.sql.Types.VARCHAR);
            }
            if (group.getGroupAdmin().getNationality() != null) {
                ps.setString(4, group.getGroupAdmin().getNationality().name());
            } else {
                ps.setNull(4, java.sql.Types.VARCHAR);
            }
            ps.setInt(5, group.getId());
            ps.executeUpdate();
        }
    }

    private int deleteById(Connection conn, Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private boolean isOwnedBy(Connection conn, Integer groupId, Integer ownerId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(CHECK_OWNERSHIP_SQL)) {
            ps.setInt(1, groupId);
            bindOwnerFilter(ps, 2, ownerId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    private void bindOwnerFilter(PreparedStatement ps, int startIndex, Integer ownerId) throws SQLException {
        setNullableOwner(ps, startIndex, ownerId);
        setNullableOwner(ps, startIndex + 1, ownerId);
        setNullableOwner(ps, startIndex + 2, ownerId);
    }

    private void setNullableOwner(PreparedStatement ps, int parameterIndex, Integer ownerId) throws SQLException {
        if (ownerId != null) {
            ps.setInt(parameterIndex, ownerId);
        } else {
            ps.setNull(parameterIndex, java.sql.Types.INTEGER);
        }
    }

}