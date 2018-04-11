package Service;

import net.company.requesttest.ResponceResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.*;

public class RequestRunnerService implements Callable<Boolean>, Runnable {
    private String json;
    private String url;
    private String keyword;
    private static Long counter = 0L;
    private Long currentThread;
    private Long startTime;
    private Long finishedTime;
    private int threadTimeoutSec;
    private Thread currThread;

    public RequestRunnerService(String json, String url, String keyword, int threadTimeoutSec) {
        startTime = System.currentTimeMillis();
        this.json = json;
        this.url = url;
        this.keyword = keyword;
        this.threadTimeoutSec = threadTimeoutSec;
        currentThread = ++counter;

        System.out.println("new runner service " + this);

        Thread thread = new Thread(this, "ThreadOf-" + currentThread);
        thread.setDaemon(true);
        ResponceResult.addThread(thread);
        currThread = thread;
        thread.start();

    }

    public void run() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit((Callable<Boolean>) this);

        Long currentStartTime = System.currentTimeMillis();

        try {
            //System.out.println("Started..");
            try {
                System.out.println(future.get(threadTimeoutSec, TimeUnit.SECONDS).toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            finishedTime = System.currentTimeMillis();
            future.cancel(true);
            //future.cancel(false);
            System.out.println("future is done = " + future.isDone());
            //future = null;

            Long spendedTime = (finishedTime - currentStartTime) / 1000;
            System.out.println(this + " terminated by timeout limit!. " + spendedTime);
            ResponceResult.addResult(url, keyword, -4, "timeout exception", startTime, finishedTime, currThread);
        }

        executor.shutdownNow();
        //executor.shutdown();

        System.out.println("is shutdowned = " + executor.isShutdown());
        System.out.println("isTerminated = " + executor.isTerminated());

        try {
            executor.awaitTermination(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e ){
            e.printStackTrace();
            System.out.println(e.toString());
        }
        System.out.println("is shutdowned = " + executor.isShutdown());
        System.out.println("isTerminated = " + executor.isTerminated());
    }


    HttpPost createConnection(String restUrl) {
        HttpPost post = new HttpPost(restUrl);
        post.setHeader("Content-Type", "application/json");
        return post;
    }

    void executeReq(String jsonData, HttpPost httpPost) {
        try {
            executeHttpRequest(jsonData, httpPost);
        } catch (UnsupportedEncodingException e) {
            String currentResult = "error while encoding api url : " + e;
            finishedTime = System.currentTimeMillis();
            ResponceResult.addResult(url, keyword, -1, currentResult, startTime, finishedTime, currThread);
        } catch (IOException e) {
            String currentResult = "ioException occured while sending http request : " + e;
            finishedTime = System.currentTimeMillis();
            ResponceResult.addResult(url, keyword, -2, currentResult, startTime, finishedTime, currThread);
        } catch (Exception e) {
            String currentResult = "exception occured while sending http request : " + e;
            finishedTime = System.currentTimeMillis();
            ResponceResult.addResult(url, keyword, -3, currentResult, startTime, finishedTime, currThread);
        } finally {
            httpPost.releaseConnection();
        }
    }

    void executeHttpRequest(String jsonData, HttpPost httpPost) throws UnsupportedEncodingException, IOException {
        HttpResponse response = null;
        String line = "";
        StringBuffer result = new StringBuffer();
        httpPost.setEntity(new StringEntity(jsonData));
        HttpClient client = HttpClientBuilder.create().build();
        response = client.execute(httpPost);
        //System.out.println("Post parameters : " + jsonData);
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        String currentResult = result.toString();

        System.out.println(currentResult);
        finishedTime = System.currentTimeMillis();
        ResponceResult.addResult(url, keyword, statusCode, currentResult, startTime, finishedTime, currThread);
    }

    @Override
    public String toString() {
        return "Runner № " + currentThread + "(" + url + ";" + keyword + ")";
    }

    @Override
    public Boolean call() throws Exception {
        System.out.println("Runner service " + this + " started");
        HttpPost httpPost = createConnection(url);
        executeReq(json, httpPost);
        System.out.println("Runner service " + this + " finished");
        return true;
    }
}
