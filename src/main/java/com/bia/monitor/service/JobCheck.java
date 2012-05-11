/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bia.monitor.service;

import com.bia.monitor.email.EmailServiceImpl;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author intesar
 */
class JobCheck implements Runnable {

    private Job job;
    protected static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JobCheck.class);

    JobCheck(Job job) {
        this.job = job;
    }

    public void run() {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace(" pinging " + job.getUrl());
            }
            HttpURLConnection connection = (HttpURLConnection) new URL(job.getUrl()).openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // OK.
                if (logger.isTraceEnabled()) {
                    logger.trace(" ping successful " + job.getUrl());
                }
                if (!job.isLastUp()) {
                    job.setLastUp(true);
                    // send site up notification
                    int mins = (int) ((new Date().getTime() / 60000) - (job.getDownSince().getTime() / 60000));
                    StringBuilder body = new StringBuilder();

                    body.append(job.getUrl()).append(" is Up after ").append(mins).append(" mins of downtime!");
                    EmailServiceImpl.getInstance().sendEmail(job.getEmail(), job.getUrl() + " is Up!", "");
                }
                return;
            }

            // < 100 is undertermined.
            // 1nn is informal (shouldn't happen on a GET/HEAD)
            // 2nn is success
            // 3nn is redirect
            // 4nn is client error
            // 5nn is server error
        } catch (IOException ex) {
            Logger.getLogger(JobCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (logger.isTraceEnabled()) {
            logger.trace(" ping failed " + job.getUrl());
        }
        job.setLastUp(false);
        job.setDownSince(new Date());
        // send alert email
        StringBuilder body = new StringBuilder();
        body.append(job.getUrl()).append(" is Down! ");
        EmailServiceImpl.getInstance().sendEmail(job.getEmail(), job.getUrl() + " is Down!", "");

    }
}
