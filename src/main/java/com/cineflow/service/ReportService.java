package com.cineflow.service;

import com.cineflow.dao.ReportDAO;
import com.cineflow.model.OccupancyReportItem;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service providing aggregated financial and performance reporting.
 */
public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public ReportService(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    public BigDecimal getTotalRevenue() throws SQLException {
        return reportDAO.getTotalRevenue();
    }

    public int getTodayBookingsCount() throws SQLException {
        return reportDAO.getTodayBookingsCount();
    }

    public Map<LocalDate, BigDecimal> getSevenDayRevenue() throws SQLException {
        return reportDAO.getSevenDayRevenue();
    }

    public Map<String, Integer> getTopMoviesByTicketSales() throws SQLException {
        return reportDAO.getTopMoviesByTicketSales();
    }

    public List<OccupancyReportItem> getShowtimeOccupancy() throws SQLException {
        return reportDAO.getShowtimeOccupancy();
    }
}
