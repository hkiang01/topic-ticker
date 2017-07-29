
Switch to `postgres` user

```
$ sudo su - postgres
```

You should see something like this

```
$ postgres@my-machine:
```

Create a role with a user and password.
I'm going to create the user `harry` with password `mypassword`.

```
$ psql -q -U postgres postgres
postgres=# CREATE user harry;
postgres=# ALTER USER harry PASSWORD 'mypassword';
postgres=# \q
```

Log out as the postgres user by hitting `Ctrl+D`.
You should be back at the terminal as your user

```
myuser@mymachine:
```

Create the database `topictickerdb` with the user you created as the owner.
I'm going to do so with the owner as `harry`

```
$ sudo -u postgres createdb -O harry topictickerdb
```

You have now created a postgres database called topictickerdb with `user` as the owner.
(In my case, I have created a postgres databse called topictickerdb with `harry` as the owner)

To undo what you have done:

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
postgres=# DROP USER harry;
```