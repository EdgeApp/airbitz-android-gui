LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := airbitz
LOCAL_SRC_FILES := ABC_wrap.c ABC_android_util.c
LOCAL_LDLIBS  := -llog -labc

ifeq ($(TARGET_ARCH_ABI),x86)
	LOCAL_LDFLAGS := -L../jniLibs/$(TARGET_ARCH)/
else
	LOCAL_LDFLAGS := -L../jniLibs/armeabi/
endif

include $(BUILD_SHARED_LIBRARY)
