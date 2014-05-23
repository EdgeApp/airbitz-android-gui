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
typedef void (*callback_r)(int arg, void *data);
typedef void (*callback_r_proxy) (int arg);

static void *data = NULL;
static callback_t active = NULL;

static void *data_r = NULL;
static callback_r active_r = NULL;

static void setInfoCallback(callback_t cb, void *userdata) {
    active = cb;
    data = userdata;
//    ALOG("Setting values callback %lu, userdata %lu", active, userdata);
}


static void dispatchInfo(int val) {
//    ALOG("Dispatching value %d", val);
    active(val, data);
}

static long getInfoCallback() {
    return (long) active;
}

static void setRequestCallback(callback_r cb, void *userdata) {
    active_r = cb;
    data_r = userdata;
//    ALOG("Setting values callback %lu, userdata %lu", active, userdata);
}

static callback_r_proxy dispatchRequest(int val) {
//    ALOG("Dispatching value %d", val);
    active_r(val, data_r);
}

static callback_r_proxy getRequestCallback() {
    return dispatchRequest;
}



