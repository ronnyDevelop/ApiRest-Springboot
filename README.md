# Evaluación de JAVA - API RESTful de Creación de Usuarios

Este proyecto consiste en la creación de una aplicación en Java que expone una API RESTful para la gestión de usuarios. Incluye la implementación de los métodos HTTP: GET, POST, PUT, PATCH y DELETE

## Características Principales

* **Registro de Usuarios:** Permite registrar nuevos usuarios con información personal y números de teléfono.
* **Validación de Datos:** Valida el formato del correo electrónico y la contraseña mediante expresiones regulares configurables.
* **Manejo de Errores:** Retorna mensajes de error en formato JSON para cualquier problema durante el registro.
* **Generación de Tokens:** Genera tokens de acceso (JWT) para cada usuario registrado.
* **Persistencia de Datos:** Almacena la información de los usuarios y sus tokens en una base de datos en memoria.
* **Documentación:** La API está documentada con Swagger para facilitar su uso.

## Requisitos

* Java 17
* Gradle
* Spring Boot
* H2 Database
* Hibernate (JPA)
* JWT (JSON Web Tokens)
* Swagger

## Configuración

1.  **Clonar el repositorio:**

    ```bash
    git clone <URL_DEL_REPOSITORIO>
    ```

2.  **Construir el proyecto:**

    ```bash
     ./gradlew clean build

    ```

3.  **Ejecutar la aplicación:**

    ```bash
    ./gradlew bootRun    

    ```

## Formato esperado para la creación de usuarios

* Cada solicitud de creación de usuario debe incluir los siguientes campos:

## Formato de Solicitud de Registro

```json
{
  "name": "Juan Rodriguez",
  "email": "juan@rodriguez.org",
  "password": "hunter2",
  "phones": [
    {
      "number": "1234567",
      "citycode": "1",
      "contrycode": "57"
    }
  ]
}

