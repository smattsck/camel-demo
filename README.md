# camel-demo 
Par Matthieu Manginot

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
* Web Services : spring-ws, CXF, [Rest](http://camel.apache.org/rest-dsl.html), ...

[Liste complète des composants Camel.](https://camel.apache.org/components.html)

La documentation sur le site web est très complète et il y a une bonne communauté. Il existe également un livre écrit par les développeurs de Camel *"Camel In Action, Second Edition"* by Claus Ibsen and Jonathan Anstey.

### Eléments de base Camel - Route, Processor et Endpoint

* Une route décrit un canal (*channel*) entre deux endpoints ou plus, pouvant faire appel à des processors.
	* Elle véhicule des messages sous forme d'objet Exchange, contenant des headers et un body. C'est le body qui contient les données.
* Une route ne possède qu'un seul point d'entrée (**from**) et une ou plusieurs sorties (**to**). Elles peuvent s'enchainer, s'imbriquer, etc.
* Les processors permettent de réaliser un ou plusieurs traitements.
* Les endpoints sont généralement décrits par une URI et correspondent à des ressources (JPA, Fichiers, ...).

La déclaration des routes peut se faire en utilisant différent DSL (Domain Specific Language), tel que JAVA ou Spring XML. [Il est possible d'en utiliser d'autres.](http://camel.apache.org/dsl.html)

Nous utiliserons JAVA dans cette présentation.

Toutes les déclarations doivent se faire au sein d'un CamelContext.

## Initialiation du projet - pom.xml

SpringBoot est utilisé comme base d'application.

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

Côté Java, les routes Camel sont définies dans la classe RouteConfig :

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

Le [composant Log](http://camel.apache.org/log.html]) offre plus de possibilités que la fonction log de base.

a. Commenter la ligne `.log(LoggingLevel.INFO, "Hello World !");` 

* Puis décommenter les 2 lignes : `//.setBody(simple("Simple Hello..."))//.to("log:fr.cameldemo.RouteConfig?level=INFO&groupSize=5");`
	* On remarquera ici l'utilisation de l'expression *simple*, permettant d'intéragir avec l'*exchange* Camel directement au niveau de la route
		* Ici, le *body* de l'*exchange* est rempli avec une chaine de caractères
		* [Pour plus d'infos sur la syntaxe Simple](http://camel.apache.org/simple.html)
* Tester le fonctionnement, l'application groupe alors les logs par paquet de 5 avec l'option *groupSize*.

b. Remplacer le groupement par paquet par un groupement avec intervalle de temps, toutes les 10s par exemple.

### Utilisation d'un processor

Un *processor* est une classe Java implémentant l'interface *Processor* de Camel permettant ainsi d'être utilisé dans les routes via la méthode `.bean(Processor.class)`.

c. Utiliser un *processor* Java à la place du composant *log* pour logger l'*exchange* Camel.

Vous pouvez vous servir du processor `fr/cameldemo/processors/Ex1Processor.java`.

##Exercice 2 : Fichier

### Déplacement d'un fichier

Le [composant File](http://camel.apache.org/file2.html) permet d'intéragir avec les fichiers : création, deplacement,...

Exemple :

```java
from("file:in")
	.to("file:out");
```

Cette route déplace tous les fichiers du répertoire *in* vers le répertoire *out*.

a. Créer une route permettant de déplacer le fichier `ex2-1.txt` sous `/src/files/ex2/1/` vers le répertoire d'historique `/src/history`

> Utile : Pour les tests, l'option `?noop=true` ne déplacera et ne supprimera pas les fichiers

b. Ajouter une instruction pour renommer le fichier via le *header* `CamelFileName`. A l'aide du [File Expression Language](http://camel.apache.org/file-language.html) ajouter la date et l'heure de passage dans le nom du fichier sous cette forme `nom-yyyyMMddHHmmss.extension`

### Utilisation du composant Direct

Le [composant Direct](https://camel.apache.org/direct.html) permet diviser les routes au sein d'un même CamelContext pour ainsi moduler et réutiliser des traitements.

Exemple :

```java
from("file:in")
	.to("direct:processFile");

from("direct:processFile")
	.to("file:out");
```

b. Diviser la route en deux (Récupération/Renommage+Ecriture)

c. Ajouter une nouvelle route pour déplacer le fichier `ex2-2.txt` sous `/src/files/ex2/2/` vers le répertoire d'historique

d. Modifier l'endpoint d'écriture dans le répertoire d'historique pour générer dynamiquement une arborescence sous cette forme : année/mois/jours/heure : `yyyy/yyyy-MM/yyyy-MM-dd/yyyy-MM-dd_HH/`

##Exercice 3 : CSV

Le [composant Bindy](http://camel.apache.org/bindy.html) permet de convertir des fichiers CSV en objet Java, via des annotations pour définir le séparateur utilisé ou encore les positions des données dans le fichier.

Exemple :

```java
@CsvRecord(separator = ";", skipFirstLine = true)
public Class Order {
	...
	@DataField(pos = 1)
	private int orderNr;
	...
}
```

Les méthodes à utiliser dans une route :
* Pour sérialiser : `.marshal(new BindyCsvDataFormat(Order.class))`
* Pour désérialiser : `.unmarshal(new BindyCsvDataFormat(Order.class))`

Ajouter dans le pom.xml la dépendance suivante :

```java
<dependency>
	<groupId>org.apache.camel</groupId>
	<artifactId>camel-bindy</artifactId>
	<version>${camel.version}</version>
</dependency>
```

a. Annoter l'objet `fr/cameldemo/entities/Ex3.java` puis créer une route pour désérialiser le fichier `ex3.csv` sous `/src/files/ex3/` et logger chaque ligne dans un processor

> Utile : Le résultat de la conversion étant une liste, il est possible de la *splitter* pour réaliser un *forEach* directement dans la route : `.split(body())`

b. Dans le processor modifier les données puis re-générer le fichier csv dans le répertoire d'historique `/src/history`

> Utile : Il est possible d'utiliser CamelBindy en java uniquement :

```java
public static <T> List<T> convertCsvToFile(final InputStream is, final Class<T> cl) throws Exception {
	BindyCsvDataFormat bindy = new BindyCsvDataFormat(cl);
	if (is instanceof InputStream) {
		CamelContext ctx = new DefaultCamelContext();
		Exchange ex = new DefaultExchange(ctx);
		Object o = bindy.unmarshal(ex, is);
		if (o instanceof List<?>) {
			return (List<T>) o;
		}
		if (o.getClass() == cl) {
			List<T> l = new ArrayList<>();
			l.add((T) o);
			return l;
		}
	}
	return null;
}
```

##Tuto 4 : ErrorHandler

Pour gérer les exceptions qui surviennent et pour éviter les relances à l'infini, il est important de définir un [ErrorHandler](http://camel.apache.org/error-handler.html).

Celui-ci permet notamment :

* de re-router les exchanges en exceptions, 
* d'afficher le message d'erreur 
* et de définir un nombre d'essai maximum.

```java
errorHandler(deadLetterChannel("seda:errors").log(RouteConfig.class).retryAttemptedLogLevel(LoggingLevel.INFO).maximumRedeliveries(3));
```

Ci-dessus, l'*errorHandler* défini est un *DeadLetterChannel* permmettant de définir un nombre d'essai avant de transférer l'*exchange* vers un autre *endpoint*. Ici 3 tentatives `.maximumRedeliveries(3)` puis envoi vers `seda:errors`.

Il est ici défini globalement pour toutes les routes du CamelContext. Au besoin, un *errorHandler* peut être défini au niveau d'une route.

> Utile : On remarquera l'utilisation du [composant Seda](https://camel.apache.org/seda.html) qui est l'équivalent du [composant Direct](https://camel.apache.org/direct.html) mais avec un fonctionnement asynchrone.

Il est également possible de définir des règles par [type d'exception](http://camel.apache.org/exception-clause.html) avec la méthode `onException(IOException.class)....to(...`. Idem globalement ou par route.

##Tuto 5 : FTP

Le [composant FTP](http://camel.apache.org/ftp2.html) est trés utile et simple d'utilisation.

Il est nécessaire d'ajouter la dépendance suivante :

```java
<dependency>
	<groupId>org.apache.camel</groupId>
	<artifactId>camel-ftp</artifactId>
	<version>${camel.version}</version>
</dependency>
```

Voici un exemple de route :
```java
from("ftp://server.fr/fichiersARecuperer/?username=login&password=secret&consumer.delay=60000")
.to("file:repertoireApplication");
```

Toutes les 60 secondes cette route récupérera les fichiers présents dans le répertoire en paramètre.

> Utile : Il est possible de filtrer les fichiers à récupérer avec l'option `?filter=` pointant vers une classe implèmentant l'interface `GenericFileFilter` -> [Plus d'infos](http://camel.apache.org/ftp2.html#FTP2-Filterusingorg.apache.camel.component.file.GenericFileFilter)

##ActiveMQ

Camel dispose également un [composant ActiveMQ](http://camel.apache.org/activemq.html) permettant de communiquer avec un serveur ActiveMQ des messages sur des *Queue* et *Topic* JMS.

ActiveMQ sera utile pour des applications :
* Qui ne doivent perdre aucun message, aussi bien pendant le fonctionnement qu'à l'arrêt
* Avec un volume de messages important
	* Il est possible d'optimiser la récupération des messages de plusieurs façons, notamment en parallèlisant les traitements

> Utile : Il est possible d'embarquer un serveur ActiveMQ dans l'application -> [Plus d'info](http://activemq.apache.org/how-do-i-embed-a-broker-inside-a-connection.html)

##Pour conclure

Cette présentation pose les bases de l'utilisation de Camel et donne un aperçu des principaux composants, offrants un gain de temps et une facilité de mise en place.

Cela ne représente qu'une mince partie du potentiel de Camel, qui est plus particulièrement utile dans les échanges entre applications ([JMS](http://camel.apache.org/jms.html), [ActiveMQ](http://camel.apache.org/activemq.html), [VM](http://camel.apache.org/vm.html), [Docker](http://camel.apache.org/docker.html),...).

De manière générale :

* Préférer l'utilisation du DSL Java pour décrire les *routes* et l'utilisation de *processors* pour les traitements
* Eviter au maximum l'utilisation des expressions *Simple* ou autre appel via chaine de caractères
	* Cela peut vite compléxifier le code et sa maintenabilité
* Dans le cas une application contenant beaucoup de routes et sous-routes :
	* Eviter l'ajout de données dans les *headers*
	* Préférer l'utilisation d'un objet unique contenant toutes vos variables
* Son utilisation dépendra du projet
	* Pour réaliser des taches simples, Camel n'est pas forcèment nécessaire
	* Exemples :
		* Alternative à la gestion des fichiers : [Java 8 Files](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html)
		* Alternative au Cron : [Spring Cron](https://spring.io/guides/gs/scheduling-tasks/)