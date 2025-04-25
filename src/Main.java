import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;

public class Main {

    private static final BigDecimal PERCENTAGE_DIVISOR = new BigDecimal("0.01");
    private static final BigDecimal LEAP_YEAR_DAYS = new BigDecimal("366");
    private static final BigDecimal NON_LEAP_YEAR_DAYS = new BigDecimal("365");

    public static void main(String[] args) {

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("ru", "Ru"));
        Scanner scanner = new Scanner(System.in);
        CapitalizationCondition condition = askCapitalizationCondition(scanner);
        System.out.println("Вы выбрали: " + condition.getDescription());


        var depositAmount = getDepositAmount(scanner);
        var AnnualInterestRate = getAnnualInterestRate(scanner);
        var startDate = askDate(scanner, "Введите дату открытия вклада (ДД.ММ.ГГГГ): ");
        var endDate = askDate(scanner, "Введите дату окончания срока вклада (ДД.ММ.ГГГГ): ");

        // Убеждаемся, что startDate раньше endDate, в противном случае - меняем местами
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
            System.out.println("Введенная дата открытия вклада позже даты окончания срока вклада, даты поменялись местами");
        }

        var totalInterestEarned = calculationOfInterestOnDeposit(condition, depositAmount, AnnualInterestRate, startDate, endDate);

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
                    System.out.print("Уточните, проценты причисляются к сумме вклада: в последний календарный день месяца (Введите: 'К') или в последний рабочий день месяца (Введите 'Р')? ");
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


    private static BigDecimal getDepositAmount(Scanner scanner) {//ввод суммы вклада пользователем, проверка корректности формата введенной суммы
        BigDecimal depositAmount = new BigDecimal("-1");
        while (depositAmount.compareTo(new BigDecimal("0")) < 0) {
            System.out.print("Введите сумму вклада в рублях РФ (разделитель между рублями и копейками - запятая): ");
            String inputAmount = scanner.nextLine().replace(',', '.');
            try {
                depositAmount = new BigDecimal(inputAmount);

                if (depositAmount.compareTo(new BigDecimal("0")) < 0) {
                    System.out.println("Введенная сумма вклада некорректна (меньше нуля)!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число, например 15000,50: ");
                depositAmount = new BigDecimal("-1");
            }
        }
        return depositAmount;
    }

    private static BigDecimal getAnnualInterestRate(Scanner scanner) {//ввод процентной ставки пользователем, проверка корректности формата введенной ставки
        BigDecimal annualInterestRate = new BigDecimal("-1");
        while (annualInterestRate.compareTo(new BigDecimal("0")) < 0) {
            System.out.print("Введите процентную ставку (в % годовых без знака <%>, разделитель - запятая): ");
            String inputInterestRate = scanner.nextLine().replace(',', '.');
            try {
                annualInterestRate = new BigDecimal(inputInterestRate);

                if (annualInterestRate.compareTo(new BigDecimal("0")) < 0) {
                    System.out.println("Введенная процентная ставка некорректна (меньше нуля)!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число, например 11,55: ");
                annualInterestRate = new BigDecimal("-1");
            }
        }
        return annualInterestRate;
    }

    private static LocalDate askDate(Scanner scanner, String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        while (true) {
            System.out.print(message);
            try {
                return LocalDate.parse(scanner.nextLine(), formatter);
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

        BigDecimal totalInterestEarned = new BigDecimal("0");
        BigDecimal income; // данная переменная обозначает сумму начисленных процентов по вкладу, без суммы вклада

        LocalDate current = startDate;
        if (condition == CapitalizationCondition.YES_LAST_DAY_OF_MONTH) {
            while (current.isBefore(endDate)) {

                boolean isLeap = current.isLeapYear();

                YearMonth ym = YearMonth.from(current); // извлекаем год и месяц из даты, сохраняем в переменную ym
                LocalDate lastDayOfMonth = ym.atEndOfMonth(); // ищем последнюю дату месяца
                LocalDate nextPoint = lastDayOfMonth.isBefore(endDate) ? lastDayOfMonth.plusDays(1) : endDate;// по аналогии с предыдущим блоком

                long daysInThisMonth = ChronoUnit.DAYS.between(current, nextPoint);
                BigDecimal bigDecimalDaysInThisMonth = new BigDecimal(daysInThisMonth);

                income = (depositAmount.add(totalInterestEarned))
                        .multiply(annualInterestRate)
                        .multiply(bigDecimalDaysInThisMonth)
                        .multiply(PERCENTAGE_DIVISOR)
                        .divide(isLeap ? LEAP_YEAR_DAYS : NON_LEAP_YEAR_DAYS, 2, RoundingMode.HALF_UP);


                totalInterestEarned = totalInterestEarned.add(income); // скидываем проценты в счетчик

                current = nextPoint;// переходим на следующий месяц
            }

        } else if (condition == CapitalizationCondition.NO) {
            long leapYearDaysCount = 0L;
            long nonLeapYearDaysCount = 0L;

            /* Расчет без капитализации путем пересчета в счетчиках общего количества дней в високосных или невисокосных годах,
        а также последующего вычисления процентов по формуле расчета процентов по вкладу*/
            while (current.isBefore(endDate)) {
                boolean isLeap = current.isLeapYear();

                LocalDate yearEnd = LocalDate.of(current.getYear(), 12, 31); // находим последнюю дату года, соответствующего заданной дате current
                LocalDate nextPoint = yearEnd.isBefore(endDate) ? yearEnd.plusDays(1) : endDate; // если current меньше чем конечная дата, заданная пользователем, переходим на следующий год, иначе - на конечную дату

                long daysInThisYear = ChronoUnit.DAYS.between(current, nextPoint); // считаем количество дней на данном отрезке

                if (isLeap) {
                    leapYearDaysCount += daysInThisYear;
                } else {
                    nonLeapYearDaysCount += daysInThisYear;
                }

                current = nextPoint; // начинаем цикл заново со следующей точки
            }

            BigDecimal leapYearDays = new BigDecimal(leapYearDaysCount);
            BigDecimal nonLeapYearDays = new BigDecimal(nonLeapYearDaysCount);

            BigDecimal multipliedLeapYearDaysIncome =
                    depositAmount.multiply(annualInterestRate)
                            .multiply(leapYearDays)
                            .multiply(PERCENTAGE_DIVISOR)
                            .divide(LEAP_YEAR_DAYS, 2, RoundingMode.HALF_UP);

            BigDecimal multipliedNONLeapYearDaysIncome =
                    depositAmount.multiply(annualInterestRate)
                            .multiply(nonLeapYearDays)
                            .multiply(PERCENTAGE_DIVISOR)
                            .divide(NON_LEAP_YEAR_DAYS, 2, RoundingMode.HALF_UP);

            totalInterestEarned = multipliedLeapYearDaysIncome.add(multipliedNONLeapYearDaysIncome);

        } else if (condition == CapitalizationCondition.YES_LAST_WORKING_DAY_OF_MONTH) {
            System.out.println("В процессе разработки");
        }
        return totalInterestEarned;
    }
}
