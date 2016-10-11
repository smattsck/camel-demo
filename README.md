# camel-demo

## Apache Camel
[Apache Camel](http://camel.apache.org/) est un framework Java d'intégration qui peut être apparenté à un ESB (Enterprise Service Bus) léger,
il fourni des implémentations des EIP (Enterprise Integration Patterns), qui basés sur la notion de couplage faible,
permettent de réaliser des tâches telles que : la transformation, l'enrichissement ou l'aggrégation de données 
ou en encore le routage de messages/données entre applications.

[Liste de l'ensemble des EIP](http://www.enterpriseintegrationpatterns.com/patterns/messaging/toc.html) en référence au livre "Enterprise Integration Patterns : Designing, Building, and Deploying Messaging Solutions" by Gregor Hohpe, Bobby Woolf.

![Possibilités des EIP](/docs/eip-patterns.png)

*Possibilités des EIP - Exemples de flux*

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

### Eléments de base Camel - Route, Processor et Endpoint

* Une route décrit un canal (channel) entre deux endpoints ou plus pouvant faire appel à des processors.
	* Elle véhicule des messages sous forme d'objet Exchange, contenant des headers et un body. C'est le body qui contient les données.
* Une route ne possède qu'un seul point d'entrée (from) et une ou plusieurs sorties (to). Elles peuvent s'enchainer, s'imbriquer, etc.
* Les processors permettent de réaliser un ou plusieurs traitements.
* Les endpoints sont généralement décrits par une URI et correspondent à des ressources (JPA, Fichiers, ...).

La déclaration des routes peut se faire en utilisant différent DSL (Domain Specific Language), tel que JAVA ou Spring XML. [Il est possible d'en utiliser d'autres.](http://camel.apache.org/dsl.html)
Nous utiliserons JAVA pour nos déclarations.

Toutes les déclarations doivent se faire au sein d'un CamelContext.

## Initialiation du projet

Nous utiliserons SpringBoot comme base à notre application.

Dans le pom.xml :
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
