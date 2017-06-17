# topic-ticker

## Getting started
In IntelliJ, open `pom.xml`. A project will be created.

## Secrets
Store API keys in `/src/main/resources/application.conf` (see `sample-application.conf`)

Supported API sources:
* Google News API

## Download sample data

```
chmod +x scripts/ingest-sample-data
./scripts/ingest-sample-data
```

Supported sample data:

* [New York City Taxi & Limousine Commission](http://www.nyc.gov/html/tlc/html/home/home.shtml)
    * Public [data set](https://uofi.app.box.com/NYCtaxidata) about taxi rides in New York City from 2009-2015.
    * More information:https://dataartisans.github.io/flink-training/exercises/taxiData.html
