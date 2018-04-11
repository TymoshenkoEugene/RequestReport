package net.company.requesttest;


import Service.RequestRunnerService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.*;
import java.util.Properties;


public class MainApp {
    private static Properties properties;
    //private static String url;
    private static String jsonDataTemplate;
    private static int delayBetweenThreads;
    private static int threadsCount;
    private static int threadTimeoutSec = 0;

    public static int STATUS_CODE_SUCCESS = 200;


    public static void main(String args[]) {
        fillProperties();

        String urlsString = properties.getProperty("server.urls");
        jsonDataTemplate = properties.getProperty("content.body");
        threadsCount = Integer.parseInt(properties.getProperty("threads.count"));
        delayBetweenThreads = Integer.parseInt(properties.getProperty("threads.delay"));
        threadTimeoutSec = Integer.parseInt(properties.getProperty("threads.timeoutsec"));
        String keywordsString = properties.getProperty("searchwords");

        String keywords[] = keywordsString.split(";");
        String urls[] = urlsString.split(";");
        ResponceResult.initializeRequestStatistic(urls, keywords);

        for (int urlKey = 0; urlKey < urls.length; urlKey++) {
            String url = urls[urlKey];
            for (int iKey = 0; iKey < keywords.length; iKey++) {
                String keyword = keywords[iKey];
                for (int i = 1; i <= threadsCount; i++) {
                    try {
                        Thread.sleep(delayBetweenThreads);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String jsonData = jsonDataTemplate.replaceAll("@searchword", keyword);
                    new RequestRunnerService(jsonData, url, keyword,threadTimeoutSec);
                }
            }

            int tryCounter = 0;
            while (ResponceResult.getThreads().size() != 0) {
                try {
                    System.out.println("По серверу " + url + " есть незавершенные потоки. Кво " + ResponceResult.getThreads().size());
                    Thread.sleep(1000);
                    tryCounter++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*Set<Thread> threads = ResponceResult.getThreads();
                for (Thread thread:threads){
                    if(thread.isAlive())
                }*/



            }
        }


        ResponceResult.showResults();
        try {
            ResponceResult.doReportXls();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }


    private static void fillProperties() {
        properties = new Properties();
        InputStream input = null;

        try {

            String filename = "config.properties";
            input = MainApp.class.getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                System.out.println("Sorry, unable to find " + filename);
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

