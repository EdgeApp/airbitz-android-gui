#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <ABC_android.h>
#include <android/log.h>

/*
 * Asynchronous callback for Transactions
*/
// cached refs for later callbacks
JavaVM * g_vm;
jobject g_obj;
jmethodID g_mid_callback;
void *bitcoinInfo;

void bitcoinCallback(const tABC_AsyncBitCoinInfo *pInfo) {
	JNIEnv * g_env;
	// double check it's all ok
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "Entering bitcoinCallback");
	int getEnvStat = (*g_vm)->GetEnv(g_vm, (void **)&g_env, JNI_VERSION_1_6);
	if (getEnvStat == JNI_EDETACHED) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "GetEnv: not attached");
		if ((*g_vm)->AttachCurrentThread(g_vm, (struct JNINativeInterface const ***) &g_env, NULL) != 0) {
//            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "Failed to attach");
		}
	} else if (getEnvStat == JNI_OK) {
	} else if (getEnvStat == JNI_EVERSION) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "GetEnv: version not supported");
	}

    (*g_env)->CallVoidMethod(g_env, g_obj, g_mid_callback, (void *) pInfo); //walletUUID, txId);

//
//	if ((*g_env)->ExceptionCheck(g_env)) {
//		(*g_env)->ExceptionDescribe(g_env);
//	}

	(*g_vm)->DetachCurrentThread(g_vm);
}

void ABC_BitCoin_Event_Callback(const tABC_AsyncBitCoinInfo *pInfo)
{
    bitcoinCallback(pInfo);
//    if (pInfo->eventType == ABC_AsyncEventType_IncomingBitCoin)
//    {
////        NSString *walletUUID = [NSString stringWithUTF8String:pInfo->szWalletUUID];
////        NSString *txId = [NSString stringWithUTF8String:pInfo->szTxID];
////        NSArray *params = [NSArray arrayWithObjects: walletUUID, txId, nil];
////        [mainId performSelectorOnMainThread:@selector(launchReceiving:) withObject:params waitUntilDone:NO];
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "incoming bitcoin received");
//    } else if (pInfo->eventType == ABC_AsyncEventType_BlockHeightChange) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "Block Height change received");
////        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIFICATION_BLOCK_HEIGHT_CHANGE object:mainId];
//    } else if (pInfo->eventType == ABC_AsyncEventType_ExchangeRateUpdate) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "Exchange Rate Update received");
////        NSLog(@"Exchange rate change fired!!!!!!!!!!!");
////        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIFICATION_EXCHANGE_RATE_CHANGE object:mainId];
//    }
}

// Custom initialization to handle callbacks
JNIEXPORT jint JNICALL
Java_com_airbitz_api_CoreAPI_coreInitialize(JNIEnv *jenv, jclass jcls, jstring jfile, jstring jseed, jlong jseedLength, jlong jerrorp) {
  jint jresult = 0 ;
  char *arg1 = (char *) 0 ;
  tABC_BitCoin_Event_Callback arg2 = (tABC_BitCoin_Event_Callback) 0 ;
  void *arg3 = (void *) 0 ;
  unsigned char *seed = (unsigned char *) 0 ;
  unsigned int seedLength ;
  tABC_Error *errorp = (tABC_Error *) 0 ;
  tABC_CC result;

  (void)jenv;
  (void)jcls;

  arg1 = 0;
  if (jfile) {
    arg1 = (char *)(*jenv)->GetStringUTFChars(jenv, jfile, 0);
    if (!arg1) return 0;
  }
  arg2 = ABC_BitCoin_Event_Callback; // *(tABC_BitCoin_Event_Callback *)&jarg2;
  arg3 = *(void **)&bitcoinInfo;    // holds bitcoinInfo
  seed = 0;
  if (jseed) {
    seed = (unsigned char *)(*jenv)->GetStringUTFChars(jenv, jseed, 0);
    if (!seed) return 0;
  }
  seedLength = (unsigned int)jseedLength;
  errorp = *(tABC_Error **)&jerrorp;
  result = (tABC_CC)ABC_Initialize((char const *)arg1,arg2,arg3,(unsigned char const *)seed,seedLength,errorp);
  jresult = (jint)result;
  if (arg1) (*jenv)->ReleaseStringUTFChars(jenv, jfile, (const char *)arg1);
  if (seed) (*jenv)->ReleaseStringUTFChars(jenv, jseed, (const char *)seed);
  return jresult;
}

