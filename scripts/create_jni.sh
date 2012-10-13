#!/bin/bash

set -o errexit
set -o nounset

readonly REPO="$(
    readlink -f -n -- "$(
        dirname -- "$(
            readlink -f "${BASH_SOURCE[0]}"
        )"
    )/../"
)"

cd -- "${REPO}"

ant -buildfile android/ debug
javah \
    -classpath \
    '.:/opt/android-sdk/platforms/android-13/android.jar:android/bin/classes' \
    -d android/jni/ \
    -verbose \
    'uk.me.eldog.fface.ViewScoreActivity'

