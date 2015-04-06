JEPLayer
========
	
Simple persistent Object Relational Mapping (ORM) API on top of JDBC and JTA 

News
------

- 2015-03-13 Tutorial [JEPLayer, Java 8 and jOOQ a Match Made in German Heaven](http://jmarranz.blogspot.com.es/2015/03/jeplayer-java-8-and-jooq-match-made-in.html).
- 2015-03-09 v1.2 Released. First release in JCenter and Maven Central. [Release Notes](https://github.com/jmarranz/jeplayer/blob/master/CHANGES.txt).
- 2012-11-25 Source code now in GitHub
- 2012-10-27 JEPLayer 1.1 is out. [Release Notes](https://github.com/jmarranz/jeplayer/blob/master/CHANGES.txt).
- 2012-03-28 JEPLayer 1.0.1 is out. [Release Notes](https://github.com/jmarranz/jeplayer/blob/master/CHANGES.txt).

Download Binaries and Docs
------

[Download](https://sourceforge.net/projects/jeplayer/files/)

Distribution file includes binaries (jar), manual and javadocs.

Artefacts (as of v1.2) are uploaded to [JCenter](https://bintray.com/jmarranz/maven/jeplayer/view) and [Maven Central](https://oss.sonatype.org/content/repositories/releases/com/innowhere/jeplayer/) repositories

Maven: 

```xml
<groupId>com.innowhere</groupId>
<artifactId>jeplayer</artifactId>
<version>(version)</version>
<type>jar</type>
```

Overview: Why another ORM tool?
------

JEPLayer was born to provide:

1) A simple API to avoid the tedious tasks of JDBC and/or JTA.

2) Several optional listeners to fully customize the lifecycle of JDBC persistence (simple and non-invasive IoC). Most of them Java 8 (lambdas) friendly.

3) Methods to build simple and complex DAOs, automatic attribute-table field binding or fully customized.

4) Does not replace JDBC, instead of this, JDBC objects are exposed when required.

5) Ever using PreparedStatement, ever secure.

6) PreparedStatement objects are automatically cached and reused.

7) Fluid API for queries similar to JPA.

8) Extremely simple, automatic, fully configurable, declarative and programmatic, non-invasive and error-free way to demarcate JDBC and JTA transactions

9) JTA transaction declaration with the same semantics as JavaEE EJB beans

10) Extremely simple and error-free two-phase commit JTA transactions for multiple databases

11) False JTA transactions with pure JDBC infrastructure, change to real JTA with a simple method call.

What is Different In JEPLayer
------

* JEPLayer is simpler than Spring’s JdbcTemplate and transactions and has similar power, the persistent lifecycle can be fully configurable providing more interception points, 
it does not try to replace JDBC and JDBC/JTA transactions are built-in and tightly integrated. JTA transactions for multiple databases are built-in.

* JEPLayer is programmatic instead of the declarative path of iBatis/MyBatis. Transactions can be optionally declared in methods.

* JEPLayer allows getting the most of the database with no performance penalty and no waste of control typical of transparent persistence ORMs (like Hibernate or JPA).



Online Docs Last Version
------

[Manual PDF](http://jeplayer.sourceforge.net/docs/manual/jeplayer_manual.pdf)

[Manual HTML](http://jeplayer.sourceforge.net/docs/manual/jeplayer_manual.htm)

[JavaDocs](http://jeplayer.sourceforge.net/docs/javadoc/)

Examples
------

See the GitHub repository [JEPLayer Examples](https://github.com/jmarranz/jeplayer_examples)

Questions and discussions
------

There is a [Google Group](https://groups.google.com/forum/#!forum/jeplayer) for JEPLayer.

Bug Reporting
------

Use this GitHub project.


Articles/Blogs/Presentations
------

- 2015-03-13 Tutorial [JEPLayer, Java 8 and jOOQ a Match Made in German Heaven](http://jmarranz.blogspot.com.es/2015/03/jeplayer-java-8-and-jooq-match-made-in.html).

- 2012-03-26 Tutorial [Horizontal scaling of RDBMS with JTA and JEPLayer](http://java.dzone.com/articles/horizontal-scaling-rdbms-jta) at JavaLobby. Spanish version
at [javaHispano](http://www.javahispano.org/portada/2012/3/19/escalamiento-horizontal-acid-de-rdbms-con-jeplayer.html)

- 2011-12-18 Tutorial [JEPLayer ORM v1.0, JTA Transactions and Fluid Query API](http://java.dzone.com/announcements/jeplayer-orm-v10-jta) at JavaLobby. Spanish version
at [javaHispano](http://www.javahispano.org/portada/2011/12/16/jeplayer-orm-10-transacciones-jta-y-api-fluida.html)

- 2011-03-22 Tutorial (old version outdated) [Java Easy Persistent Layer - JEPLayer](http://java.dzone.com/articles/java-easy-persistent-layer) 

Related
------

[JEPLDroid](http://code.google.com/p/jepldroid/) is a port of JEPLayer for Android, most of features are supported but JTA.

