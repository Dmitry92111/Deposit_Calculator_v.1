import java.util.Scanner;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {
    public static void main(String[] args) {

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        Scanner scanner = new Scanner(System.in);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        System.out.print("Происходит ли капитализация процентов по вкладу в последний календарный день месяца? (Введите Да или Нет): ");
        String capitalization = scanner.nextLine();

        while (!capitalization.equals("Нет") && !capitalization.equals("Да")) {
            System.out.println("Неизвестная команда в строке ввода");
            System.out.print("Происходит ли капитализация процентов по вкладу в последний календарный день месяца? (Введите Да или Нет): ");
            capitalization = scanner.nextLine();
        }

        //ввод суммы вклада пользователем, проверка корректности формата введенной суммы
        double amount = -1;
        while (amount < 0) {
            System.out.print("Введите сумму вклада в рублях РФ (разделитель между рублями и копейками - запятая): ");
            String inputAmount = scanner.nextLine().replace(',', '.');
            try {
                amount = Double.parseDouble(inputAmount);

                if (amount < 0) {
                    System.out.println("Введенная сумма вклада некорректна (меньше нуля)!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число, например 15000,50: ");
                amount = -1;
            }
        }

        //ввод процентной ставки пользователем, проверка корректности формата введенной ставки
        double interestRate = -1;
        while (interestRate < 0) {
            System.out.print("Введите процентную ставку (в % годовых без знака <%>, разделитель - запятая): ");
            String inputInterestRate = scanner.nextLine().replace(',', '.');
            try {
                interestRate = Double.parseDouble(inputInterestRate);

                if (interestRate < 0) {
                    System.out.println("Введенная процентная ставка некорректна (меньше нуля)!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число, например 11,55: ");
                interestRate = -1;
            }
        }


        // Ввод двух дат пользователем, проверка корректности вводимых дат и их формата
        LocalDate startDate = null;
        while (startDate == null) {
            System.out.print("Введите дату открытия вклада (в формате ДД.ММ.ГГГГ): ");
            String inputStartDate = scanner.nextLine();
            try {
                startDate = LocalDate.parse(inputStartDate, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты! Попробуйте снова");
            }
        }

        LocalDate endDate = null;
        while (endDate == null) {
            System.out.print("Введите дату окончания срока вклада (в формате ДД.ММ.ГГГГ): ");
            String inputEndDate = scanner.nextLine();
            try {
                endDate = LocalDate.parse(inputEndDate, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты! Попробуйте снова");
            }
        }

        // Убеждаемся, что startDate раньше endDate, в противном случае - меняем местами
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
            System.out.println("Введенная дата открытия вклада позже даты окончания срока вклада, даты поменялись местами");
        }

        double totalIncome = 0;
        double income; // данная переменная обозначает сумму начисленных процентов по вкладу, без суммы вклада

        /* Расчет без капитализации путем пересчета в счетчиках общего количества дней в високосных или невисокосных годах,
        а также последующего вычисления процентов по формуле расчета процентов по вкладу*/

        if (capitalization.equals("Нет")) {
            long leapYearDays = 0L;
            long nonLeapYearDays = 0L;

            LocalDate current = startDate;

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

            totalIncome = (amount * interestRate * leapYearDays / 366 / 100) + (amount * interestRate * nonLeapYearDays / 365 / 100); // считаем проценты

        } else {
            LocalDate current = startDate; // считаем по аналогии с предыдущим блоком только в последнюю дату месяца прибавляем начисленные проценты за текущий месяц (капитализация)

            while (current.isBefore(endDate)) {

                boolean isLeap = current.isLeapYear();

                YearMonth ym = YearMonth.from(current); // извлекаем год и месяц из даты, сохраняем в переменную ym
                LocalDate lastDayOfMonth = ym.atEndOfMonth(); // ищем последнюю дату месяца
                LocalDate nextPoint = lastDayOfMonth.isBefore(endDate) ? lastDayOfMonth.plusDays(1) : endDate;// по аналогии с предыдущим блоком

                long daysInThisMonth = ChronoUnit.DAYS.between(current, nextPoint);

                if (isLeap) {
                    income = (amount + totalIncome) * interestRate * daysInThisMonth / 366 / 100;

                } else {
                    income = (amount + totalIncome) * interestRate * daysInThisMonth / 365 / 100;

                }
                income = Math.round(income * 100.0) / 100.0; //округляем проценты

                totalIncome = totalIncome + income; // скидываем проценты в счетчик

                current = nextPoint;// переходим на следующий месяц
            }
        }
        double totalWithdrawSum;
        totalWithdrawSum = amount + totalIncome;
        String incomeCommaFormat = decimalFormat.format(totalIncome); // для формата вывода данных через запятую + округление
        String totalWithdrawSumCommaFormat = decimalFormat.format(totalWithdrawSum); // для формата вывода данных через запятую + округление
        System.out.println("Вы заработали:" + " " + incomeCommaFormat + " " + "рублей");
        System.out.println("Сумма, доступная к выводу:" + " " + totalWithdrawSumCommaFormat + " " + "рублей");
    }
}