JNIEXPORT jboolean JNICALL
Java_com_airbitz_api_CoreAPI_RegisterAsyncCallback (JNIEnv * env, jobject obj)
{
        bool returnValue = true;
		// convert local to global reference
        // (local will die after this method call)
		g_obj = (*env)->NewGlobalRef(env, obj);

		// Save global vm
        int status = (*env)->GetJavaVM(env, &g_vm);
        if(status != 0) {
            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "RegisterAsyncCallback global vm fail");
        }

		// save refs for callback
		jclass g_clazz = (*env)->GetObjectClass(env, g_obj);
		if (g_clazz == NULL) {
            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "RegisterAsyncCallback failed to find class");
		}

		g_mid_callback = (*env)->GetMethodID(env, g_clazz, "callbackAsyncBitcoinInfo", "(J)V");
		if (g_mid_callback == NULL) {
            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "RegisterAsyncCallback unable to get method ref");
		}

		return (jboolean)returnValue;
}


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
 * Return byte array from ptr to string
 */
JNIEXPORT jbyteArray JNICALL
Java_com_airbitz_api_CoreAPI_getBytesAtPtr( JNIEnv *env, jobject obj, jlong ptr , jint len)
{
    jbyteArray result = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion( env, result, 0, len, *(const jbyte **) &ptr );
    return result;
}

JNIEXPORT jlong JNICALL
Java_com_airbitz_api_CoreAPI_TxDetailsGetAmountSatoshi(jlong jarg1) {
  struct sABC_TxDetails *arg1 = (struct sABC_TxDetails *) 0 ;
  int64_t result;
  int result2;

  arg1 = *(struct sABC_TxDetails **)&jarg1;
  result =  ((arg1)->amountSatoshi);
//  result2 = ((arg1)->amountSatoshi);
  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "txdetailsgetamountsatoshi=%lld", result);
  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "txdetailsgetamountsatoshi(hex)=%llx", result);
//  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "txdetailsgetamountsatoshi2=%d", result2);
//  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "txdetailsgetamountsatoshi2(hex)=%x", result2);

  return result;
}

JNIEXPORT jlong JNICALL
Java_com_airbitz_api_CoreAPI_TxDetailsGetAmountFeesAirbitzSatoshi(jlong jarg1) {
  struct sABC_TxDetails *arg1 = (struct sABC_TxDetails *) 0 ;
  int64_t result;

  arg1 = *(struct sABC_TxDetails **)&jarg1;
  result =  ((arg1)->amountFeesAirbitzSatoshi);
//  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "txdetailsgetamountAIRBITZFEESsatoshi=%lld", result);
//  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "txdetailsgetamountAIRBITZFEESsatoshi(hex)=%llx", result);

  return (jlong) result;
}

JNIEXPORT jlong JNICALL
Java_com_airbitz_api_CoreAPI_TxDetailsGetAmountFeesMinersSatoshi(jlong jarg1) {
  struct sABC_TxDetails *arg1 = (struct sABC_TxDetails *) 0 ;
  int64_t result;

  arg1 = *(struct sABC_TxDetails **)&jarg1;
  result =  ((arg1)->amountFeesMinersSatoshi);
//  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "txdetailsgetamountMINERSFEESsatoshi=%lld", result);
//  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "txdetailsgetamountMINERSFEESsatoshi(hex)=%llx", result);

  return (jlong) result;
}


/*
 * Return 64 bit long from pointer
 */
JNIEXPORT jlong JNICALL
Java_com_airbitz_api_CoreAPI_getLongAtPtr(jlong *obj)
{
    return *obj;
}

/*
 * SWIG problem so custom call here
 */
JNIEXPORT void JNICALL
Java_com_airbitz_api_CoreAPI_int64TPAssign(jlong obj, jlong value) {
    int64_t *ptr;
    ptr = (int64_t *) obj; //*(int64_t **)&obj;
    *ptr = value;
}

/*
 * Proper conversion to currency without SWIG problems
*/
JNIEXPORT jint JNICALL
Java_com_airbitz_api_CoreAPI_satoshiToCurrency( JNIEnv *jenv, jobject obj,
    jstring jarg1, jstring jarg2, jlong satoshi, jlong currencyp, jint currencyNumber, jlong error )
{
    tABC_CC result;
    double *arg4;
    arg4 = *(double **)&currencyp;
    int number = (int) currencyNumber;
    int64_t sat = satoshi;
    tABC_Error *argError = (tABC_Error *) 0 ;
    argError = *(tABC_Error **)&error;

      char *username = (char *) 0 ;
      char *password = (char *) 0 ;

      username = 0;
      if (jarg1) {
        username = (char *)(*jenv)->GetStringUTFChars(jenv, jarg1, 0);
        if (!username) return 0;
      }
      password = 0;
      if (jarg2) {
        password = (char *)(*jenv)->GetStringUTFChars(jenv, jarg2, 0);
        if (!password) return 0;
      }
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "username=%s", username);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "password=%s", password);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "unsigned satoshi=%llu", sat);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "hex satoshi=%llx", sat);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "signed satoshi=%lld", sat);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "currencyNumber=%d", currencyNumber);

    result = ABC_SatoshiToCurrency(username, password, sat, arg4, currencyNumber, argError);

//    if(result==0) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "hex currency=%llx", *currencyp);
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "float currency=%f", *currencyp);
//    }
    return result;
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

