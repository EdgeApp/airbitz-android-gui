#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <ABC_android.h>
#include <android/log.h>

/*
 * Return String from ptr to string
 */
JNIEXPORT jstring JNICALL
Java_com_airbitz_api_CoreAPI_getStringAtPtr( JNIEnv *env, jobject obj, jlong ptr )
{
    char *buf = *(char **) &ptr; //*(unsigned int **)&jarg4;
    jstring jresult = (*env)->NewStringUTF(env, buf);
    return jresult;
}

/*
 * Set Wallet order
 */
JNIEXPORT jint JNICALL
Java_com_airbitz_api_CoreAPI_setWalletOrder( JNIEnv *jenv, jobject obj, jstring jarg1, jstring jarg2, jobjectArray stringArray, jlong jarg5 )
{
      jint jresult = 0 ;
      int i;
      char *arg1 = (char *) 0 ;
      char *arg2 = (char *) 0 ;
      char **arg3 = (char **) 0;
      tABC_Error *arg5 = (tABC_Error *) 0 ;
      tABC_CC result;

      arg1 = 0;
      if (jarg1) {
        arg1 = (char *)(*jenv)->GetStringUTFChars(jenv, jarg1, 0);
        if (!arg1) return 0;
      }
      arg2 = 0;
      if (jarg2) {
        arg2 = (char *)(*jenv)->GetStringUTFChars(jenv, jarg2, 0);
        if (!arg2) return 0;
      }

    unsigned int count = (unsigned int) (*jenv)->GetArrayLength(jenv, stringArray);
    const char **param = (const char **) malloc(count*sizeof(const char *));
    for(i = 0; i < count; i++)
    {
        jstring string = (jstring) (*jenv)->GetObjectArrayElement(jenv, stringArray, i);
        param[i] = (*jenv)->GetStringUTFChars(jenv, string, 0);
//        __android_log_write(ANDROID_LOG_INFO, "ABC_android_util", param[i]);
    }


    arg3 = (char **) param;
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "count=%d", count);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "username=%s", arg1);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "password=%s", arg2);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "arg3=%p", arg3);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "param=%p", param);
    arg5 = *(tABC_Error **)&jarg5;
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "arg5=%p", arg5);

    result = (tABC_CC) ABC_SetWalletOrder((char const *)arg1,(char const *)arg2,arg3,count,arg5);

    for (i=0; i<count; i++) {
        jstring string = (jstring) (*jenv)->GetObjectArrayElement(jenv, stringArray, i);
        (*jenv)->ReleaseStringUTFChars(jenv, string, param[i]);
    }
    jresult = (jint)result;

    return jresult;
}

