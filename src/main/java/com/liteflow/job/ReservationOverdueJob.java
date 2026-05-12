package com.liteflow.job;

import com.liteflow.service.inventory.ReservationService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Reservation Overdue Job
 * Scheduled job that runs every 5 minutes to check and mark overdue reservations
 * as NO_SHOW (reservations that are more than 30 minutes past arrival time)
 */
public class ReservationOverdueJob implements Job {

    private static final int OVERDUE_THRESHOLD_MINUTES = 30;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("===========================================");
        System.out.println("🕐 Reservation Overdue Job Started");
        System.out.println("   Time: " + LocalDateTime.now().format(FORMATTER));
        System.out.println("===========================================");

        try {
            ReservationService reservationService = new ReservationService();
            
            // Check and mark overdue reservations
            int count = reservationService.autoCheckOverdue();
            
            System.out.println("✅ Reservation Overdue Job Completed");
            System.out.println("   Checked and marked " + count + " overdue reservations as NO_SHOW");
            System.out.println("   Threshold: " + OVERDUE_THRESHOLD_MINUTES + " minutes");
            System.out.println("===========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Error in Reservation Overdue Job:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
            System.out.println("===========================================");
            throw new JobExecutionException("Reservation overdue job failed", e);
        }
    }
}

