import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.math.BigDecimal;


public class Main {

    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(0.01);
    private static final BigDecimal LEAP_YEAR_DAYS = BigDecimal.valueOf(366);
    private static final BigDecimal NON_LEAP_YEAR_DAYS = BigDecimal.valueOf(365);
    private static final String inProgress = "В процессе разработки";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args) {

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("ru", "Ru"));
        Scanner scanner = new Scanner(System.in);
        CapitalizationCondition condition = askCapitalizationCondition(scanner);
        System.out.println("Вы выбрали: " + condition.getDescription());


        var depositAmount = getBigDecimalInput(scanner, "Введите сумму вклада в рублях РФ (разделитель между рублями и копейками - запятая): ");
        var annualInterestRate = getBigDecimalInput(scanner, "Введите процентную ставку (в % годовых без знака <%>, разделитель - запятая): ");
        var startDate = askDate(scanner, "Введите дату открытия вклада (ДД.ММ.ГГГГ): ", condition);
        var endDate = askDate(scanner, "Введите дату окончания срока вклада (ДД.ММ.ГГГГ): ", condition);

        // Убеждаемся, что startDate раньше endDate, в противном случае - меняем местами
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
            System.out.println("Введенная дата открытия вклада позже даты окончания срока вклада, даты поменялись местами");
        }

        var totalInterestEarned = calculationOfInterestOnDeposit(condition, depositAmount, annualInterestRate, startDate, endDate);

        BigDecimal totalWithdrawSum = depositAmount.add(totalInterestEarned);
        String formattedIncome = currencyFormat.format(totalInterestEarned); // для формата вывода данных через запятую + округление + разделители между разрядами
        String formattedTotalWithdrawSum = currencyFormat.format(totalWithdrawSum); // для формата вывода данных через запятую + округление + разделители между разрядами

        System.out.println("Вы заработали: " + formattedIncome);
        System.out.println("Сумма, доступная к выводу: " + formattedTotalWithdrawSum);
    }

    public enum CapitalizationCondition {
        NO("Капитализация процентов по вкладу не происходит"),
        YES_LAST_DAY_OF_MONTH("Капитализация в последний календарный день месяца"),
        YES_LAST_WORKING_DAY_OF_MONTH("Капитализация в последний рабочий день месяца");

        private final String description;

        CapitalizationCondition(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static CapitalizationCondition askCapitalizationCondition(Scanner scanner) {
        while (true) {
            System.out.print("Происходит ли капитализация процентов по вкладу (Введите Да/Нет): ");
            String firstAnswer = scanner.nextLine().trim();

            if (firstAnswer.equalsIgnoreCase("Нет")) {
                return CapitalizationCondition.NO;

            } else if (firstAnswer.equalsIgnoreCase("Да")) {
                while (true) {
                    System.out.print("Уточните, проценты причисляются к сумме вклада: в последний календарный день месяца (Введите: 'К') или в последний рабочий день месяца в период с 01.01.2003 по 12.12.2025 (Введите 'Р')? ");
                    String secondAnswer = scanner.nextLine().trim();

                    if (secondAnswer.equalsIgnoreCase("К")) {
                        return CapitalizationCondition.YES_LAST_DAY_OF_MONTH;
                    } else if (secondAnswer.equalsIgnoreCase("Р")) {
                        return CapitalizationCondition.YES_LAST_WORKING_DAY_OF_MONTH;
                    } else {
                        System.out.println("Пожалуйста, введите 'К' или 'Р'.");
                    }
                }
            } else {
                System.out.println("Пожалуйста, введите 'Да' или 'Нет'.");
            }
        }
    }


    private static BigDecimal getBigDecimalInput(Scanner scanner, String message) {//ввод суммы вклада пользователем, проверка корректности формата введенной суммы
        BigDecimal bigDecimalInput;

        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().replace(',', '.');

            try {
                bigDecimalInput = new BigDecimal(input);

                if (bigDecimalInput.compareTo(BigDecimal.ZERO) < 0) {
                    System.out.println("Введенное значение некорректно (меньше нуля)!");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число, например 15000,50 или 15,34: ");
            }
        }
        return bigDecimalInput;
    }

    private static LocalDate askDate(Scanner scanner, String message, CapitalizationCondition condition) {


        while (true) {
            System.out.print(message);
            try {
                LocalDate date = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);
                if (condition == CapitalizationCondition.YES_LAST_WORKING_DAY_OF_MONTH) {
                    if (date.isAfter(LocalDate.of(2002, 12, 31)) && date.isBefore(LocalDate.of(2026, 1, 1))) {
                        return date;
                    } else {
                        System.out.println("Неверный формат дат, введите дату не ранее 01.01.2003 и не позже 31.12.2025");
                        continue;
                    }
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты! Попробуйте снова");
            }
        }
    }


    private static BigDecimal calculationOfInterestOnDeposit
            (CapitalizationCondition condition,
             BigDecimal depositAmount,
             BigDecimal annualInterestRate,
             LocalDate startDate,
             LocalDate endDate) {

        BigDecimal totalInterestEarned = BigDecimal.ZERO;
        BigDecimal income; // данная переменная обозначает сумму начисленных процентов по вкладу, без суммы вклада
        LocalDate current = startDate.plusDays(1);

        if (condition == CapitalizationCondition.YES_LAST_DAY_OF_MONTH) {
            while (current.isBefore(endDate)) {

                boolean isLeap = current.isLeapYear();

                LocalDate lastDayOfMonth = current.with(TemporalAdjusters.lastDayOfMonth()); // ищем последнюю дату месяца
                LocalDate nextPoint = lastDayOfMonth.isBefore(endDate) ? lastDayOfMonth : endDate;

                long daysBetween = ChronoUnit.DAYS.between(current, nextPoint) + 1;

                BigDecimal daysInThisMonth = BigDecimal.valueOf(daysBetween);

                income = (depositAmount.add(totalInterestEarned))
                        .multiply(annualInterestRate)
                        .multiply(daysInThisMonth)
                        .multiply(PERCENTAGE_DIVISOR)
                        .divide(isLeap ? LEAP_YEAR_DAYS : NON_LEAP_YEAR_DAYS, 2, RoundingMode.HALF_UP);


                totalInterestEarned = totalInterestEarned.add(income); // скидываем проценты в счетчик

                current = nextPoint.plusDays(1);// переходим на следующий месяц
            }

        } else if (condition == CapitalizationCondition.NO) {
            long leapYearDaysCount = 0L;
            long nonLeapYearDaysCount = 0L;

            /* Расчет без капитализации путем пересчета в счетчиках общего количества дней в високосных или невисокосных годах,
        а также последующего вычисления процентов по формуле расчета процентов по вкладу*/
            while (current.isBefore(endDate)) {
                boolean isLeap = current.isLeapYear();

                LocalDate yearEnd = LocalDate.of(current.getYear(), 12, 31); // находим последнюю дату года, соответствующего заданной дате current
                LocalDate nextPoint = yearEnd.isBefore(endDate) ? yearEnd : endDate; // если current меньше чем конечная дата, заданная пользователем, переходим на следующий год, иначе - на конечную дату

                long daysInThisYear = ChronoUnit.DAYS.between(current, nextPoint) + 1; // считаем количество дней на данном отрезке

                if (isLeap) {
                    leapYearDaysCount += daysInThisYear;
                } else {
                    nonLeapYearDaysCount += daysInThisYear;
                }

                current = nextPoint.plusDays(1);
            }

            BigDecimal leapYearDays = BigDecimal.valueOf(leapYearDaysCount);
            BigDecimal nonLeapYearDays = BigDecimal.valueOf(nonLeapYearDaysCount);

            BigDecimal multipliedLeapYearDaysIncome =
                    depositAmount.multiply(annualInterestRate)
                            .multiply(leapYearDays)
                            .multiply(PERCENTAGE_DIVISOR)
                            .divide(LEAP_YEAR_DAYS, 10, RoundingMode.HALF_UP);

            BigDecimal multipliedNONLeapYearDaysIncome =
                    depositAmount.multiply(annualInterestRate)
                            .multiply(nonLeapYearDays)
                            .multiply(PERCENTAGE_DIVISOR)
                            .divide(NON_LEAP_YEAR_DAYS, 10, RoundingMode.HALF_UP);

            totalInterestEarned = multipliedLeapYearDaysIncome.add(multipliedNONLeapYearDaysIncome);
            totalInterestEarned = totalInterestEarned.setScale(2, RoundingMode.HALF_UP);

        } else if (condition == CapitalizationCondition.YES_LAST_WORKING_DAY_OF_MONTH) {

            BigDecimal nonCapitalizedInterest = BigDecimal.ZERO;
            while (current.isBefore(endDate)) {

                YearMonth ym = YearMonth.from(current);// извлекаем год и месяц из даты, сохраняем в переменную ym

                int year = ym.getYear();
                int month = ym.getMonthValue();

                LocalDate lastWorkingDay = WorkingDayRepository.findLastWorkingDayOfMonth(year, month);// ищем последний рабочий день месяца путем запроса в WorkingDayRepository
                LocalDate lastDayOfMonth = current.with(TemporalAdjusters.lastDayOfMonth());

                if (lastWorkingDay == null) {
                    // Если последний рабочий день не найден, пропускаем месяц (это маловероятно)
                    System.out.println("Ошибка в базе данных, расчет невозможен");
                    break;
                }

                if (lastWorkingDay == lastDayOfMonth) {
                    boolean isLeap = current.isLeapYear();
                    LocalDate nextPoint = lastDayOfMonth.isBefore(endDate) ? lastDayOfMonth : endDate;

                    long daysBetween = ChronoUnit.DAYS.between(current, nextPoint) + 1;

                    BigDecimal daysInThisMonth = BigDecimal.valueOf(daysBetween);

                    income = (depositAmount.add(totalInterestEarned))
                            .multiply(annualInterestRate)
                            .multiply(daysInThisMonth)
                            .multiply(PERCENTAGE_DIVISOR)
                            .divide(isLeap ? LEAP_YEAR_DAYS : NON_LEAP_YEAR_DAYS, 10, RoundingMode.HALF_UP);

                    totalInterestEarned = totalInterestEarned.add(income).add(nonCapitalizedInterest).setScale(2, RoundingMode.HALF_UP);
                    nonCapitalizedInterest = BigDecimal.ZERO;
                    current = nextPoint.plusDays(1);

                } else {
                    boolean isLeap = current.isLeapYear();
                    LocalDate nextPoint1 = lastWorkingDay.isBefore(endDate) ? lastWorkingDay : endDate;
                    BigDecimal daysInFirstPeriod = BigDecimal.valueOf(ChronoUnit.DAYS.between(current, nextPoint1) + 1);


                    BigDecimal firstPeriodIncome = (depositAmount.add(totalInterestEarned))
                            .multiply(annualInterestRate)
                            .multiply(daysInFirstPeriod)
                            .multiply(PERCENTAGE_DIVISOR)
                            .divide(isLeap ? LEAP_YEAR_DAYS : NON_LEAP_YEAR_DAYS, 10, RoundingMode.HALF_UP);

                    current = nextPoint1.plusDays(1);
                    totalInterestEarned = totalInterestEarned.add(firstPeriodIncome).add(nonCapitalizedInterest).setScale(2, RoundingMode.HALF_UP);

                    LocalDate nextPoint2 = lastDayOfMonth.isBefore(endDate) ? lastDayOfMonth : endDate;
                    BigDecimal daysInSecondPeriod = BigDecimal.valueOf(ChronoUnit.DAYS.between(current, nextPoint2) + 1);
                    BigDecimal secondPeriodIncome = (depositAmount.add(totalInterestEarned))
                            .multiply(annualInterestRate)
                            .multiply(daysInSecondPeriod)
                            .multiply(PERCENTAGE_DIVISOR)
                            .divide(isLeap ? LEAP_YEAR_DAYS : NON_LEAP_YEAR_DAYS, 10, RoundingMode.HALF_UP);

                    current = nextPoint2.plusDays(1);
                    nonCapitalizedInterest = secondPeriodIncome;
                }
            }
        }
        return totalInterestEarned;
    }
}