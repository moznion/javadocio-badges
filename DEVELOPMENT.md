Development manual
==

Setup DB
--

```
psql javadocio_badges < src/main/resources/sql/init.db
```

Run tests
--

__CAUTION!__

Test drops DB tables!!

```
DATABASE_URL=postgres://<user_name>@<host>/javadocio_badges_test mvn test
```

