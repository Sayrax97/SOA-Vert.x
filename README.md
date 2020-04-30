# SOA-Vert.x

SOA proejkat Vert.x

This project is a built with [Vert.x](https://vertx.io/) as an server API, the UI application is an Angluar app and databse is MySQL.

### Dobrodosli u CycloMeter demo aplikcaiju

### Pravljenu da simulira rad senzora na bicikli

![](homePage.jpg)

## Vert.x

### Local setup

1. Need some IDE or something else that can run and compile gradle
2. open [vertx server](./CycloMeter) via that IDE
3. build it
4. Run [Server.java](./CycloMeter/src/main/java/com/_4infinity/CycloMeter)
5. API is hosted on localhost:1998

## Start UI server [(client)](./client)

```
cd ./client
```

install dependencies and run server

```
npm install
ng serve
```
