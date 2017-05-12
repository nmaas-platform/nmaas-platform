COMMAND=$(ps aux | grep nmaas-platform | grep -v grep | awk '{print $2}')
if [ -z "$COMMAND" ]
then
    echo "ERROR: nmaas-platform is not running"
    exit 13
else
    echo "nmaas-platform started"
fi
