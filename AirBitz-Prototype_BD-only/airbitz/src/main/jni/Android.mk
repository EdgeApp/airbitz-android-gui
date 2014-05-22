LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := airbitz
LOCAL_SRC_FILES := ABC_wrap.c
#LOCAL_SHARED_LIBRARIES := libcore.so
LOCAL_LDFLAGS := -lcore -L.
LOCAL_LDLIBS  := -llog

include $(BUILD_SHARED_LIBRARY)
