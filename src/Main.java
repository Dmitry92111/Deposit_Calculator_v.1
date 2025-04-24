import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {
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

        double totalWithdrawSum = depositAmount + totalInterestEarned;
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


    private static double getDepositAmount(Scanner scanner) {//ввод суммы вклада пользователем, проверка корректности формата введенной суммы
        double depositAmount = -1;
        while (depositAmount < 0) {
            System.out.print("Введите сумму вклада в рублях РФ (разделитель между рублями и копейками - запятая): ");
            String inputAmount = scanner.nextLine().replace(',', '.');
            try {
                depositAmount = Double.parseDouble(inputAmount);

                if (depositAmount < 0) {
                    System.out.println("Введенная сумма вклада некорректна (меньше нуля)!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число, например 15000,50: ");
                depositAmount = -1;
            }
        }
        return depositAmount;
    }

    private static double getAnnualInterestRate(Scanner scanner) {//ввод процентной ставки пользователем, проверка корректности формата введенной ставки
        double annualInterestRate = -1;
        while (annualInterestRate < 0) {
            System.out.print("Введите процентную ставку (в % годовых без знака <%>, разделитель - запятая): ");
            String inputInterestRate = scanner.nextLine().replace(',', '.');
            try {
                annualInterestRate = Double.parseDouble(inputInterestRate);

                if (annualInterestRate < 0) {
                    System.out.println("Введенная процентная ставка некорректна (меньше нуля)!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число, например 11,55: ");
                annualInterestRate = -1;
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


    private static double calculationOfInterestOnDeposit
            (CapitalizationCondition condition,
             double depositAmount,
             double annualInterestRate,
             LocalDate startDate,
             LocalDate endDate) {

        double totalInterestEarned = 0;
        double income; // данная переменная обозначает сумму начисленных процентов по вкладу, без суммы вклада

        LocalDate current = startDate;
        if (condition == CapitalizationCondition.YES_LAST_DAY_OF_MONTH) {
            while (current.isBefore(endDate)) {

                boolean isLeap = current.isLeapYear();

                YearMonth ym = YearMonth.from(current); // извлекаем год и месяц из даты, сохраняем в переменную ym
                LocalDate lastDayOfMonth = ym.atEndOfMonth(); // ищем последнюю дату месяца
                LocalDate nextPoint = lastDayOfMonth.isBefore(endDate) ? lastDayOfMonth.plusDays(1) : endDate;// по аналогии с предыдущим блоком

                long daysInThisMonth = ChronoUnit.DAYS.between(current, nextPoint);

                if (isLeap) {
                    income = (depositAmount + totalInterestEarned) * annualInterestRate * daysInThisMonth / 366 / 100;

                } else {
                    income = (depositAmount + totalInterestEarned) * annualInterestRate * daysInThisMonth / 365 / 100;

                }
                income = Math.round(income * 100.0) / 100.0; //округляем проценты

                totalInterestEarned += income; // скидываем проценты в счетчик

                current = nextPoint;// переходим на следующий месяц
            }

        } else if (condition == CapitalizationCondition.NO) {
            long leapYearDays = 0L;
            long nonLeapYearDays = 0L;

            /* Расчет без капитализации путем пересчета в счетчиках общего количества дней в високосных или невисокосных годах,
        а также последующего вычисления процентов по формуле расчета процентов по вкладу*/
            while (current.isBefore(endDate)) {
                boolean isLeap = current.isLeapYear();

                LocalDate yearEnd = LocalDate.of(current.getYear(), 12, 31); // находим последнюю дату года, соответствующего заданной дате current
                LocalDate nextPoint = yearEnd.isBefore(endDate) ? yearEnd.plusDays(1) : endDate; // если current меньше чем конечная дата, заданная пользователем, переходим на следующий год, иначе - на конечную дату

                long daysInThisYear = ChronoUnit.DAYS.between(current, nextPoint); // считаем количество дней на данном отрезке

                if (isLeap) {
                    leapYearDays += daysInThisYear;
                } else {
                    nonLeapYearDays += daysInThisYear;
                }

                current = nextPoint; // начинаем цикл заново со следующей точки
            }

            totalInterestEarned = (depositAmount * annualInterestRate * leapYearDays / 366 / 100) + (depositAmount * annualInterestRate * nonLeapYearDays / 365 / 100); // считаем проценты

        } else if (condition == CapitalizationCondition.YES_LAST_WORKING_DAY_OF_MONTH) {
            System.out.println("В процессе разработки");
        }
        return totalInterestEarned;
    }
}
