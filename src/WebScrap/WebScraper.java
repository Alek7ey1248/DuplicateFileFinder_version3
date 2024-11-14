package WebScrap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class WebScraper {
    public static void main(String[] args) {
        String url = "https://minfin.com.ua/currency/auction/exchanger/eur/buy/kiev/";

        try {
            // Fetch the HTML code
            Document document = Jsoup.connect(url).get();

            // Select the elements you want to scrape
            //Elements elements = document.select("h1");

            Elements elements = document.select("span.Typography.cardHeadlineL.align");
            // <span class="Typography cardHeadlineL align" align="left" variant="cardHeadlineL">44,45<div class="diff"></div></span>

            // Print the scraped data
            for (Element element : elements) {
                System.out.println(element.text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
