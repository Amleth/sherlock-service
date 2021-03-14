# SHERLOCK

## Caractéristiques techniques

### Composants

Framework : [Micronaut](https://micronaut.io/)
<br/>
Langage : Java
<br/>
Sécurité : [Micronaut Security](https://micronaut-projects.github.io/micronaut-security/latest/guide/)
<br/>
Persistance : [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/)
<br/>
Tests : [Spock](http://spockframework.org/) + [Micronaut Test](https://micronaut-projects.github.io/micronaut-test/latest/guide/index.html#spock)
<br/>
Moteur de production : [Gradle](https://gradle.org/)
<br/>
Documentation de l'API : [Micronaut OpenAPI/Swagger Support](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html)

### Préparation de l'environnement de développement

1) Télécharger & dézipper [Apache Jena FUseki](https://jena.apache.org/download/index.cgi).
2) Éxécuter fuseki-server.
3) Créer un dataset : aller sur http://localhost:3030/, onglet « manage dataset », icône « add new dataset », saisir un nom et cocher « *Persistent (TDB2) – dataset will persist across Fuseki restarts* ».
4) Lancer l'application : ```./gradlew run --continuous```.

## Exemples d'interactions avec l'API REST

### Authentification

Obtention & utilisation du *token access* :
```sh
curl -X "POST" "http://localhost:5555/sherlock/api/login" \
  -H 'Content-Type: application/json; charset=utf-8' \
  -d '{ "username": "sherlock", "password": "password" }'
```

Utilisation du *token access* :
```sh
curl -v -i -H "Authorization: Bearer <access_token>>" http://localhost:5555
```

Obtention et utilisation immédiate du *token access* :
```sh
TOKEN=$(curl -s -X POST -H 'Accept: application/json' -H 'Content-Type: application/json' -d '{"username":"sherlock","password":"password"}' http://localhost:5555/sherlock/api/login | jq -r .access_token) ; curl -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN>" http://localhost:5555/sherlock/api/ | jq
```

## Test

- https://objectcomputing.com/files/9815/9259/7089/slide_deck_Micronaut_Testing_Best_Practices_webinar.pdf
- https://stackoverflow.com/questions/64012665/how-to-assert-validate-the-json-body-and-properties-returned-by-a-micronaut-cont