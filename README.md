# Backend Java Autenticado con JWT OAuth2 con Keycloak

Backend sencillo con autenticación por OAuth2 con JWT.

En el ejemplo se incluye la configuración de seguridad, mapeo de jwt del realm de KeyCloak, integración con Swagger y la autenticación.

## ¿Qué necesito?

- docker
- jdk21
- maven 3.9 o superior
- postman o curl

## ¿Cómo arranco?

El repositorio incluye un backend y un docker-compose.yaml con Keycloak y un postgresql para persistir la configuración de keycloak.

### Levantar el docker compose

Desde consola:

```
docker compose up -d
```

Verificar que esté levantado en http://localhost:8080/. Ingresar con usuario admin / admin1234. Verificar que se haya definido el realm FluxIT.

### Levantar el backend

Podemos hacerlo desde la consola en el mismo directorio del proyecto con:

```
mvn springboot:run
```

Le damos unos segundos y verificamos ingresando a http://localhost:7000/swagger-ui/index.html

### Autenticación

El ejemplo viene con dos usuarios que solo pueden acceder mediante post a la URL: http://localhost:8080/realms/FluxITRealm/protocol/openid-connect/token

```bash
# usuarion estándar
curl -d 'client_id=fluxit-client' \
  -d 'username=reneperez' \
  -d 'password=123456' \
  -d 'grant_type=password' \
  'http://localhost:8080/realms/FluxITRealm/protocol/openid-connect/token'

# usuario admin
curl -d 'client_id=fluxit-client' \
  -d 'username=andygomez' \
  -d 'password=123456' \
  -d 'grant_type=password' \
  'http://localhost:8080/realms/FluxITRealm/protocol/openid-connect/token'
```

Esto debe devolver un json con el jwt en el campo access_token. 

Si tenés jq agrega al final "| jq '.access_token'".

Si te da curiosidad que viene en el Token lo podés revisar en: https://jwt.io. También te sirve para adaptar a otros payloads de token.

### Para probarlo desde consola

Guardate el token del paso anterior y usando curl:

```bash
curl http://localhost:7000/api/public
#Devuelve: This is a public endpoint

curl http://localhost:7000/api/authenticated -I
#Devuelve: HTTP/1.1 401 

curl http://localhost:7000/api/authenticated -I -H 'Authorization: Bearer ACA_VA_EL_TOKEN_SUPERLARRRRGO'
#Devuelve: HTTP/1.1 200 

curl http://localhost:7000/api/admin -I -H 'Authorization: Bearer ACA_VA_EL_TOKEN_SUPERLARRRRGO'
#Con Token con role admin devuelve: HTTP/1.1 200
#Con Token sin role admin devuelve: HTTP/1.1 403 
```

### Probar con Postman

Se incluye una colección para Postman en el archivo auth.json para directorio postman.

Esta tiene una carpeta para autenticar con cada uno de los perfiles y otra carpeta con ejemplos autenticados para cada api.

## ¿Cómo me lo llevo a mi proyecto?

Para arrancar necesitamos un proyecto spring boot con el web starter y hay que agregar al pom las dependencias:

```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
		</dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
        </dependency>
    </dependencies>
```

En application.properties incluimos:

```properties
# Security Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=${TOKEN_ISSUER_URL:http://localhost:8080/realms/FluxITRealm}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/certs

# JWT Configuration
jwt.auth.converter.resource-id=fluxit-client
jwt.auth.converter.principal-attribute=principal_username
```

En la clase SecurityConfig se define lo básico de Spring Security y habilita el acceso al público de las URLs de swagger y el actuator si fuera necesario. Se habilita también la autenticación por método con la anotación @EnableMethodSecurity.

Esto permite agregarle a los controladores la anotación 

```java
    @PreAuthorize("hasRole('admin')")
    public String adminEndpoint() {
        // mejor que sólo entre admin...
    }
```

En JwtConverter se mapea los roles del Realm de KeyCloak a Spring Security. Esto es interesante porque nos permite utilizar diferentes formatos de JWT.

En el caso de esta aplicación tenemos un filtro de roles permitidos. Puede pasar que en el payload de JWT vengan más roles pero que no son de interés para la aplicación. Para eso se agrega el parámetro authentication.validRoles al application.properties. Los valores por defecto son user y admin.

## Siempre me da 401... ¿Por qué no me lleva a la página de login?

No es responsabilidad de la API hacerlo, en todo caso si tenemos un API gateway es este el que debe redirigirnos.

Para hacer login podes usar el ejemplo de curl o el postman provisto.

## Antes de implementarlo verificar:

- Los tokens que llegan por JWT
- Qué rutas queremos dejar públicas.
- Qué granularidad queremos manejar de autenticación: ¿path o método?
