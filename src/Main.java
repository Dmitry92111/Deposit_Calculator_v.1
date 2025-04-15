import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class Main {
    public static void main (String[] args) {

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Происходит ли капитализация процентов по вкладу в последний календарный день месяца? (Введите Да или Нет): ");
        String capitalization = scanner.nextLine();

        if(!capitalization.equals("Нет") && !capitalization.equals("Да")) {

            System.out.println("Неизвестная команда в строке в строке ввода капитализации");

        } else {

            System.out.print("Введите сумму вклада в рублях РФ (разделитель между рублями и копейками - запятая): ");
            double amount = scanner.nextDouble();
            scanner.nextLine();

            if (amount < 0) {

                System.out.println("Введенная сумма вклада некорректна!");

            } else {

                System.out.print("Введите процентную ставку (в % годовых без знака <%>, разделитель - запятая): ");
                double interestRate = scanner.nextDouble();
                scanner.nextLine();

                if (interestRate < 0) {

                    System.out.println("Введенная процентная ставка некорректна!");

                }  else {

                    double totalIncome = 0;

                    // Ввод двух дат пользователем
                    System.out.print("Введите дату открытия вклада (в формате ГГГГ-ММ-ДД): ");
                    LocalDate startDate = LocalDate.parse(scanner.nextLine());

                    System.out.print("Введите дату окончания срока вклада (в формате ГГГГ-ММ-ДД): ");
                    LocalDate endDate = LocalDate.parse(scanner.nextLine());

                    // Убеждаемся, что startDate раньше endDate, в противном случае - меняем местами
                    if (startDate.isAfter(endDate)) {
                        LocalDate temp = startDate;
                        startDate = endDate;
                        endDate = temp;
                    }

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

                            amount = amount + totalIncome;

                            if (isLeap) {
                                income = amount * interestRate * daysInThisMonth / 366 / 100;

                            } else {
                                income = amount * interestRate * daysInThisMonth / 365 / 100;

                            }
                            amount = amount - totalIncome;// приводим сумму в первоначальное значение

                            income = Math.round(income * 100.0) / 100.0; //округляем проценты

                            totalIncome = totalIncome + income; // скидываем проценты в счетчик

                            current = nextPoint;// переходим на следующий месяц
                        }
                    }
                    double totalWithdrawSum;
                    totalWithdrawSum = amount + totalIncome;
                    String IncomeCommaFormat = decimalFormat.format (totalIncome); // для формата вывода данных через запятую + округление
                    String totalWithdrawSumCommaFormat = decimalFormat.format(totalWithdrawSum); // для формата вывода данных через запятую + округление
                    System.out.println("Вы заработали:" + " " + IncomeCommaFormat + " " + "рублей");
                    System.out.println("Сумма, доступная к выводу:" + " " + totalWithdrawSumCommaFormat + " " + "рублей");
                }
            }
        }
    }
}