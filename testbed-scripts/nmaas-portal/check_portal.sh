COMMAND=$(ps aux | grep http-server | grep -v grep | awk '{print $2}')
if [ -z "$COMMAND" ]
then
    echo "ERROR: nmaas-portal is not running"
    exit 13
else
    echo "nmaas-portal started"
fi
