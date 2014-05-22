#include "ABC.h"
#include <android/log.h>

#define  ALOG(...)  __android_log_print(ANDROID_LOG_INFO,"callback jni",__VA_ARGS__)


//typedef void (*callback_t)(int arg, void *data);

//static void *data = NULL;
//static callback_t active = NULL;
//
//static void set(callback_t cb, void *userdata) {
//  active = cb;
//  data = userdata;
//    ALOG("Setting values.");
//}
//
//static void dispatch(int val) {
//    ALOG("Dispatching value %d", val);
//    active(val, data);
//}
//

typedef void (*callback_t)(int arg, void *data);

static void *data = NULL;
static callback_t active = NULL;

static void set(callback_t cb, void *userdata) {
    active = cb;
    data = userdata;
    ALOG("Setting values.");
}

static void dispatch(int val) {
    ALOG("Dispatching value %d", val);
    active(val, data);
}

