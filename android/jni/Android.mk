LOCAL_PATH := $(call my-dir)
LIB_PATH := $(LOCAL_PATH)/../../lib/
EXPAT_PATH := $(LIB_PATH)/expat
CVCONVNET_PATH := $(LIB_PATH)/cvconvnet

include $(CLEAR_VARS)
OPENCV_MK_PATH:=$(HOME)/android-opencv-2.3.1/share/OpenCV/OpenCV.mk
ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
	#try to load OpenCV.mk from default install location
	include $(TOOLCHAIN_PREBUILT_ROOT)/user/share/OpenCV/OpenCV.mk
else
	include $(OPENCV_MK_PATH)
endif

LOCAL_CFLAGS += -Wall -fexceptions -DHAVE_EXPAT_CONFIG_H

LOCAL_C_INCLUDES += \
	$(EXPAT_PATH) \
	$(EXPAT_PATH)/lib \
	$(CVCONVNET_PATH)/include

EXPAT_SRC_PATH=../$(EXPAT_PATH)/lib
CVCONVNET_SRC_PATH=../$(CVCONVNET_PATH)/src

LOCAL_MODULE := FaceCapture
LOCAL_SRC_FILES := \
    $(EXPAT_SRC_PATH)/xmlparse.c \
    $(EXPAT_SRC_PATH)/xmlrole.c \
    $(EXPAT_SRC_PATH)/xmltok.c \
    $(CVCONVNET_SRC_PATH)/cvconvnet.cpp \
    $(CVCONVNET_SRC_PATH)/cvconvolutionplane.cpp \
    $(CVCONVNET_SRC_PATH)/cvgenericplane.cpp \
    $(CVCONVNET_SRC_PATH)/cvmaxplane.cpp \
    $(CVCONVNET_SRC_PATH)/cvregressionplane.cpp \
    $(CVCONVNET_SRC_PATH)/cvsubsamplingplane.cpp \
    $(CVCONVNET_SRC_PATH)/cvconvnetparser.cpp \
    $(CVCONVNET_SRC_PATH)/cvfastsigmoid.cpp \
    $(CVCONVNET_SRC_PATH)/cvmaxoperatorplane.cpp \
    $(CVCONVNET_SRC_PATH)/cvrbfplane.cpp \
    $(CVCONVNET_SRC_PATH)/cvsourceplane.cpp \
    FaceCapture.cpp \
    cnn.cpp

LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)

