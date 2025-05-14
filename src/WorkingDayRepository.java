import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkingDayRepository {
    private static final Logger logger = LoggerFactory.getLogger(WorkingDayRepository.class);
    private static final String URL = "jdbc:sqlite:C:/Users/Dmitry/Базы данных/Calendar.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static LocalDate findLastWorkingDayOfMonth(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth());

        try (Connection conn = WorkingDayRepository.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT is_holiday FROM holidays WHERE date = ?")) {

            while (!date.isBefore(LocalDate.of(year, month, 1))) {
                String dateStr = date.toString(); // формат ГГГГ-ММ-ДД

                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int isHoliday = rs.getInt("is_holiday");
                    if (isHoliday == 0) { // если рабочий день
                        return date;
                    }
                }
                date = date.minusDays(1); // идем на день назад
            }

        } catch (SQLException e) {
            logger.error("Ошибка подключения к базе данных: ", e);
        }
        return null; // если не найдено (теоретически невозможно)
    }
}