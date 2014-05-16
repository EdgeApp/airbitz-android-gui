LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := airbitz
LOCAL_SRC_FILES := ABCjni.cpp
#LOCAL_SHARED_LIBRARIES := libstuff.so
LOCAL_LDFLAGS := -lstuff -L.

include $(BUILD_SHARED_LIBRARY)
