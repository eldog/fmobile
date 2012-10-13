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

n_jobs=$(( `grep 'processor' /proc/cpuinfo | wc -l` ))

android update project -p android/
~/android-ndk-r8b/ndk-build --directory android/ --jobs="${n_jobs}"
ant -buildfile android/build.xml clean uninstall debug install
adb shell am start -n uk.me.eldog.fface/.FaceCaptureActivity
logdog.py

