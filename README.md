# org.kie.kogito.kogito-quarkus-archetype - 0.1.3 #

# Running

- Compile and Run

    ```
     mvn clean package quarkus:dev    
    ```

- Native Image (requires JAVA_HOME to point to a valid GraalVM)

    ```
    mvn clean package -Pnative
    ```
  
  native executable (and runnable jar) generated in `target/`

# Test your application

The application exposes two RESTful endpoint, a synchronous endpoint and an asynchronous/reactive endpoint. Theycan be tested with the following commands:

Asynchronous:

```sh
curl -X GET 'http://localhost:8080/drinks?name=Duncan&age=40&ingredients=whiskey'
```

Synchronous:

```sh
curl -X GET 'http://localhost:8080/drinksSync?name=Duncan&age=40&ingredients=whiskey'
```


Once successfully invoked you should see:

```
Duncan CAN DRINK!
---------------------------
Selected drinks:
Whiskey,Whiskey and Coke
```

# Developing

Add your business assets resources (process definition, rules, decisions) into src/main/resources.

Add your java classes (data model, utilities, services) into src/main/java.

Then just build the project and run.


# Swagger documentation

Point to [swagger docs](http://localhost:8080/docs/swagger.json) to retrieve swagger definition of the exposed service

You can visualize that JSON file at [swagger editor](https://editor.swagger.io)

In addition client application can be easily generated from the swagger definition to interact with this service.
