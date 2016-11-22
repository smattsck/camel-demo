# camel-demo

## Apache Camel
[Apache Camel](http://camel.apache.org/) est un framework Java d'intégration qui peut être apparenté à un ESB (Enterprise Service Bus) léger,
il fourni des implémentations des EIP (Enterprise Integration Patterns), qui basés sur la notion de couplage faible,
permettent de réaliser des tâches telles que : la transformation, l'enrichissement ou l'aggrégation de données 
ou en encore le routage de messages/données entre applications.

[Liste de l'ensemble des EIP](http://www.enterpriseintegrationpatterns.com/patterns/messaging/toc.html) en référence au livre *"Enterprise Integration Patterns : Designing, Building, and Deploying Messaging Solutions"* by Gregor Hohpe, Bobby Woolf.

*Possibilités des EIP - Exemples de flux :*

![Possibilités des EIP](/docs/eip-patterns.png)

[Liste des EIP implémentés par Camel.](http://camel.apache.org/enterprise-integration-patterns.html)

Apache Camel fourni de nombreux composants pour la réalisation de ces EIP, exemples :

* Connecteurs :
	* Traitement de fichiers (récupération, écriture, ...) : camel-file
	* Appel direct vers/depuis la base de données : camel-jpa
	* Appel de classes JAVA : bean (inclus au camel-core)
* Conversion de données :
	* XML : JAXB, ...
	* Objets : JSON, ...
	* Fichiers plats : CSV, ...
* Support de protocoles : camel-http, -ftp (sftp), -scp, ...
* Routage de messages : activemq-camel, camel-jms, ...
* Web Services : spring-ws, CXF, Rest, ...

[Liste complète des composants Camel.](https://camel.apache.org/components.html)

Pour information, la documentation sur le site web est très complète et il y a une bonne communauté. Il existe également un livre écrit par les développeurs de Camel *"Camel In Action, Second Edition"* by Claus Ibsen and Jonathan Anstey.

### Eléments de base Camel - Route, Processor et Endpoint

* Une route décrit un canal (*channel*) entre deux endpoints ou plus, pouvant faire appel à des processors.
	* Elle véhicule des messages sous forme d'objet Exchange, contenant des headers et un body. C'est le body qui contient les données.
* Une route ne possède qu'un seul point d'entrée (**from**) et une ou plusieurs sorties (**to**). Elles peuvent s'enchainer, s'imbriquer, etc.
* Les processors permettent de réaliser un ou plusieurs traitements.
* Les endpoints sont généralement décrits par une URI et correspondent à des ressources (JPA, Fichiers, ...).

La déclaration des routes peut se faire en utilisant différent DSL (Domain Specific Language), tel que JAVA ou Spring XML. [Il est possible d'en utiliser d'autres.](http://camel.apache.org/dsl.html)

Nous utiliserons JAVA pour nos déclarations.

Toutes les déclarations doivent se faire au sein d'un CamelContext.

## Initialiation du projet - pom.xml

Nous utiliserons SpringBoot comme base d'application.

Dépendances SpringBoot :
```xml
<parent>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-parent</artifactId>
	<version>1.4.1.RELEASE</version>
</parent>
<dependencies>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-web</artifactId>
	</dependency>
</dependencies>
```

Pour démarrer l'application, il faudra exécuter la commande maven `mvn spring-boot:run`.

Pour l'utilisation de Camel, il faut ajouter la librairie principale :

```xml
<!-- Camel -->
<dependency>
	<groupId>org.apache.camel</groupId>
	<artifactId>camel-core</artifactId>
	<version>${camel.version}</version>
</dependency>
```

Ainsi que la dépendance suivante pour la déclaration en Java des routes :

```xml
<dependency>
	<groupId>org.apache.camel</groupId>
	<artifactId>camel-spring-javaconfig</artifactId>
	<version>${camel.version}</version>
</dependency>
```

Et pour l'utilisation d'expressions Cron, on ajoute camel-quartz :
```xml
<dependency>
	<groupId>org.apache.camel</groupId>
	<artifactId>camel-quartz2</artifactId>
	<version>${camel.version}</version>
</dependency>
```

Côté Java, les routes Camel seront définies dans la classe RouteConfig :

```java
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
						.autoStartup(isAutoStartedRoute(plageHoraire()))
						/* Définition d'un id pour la route */
						.routeId(JOB_HELLO_WORLD)
						// .setBody(simple("Simple Hello..."))
						// .to("log:fr.cameldemo.RouteConfig?level=INFO&groupSize=5");
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

	/** Permet de vérifier si on est dans la plage horaire de la policy en paramètre
	 *
	 * @return true si dans la plage horaire */
	public static boolean isAutoStartedRoute(final CronScheduledRoutePolicy csrp) {
		CronTrigger ctstart = new CronTrigger(csrp.getRouteStartTime(), TimeZone.getDefault());
		CronTrigger ctstop = new CronTrigger(csrp.getRouteStopTime(), TimeZone.getDefault());
		SimpleTriggerContext context = new SimpleTriggerContext();
		boolean isAutoStartedRoute = ctstart.nextExecutionTime(context).compareTo(ctstop.nextExecutionTime(context)) > 0;
		return isAutoStartedRoute;
	}
}
```

Cette classe servira de base aux exercices.

##Exercice 1 : Hello World

### Cloner le repository

```ssh
git clone git@github.com:smattsck/camel-demo.git
```

### Démarrer l'application

```ssh
mvn spring-boot:run
```

La route HELLO_WORLD doit alors créer un log INFO toutes les secondes.

### Utilisation du composant LOG

Détail du composant : [http://camel.apache.org/log.html](http://camel.apache.org/log.html)

a. Commenter la ligne `.log(LoggingLevel.INFO, "Hello World !");` 

* Puis décommenter les 2 lignes : `//.setBody(simple("Simple Hello...")).to("log:fr.cameldemo.RouteConfig?level=INFO&groupSize=5");`
	* On remarquera ici l'utilisation de l'expression *simple*, permettant d'intéragir avec l'exchange Camel directement au niveau de la route
		* [Pour plus d'infos sur la syntaxe Simple](http://camel.apache.org/simple.html)
* Et tester le fonctionnement, l'application groupe alors les logs par paquet de 5 avec l'option *groupSize*.

b. Remplacer le groupement par paquet par un groupement avec intervalle de temps, toutes les 10s par exemple.

### Utilisation d'un processor

c. Utiliser un processor Java à la place du composant log pour logger l'Exchange Camel.

Un processor est une classe Java implémentant l'interface Processor de Camel permettant ainsi d'être utilisé dans les routes via l'instruction `.bean(Processor.class)`.

Vous pouvez vous servir du processor `fr/cameldemo/processors/Ex1Processor.java`.

> De manière générale :

> Préférer l'utilisation du DSL Java pour décrire les routes et l'utilisation de processors pour les traitements

> Eviter au maximum l'utilisation des expressions Simple ou appel via chaine de caractères

> Cela peut vite compléxifier la lecture des routes et la maintenabilité du code

##Exercice 2 : FTP

### Déplacement d'un fichier

a. A l'aide du [composant File](http://camel.apache.org/file2.html), créer une route permettant de déplacer le fichier `ex2-1.txt` sous `/src/files/ex2/1/` vers le répertoire d'historique `/src/history`

> Astuce : Utile pour les tests, l'option `noop=true` ne déplacera et ne supprimera pas les fichiers

b. Ajouter une instruction permettant de renommer le fichier via le header `CamelFileName`. A l'aide du [File Expression Language](http://camel.apache.org/file-language.html) ajouter la date et l'heure de passage dans nom du fichier sous cette forme `nom-yyyyMMddHHmmss.extension`

### Utilisation du composant Direct

b. Il peut être parfois utile de diviser la route pour moduler les traitements, à l'aide du [composant Direct](https://camel.apache.org/direct.html) diviser la route en deux (Récupération/Renommage+Ecriture)

c. Ajouter une nouvelle route pour déplacer le fichier `ex2-2.txt` sous `/src/files/ex2/2/` vers le répertoire d'historique `/src/history`

d. Modifier l'Endpoint de destination pour générer dynamiquement une arborescence d'historique sous cette forme : année/mois/jours/heure : `yyyy/yyyy-MM/yyyy-MM-dd/yyyy-MM-dd_HH/`

##Exercice 3 : CSV

##Exercice 4 : Rest

##Exercice 5 : Exception Handler
