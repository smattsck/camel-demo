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

@Configuration
public class RouteConfig extends SingleRouteCamelConfiguration {
	/** Logger */
	static final Logger LOG = LoggerFactory.getLogger(RouteConfig.class);

	/* Jobs Name */
	public static final String JOB_TEST_LOG = "TEST_LOGS";

	public static final String CRON_DEFAULT_SEC = "0+*+*+*+*+?";
	public static final String CRON_DEFAULT_MIN = "*+*+*+*+*+?";

	@Bean
	@Override
	public RouteBuilder route() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				/** Camel log **/
				/* Expression cron de déclenchement */
				from("quartz2://" + JOB_TEST_LOG + "?cron=" + CRON_DEFAULT_SEC)
						/* Prise en compte de la plage horaire */
						.routePolicy(plageHoraire())
						/* AutoStartUp et Id de la route */
						.autoStartup(isAutoStartedRoute(plageHoraire())).routeId(JOB_TEST_LOG)
						// .setBody(simple("Logging..."))
						// .to("log:fr.cameldemo.RouteConfig?level=INFO&groupingSize=5");
						.log(LoggingLevel.INFO, "Logging...");

				/* Process */
				// TODO
			}
		};
	}

	/** Définition d'une plage horaire générale pour le démarrage et l'arrêt des chargements jobs
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

	public static boolean isAutoStartedRoute(final CronScheduledRoutePolicy csrp) {
		CronTrigger ctstart = new CronTrigger(csrp.getRouteStartTime(), TimeZone.getDefault());
		CronTrigger ctstop = new CronTrigger(csrp.getRouteStopTime(), TimeZone.getDefault());
		SimpleTriggerContext context = new SimpleTriggerContext();
		boolean isAutoStartedRoute = ctstart.nextExecutionTime(context).compareTo(ctstop.nextExecutionTime(context)) > 0;
		return isAutoStartedRoute;
	}
}
