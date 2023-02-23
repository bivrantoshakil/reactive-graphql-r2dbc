# anyx
This project is a Kotlin-based graphql application that provides simple interfaces to make payments
and also see sales statements on hourly interval. It uses Spring Webflux reactive web framework and
Postgresql for persistence.  

## Prerequisites

Before starting, you must have the following installed:

- [Java Development Kit (JDK) 17](https://www.azul.com/downloads/?package=jdk)
- [Gradle](https://gradle.org/install/)
- [Docker](https://docs.docker.com/engine/install/)

## Running in local environment

1. Clone this repository onto your local machine
2. Open the project in any IDE (IntellijIDEA recommended)
3. Open terminal and execute these commands to start required docker containers
    ```bash
    $ cd dev-tools
    $ docker-compose up
    ```
4. Open another terminal tab and from the classpath, execute these commands to run the application
    ```bash
    $ ./gradlew clean build
    $ ./gradlew bootRun
    ```
5. There is a web based Postgresql client already added in the docker-compose file which can be used from 
[here](http://localhost:5050/browser/). Use this credential to login
   ```python
   Email Address: root@anyx.com
   Password: password  
   ```

## Usage
### GraphQL Queries
There is a web interface already available provided by graphql to execute graphql queries. Open your preferred
browser and open [this url](http://localhost:8080/graphiql?path=/graphql)

#### Make Payment
```python
mutation{
  makePayment(payment: {price: "100.00", priceModifier: 0.95, paymentMethod: CASH, datetime: "2023-02-20T10:07:37Z" }){
    finalPrice
    points
  }
}
```
this should produce a response like this
```json
{
  "data": {
    "makePayment": {
      "finalPrice": "95.00",
      "points": 5
    }
  }
}
```

#### Get hourly sales statement
```python
query{
  getHourlySalesStatement(timeRange:{startDateTime: "2023-02-18T22:07:37Z",endDateTime: "2023-02-24T22:07:37Z"}){
    sales{
      datetime
      sales
      points
    }
  }
}
```
the response should be
```json
{
  "data": {
    "getHourlySalesStatement": {
      "sales": [
        {
          "datetime": "2023-02-20T10:00:00Z",
          "sales": "95.00",
          "points": 5
        }
      ]
    }
  }
}
```

### Using curls
Its also possible to use curl commands to populate the same as the graphql queries above.
#### Make Payment
```bash
curl --location 'http://localhost:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "mutation{\n  makePayment(payment: {price: \"100.00\", priceModifier: 0.95, paymentMethod: CASH, datetime: \"2023-02-20T23:07:37Z\" }){\n    finalPrice\n    points\n  }\n}"
}'
```

#### Get hourly sales statement
```bash
curl --location 'http://localhost:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "query{\n  getHourlySalesStatement(timeRange:{startDateTime: \"2023-02-18T22:07:37Z\",endDateTime: \"2023-02-24T22:07:37Z\"}){\n    sales{\n      datetime\n      sales\n      points\n    }\n  }\n}"
}'
```

## Contributing
1. Fork this repository
2. Create a new branch for your changes
3. Make your changes and commit them with clear, descriptive messages
4. Push your changes to your fork
5. Submit a pull request to this repository

Happy Coding :)