package games.features.news;

/**
 * Created by tuanhoang on 9/13/17.
 */
public class NewsService {
    private static NewsService instance;
    public static NewsService getInstance() {
        if (instance == null) instance = new NewsService();
        return instance;
    }
    NewsService() {

    }
}
