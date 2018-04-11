package net.company.requesttest;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import report.RequestReport;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class ResponceResult {
    private volatile static Map<String, ResultEntry> requestsStatistic = new LinkedHashMap<String, ResultEntry>();
    private volatile static Set<Thread> threads = new HashSet<Thread>();


    private ResponceResult() {
    }

    public static void addThread(Thread thread) {
        threads.add(thread);
    }

    public static Set<Thread> getThreads() {
        return threads;
    }


    public static void initializeRequestStatistic(String[] urls, String[] keywords) {
        requestsStatistic.clear();
        for (int i1 = 0; i1 < urls.length; i1++) {
            String url = urls[i1];
            for (int i2 = 0; i2 < keywords.length; i2++) {
                String keyword = keywords[i2];
                String key = url + ";" + keyword;
                requestsStatistic.put(key, new ResultEntry());
            }
        }

    }


    public static void addResult(String url, String keyword, int statusCode, String responce, Long startTime, Long finishedTime, Thread thread) {
        String key = url + ";" + keyword;

        if (!requestsStatistic.containsKey(key)) {
            //requestsStatistic.put(key, new ResultEntry());
            throw new Error("Can't find request statistic for key " + key);
        }

        Long currentTime = finishedTime - startTime;

        ResultEntry resultEntry = requestsStatistic.get(key);
        resultEntry.count++;

        if (statusCode == MainApp.STATUS_CODE_SUCCESS) {
            resultEntry.countSuccessed++;
            resultEntry.commonTimeSuccessed = resultEntry.commonTimeSuccessed + currentTime;
            if (resultEntry.minTimeSuccessed == 0) {
                resultEntry.minTimeSuccessed = currentTime;
            } else {
                resultEntry.minTimeSuccessed = Math.min(resultEntry.minTimeSuccessed, currentTime);
            }

            resultEntry.maxTimeSuccessed = Math.max(resultEntry.maxTimeSuccessed, currentTime);
        } else if (statusCode >= 0) {
            resultEntry.countFailed++;
            resultEntry.commonTimeFailed = resultEntry.commonTimeFailed + currentTime;
            if (resultEntry.minTimeFailed == 0) {
                resultEntry.minTimeFailed = currentTime;
            } else {
                resultEntry.minTimeFailed = Math.min(resultEntry.minTimeFailed, currentTime);
            }

            resultEntry.maxTimeFailed = Math.max(resultEntry.maxTimeFailed, currentTime);
        } else {
            resultEntry.countTimeout++;
            resultEntry.commonTimeTimeout = resultEntry.commonTimeTimeout + currentTime;
            if (resultEntry.minTimeTimeout == 0) {
                resultEntry.minTimeTimeout = currentTime;
            } else {
                resultEntry.minTimeTimeout = Math.min(resultEntry.minTimeTimeout, currentTime);
            }

            resultEntry.maxTimeTimeout = Math.max(resultEntry.maxTimeTimeout, currentTime);
        }


        if(!threads.contains(thread)){
            throw new Error("Ошибка удаления потока " + thread + " из списка потоков");
        }

        threads.remove(thread);
    }

    public static void showResults() {
        System.out.println("Result:");
        for (Map.Entry<String, ResultEntry> entry : requestsStatistic.entrySet()) {
            ResultEntry resultEntry = entry.getValue();

            Double averageTimeSuccessed = 0D;
            Double averageTimeFailed = 0D;
            if (resultEntry.countSuccessed != 0) {
                averageTimeSuccessed = Double.valueOf(resultEntry.commonTimeSuccessed) / resultEntry.countSuccessed;
            }

            if (resultEntry.countFailed != 0) {
                averageTimeFailed = Double.valueOf(resultEntry.commonTimeFailed) / resultEntry.countFailed;
            }

            System.out.println("key: " + entry.getKey() + ": \n" +
                    "count = " + resultEntry.count + " \n" +
                    "common time successed = " + resultEntry.commonTimeSuccessed + " \n" +
                    "min time successed = " + resultEntry.minTimeSuccessed + " \n" +
                    "average time successed = " + averageTimeSuccessed + " \n" +
                    "max time failed = " + resultEntry.maxTimeFailed + " \n" +
                    "common time failed = " + resultEntry.commonTimeFailed + " \n" +
                    "min time failed = " + resultEntry.minTimeFailed + " \n" +
                    "average time failed = " + averageTimeFailed + " \n" +
                    "max time failed = " + resultEntry.maxTimeFailed + " \n"
            );

        }
    }

    public static void doReportXls() throws IOException, InvalidFormatException {
        LocalDateTime currentDateTime = LocalDateTime.now();

        LinkedHashMap<Integer, LinkedHashMap<Integer, String>> reportData = new LinkedHashMap<Integer, LinkedHashMap<Integer, String>>();
        LinkedHashMap<Integer, String> reportRowHeader = new LinkedHashMap<Integer, String>();

        reportRowHeader.put(2, currentDateTime.toString());
        reportData.put(1, reportRowHeader);

        int currentRowNumber = 3;
        Integer currentStringNumberForReport = 0;
        for (Map.Entry<String, ResultEntry> entry : requestsStatistic.entrySet()) {
            currentRowNumber++;
            currentStringNumberForReport++;
            ResultEntry resultEntry = entry.getValue();

            Double averageTimeSuccessed = 0D;
            Double averageTimeFailed = 0D;
            Double averageTimeTimeout = 0D;
            if (resultEntry.countSuccessed != 0) {
                averageTimeSuccessed = Double.valueOf(resultEntry.commonTimeSuccessed) / resultEntry.countSuccessed;
            }

            if (resultEntry.countFailed != 0) {
                averageTimeFailed = Double.valueOf(resultEntry.commonTimeFailed) / resultEntry.countFailed;
            }

            if (resultEntry.countTimeout != 0) {
                averageTimeTimeout = Double.valueOf(resultEntry.commonTimeTimeout) / resultEntry.countTimeout;
            }

            String keys[] = entry.getKey().split(";");

            Double percentCompleted = 100 * resultEntry.countSuccessed.doubleValue() / resultEntry.count.doubleValue();


            LinkedHashMap<Integer, String> reportRowString = new LinkedHashMap<Integer, String>();
            reportRowString.put(1, currentStringNumberForReport.toString());//№
            reportRowString.put(2, keys[0]);//Server
            reportRowString.put(3, keys[1]);//SearchWord
            reportRowString.put(4, resultEntry.count.toString());//Count
            reportRowString.put(5, resultEntry.countSuccessed.toString());//Completed
            reportRowString.put(6, resultEntry.countFailed.toString());//Errors
            reportRowString.put(7, resultEntry.countTimeout.toString());//timeouts
            reportRowString.put(8, percentCompleted.toString());//% completed
            reportRowString.put(9, resultEntry.minTimeSuccessed.toString());//min_time success
            reportRowString.put(10, averageTimeSuccessed.toString());//average_time success
            reportRowString.put(11, resultEntry.maxTimeSuccessed.toString());//max_time success
            reportRowString.put(12, resultEntry.minTimeFailed.toString());//min_time failed
            reportRowString.put(13, averageTimeFailed.toString());//average_time failed
            reportRowString.put(14, resultEntry.maxTimeFailed.toString());//max time failed
            reportRowString.put(15, resultEntry.minTimeTimeout.toString());//min_time failed
            reportRowString.put(16, averageTimeTimeout.toString());//average_time failed
            reportRowString.put(17, resultEntry.maxTimeTimeout.toString());//max time failed


            reportData.put(currentRowNumber, reportRowString);
        }

        RequestReport.createReport(reportData);


    }


    private static class ResultEntry {
        private Long count = 0L;
        private Long countSuccessed = 0L;
        private Long commonTimeSuccessed = 0L;
        private Long minTimeSuccessed = 0L;
        private Long maxTimeSuccessed = 0L;

        private Long commonTimeFailed = 0L;
        private Long countFailed = 0L;
        private Long minTimeFailed = 0L;
        private Long maxTimeFailed = 0L;

        private Long commonTimeTimeout = 0L;
        private Long countTimeout = 0L;
        private Long minTimeTimeout = 0L;
        private Long maxTimeTimeout = 0L;

        public ResultEntry() {
        }
    }




}