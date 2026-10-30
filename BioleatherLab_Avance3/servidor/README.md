# Bioleather servidor

## Requisitos

- JDK 25 o posterior para compilar y ejecutar la aplicación.
- Maven 3.9 o posterior.

El proyecto configura `java.version` en `pom.xml` para compilar con Java 25. La aplicación requiere un runtime Java 25 o posterior.

## Compilación y pruebas

Desde este directorio, ejecute:

```shell
mvn clean test
```

Para compilar el código principal y las clases de prueba sin ejecutar las pruebas:

```shell
mvn clean test-compile
```
