/*
 * Interface file for swig
 */
%module core

%{
#include <assert.h>
#include "callback.h"

// 1:
struct callback_data {
  JNIEnv *env;
  jobject obj;
};

// 2:
void java_callback(int arg, void *ptr) {
  struct callback_data *data = ptr;
  jclass callbackInterfaceClass = (*(data->env))->GetObjectClass(data->env, data->obj);
  /* const jclass callbackInterfaceClass = (*data->env)->FindClass(data->env, "CallbackAsyncBitCoinInfo"); */
  assert(callbackInterfaceClass);
  const jmethodID meth = (*data->env)->GetMethodID(data->env, callbackInterfaceClass, "OnAsyncBitCoinInfo", "(I)V");
  assert(meth);
  (*data->env)->CallVoidMethod(data->env, data->obj, meth, (jint)arg);
}

// 1:
struct callback_data_r {
  JNIEnv *env;
  jobject obj;
};

// 2:
void java_callback_r(int arg, void *ptr) {
  struct callback_data_r *data_r = ptr;
  jclass callbackInterfaceClass_r = (*(data_r->env))->GetObjectClass(data_r->env, data_r->obj);
  assert(callbackInterfaceClass_r);
  const jmethodID meth = (*data_r->env)->GetMethodID(data_r->env, callbackInterfaceClass_r, "OnRequestResults", "(I)V");
  assert(meth);
  (*data_r->env)->CallVoidMethod(data_r->env, data_r->obj, meth, (jint)arg);
}

%}

// 3:
%typemap(jstype) callback_t cb "CallbackAsyncBitCoinInfo";
%typemap(jtype) callback_t cb "CallbackAsyncBitCoinInfo";
%typemap(jni) callback_t cb "jobject";
%typemap(javain) callback_t cb "$javainput";
// 4:
%typemap(in,numinputs=1) (callback_t cb, void *userdata) {
  struct callback_data *data = malloc(sizeof *data);
  data->env = jenv;
  data->obj = JCALL1(NewGlobalRef, jenv, $input);
  JCALL1(DeleteLocalRef, jenv, $input);
  $1 = java_callback;
  $2 = data;
}

// 3:
%typemap(jstype) callback_r cb "CallbackRequestResults";
%typemap(jtype) callback_r cb "CallbackRequestResults";
%typemap(jni) callback_r cb "jobject";
%typemap(javain) callback_r cb "$javainput";
// 4:
%typemap(in,numinputs=1) (callback_r cb, void *userdata) {
  struct callback_data *data_r = malloc(sizeof *data_r);
  data_r->env = jenv;
  data_r->obj = JCALL1(NewGlobalRef, jenv, $input);
  JCALL1(DeleteLocalRef, jenv, $input);
  $1 = java_callback_r;
  $2 = data_r;
}


%include "callback.h"

%include typemaps.i
%apply char * { unsigned char * }

%{
#include "ABC.h"
%}

/* Let's just grab the original header file here */
%include "ABC.h"

