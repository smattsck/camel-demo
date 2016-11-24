package fr.cameldemo;

import java.util.TimeZone;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.routepolicy.quartz2.CronScheduledRoutePolicy;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

/** Configuration d'un CamelContext */
@Configuration
public class RouteConfig extends SingleRouteCamelConfiguration {
	/** Logger */
	static final Logger LOG = LoggerFactory.getLogger(RouteConfig.class);

	/* Jobs Name */
	public static final String JOB_HELLO_WORLD = "HELLO_WORLD";

	/* Crons */
	public static final String CRON_DEFAULT_MIN = "0+*+*+*+*+?";
	public static final String CRON_DEFAULT_SEC = "*+*+*+*+*+?";

	/** Configuration des routes
	 *
	 * @see org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration#route() */
	@Bean
	@Override
	public RouteBuilder route() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				/** Route HelloWorld - Log **/
				/* Expression cron de déclenchement */
				from("quartz2://" + JOB_HELLO_WORLD + "?cron=" + CRON_DEFAULT_SEC)
						/* Prise en compte d'une plage horaire pour le démarrage et l'arrêt automatique de la route */
						.routePolicy(plageHoraire())
						/* AutoStartUp : démarrage automatique de la route au lancement de l'appli en fonction de la plage horaire */
						.autoStartup(isCronNextExecutionTimeIn(plageHoraire()))
						/* Définition d'un id pour la route */
						.routeId(JOB_HELLO_WORLD)
						/* Ajout d'une donnée dans l'exchange en cours */
						// .setBody(simple("Simple Hello..."))
						/* Utilisation du composant Log */
						// .to("log:fr.cameldemo.RouteConfig?level=INFO&groupSize=5");
						/* Log */
						.log(LoggingLevel.INFO, "Hello World !");
			}
		};
	}

	/** Définition d'une plage horaire pour le démarrage et l'arrêt des routes
	 *
	 * @return CronScheduledRoutePolicy */
	public static CronScheduledRoutePolicy plageHoraire() {
		/* Démarrage tous les jours à 6 heures */
		CronScheduledRoutePolicy cronGeneral = new CronScheduledRoutePolicy();
		cronGeneral.setRouteStartTime("0 0 6 * * ?");
		/* Arrêt tous les jours à 20 heures */
		cronGeneral.setRouteStopTime("0 0 20 * * ?");
		return cronGeneral;
	}

	/** Permet de vérifier si la prochaine exécution du cron est comprise dans la plage horaire de la policy en paramètre
	 *
	 * @return true si dans la plage horaire */
	public static boolean isCronNextExecutionTimeIn(final CronScheduledRoutePolicy csrp) {
		CronTrigger ctstart = new CronTrigger(csrp.getRouteStartTime(), TimeZone.getDefault());
		CronTrigger ctstop = new CronTrigger(csrp.getRouteStopTime(), TimeZone.getDefault());
		SimpleTriggerContext context = new SimpleTriggerContext();
		return ctstart.nextExecutionTime(context).compareTo(ctstop.nextExecutionTime(context)) > 0;
	}
}
