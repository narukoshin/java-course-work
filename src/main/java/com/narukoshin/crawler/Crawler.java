package com.narukoshin.crawler;

import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    /**
     * The instance of the BrainStorm class.
     * */
    private BrainStorm BrainStorm;

    /**
     * This will launch the crawler.
     *
     * @param Running boolean
     * */
    public void StartCrawler(boolean Running){
        // While Running is true, crawler will do its job. xd
        if (Running) {
            System.out.println("Starting Crawler...");
            // Crawling the website
            _execute();
        }
    }

    /**
     * This function will create an instance of the BrainStorm class.
     *
     * @param b BrainStorm
     * */
    public void createReference(BrainStorm b){
        BrainStorm = b;
    }
    /**
     * Function where the crawling itself is called
     * and where all the operations are happening.
     *  */
    public void _execute(){
        // Getting config instance
        Config c = Config.getInstance();

        // Query that will be used for finding websites
        String query;

        // Additional parameters that will be passed in the url.
        String additionalParameters;

        // Cookie value
        String cookieValue;

        // trying to load the settings from the database.
        // if it fails, we are loading the default one.
        try {
            query = c.getSetting("queryString");
            additionalParameters = c.getSetting("additionalParameters");
            cookieValue = c.getSetting("cookieValue");
        } catch (SQLException e) {
            e.printStackTrace();
            query = Config.queryString;
            additionalParameters = Config.additionalParameters;
            cookieValue = Config.cookieValue;
        }

        System.out.println(query);

        // Websites per one page.
        int num = 100;

        // When we finished with the page, we are adding num variable to this variable to load the websites from the next page.
        int numPage = 0;

        // Delay in seconds before sending a next request to the Google.
        int secondsDelay = 15;

        while (Config.isRunning) {
            // Building Google GET request
            String google = String.format("https://www.google.com/search?num=%d&start=%d%s&q=", num, numPage, additionalParameters);
            // Declaring the User-Agent value.
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3";

            String fullUrl = google + query;

            System.out.println(fullUrl);

            Connection conn = Jsoup.connect(fullUrl);

            // Adding some headers to not look like a sussy bakka.
            conn.userAgent(userAgent);
            conn.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            conn.header("Accept-Language", "en-US,en;q=0.5");
            conn.header("Connection", "keep-alive");
            conn.header("DNT", "1");
            conn.header("Cookie", cookieValue);
            conn.referrer("https://www.google.com/");

            Document doc;
            try {
                doc = conn.get();
            } catch (IOException e) {
                BrainStorm.stopCrawlerExecution();
                e.printStackTrace();
                return;
            }

            // Checking if doc is not null.
            if (doc.toString().contains("captcha-form")){
                System.out.println("Pizdets");
            }

            Elements links = doc.select(".yuRUbf a");

            // Checking if there's links
            if (links.size() == 0) {
                // If there are no links, we are stopping the crawling.
                BrainStorm.stopCrawlerExecution();
                return;
            }

            System.out.println(links.size());


            // Print the URLs
            for (org.jsoup.nodes.Element link : links) {
                // Getting the value of href if there is any.
                String text = link.attr("href");
                // Skipping all domains that contains translate.google.com, because we don't need those.
                if (text.contains("translate.google.com")) {
                    continue;
                }
                // Filtering out all the links
                String regex = "https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    // Printing out all matches
                    // If we can read the .git folder that means we can get the source code of the website that may contain important credentials of information.

                    String url = null;
                    try {
                        url = filterCrawlURL(matcher.group());
                    } catch (URISyntaxException e) {
                        System.out.println("Kaut kas tur");
                    }

                    boolean isGithubFolderFound = false;
                    try {
                        isGithubFolderFound = searchGithubFolder(url);
                    } catch (IOException e) {
                        System.out.println("Tur atkal kaut kas tur");
                    } catch (NoSuchElementException e) {
                        System.out.println("NoSuchElementException");
                    }


                    if (isGithubFolderFound) {
                        // Before adding data to the table
                        // Checking if the url isn't already in the list
                        boolean duplication = false;
                        for (TableCollection item : BrainStorm.TableCollection.getItems()) {
                            if (item.getUrl().equals(url)) {
                                duplication = true;
                                break;
                            }
                        }
                        // If there's a duplicate in the table, skipping the following code.
                        if (duplication) continue;
                        // Counting the links.
                        BrainStorm.count++;
                        // Adding data to the table.
                        TableCollection data = new TableCollection(BrainStorm.count, url);
                        BrainStorm.TableCollection.getItems().add(data);
                        BrainStorm.TableCollection.refresh();
                        System.out.println("URL found: " + url);
                    }
                }
            }

            numPage = numPage + num;

            // Debug message
            System.out.println(numPage);

            try {
                Thread.sleep(secondsDelay * 1000);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted");
            }
        }
    }
    // This will search for the GitHub folder for example website.com/.git
    // If it responses 404, then we are going to the next url, but if it's 403, then we are trying to read .git/HEAD file to check if it's readable.
    /**
     * A function that will check if there's a readable .git folder on the web server.
     * If there is a readable .git folder, it will return true.
     *
     * @param urlString String
     * @return boolean
     * */
    private boolean searchGithubFolder(String urlString) throws IOException {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3";
        URL gitUrl = new URL(urlString + "/.git/HEAD");
        HttpURLConnection connection = (HttpURLConnection) gitUrl.openConnection();
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestMethod("GET");
        int status = connection.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(connection.getInputStream());
            String content = scanner.useDelimiter("\\A").next();
            scanner.close();
            if (content.contains("ref: refs/heads/")){
                System.out.println("HEAD file is readable.");
                return true;
            }
        } else {
            System.out.println(".git/HEAD file is not readable. Response code: " + status);
        }
        return false;
    }
    /**
     * Sometimes there are websites with additional things I don't want, so we have to filter them out.
     * Basically, this function leaves only domain, like https://google.com if url is like https://google.com/search/?q=something
     *
     * @param url String    URL to filter out
     * @throws URISyntaxException
     * */
    private String filterCrawlURL(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        String filteredUrl = scheme + "://" + host;

        // If there is a port, adding it as well.
        if (port != -1) {
            filteredUrl += ":" + port;
        }
        return filteredUrl;
    }
}
