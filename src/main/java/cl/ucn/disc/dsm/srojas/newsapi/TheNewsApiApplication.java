package cl.ucn.disc.dsm.srojas.newsapi;

import cl.ucn.disc.dsm.srojas.newsapi.model.News;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

/**
 * The News API Application.
 *
 * @author Sebastián Rojas
 */
@SpringBootApplication
public class TheNewsApiApplication {

	/**
	 * The {@link NewsRepository} used to initialize the databases.
	 */
	@Autowired
	private NewsRepository newsRepository;

	/**
	 * The Main starting point.
	 *
	 * @param args to use.
	 */
	public static void main(String[] args) {
		SpringApplication.run(TheNewsApiApplication.class, args);
	}

	/**
	 * Initialize the data inside the Database.
	 * @return the data to use.
	 */
	@Bean
	protected InitializingBean initializingDatabase() {
		return () -> {
			final News news = new News(
					"Titulo",
					"Fuente",
					"Seba Rojas",
					"https://www.noticias.ucn.cl/destacado/con-entrega-de-reconocimientos-se-desarrollo-x-encuentro-de-investigacion-ucn/",
					"https://www.noticias.ucn.cl/wp-content/uploads/2021/11/WhatsApp-Image-2021-11-25-at-14.59.39.jpeg",
					"Con una emotiva entrega de reconocimientos culminó el X Encuentro de Investigación de la Universidad Católica del Norte (UCN), evento que tradicionalmente se desarrolla todos los años y que es organizado por la Vicerrectoría de Investigación y Desarrollo Tecnológico (VRIDT) de la casa de estudios.",
					"Con entrega de reconocimientos se desarrolló X Encuentro de Investigación UCN",
					ZonedDateTime.now(ZoneId.of("-4"))
			);
			// Save the news
			this.newsRepository.save(news);
		};
	}
}
