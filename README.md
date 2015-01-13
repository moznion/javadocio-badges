javadocio-badges
==

Provides badge of [javadoc.io](http://www.javadoc.io/).

Overview
--

This application is now ready on Heroku.

### Usage

Get page of javadoc.io

    https://javadocio-badges.herokuapp.com/{group_id}/{artifact_id}

Get SVG badge

    https://javadocio-badges.herokuapp.com/{group_id}/{artifact_id}/badge.svg

Example: embedded into markdown

[![javadoc.io](https://javadocio-badges.herokuapp.com/net.moznion/mysql-diff/badge.svg)](https://javadocio-badges.herokuapp.com/net.moznion/mysql-diff)

    [![javadoc.io](https://javadocio-badges.herokuapp.com/net.moznion/mysql-diff/badge.svg)](https://javadocio-badges.herokuapp.com/net.moznion/mysql-diff)

Start the server
--

    mvn jetty:run

Creating war file
--

    mvn package

How to Deploy to Heroku
--

    mvn clean heroku:deploy-war

Author
--

moznion (<moznion@gmail.com>)

License
--

MIT

