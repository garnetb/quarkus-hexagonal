# 👋 About

Maven Multi-module project to illustrate how to structure a project based on an hexagonal architecture.

## ⚙️ Technologies

### Common

| Technology | Purpose |
| ---------- |----------|
|Hexagonal architecture| I tried to follow an hexagonal clean architecture when creating this simple example. You can take a look into the project modules to see how the code was divided into application, domain and infrastructure layers. You can also take a look into the interfaces I'm using as "ports" to avoid layer couplings. I think that it could be interesting to understand the difference between the `Loan` entity at domain level (I will try to complete this example with some DDD concepts), and the `LoanEntity` at the infrastructure layer. |
| Java 21 | The project uses Java 21 as specified in the main pom.xml |
| [Lombok](https://projectlombok.org/) | Helper to create builders, setters, getters, etc|
| [MapStruct](https://mapstruct.org/) | Helper to create mappers to pass objects between the different layers |
| [Quarkus](https://quarkus.io/) | Quarkus 3.6.4 is a full-stack, Kubernetes-native Java framework made for Java virtual machines (JVMs) and native compilation, optimizing Java specifically for containers and enabling it to become an effective platform for serverless, cloud, and Kubernetes environments. |
| [JUnit 5](https://quarkus.io/guides/getting-started-testing) | Unit tests creation |
| [Mockito](https://quarkus.io/blog/mocking/) | Adds mockito framework for testing purposes |

### PostgreSQL adapter

| Technology | Purpose |
| ---------- |----------|
| [Hibernate panache](https://quarkus.io/guides/hibernate-orm-panache) | Database access |
| [Flyway](https://flywaydb.org/) | To load database at start |

### DynamoDB adapter

| Technology | Purpose |
| ---------- |----------|
| [Dynamodb enhanced](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-dynamodb.html) | Dynamodb enhanced library based on [AWS SDK for Java 2.0](https://github.com/aws/aws-sdk-java-v2), compatible with native image and quarkus. |

### Azure Cosmos DB adapter

| Technology | Purpose |
| ---------- |----------|
| [Azure Cosmos DB SDK](https://learn.microsoft.com/en-us/azure/cosmos-db/nosql/sdk-java-v4) | Azure Cosmos DB Java SDK for NoSQL API, providing a client-side logical representation for the Azure Cosmos DB service. |

### REST adapter

| Technology | Purpose |
| ---------- |----------|
| [Resteasy](https://quarkus.io/guides/rest-json) | Annotation based Rest controllers with JSON serialization / desearialization |
| [rest-assured](https://rest-assured.io/) | Testing and validating REST services |
| [smallrye-openapi](https://github.com/smallrye/smallrye-open-api) | Add OpenAPI spec to the project and helps with the generation of both schema and Swagger UI |
| [OpenAPI](https://github.com/OAI/OpenAPI-Specification) | The OpenAPI Specification, previously known as the Swagger Specification, is a specification for a machine-readable interface definition language for describing, producing, consuming and visualizing RESTful web services. |
| [openapi-generator-maven-plugin](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-plugin) | A Maven plugin to support the [OpenAPI generator project](https://github.com/OpenAPITools/openapi-generator). |

### Kafka adapter

| Technology | Purpose |
| ---------- |----------|
| [SmallRye Reactive Messaging](https://quarkus.io/guides/kafka) | Quarkus provides support for Apache Kafka through SmallRye Reactive Messaging framework |
| [Apache Avro](https://avro.apache.org/) | Data serialization system for efficient binary encoding |

### Azure Event Hubs adapter

| Technology | Purpose |
| ---------- |----------|
| [Azure Event Hubs SDK](https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-java-get-started-send) | Azure Event Hubs client library for Java, providing a client-side logical representation for the Azure Event Hubs service |
| [Azure Blob Storage Checkpoint Store](https://learn.microsoft.com/en-us/azure/event-hubs/event-processor-balance-partition-load) | Azure Blob Storage checkpoint store for Event Processor Client to track partition ownership and checkpoint events |
| [Apache Avro](https://avro.apache.org/) | Data serialization system for efficient binary encoding |

### HashiCorp Vault Integration

| Technology | Purpose |
| ---------- |----------|
| [Quarkus Vault](https://quarkiverse.github.io/quarkiverse-docs/quarkus-vault/dev/) | Integration with HashiCorp Vault for secure secrets management |

## 🚀 How to execute the application

### 🛠️ DEV mode

Compile, test, package and install the different artifacts in your local maven repository.

```shell
mvn clean install
```

After creating all artifacts you can run the project in dev mode (hotreload enabled + dev services).

```shell
mvn quarkus:dev -pl bootloader
```

`Bootloader` module is the providing the quarkus startup point. If you want to switch the database adapter, you can modify the `bootloader/pom.xml` file:

- To use PostgreSQL: uncomment the `adapter-postgresql` dependency and comment out `adapter-dynamodb` and `adapter-cosmosdb`
- To use DynamoDB: uncomment the `adapter-dynamodb` dependency and comment out `adapter-postgresql` and `adapter-cosmosdb`
- To use Cosmos DB: uncomment the `adapter-cosmosdb` dependency and comment out `adapter-postgresql` and `adapter-dynamodb`

This project is using [dev services](https://quarkus.io/guides/dev-services) to provide the external services like DB, Event broker, Identity Manager, etc.

> In order to use Dev Services you will generally need a working Docker environment (remote environments are supported). If you don’t have Docker installed you will need to configure your services normally.

### 📦 PROD mode

You will need to package the `bootloader` module:

```shell
mvn clean package -pl bootloader
```

It produces several outputs in ./bootloader/target:

* getting-started-1.0.0-SNAPSHOT.jar - containing just the classes and resources of the projects, it’s the regular artifact produced by the Maven build - it is not the runnable jar;

* the quarkus-app directory which contains the quarkus-run.jar jar file - being an executable jar. Be aware that it’s not an `über-jar` as the dependencies are copied into subdirectories of quarkus-app/lib/.

You can run the application using: java -jar ./bootloader/target/quarkus-app/quarkus-run.jar

> If you want to deploy your application somewhere (typically in a container), you need to deploy the whole quarkus-app directory.
Before running the application, don’t forget to stop the hot reload mode (hit CTRL+C), or you will have a port conflict.

For more information about running Quarkus applications (dev, test, prod, native) please take a look at [Quarkus - getting started # running-the-application](https://quarkus.io/guides/getting-started#running-the-application).
