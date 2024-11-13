# MYSQL
### create a backup of the data
#### import .env variables (Linux - adjust variables if needed)
export $(cat .env | xargs)

#### execute command
docker exec $CONTAINER_ID sh -c 'exec mysqldump --databases db_swiftion -uroot -p"$MYSQL_ROOT_PASSWORD"' > backup/backup-$(date +"%d-%m-%y").sql

# MONGODB
### create a backup of the data
#### import .env variables (Linux - adjust variables if needed)
export $(cat .env | xargs)

#### execute command, container ID can be found with 'docker container ls' command
docker exec $CONTAINER_ID sh -c 'exec mongodump --username $MONGO_INITDB_ROOT_USERNAME --password $MONGO_INITDB_ROOT_PASSWORD --authenticationDatabase admin -db $MONGODB_DB --archive' > ./backup/backup-mongodb-$(date +"%d-%m-%y").archive