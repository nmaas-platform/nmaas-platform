#!/bin/bash

export APPID=1

for NAME in /nmaas/init/data/apps/*.json; do
  echo
  echo "appid=" $APPID
  # to extract filename including the extension:
  export FILENAME=${NAME##*/}
  # to extract app name from $FILENAME
  export APPNAME=${FILENAME%.*}
  # get real app name from json:
  export REALAPPNAME=$(jq -r '.applicationBase.name' $NAME)
  # check if app name contains spaces, and replace them with %20 for use in curl api call
  if [[ "$REALAPPNAME" == *" "* ]]
  then
    REALAPPNAMESPACE="${REALAPPNAME// /%20}"
    NAMEEXISTS=$(curl --silent --header "Authorization: Bearer $TOKEN" $API_URL/apps/base/name/"$REALAPPNAMESPACE" | jq -r '.name')
  else
    NAMEEXISTS=$(curl --silent --header "Authorization: Bearer $TOKEN" $API_URL/apps/base/name/"$REALAPPNAME" | jq -r '.name')
  fi
	# if the app is not installed
  if [ "$NAMEEXISTS" != "$REALAPPNAME" ]
  then
		echo "app id=" $APPID
		echo "Adding " $REALAPPNAME
		# to find out the app logo file type:
    for IMAGETYPE in /nmaas/init/data/apps/images/logo/$APPNAME.*; do
     	EXTENSION="${IMAGETYPE##*.}"
	    if [ "$EXTENSION" = "svg" ]; then
        CONTENT_TYPE="image/svg+xml"
      elif [ "$EXTENSION" = "png" ]; then
	      CONTENT_TYPE="image/png"
      elif [ "$EXTENSION" = "jpg" ] || [ "$EXTENSION" = "jpeg" ]; then
        CONTENT_TYPE="image/jpeg"
			elif [ "$EXTENSION" = "webp" ] ; then
				CONTENT_TYPE="image/webp"
	    fi
    done
		# add app from json:
    curl --silent -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @$NAME
		# add logo
		curl --silent -X POST --header "Authorization: Bearer $TOKEN" -F "file=@/nmaas/init/data/apps/images/logo/$APPNAME.$EXTENSION;type=$CONTENT_TYPE" $API_URL/apps/$APPID/logo
		# add all screenshots
		for SCREENSHOT in /nmaas/init/data/apps/images/screenshots/$APPNAME/*; do
			curl --silent -X POST --header "Authorization: Bearer $TOKEN" -F "file=@/nmaas/init/data/apps/images/screenshots/$APPNAME/"${SCREENSHOT##*/}";type=image/png" $API_URL/apps/$APPID/screenshots
		done
		# activate the app "
		curl --silent -X PATCH $API_URL/apps/state/$APPID --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @/nmaas/init/data/apps/activations/active.json
	else
		echo $REALAPPNAME "already exists"
	fi
  APPID=$((APPID+1))
done
