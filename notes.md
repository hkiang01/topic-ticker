### Create postgres database
Note: This is tested in Ubuntu 17.04 with admin rights

Install [postgres](https://www.postgresql.org/download/)

Switch to `postgres` user

```
$ sudo su - postgres
```

You should see something like this

```
$ postgres@my-machine:
```

Create the user `topictickeruser` with password `topictickerpassword`.

```
$ psql -q -U postgres postgres
postgres=# CREATE user topictickeruser;
postgres=# ALTER USER topictickeruser PASSWORD 'topictickerpassword';
postgres=# CREATE DATABASE topictickedb OWNER topictickeruser;
```

You have now created a postgres database called topictickerdb with `topictickeruser` as the owner.

### Verification

```
\l
```

You should see something like this

```
                                        List of databases
      Name      |      Owner      | Encoding |   Collate   |    Ctype    |   Access privileges   
----------------+-----------------+----------+-------------+-------------+-----------------------
 postgres       | postgres        | UTF8     | en_US.UTF-8 | en_US.UTF-8 | 
 template0      | postgres        | UTF8     | en_US.UTF-8 | en_US.UTF-8 | =c/postgres          +
                |                 |          |             |             | postgres=CTc/postgres
 template1      | postgres        | UTF8     | en_US.UTF-8 | en_US.UTF-8 | =c/postgres          +
                |                 |          |             |             | postgres=CTc/postgres
 topictickerdb  | topictickeruser | UTF8     | en_US.UTF-8 | en_US.UTF-8 | 
(4 rows)

```

Note that the name of the database of interest is `topictickerdb` and the owner is `topictickeruser`.

### To undo what you have done:

```
postgres=# DROP DATABASE topictickerdb;
postgres=# DROP USER topictickeruser;
```

### Log out of postgres

```
postgres=# \q
postgres@my-machine: 
```

Press `Ctrl+D` on your machine. You should be at your machine's default terminal.