#ifndef FM_LOG_H
#define FM_LOG_H

#ifdef ANDROID

#include <android/log.h>

#define LOGV(tag, msg, ...) \
    __android_log_print(ANDROID_LOG_VERBOSE, tag, msg, ## __VA_ARGS__)

#define LOGI(tag, msg, ...) \
    __android_log_print(ANDROID_LOG_INFO, tag, msg, ## __VA_ARGS__)

#define LOGD(tag, msg, ...) \
    __android_log_print(ANDROID_LOG_DEBUG, tag, msg, ## __VA_ARGS__)

#define LOGW(tag, msg, ...) \
    __android_log_print(ANDROID_LOG_WARN, tag, msg, ## __VA_ARGS__)

#define LOGE(tag, msg, ...) \
    __android_log_print(ANDROID_LOG_ERROR, tag, msg, ## __VA_ARGS__)

#else

#include <stdio.h>

#define LOGV(tag, msg, ...) printf(tag " " msg "\n", ## __VA_ARGS__)
#define LOGI(tag, msg, ...) printf(tag " " msg "\n", ## __VA_ARGS__)
#define LOGD(tag, msg, ...) printf(tag " " msg "\n", ## __VA_ARGS__)
#define LOGW(tag, msg, ...) printf(tag " " msg "\n", ## __VA_ARGS__)
#define LOGE(tag, msg, ...) printf(tag " " msg "\n", ## __VA_ARGS__)

#endif /* ANDROID */

#endif /* FM_LOG_H */

