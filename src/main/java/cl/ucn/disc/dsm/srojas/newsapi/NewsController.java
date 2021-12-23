/*
 * Copyright 2021 Sebastián Rojas Rodriguez sebastian.rojas04@alumnos.ucn.cl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package cl.ucn.disc.dsm.srojas.newsapi;

import cl.ucn.disc.dsm.srojas.newsapi.model.News;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import com.kwabenaberko.newsapilib.network.APIClient;
import com.kwabenaberko.newsapilib.network.APIService;
import lombok.extern.slf4j.Slf4j;
import net.openhft.hashing.LongHashFunction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Controller of News
 * @author Sebastián Rojas
 */
@Slf4j
@RestController
public class NewsController {

    /**
     * The Repository of News
     */
    private final NewsRepository newsRepository;

    /**
     * The constructor of NewsController.
     * @param newsRepository to use.
     */
    public NewsController(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    /**
     * @return all the News in the backend.
     */
    @GetMapping("/v1/news")
    public List<News> all(@RequestParam(required = false, defaultValue = "false") Boolean reload) {

        log.info("Request all with reload: {}.", reload);

        // Is reload -> get news from NewsAPI.org
        if(reload) {
            // FIXME: Avoid the duplicated.
            this.reloadNewsFromNewsApi();
        }

        // Equals to SELECT * FROM News;
        final List<News> theNews = this.newsRepository.findAll();
        // TODO: Show the news in console
        return theNews;
    }

    /**
     * Get the News from NewsAPI and save into the database.
     */
    private void reloadNewsFromNewsApi() {

        // WARNING: just for test
        final String API_KEY = "db3ae6e6a243437e9e2d7e28d50257aa";
        final int pageSize = 100;

        // 1. Create the APIService from APIClient
        final APIService apiService = APIClient.getAPIService();

        // 2. Build the request params
        final Map<String, String> reqParams = new HashMap<>();
        // The ApiKey
        reqParams.put("apiKey", API_KEY);
        // Filter by category
        reqParams.put("category", "technology");
        // The numbers of results to return per page (request). 20 is the default, 100 is the maximum.
        reqParams.put("pageSize", String.valueOf(pageSize));

        // 3. Call the request
        try {
            Response<ArticleResponse> articlesResponse =  apiService.getTopHeadlines(reqParams).execute();

            // Success
            if(articlesResponse.isSuccessful()) {
                // TODO: Check for getArticles != null
                List<Article> articles = articlesResponse.body().getArticles();

                List<News> news = new ArrayList<>();
                // List<Article> to List<News>
                if(articles != null) {
                    for (Article article : articles) {

                        Long hashId = LongHashFunction.xx().hashChars(article.getTitle()
                                + "|" + article.getSource() + "|" + article.getAuthor());
                            news.add(toNews(article));
                    }
                }

                // 4. Save into the local database
                this.newsRepository.saveAll(news);

            }

        } catch (IOException e) {
            log.error("Getting the Articles from NewsAPI", e);
        }
    }

    /**
     * Convert a Article to News.
     * @param article to convert.
     * @return The News.
     */
    private static News toNews(final Article article) {

        // Protection: author
        if (article.getAuthor() == null || article.getAuthor().length() < 3) {
            article.setAuthor("No Author*");
        }

        // Protection: title
        if (article.getTitle() == null || article.getTitle().length() < 3) {
            article.setTitle("No Title*");
        }

        // Protection: description
        if (article.getDescription() == null || article.getDescription().length() < 4) {
            article.setDescription("No Title*");
        }

        // Parse the date and fix the zone
        ZonedDateTime publishedAt = ZonedDateTime
                .parse(article.getPublishedAt())
                // Correct from UTC to LocalTime (Chile)
                .withZoneSameInstant(ZoneId.of("-3"));

        return new News(
                article.getTitle(),
                article.getSource().getName(),
                article.getAuthor(),
                article.getUrl(),
                article.getUrlToImage(),
                article.getDescription(),
                article.getDescription(),
                publishedAt
        );
    }

    /**
     * @param id of News to retrieve.
     * @return the News.
     */
    @GetMapping("/v1/news/{id}")
    public News one(@PathVariable final Long id) {
        // FIXME: Change the RuntimeException to 404.
        return this.newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News Not Found: ("));



        /*News news = new News(
                "Titulo",
                "Fuente",
                "Seba Rojas",
                "https://www.noticias.ucn.cl/destacado/con-entrega-de-reconocimientos-se-desarrollo-x-encuentro-de-investigacion-ucn/",
                "https://www.noticias.ucn.cl/wp-content/uploads/2021/11/WhatsApp-Image-2021-11-25-at-14.59.39.jpeg",
                "Con una emotiva entrega de reconocimientos culminó el X Encuentro de Investigación de la Universidad Católica del Norte (UCN), evento que tradicionalmente se desarrolla todos los años y que es organizado por la Vicerrectoría de Investigación y Desarrollo Tecnológico (VRIDT) de la casa de estudios.",
                "Con entrega de reconocimientos se desarrolló X Encuentro de Investigación UCN",
                ZonedDateTime.now(ZoneId.of("-4"))
        );

        return news;*/
    }
}
