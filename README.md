![image](https://cloud.githubusercontent.com/assets/1872314/19116242/0b21b234-8b15-11e6-9a0d-fdb82983fb17.png)
![image](https://i.imgur.com/41jpY0i.jpg)


# Team CarEFuel

##Participant at the InformatiCup 2018

This repository contains the solution of team **CarEFuel** from the Leibniz University in Hannover. This year it is the 13th InformatiCup in a row.

Exhibitor of the [InformatiCup](http://www.informaticup.de) is the [German Informatics Society](https://gi.de).

Task of the IC 2018 was to find a useful gas station strategy. This means an algorithm, which finds the cheapest amounts of gas to fill at each gas station on a fixed gas station route.

This was the basic task and was solved by us within the project carefuel_basic, since we wanted to focus on an extended solution which is more generally and user friendly.

The carefuel_extended project consists of multiple modules:

* Gas Station Price Prediction (Tensorflow)
* Spring Webserver
* PostgreSQL Database System
* CarEFuel Android App (simplistic)

The Tensorflow part can be found in the directory __rnn_training__ containing mainly python files.

The Spring Webserver sources are in the __carefuel_extended__ directory. This also contains some script files to initialize the database correctly.

The CarEFuel Android App code can be also found in the __carefuel_extended__ directory within __CarEFuel_App__.

## The Idea
----
Our main idea was to train a RNN Prediction model with Tensorflow to predict the gas station prices each with different amount of previous price data, since not every gas station has the same amount of data sent to Tankerkönig. Tankerkönig is the provider for the gas station price data and even some more informations about each gas station which do not use yet.

With the prediction model the Spring Application triggerable to predict the gas station prices for the next 30 days. This is intended to be triggered daily by a cronjob e.g. in a Linux environment. This also updates the historical prices delivered by Tankerkönig and afterwards triggers the prediction from the Java Webserver.

The Webserver itself uses the PostgreSQL database to store the historical price data and the predicted price data for each gas station. When started, the webserver provides a website secured by SSL on port 443 and stores the gas station graph in the RAM.

## Documentation
----
The full documentation is written in LaTeX and can be found in the directory __doc__ named **InformatiCup__CarEFuel_Doku**.

## Usage
----
After everything is set up, started and the graph is initiated completely including the calculation of the prediction previously, the website can be called and used quite self explaining. See below.

![image](https://i.imgur.com/Lw31oIy.jpg)

On the left side is the control panel. You can configure the start time of your trip, the start and end position, initial tank capacity, maximum tank capacity and the gas consumption per 100 kilometres. Now just pick which fuel type you are driving and last but not least decide on the mysterious factor X wether you want to drive faster (shorter path) or cheaper (cheaper gas stations on the way). But often this only varies between a few cents. ;)

Mind, that the speed of the path requests depends **very strongly** on the power of the system it is running the Webserver on. It is **strongly** recommended to use a SSD for the database and some dedicated CPU cores. A system with Intel Xeon E3 v1230 with 4 cores + HT and SSD needs about 30-60 seconds for greater path requests.

Also mind, that the Tankerkönig data is restricted on Germany. So if you plan a road trip to france or switzerland you should only request paths until the border of germany.


## Some fine images from German Informatics Society
----
![image](https://cloud.githubusercontent.com/assets/1872314/19118630/4ea5533c-8b1d-11e6-8496-a796adce2001.png)
![image](https://github.com/InformatiCup/InformatiCup2018/raw/master/Aufgabenbeschreibung/fuel_gauge_small.png) ![image](https://github.com/InformatiCup/InformatiCup2018/raw/master/Aufgabenbeschreibung/landmark_small.png)
![image](https://cloud.githubusercontent.com/assets/1872314/19119326/b43d4978-8b1f-11e6-9736-a31f92e75424.png)
![image](https://cloud.githubusercontent.com/assets/1872314/19118952/6e878106-8b1e-11e6-9e3d-0f7dc393d71a.png)
![image](https://cloud.githubusercontent.com/assets/1872314/19183660/a90e3f84-8c79-11e6-9047-b13c02a3290d.png)
![image](https://cloud.githubusercontent.com/assets/1872314/19119143/16a67f04-8b1f-11e6-8b47-0d3510eae0b8.png)

## Sponsores of the InformatiCup
----
* Tankerkönig  
![image](https://creativecommons.tankerkoenig.de/assets/images/TK-Logo-Combined.svg)

* Amazon
* TWT
* PPI