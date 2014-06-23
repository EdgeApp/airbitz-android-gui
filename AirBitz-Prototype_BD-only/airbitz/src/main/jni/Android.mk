LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := airbitz
LOCAL_SRC_FILES := ABC_wrap.c ABC_android_util.c
#LOCAL_SHARED_LIBRARIES := libabc.so
LOCAL_LDFLAGS := -labc -L.
LOCAL_LDLIBS  := -llog

include $(BUILD_SHARED_LIBRARY)
