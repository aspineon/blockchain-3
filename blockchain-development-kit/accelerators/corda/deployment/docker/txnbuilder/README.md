Run just the corda-local-network & corda-transaction-builder services 
with UI enabled. This is NOT a full workbench integration stack, but it does 
enable to basic corda integration services through a native UI 

```bash
# to start
docker-compose pull & docker-compose up -d
docker ps 

# to stop
docker-compose down
```

The corda-local-network UI is at http://localhost:1115
The corda-transaction-builder  UI is at http://localhost:1116

# Logspout 

An easy way of tracking docker logs from multiple containers. See also
https://github.com/gliderlabs/logspout

```bash
# to start 
docker run -d --name="logspout" \
	--volume=/var/run/docker.sock:/var/run/docker.sock \
	--publish=127.0.0.1:8000:80 \
	gliderlabs/logspout
	
# monitor logs
curl http://127.0.0.1:8000/logs
```
