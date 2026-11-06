package com.cineflow.util;

import com.cineflow.model.Booking;
import com.cineflow.model.OccupancyReportItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Utility for exporting report datasets to standard RFC-4180 CSV files.
 * Properly escapes commas, double quotes, and line breaks.
 */
public class CsvExporter {

    public static void exportBookingsToCsv(File file, List<Booking> bookings) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Booking ID,Customer Username,Customer Name,Movie,Auditorium,Showtime,Seats,Total Amount (INR),Booking Time,Status,Payment Method");

            for (Booking b : bookings) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        escape(String.valueOf(b.getId())),
                        escape(b.getUsername()),
                        escape(b.getCustomerName()),
                        escape(b.getMovieTitle()),
                        escape(b.getScreenName()),
                        escape(b.getFormattedShowtimeStart()),
                        escape(b.getSeatNumbers()),
                        escape(b.getTotalAmount().toPlainString()),
                        escape(b.getFormattedBookingTime()),
                        escape(b.getStatus().name()),
                        escape(b.getPaymentMethod() != null ? b.getPaymentMethod().name() : "N/A")
                );
            }
        }
    }

    public static void exportRevenueToCsv(File file, Map<LocalDate, BigDecimal> dailyRevenue, BigDecimal totalRevenue) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Date,Revenue (INR)");

            for (Map.Entry<LocalDate, BigDecimal> entry : dailyRevenue.entrySet()) {
                writer.printf("%s,%s%n",
                        escape(entry.getKey().toString()),
                        escape(entry.getValue().toPlainString())
                );
            }
            writer.printf("TOTAL REVENUE,%s%n", escape(totalRevenue.toPlainString()));
        }
    }

    public static void exportMovieSalesToCsv(File file, Map<String, Integer> movieSales) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Movie Title,Tickets Sold");

            for (Map.Entry<String, Integer> entry : movieSales.entrySet()) {
                writer.printf("%s,%d%n",
                        escape(entry.getKey()),
                        entry.getValue()
                );
            }
        }
    }

    public static void exportOccupancyToCsv(File file, List<OccupancyReportItem> items) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Showtime ID,Movie Title,Auditorium,Start Time,Booked Seats,Total Capacity,Occupancy Percentage");

            for (OccupancyReportItem item : items) {
                writer.printf("%d,%s,%s,%s,%d,%d,%.2f%%%n",
                        item.getShowtimeId(),
                        escape(item.getMovieTitle()),
                        escape(item.getScreenName()),
                        escape(item.getFormattedStartTime()),
                        item.getBookedSeats(),
                        item.getTotalSeats(),
                        item.getOccupancyPercentage()
                );
            }
        }
    }

    private static String escape(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
