package Innovatech.ms_analiticas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * ms-analiticas ya no accede a ninguna base de datos: agrega via HTTP los datos
 * de ms-gestion_proyectos y ms-recursos_colaboraciones. Se excluye la
 * autoconfiguracion de datasource/JPA para que el servicio arranque sin RDS.
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class MsAnaliticasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAnaliticasApplication.class, args);
	}

}
