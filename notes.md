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
postgres=# \q
```

Log out as the postgres user by hitting `Ctrl+D`.
You should be back at the terminal as your user

```
myuser@mymachine:
```

Create the database `topictickerdb` with the `topictickeruser` as the owner.

```
$ sudo -u postgres createdb -O topictickeruser topictickerdb
```

You have now created a postgres database called topictickerdb with `topictickeruser` as the owner.

### To undo what you have done:

```
$ dropdb topictickerdb
$ sudo su - postgres
```

You should see something like this

```
$ postgres@my-machine: 
```

Drop the user

```
psql -q -U postgres postgres
postgres=# DROP USER topictickeruser;
```